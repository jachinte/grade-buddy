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

import com.rigiresearch.gradebuddy.io.AutomatedMarking;
import com.rigiresearch.gradebuddy.model.CsvReport;
import com.rigiresearch.gradebuddy.model.Submission;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * A simple tool bar.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-14
 * @version $Id$
 * @since 0.0.1
 */
@Accessors(fluent = true)
@Getter
public final class Toolbar extends JPanel implements ActionListener {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -4239376554933265490L;

    /**
     * The submission table.
     */
    private final SubmissionTable table;

    /**
     * The marking object.
     */
    private final AutomatedMarking marking;

    /**
     * A marking call-back.
     */
    private final Function<Submission, Object> onMarking;

    /**
     * The tool bar component.
     */
    private JToolBar toolBar;

    /**
     * The current selected row index.
     */
    private int selectedRow = -1;

    /**
     * Default constructor.
     * @param table The submission table
     * @param marking The marking object
     * @param onMarking A marking call-back
     */
    public Toolbar(final SubmissionTable table,
        final AutomatedMarking marking,
        final Function<Submission, Object> onMarking) {
        super(new BorderLayout());
        this.table = table;
        this.marking = marking;
        this.onMarking = onMarking;
        this.initialize();
        this.configure();
    }

    /**
     * Initialize graphical components.
     */
    private void initialize() {
        JButton backup = new JButton("Save backup");
        backup.setActionCommand("save");
        backup.addActionListener(this);
        JButton export = new JButton("Export Report");
        export.setActionCommand("export");
        export.addActionListener(this);
        JButton mark = new JButton("Re-Mark Submission");
        mark.setActionCommand("mark");
        mark.addActionListener(this);
        this.toolBar = new JToolBar("Tools");
        this.toolBar.add(backup);
        this.toolBar.add(export);
        this.toolBar.add(mark);
        this.add(toolBar, BorderLayout.PAGE_START);
    }

    /**
     * Configure call-backs.
     */
    private void configure() {
        this.table.getSelectionModel()
            .addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    final ListSelectionModel model =
                        (ListSelectionModel) e.getSource();
                    Toolbar.this.selectedRow = model.getMinSelectionIndex();
                }
            });
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     *  #actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "save") {
            this.saveBackup();
        } else if (e.getActionCommand() == "export") {
            this.export();
        } else if (e.getActionCommand() == "mark") {
            this.markSubmission();
        }
    }

    /**
     * Saves a backup file.
     */
    private void saveBackup() {
        boolean error = false;
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {
            final File file = new File(fc.getSelectedFile(), "marking.backup");
            ObjectOutputStream stream = null;
            try {
                stream = new ObjectOutputStream(
                    new FileOutputStream(file)
                );
                stream.writeObject(this.marking);
            } catch (Exception e) {
                error = true;
                e.printStackTrace();
            } finally {
                try {
                    if (stream != null) {
                        stream.flush();
                        stream.close();
                    }
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
        if (error) {
            JOptionPane.showMessageDialog(
                this,
                "Error writing backup file. Please read the error output.",
                "Backup error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Export the report.
     */
    private void export() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {
            final File file = new File(fc.getSelectedFile(), "report.csv");
            try {
                Files.write(
                    Paths.get(file.getAbsolutePath()),
                    new CsvReport(this.marking.submissions())
                        .report(true)
                        .getBytes()
                );
            } catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(
                    this, e1.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Trigger submission marking.
     */
    private void markSubmission() {
        if (this.selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a submission on the table.",
                "Marking error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        final Submission submission = this.marking.submissions()
            .get(this.selectedRow);
        try {
            submission.results(
                this.marking.markingResults(
                    submission.directory()
                )
            );
            this.table.triggerRowUpdate(submission);
            this.onMarking.apply(submission);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
