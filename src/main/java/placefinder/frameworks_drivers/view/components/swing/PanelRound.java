package placefinder.frameworks_drivers.view.components.swing;

import javax.swing.*;
import java.awt.*;

/**
 * Rounded card panel similar to Raven's UI.
 * White background, large corner radius, drop shadow-style border.
 */
public class PanelRound extends JPanel {

    private int cornerRadius = 25;

    public PanelRound() {
        setOpaque(false);
        setBackground(Color.WHITE);
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // subtle shadow
        g2.setColor(new Color(0, 0, 0, 25));
        g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 10, cornerRadius, cornerRadius);

        // card background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 10, cornerRadius, cornerRadius);

        super.paintComponent(g2);
        g2.dispose();
    }
}
