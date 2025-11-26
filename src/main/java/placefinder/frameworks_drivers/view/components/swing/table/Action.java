package placefinder.frameworks_drivers.view.components.swing.table;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that shows two icon-only buttons: edit and delete.
 * It delegates to a RowActionHandler with the current row index.
 */
public class Action extends JPanel {

    private final JButton editButton;
    private final JButton deleteButton;
    private int row = -1;
    private final RowActionHandler handler;

    public Action(RowActionHandler handler) {
        this.handler = handler;
        setOpaque(true);
        setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
        setBackground(Color.WHITE);

        editButton = createIconButton("/icons/edit.png", "Edit");
        deleteButton = createIconButton("/icons/delete.png", "Delete");

        editButton.addActionListener(e -> {
            if (handler != null && row >= 0) {
                handler.onEdit(row);
            }
        });

        deleteButton.addActionListener(e -> {
            if (handler != null && row >= 0) {
                handler.onDelete(row);
            }
        });

        add(editButton);
        add(deleteButton);
    }

    private JButton createIconButton(String resourcePath, String altText) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setMinimumSize(new Dimension(26, 26));
        btn.setMaximumSize(new Dimension(26, 26));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setToolTipText(altText);

        java.net.URL url = getClass().getResource(resourcePath);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } else {
            // Fallback if icon is not found
            btn.setText(altText.substring(0, 1));
        }
        return btn;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void updateBackground(boolean selected) {
        if (selected) {
            setBackground(new Color(239, 244, 255));
        } else {
            setBackground(Color.WHITE);
        }
    }
}
