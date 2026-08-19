package com.example.chat.frontend;

import com.example.chat.frontend.auth.AuthApiClient;
import com.example.chat.frontend.auth.AuthSession;
import com.example.chat.frontend.ui.AppTheme;
import com.example.chat.frontend.ui.LoginDialog;
import com.example.chat.frontend.ui.MainFrame;

import javax.swing.SwingUtilities;

public class ChatClientApplication {

    private static final String API_BASE_URL = "http://localhost:8080/";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppTheme.apply();

            AuthApiClient authApiClient = new AuthApiClient(API_BASE_URL);
            AuthSession session = LoginDialog.showLogin(null, authApiClient);
            if (session == null) {
                return;
            }

            MainFrame frame = new MainFrame(session);
            frame.start();
        });
    }
}
