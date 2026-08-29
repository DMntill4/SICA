package com.acme.sica.infrastructure.adapter.in.gui.theme;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Sistema de Diseño de Seguridad SICA - Tema Azul Marino Cálido (Warm Slate Navy).
 * Apariencia profesional de alto impacto para Login, Dashboard Desktop y Portal Web.
 */
public class SicaTheme {

    // Paleta Principal: Azul Marino Cálido (Warm Dark Slate Navy)
    public static final Color BG_MAIN = new Color(30, 41, 59);          // #1E293B Fondo Azul Marino Cálido
    public static final Color CARD_BG = new Color(36, 51, 70);          // #243346 Tarjetas Azul Pizarra
    public static final Color CARD_BG_ALT = new Color(45, 62, 84);      // #2D3E54 Contenedores Elevados
    public static final Color HEADER_BG = new Color(17, 24, 35);        // #111823 Encabezado Azul Profundo

    public static final Color BORDER_SUBTLE = new Color(51, 65, 85);     // #334155 Borde 1px Sutil
    public static final Color BORDER_LIGHT = new Color(71, 85, 105);     // #475569

    public static final Color ACCENT_CYAN = new Color(46, 155, 214);     // #2E9BD6 Celeste Primario de Seguridad
    public static final Color ACCENT_CYAN_HOVER = new Color(33, 127, 179); // #217FB3 Hover
    public static final Color ACCENT_CYAN_LIGHT = new Color(30, 58, 95);  // #1E3A5F Fondo Activo
    public static final Color ACCENT_NAVY = new Color(147, 197, 253);    // #93C5FD Texto Destacado Hielo

    public static final Color TEXT_MAIN = new Color(248, 250, 252);      // #F8FAFC Texto Principal Blanco Crema
    public static final Color TEXT_MUTED = new Color(148, 163, 184);      // #94A3B8 Texto Secundario Pizarra
    public static final Color TEXT_DISABLED = new Color(100, 116, 139);  // #64748B

    // Estados Semánticos de Control de Accesos en Oscuro Cálido
    public static final Color STATUS_GRANTED_TEXT = new Color(52, 211, 153);  // #34D399 Esmeralda
    public static final Color STATUS_GRANTED_BG   = new Color(20, 56, 43);    // #14382B
    
    public static final Color STATUS_WARNING_TEXT = new Color(251, 191, 36);  // #FBBF24 Ámbar
    public static final Color STATUS_WARNING_BG   = new Color(61, 45, 20);    // #3D2D14

    public static final Color STATUS_DENIED_TEXT  = new Color(248, 113, 113);  // #F87171 Carmesí
    public static final Color STATUS_DENIED_BG    = new Color(69, 26, 26);    // #451A1A

    public static final Color STATUS_INFO_TEXT    = new Color(96, 165, 250);   // #60A5FA Celeste Informativo
    public static final Color STATUS_INFO_BG      = new Color(30, 58, 95);    // #1E3A5F

