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
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Displays submission information in a table format.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-14
 * @version $Id$
 * @since 0.0.1
 */
@Accessors(fluent = true)
@Getter
public final class SubmissionTable extends JTable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 5339091495287909084L;

    /**
     * The submissions.
     */
    private final List<Submission> submissions;

    /**
     * Default constructor.
     */
    public SubmissionTable(final List<Submission> submissions) {
        this.submissions = submissions;
        this.setModel(new SubmissionTableModel(this.submissions));
        for (int i = 0; i < this.getColumnModel().getColumnCount(); i++) {
            final TableColumn column = getColumnModel().getColumn(i);
            if (i % 2 == 0) {
                column.setCellRenderer(new TextAreaCellRenderer());
                column.setCellEditor(new TextAreaCellEditor());
            }
        }
        this.updateRowDimensions();
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
     */
    @Override
    public String getToolTipText(MouseEvent e) {
        final int rowIndex = rowAtPoint(e.getPoint());
        final int columnIndex = columnAtPoint(e.getPoint());
        if (rowIndex > -1 && columnIndex == 0)
            return this.submissions.get(rowIndex)
                .directory()
                .getAbsolutePath();
        return super.getToolTipText(e);
    }

    /**
     * Updates height and width of columns in all rows.
     */
    public void updateRowDimensions() {
        for (int row = 0; row < this.getRowCount(); row++) {
            this.updateRowDimension(row);
        }
    }

    /**
     * Updates height and width of columns in a particular row.
     * @param row The row to update
     */
    public void updateRowDimension(final int row) {
        int rowHeight = this.getRowHeight();
        for (int column = 0; column < this.getColumnCount(); column++) {
            Component c = this.prepareRenderer(
                this.getCellRenderer(row, column),
                row,
                column
            );
            rowHeight = Math.max(rowHeight, c.getPreferredSize().height);
            this.getColumnModel()
                .getColumn(column)
                .setPreferredWidth(c.getPreferredSize().width);
        }
        this.setRowHeight(row, rowHeight);
    }
}
