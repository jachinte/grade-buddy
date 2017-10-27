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
import com.rigiresearch.gradebuddy.io.Command;
import com.rigiresearch.gradebuddy.model.Result;
import com.rigiresearch.gradebuddy.model.Submission;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
     * Script to run on submission selection.
     */
    private File selectionScript;

    /**
     * Updates the selection script.
     * @param selectionScript the new script
     */
    public void selectionScript(final File selectionScript) {
        this.selectionScript = selectionScript;
    }

    /**
     * Configures this window.
     * @throws IOException 
     */
    public void configure() throws IOException {
        this.setLayout(new BorderLayout());
        this.setTitle("Grade Buddy - github.com/jachinte/grade-buddy");
        this.setBounds(20, 20, 800, 640);
        this.setExtendedState(MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.initialise();
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
        this.table.getSelectionModel()
            .addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    final ListSelectionModel model =
                        (ListSelectionModel) e.getSource();
                    MainWindow.this.onSelectionChange(
                        MainWindow.this.marking
                            .submissions()
                            .get(model.getMinSelectionIndex())
                    );
                }
            });

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
        Toolbar toolBar = new Toolbar(
            this.table,
            this.marking,
            new Function<Submission, Object>() {
                @Override
                public Object apply(Submission submission) {
                    try {
                        MainWindow.this.displayData(submission);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new Object();
                }
            }
        );

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
     * Executes actions on row selection.
     * @param submission The currently selected submission
     */
    private void onSelectionChange(final Submission submission) {
        try {
            this.displayData(submission);
            if (this.selectionScript != null)
                this.runScript(submission);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the source code and output of the submission files.
     * @param submission The selected submission
     * @throws IOException If the something goes wrong reading the files
     */
    private void displayData(final Submission submission)
        throws IOException {
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
                        "%s%s// %s\n%s",
                        this.sourceTextArea.getText(),
                        this.sourceTextArea.getText().isEmpty()? "" : "\n\n",
                        r.markedFile().getName(),
                        contents
                    )
                );
                this.outputTextArea.setText(
                    String.format(
                        "%s%s// %s\n%s",
                        this.outputTextArea.getText(),
                        this.outputTextArea.getText().isEmpty()? "" : "\n\n",
                        r.markedFile().getName(),
                        r.output()
                    )
                );
            }
        }
    }

    /**
     * Executes the selection script.
     * @param submission The currently selected submission
     * @throws Exception See {@link Command#execute()}
     */
    private void runScript(final Submission submission) throws Exception {
        Command c = new Command(
            new String[]{
                "sh",
                this.selectionScript.getAbsolutePath(),
                submission.directory().getAbsolutePath()
            }
        ).execute();
        if (c.result().exitCode() != 0) {
            System.out.println(c.result().outputStream().toString());
            System.err.println(c.result().errorStream().toString());
            JOptionPane.showMessageDialog(
                this,
                "Error executing script. Please read the error output.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
