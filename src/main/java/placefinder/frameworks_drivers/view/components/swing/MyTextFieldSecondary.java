package placefinder.frameworks_drivers.view.components.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Rounded text field with hint and animated bottom line.
 * Styled close to Raven's login fields (soft teal background, grey text).
 */
public class MyTextFieldSecondary extends JTextField {

    private String hint = "";
    private float focusAnim = 0f; // 0..1
    private Timer timer;

    public MyTextFieldSecondary() {
        setOpaque(false);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setFont(new Font("sansserif", Font.PLAIN, 13));
        setForeground(new Color(0, 0, 0));
        setCaretColor(new Color(7, 164, 121));

        timer = new Timer(15, e -> animateFocus());

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                startAnim(true);
            }
            @Override
            public void focusLost(FocusEvent e) {
                startAnim(false);
            }
        });
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
        repaint();
    }

    private boolean focusTarget = false;

    private void startAnim(boolean toFocused) {
        focusTarget = toFocused;
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    private void animateFocus() {
        float step = 0.08f;
        if (focusTarget) {
            focusAnim = Math.min(1f, focusAnim + step);
        } else {
            focusAnim = Math.max(0f, focusAnim - step);
        }
        if (focusAnim == 0f || focusAnim == 1f) {
            timer.stop();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // soft background similar to Raven
        g2.setColor(new Color(255, 255, 255));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        super.paintComponent(g2);

        // hint
        if (getText().isEmpty() && !isFocusOwner() && hint != null && !hint.isEmpty()) {
            Insets in = getInsets();
            g2.setColor(new Color(255, 255, 255));
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(hint, in.left, y);
        }

        // bottom accent line
        int lineWidth = (int) (getWidth() * (0.3 + 0.7 * focusAnim));
        int x = (getWidth() - lineWidth) / 2;
        int y = getHeight() - 3;
        g2.setColor(new Color(7, 164, 121));
        g2.fillRoundRect(x, y, lineWidth, 2, 4, 4);

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // no default border; background & bottom line act as visual border
    }
}
