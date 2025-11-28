package placefinder.frameworks_drivers.view.frames;

import placefinder.frameworks_drivers.view.components.swing.Button;
import placefinder.frameworks_drivers.view.components.swing.MyTextField;
import placefinder.frameworks_drivers.view.components.swing.PanelRound;
import placefinder.entities.Plan;
import placefinder.entities.PlanStop;
import placefinder.entities.Place;
import placefinder.interface_adapters.controllers.PlanCreationController;
import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * UI panel for building a plan based on user selected preferences.
 */
public class PlanBuilderPanel extends JPanel {

    private final AppFrame appFrame;
    private final PlanCreationController planCreationController;
    private final PlanCreationViewModel planCreationVM;

    private MyTextField locationField;
    private MyTextField dateField;
    private MyTextField startTimeField;

    private DefaultListModel<Place> recommendedModel;
    private JList<Place> recommendedList;
    private DefaultListModel<Place> selectedModel;
    private JList<Place> selectedList;

    private JTextArea planPreviewArea;
    private JLabel infoLabel;
    private JLabel errorLabel;

    private Integer editingPlanId = null;

    public PlanBuilderPanel(AppFrame appFrame,
                            PlanCreationController planCreationController,
                            PlanCreationViewModel planCreationVM) {
        this.appFrame = appFrame;
        this.planCreationController = planCreationController;
        this.planCreationVM = planCreationVM;
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // gradient background
        Graphics2D g2 = (Graphics2D) g.create();
        Color top = new Color(243, 250, 255);
        Color bottom = new Color(228, 238, 255);
        g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
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

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Build your day plan");
        title.setFont(new Font("sansserif", Font.BOLD, 22));
        title.setForeground(new Color(40, 40, 40));

        JLabel subtitle = new JLabel("Pick a location, date, and start time, then choose places for a one-day itinerary.");
        subtitle.setFont(new Font("sansserif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 120, 120));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        // ===== Top search row =====
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new GridBagLayout());
        GridBagConstraints tgc = new GridBagConstraints();
        tgc.gridy = 0;
        tgc.insets = new Insets(4, 4, 4, 4);
        tgc.fill = GridBagConstraints.HORIZONTAL;

        tgc.gridx = 0;
        tgc.weightx = 0.4;
        locationField = new MyTextField();
        locationField.setHint("Location (city or favorite)");
        top.add(locationField, tgc);

        tgc.gridx = 1;
        tgc.weightx = 0.25;
        dateField = new MyTextField();
        dateField.setHint("Date (YYYY-MM-DD)");
        top.add(dateField, tgc);

        tgc.gridx = 2;
        tgc.weightx = 0.15;
        startTimeField = new MyTextField();
        startTimeField.setHint("Start time (HH:MM)");
        top.add(startTimeField, tgc);

        tgc.gridx = 3;
        tgc.weightx = 0.2;
        Button searchButton = new Button();
        searchButton.setText("Search places");
        searchButton.setBackground(new Color(7, 164, 121));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("sansserif", Font.BOLD, 12));
        searchButton.addActionListener(e -> searchPlaces());
        top.add(searchButton, tgc);

        // ✅ combine header + search row in NORTH
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.setOpaque(false);
        northContainer.add(header, BorderLayout.NORTH);
        northContainer.add(top, BorderLayout.CENTER);
        card.add(northContainer, BorderLayout.NORTH);

        // ===== Center: lists =====
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        card.add(center, BorderLayout.CENTER);

        JPanel listsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        listsPanel.setOpaque(false);
        center.add(listsPanel, BorderLayout.CENTER);

        recommendedModel = new DefaultListModel<>();
        recommendedList = new JList<>(recommendedModel);
        recommendedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        recommendedList.setVisibleRowCount(10);
        recommendedList.setCellRenderer(createPlaceRenderer());

        selectedModel = new DefaultListModel<>();
        selectedList = new JList<>(selectedModel);
        selectedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedList.setVisibleRowCount(10);
        selectedList.setCellRenderer(createPlaceRenderer());

        JPanel recPanel = new JPanel(new BorderLayout(5, 5));
        recPanel.setOpaque(false);
        JLabel recLabel = new JLabel("Recommended places");
        recLabel.setFont(new Font("sansserif", Font.BOLD, 13));
        recLabel.setForeground(new Color(60, 60, 60));
        recPanel.add(recLabel, BorderLayout.NORTH);
        recPanel.add(new JScrollPane(recommendedList), BorderLayout.CENTER);
        listsPanel.add(recPanel);

        JPanel middleButtons = new JPanel();
        middleButtons.setOpaque(false);
        middleButtons.setLayout(new BoxLayout(middleButtons, BoxLayout.Y_AXIS));

        Button addButton = new Button();
        addButton.setText("Add >>");
        styleSecondaryButton(addButton);
        addButton.addActionListener(e -> addSelectedPlaces());

        Button removeButton = new Button();
        removeButton.setText("<< Remove");
        styleSecondaryButton(removeButton);
        removeButton.addActionListener(e -> removeSelectedPlace());

        Button upButton = new Button();
        upButton.setText("Move up");
        styleSecondaryButton(upButton);
        upButton.addActionListener(e -> moveSelectedPlace(-1));

        Button downButton = new Button();
        downButton.setText("Move down");
        styleSecondaryButton(downButton);
        downButton.addActionListener(e -> moveSelectedPlace(1));

        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        upButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        downButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        middleButtons.add(Box.createVerticalGlue());
        middleButtons.add(addButton);
        middleButtons.add(Box.createVerticalStrut(8));
        middleButtons.add(removeButton);
        middleButtons.add(Box.createVerticalStrut(8));
        middleButtons.add(upButton);
        middleButtons.add(Box.createVerticalStrut(8));
        middleButtons.add(downButton);
        middleButtons.add(Box.createVerticalGlue());

        listsPanel.add(middleButtons);

        JPanel selectedPanel = new JPanel(new BorderLayout(5, 5));
        selectedPanel.setOpaque(false);
        JLabel selLabel = new JLabel("Selected places (in order)");
        selLabel.setFont(new Font("sansserif", Font.BOLD, 13));
        selLabel.setForeground(new Color(60, 60, 60));
        selectedPanel.add(selLabel, BorderLayout.NORTH);
        selectedPanel.add(new JScrollPane(selectedList), BorderLayout.CENTER);
        listsPanel.add(selectedPanel);

        // ===== Bottom: preview + actions =====
        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setOpaque(false);
        center.add(bottom, BorderLayout.SOUTH);

        planPreviewArea = new JTextArea(6, 20);
        planPreviewArea.setEditable(false);
        planPreviewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane previewScroll = new JScrollPane(planPreviewArea);
        previewScroll.setBorder(BorderFactory.createTitledBorder("Plan preview"));
        bottom.add(previewScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonsRow.setOpaque(false);

        Button backButton = new Button();
        backButton.setText("Back to dashboard");
        backButton.setBackground(new Color(60, 62, 62));
        backButton.setForeground(new Color(253, 253, 253));
        backButton.setFont(new Font("sansserif", Font.BOLD, 12));
        backButton.addActionListener(e -> appFrame.showDashboard());

        Button generateButton = new Button();
        generateButton.setText("Generate plan");
        generateButton.setBackground(new Color(7, 164, 121));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFont(new Font("sansserif", Font.BOLD, 12));
        generateButton.addActionListener(e -> generatePlan());

        Button saveButton = new Button();
        saveButton.setText("Save plan");
        saveButton.setBackground(new Color(25, 118, 210));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("sansserif", Font.BOLD, 12));
        saveButton.addActionListener(e -> savePlan());

        buttonsRow.add(backButton);
        buttonsRow.add(generateButton);
        buttonsRow.add(saveButton);

        infoLabel = new JLabel(" ");
        infoLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(60, 60, 60));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(180, 40, 40));

        actions.add(buttonsRow);
        actions.add(Box.createVerticalStrut(4));
        actions.add(infoLabel);
        actions.add(Box.createVerticalStrut(2));
        actions.add(errorLabel);

        bottom.add(actions, BorderLayout.SOUTH);
    }

    private ListCellRenderer<? super Place> createPlaceRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Place) {
                    Place place = (Place) value;
                    lbl.setText(place.getName() + "  \u2013  " + place.getAddress());
                }
                lbl.setBorder(new EmptyBorder(2, 4, 2, 4));
                return lbl;
            }
        };
    }

    private void styleSecondaryButton(Button b) {
        b.setBackground(new Color(245, 245, 245));
        b.setForeground(new Color(70, 70, 70));
        b.setFont(new Font("sansserif", Font.PLAIN, 12));
    }

    // ===== API used from AppFrame =====

    public void setupForNewPlan() {
        editingPlanId = null;
        locationField.setText("");
        dateField.setText(LocalDate.now().toString());
        startTimeField.setText("13:00");
        recommendedModel.clear();
        selectedModel.clear();
        planPreviewArea.setText("");
        infoLabel.setText(" ");
        errorLabel.setText(" ");
    }

    public void editExistingPlan(Plan plan) {
        if (plan == null) return;
        editingPlanId = plan.getId();
        locationField.setText(plan.getOriginAddress());
        if (plan.getDate() != null) {
            dateField.setText(plan.getDate().toString());
        } else {
            dateField.setText("");
        }
        if (plan.getStartTime() != null) {
            startTimeField.setText(plan.getStartTime().toString());
        } else {
            startTimeField.setText("");
        }
        recommendedModel.clear();
        selectedModel.clear();
        if (plan.getRoute() != null && plan.getRoute().getStops() != null) {
            for (PlanStop stop : plan.getRoute().getStops()) {
                selectedModel.addElement(stop.getPlace());
            }
        }
        planPreviewArea.setText(buildPlanPreviewText(plan));
        infoLabel.setText("Editing existing plan: " + plan.getName());
        errorLabel.setText(" ");
    }

    // ===== Core actions =====

    private void searchPlaces() {
        Integer userId = appFrame.getCurrentUserId();
        if (userId == null) {
            JOptionPane.showMessageDialog(this,
                    "You must be logged in.",
                    "Not logged in",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loc = locationField.getText().trim();
        String date = dateField.getText().trim();

        if (loc.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both location and date.",
                    "Missing data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        planCreationController.searchPlaces(userId, loc, date);
        recommendedModel.clear();
        for (Place p : planCreationVM.getRecommendedPlaces()) {
            recommendedModel.addElement(p);
        }

        if (planCreationVM.getErrorMessage() != null) {
            errorLabel.setText(planCreationVM.getErrorMessage());
        } else {
            errorLabel.setText(" ");
        }
    }

    private void addSelectedPlaces() {
        List<Place> selected = recommendedList.getSelectedValuesList();
        for (Place p : selected) {
            if (!selectedModel.contains(p)) {
                selectedModel.addElement(p);
            }
        }
    }

    private void removeSelectedPlace() {
        Place sel = selectedList.getSelectedValue();
        if (sel != null) {
            selectedModel.removeElement(sel);
        }
    }

    private void moveSelectedPlace(int delta) {
        int idx = selectedList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        int newIdx = idx + delta;
        if (newIdx < 0 || newIdx >= selectedModel.size()) {
            return;
        }
        Place p = selectedModel.get(idx);
        selectedModel.remove(idx);
        selectedModel.add(newIdx, p);
        selectedList.setSelectedIndex(newIdx);
    }

    private List<Place> getSelectedPlacesList() {
        List<Place> list = new ArrayList<>();
        for (int i = 0; i < selectedModel.size(); i++) {
            list.add(selectedModel.getElementAt(i));
        }
        return list;
    }

    private void generatePlan() {
        Integer userId = appFrame.getCurrentUserId();
        if (userId == null) {
            JOptionPane.showMessageDialog(this,
                    "You must be logged in.",
                    "Not logged in",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loc = locationField.getText().trim();
        String date = dateField.getText().trim();
        String startTime = startTimeField.getText().trim();
        List<Place> selectedPlaces = getSelectedPlacesList();

        if (selectedPlaces.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one place.",
                    "No places selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        planCreationController.buildPlan(userId, loc, date, startTime, selectedPlaces, editingPlanId);

        if (planCreationVM.getErrorMessage() != null) {
            errorLabel.setText(planCreationVM.getErrorMessage());
            planPreviewArea.setText("");
            return;
        }

        errorLabel.setText(" ");
        Plan plan = planCreationVM.getPlanPreview();
        if (plan != null) {
            planPreviewArea.setText(buildPlanPreviewText(plan));
            String msg = planCreationVM.getInfoMessage() != null
                    ? planCreationVM.getInfoMessage()
                    : "Plan generated.";
            infoLabel.setText(msg);
        } else {
            planPreviewArea.setText("");
            infoLabel.setText(" ");
        }
    }

    private void savePlan() {
        String name = JOptionPane.showInputDialog(this,
                "Enter plan name:",
                "Save Plan",
                JOptionPane.QUESTION_MESSAGE);
        if (name == null) {
            return;
        }
        planCreationController.saveCurrentPlan(name);
        if (planCreationVM.getErrorMessage() != null) {
            JOptionPane.showMessageDialog(this,
                    planCreationVM.getErrorMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            String msg = planCreationVM.getInfoMessage() != null
                    ? planCreationVM.getInfoMessage()
                    : "Plan saved.";
            JOptionPane.showMessageDialog(this,
                    msg,
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String buildPlanPreviewText(Plan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("Location: ").append(plan.getOriginAddress()).append('\n');
        sb.append("Date: ").append(plan.getDate()).append('\n');
        sb.append("Start time: ").append(plan.getStartTime()).append('\n');
        sb.append("Timeline:\n");
        if (plan.getRoute() != null && plan.getRoute().getStops() != null) {
            for (PlanStop stop : plan.getRoute().getStops()) {
                sb.append(String.format("%s - %s  %s%n",
                        stop.getStartTime(),
                        stop.getEndTime(),
                        stop.getPlace().getName()));
            }
        }
        return sb.toString();
    }
}
