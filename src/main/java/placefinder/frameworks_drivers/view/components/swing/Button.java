package placefinder.frameworks_drivers.view.components.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Timer;

/**
 * Pill-shaped button with hover animation, similar feel to Raven's demo.
 */
public class Button extends JButton {

    private Color baseColor = new Color(7, 164, 121);
    private Color hoverColor = new Color(5, 140, 103);
    private float hoverAmount = 0f; // 0..1
    private Timer animationTimer;
    private boolean hovering = false;

    public Button() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setBackground(baseColor);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        setFont(new Font("sansserif", Font.BOLD, 13));

        animationTimer = new Timer(15, e -> animate());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                if (!animationTimer.isRunning()) {
                    animationTimer.start();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                if (!animationTimer.isRunning()) {
                    animationTimer.start();
                }
            }
        });
    }

    private void animate() {
        float step = 0.08f;
        if (hovering) {
            hoverAmount = Math.min(1f, hoverAmount + step);
        } else {
            hoverAmount = Math.max(0f, hoverAmount - step);
        }
        if (hoverAmount == 0f || hoverAmount == 1f) {
            animationTimer.stop();
        }
        repaint();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        this.baseColor = bg;
        // derive hover color slightly darker
        this.hoverColor = bg.darker();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // interpolate color between base and hover
        int r = (int) (baseColor.getRed() + (hoverColor.getRed() - baseColor.getRed()) * hoverAmount);
        int gr = (int) (baseColor.getGreen() + (hoverColor.getGreen() - baseColor.getGreen()) * hoverAmount);
        int b = (int) (baseColor.getBlue() + (hoverColor.getBlue() - baseColor.getBlue()) * hoverAmount);
        g2.setColor(new Color(r, gr, b));

        int arc = getHeight(); // pill
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);

        g2.dispose();
    }
}
