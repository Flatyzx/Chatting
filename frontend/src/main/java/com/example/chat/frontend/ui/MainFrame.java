package com.example.chat.frontend.ui;

import com.example.chat.frontend.auth.AuthSession;
import com.example.chat.frontend.client.ChatWebSocketClient;
import com.example.chat.frontend.dto.Message;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class MainFrame extends JFrame {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AuthSession authSession;
    private final String username;
    private final DefaultListModel<String> usersModel = new DefaultListModel<>();
    private final JPanel messagesContainer = new JPanel();
    private final JTextField messageInput = new JTextField();
    private final JButton sendButton = new JButton("Enviar");
    private final JLabel statusLabel = new JLabel("Desconectado");
    private final ChatWebSocketClient client;

    public MainFrame(AuthSession authSession) {
        this.authSession = Objects.requireNonNull(authSession, "authSession");
        this.username = authSession.nomeUsuario();
        this.client = new ChatWebSocketClient(
                authSession,
                this::receiveMessage,
                this::updateUsers,
                this::updateStatus
        );
        configureFrame();
        buildUi();
        configureEvents();
    }

    public void start() {
        setVisible(true);
        client.connect();
    }

    private void configureFrame() {
        setTitle("Chat Desktop — " + username);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1060, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppTheme.BACKGROUND);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(AppTheme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMessagesPanel(), BorderLayout.CENTER);
        root.add(buildComposer(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        RoundedPanel header = new RoundedPanel(AppTheme.CARD, AppTheme.RADIUS);
        header.setBorder(BorderFactory.createEmptyBorder(13, 18, 13, 18));
        header.setLayout(new BorderLayout());

        JPanel titleGroup = new JPanel(new GridBagLayout());
        titleGroup.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        JLabel title = new JLabel("Chat geral");
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setFont(AppTheme.TITLE_FONT.deriveFont(18f));
        titleGroup.add(title, constraints);
        constraints.gridy++;
        JLabel subtitle = new JLabel("Conversa aberta para todos os usuários");
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setFont(AppTheme.SMALL_FONT);
        titleGroup.add(subtitle, constraints);

        JPanel identity = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        identity.setOpaque(false);
        JLabel currentUserLabel = new JLabel(username);
        currentUserLabel.setForeground(AppTheme.TEXT_PRIMARY);
        currentUserLabel.setFont(AppTheme.SECTION_FONT);
        statusLabel.setForeground(AppTheme.TEXT_MUTED);
        statusLabel.setFont(AppTheme.SMALL_FONT);
        identity.add(currentUserLabel);
        identity.add(dotLabel());
        identity.add(statusLabel);

        header.add(titleGroup, BorderLayout.WEST);
        header.add(identity, BorderLayout.EAST);
        return header;
    }

    private JLabel dotLabel() {
        JLabel dot = new JLabel("•");
        dot.setForeground(AppTheme.ACCENT);
        return dot;
    }

    private JPanel buildSidebar() {
        RoundedPanel sidebar = new RoundedPanel(AppTheme.SIDEBAR, AppTheme.RADIUS);
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 14, 14, 14));
        sidebar.setPreferredSize(new Dimension(218, 0));
        sidebar.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Usuários conectados");
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setFont(AppTheme.SECTION_FONT);

        JList<String> userList = new JList<>(usersModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setOpaque(false);
        userList.setBackground(AppTheme.SIDEBAR);
        userList.setForeground(AppTheme.TEXT_PRIMARY);
        userList.setBorder(BorderFactory.createEmptyBorder());
        userList.setCellRenderer(new UserCellRenderer());
        userList.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                int index = userList.locationToIndex(event.getPoint());
                if (index >= 0) {
                    userList.setSelectedIndex(index);
                }
            }
        });
        JScrollPane usersScrollPane = new JScrollPane(userList);
        usersScrollPane.setBorder(BorderFactory.createEmptyBorder());
        usersScrollPane.setOpaque(false);
        usersScrollPane.getViewport().setOpaque(false);
        usersScrollPane.getVerticalScrollBar().setUnitIncrement(12);

        sidebar.add(title, BorderLayout.NORTH);
        sidebar.add(usersScrollPane, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildMessagesPanel() {
        RoundedPanel panel = new RoundedPanel(AppTheme.BACKGROUND, AppTheme.RADIUS);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panel.setLayout(new BorderLayout());

        messagesContainer.setOpaque(false);
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane messagesScrollPane = new JScrollPane(messagesContainer);
        messagesScrollPane.setBorder(BorderFactory.createEmptyBorder());
        messagesScrollPane.setOpaque(false);
        messagesScrollPane.getViewport().setOpaque(false);
        messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(messagesScrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildComposer() {
        RoundedPanel composer = new RoundedPanel(AppTheme.CARD, AppTheme.RADIUS);
        composer.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 8));
        composer.setLayout(new BorderLayout(10, 0));

        messageInput.putClientProperty("JTextField.placeholderText", "Escreva uma mensagem...");
        messageInput.setBorder(BorderFactory.createEmptyBorder(9, 5, 9, 5));
        messageInput.setBackground(AppTheme.CARD);
        messageInput.setForeground(AppTheme.TEXT_PRIMARY);
        messageInput.setCaretColor(AppTheme.ACCENT);
        messageInput.setFont(AppTheme.BODY_FONT);
        sendButton.setPreferredSize(new Dimension(98, 40));
        AppTheme.stylePrimaryButton(sendButton);
        composer.add(messageInput, BorderLayout.CENTER);
        composer.add(sendButton, BorderLayout.EAST);
        return composer;
    }

    private void configureEvents() {
        sendButton.addActionListener(event -> sendCurrentMessage());
        messageInput.addActionListener(event -> sendCurrentMessage());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                client.close();
                dispose();
            }
        });
        setComposerEnabled(false);
    }

    private void sendCurrentMessage() {
        String content = messageInput.getText().trim();
        if (content.isBlank()) {
            return;
        }
        client.sendMessage(content);
        messageInput.setText("");
        messageInput.requestFocusInWindow();
    }

    private void receiveMessage(Message message) {
        runOnEdt(() -> appendMessage(message));
    }

    private void appendMessage(Message message) {
        boolean ownMessage = username.equals(message.remetente());
        JPanel row = new JPanel(new FlowLayout(ownMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        RoundedPanel bubble = new RoundedPanel(ownMessage ? AppTheme.MESSAGE_OWN : AppTheme.CARD, AppTheme.SMALL_RADIUS);
        bubble.setBorder(BorderFactory.createEmptyBorder(9, 13, 9, 13));
        bubble.setLayout(new BorderLayout(0, 4));
        bubble.setMaximumSize(new Dimension(620, 130));

        JPanel meta = new JPanel(new BorderLayout());
        meta.setOpaque(false);
        JLabel sender = new JLabel(message.remetente());
        sender.setForeground(ownMessage ? AppTheme.ACCENT_HOVER : AppTheme.TEXT_PRIMARY);
        sender.setFont(AppTheme.SECTION_FONT.deriveFont(12f));
        JLabel time = new JLabel(formatTime(message.horario()));
        time.setForeground(AppTheme.TEXT_MUTED);
        time.setFont(AppTheme.SMALL_FONT);
        meta.add(sender, BorderLayout.WEST);
        meta.add(time, BorderLayout.EAST);

        JLabel content = new JLabel(toHtml(message.conteudo()));
        content.setForeground(AppTheme.TEXT_PRIMARY);
        content.setFont(AppTheme.BODY_FONT);
        content.setVerticalAlignment(SwingConstants.TOP);
        bubble.add(meta, BorderLayout.NORTH);
        bubble.add(content, BorderLayout.CENTER);
        row.add(bubble);
        messagesContainer.add(row);
        messagesContainer.add(Box.createVerticalStrut(9));
        messagesContainer.revalidate();
        messagesContainer.repaint();
        SwingUtilities.invokeLater(() -> {
            if (messagesContainer.getParent() instanceof javax.swing.JViewport viewport) {
                viewport.setViewPosition(new java.awt.Point(0, messagesContainer.getHeight()));
            }
        });
    }

    private String toHtml(String text) {
        String safe = text == null ? "" : text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><body style='width: 430px'>" + safe + "</body></html>";
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "--:--" : TIME_FORMATTER.format(time);
    }

    private void updateUsers(List<String> users) {
        runOnEdt(() -> {
            usersModel.clear();
            users.stream().filter(Objects::nonNull).distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(usersModel::addElement);
        });
    }

    private void updateStatus(String status) {
        runOnEdt(() -> {
            statusLabel.setText(status);
            statusLabel.setForeground("Conectado".equals(status) ? AppTheme.SUCCESS : AppTheme.TEXT_MUTED);
            setComposerEnabled("Conectado".equals(status));
        });
    }

    private void setComposerEnabled(boolean enabled) {
        messageInput.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }

    private void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    private static class UserCellRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel dot = new JLabel("•");
        private final JLabel name = new JLabel();

        private UserCellRenderer() {
            super(new BorderLayout(8, 0));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(9, 8, 9, 8));
            dot.setForeground(AppTheme.ACCENT);
            name.setForeground(AppTheme.TEXT_PRIMARY);
            name.setFont(AppTheme.BODY_FONT);
            add(dot, BorderLayout.WEST);
            add(name, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                                                       String value,
                                                       int index,
                                                       boolean selected,
                                                       boolean focused) {
            name.setText(value);
            setBackground(selected ? AppTheme.CARD_HOVER : AppTheme.SIDEBAR);
            return this;
        }
    }
}
