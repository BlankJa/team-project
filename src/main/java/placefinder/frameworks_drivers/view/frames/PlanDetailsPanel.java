package placefinder.frameworks_drivers.view.frames;

import placefinder.frameworks_drivers.view.components.swing.Button;
import placefinder.frameworks_drivers.view.components.swing.PanelRound;
import placefinder.entities.Plan;
import placefinder.entities.PlanStop;
import placefinder.interface_adapters.controllers.DeletePlanController;
import placefinder.interface_adapters.controllers.ApplyPreferencesFromPlanController;
import placefinder.interface_adapters.controllers.GetRouteDetailsController;
import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.interface_adapters.viewmodels.DirectionsViewModel;
import placefinder.interface_adapters.viewmodels.PlanDetailsViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * UI panel for displaying the details of a plan based on user selected preferences.
 */
public class PlanDetailsPanel extends JPanel {

    private final AppFrame appFrame;
    private final DeletePlanController deletePlanController;
    private final ApplyPreferencesFromPlanController applyPreferencesFromPlanController;
    private final DashboardViewModel dashboardVM;
    private final PlanDetailsViewModel planDetailsVM;
    private DirectionsViewModel directionsVM;
    private GetRouteDetailsController getRouteDetailsController;

    private JLabel nameLabel;
    private JLabel dateLabel;
    private JLabel locationLabel;
    private JLabel prefsLabel;
    private JTextArea timelineArea;

    private DirectionsPanel directionsPanel;

    private Plan currentPlan;

