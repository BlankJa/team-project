package placefinder.frameworks_drivers.view.components.swing.table;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Cell renderer/editor for the actions column.
 * It reuses a single Action panel instance and only cares about the row index.
 */
public class TableCellAction extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor {

    private final Action actionPanel;

    public TableCellAction(RowActionHandler handler) {
        this.actionPanel = new Action(handler);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        actionPanel.setRow(row);
        actionPanel.updateBackground(isSelected);
        return actionPanel;
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
        actionPanel.setRow(row);
        actionPanel.updateBackground(true);
        return actionPanel;
    }

    @Override
    public Object getCellEditorValue() {
        return null; // we don't store any value in this column
    }
}
