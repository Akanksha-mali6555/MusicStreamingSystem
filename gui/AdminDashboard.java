package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.sql.*;
import db.DBConnection;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Modern dark-themed AdminDashboard with file browsing for images & audio,
 * image preview, and simple JavaFX-based audio playback.
 *
 * NOTE: JavaFX runtime must be available on classpath (OpenJFX). The code
 * initializes JavaFX via new JFXPanel().
 */
public class AdminDashboard extends JFrame {

    private final String emojiFont;
    private JTable userTable, songTable, artistTable;
    private DefaultTableModel userModel, songModel, artistModel;

    // JavaFX audio player (single shared player)
    private MediaPlayer fxPlayer;
    private final JFXPanel jfxPanel; // ensures JavaFX runtime initialized

    public AdminDashboard() {
        // init JavaFX runtime for audio
        jfxPanel = new JFXPanel(); // creates JavaFX runtime
        emojiFont = detectEmojiFont();

        setTitle("👑 Admin Dashboard");
        setSize(1200, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Global look tweaks (fonts)
        UIManager.put("Label.font", new Font(emojiFont, Font.PLAIN, 14));
        UIManager.put("Button.font", new Font(emojiFont, Font.BOLD, 14));
        UIManager.put("Table.font", new Font(emojiFont, Font.PLAIN, 13));
        UIManager.put("TableHeader.font", new Font(emojiFont, Font.BOLD, 13));

        // Top header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(emojiFont, Font.BOLD, 15));
        tabs.setBackground(new Color(18, 18, 18));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("👤 Users", createUserPanel());
        tabs.addTab("🎵 Songs", createSongPanel());
        tabs.addTab("🎤 Artists", createArtistPanel());

        add(tabs, BorderLayout.CENTER);

        // Footer small player for selected song (play/stop)
        add(createMiniPlayerPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // -------------------- Header --------------------
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(12, 12, 12));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("🎧 Admin Control Panel");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(emojiFont, Font.BOLD, 26));
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton refreshAll = createStyledButton("🔄 Refresh All", new Color(60, 160, 230));
        refreshAll.addActionListener(e -> {
            loadUsers();
            loadSongs();
            loadArtists();
        });

        JButton logoutBtn = createStyledButton("🚪 Logout", new Color(220, 80, 80));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Logout from Admin Panel?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                // new AuthPage().setVisible(true); // uncomment if AuthPage exists
            }
        });

        right.add(refreshAll);
        right.add(logoutBtn);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    // -------------------- USERS --------------------
    private JPanel createUserPanel() {
        JPanel panel = createBasePanel();
        String[] cols = {"User_ID", "Full_Name", "Email", "Date_of_Birth", "Country"};
        userModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        userTable = new JTable(userModel);
        styleTable(userTable);

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(8, 8, 8, 8));

        btnRow.add(createStyledButton("➕ Add User", new Color(90, 180, 140), e -> addUser()));
        btnRow.add(createStyledButton("✏️ Edit User", new Color(90, 140, 220), e -> editUser()));
        btnRow.add(createStyledButton("🗑 Delete User", new Color(200, 80, 80), e -> deleteUser()));
        btnRow.add(createStyledButton("🔄 Refresh", new Color(130, 130, 130), e -> loadUsers()));

        panel.add(btnRow, BorderLayout.SOUTH);
        loadUsers();
        return panel;
    }

    private void loadUsers() {
        userModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM user")) {
            while (rs.next()) {
                userModel.addRow(new Object[]{
                        rs.getInt("User_ID"),
                        rs.getString("Full_Name"),
                        rs.getString("Email"),
                        rs.getString("Date_of_Birth"),
                        rs.getString("Country")
                });
            }
        } catch (Exception e) {
            showError("Error loading users", e);
        }
    }

    private void addUser() {
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField password = new JTextField();
        JTextField dob = new JTextField();
        JTextField country = new JTextField();

        Object[] fields = {"Full Name:", name, "Email:", email, "Password:", password, "Date of Birth:", dob, "Country:", country};
        int opt = JOptionPane.showConfirmDialog(this, fields, "Add User", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO user (Full_Name, Email, Password, Date_of_Birth, Country) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, name.getText());
                ps.setString(2, email.getText());
                ps.setString(3, password.getText());
                ps.setString(4, dob.getText());
                ps.setString(5, country.getText());
                ps.executeUpdate();
                showInfo("User added");
                loadUsers();
            } catch (Exception ex) {
                showError("Error adding user", ex);
            }
        }
    }

    private void editUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "⚠ Select a user to edit."); return; }

        int id = (int) userModel.getValueAt(row, 0);
        JTextField name = new JTextField((String) userModel.getValueAt(row, 1));
        JTextField email = new JTextField((String) userModel.getValueAt(row, 2));
        JTextField dob = new JTextField((String) userModel.getValueAt(row, 3));
        JTextField country = new JTextField((String) userModel.getValueAt(row, 4));

        Object[] fields = {"Full Name:", name, "Email:", email, "Date of Birth:", dob, "Country:", country};
        int opt = JOptionPane.showConfirmDialog(this, fields, "Edit User", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE user SET Full_Name=?, Email=?, Date_of_Birth=?, Country=? WHERE User_ID=?")) {
                ps.setString(1, name.getText());
                ps.setString(2, email.getText());
                ps.setString(3, dob.getText());
                ps.setString(4, country.getText());
                ps.setInt(5, id);
                ps.executeUpdate();
                showInfo("User updated");
                loadUsers();
            } catch (Exception ex) {
                showError("Error updating user", ex);
            }
        }
    }

    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "⚠ Select a user to delete."); return; }
        int id = (int) userModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM user WHERE User_ID=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                showInfo("User deleted");
                loadUsers();
            } catch (Exception ex) {
                showError("Error deleting user", ex);
            }
        }
    }

    // -------------------- SONGS --------------------
    private JPanel createSongPanel() {
        JPanel panel = createBasePanel();
        String[] cols = {"ID", "Title", "Artist", "Genre", "Duration(s)", "Release_Date", "Image_Path", "Audio_Path"};
        songModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        songTable = new JTable(songModel);
        styleTable(songTable);

        panel.add(new JScrollPane(songTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(8, 8, 8, 8));

        btnRow.add(createStyledButton("➕ Add Song", new Color(60, 180, 140), e -> openSongDialog(null)));
        btnRow.add(createStyledButton("✏️ Edit Song", new Color(60, 140, 220), e -> editSongAction()));
        btnRow.add(createStyledButton("🗑 Delete Song", new Color(200, 80, 80), e -> deleteSong()));
        btnRow.add(createStyledButton("🔄 Refresh", new Color(130, 130, 130), e -> loadSongs()));

        panel.add(btnRow, BorderLayout.SOUTH);
        loadSongs();
        return panel;
    }

    private void loadSongs() {
        songModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM songs")) {
            while (rs.next()) {
                songModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getString("release_date"),
                        rs.getString("image_path"),
                        rs.getString("audio_path")
                });
            }
        } catch (Exception e) {
            showError("Error loading songs", e);
        }
    }

    private void openSongDialog(Integer editSongId) {
        // If editSongId == null -> add, else edit
        JDialog dialog = new JDialog(this, editSongId == null ? "Add Song" : "Edit Song", true);
        dialog.setSize(600, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(22, 22, 22));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleF = new JTextField();
        JTextField artistF = new JTextField();
        JTextField genreF = new JTextField();
        JTextField durationF = new JTextField();
        JTextField releaseF = new JTextField();
        JTextField imagePathF = new JTextField();
        JTextField audioPathF = new JTextField();

        // image preview
        JLabel imagePreview = new JLabel();
        imagePreview.setPreferredSize(new Dimension(140, 140));
        imagePreview.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2, true));

        // Browse buttons
        JButton browseImage = createStyledButton("📁 Browse Image", new Color(80, 130, 200));
        browseImage.addActionListener(e -> {
            File f = chooseFile(dialog, new String[]{"png", "jpg", "jpeg"}, "Image Files");
            if (f != null) {
                imagePathF.setText(f.getAbsolutePath());
                imagePreview.setIcon(loadScaledIcon(f.getAbsolutePath(), 140, 140));
            }
        });

        JButton browseAudio = createStyledButton("🎵 Browse Audio", new Color(60, 170, 140));
        browseAudio.addActionListener(e -> {
            File f = chooseFile(dialog, new String[]{"mp3", "wav", "aac", "m4a"}, "Audio Files");
            if (f != null) {
                audioPathF.setText(f.getAbsolutePath());
            }
        });

        JButton testPlay = createStyledButton("▶ Test Play", new Color(50, 200, 180));
        testPlay.addActionListener(e -> {
            String path = audioPathF.getText();
            if (path == null || path.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Select an audio file first.");
                return;
            }
            playAudio(path);
        });

        JButton stopPlay = createStyledButton("⏹ Stop", new Color(200, 80, 80));
        stopPlay.addActionListener(e -> stopAudio());

        // place fields
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        content.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 0.85;
        content.add(titleF, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.15;
        content.add(new JLabel("Artist:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; content.add(artistF, gbc);

        gbc.gridx = 0; gbc.gridy = 2; content.add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; content.add(genreF, gbc);
        gbc.gridx = 2; gbc.gridy = 2; content.add(new JLabel("Duration (s)"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2; content.add(durationF, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; content.add(new JLabel("Release:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2; content.add(releaseF, gbc);

        gbc.gridx = 0; gbc.gridy = 5; content.add(new JLabel("Image:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; content.add(imagePathF, gbc);
        gbc.gridx = 2; gbc.gridy = 5; content.add(browseImage, gbc);

        gbc.gridx = 0; gbc.gridy = 6; content.add(new JLabel("Audio:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; content.add(audioPathF, gbc);
        gbc.gridx = 2; gbc.gridy = 6; content.add(browseAudio, gbc);

        // preview + play controls on right side
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(imagePreview);
        right.add(Box.createRigidArea(new Dimension(0, 8)));
        JPanel playRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        playRow.setOpaque(false);
        playRow.add(testPlay);
        playRow.add(stopPlay);
        right.add(playRow);

        // if editing, populate fields
        if (editSongId != null) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT * FROM songs WHERE id = ?")) {
                ps.setInt(1, editSongId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        titleF.setText(rs.getString("title"));
                        artistF.setText(rs.getString("artist"));
                        genreF.setText(rs.getString("genre"));
                        durationF.setText(String.valueOf(rs.getInt("duration")));
                        releaseF.setText(rs.getString("release_date"));
                        imagePathF.setText(rs.getString("image_path"));
                        audioPathF.setText(rs.getString("audio_path"));
                        if (imagePathF.getText() != null && !imagePathF.getText().isEmpty()) {
                            imagePreview.setIcon(loadScaledIcon(imagePathF.getText(), 140, 140));
                        }
                    }
                }
            } catch (Exception ex) {
                showError("Error loading song for edit", ex);
            }
        }

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(right, BorderLayout.EAST);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton ok = createStyledButton("✅ Save", new Color(70, 180, 120));
        ok.addActionListener(e -> {
            try {
                String t = titleF.getText();
                String art = artistF.getText();
                String g = genreF.getText();
                int dur = Integer.parseInt(durationF.getText().isEmpty() ? "0" : durationF.getText());
                String rel = releaseF.getText();
                String img = imagePathF.getText();
                String aud = audioPathF.getText();

                if (editSongId == null) {
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement("INSERT INTO songs (title, artist, genre, duration, release_date, image_path, audio_path) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setString(1, t);
                        ps.setString(2, art);
                        ps.setString(3, g);
                        ps.setInt(4, dur);
                        ps.setString(5, rel);
                        ps.setString(6, img);
                        ps.setString(7, aud);
                        ps.executeUpdate();
                    }
                    showInfo("Song added");
                } else {
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement("UPDATE songs SET title=?, artist=?, genre=?, duration=?, release_date=?, image_path=?, audio_path=? WHERE id=?")) {
                        ps.setString(1, t);
                        ps.setString(2, art);
                        ps.setString(3, g);
                        ps.setInt(4, dur);
                        ps.setString(5, rel);
                        ps.setString(6, img);
                        ps.setString(7, aud);
                        ps.setInt(8, editSongId);
                        ps.executeUpdate();
                    }
                    showInfo("Song updated");
                }
                dialog.dispose();
                loadSongs();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Duration must be a number (seconds).");
            } catch (Exception ex) {
                showError("Error saving song", ex);
            }
        });

        JButton cancel = createStyledButton("Cancel", new Color(120, 120, 120));
        cancel.addActionListener(e -> dialog.dispose());

        bottom.add(cancel);
        bottom.add(ok);

        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSongAction() {
        int row = songTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "⚠ Select a song to edit."); return; }
        int id = (int) songModel.getValueAt(row, 0);
        openSongDialog(id);
    }

    private void deleteSong() {
        int row = songTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "⚠ Select a song to delete."); return; }
        int id = (int) songModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this song?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM songs WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                showInfo("Song deleted");
                loadSongs();
            } catch (Exception ex) {
                showError("Error deleting song", ex);
            }
        }
    }

    // -------------------- ARTISTS --------------------
    private JPanel createArtistPanel() {
        JPanel panel = createBasePanel();
        String[] cols = {"Artist_ID", "Name", "Country", "Image_Path"};
        artistModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        artistTable = new JTable(artistModel);
        styleTable(artistTable);

        panel.add(new JScrollPane(artistTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(8, 8, 8, 8));

        btnRow.add(createStyledButton("➕ Add Artist", new Color(90, 180, 140), e -> openArtistDialog(null)));
        btnRow.add(createStyledButton("✏️ Edit Artist", new Color(90, 140, 220), e -> editArtistAction()));
        btnRow.add(createStyledButton("🗑 Delete Artist", new Color(200, 80, 80), e -> deleteArtist()));
        btnRow.add(createStyledButton("🔄 Refresh", new Color(130, 130, 130), e -> loadArtists()));

        panel.add(btnRow, BorderLayout.SOUTH);
        loadArtists();
        return panel;
    }

    private void loadArtists() {
        artistModel.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM artist")) {
            while (rs.next()) {
                artistModel.addRow(new Object[]{
                        rs.getInt("artist_id"),
                        rs.getString("name"),
                        rs.getString("country"),
                        rs.getString("image_path")
                });
            }
        } catch (Exception e) {
            showError("Error loading artists", e);
        }
    }

    private void openArtistDialog(Integer editArtistId) {
        JDialog dialog = new JDialog(this, editArtistId == null ? "Add Artist" : "Edit Artist", true);
        dialog.setSize(520, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(22, 22, 22));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameF = new JTextField();
        JTextField countryF = new JTextField();
        JTextField imagePathF = new JTextField();

        JLabel preview = new JLabel();
        preview.setPreferredSize(new Dimension(160, 160));
        preview.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2, true));

        JButton browseImg = createStyledButton("📁 Browse Image", new Color(80, 130, 200));
        browseImg.addActionListener(e -> {
            File f = chooseFile(dialog, new String[]{"png","jpg","jpeg"}, "Image Files");
            if (f != null) {
                imagePathF.setText(f.getAbsolutePath());
                preview.setIcon(loadScaledIcon(f.getAbsolutePath(), 160, 160));
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        content.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 0.8;
        content.add(nameF, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        content.add(new JLabel("Country:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        content.add(countryF, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        content.add(new JLabel("Image:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        content.add(imagePathF, gbc);
        gbc.gridx = 2; gbc.gridy = 2;
        content.add(browseImg, gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.gridheight = 3;
        content.add(preview, gbc);

        // If editing, load row
        if (editArtistId != null) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT * FROM artist WHERE artist_id = ?")) {
                ps.setInt(1, editArtistId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nameF.setText(rs.getString("name"));
                        countryF.setText(rs.getString("country"));
                        imagePathF.setText(rs.getString("image_path"));
                        if (imagePathF.getText() != null && !imagePathF.getText().isEmpty())
                            preview.setIcon(loadScaledIcon(imagePathF.getText(), 160, 160));
                    }
                }
            } catch (Exception ex) {
                showError("Error loading artist for edit", ex);
            }
        }

        dialog.add(content, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton save = createStyledButton("✅ Save", new Color(70, 180, 120));
        save.addActionListener(e -> {
            try {
                String n = nameF.getText();
                String c = countryF.getText();
                String img = imagePathF.getText();
                if (editArtistId == null) {
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement("INSERT INTO artist (name, country, image_path) VALUES (?, ?, ?)")) {
                        ps.setString(1, n);
                        ps.setString(2, c);
                        ps.setString(3, img);
                        ps.executeUpdate();
                    }
                    showInfo("Artist added");
                } else {
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement("UPDATE artist SET name=?, country=?, image_path=? WHERE artist_id=?")) {
                        ps.setString(1, n);
                        ps.setString(2, c);
                        ps.setString(3, img);
                        ps.setInt(4, editArtistId);
                        ps.executeUpdate();
                    }
                    showInfo("Artist updated");
                }
                dialog.dispose();
                loadArtists();
            } catch (Exception ex) {
                showError("Error saving artist", ex);
            }
        });

        JButton cancel = createStyledButton("Cancel", new Color(120,120,120));
        cancel.addActionListener(e -> dialog.dispose());
        bottom.add(cancel);
        bottom.add(save);

        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editArtistAction() {
        int row = artistTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "⚠ Select an artist to edit."); return; }
        int id = (int) artistModel.getValueAt(row, 0);
        openArtistDialog(id);
    }

    private void deleteArtist() {
        int row = artistTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "⚠ Select an artist to delete."); return; }
        int id = (int) artistModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this artist?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("DELETE FROM artist WHERE artist_id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                showInfo("Artist deleted");
                loadArtists();
            } catch (Exception ex) {
                showError("Error deleting artist", ex);
            }
        }
    }

    // -------------------- AUDIO PLAYBACK --------------------
    private void playAudio(String path) {
        stopAudio();
        try {
            File f = new File(path);
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, "Audio file not found: " + path);
                return;
            }
            Media media = new Media(f.toURI().toString());
            fxPlayer = new MediaPlayer(media);
            fxPlayer.play();
        } catch (Exception e) {
            showError("Could not play audio", e);
        }
    }

    private void stopAudio() {
        try {
            if (fxPlayer != null) {
                fxPlayer.stop();
                fxPlayer.dispose();
                fxPlayer = null;
            }
        } catch (Exception e) {
            // ignore
        }
    }

    // -------------------- MINI PLAYER PANEL --------------------
    private JPanel createMiniPlayerPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        p.setBackground(new Color(18, 18, 18));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 40, 40)));

        JButton playSel = createStyledButton("▶ Play Selected", new Color(60, 170, 150));
        playSel.addActionListener(e -> {
            int row = songTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a song from the Songs tab."); return; }
            String audioPath = (String) songModel.getValueAt(row, 7);
            if (audioPath == null || audioPath.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selected song doesn't have an audio path.");
                return;
            }
            playAudio(audioPath);
        });

        JButton stopSel = createStyledButton("⏹ Stop", new Color(200, 80, 80));
        stopSel.addActionListener(e -> stopAudio());

        p.add(playSel);
        p.add(stopSel);
        p.add(Box.createHorizontalStrut(20));
        JLabel note = new JLabel("Tip: Use Add/Edit to browse image/audio files.");
        note.setForeground(new Color(180, 180, 180));
        p.add(note);
        return p;
    }

    // -------------------- UTILITIES --------------------
    private JPanel createBasePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(18, 18, 18));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private JButton createStyledButton(String text, Color bg) {
        return createStyledButton(text, bg, null);
    }

    private JButton createStyledButton(String text, Color bg, ActionListener act) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(emojiFont, Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        if (act != null) btn.addActionListener(act);
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setGridColor(new Color(40, 40, 40));
        table.setShowGrid(false);
        table.setSelectionBackground(new Color(60, 110, 180));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setBackground(new Color(28, 28, 28));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font(emojiFont, Font.BOLD, 13));

        // Alternate row colors renderer
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            final Color alt = new Color(22, 22, 22);
            final Color even = new Color(16, 16, 16);
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setForeground(Color.WHITE);
                if (!isSelected) c.setBackground(row % 2 == 0 ? even : alt);
                return c;
            }
        });
    }

    private File chooseFile(Component parent, String[] exts, String desc) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(desc, exts));
        chooser.setDialogTitle("Select " + desc);
        int r = chooser.showOpenDialog(parent);
        if (r == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;
    }

    private ImageIcon loadScaledIcon(String pathOrResource, int w, int h) {
        try {
            Image img = null;
            // try resource first
            URL res = getClass().getClassLoader().getResource(pathOrResource);
            if (res != null) img = ImageIO.read(res);
            if (img == null) {
                File f = new File(pathOrResource);
                if (f.exists()) img = ImageIO.read(f);
            }
            if (img != null) {
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            // ignore
        }
        // return placeholder
        BufferedImagePlaceholder placeholder = new BufferedImagePlaceholder(w, h);
        return new ImageIcon(placeholder.getImage());
    }

    private String detectEmojiFont() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) return "Apple Color Emoji";
        if (os.contains("nux") || os.contains("nix")) return "Noto Color Emoji";
        return "Segoe UI Emoji";
    }

    private void showError(String msg, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "⚠ " + msg + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // -------------------- Small placeholder image generator --------------------
    // Simple inner helper to create a neutral image when file not found.
    private static class BufferedImagePlaceholder {
        private final Image image;
        BufferedImagePlaceholder(int w, int h) {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(40, 40, 40));
            g.fillRect(0,0,w,h);
            g.setColor(new Color(90, 90, 90));
            g.drawRect(2,2,w-4,h-4);
            g.setColor(new Color(140,140,140));
            g.setFont(new Font("Arial", Font.PLAIN, Math.max(12, w/10)));
            FontMetrics fm = g.getFontMetrics();
            String s = "No Image";
            int sw = fm.stringWidth(s);
            g.drawString(s, (w-sw)/2, h/2 + fm.getAscent()/2);
            g.dispose();
            image = img;
        }
        public Image getImage() { return image; }
    }

    // -------------------- Main --------------------
    public static void main(String[] args) {
        // Ensure look & feel (optional)
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
