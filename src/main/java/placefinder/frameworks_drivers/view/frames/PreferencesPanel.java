package placefinder.frameworks_drivers.view.frames;

import placefinder.frameworks_drivers.view.components.swing.Button;
import placefinder.frameworks_drivers.view.components.swing.MyTextField;
import placefinder.frameworks_drivers.view.components.swing.PanelRound;
import placefinder.entities.DayTripExperienceCategories;
import placefinder.interface_adapters.controllers.PreferencesController;
import placefinder.interface_adapters.viewmodels.PreferencesViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * UI panel for displaying and editing user preferences including radius and categories.
 */
public class PreferencesPanel extends JPanel {

    private final AppFrame appFrame;
    private final PreferencesController preferencesController;
    private final PreferencesViewModel preferencesVM;

    private JSpinner radiusSpinner;
    private JComboBox<String> mainCategoryCombo;
    private JPanel subCategoriesPanel;
    private Map<String, Map<String, JCheckBox>> subCategoryCheckboxes = new HashMap<>();
    private Map<String, List<String>> currentSelectedCategories = new HashMap<>();
    private JLabel messageLabel;

    public PreferencesPanel(AppFrame appFrame,
                            PreferencesController preferencesController,
                            PreferencesViewModel preferencesVM) {
        this.appFrame = appFrame;
        this.preferencesController = preferencesController;
        this.preferencesVM = preferencesVM;
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Gradient background
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
        card.setBackground(new Color(234, 246, 234));
        card.setLayout(new BorderLayout(20, 20));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(card, gbc);

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Preferences");
        title.setFont(new Font("sansserif", Font.BOLD, 28));
        title.setForeground(new Color(40, 40, 40));

        JLabel subtitle = new JLabel("Set radius and interests to personalize your day trips.");
        subtitle.setFont(new Font("sansserif", Font.PLAIN, 15));
        subtitle.setForeground(new Color(120, 120, 120));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        Button backButton = new Button();
        backButton.setText("Back");
        backButton.setBackground(new Color(230, 230, 230));
        backButton.setForeground(new Color(60, 60, 60));
        backButton.setFont(new Font("sansserif", Font.BOLD, 12));
        backButton.addActionListener(e -> appFrame.showLogin());

        header.add(titleBox, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // ===== Center content =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.add(center, BorderLayout.CENTER);

        JLabel radiusLabel = new JLabel("Radius (km)");
        radiusLabel.setFont(new Font("sansserif", Font.BOLD, 13));
        radiusLabel.setForeground(new Color(70, 70, 70));
        center.add(radiusLabel);

        JLabel radiusHint = new JLabel("0 – 5 km");
        radiusHint.setFont(new Font("sansserif", Font.PLAIN, 11));
        radiusHint.setForeground(new Color(140, 140, 140));
        center.add(radiusHint);
        center.add(Box.createVerticalStrut(5));

        radiusSpinner = new JSpinner(new SpinnerNumberModel(2.0, 0.0, 5.0, 0.5));
        radiusSpinner.setFont(new Font("sansserif", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) radiusSpinner.getEditor()).getTextField().setColumns(4);
        center.add(radiusSpinner);
        center.add(Box.createVerticalStrut(20));

        // --- Categories selection ---
        JLabel categoriesLabel = new JLabel("Categories (at least 3 sub-categories required)");
        categoriesLabel.setFont(new Font("sansserif", Font.BOLD, 13));
        categoriesLabel.setForeground(new Color(70, 70, 70));
        center.add(categoriesLabel);
        center.add(Box.createVerticalStrut(5));

        JLabel categoryHint = new JLabel("Select a main category, then choose sub-categories");
        categoryHint.setFont(new Font("sansserif", Font.PLAIN, 11));
        categoryHint.setForeground(new Color(140, 140, 140));
        center.add(categoryHint);
        center.add(Box.createVerticalStrut(5));

        List<String> mainCategories = DayTripExperienceCategories.getMainCategories();
        mainCategoryCombo = new JComboBox<>(mainCategories.toArray(new String[0]));
        mainCategoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                String displayName = "";
                if (value != null) {
                    String category = (String) value;
                    displayName = DayTripExperienceCategories.getDisplayName(category);
                }
                return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            }
        });
        mainCategoryCombo.setSelectedIndex(-1);
        mainCategoryCombo.addActionListener(e -> updateSubCategoriesPanel());
        center.add(mainCategoryCombo);
        center.add(Box.createVerticalStrut(8));

        subCategoriesPanel = new JPanel();
        subCategoriesPanel.setLayout(new BoxLayout(subCategoriesPanel, BoxLayout.Y_AXIS));
        subCategoriesPanel.setOpaque(false);
        JScrollPane subCategoriesScroll = new JScrollPane(subCategoriesPanel);
        subCategoriesScroll.setBorder(BorderFactory.createEmptyBorder());
        subCategoriesScroll.setPreferredSize(new Dimension(0, 200));
        center.add(subCategoriesScroll);

        center.add(Box.createVerticalStrut(15));

        Button saveButton = new Button();
        saveButton.setText("Save Preferences");
        saveButton.setBackground(new Color(7, 164, 121));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("sansserif", Font.BOLD, 13));
        saveButton.addActionListener(e -> savePreferences());
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(saveButton);

        // ===== Footer message =====
        messageLabel = new JLabel(" ", SwingConstants.LEFT);
        messageLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(120, 120, 120));
        card.add(messageLabel, BorderLayout.SOUTH);
    }

    // ===== Public API for AppFrame =====

    public void loadForCurrentUser() {
        Integer userId = appFrame.getCurrentUserId();
        if (userId == null) return;
        preferencesController.loadPreferences(userId);

        // radius
        radiusSpinner.setValue(preferencesVM.getRadiusKm());

        // categories
        Map<String, List<String>> savedCategories = preferencesVM.getSelectedCategories();
        subCategoryCheckboxes.clear();
        currentSelectedCategories = new HashMap<>(savedCategories != null ? savedCategories : new HashMap<>());
        mainCategoryCombo.setSelectedIndex(-1);
        if (savedCategories != null && !savedCategories.isEmpty()) {
            String firstMainCategory = savedCategories.keySet().iterator().next();
            mainCategoryCombo.setSelectedItem(firstMainCategory);
            updateSubCategoriesPanel();
        } else {
            updateSubCategoriesPanel();
        }

        String msg = preferencesVM.getErrorMessage() != null
                ? preferencesVM.getErrorMessage()
                : preferencesVM.getMessage();
        messageLabel.setText(msg != null ? msg : " ");
    }

    // ===== Internal helpers =====

    private void updateSubCategoriesPanel() {
        saveCurrentMainCategorySelection();
        
        subCategoriesPanel.removeAll();
        String selectedMainCategory = (String) mainCategoryCombo.getSelectedItem();
        if (selectedMainCategory == null) {
            subCategoriesPanel.revalidate();
            subCategoriesPanel.repaint();
            return;
        }

        List<String> subCategories = DayTripExperienceCategories.getSubCategories(selectedMainCategory);
        Map<String, JCheckBox> checkboxes = new HashMap<>();
        
        for (String subCategory : subCategories) {
            JCheckBox cb = new JCheckBox(formatSubCategoryName(subCategory));
            cb.setOpaque(false);
            cb.setFont(new Font("sansserif", Font.PLAIN, 11));
            cb.setForeground(new Color(60, 60, 60));
            
            boolean isSelected = false;
            if (currentSelectedCategories.containsKey(selectedMainCategory)) {
                List<String> selectedSubs = currentSelectedCategories.get(selectedMainCategory);
                isSelected = selectedSubs != null && selectedSubs.contains(subCategory);
            } else {
                Map<String, List<String>> savedCategories = preferencesVM.getSelectedCategories();
                if (savedCategories != null && savedCategories.containsKey(selectedMainCategory)) {
                    List<String> selectedSubs = savedCategories.get(selectedMainCategory);
                    isSelected = selectedSubs != null && selectedSubs.contains(subCategory);
                }
            }
            
            cb.setSelected(isSelected);
            
            checkboxes.put(subCategory, cb);
            subCategoriesPanel.add(cb);
        }
        
        subCategoryCheckboxes.put(selectedMainCategory, checkboxes);
        subCategoriesPanel.revalidate();
        subCategoriesPanel.repaint();
    }

    private void saveCurrentMainCategorySelection() {
        String currentMainCategory = (String) mainCategoryCombo.getSelectedItem();
        if (currentMainCategory != null && subCategoryCheckboxes.containsKey(currentMainCategory)) {
            List<String> selectedSubs = new ArrayList<>();
            Map<String, JCheckBox> checkboxes = subCategoryCheckboxes.get(currentMainCategory);
            for (Map.Entry<String, JCheckBox> entry : checkboxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedSubs.add(entry.getKey());
                }
            }
            if (!selectedSubs.isEmpty()) {
                currentSelectedCategories.put(currentMainCategory, selectedSubs);
            } else {
                currentSelectedCategories.remove(currentMainCategory);
            }
        }
    }

    private String formatSubCategoryName(String subCategory) {
        if (subCategory == null || subCategory.isEmpty()) {
            return subCategory;
        }
        String[] parts = subCategory.split("\\.");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            String formatted = lastPart.replace("_", " ");
            if (!formatted.isEmpty()) {
                formatted = Character.toUpperCase(formatted.charAt(0)) + 
                           (formatted.length() > 1 ? formatted.substring(1) : "");
            }
            return formatted;
        }
        return subCategory;
    }

    private void savePreferences() {
        Integer userId = appFrame.getCurrentUserId();
        if (userId == null) return;

        double radius = ((Number) radiusSpinner.getValue()).doubleValue();

        saveCurrentMainCategorySelection();
        
        Map<String, List<String>> selectedCategories = new HashMap<>(currentSelectedCategories);

        preferencesController.savePreferences(userId, radius, selectedCategories);

        if (preferencesVM.getErrorMessage() != null) {
            JOptionPane.showMessageDialog(this,
                    preferencesVM.getErrorMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        String msg = preferencesVM.getMessage();
        messageLabel.setText(msg != null ? msg : " ");
    }
}

