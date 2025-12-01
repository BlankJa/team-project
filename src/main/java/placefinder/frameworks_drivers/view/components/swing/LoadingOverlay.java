package placefinder.frameworks_drivers.view.components.swing;

import javax.swing.*;
import java.awt.*;

/**
 * Loading overlay that displays a loading animation during API calls.
 * Matches PlaceFinder's UI design style.
 */
public class LoadingOverlay extends JPanel {
    
    private LoadingSpinner spinner;
    private JLabel messageLabel;
    
    public LoadingOverlay() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        
        // Create semi-transparent background
        setBackground(new Color(0, 0, 0, 100));
        
        // Create content panel
        PanelRound contentPanel = new PanelRound();
        contentPanel.setBackground(new Color(255, 255, 255, 250));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Add loading animation
        spinner = new LoadingSpinner();
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(spinner);
        
        // Add message label
        messageLabel = new JLabel("Loading...", SwingConstants.CENTER);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setFont(new Font("sansserif", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(60, 60, 60));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        contentPanel.add(messageLabel);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(contentPanel, gbc);
    }
    
    public LoadingOverlay(String message) {
        this();
        setMessage(message);
    }
    
    public void setMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw semi-transparent background
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
    
    public void showOverlay(Container parent) {
        if (parent == null) return;
        
        // Ensure execution on EDT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showOverlay(parent));
            return;
        }
        
        // Get JFrame and JLayeredPane
        JFrame frame = getJFrame(parent);
        if (frame != null) {
            JLayeredPane layeredPane = frame.getRootPane().getLayeredPane();
            if (layeredPane != null) {
                // Calculate position and size relative to window
                Point location = SwingUtilities.convertPoint(parent, 0, 0, layeredPane);
                Dimension size = parent.getSize();
                if (size.width == 0 || size.height == 0) {
                    size = parent.getPreferredSize();
                }
                if (size.width == 0 || size.height == 0) {
                    size = frame.getSize();
                }
                setBounds(location.x, location.y, size.width, size.height);
                layeredPane.add(this, JLayeredPane.MODAL_LAYER);
                setVisible(true);
                // Start animation immediately
                spinner.start();
                layeredPane.revalidate();
                layeredPane.repaint();
                return;
            }
        }
        
        // Fallback: use OverlayLayout (doesn't change existing layout)
        LayoutManager originalLayout = parent.getLayout();
        if (!(originalLayout instanceof OverlayLayout)) {
            // Temporarily use OverlayLayout
            parent.setLayout(new OverlayLayout(parent));
        }
        Dimension size = parent.getSize();
        if (size.width == 0 || size.height == 0) {
            size = parent.getPreferredSize();
        }
        setBounds(0, 0, size.width, size.height);
        parent.add(this, 0);
        setVisible(true);
        
        // Start animation immediately
        spinner.start();
        
        parent.revalidate();
        parent.repaint();
    }
    
    public void hideOverlay(Container parent) {
        if (parent == null) return;
        
        // Ensure execution on EDT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> hideOverlay(parent));
            return;
        }
        
        // Stop animation
        spinner.stop();
        
        // Try to remove from JLayeredPane
        JFrame frame = getJFrame(parent);
        if (frame != null) {
            JLayeredPane layeredPane = frame.getRootPane().getLayeredPane();
            if (layeredPane != null && layeredPane.isAncestorOf(this)) {
                layeredPane.remove(this);
                setVisible(false);
                layeredPane.revalidate();
                layeredPane.repaint();
                return;
            }
        }
        
        // Fallback: remove from parent container
        if (parent.isAncestorOf(this)) {
            parent.remove(this);
            setVisible(false);
            // If OverlayLayout was used, keep it as other components might be using it
            if (parent.getLayout() instanceof OverlayLayout && parent.getComponentCount() > 0) {
                // Keep OverlayLayout as other components might be using it
            }
            parent.revalidate();
            parent.repaint();
        }
    }
    
    private JFrame getJFrame(Container component) {
        Container parent = component;
        while (parent != null) {
            if (parent instanceof JFrame) {
                return (JFrame) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
}

