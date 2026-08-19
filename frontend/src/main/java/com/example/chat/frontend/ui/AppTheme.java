package com.example.chat.frontend.ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Set;

public final class AppTheme {

    public static final Color BACKGROUND = Color.decode("#262624");
    public static final Color SIDEBAR = Color.decode("#1E1D1B");
    public static final Color CARD = Color.decode("#33322F");
    public static final Color CARD_HOVER = Color.decode("#3A3935");
    public static final Color MESSAGE_OWN = Color.decode("#45413B");
    public static final Color TEXT_PRIMARY = Color.decode("#ECECE6");
    public static final Color TEXT_MUTED = Color.decode("#9C9A93");
    public static final Color ACCENT = Color.decode("#D97757");
    public static final Color ACCENT_HOVER = Color.decode("#E28A6C");
    public static final Color BORDER = Color.decode("#3A3935");
    public static final Color ERROR = Color.decode("#F28B82");
    public static final Color SUCCESS = Color.decode("#A8C7A0");

    public static final int RADIUS = 12;
    public static final int SMALL_RADIUS = 9;
    public static final int SPACING = 12;
    public static final int CONTENT_PADDING = 16;
    private static final String FONT_FAMILY = detectFontFamily();
    public static final Font BODY_FONT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font SECTION_FONT = new Font(FONT_FAMILY, Font.BOLD, 14);

    private AppTheme() {
    }

    private static String detectFontFamily() {
        Set<String> available = Set.of(GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames());
        for (String candidate : new String[]{"Aptos", "Segoe UI Variable", "Inter", "Segoe UI"}) {
            if (available.contains(candidate)) {
                return candidate;
            }
        }
        return Font.SANS_SERIF;
    }

    public static String fontFamily() {
        return FONT_FAMILY;
    }

    public static void apply() {
        FlatDarkLaf.setup();
        FontUIResource defaultFont = new FontUIResource(BODY_FONT);
        UIManager.put("defaultFont", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("PasswordField.font", defaultFont);
        UIManager.put("Button.font", defaultFont);
        UIManager.put("List.font", defaultFont);

        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("RootPane.background", BACKGROUND);
        UIManager.put("Window.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.background", CARD);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("TextField.placeholderForeground", TEXT_MUTED);
        UIManager.put("PasswordField.background", CARD);
        UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", ACCENT);
        UIManager.put("List.background", SIDEBAR);
        UIManager.put("List.foreground", TEXT_PRIMARY);
        UIManager.put("List.selectionBackground", CARD_HOVER);
        UIManager.put("List.selectionForeground", TEXT_PRIMARY);
        UIManager.put("TextArea.background", BACKGROUND);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("ScrollPane.background", BACKGROUND);
        UIManager.put("Viewport.background", BACKGROUND);
        UIManager.put("ScrollBar.thumb", BORDER);
        UIManager.put("ScrollBar.track", SIDEBAR);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Button.background", ACCENT);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("Button.hoverBackground", ACCENT_HOVER);
        UIManager.put("Button.pressedBackground", Color.decode("#BF654A"));
        UIManager.put("Button.borderColor", ACCENT);
        UIManager.put("Button.arc", RADIUS);
        UIManager.put("Component.arc", RADIUS);
        UIManager.put("TextComponent.arc", SMALL_RADIUS);
        UIManager.put("ScrollBar.thumbArc", RADIUS);
        UIManager.put("ScrollBar.trackArc", RADIUS);
        UIManager.put("TitlePane.background", BACKGROUND);
        UIManager.put("TitlePane.foreground", TEXT_PRIMARY);
    }

    public static void stylePrimaryButton(JButton button) {
        button.setBackground(ACCENT);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        button.putClientProperty("Button.arc", RADIUS);
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    public static void styleSecondaryButton(JButton button) {
        button.setBackground(CARD);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        button.putClientProperty("Button.arc", RADIUS);
    }

    public static void styleInput(JComponent component) {
        component.setBackground(CARD);
        component.setForeground(TEXT_PRIMARY);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        component.putClientProperty("Component.arc", SMALL_RADIUS);
    }
}
