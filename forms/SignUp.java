package forms;

import dao.ConnectionProvider;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.*;
import utility.PhotoHelper;

public class SignUp extends JFrame {

    private final JComboBox<String> cmbRole, cmbGender, cmbBranch, cmbSem;
    private final JTextField txtName, txtEmail, txtRollNo, txtContact;
    private final JPasswordField txtPassword;
    private final JLabel lblRollNo, lblGender, lblContact, lblBranch, lblSem;

    public SignUp() {
        setSize(600, 580);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(new Color(15, 23, 42));
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(56, 189, 248)));

        JButton btnExit = new JButton("X");
        btnExit.setBounds(540, 15, 45, 32);
        btnExit.setMargin(new Insets(0, 0, 0, 0));
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnExit, new Color(239, 68, 68), new Color(220, 38, 38));
        btnExit.addActionListener(e -> System.exit(0));
        add(btnExit);

        JLabel lblTitle = new JLabel("USER REGISTRATION", SwingConstants.CENTER);
        lblTitle.setBounds(0, 20, 600, 35);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(56, 189, 248));
        add(lblTitle);

        JLabel lblSub = new JLabel("Create a New Student or Administrator Account", SwingConstants.CENTER);
        lblSub.setBounds(0, 58, 600, 22);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(148, 163, 184));
        add(lblSub);

        addLabel("Register As:", 80, 100);
        cmbRole = new JComboBox<>(new String[]{"Student", "Administrator"});
        cmbRole.setBounds(220, 100, 290, 32);
        cmbRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(cmbRole);

        addLabel("Full Name:", 80, 145);
        txtName = addTextField(220, 145);

        addLabel("Email Address:", 80, 190);
        txtEmail = addTextField(220, 190);

        addLabel("Password:", 80, 235);
        txtPassword = new JPasswordField();
        txtPassword.setBounds(220, 235, 290, 32);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(txtPassword);

        lblRollNo = addLabel("Roll Number:", 80, 280);
        txtRollNo = addTextField(220, 280);

        lblGender = addLabel("Gender:", 80, 325);
        cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
        cmbGender.setBounds(220, 325, 290, 32);
        cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(cmbGender);

        lblContact = addLabel("Contact Number:", 80, 370);
        txtContact = addTextField(220, 370);

        lblBranch = addLabel("Branch & Sem:", 80, 415);
        cmbBranch = new JComboBox<>(new String[]{"CSE", "IT", "ECE", "EE", "ME", "Civil"});
        cmbBranch.setBounds(220, 415, 140, 32);
        cmbBranch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(cmbBranch);

        lblSem = new JLabel("");
        cmbSem = new JComboBox<>(new String[]{"1st Sem", "2nd Sem", "3rd Sem", "4th Sem", "5th Sem", "6th Sem", "7th Sem", "8th Sem"});
        cmbSem.setBounds(370, 415, 140, 32);
        cmbSem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(cmbSem);

        cmbRole.addActionListener(e -> toggleStudentFields());

        JButton btnRegister = new JButton("Create Account");
        btnRegister.setBounds(220, 480, 140, 40);
        btnRegister.setMargin(new Insets(0, 0, 0, 0));
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnRegister, new Color(34, 197, 94), new Color(22, 163, 74));
        btnRegister.addActionListener(e -> registerUser());
        add(btnRegister);

        JButton btnBack = new JButton("Back to Sign In");
        btnBack.setBounds(370, 480, 140, 40);
        btnBack.setMargin(new Insets(0, 0, 0, 0));
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnBack, new Color(59, 130, 246), new Color(37, 99, 235));
        btnBack.addActionListener(e -> {
            this.dispose();
            new Login().setVisible(true);
        });
        add(btnBack);
    }

    private JLabel addLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(x, y, 130, 30);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        add(lbl);
        return lbl;
    }

    private JTextField addTextField(int x, int y) {
        JTextField txt = new JTextField();
        txt.setBounds(x, y, 290, 32);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(txt);
        return txt;
    }

    private void toggleStudentFields() {
        boolean isStudent = cmbRole.getSelectedItem().toString().equals("Student");
        lblRollNo.setVisible(isStudent);
        txtRollNo.setVisible(isStudent);
        lblGender.setVisible(isStudent);
        cmbGender.setVisible(isStudent);
        lblContact.setVisible(isStudent);
        txtContact.setVisible(isStudent);
        lblBranch.setVisible(isStudent);
        cmbBranch.setVisible(isStudent);
        lblSem.setVisible(isStudent);
        cmbSem.setVisible(isStudent);
    }

    private void registerUser() {
        String role = cmbRole.getSelectedItem().toString();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name, Email Address, and Password are required fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed. Please check server status.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (role.equals("Administrator")) {
                String query = "INSERT INTO admins (full_name, email, password) VALUES (?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setString(2, email);
                pst.setString(3, password);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Administrator account registered successfully. You may now sign in.", "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String rollNo = txtRollNo.getText().trim();
                String gender = cmbGender.getSelectedItem().toString();
                String contact = txtContact.getText().trim();
                String branch = cmbBranch.getSelectedItem().toString();
                String sem = cmbSem.getSelectedItem().toString();
                String uniqueRegId = "SP-" + System.currentTimeMillis();

                if (rollNo.isEmpty() || contact.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Roll Number and Contact Number are mandatory for student registration.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String query = "INSERT INTO students (name, roll_no, gender, email, contact, branch, semester, unique_reg_id, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, name);
                pst.setString(2, rollNo);
                pst.setString(3, gender);
                pst.setString(4, email);
                pst.setString(5, contact);
                pst.setString(6, branch);
                pst.setString(7, sem);
                pst.setString(8, uniqueRegId);
                pst.setString(9, password);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student account registered successfully. Digital Pass ID generated.", "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            }

            this.dispose();
            new Login().setVisible(true);

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "An account with this Email or Roll Number already exists in the system.", "Duplicate Record", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}