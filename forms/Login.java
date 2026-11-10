package forms;

import dao.ConnectionProvider;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import utility.PhotoHelper;

public class Login extends JFrame {

    private final JComboBox<String> cmbRole;
    private final JTextField txtUserId;
    private final JPasswordField txtPassword;

    public Login() {
        setSize(580, 460);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(new Color(15, 23, 42));
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(56, 189, 248)));

        JButton btnExit = new JButton("X");
        btnExit.setBounds(520, 15, 45, 32);
        btnExit.setMargin(new Insets(0, 0, 0, 0));
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnExit, new Color(239, 68, 68), new Color(220, 38, 38));
        btnExit.addActionListener(e -> System.exit(0));
        add(btnExit);

        JLabel lblTitle = new JLabel("SMARTPASS SUITE", SwingConstants.CENTER);
        lblTitle.setBounds(0, 30, 580, 40);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(new Color(56, 189, 248));
        add(lblTitle);

        JLabel lblSub = new JLabel("Enterprise QR Campus Entry & Attendance System", SwingConstants.CENTER);
        lblSub.setBounds(0, 72, 580, 25);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSub.setForeground(new Color(148, 163, 184));
        add(lblSub);

        JLabel lblRole = new JLabel("Account Role:");
        lblRole.setBounds(80, 130, 120, 32);
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRole.setForeground(Color.WHITE);
        add(lblRole);

        cmbRole = new JComboBox<>(new String[]{"Student", "Administrator"});
        cmbRole.setBounds(210, 130, 280, 34);
        cmbRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(cmbRole);

        JLabel lblEmail = new JLabel("Email / Roll No:");
        lblEmail.setBounds(80, 182, 120, 32);
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEmail.setForeground(Color.WHITE);
        add(lblEmail);

        txtUserId = new JTextField();
        txtUserId.setBounds(210, 182, 280, 34);
        txtUserId.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(txtUserId);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setBounds(80, 234, 110, 32);
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setForeground(Color.WHITE);
        add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(210, 234, 280, 34);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(txtPassword);

        JButton btnLogin = new JButton("Sign In");
        btnLogin.setBounds(210, 300, 130, 40);
        btnLogin.setMargin(new Insets(0, 0, 0, 0));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnLogin, new Color(34, 197, 94), new Color(22, 163, 74));
        btnLogin.addActionListener(e -> handleLogin());
        add(btnLogin);

        JButton btnSignUp = new JButton("Create Account");
        btnSignUp.setBounds(350, 300, 140, 40);
        btnSignUp.setMargin(new Insets(0, 0, 0, 0));
        btnSignUp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSignUp.setForeground(Color.WHITE);
        btnSignUp.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnSignUp, new Color(59, 130, 246), new Color(37, 99, 235));
        btnSignUp.addActionListener(e -> {
            this.dispose();
            new SignUp().setVisible(true);
        });
        add(btnSignUp);
    }

    private void handleLogin() {
        String role = cmbRole.getSelectedItem().toString();
        String userId = txtUserId.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both User ID and Password to proceed.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Unable to establish connection with the database server. Please verify JDBC configuration.", "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (role.equals("Administrator")) {
                String query = "SELECT * FROM admins WHERE email = ? AND password = ?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, userId);
                pst.setString(2, password);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    int adminId = rs.getInt("id");
                    String adminName = rs.getString("full_name");
                    this.dispose();
                    new AdminDashboard(adminId, adminName).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Administrator credentials. Please verify your Email and Password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                String query = "SELECT * FROM students WHERE (email = ? OR roll_no = ?) AND password = ?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, userId);
                pst.setString(2, userId);
                pst.setString(3, password);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    int studentId = rs.getInt("id");
                    this.dispose();
                    new StudentDashboard(studentId).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Student credentials. Please check your Email/Roll Number and Password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Query Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}