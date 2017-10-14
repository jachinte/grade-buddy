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
package com.rigiresearch.markinghelper.io;

import com.rigiresearch.markinghelper.model.Result;
import com.rigiresearch.markinghelper.model.Submission;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.zeroturnaround.exec.ProcessExecutor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * This class is in charge of coordinating the automated marking.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-13
 * @version $Id$
 * @since 0.0.1
 */
@Accessors(fluent = true)
@AllArgsConstructor
@Getter
public final class AutomatedMarking {

    /**
     * The collection of submissions.
     */
    private final List<Submission> submissions;

    /**
     * The shell scripts (one per assignment part) to run over the submissions.
     */
    private final List<File> scripts;

    /**
     * Marks all of the submissions.
     */
    public void mark() throws Exception {
        for (Submission submission : this.submissions) {
            submission.results(this.markingResults(submission.directory()));
        }
    }

    /**
     * Runs the assignment shell scripts on the specified submission and return
     * the corresponding marking results.
     * @param submission The submission to mark
     * @return The marking results
     * @throws Exception If something bad happens when running the scripts
     */
    public List<Result> markingResults(final File submission)
        throws Exception {
        List<Result> results = new ArrayList<>();
        for (File script : this.scripts) {
            results.add(this.markingResult(submission, script));
        }
        return results;
    }

    /**
     * Runs the shell script on the specified submission and return the
     * corresponding marks.
     * @param submission The submission to mark
     * @param script The marking script
     * @return The marking result
     * @throws Exception If something bad happens when running the script
     */
    public Result markingResult(final File submission, final File script)
        throws Exception {
        File file = new File("");
        double marks = 0d;
        String feedback = "";
        String output = "";
        try {
            final ByteArrayOutputStream stdOutput = new ByteArrayOutputStream();
            final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
            final int exitCode = new ProcessExecutor()
                .environment(System.getenv())
                .directory(script.getParentFile())
                .command("sh", script.getName(), submission.getAbsolutePath())
                .timeout(60, TimeUnit.SECONDS)
                .redirectOutput(stdOutput)
                .redirectError(stdErr)
                .readOutput(true)
                .execute()
                .getExitValue();
            final Result r = this.handleOutput(
                exitCode,
                stdOutput.toString(),
                stdErr.toString()
            );
            file = r.markedFile();
            marks = r.marks();
            feedback = r.feedback();
            output = r.output();
        } catch (TimeoutException e) {
            feedback = "Timeout while trying to mark the submission";
        }
        return new Result(file, marks, feedback, output);
    }

    /**
     * Determines the marks and feedback from the script's output.
     * @param exitCode The exit code returned by the marking script
     * @param stdOutput The output text from the marking script execution
     * @param stdErr The error text from the marking script execution
     * @return The marking result
     * @throws Exception If something went wrong while executing the script
     */
    private Result handleOutput(final int exitCode, final String stdOutput,
        final String stdErr) throws Exception {
        File file = new File("");
        double marks = 0d;
        String feedback = "";
        String output = "";
        if (exitCode != 0) {
            feedback = String.format(
                "The marking script returned a non-zero code (%d)."
                + "\nOutput stream: %s\nError stream: %s",
                exitCode,
                stdOutput,
                stdErr
            );
        } else {
            final Pattern pattern = Pattern.compile(
                String.format(
                    "%s%s%s%s",
                    "^(\\d*\\.\\d+|\\d+\\.\\d*)[\\s\\S]", // the marks
                    "(.*)[\\s\\S]", // the marked file
                    "(.*)[\\s\\S]", // the feedback
                    "([.\\s\\S]*)$" // the program's output
                )
            );
            final Matcher matcher = pattern.matcher(stdOutput.toString());
            if (matcher.find()) {
                marks = Double.parseDouble(matcher.group(1));
                file = new File(matcher.group(2));
                feedback = matcher.group(3);
                output = matcher.group(4);
            } else {
                throw new Exception(
                    String.format(
                        "Output from marking script does not follow "
                        + "expected output.\nActual output: %s",
                        stdOutput.toString()
                    )
                );
            }
        }
        return new Result(file, marks, feedback, output);
    }

}
