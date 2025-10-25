package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.DBConnection;
import model.User;

public class PlaylistSongsDialog extends JDialog {
    private String playlistName;
    private User user;
    private AudioPlayer audioPlayer;
    private DefaultListModel<String> songModel;
    private JList<String> songList;

    public PlaylistSongsDialog(JFrame parent, String playlistName, User user, AudioPlayer audioPlayer) {
        super(parent, "🎵 " + playlistName, true);
        this.playlistName = playlistName;
        this.user = user;
        this.audioPlayer = audioPlayer;

        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(18, 18, 18));

        JLabel header = new JLabel("🎵 Songs in " + playlistName);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(header, BorderLayout.NORTH);

        songModel = new DefaultListModel<>();
        songList = new JList<>(songModel);
        songList.setBackground(new Color(28, 28, 28));
        songList.setForeground(Color.WHITE);
        songList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        songList.setSelectionBackground(new Color(30, 215, 96));
        add(new JScrollPane(songList), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setBackground(new Color(18, 18, 18));

        JButton addBtn = new JButton("➕ Add Song");
        JButton removeBtn = new JButton("🗑 Remove");
        for (JButton b : new JButton[]{addBtn, removeBtn}) {
            b.setBackground(new Color(30, 215, 96));
            b.setForeground(Color.BLACK);
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            buttons.add(b);
        }
        add(buttons, BorderLayout.SOUTH);

        loadSongs();

        // Double-click to play
        songList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String songName = songList.getSelectedValue();
                    if (songName != null) playSong(songName);
                }
            }
        });

        addBtn.addActionListener(e -> addSong());
        removeBtn.addActionListener(e -> removeSong());

        setVisible(true);
    }

    /** Load all songs in this playlist */
    private void loadSongs() {
        songModel.clear();
        String query = "SELECT s.title " +
                       "FROM playlist_songs ps " +
                       "JOIN songs s ON ps.Song_ID = s.id " +
                       "JOIN playlist p ON ps.Playlist_ID = p.Playlist_ID " +
                       "WHERE p.Playlist_Name=? AND p.User_ID=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, playlistName);
            ps.setInt(2, user.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) songModel.addElement(rs.getString("title"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Add a song to playlist */
    private void addSong() {
        String songName = JOptionPane.showInputDialog(this, "Enter Song Title to Add:");
        if (songName == null || songName.trim().isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {
            // Get Song ID
            PreparedStatement getSong = con.prepareStatement("SELECT id FROM songs WHERE title=?");
            getSong.setString(1, songName);
            ResultSet rs = getSong.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Song not found!");
                return;
            }
            int songId = rs.getInt("id");

            // Get Playlist ID
            PreparedStatement getPlaylist = con.prepareStatement("SELECT Playlist_ID FROM playlist WHERE Playlist_Name=? AND User_ID=?");
            getPlaylist.setString(1, playlistName);
            getPlaylist.setInt(2, user.getId());
            ResultSet rs2 = getPlaylist.executeQuery();

            if (rs2.next()) {
                int playlistId = rs2.getInt("Playlist_ID");
                PreparedStatement add = con.prepareStatement("INSERT INTO playlist_songs (Playlist_ID, Song_ID) VALUES (?, ?)");
                add.setInt(1, playlistId);
                add.setInt(2, songId);
                add.executeUpdate();
                loadSongs();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Remove selected song from playlist */
    private void removeSong() {
        String selected = songList.getSelectedValue();
        if (selected == null) return;

        String query = "DELETE ps FROM playlist_songs ps " +
                       "JOIN songs s ON ps.Song_ID = s.id " +
                       "JOIN playlist p ON ps.Playlist_ID = p.Playlist_ID " +
                       "WHERE s.title=? AND p.Playlist_Name=? AND p.User_ID=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, selected);
            ps.setString(2, playlistName);
            ps.setInt(3, user.getId());
            ps.executeUpdate();
            loadSongs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Play selected song */
    private void playSong(String songName) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT audio_path FROM songs WHERE title=?")) {
            ps.setString(1, songName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String path = rs.getString("audio_path");
                audioPlayer.playSong(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
