package frameworks_and_drivers.swing.utils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SwingWorkerHelper {

    /**
     * Execute a task in background and handle results on EDT
     */
    public static <T> void executeBackgroundTask(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Exception> onError,
            Runnable onComplete) {

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return backgroundTask.get();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    SwingUtilities.invokeLater(() -> {
                        if (onSuccess != null) {
                            onSuccess.accept(result);
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        if (onError != null) {
                            onError.accept(e);
                        } else {
                            // Default error handling
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Operation failed: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });
                }
            }
        }.execute();
    }

    /**
     * Execute a task with progress updates
     */
    public static <T> void executeTaskWithProgress(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Exception> onError,
            Runnable onComplete,
            JProgressBar progressBar,
            JButton actionButton) {

        if (progressBar != null) {
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
        }

        if (actionButton != null) {
            actionButton.setEnabled(false);
        }

        executeBackgroundTask(
                backgroundTask,
                result -> {
                    if (progressBar != null) {
                        progressBar.setVisible(false);
                    }
                    if (actionButton != null) {
                        actionButton.setEnabled(true);
                    }
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                },
                error -> {
                    if (progressBar != null) {
                        progressBar.setVisible(false);
                    }
                    if (actionButton != null) {
                        actionButton.setEnabled(true);
                    }
                    if (onError != null) {
                        onError.accept(error);
                    }
                },
                onComplete
        );
    }

    /**
     * Execute a task with loading dialog
     */
    public static <T> void executeTaskWithLoadingDialog(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Exception> onError,
            Component parent,
            String loadingMessage) {

        JDialog loadingDialog = UIHelper.createLoadingDialog(parent, loadingMessage);

        executeBackgroundTask(
                backgroundTask,
                result -> {
                    loadingDialog.dispose();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                },
                error -> {
                    loadingDialog.dispose();
                    if (onError != null) {
                        onError.accept(error);
                    }
                },
                null
        );

        loadingDialog.setVisible(true);
    }

    /**
     * Chain multiple background tasks
     */
    @SafeVarargs
    public static void executeTaskChain(Runnable... tasks) {
        if (tasks == null || tasks.length == 0) {
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (Runnable task : tasks) {
                    task.run();
                }
                return null;
            }

            @Override
            protected void done() {
                // Chain completed
            }
        }.execute();
    }

    /**
     * Execute a task with retry logic
     */
    public static <T> void executeWithRetry(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Exception> onError,
            int maxRetries,
            long retryDelayMs) {

        new SwingWorker<T, Void>() {
            private int retryCount = 0;

            @Override
            protected T doInBackground() throws Exception {
                while (retryCount <= maxRetries) {
                    try {
                        return backgroundTask.get();
                    } catch (Exception e) {
                        retryCount++;
                        if (retryCount > maxRetries) {
                            throw e;
                        }
                        // Wait before retry
                        Thread.sleep(retryDelayMs);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    SwingUtilities.invokeLater(() -> {
                        if (onSuccess != null) {
                            onSuccess.accept(result);
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        if (onError != null) {
                            onError.accept(e);
                        }
                    });
                }
            }
        }.execute();
    }

    /**
     * Execute multiple tasks in parallel and combine results
     */
    @SafeVarargs
    public static void executeParallelTasks(
            Runnable onComplete,
            Supplier<?>... tasks) {

        if (tasks == null || tasks.length == 0) {
            if (onComplete != null) {
                SwingUtilities.invokeLater(onComplete);
            }
            return;
        }

        CompletableFuture<?>[] futures = new CompletableFuture[tasks.length];

        for (int i = 0; i < tasks.length; i++) {
            final int index = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    return tasks[index].get();
                } catch (Exception e) {
                    throw new RuntimeException("Task " + index + " failed", e);
                }
            });
        }

        CompletableFuture.allOf(futures)
                .whenComplete((result, throwable) -> {
                    SwingUtilities.invokeLater(() -> {
                        if (throwable != null) {
                            // Handle error
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Parallel execution failed: " + throwable.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });
                });
    }

    /**
     * Debounce task execution - useful for search inputs
     */
    public static class Debouncer {
        private Timer timer;

        public void debounce(Runnable task, int delayMs) {
            if (timer != null) {
                timer.stop();
            }
            timer = new Timer(delayMs, e -> {
                task.run();
                timer = null;
            });
            timer.setRepeats(false);
            timer.start();
        }

        public void cancel() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }
    }

    /**
     * Throttle task execution - limit execution rate
     */
    public static class Throttler {
        private long lastExecutionTime = 0;
        private final long throttleDelay;

        public Throttler(long throttleDelayMs) {
            this.throttleDelay = throttleDelayMs;
        }

        public void throttle(Runnable task) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastExecutionTime >= throttleDelay) {
                task.run();
                lastExecutionTime = currentTime;
            }
        }
    }

    /**
     * Simple progress tracking for long-running tasks
     */
    public static class ProgressTracker {
        private final JProgressBar progressBar;
        private final int totalSteps;
        private int currentStep = 0;

        public ProgressTracker(JProgressBar progressBar, int totalSteps) {
            this.progressBar = progressBar;
            this.totalSteps = totalSteps;
            if (progressBar != null) {
                progressBar.setMinimum(0);
                progressBar.setMaximum(totalSteps);
                progressBar.setValue(0);
            }
        }

        public void updateProgress(String message) {
            currentStep++;
            if (progressBar != null) {
                progressBar.setValue(currentStep);
            }
        }

        public void complete() {
            if (progressBar != null) {
                progressBar.setValue(totalSteps);
            }
        }
    }
}