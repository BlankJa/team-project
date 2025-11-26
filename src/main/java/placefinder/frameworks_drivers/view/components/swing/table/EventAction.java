package placefinder.frameworks_drivers.view.components.swing.table;

/**
 * What to do when the user clicks edit/delete for a given row.
 */
public interface EventAction<T> {
    void update(T rowData);
    void delete(T rowData);
}
