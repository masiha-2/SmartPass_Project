package forms;

import dao.ConnectionProvider;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.*;
import utility.PhotoHelper;

public class AdminDashboard extends JFrame {

    private final int adminId;
    private final JLabel lblWelcome, lblStatStudents, lblStatTodayAtt;
    private String adminImageName = null;

    public AdminDashboard(int adminId, String adminName) {
        this.adminId = adminId;

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(new Color(15, 23, 42));
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(56, 189, 248)));

        // Top Navigation Bar
        JPanel topBar = new JPanel(null);
        topBar.setBounds(0, 0, screen.width, 95);
        topBar.setBackground(new Color(30, 41, 59));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(56, 189, 248)));
        add(topBar);

        JLabel lblTitle = new JLabel("SMARTPASS ENTERPRISE - ADMIN CONSOLE");
        lblTitle.setBounds(45, 18, 650, 35);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(56, 189, 248));
        topBar.add(lblTitle);

        lblWelcome = new JLabel("Authenticated Administrator: " + adminName + " | Status: Online");
        lblWelcome.setBounds(45, 55, 600, 25);
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblWelcome.setForeground(new Color(148, 163, 184));
        topBar.add(lblWelcome);

        JButton btnRefresh = new JButton("Refresh Stats");
        btnRefresh.setBounds(screen.width - 325, 28, 120, 38);
        btnRefresh.setMargin(new Insets(0, 0, 0, 0));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnRefresh, new Color(14, 165, 233), new Color(2, 132, 199));
        btnRefresh.addActionListener(e -> loadLiveMetrics());
        topBar.add(btnRefresh);

        JButton btnLogout = new JButton("Sign Out");
        btnLogout.setBounds(screen.width - 190, 28, 105, 38);
        btnLogout.setMargin(new Insets(0, 0, 0, 0));
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnLogout, new Color(59, 130, 246), new Color(37, 99, 235));
        btnLogout.addActionListener(e -> {
            this.dispose();
            new Login().setVisible(true);
        });
        topBar.add(btnLogout);

        JButton btnExit = new JButton("X");
        btnExit.setBounds(screen.width - 75, 28, 48, 38);
        btnExit.setMargin(new Insets(0, 0, 0, 0));
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnExit, new Color(239, 68, 68), new Color(220, 38, 38));
        btnExit.addActionListener(e -> System.exit(0));
        topBar.add(btnExit);

        // Live Analytics Cards Row
        int startX1 = (screen.width - 900) / 2;
        int startX2 = startX1 + 480;

        lblStatStudents = createMetricCard("TOTAL REGISTERED STUDENTS: 0", startX1, 125, new Color(30, 41, 59), new Color(56, 189, 248));
        add(lblStatStudents);

        lblStatTodayAtt = createMetricCard("TODAY'S CAMPUS CHECK-INS: 0", startX2, 125, new Color(30, 41, 59), new Color(34, 197, 94));
        add(lblStatTodayAtt);

        // 6 Main Operational Modules
        int startY = 230;

        JButton btnProfile = createMenuButton("1. Administrator Profile & Security", startX1, startY, new Color(14, 165, 233), new Color(2, 132, 199));
        btnProfile.addActionListener(e -> openAdminProfileDialog());
        add(btnProfile);

        JButton btnAddStudent = createMenuButton("2. Enroll New Student Record", startX2, startY, new Color(34, 197, 94), new Color(22, 163, 74));
        btnAddStudent.addActionListener(e -> new ManageStudents().setVisible(true));
        add(btnAddStudent);

        JButton btnManageStudents = createMenuButton("3. Student Directory (Search / Edit / Delete)", startX1, startY + 125, new Color(99, 102, 241), new Color(79, 70, 229));
        btnManageStudents.addActionListener(e -> new ManageStudents().setVisible(true));
        add(btnManageStudents);

        JButton btnGenerateQR = createMenuButton("4. Digital QR Pass Studio", startX2, startY + 125, new Color(168, 85, 247), new Color(147, 51, 234));
        btnGenerateQR.addActionListener(e -> new GenerateQR().setVisible(true));
        add(btnGenerateQR);

        JButton btnScanQR = createMenuButton("5. QR Attendance Scanner Terminal", startX1, startY + 250, new Color(234, 179, 8), new Color(202, 138, 4));
        btnScanQR.addActionListener(e -> new ScanQRAttendance().setVisible(true));
        add(btnScanQR);

        JButton btnManualAtt = createMenuButton("6. Attendance Logs & Manual Override", startX2, startY + 250, new Color(249, 115, 22), new Color(234, 88, 12));
        btnManualAtt.addActionListener(e -> new ManageAttendance().setVisible(true));
        add(btnManualAtt);

        loadLiveMetrics();
    }

    private JLabel createMetricCard(String text, int x, int y, Color bg, Color borderCol) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setBounds(x, y, 420, 70);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(borderCol);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setBorder(BorderFactory.createLineBorder(borderCol, 2));
        return lbl;
    }

    private JButton createMenuButton(String text, int x, int y, Color normalBg, Color hoverBg) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 420, 92);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 17));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184), 1));
        PhotoHelper.applyHoverEffect(btn, normalBg, hoverBg);
        return btn;
    }

    private void loadLiveMetrics() {
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) return;
            ResultSet rs1 = con.createStatement().executeQuery("SELECT COUNT(*) FROM students");
            if (rs1.next()) {
                lblStatStudents.setText("TOTAL REGISTERED STUDENTS: " + rs1.getInt(1));
            }
            PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM attendance WHERE att_date = ?");
            pst.setString(1, LocalDate.now().toString());
            ResultSet rs2 = pst.executeQuery();
            if (rs2.next()) {
                lblStatTodayAtt.setText("TODAY'S CAMPUS CHECK-INS: " + rs2.getInt(1));
            }
        } catch (SQLException ex) {
            // Ignore metric refresh error
        }
    }

    private void openAdminProfileDialog() {
        JDialog dialog = new JDialog(this, "Administrator Profile & Security Settings", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);
        dialog.getContentPane().setBackground(new Color(30, 41, 59));

        JLabel lblHead = new JLabel("Administrator Profile Configuration", SwingConstants.CENTER);
        lblHead.setBounds(0, 15, 480, 30);
        lblHead.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHead.setForeground(new Color(56, 189, 248));
        dialog.add(lblHead);

        JLabel lblAdminPhoto = new JLabel("Click for Image Options", SwingConstants.CENTER);
        lblAdminPhoto.setBounds(160, 55, 160, 145);
        lblAdminPhoto.setForeground(Color.LIGHT_GRAY);
        lblAdminPhoto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAdminPhoto.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 2));
        lblAdminPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dialog.add(lblAdminPhoto);

        JTextField txtName = new JTextField();
        txtName.setBounds(150, 225, 260, 32);
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dialog.add(txtName);

        JTextField txtEmail = new JTextField();
        txtEmail.setBounds(150, 275, 260, 32);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dialog.add(txtEmail);

        JTextField txtPass = new JTextField();
        txtPass.setBounds(150, 325, 260, 32);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dialog.add(txtPass);

        JLabel l1 = new JLabel("Full Name:");
        l1.setBounds(45, 225, 100, 30);
        l1.setForeground(Color.WHITE);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dialog.add(l1);

        JLabel l2 = new JLabel("Email ID:");
        l2.setBounds(45, 275, 100, 30);
        l2.setForeground(Color.WHITE);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dialog.add(l2);

        JLabel l3 = new JLabel("Password:");
        l3.setBounds(45, 325, 100, 30);
        l3.setForeground(Color.WHITE);
        l3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dialog.add(l3);

        try (Connection con = ConnectionProvider.getCon()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM admins WHERE id = ?");
            pst.setInt(1, adminId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtName.setText(rs.getString("full_name"));
                txtEmail.setText(rs.getString("email"));
                txtPass.setText(rs.getString("password"));
                adminImageName = rs.getString("image_name");
                String path = (adminImageName != null) ? "images/" + adminImageName : null;
                PhotoHelper.setPhotoOnLabel(lblAdminPhoto, path, 160, 145, "Click for Image Options");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Failed to load profile: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        lblAdminPhoto.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();

                JMenuItem itemShow = new JMenuItem("1. View Full Image");
                itemShow.addActionListener(ev -> {
                    String path = (adminImageName != null) ? "images/" + adminImageName : null;
                    PhotoHelper.showFullImage(dialog, path);
                });

                JMenuItem itemUpload = new JMenuItem("2. Upload / Replace Image");
                itemUpload.addActionListener(ev -> {
                    File picked = PhotoHelper.showImageChooser(dialog);
                    if (picked != null) {
                        try {
                            File dir = new File("images");
                            if (!dir.exists()) dir.mkdirs();
                            String fName = "admin_" + adminId + "_" + System.currentTimeMillis() + ".jpg";
                            Files.copy(picked.toPath(), new File(dir, fName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            adminImageName = fName;
                            PhotoHelper.setPhotoOnLabel(lblAdminPhoto, "images/" + adminImageName, 160, 145, "Click for Image Options");
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dialog, "Image upload failed: " + ex.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                JMenuItem itemRemove = new JMenuItem("3. Remove Image");
                itemRemove.setForeground(Color.RED);
                itemRemove.addActionListener(ev -> {
                    if (adminImageName != null) new File("images/" + adminImageName).delete();
                    adminImageName = null;
                    PhotoHelper.setPhotoOnLabel(lblAdminPhoto, null, 160, 145, "Click for Image Options");
                });

                menu.add(itemShow);
                menu.add(itemUpload);
                if (adminImageName != null && !adminImageName.isEmpty()) {
                    menu.addSeparator();
                    menu.add(itemRemove);
                }
                menu.show(lblAdminPhoto, e.getX(), e.getY());
            }
        });

        JButton btnSave = new JButton("Save Changes");
        btnSave.setBounds(150, 395, 200, 40);
        btnSave.setMargin(new Insets(0, 0, 0, 0));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setForeground(Color.WHITE);
        PhotoHelper.applyHoverEffect(btnSave, new Color(34, 197, 94), new Color(22, 163, 74));
        btnSave.addActionListener(e -> {
            try (Connection con = ConnectionProvider.getCon()) {
                PreparedStatement pst = con.prepareStatement("UPDATE admins SET full_name=?, email=?, password=?, image_name=? WHERE id=?");
                pst.setString(1, txtName.getText().trim());
                pst.setString(2, txtEmail.getText().trim());
                pst.setString(3, txtPass.getText().trim());
                pst.setString(4, adminImageName);
                pst.setInt(5, adminId);
                pst.executeUpdate();

                lblWelcome.setText("Authenticated Administrator: " + txtName.getText().trim() + " | Status: Online");
                JOptionPane.showMessageDialog(dialog, "Administrator profile updated successfully.", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to update profile: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(btnSave);

        dialog.setVisible(true);
    }
}