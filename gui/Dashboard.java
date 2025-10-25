package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import model.Song;
import model.User;
import db.DBConnection;
import java.sql.*;

public class Dashboard extends JFrame {
    private final User user;
    private final AudioPlayer player;
    private SongsPanel songsPanel;

    public Dashboard(User user) {
        this.user = user;
        this.player = AudioPlayer.getInstance();

        // FRAME SETUP
        setTitle("\uD83C\uDFB5 Welcome " + user.getFullName());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(18, 18, 18));

        // HEADER PANEL
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 24, 24));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel header = new JLabel("\uD83C\uDFA7 Hello, " + user.getFullName() + "!");
        header.setFont(new Font("Segoe UI Emoji", Font.BOLD, 26));
        header.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, new Color(200, 50, 50));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new AuthPage().setVisible(true);
            }
        });

        headerPanel.add(header, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // TABBED PANE
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        tabs.setBackground(new Color(30, 30, 30));
        tabs.setForeground(Color.WHITE);

        // Load songs from DB
        List<Song> allSongs = getAllSongsFromDB();

        // Initialize SongsPanel
        songsPanel = new SongsPanel(allSongs);
        tabs.addTab("\u266B Songs", songsPanel);
        tabs.addTab("\uD83C\uDFA4 Artists", new ArtistPanel());
        tabs.addTab("\uD83C\uDFB6 Playlists", new PlaylistPanel(user));

        add(tabs, BorderLayout.CENTER);

        // FOOTER PANEL
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(24, 24, 24));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        footerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel playingLabel = new JLabel("No song playing");
        playingLabel.setForeground(Color.WHITE);
        playingLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        footerPanel.add(playingLabel);

        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // BUTTON STYLE
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
    }

    // LOAD SONGS FROM DB
    private List<Song> getAllSongsFromDB() {
        List<Song> songs = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM songs")) {

            while (rs.next()) {
                java.sql.Date releaseDate = rs.getDate("release_date");
                songs.add(new Song(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        releaseDate != null ? releaseDate.toString() : "",
                        rs.getString("image_path"),
                        rs.getString("audio_path")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading songs: " + e.getMessage());
        }
        return songs;
    }
}
