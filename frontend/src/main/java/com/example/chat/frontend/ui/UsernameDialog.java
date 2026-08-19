package com.example.chat.frontend.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;

public final class UsernameDialog {

    private UsernameDialog() {
    }

    public static String show(Component parent) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Entrar no chat", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));
        JLabel label = new JLabel("Nome de usuário");
        JTextField input = new JTextField(22);
        input.setToolTipText("Digite o nome que será exibido no chat");

        JPanel fieldPanel = new JPanel(new BorderLayout(0, 8));
        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(input, BorderLayout.CENTER);
        content.add(fieldPanel, BorderLayout.CENTER);

        JButton cancelButton = new JButton("Cancelar");
        JButton confirmButton = new JButton("Entrar");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(cancelButton);
        actions.add(confirmButton);
        content.add(actions, BorderLayout.SOUTH);

        final String[] result = {null};
        Runnable confirm = () -> {
            String username = input.getText().trim();
            if (!username.isBlank()) {
                result[0] = username;
                dialog.dispose();
            }
        };
        confirmButton.addActionListener(event -> confirm.run());
        input.addActionListener(event -> confirm.run());
        cancelButton.addActionListener(event -> dialog.dispose());

        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(confirmButton);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }
}
