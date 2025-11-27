package placefinder.frameworks_drivers.view.frames;

import placefinder.frameworks_drivers.view.components.swing.Button;
import placefinder.interface_adapters.controllers.VerifyEmailController;
import placefinder.interface_adapters.viewmodels.VerifyEmailViewModel;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog that lets the user enter the 6-digit verification code
 * that was emailed to them.
 */
public class VerifyEmailDialog extends JDialog {

    private final VerifyEmailController controller;
    private final VerifyEmailViewModel viewModel;
    private final String email;   // email we are verifying (from RegisterPanel)

    private JTextField codeField;
    private JLabel messageLabel;

    public VerifyEmailDialog(Frame owner,
                             VerifyEmailController controller,
                             VerifyEmailViewModel viewModel,
                             String email) {
        super(owner, "Verify Email", true);
        this.controller = controller;
        this.viewModel = viewModel;
        this.email = email == null ? "" : email.trim();
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 220);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel();
        root.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Verify your email", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("sansserif", Font.BOLD, 18));

        JLabel subtitle = new JLabel(
                "<html>We sent a 6-digit code to:<br><b>" + email + "</b></html>",
                SwingConstants.CENTER
        );
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("sansserif", Font.PLAIN, 12));

        root.add(title);
        root.add(Box.createVerticalStrut(5));
        root.add(subtitle);
        root.add(Box.createVerticalStrut(15));

        JPanel codePanel = new JPanel(new BorderLayout(5, 5));
        codePanel.setOpaque(false);
        JLabel codeLabel = new JLabel("Verification code:");
        codeField = new JTextField();
        codePanel.add(codeLabel, BorderLayout.WEST);
        codePanel.add(codeField, BorderLayout.CENTER);
        root.add(codePanel);
        root.add(Box.createVerticalStrut(10));

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("sansserif", Font.PLAIN, 11));
        root.add(messageLabel);
        root.add(Box.createVerticalStrut(10));

        JPanel buttonRow = new JPanel();
        buttonRow.setOpaque(false);
        buttonRow.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));

        Button verifyButton = new Button();
        verifyButton.setText("Verify");
        verifyButton.addActionListener(e -> doVerify());

        Button cancelButton = new Button();
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonRow.add(verifyButton);
        buttonRow.add(cancelButton);

        root.add(buttonRow);

        setContentPane(root);
    }

    private void doVerify() {
        String code = codeField.getText().trim();

        // Call use case
        controller.verify(email, code);

        // Read result from ViewModel
        boolean success = viewModel.isSuccess();
        String msg = viewModel.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = success ? "Email verified successfully." : "Verification failed.";
        }

        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    msg,
                    "Verified",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } else {
            messageLabel.setText(msg);
        }
    }
}