    public PlanDetailsPanel(DeletePlanController deletePlanController,
                            ApplyPreferencesFromPlanController applyPreferencesFromPlanController,
                            DashboardViewModel dashboardVM,
                            PlanDetailsViewModel planDetailsVM,
                            DirectionsViewModel directionsVM,
                            GetRouteDetailsController getRouteDetailsController,
                            AppFrame appFrame) {
        this.appFrame = appFrame;
        this.deletePlanController = deletePlanController;
        this.applyPreferencesFromPlanController = applyPreferencesFromPlanController;
        this.dashboardVM = dashboardVM;
        this.planDetailsVM = planDetailsVM;
        this.directionsVM = directionsVM;
        this.getRouteDetailsController = getRouteDetailsController;
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c1 = new Color(7, 164, 121);
        Color c2 = new Color(0, 92, 75);
        GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(20, 40, 20, 40);
        gbc.fill = GridBagConstraints.BOTH;

        PanelRound card = new PanelRound();
        card.setBackground(new Color(250, 250, 250));
        card.setLayout(new BorderLayout(20, 20));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(card, gbc);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Plan details");
        title.setFont(new Font("sansserif", Font.BOLD, 22));
        title.setForeground(new Color(40, 40, 40));

        JLabel subtitle = new JLabel("Review your itinerary, edit it, or reuse its preferences.");
        subtitle.setFont(new Font("sansserif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(subtitle);

        header.add(titleBox, BorderLayout.WEST);

        Button backButton = new Button();
        backButton.setText("Back to Dashboard");
        backButton.setBackground(new Color(0, 92, 75));
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("sansserif", Font.BOLD, 12));
        backButton.setPreferredSize(new Dimension(170, 34));
        backButton.addActionListener(e -> appFrame.showDashboard());

        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backWrap.setOpaque(false);
        backWrap.add(backButton);
        header.add(backWrap, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);
        card.add(center, BorderLayout.CENTER);

        PanelRound infoCard = new PanelRound();
        infoCard.setBackground(Color.WHITE);
        infoCard.setLayout(new GridBagLayout());
        infoCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints ic = new GridBagConstraints();
        ic.insets = new Insets(4, 4, 4, 4);
        ic.gridx = 0;
        ic.gridy = 0;
        ic.anchor = GridBagConstraints.WEST;
        ic.fill = GridBagConstraints.HORIZONTAL;
        ic.weightx = 1;

        nameLabel = new JLabel("Plan name");
        nameLabel.setFont(new Font("sansserif", Font.BOLD, 16));
        nameLabel.setForeground(new Color(40, 40, 40));
        infoCard.add(nameLabel, ic);

        ic.gridy++;
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row1.setOpaque(false);
        dateLabel = new JLabel("Date: -");
        locationLabel = new JLabel("Location: -");
        row1.add(dateLabel);
        row1.add(locationLabel);
        infoCard.add(row1, ic);

        ic.gridy++;
        prefsLabel = new JLabel("Preferences: -");
        prefsLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        prefsLabel.setForeground(new Color(80, 80, 80));
        infoCard.add(prefsLabel, ic);

        center.add(infoCard, BorderLayout.NORTH);

        PanelRound timelineCard = new PanelRound();
        timelineCard.setBackground(Color.WHITE);
        timelineCard.setLayout(new BorderLayout());
        timelineCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel timelineLabel = new JLabel("Timeline");
        timelineLabel.setFont(new Font("sansserif", Font.BOLD, 14));
        timelineLabel.setForeground(new Color(60, 60, 60));
        timelineCard.add(timelineLabel, BorderLayout.NORTH);

        timelineArea = new JTextArea(12, 40);
        timelineArea.setEditable(false);
        timelineArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(timelineArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        timelineCard.add(scroll, BorderLayout.CENTER);

        JLabel directionsLabel = new JLabel("Directions");
        directionsLabel.setFont(new Font("sansserif", Font.BOLD, 14));
        directionsLabel.setForeground(new Color(60, 60, 60));

        this.directionsPanel = new DirectionsPanel(directionsVM);
        JScrollPane dirScroll = new JScrollPane(directionsPanel);
        dirScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel directionsWrap = new JPanel(new BorderLayout());
        directionsWrap.setOpaque(false);
        directionsWrap.add(directionsLabel, BorderLayout.NORTH);
        directionsWrap.add(dirScroll, BorderLayout.CENTER);

        timelineCard.add(directionsWrap, BorderLayout.SOUTH);
        center.add(timelineCard, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        Button editButton = new Button();
        editButton.setText("Edit plan");
        editButton.setBackground(new Color(7, 164, 121));
        editButton.setForeground(Color.WHITE);
        editButton.setFont(new Font("sansserif", Font.BOLD, 12));
        editButton.setPreferredSize(new Dimension(110, 30));
        editButton.addActionListener(e -> editCurrentPlan());

        Button prefsButton = new Button();
        prefsButton.setText("Apply preferences");
        prefsButton.setBackground(new Color(0, 92, 75));
        prefsButton.setForeground(Color.WHITE);
        prefsButton.setFont(new Font("sansserif", Font.BOLD, 12));
        prefsButton.setPreferredSize(new Dimension(150, 30));
        prefsButton.addActionListener(e -> applyPrefsFromCurrentPlan());

        Button deleteButton = new Button();
        deleteButton.setText("Delete");
        deleteButton.setBackground(new Color(220, 72, 72));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("sansserif", Font.BOLD, 12));
        deleteButton.setPreferredSize(new Dimension(90, 30));
        deleteButton.addActionListener(e -> deleteCurrentPlan());

        bottom.add(editButton);
        bottom.add(prefsButton);
        bottom.add(deleteButton);

        card.add(bottom, BorderLayout.SOUTH);
    }

    public void showFromViewModel() {
        this.currentPlan = planDetailsVM.getPlan();
        if (currentPlan == null) {
            nameLabel.setText("Plan not found");
            dateLabel.setText("Date: -");
            locationLabel.setText("Location: -");
            prefsLabel.setText("Preferences: -");
            timelineArea.setText("");
            return;
        }

        nameLabel.setText(currentPlan.getName());
        dateLabel.setText("Date: " + currentPlan.getDate());
        locationLabel.setText("Location: " + currentPlan.getOriginAddress());

        String categoriesStr;
        Map<String, List<String>> snapshotCategories = currentPlan.getSnapshotCategories();
        if (snapshotCategories == null || snapshotCategories.isEmpty()) {
            categoriesStr = "(none)";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : snapshotCategories.entrySet()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(entry.getKey());
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    sb.append(": ").append(String.join(", ", entry.getValue()));
                }
            }
            categoriesStr = sb.toString();
        }
        String prefs = "Radius " + currentPlan.getSnapshotRadiusKm() + " km; Categories: " + categoriesStr;
        prefsLabel.setText(prefs);

        StringBuilder sb = new StringBuilder();
        sb.append("Timeline:\n");
        if (currentPlan.getRoute() != null && currentPlan.getRoute().getStops() != null) {
            for (PlanStop stop : currentPlan.getRoute().getStops()) {
                sb.append(String.format("%s - %s  %s%n",
                        stop.getStartTime(),
                        stop.getEndTime(),
                        stop.getPlace().getName()));
            }
        }
        timelineArea.setText(sb.toString());

        if (currentPlan != null) {
            getRouteDetailsController.loadRouteDetails(currentPlan.getId());
            directionsPanel.refresh(); // rebuild the UI from the updated view model
        }
    }

    private void editCurrentPlan() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this,
                    "No plan selected.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        appFrame.openPlanEditorWithPlan(currentPlan);
    }

    private void deleteCurrentPlan() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this,
                    "No plan selected.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer userId = appFrame.getCurrentUserId();
        if (userId == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this plan?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        deletePlanController.deletePlan(userId, currentPlan.getId());
        appFrame.showDashboard();
    }

    private void applyPrefsFromCurrentPlan() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this,
                    "No plan selected.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer userId = appFrame.getCurrentUserId();
        if (userId == null) return;
        applyPreferencesFromPlanController.applyPreferencesFromPlan(userId, currentPlan.getId());
        String msg = dashboardVM.getMessage() != null
                ? dashboardVM.getMessage()
                : "Preferences updated from this plan.";
        JOptionPane.showMessageDialog(this,
                msg,
                "Preferences applied",
                JOptionPane.INFORMATION_MESSAGE);
    }
}