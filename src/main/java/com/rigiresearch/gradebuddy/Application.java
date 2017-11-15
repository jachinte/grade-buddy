/**
 * Copyright 2017 University of Victoria
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.rigiresearch.gradebuddy;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.rigiresearch.gradebuddy.io.AutomatedMarking;
import com.rigiresearch.gradebuddy.io.FileSubmissionProvider;
import com.rigiresearch.gradebuddy.model.CsvReport;
import com.rigiresearch.gradebuddy.model.Submission;
import com.rigiresearch.gradebuddy.ui.MainWindow;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Main entry class.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-13
 * @version $Id$
 * @since 0.0.1
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class Application implements Runnable {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(
        names = {"--backup", "-b"},
        description = "A backup file containing a previous configuration",
        order = 1
    )
    private String backup;

    @Parameter(
        names = {"--directory", "-d"},
        description = "The directory containing the assignment submissions",
        order = 2
    )
    private String directory;

    @Parameter(
        names = {"--marking-script", "-m"},
        description = "A shell script to run over each submission",
        order = 3
    )
    private List<String> markingScripts = new ArrayList<>();

    @Parameter(
        names = {"--naming-script", "-n"},
        description = "A shell script to extract the submission's id",
        order = 4
    )
    private String namingScript;

    @Parameter(
        names = {"--on-selected-script", "-s"},
        description = "A shell script to be executed when a submission is "
                + "selected (useful only when running the UI)",
        order = 5
    )
    private String onSelectedScript;

    @Parameter(
        names = {"--exclude", "-e"},
        description = "Regular expression to exclude directories",
        order = 6
    )
    private String exclusionRegexp = "";

    @Parameter(
        names = {"--ui", "-u"},
        description = "Open the graphical user interface",
        order = 7
    )
    private boolean ui = false;

    @Parameter(
        names = {"--timeout", "-to"},
        description = "The timeout for each submission part (in milliseconds)",
        order = 8
    )
    private int timeout = 60000;

    @Parameter(
        names = {"--thread-pool", "-t"},
        description = "The thread-pool size to use in marking the submissions",
        order = 9
    )
    private int threads = 1;

    @Parameter(
        names = {"--help", "-h"},
        description = "Shows this message",
        order = 10
    )
    private boolean help = false;

    /**
     * Main method.
     * @param args This program's arguments
     */
    public static void main(String[] args) {
        Application app = new Application();
        try {
            JCommander jc = JCommander.newBuilder()
                .addObject(app)
                .programName("<program>")
                .build();
            jc.parse(args);
            if (app.help) {
                jc.usage();
                return;
            } else if (!app.parameters.isEmpty()) {
                System.err.printf("Unknown parameter(s) %s\n", app.parameters);
                System.exit(1);
            } else if (app.backup == null && (
                app.directory == null
                || app.namingScript == null
                || app.markingScripts.isEmpty())) {
                System.err.println(
                    "Expecting parameters Directory, Naming script, and "
                    + "Marking script"
                );
                jc.usage();
                System.exit(1);
            }
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            new JCommander(new Application()).usage();
            System.exit(2);
        }
        app.run();
    }

    /**
     * Validates that all input paths exist.
     */
    public void validateArguments() {
        final List<String> paths = new ArrayList<>();
        if (this.onSelectedScript != null)
            paths.add(this.onSelectedScript);
        if (this.backup != null) {
            paths.add(this.backup);
        } else {
            paths.add(this.directory);
            paths.add(this.namingScript);
            paths.addAll(this.markingScripts);
        }
        paths.stream().forEach(path -> {
            if (!new File(path).exists()) {
                System.err.printf("Input path '%s' does not exist\n", path);
                System.exit(3);
            }
        });
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        validateArguments();
        AutomatedMarking marker;
        try {
            if (this.backup != null) {
                marker = this.loadBackup();
            } else {
                List<Submission> submissions = new FileSubmissionProvider(
                    new File(this.directory),
                    this.exclusionRegexp,
                    new File(this.namingScript)
                ).submissions();
                marker = new AutomatedMarking(
                    submissions,
                    this.markingScripts.stream()
                        .map(script -> new File(script))
                        .collect(Collectors.toList()),
                    this.timeout,
                    TimeUnit.MILLISECONDS
                );
                marker.mark(this.threads);
            }
            if (this.ui) {
                final MainWindow window = new MainWindow(marker);
                if (this.onSelectedScript != null)
                    window.selectionScript(new File(this.onSelectedScript));
                window.configure();
            } else {
                System.out.println(
                    new CsvReport(marker.submissions()).report(true)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(5);
        }
    }

    /**
     * Loads a backup file.
     * @return a marking object.
     * @throws IOException If there is an I/O error
     * @throws ClassNotFoundException If the saved class is not found
     */
    private AutomatedMarking loadBackup()
        throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(
            new FileInputStream(
                new File(this.backup)
            )
        );
        final AutomatedMarking marking =
            (AutomatedMarking) stream.readObject();
        stream.close();
        return marking;
    }

}
