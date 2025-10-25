package gui;

import model.User;
import javax.swing.*;
import java.awt.*;

public class UserPanel extends JPanel {

    private final User currentUser;

    public UserPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));

        JLabel header = new JLabel("\uD83D\uDC64 Your Profile", SwingConstants.CENTER); // 👤 Your Profile
        header.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        JLabel name = new JLabel("Name: " + currentUser.getFullName());
        JLabel email = new JLabel("Email: " + currentUser.getEmail());
        JLabel country = new JLabel("Country: (update later)");

        for (JLabel l : new JLabel[]{name, email, country}) {
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            info.add(l);
            info.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JButton logout = new JButton("\uD83D\uDEAA Logout"); // 🚪 Logout
        logout.setBackground(new Color(220, 53, 69));
        logout.setForeground(Color.WHITE);
        logout.setFocusPainted(false);
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                SwingUtilities.getWindowAncestor(this).dispose();
            }
        });

        info.add(Box.createRigidArea(new Dimension(0, 20)));
        info.add(logout);

        add(info, BorderLayout.CENTER);
    }
}
