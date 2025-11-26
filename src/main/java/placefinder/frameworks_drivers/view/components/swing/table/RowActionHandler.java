package placefinder.frameworks_drivers.view.components.swing.table;

/**
 * Handler for row-level actions in the table.
 * The row index is always the model row index of the clicked row.
 */
public interface RowActionHandler {
    void onEdit(int row);
    void onDelete(int row);
}
