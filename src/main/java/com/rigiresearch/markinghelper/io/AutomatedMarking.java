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
    private final Iterable<Submission> submissions;

    /**
     * The shell scripts (one per assignment part) to run over the submissions.
     */
    private final List<File> scripts;

    /**
     * Marks all of the submissions.
     */
    public void mark() throws Exception {
        for (Submission submission : this.submissions) {
            submission.result(this.markingResult(submission.directory()));
        }
    }

    /**
     * Runs the assignment shell scripts on the specified submission and return
     * the sum of the corresponding marks.
     * @param submission The submission to mark
     * @return The marking result
     * @throws Exception If something bad happens when running the script
     */
    public Result markingResult(final File submission) throws Exception {
        double marks = 0d;
        String feedback = new String();
        for (File script : this.scripts) {
            final Result r = this.markingResult(submission, script);
            marks += r.marks();
            feedback += String.format("%s\n", r.feedback());
        }
        return new Result(marks, feedback);
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
        double marks = 0d;
        String feedback = "";
        try {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final ByteArrayOutputStream errOutput = new ByteArrayOutputStream();
            final int exitCode = new ProcessExecutor()
                .directory(script.getParentFile())
                .command("sh", script.getName(), submission.getAbsolutePath())
                .timeout(60, TimeUnit.SECONDS)
                .redirectOutput(output)
                .redirectError(errOutput)
                .readOutput(true)
                .execute()
                .getExitValue();
            final Result r = this.handleOutput(
                exitCode,
                output.toString(),
                errOutput.toString()
            );
            marks = r.marks();
            feedback = r.feedback();
        } catch (TimeoutException e) {
            feedback = "Timeout while trying to mark the submission";
        }
        return new Result(marks, feedback);
    }

    /**
     * Determines the marks and feedback from the script's output.
     * @param exitCode The exit code returned by the marking script
     * @param output The output text from the marking script execution
     * @param errOutput The error text from the marking script execution
     * @return The marking result
     * @throws Exception If something went wrong while executing the script
     */
    private Result handleOutput(final int exitCode, final String output,
        final String errOutput) throws Exception {
        double marks = 0d;
        String feedback = "";
        if (exitCode != 0) {
            feedback = String.format(
                "The marking script returned a non-zero code (%d)."
                + "\nOutput stream: %s\nError stream: %s",
                exitCode,
                output,
                errOutput
            );
        } else {
            final Pattern pattern = Pattern.compile("^(\\d*\\.\\d+|\\d+\\.\\d*)([.\\s\\S]*)$");
            final Matcher matcher = pattern.matcher(output.toString());
            if (matcher.find()) {
                marks = Double.parseDouble(matcher.group(1));
                feedback = matcher.group(2).trim();
            } else {
                throw new Exception(
                    String.format(
                        "Output from marking script does not follow "
                        + "expected output.\nActual output: %s",
                        output.toString()
                    )
                );
            }
        }
        return new Result(marks, feedback);
    }

}
