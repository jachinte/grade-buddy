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
package com.rigiresearch.markinghelper.model;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Generates a simple report in CSV format.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-13
 * @version $Id$
 * @since 0.0.1
 */
@Accessors(fluent = true)
@AllArgsConstructor
@Getter
public final class CsvReport {

    /**
     * The submissions to include in the report.
     */
    private final Iterable<Submission> submissions;

    /**
     * The CSV report.
     * @return A valid CSV string
     */
    public String report() {
        StringBuilder builder = new StringBuilder();
        builder.append("StudentId,Marks,Feedback,Directory\n");
        submissions.forEach(submission -> {
            double marks = 0d;
            String feedback = new String();
            final AtomicInteger i = new AtomicInteger(1);
            for (Result result : submission.results()) {
                marks += result.marks();
                feedback += String.format(
                    "PART %d (%s): %s\n",
                    i.getAndIncrement(),
                    result.markedFile().getName(),
                    result.feedback().isEmpty() ?
                        "No feedback provided" : result.feedback()
                );
            }
            builder.append(this.escape(submission.studentId()));
            builder.append(",");
            builder.append(marks);
            builder.append(",");
            builder.append(this.escape(feedback));
            builder.append(",");
            builder.append(submission.directory().getAbsolutePath());
            builder.append("\n");
        });
        return builder.toString();
    }

    /**
     * Escapes text.
     * @param text The text to escape
     * @return Escaped text
     */
    private String escape(final String text) {
        return String.format(
            "\"%s\"",
            text.replace(
                "\"",
                "'"
            )
        );
    }
}
