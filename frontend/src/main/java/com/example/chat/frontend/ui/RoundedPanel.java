package com.example.chat.frontend.ui;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class RoundedPanel extends JPanel {

    private int cornerRadius = AppTheme.RADIUS;
    private Color fillColor = AppTheme.CARD;

    public RoundedPanel() {
        this(AppTheme.CARD, AppTheme.RADIUS);
    }

    public RoundedPanel(Color fillColor, int cornerRadius) {
        this.fillColor = fillColor;
        this.cornerRadius = cornerRadius;
        setOpaque(false);
        setBorder(new EmptyBorder(12, 14, 12, 14));
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        repaint();
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2.dispose();
        super.paintComponent(graphics);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        return new Dimension(preferred.width, Math.max(preferred.height, 44));
    }
}
