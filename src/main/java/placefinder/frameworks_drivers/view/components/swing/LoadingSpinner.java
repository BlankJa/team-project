package placefinder.frameworks_drivers.view.components.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Loading spinner component that displays a rotating circle animation.
 * Matches PlaceFinder's UI design style (green theme).
 */
public class LoadingSpinner extends JPanel {
    
    private Timer timer;
    private double angle = 0;
    private final Color primaryColor = new Color(7, 164, 121); // Primary green
    private final Color secondaryColor = new Color(7, 164, 121, 50); // Semi-transparent green
    
    public LoadingSpinner() {
        setOpaque(false);
        setPreferredSize(new Dimension(60, 60));
        setMinimumSize(new Dimension(60, 60));
        setMaximumSize(new Dimension(60, 60));
        
        // Create timer that updates every 10ms (approximately 100fps for smooth animation)
        // Timer's ActionListener always executes on EDT, so we can directly call repaint
        timer = new Timer(10, e -> {
            angle += 3.0; // Rotation speed (increment by 3 degrees each time)
            if (angle >= 360) {
                angle -= 360; // Keep angle within 0-360 range
            }
            repaint(); // Timer callback is already on EDT, so we can directly repaint
        });
    }
    
    public void start() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }
    
    public void stop() {
        if (timer.isRunning()) {
            timer.stop();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        int size = Math.min(getWidth(), getHeight()) - 10;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        
        // Draw background circle (semi-transparent)
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(secondaryColor);
        g2.drawOval(x, y, size, size);
        
        // Draw rotating arc
        g2.setColor(primaryColor);
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Draw a 270-degree arc starting from current angle
        Arc2D arc = new Arc2D.Double(x, y, size, size, angle, 270, Arc2D.OPEN);
        g2.draw(arc);
        
        g2.dispose();
    }
}

