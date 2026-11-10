package forms;

import dao.ConnectionProvider;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import utility.PhotoHelper;

public class ManageStudents extends JFrame {

    private final JTextField txtName, txtRollNo, txtEmail, txtContact, txtSearch;
    private final JPasswordField txtPassword;
    private final JComboBox<String> cmbGender, cmbBranch, cmbSem;
    private final JLabel lblImage;
    private final JTable studentTable;
    private final DefaultTableModel tableModel;

    private int selectedStudentId = -1;
    private File selectedImageFile = null;
    private String currentImageName = null;
    private boolean removePhotoFlag = false;

    public ManageStudents() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(new Color(15, 23, 42));
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(56, 189, 248)));

        JLabel lblTitle = new JLabel("STUDENT DIRECTORY MANAGEMENT (ENROLL / UPDATE / DELETE)");
        lblTitle.setBounds(40, 20, 820, 35);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(56, 189, 248));
        add(lblTitle);

        JButton btnBack = new JButton("Return to Console");
        btnBack.setBounds(screen.width - 200, 20, 160, 36);
        btnBack.setMargin(new Insets(0, 0, 0, 0));
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnBack, new Color(239, 68, 68), new Color(220, 38, 38));
        btnBack.addActionListener(e -> this.dispose());
        add(btnBack);

        JPanel formPanel = new JPanel();
        formPanel.setBounds(40, 75, 440, screen.height - 115);
        formPanel.setBackground(new Color(30, 41, 59));
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 1));
        formPanel.setLayout(null);
        add(formPanel);

        JLabel lblFormHead = new JLabel("Student Enrollment Form", SwingConstants.CENTER);
        lblFormHead.setBounds(0, 15, 440, 30);
        lblFormHead.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblFormHead.setForeground(new Color(56, 189, 248));
        formPanel.add(lblFormHead);

        lblImage = new JLabel("Click for Image Options", SwingConstants.CENTER);
        lblImage.setBounds(145, 55, 150, 140);
        lblImage.setForeground(Color.LIGHT_GRAY);
        lblImage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblImage.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 2));
        lblImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPhotoMenu(e);
            }
        });
        formPanel.add(lblImage);

        addFormLabel(formPanel, "Full Name:", 25, 215);
        txtName = addFormTextField(formPanel, 145, 215);

        addFormLabel(formPanel, "Roll Number:", 25, 260);
        txtRollNo = addFormTextField(formPanel, 145, 260);

        addFormLabel(formPanel, "Gender:", 25, 305);
        cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
        cmbGender.setBounds(145, 305, 265, 30);
        cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(cmbGender);

        addFormLabel(formPanel, "Email Address:", 25, 350);
        txtEmail = addFormTextField(formPanel, 145, 350);

        addFormLabel(formPanel, "Contact No:", 25, 395);
        txtContact = addFormTextField(formPanel, 145, 395);

        addFormLabel(formPanel, "Branch & Sem:", 25, 440);
        cmbBranch = new JComboBox<>(new String[]{"CSE", "IT", "ECE", "EE", "ME", "Civil"});
        cmbBranch.setBounds(145, 440, 125, 30);
        cmbBranch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(cmbBranch);

        cmbSem = new JComboBox<>(new String[]{"1st Sem", "2nd Sem", "3rd Sem", "4th Sem", "5th Sem", "6th Sem", "7th Sem", "8th Sem"});
        cmbSem.setBounds(280, 440, 130, 30);
        cmbSem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(cmbSem);

        addFormLabel(formPanel, "Password:", 25, 485);
        txtPassword = new JPasswordField();
        txtPassword.setBounds(145, 485, 265, 30);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(txtPassword);

        JButton btnInsert = createActionButton("Enroll", 25, 540, new Color(34, 197, 94), new Color(22, 163, 74));
        btnInsert.addActionListener(e -> insertStudent());
        formPanel.add(btnInsert);

        JButton btnUpdate = createActionButton("Update", 125, 540, new Color(59, 130, 246), new Color(37, 99, 235));
        btnUpdate.addActionListener(e -> updateStudent());
        formPanel.add(btnUpdate);

        JButton btnDelete = createActionButton("Delete", 225, 540, new Color(239, 68, 68), new Color(220, 38, 38));
        btnDelete.addActionListener(e -> deleteStudent());
        formPanel.add(btnDelete);

        JButton btnClear = createActionButton("Reset", 325, 540, new Color(100, 116, 139), new Color(71, 85, 105));
        btnClear.addActionListener(e -> clearForm());
        formPanel.add(btnClear);

        JLabel lblSearch = new JLabel("Search Directory (Name / Roll No / Email):");
        lblSearch.setBounds(510, 80, 330, 30);
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSearch.setForeground(Color.WHITE);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(840, 80, screen.width - 880, 32);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadStudents(txtSearch.getText().trim());
            }
        });
        add(txtSearch);

        String[] cols = {"ID", "Full Name", "Roll No", "Gender", "Email", "Contact", "Branch", "Semester", "Pass ID", "Image File"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        PhotoHelper.styleTable(studentTable);
        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectStudentFromTable();
            }
        });

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.getViewport().setBackground(new Color(30, 41, 59));
        scrollPane.setBounds(510, 125, screen.width - 550, screen.height - 165);
        add(scrollPane);

        loadStudents("");
    }

    private void showPhotoMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem itemShow = new JMenuItem("1. View Full Image");
        itemShow.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemShow.addActionListener(ev -> {
            String path = null;
            if (selectedImageFile != null) path = selectedImageFile.getAbsolutePath();
            else if (currentImageName != null && !removePhotoFlag) path = "images/" + currentImageName;
            PhotoHelper.showFullImage(this, path);
        });

        JMenuItem itemUpload = new JMenuItem("2. Upload / Replace Image");
        itemUpload.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemUpload.addActionListener(ev -> choosePhoto());

        JMenuItem itemRemove = new JMenuItem("3. Remove Image");
        itemRemove.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemRemove.setForeground(Color.RED);
        itemRemove.addActionListener(ev -> {
            selectedImageFile = null;
            removePhotoFlag = true;
            PhotoHelper.setPhotoOnLabel(lblImage, null, 150, 140, "Marked for Removal");
        });

        menu.add(itemShow);
        menu.add(itemUpload);
        if (selectedImageFile != null || (currentImageName != null && !currentImageName.isEmpty() && !removePhotoFlag)) {
            menu.addSeparator();
            menu.add(itemRemove);
        }
        menu.show(lblImage, e.getX(), e.getY());
    }

    private void addFormLabel(JPanel panel, String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(x, y, 115, 30);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl);
    }

    private JTextField addFormTextField(JPanel panel, int x, int y) {
        JTextField txt = new JTextField();
        txt.setBounds(x, y, 265, 30);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txt);
        return txt;
    }

    private JButton createActionButton(String text, int x, int y, Color normalBg, Color hoverBg) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 90, 38);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btn, normalBg, hoverBg);
        return btn;
    }

    private void choosePhoto() {
        File picked = PhotoHelper.showImageChooser(this);
        if (picked != null) {
            selectedImageFile = picked;
            removePhotoFlag = false;
            PhotoHelper.setPhotoOnLabel(lblImage, selectedImageFile.getAbsolutePath(), 150, 140, "Click for Image Options");
        }
    }

    private String saveImageToFolder(String rollNo) {
        if (removePhotoFlag) {
            if (currentImageName != null) {
                new File("images/" + currentImageName).delete();
            }
            return null;
        }
        if (selectedImageFile == null) {
            return currentImageName;
        }
        try {
            File dir = new File("images");
            if (!dir.exists()) dir.mkdirs();
            String fileName = "student_" + rollNo + "_" + System.currentTimeMillis() + ".jpg";
            File dest = new File(dir, fileName);
            Files.copy(selectedImageFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception ex) {
            return currentImageName;
        }
    }

    private void loadStudents(String search) {
        tableModel.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon()) {
            String query = "SELECT * FROM students WHERE name LIKE ? OR roll_no LIKE ? OR email LIKE ? ORDER BY id DESC";
            PreparedStatement pst = con.prepareStatement(query);
            String pattern = "%" + search + "%";
            pst.setString(1, pattern);
            pst.setString(2, pattern);
            pst.setString(3, pattern);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("roll_no"),
                    rs.getString("gender"),
                    rs.getString("email"),
                    rs.getString("contact"),
                    rs.getString("branch"),
                    rs.getString("semester"),
                    rs.getString("unique_reg_id"),
                    rs.getString("image_name")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to retrieve student directory: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectStudentFromTable() {
        int row = studentTable.getSelectedRow();
        if (row < 0) return;

        selectedStudentId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        txtRollNo.setText(tableModel.getValueAt(row, 2).toString());
        cmbGender.setSelectedItem(tableModel.getValueAt(row, 3).toString());
        txtEmail.setText(tableModel.getValueAt(row, 4).toString());
        txtContact.setText(tableModel.getValueAt(row, 5).toString());
        cmbBranch.setSelectedItem(tableModel.getValueAt(row, 6).toString());
        cmbSem.setSelectedItem(tableModel.getValueAt(row, 7).toString());

        Object imgObj = tableModel.getValueAt(row, 9);
        currentImageName = (imgObj != null) ? imgObj.toString() : null;
        selectedImageFile = null;
        removePhotoFlag = false;

        String path = (currentImageName != null && !currentImageName.isEmpty()) ? "images/" + currentImageName : null;
        PhotoHelper.setPhotoOnLabel(lblImage, path, 150, 140, "Click for Image Options");

        try (Connection con = ConnectionProvider.getCon()) {
            PreparedStatement pst = con.prepareStatement("SELECT password FROM students WHERE id = ?");
            pst.setInt(1, selectedStudentId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtPassword.setText(rs.getString("password"));
            }
        } catch (SQLException ex) {
            // Ignore
        }
    }

    private void insertStudent() {
        String name = txtName.getText().trim();
        String rollNo = txtRollNo.getText().trim();
        String gender = cmbGender.getSelectedItem().toString();
        String email = txtEmail.getText().trim();
        String contact = txtContact.getText().trim();
        String branch = cmbBranch.getSelectedItem().toString();
        String sem = cmbSem.getSelectedItem().toString();
        String password = new String(txtPassword.getPassword()).trim();

        if (name.isEmpty() || rollNo.isEmpty() || email.isEmpty() || contact.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All student enrollment fields are mandatory.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String savedImg = saveImageToFolder(rollNo);
        String uniqueId = "SP-" + System.currentTimeMillis();

        try (Connection con = ConnectionProvider.getCon()) {
            String query = "INSERT INTO students (name, roll_no, gender, email, contact, branch, semester, unique_reg_id, image_name, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, rollNo);
            pst.setString(3, gender);
            pst.setString(4, email);
            pst.setString(5, contact);
            pst.setString(6, branch);
            pst.setString(7, sem);
            pst.setString(8, uniqueId);
            pst.setString(9, savedImg);
            pst.setString(10, password);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "New student record enrolled successfully.", "Enrollment Complete", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadStudents("");
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "A student with this Roll Number or Email Address already exists.", "Duplicate Record", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Enrollment Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStudent() {
        if (selectedStudentId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student record from the directory table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        String rollNo = txtRollNo.getText().trim();
        String gender = cmbGender.getSelectedItem().toString();
        String email = txtEmail.getText().trim();
        String contact = txtContact.getText().trim();
        String branch = cmbBranch.getSelectedItem().toString();
        String sem = cmbSem.getSelectedItem().toString();
        String password = new String(txtPassword.getPassword()).trim();

        String savedImg = saveImageToFolder(rollNo);

        try (Connection con = ConnectionProvider.getCon()) {
            String query = "UPDATE students SET name=?, roll_no=?, gender=?, email=?, contact=?, branch=?, semester=?, image_name=?, password=? WHERE id=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, rollNo);
            pst.setString(3, gender);
            pst.setString(4, email);
            pst.setString(5, contact);
            pst.setString(6, branch);
            pst.setString(7, sem);
            pst.setString(8, savedImg);
            pst.setString(9, password);
            pst.setInt(10, selectedStudentId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student record updated successfully.", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadStudents(txtSearch.getText().trim());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        if (selectedStudentId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student record from the directory table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete this student and all associated attendance logs?",
                "Confirm Permanent Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = ConnectionProvider.getCon()) {
                PreparedStatement pst1 = con.prepareStatement("DELETE FROM attendance WHERE student_id = ?");
                pst1.setInt(1, selectedStudentId);
                pst1.executeUpdate();

                PreparedStatement pst2 = con.prepareStatement("DELETE FROM students WHERE id = ?");
                pst2.setInt(1, selectedStudentId);
                pst2.executeUpdate();

                if (currentImageName != null) {
                    new File("images/" + currentImageName).delete();
                }

                JOptionPane.showMessageDialog(this, "Student record and associated logs deleted successfully.", "Deletion Complete", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadStudents(txtSearch.getText().trim());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Deletion Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedStudentId = -1;
        selectedImageFile = null;
        currentImageName = null;
        removePhotoFlag = false;
        txtName.setText("");
        txtRollNo.setText("");
        txtEmail.setText("");
        txtContact.setText("");
        txtPassword.setText("");
        cmbGender.setSelectedIndex(0);
        cmbBranch.setSelectedIndex(0);
        cmbSem.setSelectedIndex(0);
        PhotoHelper.setPhotoOnLabel(lblImage, null, 150, 140, "Click for Image Options");
        studentTable.clearSelection();
    }
}