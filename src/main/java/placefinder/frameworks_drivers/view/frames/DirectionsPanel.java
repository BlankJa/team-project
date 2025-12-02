package placefinder.frameworks_drivers.view.frames;

import placefinder.interface_adapters.viewmodels.DirectionsViewModel;
import placefinder.interface_adapters.viewmodels.LegViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Swing panel for displaying navigation directions with expandable legs.  This
 * component observes a {@link DirectionsViewModel} and renders a list of legs.
 * Each leg header is a button that toggles expansion to reveal or hide its
 * associated step instructions.  When the view model changes, call
 * {@link #refresh()} to rebuild the UI.
 */
public class DirectionsPanel extends JPanel {
    private DirectionsViewModel viewModel;

    public DirectionsPanel(DirectionsViewModel viewModel) {
        this.viewModel = viewModel;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        refresh();
    }

    /**
     * Assign a new view model to this panel and rebuild the UI.  Useful if
     * switching between different sets of directions.
     *
     * @param viewModel the new directions view model
     */
    public void setViewModel(DirectionsViewModel viewModel) {
        this.viewModel = viewModel;
        refresh();
    }

    /**
     * Rebuilds the UI based on the current state of the view model.  All
     * existing components are removed before new ones are added.  Call this
     * method after updating the view model (e.g., calling
     * {@link DirectionsViewModel#setFromRoute(java.util.List)}).
     */
    public void refresh() {
        removeAll();
        if (viewModel == null || viewModel.getLegs() == null) {
            revalidate();
            repaint();
            return;
        }
        for (LegViewModel leg : viewModel.getLegs()) {
            add(createLegPanel(leg));
        }
        // Fill vertical space if necessary
        add(Box.createVerticalGlue());
        revalidate();
        repaint();
    }

    /**
     * Creates a panel representing a single leg.  The panel contains a header
     * button with the start and end place names.  Clicking the button
     * toggles expansion to show or hide the list of step instructions.  The
     * steps are displayed in a vertical list of labels.
     *
     * @param leg the leg view model to render
     * @return a panel for the leg
     */
    private JPanel createLegPanel(LegViewModel leg) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());

        // Header button toggles the expanded state
        JButton header = new JButton("From " + leg.getStartPlace() + " to " + leg.getEndPlace());
        header.setFocusPainted(false);
        header.setContentAreaFilled(false);
        header.setBorderPainted(false);
        header.setFont(new Font("sansserif", Font.BOLD, 12));
        header.setForeground(new Color(0, 92, 75));
        header.addActionListener(e -> {
            leg.toggleExpanded();
            refresh();
        });
        panel.add(header, BorderLayout.NORTH);

        // Steps panel displayed only when expanded
        if (leg.isExpanded()) {
            JPanel stepsPanel = new JPanel();
            stepsPanel.setOpaque(false);
            stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
            stepsPanel.setBorder(new EmptyBorder(4, 16, 4, 4));
            for (String step : leg.getSteps()) {
                JLabel stepLabel = new JLabel("\u2022 " + step);
                stepLabel.setFont(new Font("monospaced", Font.PLAIN, 12));
                stepLabel.setForeground(new Color(50, 50, 50));
                stepsPanel.add(stepLabel);
            }
            panel.add(stepsPanel, BorderLayout.CENTER);
        }
        return panel;
    }
}