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
package com.rigiresearch.gradebuddy.ui;

import com.rigiresearch.gradebuddy.model.Submission;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A table model to display submissions and handle updates.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-14
 * @version $Id$
 * @since 0.0.1
 */
@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public final class SubmissionTableModel extends AbstractTableModel {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -6845147945555926643L;

    /**
     * The list of submissions.
     */
    private final List<Submission> submissions;

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return this.submissions.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        // Student id + results * (marks, feedback)
        return 1 + this.submissions.get(0).results().size() * 2;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0)
            return "Student ID";
        else if (columnIndex % 2 != 0)
            return String.format("Marks (P%d)", columnIndex/2 + 1);
        else {
            return String.format("Feedback (P%d)", columnIndex/2);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        final Submission submission = this.submissions.get(rowIndex);
        if (columnIndex == 0)
            return submission.studentId();
        else if (columnIndex % 2 != 0)
            return submission.results().get(columnIndex/2).marks();
        else {
            return submission.results().get(columnIndex/2 - 1).feedback();
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        final Submission submission = this.submissions.get(rowIndex);
        if (columnIndex % 2 != 0) {
            submission.results()
                .get(columnIndex/2)
                .marks(Double.valueOf(value.toString()));
        } else {
            submission.results()
                .get(columnIndex/2 - 1)
                .feedback(value.toString());
        }
        this.fireTableCellUpdated(rowIndex, columnIndex);
    }

}
