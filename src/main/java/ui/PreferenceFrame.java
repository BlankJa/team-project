package ui;

import model.User;
import model.PreferenceProfile;
import service.DatabaseService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class PreferenceFrame extends JFrame {
    private User currentUser;
    private DatabaseService databaseService;
    private JComboBox<String> preferredCityComboBox;
    private JSlider radiusSlider;
    private JList<String> interestsList;
    private JList<String> citiesList;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton addCityButton;
    private JButton removeCityButton;
    private DefaultListModel<String> citiesModel;

    public PreferenceFrame(User user) {
        this.currentUser = user;
        this.databaseService = DatabaseService.getInstance();
        initializeUI();
        loadCurrentPreferences();
    }

    private void initializeUI() {
        setTitle("Travel Preferences");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Set Your Travel Preferences", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(70, 130, 180));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Basic Preferences", createBasicPreferencesPanel());
        tabbedPane.addTab("Manage Cities", createCitiesManagementPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 248, 255));

        saveButton = new JButton("Save Preferences");
        saveButton.setBackground(new Color(70, 130, 180));
        saveButton.setForeground(Color.BLACK);
        saveButton.setFocusPainted(false);

        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(100, 149, 237));
        cancelButton.setForeground(Color.BLUE);
        cancelButton.setFocusPainted(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        saveButton.addActionListener(new SavePreferencesAction());
        cancelButton.addActionListener(e -> dispose());
    }

    private JPanel createBasicPreferencesPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Preferred City:"));
        preferredCityComboBox = new JComboBox<>();
        panel.add(preferredCityComboBox);

        panel.add(new JLabel("Search Radius (km):"));
        radiusSlider = new JSlider(1, 50, 10);
        radiusSlider.setMajorTickSpacing(10);
        radiusSlider.setMinorTickSpacing(1);
        radiusSlider.setPaintTicks(true);
        radiusSlider.setPaintLabels(true);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(1, new JLabel("1"));
        labelTable.put(10, new JLabel("10"));
        labelTable.put(20, new JLabel("20"));
        labelTable.put(30, new JLabel("30"));
        labelTable.put(40, new JLabel("40"));
        labelTable.put(50, new JLabel("50"));
        radiusSlider.setLabelTable(labelTable);
        panel.add(radiusSlider);

        panel.add(new JLabel("Interests:"));
        String[] interests = {"Museums", "Parks", "Shopping", "Restaurants",
                "Historical Sites", "Art Galleries", "Nightlife",
                "Sports", "Beaches", "Mountains", "Food Tours", "Local Markets"};
        interestsList = new JList<>(interests);
        interestsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(interestsList);
        scrollPane.setPreferredSize(new Dimension(200, 100));
        panel.add(scrollPane);

        return panel;
    }

    private JPanel createCitiesManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Manage Your Cities", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(70, 130, 180));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Your Cities"));

        citiesModel = new DefaultListModel<>();
        citiesList = new JList<>(citiesModel);
        citiesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(citiesList);
        listPanel.add(listScrollPane, BorderLayout.CENTER);

        panel.add(listPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        addCityButton = new JButton("Add City");
        addCityButton.setBackground(new Color(70, 130, 180));
        addCityButton.setForeground(Color.BLACK);
        addCityButton.setFocusPainted(false);

        removeCityButton = new JButton("Remove Selected");
        removeCityButton.setBackground(new Color(220, 80, 80));
        removeCityButton.setForeground(Color.BLACK);
        removeCityButton.setFocusPainted(false);

        buttonPanel.add(addCityButton);
        buttonPanel.add(removeCityButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        addCityButton.addActionListener(new AddCityAction());
        removeCityButton.addActionListener(new RemoveCityAction());

        return panel;
    }

    private void loadCurrentPreferences() {
        PreferenceProfile profile = currentUser.getProfile();
        // Load cities from PreferenceProfile
        String[] cities = profile.getCities();
        citiesModel.clear();
        for (String city : cities) {
            if (!city.trim().isEmpty()) {
                citiesModel.addElement(city);
            }
        }
        updatePreferredCityComboBox();

        radiusSlider.setValue((int) profile.getRadius());

        String[] currentInterests = profile.getInterests();
        if (currentInterests.length > 0) {
            int[] indices = new int[currentInterests.length];
            for (int i = 0; i < currentInterests.length; i++) {
                for (int j = 0; j < interestsList.getModel().getSize(); j++) {
                    if (interestsList.getModel().getElementAt(j).equals(currentInterests[i])) {
                        indices[i] = j;
                        break;
                    }
                }
            }
            interestsList.setSelectedIndices(indices);
        }

        if (cities.length > 0 && !cities[0].isEmpty()) {
            preferredCityComboBox.setSelectedItem(cities[0]);
        }
    }

    private void updatePreferredCityComboBox() {
        preferredCityComboBox.removeAllItems();
        for (int i = 0; i < citiesModel.size(); i++) {
            preferredCityComboBox.addItem(citiesModel.getElementAt(i));
        }

        // Add default cities if list is empty
        if (citiesModel.isEmpty()) {
            String[] defaultCities = {"Toronto", "Vancouver", "Ottawa", "New York"};
            for (String city : defaultCities) {
                citiesModel.addElement(city);
                preferredCityComboBox.addItem(city);
            }
        }
    }

    private class AddCityAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String newCity = JOptionPane.showInputDialog(
                    PreferenceFrame.this,
                    "Enter city name:",
                    "Add City",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (newCity != null && !newCity.trim().isEmpty()) {
                String city = newCity.trim();
                if (!citiesModel.contains(city)) {
                    citiesModel.addElement(city);
                    updatePreferredCityComboBox();
                    preferredCityComboBox.setSelectedItem(city);
                } else {
                    JOptionPane.showMessageDialog(
                            PreferenceFrame.this,
                            "City '" + city + "' already exists in your list.",
                            "Duplicate City",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        }
    }

    private class RemoveCityAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = citiesList.getSelectedIndex();
            if (selectedIndex != -1) {
                String cityToRemove = citiesModel.getElementAt(selectedIndex);
                int confirm = JOptionPane.showConfirmDialog(
                        PreferenceFrame.this,
                        "Are you sure you want to remove '" + cityToRemove + "' from your cities list?",
                        "Confirm Removal",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    citiesModel.remove(selectedIndex);
                    updatePreferredCityComboBox();

                    if (preferredCityComboBox.getItemCount() > 0) {
                        preferredCityComboBox.setSelectedIndex(0);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        PreferenceFrame.this,
                        "Please select a city to remove.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    private class SavePreferencesAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedCity = (String) preferredCityComboBox.getSelectedItem();
            float radius = radiusSlider.getValue();
            java.util.List<String> selectedInterestsList = interestsList.getSelectedValuesList();
            String[] selectedInterests = selectedInterestsList.toArray(new String[0]);

            // Get all cities from the list
            String[] allCities = new String[citiesModel.size()];
            for (int i = 0; i < citiesModel.size(); i++) {
                allCities[i] = citiesModel.getElementAt(i);
            }

            // Ensure preferred city is first in the array
            String[] citiesWithPreferredFirst;
            if (selectedCity != null && !selectedCity.isEmpty()) {
                List<String> citiesList = new ArrayList<>(Arrays.asList(allCities));
                citiesList.remove(selectedCity);
                citiesList.add(0, selectedCity);
                citiesWithPreferredFirst = citiesList.toArray(new String[0]);
            } else {
                citiesWithPreferredFirst = allCities;
            }

            // Create new profile with all preferences including cities
            PreferenceProfile newProfile = new PreferenceProfile(
                    selectedInterests, new String[]{selectedCity}, radius, citiesWithPreferredFirst
            );

            // Update user profile
            currentUser.updateProfile(newProfile);

            // Save to database
            boolean success = databaseService.updateUserPreferences(currentUser.getEmail(), newProfile);

            if (success) {
                JOptionPane.showMessageDialog(PreferenceFrame.this,
                        "Preferences saved successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Display saved preferences summary
                StringBuilder summary = new StringBuilder("Saved Preferences:\n\n");
                summary.append("Preferred City: ").append(selectedCity != null ? selectedCity : "None").append("\n");
                summary.append("Search Radius: ").append(radius).append(" km\n");
                summary.append("Interests: ");
                if (selectedInterests.length > 0) {
                    summary.append(String.join(", ", selectedInterests));
                } else {
                    summary.append("None selected");
                }
                summary.append("\n");
                summary.append("Cities: ").append(String.join(", ", citiesWithPreferredFirst));

                JOptionPane.showMessageDialog(PreferenceFrame.this,
                        summary.toString(), "Preferences Summary",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(PreferenceFrame.this,
                        "Failed to save preferences. Please try again.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}