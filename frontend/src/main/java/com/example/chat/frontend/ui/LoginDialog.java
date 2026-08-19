package com.example.chat.frontend.ui;

import com.example.chat.frontend.auth.AuthApiClient;
import com.example.chat.frontend.auth.AuthSession;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.concurrent.ExecutionException;

public class LoginDialog extends JDialog {

    private final AuthApiClient authApiClient;
    private final JTextField usernameField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JLabel feedbackLabel = new JLabel(" ");
    private final JButton loginButton = new JButton("Entrar");
    private final JButton registerButton = new JButton("Criar conta");
    private AuthSession authSession;

    private LoginDialog(Window owner, AuthApiClient authApiClient) {
        super(owner, "Entrar no Chat Desktop", Dialog.ModalityType.APPLICATION_MODAL);
        this.authApiClient = authApiClient;
        buildUi();
        configureEvents();
    }

    public static AuthSession showLogin(Window owner, AuthApiClient authApiClient) {
        LoginDialog dialog = new LoginDialog(owner, authApiClient);
        dialog.setVisible(true);
        return dialog.authSession;
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
        JLabel eyebrow = new JLabel("CHAT DESKTOP");
        eyebrow.setForeground(AppTheme.ACCENT);
        eyebrow.setFont(AppTheme.SMALL_FONT.deriveFont(Font.BOLD, 11f));
        JLabel title = new JLabel("Bem-vindo de volta");
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setFont(AppTheme.TITLE_FONT);
        JLabel subtitle = new JLabel("Entre para continuar no chat.");
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
        AppTheme.styleInput(usernameField);
        AppTheme.styleInput(passwordField);
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
        constraints.insets = new Insets(0, 0, 14, 0);
        fields.add(usernameField, constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 7, 0);
        fields.add(label("Senha"), constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 5, 0);
        fields.add(passwordField, constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 0, 0);
        feedbackLabel.setForeground(AppTheme.ERROR);
        feedbackLabel.setFont(AppTheme.SMALL_FONT);
        fields.add(feedbackLabel, constraints);
        card.add(fields, BorderLayout.CENTER);

        loginButton.setPreferredSize(new Dimension(108, 40));
        registerButton.setPreferredSize(new Dimension(124, 40));
        AppTheme.stylePrimaryButton(loginButton);
        AppTheme.styleSecondaryButton(registerButton);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(registerButton);
        actions.add(loginButton);
        card.add(actions, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
        getRootPane().setDefaultButton(loginButton);
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
        loginButton.addActionListener(event -> login());
        passwordField.addActionListener(event -> login());
        registerButton.addActionListener(event -> openRegistration());
    }

    private void login() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        if (username.isBlank() || password.length == 0) {
            showError("Informe o usuário e a senha.");
            return;
        }

        setLoading(true, "Entrando...");
        new SwingWorker<AuthSession, Void>() {
            @Override
            protected AuthSession doInBackground() throws Exception {
                return authApiClient.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    authSession = get();
                    dispose();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("A autenticação foi interrompida.");
                    setLoading(false, null);
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    showError(cause == null ? "Não foi possível entrar." : cause.getMessage());
                    setLoading(false, null);
                } finally {
                    java.util.Arrays.fill(password, '\0');
                }
            }
        }.execute();
    }

    private void openRegistration() {
        setVisible(false);
        boolean registered = RegisterDialog.showRegistration(this, authApiClient);
        if (registered) {
            showSuccess("Conta criada. Agora entre com suas credenciais.");
        }
        setVisible(true);
        usernameField.requestFocusInWindow();
    }

    private void setLoading(boolean loading, String message) {
        loginButton.setEnabled(!loading);
        registerButton.setEnabled(!loading);
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        if (message != null) {
            feedbackLabel.setForeground(AppTheme.TEXT_MUTED);
            feedbackLabel.setText(message);
        }
    }

    private void showError(String message) {
        feedbackLabel.setForeground(AppTheme.ERROR);
        feedbackLabel.setText(message == null || message.isBlank() ? "Não foi possível concluir a operação." : message);
    }

    private void showSuccess(String message) {
        feedbackLabel.setForeground(AppTheme.SUCCESS);
        feedbackLabel.setText(message);
    }
}
