package main;

import frameworks_and_drivers.di.DependencyContainer;
import frameworks_and_drivers.swing.frames.LoginFrame;

import javax.swing.*;
import java.io.File;

public class TravelSchedulerApp {
    private static DependencyContainer dependencyContainer;

    public static void main(String[] args) {
        // Initialize data directory
        initializeDataDirectory();

        // Set system look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }

        // Initialize dependency injection container
        dependencyContainer = DependencyContainer.getInstance();

        // Start the application
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = dependencyContainer.provideLoginFrame();
            loginFrame.setVisible(true);
            loginFrame.setLocationRelativeTo(null); // Center the window
        });

        // Add shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dependencyContainer != null) {
                dependencyContainer.cleanup();
            }
        }));
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

    public static DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
}