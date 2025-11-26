package placefinder.frameworks_drivers.view.components.swing.table;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Simple fancy header renderer with your green accent.
 */
public class TableHeader extends JLabel {

    public TableHeader(String text) {
        super(text);
        setOpaque(true);
        setBackground(new Color(7, 164, 121));
        setFont(new Font("sansserif", Font.BOLD, 13));
        setForeground(Color.WHITE);
        setBorder(new EmptyBorder(8, 5, 8, 5));
        setHorizontalAlignment(LEFT);
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        super.paintComponent(grphcs);
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 120, 90));
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    }
}
