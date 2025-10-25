package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import db.DBConnection;
import model.User;

public class PlaylistPanel extends JPanel {
    private User user;
    private JPanel playlistContainer;
    private JPanel songContainer;

    private JButton addPlaylistBtn, editPlaylistBtn, deletePlaylistBtn, addSongBtn;

    private PlaylistItem selectedPlaylist;

    private AudioPlayer audioPlayer = AudioPlayer.getInstance();

    private JProgressBar progressBar;
    private JButton playBtn, pauseBtn, stopBtn;

    private String emojiFont;

    public PlaylistPanel(User user) {
        this.user = user;

        // Detect OS for emoji font
        String os = System.getProperty("os.name").toLowerCase();
        emojiFont = "Segoe UI Emoji"; // Windows
        if (os.contains("mac")) emojiFont = "Apple Color Emoji";
        if (os.contains("nux")) emojiFont = "Noto Color Emoji";

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(20, 20, 20));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ===== LEFT PANEL - Playlists =====
        playlistContainer = new JPanel();
        playlistContainer.setLayout(new BoxLayout(playlistContainer, BoxLayout.Y_AXIS));
        playlistContainer.setBackground(new Color(20, 20, 20));

        JScrollPane playlistScroll = new JScrollPane(playlistContainer);
        playlistScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
                "🎶 Playlists",
                0, 0,
                new Font(emojiFont, Font.BOLD, 16),
                Color.WHITE
        ));
        playlistScroll.getVerticalScrollBar().setUnitIncrement(16);

        addPlaylistBtn = createButton("➕ Add");
        editPlaylistBtn = createButton("✏ Edit");
        deletePlaylistBtn = createButton("🗑 Delete");

        addPlaylistBtn.addActionListener(e -> addPlaylist());
        editPlaylistBtn.addActionListener(e -> editPlaylist());
        deletePlaylistBtn.addActionListener(e -> deletePlaylist());

        JPanel playlistBtnPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        playlistBtnPanel.setBackground(new Color(20, 20, 20));
        playlistBtnPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        playlistBtnPanel.add(addPlaylistBtn);
        playlistBtnPanel.add(editPlaylistBtn);
        playlistBtnPanel.add(deletePlaylistBtn);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(playlistScroll, BorderLayout.CENTER);
        leftPanel.add(playlistBtnPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setBackground(new Color(20, 20, 20));

        // ===== RIGHT PANEL - Songs =====
        songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBackground(new Color(20, 20, 20));

        JScrollPane songScroll = new JScrollPane(songContainer);
        songScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
                "🎵 Songs",
                0, 0,
                new Font(emojiFont, Font.BOLD, 16),
                Color.WHITE
        ));
        songScroll.getVerticalScrollBar().setUnitIncrement(16);

        addSongBtn = createButton("➕ Add Song");
        addSongBtn.setBackground(new Color(70, 180, 180));
        addSongBtn.addActionListener(e -> addSongToPlaylist());

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(songScroll, BorderLayout.CENTER);
        rightPanel.add(addSongBtn, BorderLayout.SOUTH);
        rightPanel.setBackground(new Color(20, 20, 20));

        // ===== BOTTOM PANEL - Player Controls =====
        JPanel playerPanel = new JPanel(new BorderLayout(10, 5));
        playerPanel.setBackground(new Color(25, 25, 25));
        playerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(70, 180, 180));
        progressBar.setBackground(new Color(50, 50, 50));
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setBackground(new Color(25, 25, 25));

        playBtn = createButton("▶");
        pauseBtn = createButton("⏸");
        stopBtn = createButton("⏹");

        playBtn.setFont(new Font(emojiFont, Font.BOLD, 16));
        pauseBtn.setFont(new Font(emojiFont, Font.BOLD, 16));
        stopBtn.setFont(new Font(emojiFont, Font.BOLD, 16));

        playBtn.addActionListener(e -> audioPlayer.resume());
        pauseBtn.addActionListener(e -> audioPlayer.pause());
        stopBtn.addActionListener(e -> audioPlayer.stop());

        controlPanel.add(playBtn);
        controlPanel.add(pauseBtn);
        controlPanel.add(stopBtn);

        playerPanel.add(progressBar, BorderLayout.CENTER);
        playerPanel.add(controlPanel, BorderLayout.EAST);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        add(playerPanel, BorderLayout.SOUTH);

        loadPlaylists();
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(emojiFont, Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(90, 160, 200));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(70, 130, 180));
            }
        });
        return btn;
    }

    // ===== LOAD PLAYLISTS =====
    private void loadPlaylists() {
        playlistContainer.removeAll();
        selectedPlaylist = null;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT Playlist_ID, Playlist_Name FROM playlist WHERE User_ID=?")) {
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PlaylistItem item = new PlaylistItem(
                        rs.getInt("Playlist_ID"),
                        rs.getString("Playlist_Name")
                );
                JPanel card = createPlaylistCard(item);
                playlistContainer.add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading playlists: " + e.getMessage());
        }

        playlistContainer.revalidate();
        playlistContainer.repaint();
        songContainer.removeAll();
    }

    private JPanel createPlaylistCard(PlaylistItem item) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 30, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(item.name);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(emojiFont, Font.PLAIN, 16));

        card.add(label, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedPlaylist = item;
                loadSongs();
                highlightSelectedCard();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(50, 50, 50));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedPlaylist != item)
                    card.setBackground(new Color(30, 30, 30));
            }
        });

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        return card;
    }

    private void highlightSelectedCard() {
        for (Component comp : playlistContainer.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                JLabel lbl = (JLabel) card.getComponent(0);
                if (selectedPlaylist != null && lbl.getText().equals(selectedPlaylist.name)) {
                    card.setBackground(new Color(70, 130, 180));
                } else {
                    card.setBackground(new Color(30, 30, 30));
                }
            }
        }
    }

    // ===== LOAD SONGS =====
    private void loadSongs() {
        songContainer.removeAll();
        if (selectedPlaylist == null) return;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT s.id, s.title, s.artist, s.genre, s.image_path, s.audio_path " +
                             "FROM songs s JOIN playlist_songs ps ON s.id = ps.Song_ID " +
                             "WHERE ps.Playlist_ID=?")) {
            ps.setInt(1, selectedPlaylist.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SongItem song = new SongItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getString("image_path"),
                        rs.getString("audio_path")
                );
                JPanel card = createSongCard(song);
                songContainer.add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        songContainer.revalidate();
        songContainer.repaint();
    }

    private JPanel createSongCard(SongItem song) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 40));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel albumLabel = new JLabel();
        albumLabel.setPreferredSize(new Dimension(50, 50));
        if (song.albumArt != null && !song.albumArt.isEmpty()) {
            File imgFile = new File(song.albumArt);
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(new ImageIcon(song.albumArt).getImage()
                        .getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                albumLabel.setIcon(icon);
            }
        }

        JLabel label = new JLabel(song.title + " - " + song.artist);
        label.setForeground(Color.WHITE);
        label.setFont(new Font(emojiFont, Font.PLAIN, 14));

        JButton playBtnSong = createButton("▶");
        playBtnSong.setFont(new Font(emojiFont, Font.BOLD, 14));
        playBtnSong.addActionListener(e -> {
            if (song.audioPath != null && !song.audioPath.isEmpty()) {
                audioPlayer.playSong(song.audioPath);
            } else {
                JOptionPane.showMessageDialog(this, "Audio file path is missing!");
            }
        });

        JButton deleteBtnSong = createButton("🗑");
        deleteBtnSong.setFont(new Font(emojiFont, Font.BOLD, 14));
        deleteBtnSong.setBackground(new Color(200, 50, 50));
        deleteBtnSong.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteBtnSong.setBackground(new Color(255, 70, 70));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                deleteBtnSong.setBackground(new Color(200, 50, 50));
            }
        });
        deleteBtnSong.addActionListener(e -> deleteSongFromPlaylist(song));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(new Color(40, 40, 40));
        buttonPanel.add(playBtnSong);
        buttonPanel.add(deleteBtnSong);

        card.add(albumLabel, BorderLayout.WEST);
        card.add(label, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(60, 60, 60));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(new Color(40, 40, 40));
            }
        });

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180, 80), 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        return card;
    }

    // ===== PLAYLIST ACTIONS =====
    private void addPlaylist() {
        String name = JOptionPane.showInputDialog(this, "Enter Playlist Name:");
        if (name == null || name.isEmpty()) return;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO playlist(User_ID, Playlist_Name, Created_Date) VALUES (?, ?, CURDATE())")) {
            ps.setInt(1, user.getId());
            ps.setString(2, name);
            ps.executeUpdate();
            loadPlaylists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editPlaylist() {
        if (selectedPlaylist == null) return;
        String newName = JOptionPane.showInputDialog(this, "Edit Playlist Name:", selectedPlaylist.name);
        if (newName == null || newName.isEmpty()) return;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE playlist SET Playlist_Name=? WHERE Playlist_ID=?")) {
            ps.setString(1, newName);
            ps.setInt(2, selectedPlaylist.id);
            ps.executeUpdate();
            loadPlaylists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePlaylist() {
        if (selectedPlaylist == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this playlist?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM playlist WHERE Playlist_ID=?")) {
            ps.setInt(1, selectedPlaylist.id);
            ps.executeUpdate();
            loadPlaylists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== SONG ACTIONS =====
    private void addSongToPlaylist() {
        if (selectedPlaylist == null) return;

        try (Connection c = DBConnection.getConnection()) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, title, artist, genre, image_path, audio_path FROM songs");
            List<SongItem> allSongs = new ArrayList<>();
            while (rs.next()) {
                allSongs.add(new SongItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getString("image_path"),
                        rs.getString("audio_path")
                ));
            }

            SongItem[] songsArray = allSongs.toArray(new SongItem[0]);
            SongItem selectedSong = (SongItem) JOptionPane.showInputDialog(
                    this, "Select a Song", "Add Song to Playlist",
                    JOptionPane.PLAIN_MESSAGE, null, songsArray, null);

            if (selectedSong == null) return;

            PreparedStatement ps = c.prepareStatement("INSERT INTO playlist_songs(Playlist_ID, Song_ID) VALUES (?, ?)");
            ps.setInt(1, selectedPlaylist.id);
            ps.setInt(2, selectedSong.id);
            ps.executeUpdate();

            loadSongs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSongFromPlaylist(SongItem song) {
        if (selectedPlaylist == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove this song from playlist?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM playlist_songs WHERE Playlist_ID=? AND Song_ID=?")) {
            ps.setInt(1, selectedPlaylist.id);
            ps.setInt(2, song.id);
            ps.executeUpdate();
            loadSongs();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting song: " + e.getMessage());
        }
    }

    // ===== ITEM CLASSES =====
    private static class PlaylistItem {
        int id;
        String name;

        public PlaylistItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class SongItem {
        int id;
        String title;
        String artist;
        String genre;
        String albumArt;
        String audioPath;

        public SongItem(int id, String title, String artist, String genre, String albumArt, String audioPath) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.genre = genre;
            this.albumArt = albumArt;
            this.audioPath = audioPath;
        }

        @Override
        public String toString() {
            return title + " - " + artist;
        }
    }
}
