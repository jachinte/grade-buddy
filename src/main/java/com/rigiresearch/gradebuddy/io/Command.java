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
package com.rigiresearch.gradebuddy.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a shell command.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-26
 * @version $Id$
 * @since 0.0.1
 */
@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public final class Command implements Serializable {

    @Accessors(fluent = true)
    @RequiredArgsConstructor
    @Getter
    public static class Result implements Serializable {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = -6392437466178740568L;

        /**
         * The command exit code.
         */
        private final int exitCode;

        /**
         * The standard output stream.
         */
        private final ByteArrayOutputStream outputStream;

        /**
         * The standard error stream.
         */
        private final ByteArrayOutputStream errorStream;
    }

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -4806469868343750558L;

    /**
     * The command parts.
     */
    private final String[] parts;

    /**
     * The execution directory.
     */
    private File directory;

    /**
     * The execution results;
     */
    private Result result;

    /**
     * Sets a directory.
     * @param directory The directory
     * @return This command
     */
    public Command onDirectory(final File directory) {
        this.directory = directory;
        return this;
    }

    /**
     * Executes this command.
     * @param timeout The allowed timeout
     * @param unit A time unit
     * @return This command
     * @throws TimeoutException See {@link ProcessExecutor#execute()}
     * @throws InvalidExitValueException See {@link ProcessExecutor#execute()}
     * @throws IOException See {@link ProcessExecutor#execute()}
     * @throws InterruptedException See {@link ProcessExecutor#execute()}
     */
    public Command execute(long timeout, TimeUnit unit)
        throws TimeoutException, InvalidExitValueException, IOException,
            InterruptedException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        final int exitCode = new ProcessExecutor()
            .environment(System.getenv())
            .directory(this.directory)
            .command(this.parts)
            .timeout(timeout, unit)
            .redirectOutput(outputStream)
            .redirectError(errorStream)
            .readOutput(true)
            .execute()
            .getExitValue();
        this.result = new Result(
            exitCode,
            outputStream,
            errorStream
        );
        return this;
    }

    /**
     * Executes this command with a timeout of 1min.
     * @return This command
     * @throws TimeoutException See {@link ProcessExecutor#execute()}
     * @throws InvalidExitValueException See {@link ProcessExecutor#execute()}
     * @throws IOException See {@link ProcessExecutor#execute()}
     * @throws InterruptedException See {@link ProcessExecutor#execute()}
     */
    public Command execute() throws InvalidExitValueException,
        TimeoutException, IOException, InterruptedException {
        this.execute(60, TimeUnit.SECONDS);
        return this;
    }

}
