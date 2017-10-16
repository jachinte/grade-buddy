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

import com.rigiresearch.markinghelper.model.Submission;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * A file-based submission list provider.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-14
 * @version $Id$
 * @since 0.0.1
 */
@Accessors(fluent = true)
@AllArgsConstructor
@Getter
public final class FileSubmissionProvider {

    /**
     * The directory containing the submissions.
     */
    private final File directory;

    /**
     * An exclusion criteria.
     */
    private final String exclusionRegexp;

    /**
     * The shell script to give each submission an identifier.
     */
    private final File namingScript;

    /**
     * Lists the submission directories as {@link Submission} instances.
     * @return an iterable of submissions.
     */
    public List<Submission> submissions() {
        return Stream.of(this.directories())
            .map(directory -> {
                final Submission submission = new Submission(directory);
                try {
                    submission.studentId(
                        new FileIdProvider(
                            directory,
                            this.namingScript
                        ).studentId()
                    );
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    System.exit(4);
                }
                return submission;
            })
            .collect(Collectors.toList());
    }

    /**
     * List submission directories filtering out exclusions.
     * @return an array of submission directories
     */
    public File[] directories() {
        return this.directory.listFiles(file -> {
            return file.isDirectory() &&
                    !file.getName().matches(this.exclusionRegexp);
            }
        );
    }

}
