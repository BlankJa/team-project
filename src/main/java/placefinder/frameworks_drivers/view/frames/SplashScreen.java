package placefinder.frameworks_drivers.view.frames;

import javax.swing.*;
import java.awt.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class SplashScreen extends JWindow {
    
    private JFXPanel fxPanel;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private boolean videoReady = false;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    
    public SplashScreen() {
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        fxPanel = new JFXPanel();
        fxPanel.setBackground(Color.BLACK);
        
        Timer initTimer = new Timer(200, null);
        initTimer.addActionListener(e -> {
            initTimer.stop();
            initializeVideo();
        });
        initTimer.setRepeats(false);
        initTimer.start();
        
        setLayout(new BorderLayout());
        add(fxPanel, BorderLayout.CENTER);
    }
    
    /**
     * Initialize video player
     */
    private void initializeVideo() {
        // Initialize media player in JavaFX application thread
        Platform.runLater(() -> {
            try {
                java.net.URL videoUrl = getClass().getResource("/icons/logo.mp4");
                if (videoUrl == null) {
                    throw new Exception("Cannot find video file: /icons/logo.mp4");
                }
                
                String videoPath;
                try {
                    videoPath = videoUrl.toURI().toURL().toExternalForm();
                } catch (Exception e) {
                    videoPath = videoUrl.toExternalForm();
                }
                
                System.out.println("Video path: " + videoPath);
                
                final Media media = new Media(videoPath);
                
                media.setOnError(() -> {
                    System.err.println("Media loading error: " + media.getError());
                    handleMediaError();
                });
                
                mediaPlayer = new MediaPlayer(media);
                
                mediaPlayer.setMute(true);
                
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                
                mediaPlayer.setOnError(() -> {
                    System.err.println("Player error: " + mediaPlayer.getError());
                    if (mediaPlayer.getError() != null) {
                        System.err.println("Error type: " + mediaPlayer.getError().getType());
                        System.err.println("Error message: " + mediaPlayer.getError().getMessage());
                    }
                    handleMediaError();
                });
                
                mediaView = new MediaView(mediaPlayer);
                mediaView.setPreserveRatio(true);
                mediaView.setSmooth(true);
                
                StackPane root = new StackPane();
                root.setStyle("-fx-background-color: black;");
                root.getChildren().add(mediaView);
                Scene scene = new Scene(root, 1000, 700);
                root.setCache(true);
                fxPanel.setScene(scene);
                
                mediaPlayer.setOnReady(() -> {
                    final double videoWidth = media.getWidth();
                    final double videoHeight = media.getHeight();
                    
                    // If video dimensions are valid, adjust window and view size
                    if (videoWidth > 0 && videoHeight > 0) {
                        // Update MediaView size in JavaFX thread (using original video dimensions)
                        mediaView.setFitWidth(videoWidth);
                        mediaView.setFitHeight(videoHeight);
                        
                        // Update root node size to match video dimensions
                        root.setPrefSize(videoWidth, videoHeight);
                        root.setMinSize(videoWidth, videoHeight);
                        root.setMaxSize(videoWidth, videoHeight);
                        
                        // Adjust window size in Swing thread
                        SwingUtilities.invokeLater(() -> {
                            setSize((int)videoWidth, (int)videoHeight);
                            setLocationRelativeTo(null);
                            fxPanel.setPreferredSize(new Dimension((int)videoWidth, (int)videoHeight));
                            fxPanel.setSize((int)videoWidth, (int)videoHeight);
                            revalidate();
                            repaint();
                        });
                    }
                    
                    // After video is ready, seek to start position
                    mediaPlayer.seek(javafx.util.Duration.ZERO);
                    
                    // Mark video as ready
                    videoReady = true;
                });
                
                // Listen for playback status changes to ensure video actually starts playing
                mediaPlayer.statusProperty().addListener((observable, oldStatus, newStatus) -> {
                    if (newStatus == javafx.scene.media.MediaPlayer.Status.PLAYING) {
                        // Video started playing, ensure view is visible
                        Platform.runLater(() -> {
                            if (mediaView != null) {
                                mediaView.setVisible(true);
                            }
                        });
                    } else if (newStatus == javafx.scene.media.MediaPlayer.Status.UNKNOWN) {
                        // If status becomes UNKNOWN, might be initialization issue, may need retry
                        System.err.println("Video status became UNKNOWN, may need retry");
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Video initialization failed: " + e.getMessage());
                handleMediaError();
            }
        });
    }
    
    /**
     * Handle media errors and attempt retry
     */
    private void handleMediaError() {
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            System.out.println("Attempting to reload video (attempt " + retryCount + ")");
            
            // Clean up current player
            if (mediaPlayer != null) {
                Platform.runLater(() -> {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                    } catch (Exception e) {
                        // Ignore cleanup errors
                    }
                    mediaPlayer = null;
                });
            }
            
            // Retry after delay
            Timer retryTimer = new Timer(500, null);
            retryTimer.addActionListener(e -> {
                retryTimer.stop();
                initializeVideo();
            });
            retryTimer.setRepeats(false);
            retryTimer.start();
        } else {
            // Retry limit reached, show error
            System.err.println("Video loading failed after " + MAX_RETRIES + " retries");
            SwingUtilities.invokeLater(() -> {
                fxPanel.removeAll();
                JLabel errorLabel = new JLabel("Cannot load video", JLabel.CENTER);
                errorLabel.setForeground(Color.WHITE);
                errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                fxPanel.setLayout(new BorderLayout());
                fxPanel.add(errorLabel, BorderLayout.CENTER);
                fxPanel.revalidate();
                fxPanel.repaint();
            });
        }
    }
    
    public void showSplash(int durationMillis) {
        // Wait for video to be ready before showing window
        waitForVideoReady(() -> {
            setVisible(true);
            // Ensure video plays immediately when shown to avoid stuttering
            Platform.runLater(() -> {
                if (mediaPlayer != null) {
                    // Seek to start position first to ensure video is ready
                    mediaPlayer.seek(javafx.util.Duration.ZERO);
                    // Wait a short time to ensure video frame is loaded before playing
                    Timer playTimer = new Timer(100, e -> {
                        Platform.runLater(() -> {
                            if (mediaPlayer != null) {
                                mediaPlayer.play();
                            }
                        });
                    });
                    playTimer.setRepeats(false);
                    playTimer.start();
                }
            });
        });
        
        // Close splash screen after specified duration
        Timer timer = new Timer(durationMillis, e -> {
            if (mediaPlayer != null) {
                Platform.runLater(() -> {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                });
            }
            dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    public void showSplash(Runnable onClose) {
        // Wait for video to be ready before showing window
        waitForVideoReady(() -> {
            setVisible(true);
            // Ensure video plays immediately when shown to avoid stuttering
            Platform.runLater(() -> {
                if (mediaPlayer != null) {
                    // Seek to start position first to ensure video is ready
                    mediaPlayer.seek(javafx.util.Duration.ZERO);
                    // Wait a short time to ensure video frame is loaded before playing
                    Timer playTimer = new Timer(100, e -> {
                        Platform.runLater(() -> {
                            if (mediaPlayer != null) {
                                mediaPlayer.play();
                            }
                        });
                    });
                    playTimer.setRepeats(false);
                    playTimer.start();
                }
            });
            
            // Use SwingWorker to wait in background thread, then close splash screen
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Wait for application initialization to complete
                    Thread.sleep(7000); // Display for 7 seconds
                    return null;
                }
                
                @Override
                protected void done() {
                    if (mediaPlayer != null) {
                        Platform.runLater(() -> {
                            mediaPlayer.stop();
                            mediaPlayer.dispose();
                        });
                    }
                    dispose();
                    if (onClose != null) {
                        SwingUtilities.invokeLater(onClose);
                    }
                }
            };
            worker.execute();
        });
    }
    
    /**
     * Wait for video to be ready before executing callback
     */
    private void waitForVideoReady(Runnable callback) {
        if (videoReady) {
            // Video is ready, execute callback directly
            callback.run();
        } else {
            // Video not ready, wait up to 2 seconds
            Timer checkTimer = new Timer(50, null);
            final long startTime = System.currentTimeMillis();
            checkTimer.addActionListener(e -> {
                if (videoReady || (System.currentTimeMillis() - startTime) > 2000) {
                    checkTimer.stop();
                    callback.run();
                }
            });
            checkTimer.start();
        }
    }
}

