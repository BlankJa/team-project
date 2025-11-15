package main;

import ui.LoginFrame;
import javax.swing.*;
import java.io.File;

public class TravelSchedulerApp {
    public static void main(String[] args) {
        // Initialize data directory
        initializeDataDirectory();

        // Set system look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }

        // Start the application - open Login Frame
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            loginFrame.setLocationRelativeTo(null); // Center the window
        });
    }

    private static void initializeDataDirectory() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                System.out.println("Created data directory: " + dataDir.getAbsolutePath());
            }
        }
    }
}