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
package com.rigiresearch.markinghelper.ui;

import com.rigiresearch.markinghelper.model.CsvReport;
import com.rigiresearch.markinghelper.model.Submission;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
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
     * The submissions.
     */
    private final List<Submission> submissions;

    /**
     * The tool bar component.
     */
    private JToolBar toolBar;

    /**
     * Default constructor.
     */
    public Toolbar(final List<Submission> submissions) {
        super(new BorderLayout());
        this.submissions = submissions;
        this.initialize();
    }

    private void initialize() {
        JButton export = new JButton("Export Report");
        export.setActionCommand("export");
        export.addActionListener(this);
        this.toolBar = new JToolBar("Tools");
        this.toolBar.add(export);
        this.add(toolBar, BorderLayout.PAGE_START);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "export") {
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {
                File file = new File(fc.getSelectedFile(), "report.csv");
                try {
                    Files.write(
                        Paths.get(file.getAbsolutePath()),
                        new CsvReport(this.submissions).report().getBytes()
                    );
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(
                        this, e1.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

}
