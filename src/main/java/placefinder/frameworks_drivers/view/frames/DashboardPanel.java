package placefinder.frameworks_drivers.view.frames;

import placefinder.frameworks_drivers.view.components.swing.Button;
import placefinder.frameworks_drivers.view.components.swing.PanelRound;
import placefinder.frameworks_drivers.view.components.swing.table.Table;
import placefinder.frameworks_drivers.view.components.swing.table.TableCellAction;
import placefinder.frameworks_drivers.view.components.swing.table.RowActionHandler;
import placefinder.entities.Plan;
import placefinder.interface_adapters.controllers.DashboardController;
import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.interface_adapters.viewmodels.PlanDetailsViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final AppFrame appFrame;
    private final DashboardController dashboardController;
    private final DashboardViewModel dashboardVM;
    private final PlanDetailsViewModel planDetailsVM;

    private JLabel welcomeLabel;
    private JLabel totalPlansValue;
    private JLabel upcomingPlansValue;
    private JLabel lastPlanDateValue;

    private Table planTable;                      // <-- use our custom Table
    private DefaultTableModel planTableModel;
    private final List<Plan> planRows = new ArrayList<>();

    private JLabel messageLabel;

    public DashboardPanel(AppFrame appFrame,
                          DashboardController dashboardController,
                          DashboardViewModel dashboardVM,
                          PlanDetailsViewModel planDetailsVM) {
        this.appFrame = appFrame;
        this.dashboardController = dashboardController;
        this.dashboardVM = dashboardVM;
        this.planDetailsVM = planDetailsVM;
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Gradient background to match Login/Register
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
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 40, 20, 40);

        PanelRound card = new PanelRound();
        card.setBackground(new Color(234, 246, 234));
        card.setLayout(new BorderLayout(20, 20));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(card, gbc);

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout(10, 5));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("sansserif", Font.BOLD, 30));
        title.setForeground(new Color(40, 40, 40));
        welcomeLabel = new JLabel("Welcome, Traveler");
        welcomeLabel.setFont(new Font("sansserif", Font.PLAIN, 18));
        welcomeLabel.setForeground(new Color(90, 90, 90));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(welcomeLabel);

        header.add(titlePanel, BorderLayout.WEST);

        Button logoutButton = new Button();
        logoutButton.setText("LOG OUT");
        logoutButton.setBackground(new Color(220, 72, 72));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("sansserif", Font.BOLD, 12));
        logoutButton.setPreferredSize(new Dimension(110, 34));
        logoutButton.addActionListener(e -> appFrame.logout());
        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(logoutButton);
        header.add(logoutWrapper, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // ===== Main content =====
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setOpaque(false);
        card.add(main, BorderLayout.CENTER);

        // Left: quick actions
        JPanel actionsPanel = new JPanel();
        actionsPanel.setOpaque(false);
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setBorder(new EmptyBorder(10, 0, 10, 10));

        JLabel actionsTitle = new JLabel("Quick actions");
        actionsTitle.setFont(new Font("sansserif", Font.BOLD, 18));
        actionsTitle.setForeground(new Color(60, 60, 60));
        actionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.add(actionsTitle);
        actionsPanel.add(Box.createVerticalStrut(10));

        Button prefButton = new Button();
        prefButton.setText("Set Preferences");
        stylePrimaryButton(prefButton);
        prefButton.addActionListener(e -> appFrame.showPreferences());

        Button planButton = new Button();
        planButton.setText("Make a New Plan");
        stylePrimaryButton(planButton);
        planButton.addActionListener(e -> appFrame.showNewPlan());

        Button weatherButton = new Button();
        weatherButton.setText("Weather Advice");
        stylePrimaryButton(weatherButton);
        weatherButton.addActionListener(e -> appFrame.showWeatherAdvice());

        actionsPanel.add(prefButton);
        actionsPanel.add(Box.createVerticalStrut(10));
        actionsPanel.add(planButton);
        actionsPanel.add(Box.createVerticalStrut(10));
        actionsPanel.add(weatherButton);
        actionsPanel.add(Box.createVerticalGlue());

        main.add(actionsPanel, BorderLayout.WEST);

        // Right side: stats + table
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BorderLayout(10, 10));
        main.add(rightPanel, BorderLayout.CENTER);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 10, 0));
        statsRow.setOpaque(false);

        totalPlansValue = createStatCard(statsRow, "Total plans");
        upcomingPlansValue = createStatCard(statsRow, "Upcoming");
        lastPlanDateValue = createStatCard(statsRow, "Last plan date");

        rightPanel.add(statsRow, BorderLayout.NORTH);

        // Plans table
        PanelRound tableCard = new PanelRound();
        tableCard.setBackground(Color.WHITE);
        tableCard.setLayout(new BorderLayout(10, 10));
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel plansLabel = new JLabel("My Plans");
        plansLabel.setFont(new Font("sansserif", Font.BOLD, 16));
        plansLabel.setForeground(new Color(50, 50, 50));
        tableCard.add(plansLabel, BorderLayout.NORTH);

        // 4 columns: Name, Date, Location, Actions
        planTableModel = new DefaultTableModel(
                new Object[]{"Name", "Date", "Location", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only actions column is editable (to allow button clicks)
                return column == 3;
            }
        };

        planTable = new Table();
        planTable.setModel(planTableModel);
        planTable.setRowHeight(40);
        planTable.setShowGrid(false);
        planTable.setIntercellSpacing(new Dimension(0, 0));
        planTable.setFillsViewportHeight(true);
        planTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        planTable.setFont(new Font("sansserif", Font.PLAIN, 13));
        planTable.setForeground(new Color(40, 40, 40));
        planTable.setSelectionBackground(new Color(230, 245, 241));
        planTable.setSelectionForeground(new Color(0, 0, 0));

        JTableHeader headerTable = planTable.getTableHeader();
        headerTable.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(planTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        // Wire up action column with icons
        RowActionHandler handler = new RowActionHandler() {
            @Override
            public void onEdit(int row) {
                // ensure selection matches clicked row
                planTable.getSelectionModel().setSelectionInterval(row, row);
                viewSelectedPlan();
            }

            @Override
            public void onDelete(int row) {
                planTable.getSelectionModel().setSelectionInterval(row, row);
                deleteSelectedPlan();
            }
        };

        TableCellAction cellAction = new TableCellAction(handler);
        planTable.getColumnModel().getColumn(3).setCellRenderer(cellAction);
        planTable.getColumnModel().getColumn(3).setCellEditor(cellAction);
        planTable.getColumnModel().getColumn(3).setMaxWidth(90);
        planTable.getColumnModel().getColumn(3).setMinWidth(90);
        planTable.getColumnModel().getColumn(3).setPreferredWidth(90);

        rightPanel.add(tableCard, BorderLayout.CENTER);

        // Message label at bottom of card
        messageLabel = new JLabel(" ", SwingConstants.LEFT);
        messageLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(90, 90, 90));
        card.add(messageLabel, BorderLayout.SOUTH);
    }

    private void stylePrimaryButton(Button btn) {
        btn.setBackground(new Color(7, 164, 121));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("sansserif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(180, 32));
        btn.setMaximumSize(new Dimension(180, 32));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleSecondaryButton(Button btn) {
        btn.setBackground(new Color(0, 92, 75));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("sansserif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(180, 32));
    }

    private void styleDestructiveButton(Button btn) {
        btn.setBackground(new Color(220, 72, 72));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("sansserif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(140, 32));
    }

    private JLabel createStatCard(JPanel parentRow, String title) {
        PanelRound card = new PanelRound();
        card.setBackground(new Color(255, 255, 255));
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(90, 90, 90));

        JLabel valueLabel = new JLabel("0");
        valueLabel.setFont(new Font("sansserif", Font.BOLD, 18));
        valueLabel.setForeground(new Color(7, 164, 121));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        parentRow.add(card);
        return valueLabel;
    }

    public void refreshPlans() {
        Integer userId = appFrame.getCurrentUserId();
        if (userId == null) {
            return;
        }
        String userName = appFrame.getCurrentUserName() != null
                ? appFrame.getCurrentUserName()
                : "Traveler";
        welcomeLabel.setText("Welcome, " + userName);

        dashboardController.loadPlans(userId);

        planRows.clear();
        planTableModel.setRowCount(0);
        for (Plan p : dashboardVM.getPlans()) {
            planRows.add(p);
            planTableModel.addRow(new Object[]{
                    p.getName(),
                    p.getDate(),
                    p.getOriginAddress(),
                    ""   // Actions column (icons only; value not used)
            });
        }

        updateStats(planRows);

        if (planRows.isEmpty()) {
            messageLabel.setText("You have no saved plans yet. Create one using 'Make a New Plan'.");
        } else {
            messageLabel.setText(dashboardVM.getMessage() != null ? dashboardVM.getMessage() : " ");
        }
    }

    private void updateStats(List<Plan> plans) {
        totalPlansValue.setText(String.valueOf(plans.size()));

        LocalDate today = LocalDate.now();
        int upcoming = 0;
        for (Plan p : plans) {
            if (p.getDate() != null && !p.getDate().isBefore(today)) {
                upcoming++;
            }
        }
        upcomingPlansValue.setText(String.valueOf(upcoming));

        if (plans.isEmpty()) {
            lastPlanDateValue.setText("-");
        } else {
            Plan last = plans.get(0);
            lastPlanDateValue.setText(
                    last.getDate() != null ? last.getDate().toString() : "-"
            );
        }
    }

    private Plan getSelectedPlan() {
        int row = planTable.getSelectedRow();
        if (row < 0 || row >= planRows.size()) {
            return null;
        }
        return planRows.get(row);
    }

    private void viewSelectedPlan() {
        Plan selected = getSelectedPlan();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a plan first.",
                    "No plan selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        dashboardController.loadPlanDetails(selected.getId());
        if (planDetailsVM.getPlan() != null) {
            appFrame.showPlanDetails();
        } else {
            JOptionPane.showMessageDialog(this,
                    planDetailsVM.getErrorMessage() != null ? planDetailsVM.getErrorMessage() : "Could not load plan details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedPlan() {
        Plan selected = getSelectedPlan();
        Integer userId = appFrame.getCurrentUserId();
        if (selected == null || userId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a plan to delete.",
                    "No plan selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this plan?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dashboardController.deletePlan(userId, selected.getId());
            refreshPlans();
        }
    }

    private void applyPreferencesFromSelectedPlan() {
        Plan selected = getSelectedPlan();
        Integer userId = appFrame.getCurrentUserId();
        if (selected == null || userId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a plan.",
                    "No plan selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        dashboardController.applyPreferencesFromPlan(userId, selected.getId());
        String msg = dashboardVM.getMessage() != null
                ? dashboardVM.getMessage()
                : "Preferences updated from plan.";
        JOptionPane.showMessageDialog(this,
                msg,
                "Preferences Applied",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