    // Compatibilidad de Constantes
    public static final Color ACCENT_ROSE = STATUS_DENIED_TEXT;
    public static final Color ACCENT_EMERALD = STATUS_GRANTED_TEXT;
    public static final Color ACCENT_AMBER = STATUS_WARNING_TEXT;
    public static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    public static final Color ACCENT_BLUE = ACCENT_CYAN;
    public static final Color BG_DARK = BG_MAIN;

    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Aplica la configuración global del Look & Feel Azul Marino Cálido (FlatDarkLaf + SicaTheme).
     */
    public static void applyGlobalDefaults() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);

            // 1. Paneles y Ventanas Oscuras
            UIManager.put("Panel.background", BG_MAIN);
            UIManager.put("Viewport.background", CARD_BG);
            UIManager.put("ScrollPane.background", CARD_BG);
            UIManager.put("SplitPane.background", BG_MAIN);
            UIManager.put("RootPane.background", BG_MAIN);

            // 2. Diálogos, Popups y OptionPane (100% Dark Slate Navy)
            UIManager.put("OptionPane.background", CARD_BG);
            UIManager.put("OptionPane.messageForeground", TEXT_MAIN);
            UIManager.put("OptionPane.foreground", TEXT_MAIN);
            UIManager.put("Dialog.background", CARD_BG);
            UIManager.put("Dialog.contentPaneBackground", CARD_BG);
            UIManager.put("Window.background", CARD_BG);

            // 3. Componentes de Formulario (Labels, TextFields, CheckBoxes, ComboBox)
            UIManager.put("Label.foreground", TEXT_MAIN);
            UIManager.put("TextField.background", CARD_BG_ALT);
            UIManager.put("TextField.foreground", TEXT_MAIN);
            UIManager.put("TextField.caretForeground", TEXT_MAIN);
            UIManager.put("TextField.selectionBackground", ACCENT_CYAN_LIGHT);
            UIManager.put("TextField.selectionForeground", TEXT_MAIN);
            UIManager.put("PasswordField.background", CARD_BG_ALT);
            UIManager.put("PasswordField.foreground", TEXT_MAIN);
            UIManager.put("TextArea.background", CARD_BG_ALT);
            UIManager.put("TextArea.foreground", TEXT_MAIN);
            UIManager.put("CheckBox.background", CARD_BG);
            UIManager.put("CheckBox.foreground", TEXT_MAIN);
            UIManager.put("RadioButton.background", CARD_BG);
            UIManager.put("RadioButton.foreground", TEXT_MAIN);
            UIManager.put("ComboBox.background", CARD_BG_ALT);
            UIManager.put("ComboBox.foreground", TEXT_MAIN);

            // 4. Pestañas y Tablas
            UIManager.put("TabbedPane.selectedBackground", CARD_BG);
            UIManager.put("TabbedPane.background", BG_MAIN);
            UIManager.put("TabbedPane.foreground", TEXT_MUTED);
            UIManager.put("TabbedPane.selectedForeground", ACCENT_CYAN);
            UIManager.put("TabbedPane.underlineColor", ACCENT_CYAN);
            UIManager.put("TabbedPane.focusColor", new Color(0, 0, 0, 0));

            UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());
            UIManager.put("Table.scrollPaneBorder", BorderFactory.createEmptyBorder());
            UIManager.put("Table.background", CARD_BG);
            UIManager.put("Table.foreground", TEXT_MAIN);
            UIManager.put("TableHeader.background", CARD_BG_ALT);
            UIManager.put("TableHeader.foreground", ACCENT_NAVY);
        } catch (Exception ignored) {}
    }

    /**
     * Aplica el tema oscuro azul marino SICA de forma recursiva a cualquier contenedor o formulario.
     */
    public static void applyDarkThemeRecursively(Component component) {
        if (component == null) return;

        if (component instanceof JPanel panel) {
            panel.setBackground(CARD_BG);
            panel.setForeground(TEXT_MAIN);
        } else if (component instanceof JDialog dialog) {
            dialog.getContentPane().setBackground(CARD_BG);
        } else if (component instanceof JLabel label) {
            label.setForeground(TEXT_MAIN);
        } else if (component instanceof JCheckBox chk) {
            chk.setBackground(CARD_BG);
            chk.setForeground(TEXT_MAIN);
            chk.setOpaque(true);
        } else if (component instanceof JTextField txt) {
            txt.setBackground(CARD_BG_ALT);
            txt.setForeground(TEXT_MAIN);
            txt.setCaretColor(TEXT_MAIN);
            txt.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_SUBTLE, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
        } else if (component instanceof JScrollPane scrollPane) {
            scrollPane.setBackground(CARD_BG);
            scrollPane.getViewport().setBackground(CARD_BG);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyDarkThemeRecursively(child);
            }
        }
    }


    public static Border createCardBorder(String title) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(10, 14, 12, 14)
        );
    }

    public static JPanel createHeaderCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(12, 16, 14, 16)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                new EmptyBorder(0, 0, 8, 0)
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(FONT_SECTION);
        lblTitle.setForeground(ACCENT_NAVY);

        header.add(lblTitle, BorderLayout.WEST);
        card.add(header, BorderLayout.NORTH);
        if (content != null) {
            card.add(content, BorderLayout.CENTER);
        }
        return card;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(FONT_BODY);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_MAIN);
        table.setSelectionBackground(ACCENT_CYAN_LIGHT);
        table.setSelectionForeground(TEXT_MAIN);
        table.setShowGrid(true);
        table.setGridColor(BORDER_SUBTLE);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(CARD_BG_ALT);
        header.setForeground(ACCENT_NAVY);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : CARD_BG_ALT);
                    c.setForeground(TEXT_MAIN);
                } else {
                    c.setBackground(ACCENT_CYAN_LIGHT);
                    c.setForeground(TEXT_MAIN);
                }
                return c;
            }
        });
    }

    public static void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bg.equals(CARD_BG_ALT) ? BORDER_SUBTLE : bg, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
    }
}
