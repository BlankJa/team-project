package placefinder.frameworks_drivers.view.components.swing.table;

/**
 * Simple data holder that pairs the row object with the event handler.
 */
public class ModelAction<T> {
    private final T rowData;
    private final EventAction<T> event;

    public ModelAction(T rowData, EventAction<T> event) {
        this.rowData = rowData;
        this.event = event;
    }

    public T getRowData() {
        return rowData;
    }

    public EventAction<T> getEvent() {
        return event;
    }
}
