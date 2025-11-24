package placefinder.frameworks_drivers.view.components.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.Timer;

/**
 * Password variant of MyTextField with same style & animation.
 */
public class MyPasswordField extends JPasswordField {

    private String hint = "";
    private float focusAnim = 0f;
    private Timer timer;
    private boolean focusTarget = false;

    public MyPasswordField() {
        setOpaque(false);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setFont(new Font("sansserif", Font.PLAIN, 13));
        setForeground(new Color(40, 40, 40));
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

        g2.setColor(new Color(230, 245, 241));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        super.paintComponent(g2);

        if (getPassword().length == 0 && !isFocusOwner() && hint != null && !hint.isEmpty()) {
            Insets in = getInsets();
            g2.setColor(new Color(140, 140, 140));
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(hint, in.left, y);
        }

        int lineWidth = (int) (getWidth() * (0.3 + 0.7 * focusAnim));
        int x = (getWidth() - lineWidth) / 2;
        int y = getHeight() - 3;
        g2.setColor(new Color(7, 164, 121));
        g2.fillRoundRect(x, y, lineWidth, 2, 4, 4);

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // no default border
    }
}
