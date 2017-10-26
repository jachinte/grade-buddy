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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/**
 * Makes {@link TextAreaCellRenderer} editable.
 * Adapted from https://stackoverflow.com/a/26107406/738968.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @date 2017-10-14
 * @version $Id$
 * @since 0.0.1
 */
public final class TextAreaCellEditor implements TableCellEditor {

    /**
     * The scroll pane displayed when editing the cell.
     */
    private final JScrollPane scroll;

    /**
     * The text area containing the editable content.
     */
    private JTextArea textArea = new JTextArea();

    protected EventListenerList listenerList = new EventListenerList();
    transient protected ChangeEvent changeEvent = null;

    /**
     * Default constructor.
     */
    public TextAreaCellEditor() {
        scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        //scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        textArea.setLineWrap(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        KeyStroke enter = KeyStroke.getKeyStroke(
            KeyEvent.VK_ENTER,
            InputEvent.CTRL_MASK
        );
        textArea.getInputMap(JComponent.WHEN_FOCUSED)
            .put(enter, new AbstractAction() {
                /**
                 * Serial version UID.
                 */
                private static final long serialVersionUID = 3349088268669286994L;
    
                @Override
                public void actionPerformed(ActionEvent e) {
                    stopCellEditing();
                }
            });
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue() {
        return textArea.getText();
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.table.TableCellEditor
     *  #getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
        textArea.setFont(table.getFont());
        textArea.setText((value != null) ? value.toString() : "");
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.setCaretPosition(textArea.getText().length());
                textArea.requestFocusInWindow();
            }
        });
        return scroll;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
     */
    @Override
    public boolean isCellEditable(final EventObject e) {
        if (e instanceof MouseEvent) {
            return ((MouseEvent) e).getClickCount() >= 2;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (e instanceof KeyEvent) {
                    KeyEvent ke = (KeyEvent) e;
                    char kc = ke.getKeyChar();
                    if (Character.isUnicodeIdentifierStart(kc)) {
                        textArea.setText(textArea.getText() + kc);
                    }
                }
            }
        });
        return true;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
     */
    @Override
    public boolean shouldSelectCell(EventObject e) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#stopCellEditing()
     */
    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#cancelCellEditing()
     */
    @Override
    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor
     *  #addCellEditorListener(javax.swing.event.CellEditorListener)
     */
    @Override
    public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor
     *  #removeCellEditorListener(javax.swing.event.CellEditorListener)
     */
    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }

    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }

    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1])
                    .editingStopped(changeEvent);
            }
        }
    }

    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1])
                    .editingCanceled(changeEvent);
            }
        }
    }
}
