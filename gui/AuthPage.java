package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.DBConnection;
import model.User;

public class AuthPage extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField loginEmailField, registerNameField, registerEmailField, registerDobField, registerCountryField;
    private JPasswordField loginPasswordField, registerPasswordField;
    private JLabel loginMessage, registerMessage;

    public AuthPage() {
        setTitle("🎧 Music App - Login / Register");
        setSize(450, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(18, 18, 18));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(18, 18, 18));

        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createRegisterPanel(), "register");

        add(mainPanel);
        cardLayout.show(mainPanel, "login");
        setVisible(true);
    }

    // ---------------- LOGIN PANEL ----------------
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(18, 18, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Welcome Back 🎵", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel emailLabel = new JLabel("Email:");
        styleLabel(emailLabel);
        panel.add(emailLabel);

        loginEmailField = new JTextField();
        styleTextField(loginEmailField);
        panel.add(loginEmailField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel passLabel = new JLabel("Password:");
        styleLabel(passLabel);
        panel.add(passLabel);

        loginPasswordField = new JPasswordField();
        styleTextField(loginPasswordField);
        panel.add(loginPasswordField);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton loginButton = new JButton("Login");
        styleButton(loginButton);
        panel.add(loginButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        loginMessage = new JLabel("", SwingConstants.CENTER);
        loginMessage.setForeground(Color.RED);
        loginMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(loginMessage);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        JLabel switchToRegister = new JLabel("Don’t have an account? Sign up here.", SwingConstants.CENTER);
        switchToRegister.setForeground(new Color(150, 150, 150));
        switchToRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchToRegister.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, "register");
            }
        });
        panel.add(switchToRegister);

        loginButton.addActionListener(e -> handleLogin());
        return panel;
    }

    // ---------------- REGISTER PANEL ----------------
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(18, 18, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Create Account ✨", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        registerNameField = createLabeledTextField(panel, "Full Name:");
        registerEmailField = createLabeledTextField(panel, "Email:");
        registerPasswordField = new JPasswordField();
        styleTextField(registerPasswordField);
        panel.add(new JLabel("Password:"));
        panel.add(registerPasswordField);

        registerDobField = createLabeledTextField(panel, "Date of Birth (YYYY-MM-DD):");
        registerCountryField = createLabeledTextField(panel, "Country:");

        JButton registerButton = new JButton("Register");
        styleButton(registerButton);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(registerButton);

        registerMessage = new JLabel("", SwingConstants.CENTER);
        registerMessage.setForeground(Color.RED);
        registerMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(registerMessage);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        JLabel switchToLogin = new JLabel("Already have an account? Login here.", SwingConstants.CENTER);
        switchToLogin.setForeground(new Color(150, 150, 150));
        switchToLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchToLogin.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, "login");
            }
        });
        panel.add(switchToLogin);

        registerButton.addActionListener(e -> handleRegister());
        return panel;
    }

    private JTextField createLabeledTextField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        styleLabel(label);
        panel.add(label);
        JTextField field = new JTextField();
        styleTextField(field);
        panel.add(field);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        return field;
    }

    // ---------------- LOGIN LOGIC ----------------
    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            loginMessage.setText("⚠ Please fill all fields.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            // ✅ Check Admin login first
            PreparedStatement psAdmin = con.prepareStatement(
                    "SELECT * FROM admin WHERE Email=? AND Password=?");
            psAdmin.setString(1, email);
            psAdmin.setString(2, password);
            ResultSet rsAdmin = psAdmin.executeQuery();

            if (rsAdmin.next()) {
                JOptionPane.showMessageDialog(this,
                        "👑 Welcome Admin " + rsAdmin.getString("Name") + "!");
                dispose();
                new  AdminDashboard(); // ✅ open admin dashboard
                return;
            }

            // ✅ Check User login
            PreparedStatement psUser = con.prepareStatement(
                    "SELECT * FROM user WHERE Email=? AND Password=?");
            psUser.setString(1, email);
            psUser.setString(2, password);
            ResultSet rsUser = psUser.executeQuery();

            if (rsUser.next()) {
                JOptionPane.showMessageDialog(this,
                        "Welcome " + rsUser.getString("Full_Name") + " 🎶");

                User user = new User(
                        rsUser.getInt("User_ID"),
                        rsUser.getString("Full_Name"),
                        rsUser.getString("Email")
                );

                dispose();
                new Dashboard(user);

            } else {
                loginMessage.setText("❌ Invalid credentials!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            loginMessage.setText("⚠ Database error!");
        }
    }

    // ---------------- REGISTER LOGIC ----------------
    private void handleRegister() {
        String name = registerNameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String dob = registerDobField.getText().trim();
        String country = registerCountryField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            registerMessage.setText("⚠ Please fill all required fields.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement check = con.prepareStatement("SELECT * FROM user WHERE Email=?");
            check.setString(1, email);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                registerMessage.setText("⚠ Email already exists!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO user (Full_Name, Email, Password, Date_of_Birth, Country) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, dob.isEmpty() ? null : dob);
            ps.setString(5, country.isEmpty() ? null : country);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "🎉 Account created successfully!");
            cardLayout.show(mainPanel, "login");

        } catch (Exception e) {
            e.printStackTrace();
            registerMessage.setText("⚠ Database error while registering!");
        }
    }

    // ---------------- STYLE HELPERS ----------------
    private void styleLabel(JLabel label) {
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleTextField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBackground(new Color(28, 28, 28));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    }

    private void styleButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(30, 215, 96));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AuthPage::new);
    }
}
