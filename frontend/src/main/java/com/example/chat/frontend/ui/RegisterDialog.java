package com.example.chat.frontend.ui;

import com.example.chat.frontend.auth.AuthApiClient;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class RegisterDialog extends JDialog {

    private final AuthApiClient authApiClient;
    private final JTextField usernameField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JPasswordField confirmationField = new JPasswordField(22);
    private final JLabel feedbackLabel = new JLabel(" ");
    private final JButton registerButton = new JButton("Criar conta");
    private final JButton cancelButton = new JButton("Voltar");
    private boolean registered;

    private RegisterDialog(Window owner, AuthApiClient authApiClient) {
        super(owner, "Criar conta", Dialog.ModalityType.APPLICATION_MODAL);
        this.authApiClient = authApiClient;
        buildUi();
        configureEvents();
    }

    public static boolean showRegistration(Window owner, AuthApiClient authApiClient) {
        RegisterDialog dialog = new RegisterDialog(owner, authApiClient);
        dialog.setVisible(true);
        return dialog.registered;
    }

    private void buildUi() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        RoundedPanel card = new RoundedPanel(AppTheme.CARD, AppTheme.RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(28, 30, 24, 30));
        card.setLayout(new BorderLayout(0, 20));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        JLabel eyebrow = new JLabel("NOVA CONTA");
        eyebrow.setForeground(AppTheme.ACCENT);
        eyebrow.setFont(AppTheme.SMALL_FONT.deriveFont(Font.BOLD, 11f));
        JLabel title = new JLabel("Criar sua conta");
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setFont(AppTheme.TITLE_FONT);
        JLabel subtitle = new JLabel("A senha deve ter pelo menos 6 caracteres.");
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setFont(AppTheme.BODY_FONT);
        JPanel titleText = new JPanel(new BorderLayout(0, 3));
        titleText.setOpaque(false);
        titleText.add(title, BorderLayout.NORTH);
        titleText.add(subtitle, BorderLayout.SOUTH);
        titlePanel.add(eyebrow, BorderLayout.NORTH);
        titlePanel.add(titleText, BorderLayout.SOUTH);
        card.add(titlePanel, BorderLayout.NORTH);

        usernameField.putClientProperty("JTextField.placeholderText", "Nome de usuário");
        passwordField.putClientProperty("JTextField.placeholderText", "Senha");
        confirmationField.putClientProperty("JTextField.placeholderText", "Confirmar senha");
        AppTheme.styleInput(usernameField);
        AppTheme.styleInput(passwordField);
        AppTheme.styleInput(confirmationField);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 7, 0);
        fields.add(label("Usuário"), constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 12, 0);
        fields.add(usernameField, constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 7, 0);
        fields.add(label("Senha"), constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 12, 0);
        fields.add(passwordField, constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 7, 0);
        fields.add(label("Confirmar senha"), constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 5, 0);
        fields.add(confirmationField, constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 0, 0);
        feedbackLabel.setForeground(AppTheme.ERROR);
        feedbackLabel.setFont(AppTheme.SMALL_FONT);
        fields.add(feedbackLabel, constraints);
        card.add(fields, BorderLayout.CENTER);

        registerButton.setPreferredSize(new Dimension(128, 40));
        cancelButton.setPreferredSize(new Dimension(100, 40));
        AppTheme.stylePrimaryButton(registerButton);
        AppTheme.styleSecondaryButton(cancelButton);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(cancelButton);
        actions.add(registerButton);
        card.add(actions, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
        getRootPane().setDefaultButton(registerButton);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(AppTheme.TEXT_MUTED);
        label.setFont(AppTheme.SMALL_FONT);
        return label;
    }

    private void configureEvents() {
        registerButton.addActionListener(event -> register());
        confirmationField.addActionListener(event -> register());
        cancelButton.addActionListener(event -> dispose());
    }

    private void register() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        char[] confirmation = confirmationField.getPassword();

        if (username.isBlank()) {
            showError("Informe um nome de usuário.");
            clear(confirmation);
            return;
        }
        if (password.length < 6) {
            showError("A senha deve ter pelo menos 6 caracteres.");
            clear(password, confirmation);
            return;
        }
        if (!Arrays.equals(password, confirmation)) {
            showError("A confirmação de senha não confere.");
            clear(password, confirmation);
            return;
        }

        setLoading(true, "Criando conta...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                authApiClient.register(username, password);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    registered = true;
                    dispose();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("O cadastro foi interrompido.");
                    setLoading(false, null);
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    showError(cause == null ? "Não foi possível criar a conta." : cause.getMessage());
                    setLoading(false, null);
                } finally {
                    clear(password, confirmation);
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading, String message) {
        registerButton.setEnabled(!loading);
        cancelButton.setEnabled(!loading);
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        confirmationField.setEnabled(!loading);
        if (message != null) {
            feedbackLabel.setForeground(AppTheme.TEXT_MUTED);
            feedbackLabel.setText(message);
        }
    }

    private void showError(String message) {
        feedbackLabel.setForeground(AppTheme.ERROR);
        feedbackLabel.setText(message == null || message.isBlank() ? "Não foi possível concluir o cadastro." : message);
    }

    private void clear(char[]... values) {
        for (char[] value : values) {
            Arrays.fill(value, '\0');
        }
    }
}
