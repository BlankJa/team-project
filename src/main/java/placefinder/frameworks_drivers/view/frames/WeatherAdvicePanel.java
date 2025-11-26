package placefinder.frameworks_drivers.view.frames;

import placefinder.frameworks_drivers.view.components.swing.Button;
import placefinder.frameworks_drivers.view.components.swing.MyTextField;
import placefinder.frameworks_drivers.view.components.swing.PanelRound;
import placefinder.interface_adapters.controllers.WeatherAdviceController;
import placefinder.interface_adapters.viewmodels.WeatherAdviceViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class WeatherAdvicePanel extends JPanel {

    private final AppFrame appFrame;
    private final WeatherAdviceController weatherAdviceController;
    private final WeatherAdviceViewModel weatherAdviceVM;

    private MyTextField locationField;
    private MyTextField dateField;
    private JTextArea summaryArea;
    private JTextArea adviceArea;
    private JLabel errorLabel;

    public WeatherAdvicePanel(AppFrame appFrame,
                              WeatherAdviceController weatherAdviceController,
                              WeatherAdviceViewModel weatherAdviceVM) {
        this.appFrame = appFrame;
        this.weatherAdviceController = weatherAdviceController;
        this.weatherAdviceVM = weatherAdviceVM;
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

        // --- MAIN CARD (Light Green Background) ---
        PanelRound card = new PanelRound();
        card.setBackground(new Color(234, 246, 234));
        card.setLayout(new BorderLayout(20, 20));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(card, gbc);

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Weather advice");
        title.setFont(new Font("sansserif", Font.BOLD, 28));
        title.setForeground(new Color(40, 40, 40));

        JLabel subtitle = new JLabel("Check the weather and get packing suggestions before you head out.");
        subtitle.setFont(new Font("sansserif", Font.PLAIN, 15));
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

        // --- Center content ---
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        card.add(center, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();

        // =========================================
        // Left side: Inputs + Fixed Image
        // =========================================
        PanelRound left = new PanelRound();
        left.setBackground(Color.WHITE);
        left.setLayout(new GridBagLayout());
        left.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints ic = new GridBagConstraints();

        ic.gridx = 0;
        ic.gridy = 0;
        ic.weightx = 1;
        ic.fill = GridBagConstraints.HORIZONTAL;
        ic.insets = new Insets(0, 0, 15, 2);

        int frameWidth = appFrame.getWidth() > 0 ? appFrame.getWidth() : 1000;
        int targetImgWidth = (int) (frameWidth * 0.4) - 80;
        if (targetImgWidth < 250) targetImgWidth = 250;

        FixedImagePanel imagePanel = new FixedImagePanel("/icons/Weather.png", targetImgWidth);
        left.add(imagePanel, ic);

        ic.insets = new Insets(6, 4, 6, 4);
        ic.gridy++;

        JLabel locLabel = new JLabel("Location");
        locLabel.setFont(new Font("sansserif", Font.BOLD, 13));
        locLabel.setForeground(new Color(60, 60, 60));
        left.add(locLabel, ic);

        ic.gridy++;
        locationField = new MyTextField();
        locationField.setHint("City or address (e.g., Toronto)");
        left.add(locationField, ic);

        ic.gridy++;
        JLabel dateLabel = new JLabel("Date (optional)");
        dateLabel.setFont(new Font("sansserif", Font.BOLD, 13));
        dateLabel.setForeground(new Color(60, 60, 60));
        left.add(dateLabel, ic);

        ic.gridy++;
        dateField = new MyTextField();
        dateField.setHint("YYYY-MM-DD (blank = today)");
        left.add(dateField, ic);

        ic.gridy++;
        ic.weighty = 1;
        ic.anchor = GridBagConstraints.NORTHWEST;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        left.add(spacer, ic);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.4;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        center.add(left, c);

        // =========================================
        // Right side: Output
        // =========================================
        PanelRound right = new PanelRound();
        right.setBackground(Color.WHITE);
        right.setLayout(new GridBagLayout());
        right.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(5, 5, 5, 5);
        rc.gridx = 0;
        rc.gridy = 0;
        rc.weightx = 1;
        rc.fill = GridBagConstraints.HORIZONTAL;

        JLabel summaryLabel = new JLabel("Weather summary");
        summaryLabel.setFont(new Font("sansserif", Font.BOLD, 13));
        summaryLabel.setForeground(new Color(60, 60, 60));
        right.add(summaryLabel, rc);

        rc.gridy++;
        rc.weighty = 0.4;
        rc.fill = GridBagConstraints.BOTH;
        summaryArea = new JTextArea(4, 30);
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setFont(new Font("sansserif", Font.PLAIN, 12));
        JScrollPane sc1 = new JScrollPane(summaryArea);
        sc1.setBorder(BorderFactory.createEmptyBorder());
        right.add(sc1, rc);

        rc.gridy++;
        rc.weighty = 0;
        rc.fill = GridBagConstraints.HORIZONTAL;
        JLabel adviceLabel = new JLabel("Packing & clothing advice");
        adviceLabel.setFont(new Font("sansserif", Font.BOLD, 13));
        adviceLabel.setForeground(new Color(60, 60, 60));
        right.add(adviceLabel, rc);

        rc.gridy++;
        rc.weighty = 0.6;
        rc.fill = GridBagConstraints.BOTH;
        adviceArea = new JTextArea(6, 30);
        adviceArea.setEditable(false);
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);
        adviceArea.setFont(new Font("sansserif", Font.PLAIN, 12));
        JScrollPane sc2 = new JScrollPane(adviceArea);
        sc2.setBorder(BorderFactory.createEmptyBorder());
        right.add(sc2, rc);

        // --- RIGHT CARD ALIGNMENT ---
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.6;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        center.add(right, c);

        // --- Bottom row ---
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonsRow.setOpaque(false);

        Button getAdviceButton = new Button();
        getAdviceButton.setText("Get advice");
        getAdviceButton.setBackground(new Color(7, 164, 121));
        getAdviceButton.setForeground(Color.WHITE);
        getAdviceButton.setFont(new Font("sansserif", Font.BOLD, 12));
        getAdviceButton.setPreferredSize(new Dimension(130, 32));
        getAdviceButton.addActionListener(e -> getAdvice());

        buttonsRow.add(getAdviceButton);
        bottom.add(buttonsRow, BorderLayout.EAST);

        errorLabel = new JLabel(" ", SwingConstants.LEFT);
        errorLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(180, 40, 40));
        bottom.add(errorLabel, BorderLayout.WEST);

        card.add(bottom, BorderLayout.SOUTH);
    }

    public void resetFields() {
        locationField.setText("");
        dateField.setText("");
        summaryArea.setText("");
        adviceArea.setText("");
        errorLabel.setText(" ");
    }

    private void getAdvice() {
        String location = locationField.getText().trim();
        String date = dateField.getText().trim();
        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a location.",
                    "Missing location",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        weatherAdviceController.getAdvice(location, date.isEmpty() ? null : date);

        if (weatherAdviceVM.getErrorMessage() != null) {
            errorLabel.setText(weatherAdviceVM.getErrorMessage());
            summaryArea.setText("");
            adviceArea.setText("");
        } else {
            errorLabel.setText(" ");
            summaryArea.setText(weatherAdviceVM.getSummary() != null ? weatherAdviceVM.getSummary() : "");
            adviceArea.setText(weatherAdviceVM.getAdvice() != null ? weatherAdviceVM.getAdvice() : "");
        }
    }

    private static class FixedImagePanel extends JPanel {
        private BufferedImage originalImage;

        public FixedImagePanel(String resourcePath, int targetWidth) {
            setOpaque(false);
            try {
                java.net.URL imgUrl = getClass().getResource(resourcePath);
                if (imgUrl != null) {
                    originalImage = ImageIO.read(imgUrl);

                    double aspectRatio = (double) originalImage.getHeight() / originalImage.getWidth();
                    int targetHeight = (int) (targetWidth * aspectRatio);

                    setPreferredSize(new Dimension(targetWidth, targetHeight));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (originalImage != null) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.drawImage(originalImage, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }
}