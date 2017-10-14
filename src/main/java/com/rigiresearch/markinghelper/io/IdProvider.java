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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.zeroturnaround.exec.ProcessExecutor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Provides the student identifier from an assignment submission.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-13
 * @version $Id$
 * @since 0.0.1
 */
@Accessors(fluent = true)
@AllArgsConstructor
@Getter
public final class IdProvider {

    /**
     * The submission directory.
     */
    private final File directory;

    /**
     * The shell script to extract the student identifier.
     */
    private final File script;

    /**
     * Runs the shell script to extract the student identifier from the
     * submission.
     * @return The student identifier
     * @throws Exception If something bad happens while executing the script
     */
    public String studentId() throws Exception {
        String identifier = "";
        try {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final ByteArrayOutputStream errOutput = new ByteArrayOutputStream();
            final int exitCode = new ProcessExecutor()
                .environment(System.getenv())
                .directory(this.script.getParentFile())
                .command("sh", this.script.getName(), directory.getAbsolutePath())
                .timeout(60, TimeUnit.SECONDS)
                .redirectOutput(output)
                .redirectError(errOutput)
                .readOutput(true)
                .execute()
                .getExitValue();
            identifier = this.handleOutput(
                exitCode,
                output.toString(),
                errOutput.toString()
            );
        } catch (TimeoutException e) {
            throw new Exception(
                String.format(
                    "Timeout while trying to extract the student identifier"
                    + " from submission %s",
                    this.directory
                )
            );
        }
        return identifier;
    }

    /**
     * Determines the student's identifier from the script's output.
     * @param exitCode The exit code returned by the script
     * @param output The output text from the script execution
     * @param errOutput The error text from the script execution
     * @return The student identifier
     * @throws Exception If something went wrong while executing the script
     */
    private String handleOutput(final int exitCode, final String output,
        final String errOutput) throws Exception {
        if (exitCode != 0) {
            throw new Exception(
                String.format(
                    "The script returned a non-zero code (%d)."
                    + "\nOutput stream: %s\nError stream: %s",
                    exitCode,
                    output,
                    errOutput
                )
            );
        }
        return output.toString().trim();
    }

}
