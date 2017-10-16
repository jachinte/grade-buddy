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

import com.rigiresearch.markinghelper.io.AutomatedMarking;
import com.rigiresearch.markinghelper.model.Result;
import com.rigiresearch.markinghelper.model.Submission;
import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import bibliothek.gui.DockController;
import bibliothek.gui.dock.DefaultDockable;
import bibliothek.gui.dock.SplitDockStation;
import bibliothek.gui.dock.station.split.SplitDockGrid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The main window.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-14
 * @version $Id$
 * @since 0.0.1
 */
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class MainWindow extends JFrame {

    class SelectionListener implements ListSelectionListener {

        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener
         *  #valueChanged(javax.swing.event.ListSelectionEvent)
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel selectionModel =
                (ListSelectionModel) e.getSource();
            try {
                MainWindow.this.displayData(
                    MainWindow.this.marking.submissions()
                        .get(selectionModel.getMinSelectionIndex())
                );
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        
    }

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 7484838982331305217L;

    /**
     * The marking object.
     */
    private final AutomatedMarking marking;

    /**
     * Text area to display code.
     */
    @Getter
    private RSyntaxTextArea sourceTextArea;

    /**
     * Text area to display programs' output;
     */
    @Getter
    private JTextArea outputTextArea;

    /**
     * The submissions table.
     */
    @Getter
    private SubmissionTable table;

    /**
     * Configures this window.
     * @throws IOException 
     */
    public void configure() throws IOException {
        this.setLayout(new BorderLayout());
        this.setTitle("Marking Helper - A utility by Miguel Jimenez");
        this.setBounds(20, 20, 800, 640);
        this.setExtendedState(MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.initialise();
        this.displayData(this.marking.submissions().iterator().next());
        this.setVisible(true);
    }

    /**
     * Initializes UI components.
     */
    private void initialise() {
        // Submissions table
        this.table = new SubmissionTable(this.marking.submissions());
        this.table.setDefaultRenderer(
            String.class,
            new TextAreaCellRenderer()
        );
        JScrollPane tablePanel = new JScrollPane(this.table);
        this.table.setFillsViewportHeight(true);
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.table.getSelectionModel().addListSelectionListener(new SelectionListener());

        // Syntax highlighting panel
        this.sourceTextArea = new RSyntaxTextArea();
        this.sourceTextArea.setEditable(false);
        this.sourceTextArea.setCodeFoldingEnabled(true);
        this.sourceTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
        RTextScrollPane syntaxPanel = new RTextScrollPane(this.sourceTextArea);

        // Output (text) panel
        this.outputTextArea = new JTextArea();
        JScrollPane outputPanel = new JScrollPane(this.outputTextArea);

        // Tool bar
        Toolbar toolBar = new Toolbar(this.marking.submissions());

        // Docking UI
        DockController controller = new DockController();
        SplitDockStation station = new SplitDockStation();
        controller.add(station);
        SplitDockGrid grid = new SplitDockGrid();
        grid.addDockable(0, 1, 2, 1, new DefaultDockable(tablePanel, "Submissions"));
        grid.addDockable(0, 2, 1, 1, new DefaultDockable(syntaxPanel, "Source code"));
        grid.addDockable(1, 2, 1, 1, new DefaultDockable(outputPanel, "Output"));
        station.dropTree(grid.toTree());
        
        this.add(toolBar, BorderLayout.PAGE_START);
        this.add(station.getComponent(), BorderLayout.CENTER);
    }

    /**
     * Displays the source code and output of the submission files.
     * @param submission The selected submission
     * @throws IOException If the something goes wrong reading the files
     */
    private void displayData(final Submission submission)
        throws IOException {
        int i = 1;
        this.sourceTextArea.setText(new String());
        this.outputTextArea.setText(new String());
        for (Result r : submission.results()) {
            if (r.markedFile().isFile()) {
                String contents = new String(
                    Files.readAllBytes(
                        Paths.get(r.markedFile().getAbsolutePath())
                    )
                );
                this.sourceTextArea.setText(
                    String.format(
                        "%s%s// PART %d\n%s",
                        this.sourceTextArea.getText(),
                        this.sourceTextArea.getText().isEmpty()? "" : "\n\n",
                        i,
                        contents
                    )
                );
                this.outputTextArea.setText(
                    String.format(
                        "%s%s// PART %d\n%s",
                        this.outputTextArea.getText(),
                        this.outputTextArea.getText().isEmpty()? "" : "\n\n",
                        i++,
                        r.output()
                    )
                );
            }
        }
    }
}
