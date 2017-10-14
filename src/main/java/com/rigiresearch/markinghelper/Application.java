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
package com.rigiresearch.markinghelper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.rigiresearch.markinghelper.io.IdProvider;
import com.rigiresearch.markinghelper.io.Marker;
import com.rigiresearch.markinghelper.model.CsvReport;
import com.rigiresearch.markinghelper.model.Submission;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        names = {"--directory", "-d"},
        description = "The directory containing the assignment submissions",
        required = true,
        order = 1
    )
    private String directory;

    @Parameter(
        names = {"--marking-script", "-m"},
        description = "A shell script to run over each submission",
        required = true,
        order = 2
    )
    private String markingScript;

    @Parameter(
        names = {"--naming-script", "-n"},
        description = "A shell script to extract the submission's id",
        required = true,
        order = 3
    )
    private String namingScript;

    @Parameter(
        names = {"--exclude", "-e"},
        description = "Regular expression to exclude directories",
        order = 4
    )
    private String exclusionRegexp = "";

    @Parameter(
        names = {"--ui", "-u"},
        description = "Open the graphical user interface",
        order = 5
    )
    private boolean ui = false;

    @Parameter(
        names = {"--help", "-h"},
        description = "Shows this message",
        order = 6
    )
    private boolean help = false;

    public static void main(String[] args) {
        Application app = new Application();
        try {
            JCommander jc = JCommander.newBuilder()
                .addObject(app)
                .build();
            jc.setProgramName("<program>");
            jc.parse(args);
            if (app.help) {
                jc.usage();
                return;
            } else if (!app.parameters.isEmpty()) {
                System.err.printf("Unknown parameter(s) %s\n", app.parameters);
                System.exit(1);
            }
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            new JCommander(new Application()).usage();
            System.exit(2);
        }
        app.run();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // list submission directories filtering out exclusions
        final File[] directories = new File(this.directory).listFiles(file -> {
            return file.isDirectory() &&
                !file.getName().matches(this.exclusionRegexp);
        });
        // map directories to submission instances
        final Iterable<Submission> submissions = Stream.of(directories)
            .map(directory -> {
                final Submission submission = new Submission(directory);
                try {
                    submission.studentId(
                        new IdProvider(
                            directory,
                            new File(this.namingScript)
                        ).studentId()
                    );
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    System.exit(3);
                }
                return submission;
            })
            .collect(Collectors.toList());
        final Marker marker = new Marker(submissions, new File(this.markingScript));
        if (this.ui) {
            // new MarkingWindow(marker);
            System.out.println("Openning UI for semi-automatic grading");
        } else {
            marker.markAll();
            System.out.println(new CsvReport(submissions).report());
        }
    }

}
