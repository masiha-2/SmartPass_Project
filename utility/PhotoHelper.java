package utility;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class PhotoHelper {

    public static void setPhotoOnLabel(JLabel label, String imagePath, int width, int height, String defaultText) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File f = new File(imagePath);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(imagePath);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setText("");
                label.setIcon(new ImageIcon(img));
                return;
            }
        }
        label.setIcon(null);
        label.setText(defaultText);
    }

    public static void showFullImage(Component parent, String imagePath) {
        if (imagePath == null || imagePath.isEmpty() || !new File(imagePath).exists()) {
            JOptionPane.showMessageDialog(parent, "No profile image is currently uploaded for this record.", "Image Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window win = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(win, "Profile Image Preview", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 520);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(new Color(15, 23, 42));
        dialog.setLayout(new BorderLayout());

        ImageIcon icon = new ImageIcon(imagePath);
        Image img = icon.getImage().getScaledInstance(460, 440, Image.SCALE_SMOOTH);
        JLabel lbl = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
        dialog.add(lbl, BorderLayout.CENTER);

        JButton btnClose = new JButton("Close Preview");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setBackground(new Color(239, 68, 68));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dialog.dispose());
        dialog.add(btnClose, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    public static File showImageChooser(Component parent) {
        Window win = SwingUtilities.getWindowAncestor(parent);
        FileDialog fd;
        if (win instanceof Frame) {
            fd = new FileDialog((Frame) win, "Select Image File (JPG / PNG)", FileDialog.LOAD);
        } else if (win instanceof Dialog) {
            fd = new FileDialog((Dialog) win, "Select Image File (JPG / PNG)", FileDialog.LOAD);
        } else {
            fd = new FileDialog((Frame) null, "Select Image File (JPG / PNG)", FileDialog.LOAD);
        }
        fd.setFile("*.jpg;*.jpeg;*.png");
        fd.setVisible(true);

        if (fd.getFile() != null) {
            return new File(fd.getDirectory(), fd.getFile());
        }
        return null;
    }

    // Modern Hover Effect for Buttons
    public static void applyHoverEffect(JButton btn, Color normalColor, Color hoverColor) {
        btn.setBackground(normalColor);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(normalColor);
            }
        });
    }

    // Modern Dark Theme Styling for JTable
    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(51, 65, 85));
        table.setSelectionBackground(new Color(14, 165, 233));
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(new Color(56, 189, 248));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
}