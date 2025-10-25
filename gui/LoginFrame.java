package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import db.DBConnection;

public class LoginFrame extends JFrame {
    JTextField emailField, regNameField, regEmailField;
    JPasswordField passwordField, regPasswordField;
    JButton loginBtn, registerBtn;
    JPanel mainPanel;

    public LoginFrame() {
        setTitle("🎵 Music Streaming");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new JPanel();
        mainPanel.setBackground(new Color(0xE3EAF2)); // Light bluish background
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Welcome to Music Streaming");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0x1C1B33)); // Dark text
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(title, gbc);

        // Email
        gbc.gridwidth = 1; gbc.gridy++;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(new Color(0x1C1B33));
        gbc.gridx = 0; mainPanel.add(emailLabel, gbc);

        emailField = new JTextField();
        emailField.setBackground(new Color(0xC9D4E3)); // Slightly darker for text field
        emailField.setForeground(new Color(0x1C1B33));
        gbc.gridx = 1; mainPanel.add(emailField, gbc);

        // Password
        gbc.gridy++;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(new Color(0x1C1B33));
        gbc.gridx = 0; mainPanel.add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setBackground(new Color(0xC9D4E3));
        passwordField.setForeground(new Color(0x1C1B33));
        gbc.gridx = 1; mainPanel.add(passwordField, gbc);

        // Buttons
        gbc.gridy++;
        loginBtn = createButton("Login", new Color(0x4CAF50)); // Green
        gbc.gridx = 0; mainPanel.add(loginBtn, gbc);

        registerBtn = createButton("Register", new Color(0xFF5722)); // Orange
        gbc.gridx = 1; mainPanel.add(registerBtn, gbc);

        add(mainPanel);

        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> showRegistrationDialog());

        setVisible(true);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM `user` WHERE Email=? AND Password=?")) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Welcome " + rs.getString("Full_Name") + "!");
               // new Dashboard(rs.getString("Full_Name"));
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showRegistrationDialog() {
        JPanel regPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        regPanel.setBackground(new Color(0xC9D4E3)); // Light panel for registration dialog

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setForeground(new Color(0x1C1B33));
        regPanel.add(nameLabel);
        regNameField = new JTextField();
        regNameField.setBackground(new Color(0xFFFFFF));
        regNameField.setForeground(new Color(0x1C1B33));
        regPanel.add(regNameField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(new Color(0x1C1B33));
        regPanel.add(emailLabel);
        regEmailField = new JTextField();
        regEmailField.setBackground(new Color(0xFFFFFF));
        regEmailField.setForeground(new Color(0x1C1B33));
        regPanel.add(regEmailField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(new Color(0x1C1B33));
        regPanel.add(passLabel);
        regPasswordField = new JPasswordField();
        regPasswordField.setBackground(new Color(0xFFFFFF));
        regPasswordField.setForeground(new Color(0x1C1B33));
        regPanel.add(regPasswordField);

        int result = JOptionPane.showConfirmDialog(this, regPanel, "Register",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            registerUser();
        }
    }

    private void registerUser() {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = new String(regPasswordField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement psCheck = con.prepareStatement(
                     "SELECT * FROM `user` WHERE Email=?")) {

            psCheck.setString(1, email);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Email already registered!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO `user` (Full_Name, Email, Password) VALUES (?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Registration successful! You can now login.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
