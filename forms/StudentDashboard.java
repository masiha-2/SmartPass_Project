package forms;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dao.ConnectionProvider;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import utility.PhotoHelper;

public class StudentDashboard extends JFrame {

    private final int studentId;
    private String studentRollNo = "";
    private String studentName = "";
    private String uniquePassId = "";
    private String currentImageName = null;
    private final JLabel lblPhoto;
    private final DefaultTableModel tableModel;
    private final JLabel lblPresentCount, lblAbsentCount;

    public StudentDashboard(int studentId) {
        this.studentId = studentId;

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(new Color(15, 23, 42));
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(56, 189, 248)));

        JLabel lblTitle = new JLabel("SMARTPASS - STUDENT SELF-SERVICE PORTAL");
        lblTitle.setBounds(40, 20, 620, 38);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(56, 189, 248));
        add(lblTitle);

        JButton btnMyQR = new JButton("View Digital QR Pass");
        btnMyQR.setBounds(screen.width - 370, 20, 165, 36);
        btnMyQR.setMargin(new Insets(0, 0, 0, 0));
        btnMyQR.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnMyQR.setForeground(Color.WHITE);
        btnMyQR.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnMyQR, new Color(168, 85, 247), new Color(147, 51, 234));
        btnMyQR.addActionListener(e -> showMyQRPassDialog());
        add(btnMyQR);

        JButton btnLogout = new JButton("Sign Out");
        btnLogout.setBounds(screen.width - 190, 20, 105, 36);
        btnLogout.setMargin(new Insets(0, 0, 0, 0));
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnLogout, new Color(59, 130, 246), new Color(37, 99, 235));
        btnLogout.addActionListener(e -> {
            this.dispose();
            new Login().setVisible(true);
        });
        add(btnLogout);

        JButton btnExit = new JButton("X");
        btnExit.setBounds(screen.width - 75, 20, 48, 36);
        btnExit.setMargin(new Insets(0, 0, 0, 0));
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnExit, new Color(239, 68, 68), new Color(220, 38, 38));
        btnExit.addActionListener(e -> System.exit(0));
        add(btnExit);

        JPanel profilePanel = new JPanel();
        profilePanel.setBounds(40, 80, 380, screen.height - 120);
        profilePanel.setBackground(new Color(30, 41, 59));
        profilePanel.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 1));
        profilePanel.setLayout(null);
        add(profilePanel);

        JLabel lblProfHead = new JLabel("Academic Profile Summary", SwingConstants.CENTER);
        lblProfHead.setBounds(0, 15, 380, 30);
        lblProfHead.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblProfHead.setForeground(new Color(56, 189, 248));
        profilePanel.add(lblProfHead);

        lblPhoto = new JLabel("Click for Image Options", SwingConstants.CENTER);
        lblPhoto.setBounds(110, 55, 160, 150);
        lblPhoto.setForeground(Color.LIGHT_GRAY);
        lblPhoto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPhoto.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 2));
        lblPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPhoto.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showStudentPhotoMenu(e);
            }
        });
        profilePanel.add(lblPhoto);

        JLabel lblPhotoHint = new JLabel("(Click photo to View, Upload or Remove)", SwingConstants.CENTER);
        lblPhotoHint.setBounds(0, 210, 380, 20);
        lblPhotoHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPhotoHint.setForeground(new Color(148, 163, 184));
        profilePanel.add(lblPhotoHint);

        JLabel lblAttHead = new JLabel("Campus Entry & Attendance Logs:");
        lblAttHead.setBounds(450, 80, 550, 30);
        lblAttHead.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblAttHead.setForeground(Color.WHITE);
        add(lblAttHead);

        lblPresentCount = new JLabel("Completed Sessions (Present): 0");
        lblPresentCount.setBounds(450, 120, 280, 25);
        lblPresentCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPresentCount.setForeground(new Color(34, 197, 94));
        add(lblPresentCount);

        lblAbsentCount = new JLabel("Incomplete / Absent Logs: 0");
        lblAbsentCount.setBounds(740, 120, 280, 25);
        lblAbsentCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAbsentCount.setForeground(new Color(239, 68, 68));
        add(lblAbsentCount);

        String[] columns = {"Date", "Check-In Timestamp", "Check-Out Timestamp", "Session Duration", "Attendance Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        PhotoHelper.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(30, 41, 59));
        scrollPane.setBounds(450, 160, screen.width - 490, screen.height - 200);
        add(scrollPane);

        loadStudentProfile(profilePanel);
        loadStudentAttendance();
    }

    private void showStudentPhotoMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem itemShow = new JMenuItem("1. View Full Image");
        itemShow.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemShow.addActionListener(ev -> {
            String path = (currentImageName != null) ? "images/" + currentImageName : null;
            PhotoHelper.showFullImage(this, path);
        });

        JMenuItem itemChange = new JMenuItem("2. Upload / Replace Image");
        itemChange.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemChange.addActionListener(ev -> uploadStudentPhoto());

        JMenuItem itemRemove = new JMenuItem("3. Remove Image");
        itemRemove.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemRemove.setForeground(Color.RED);
        itemRemove.addActionListener(ev -> removeStudentPhoto());

        menu.add(itemShow);
        menu.add(itemChange);
        if (currentImageName != null && !currentImageName.isEmpty()) {
            menu.addSeparator();
            menu.add(itemRemove);
        }
        menu.show(lblPhoto, e.getX(), e.getY());
    }

    private void uploadStudentPhoto() {
        File selected = PhotoHelper.showImageChooser(this);
        if (selected != null) {
            try {
                File dir = new File("images");
                if (!dir.exists()) dir.mkdirs();

                String fileName = "student_" + studentRollNo + "_" + System.currentTimeMillis() + ".jpg";
                File dest = new File(dir, fileName);
                Files.copy(selected.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                try (Connection con = ConnectionProvider.getCon()) {
                    PreparedStatement pst = con.prepareStatement("UPDATE students SET image_name = ? WHERE id = ?");
                    pst.setString(1, fileName);
                    pst.setInt(2, studentId);
                    pst.executeUpdate();
                }

                currentImageName = fileName;
                PhotoHelper.setPhotoOnLabel(lblPhoto, "images/" + currentImageName, 160, 150, "Click for Image Options");
                JOptionPane.showMessageDialog(this, "Profile image updated successfully.", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Image upload failed: " + ex.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeStudentPhoto() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove your profile image?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = ConnectionProvider.getCon()) {
                PreparedStatement pst = con.prepareStatement("UPDATE students SET image_name = NULL WHERE id = ?");
                pst.setInt(1, studentId);
                pst.executeUpdate();

                if (currentImageName != null) {
                    new File("images/" + currentImageName).delete();
                }
                currentImageName = null;
                PhotoHelper.setPhotoOnLabel(lblPhoto, null, 160, 150, "Click for Image Options");
                JOptionPane.showMessageDialog(this, "Profile image removed successfully.", "Removed", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showMyQRPassDialog() {
        try {
            String qrData = "SMARTPASS|" + studentId + "|" + studentRollNo + "|" + uniquePassId;
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 260, 260);
            BufferedImage qrImg = MatrixToImageWriter.toBufferedImage(matrix);

            JDialog dialog = new JDialog(this, "Digital SmartPass QR", true);
            dialog.setSize(360, 400);
            dialog.setLocationRelativeTo(this);
            dialog.getContentPane().setBackground(new Color(30, 41, 59));
            dialog.setLayout(new BorderLayout());

            JLabel lblHead = new JLabel(studentName + " (" + studentRollNo + ")", SwingConstants.CENTER);
            lblHead.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblHead.setForeground(new Color(56, 189, 248));
            lblHead.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
            dialog.add(lblHead, BorderLayout.NORTH);

            JLabel lblQR = new JLabel(new ImageIcon(qrImg), SwingConstants.CENTER);
            dialog.add(lblQR, BorderLayout.CENTER);

            JButton btnSave = new JButton("Export QR Pass as PNG");
            btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnSave.setBackground(new Color(34, 197, 94));
            btnSave.setForeground(Color.WHITE);
            btnSave.addActionListener(ev -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File("SmartPass_" + studentRollNo + ".png"));
                if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                    try {
                        ImageIO.write(qrImg, "png", chooser.getSelectedFile());
                        JOptionPane.showMessageDialog(dialog, "QR Pass exported successfully.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Export Error: " + ex.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            dialog.add(btnSave, BorderLayout.SOUTH);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "QR Generation Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStudentProfile(JPanel panel) {
        try (Connection con = ConnectionProvider.getCon()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM students WHERE id = ?");
            pst.setInt(1, studentId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                studentName = rs.getString("name");
                studentRollNo = rs.getString("roll_no");
                uniquePassId = rs.getString("unique_reg_id");
                currentImageName = rs.getString("image_name");
                String path = (currentImageName != null) ? "images/" + currentImageName : null;
                PhotoHelper.setPhotoOnLabel(lblPhoto, path, 160, 150, "Click for Image Options");

                addProfileRow(panel, "Full Name:", studentName, 250);
                addProfileRow(panel, "Roll No:", studentRollNo, 290);
                addProfileRow(panel, "Branch:", rs.getString("branch"), 330);
                addProfileRow(panel, "Semester:", rs.getString("semester"), 370);
                addProfileRow(panel, "Gender:", rs.getString("gender"), 410);
                addProfileRow(panel, "Email:", rs.getString("email"), 450);
                addProfileRow(panel, "Contact:", rs.getString("contact"), 490);
                addProfileRow(panel, "Pass ID:", uniquePassId, 530);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load student profile: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProfileRow(JPanel panel, String title, String value, int y) {
        JLabel lblT = new JLabel(title);
        lblT.setBounds(25, y, 95, 25);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblT.setForeground(new Color(148, 163, 184));
        panel.add(lblT);

        JLabel lblV = new JLabel(value);
        lblV.setBounds(125, y, 235, 25);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblV.setForeground(Color.WHITE);
        panel.add(lblV);
    }

    private void loadStudentAttendance() {
        tableModel.setRowCount(0);
        int present = 0;
        int incomplete = 0;

        try (Connection con = ConnectionProvider.getCon()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM attendance WHERE student_id = ? ORDER BY att_date DESC");
            pst.setInt(1, studentId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String date = rs.getString("att_date");
                String checkIn = rs.getString("check_in");
                String checkOut = rs.getString("check_out");
                String duration = rs.getString("work_duration");

                String status;
                if ("ABSENT".equalsIgnoreCase(duration)) {
                    status = "Absent";
                    checkIn = "---";
                    checkOut = "---";
                    incomplete++;
                } else if (checkOut != null && !checkOut.isEmpty()) {
                    status = "Present";
                    present++;
                } else {
                    status = "Checked-In Only";
                    checkOut = "---";
                    duration = "---";
                    incomplete++;
                }
                tableModel.addRow(new Object[]{date, checkIn, checkOut, duration, status});
            }
            lblPresentCount.setText("Completed Sessions (Present): " + present);
            lblAbsentCount.setText("Incomplete / Absent Logs: " + incomplete);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load attendance history: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}