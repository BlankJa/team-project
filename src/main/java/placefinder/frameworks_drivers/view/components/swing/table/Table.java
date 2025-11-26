package placefinder.frameworks_drivers.view.components.swing.table;

import placefinder.frameworks_drivers.view.components.swing.scrollbar.ScrollBarCustom;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Table extends JTable {

    public Table() {
        setShowHorizontalLines(true);
        setGridColor(new Color(230, 230, 230));
        setRowHeight(40);
        getTableHeader().setReorderingAllowed(false);

        // Custom header renderer
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                String text = value != null ? value.toString() : "";
                TableHeader header = new TableHeader(text);
                // center the actions column (assumed index 3)
                if (column == 3) {
                    header.setHorizontalAlignment(JLabel.CENTER);
                } else {
                    header.setHorizontalAlignment(JLabel.LEFT);
                }
                return header;
            }
        });

        // Default cell renderer for normal text cells
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component com = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                setBorder(noFocusBorder);
                com.setForeground(new Color(102, 102, 102));
                if (isSelected) {
                    com.setBackground(new Color(239, 244, 255));
                } else {
                    com.setBackground(Color.WHITE);
                }

                if (com instanceof JLabel) {
                    ((JLabel) com).setBorder(new EmptyBorder(0, 10, 0, 10));
                }
                return com;
            }
        });
    }

    public void addRow(Object[] row) {
        DefaultTableModel mod = (DefaultTableModel) getModel();
        mod.addRow(row);
    }

    public void fixTable(JScrollPane scroll) {
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setVerticalScrollBar(new ScrollBarCustom());
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, p);
        scroll.setBorder(new EmptyBorder(5, 10, 5, 10));
    }
}
