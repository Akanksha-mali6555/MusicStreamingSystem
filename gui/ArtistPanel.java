package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.sql.*;
import javax.imageio.ImageIO;

import db.DBConnection;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class ArtistPanel extends JPanel {

    private MediaPlayer mediaPlayer;
    private JPanel artistGrid;
    private JScrollPane scrollPane;
    private JLabel header;
    private String emojiFont;

    public ArtistPanel() {
        new JFXPanel(); // Initialize JavaFX runtime

        // Detect OS for emoji font
        String os = System.getProperty("os.name").toLowerCase();
        emojiFont = "Segoe UI Emoji"; // Windows default
        if(os.contains("mac")) emojiFont = "Apple Color Emoji";
        if(os.contains("nux")) emojiFont = "Noto Color Emoji";

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));

        // Header
        header = new JLabel("🎤 Artists");
        header.setForeground(Color.WHITE);
        header.setFont(new Font(emojiFont, Font.BOLD, 28));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 0));
        add(header, BorderLayout.NORTH);

        // Artist grid
        artistGrid = new JPanel(new GridLayout(0, 4, 20, 20));
        artistGrid.setBackground(new Color(18, 18, 18));
        artistGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        scrollPane = new JScrollPane(artistGrid);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // Load artists from DB
        loadArtistsFromDB();
    }

    /** Load artists from the database **/
    private void loadArtistsFromDB() {
        artistGrid.removeAll();

        String query = "SELECT Artist_ID, Name, Country, Image_Path FROM artist";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("Artist_ID");
                String name = rs.getString("Name");
                String country = rs.getString("Country");
                String imagePath = rs.getString("Image_Path"); // DB path

                JPanel card = createArtistCard(id, name, country, imagePath);
                artistGrid.add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        artistGrid.revalidate();
        artistGrid.repaint();
    }

    /** Create artist card **/
    private JPanel createArtistCard(int id, String name, String country, String imagePath) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 240));
        card.setBackground(new Color(28, 28, 28));
        card.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2, true));

        // Load image
        ImageIcon icon = loadImage(imagePath, "images/artist_placeholder.jpg", 200, 200);
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Name + country
        JLabel nameLabel = new JLabel("<html><center>" + name + "<br/>" + country + "</center></html>", SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font(emojiFont, Font.BOLD, 14));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        card.add(imageLabel, BorderLayout.CENTER);
        card.add(nameLabel, BorderLayout.SOUTH);

        // Click to show songs
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showSongsByArtist(id, name);
            }
        });

        return card;
    }

    /** Show songs by artist **/
    private void showSongsByArtist(int artistId, String artistName) {
        removeAll();
        revalidate();
        repaint();

        setLayout(new BorderLayout());

        // Header with back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 25));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = new JButton("⬅ Back");
        backButton.setFont(new Font(emojiFont, Font.BOLD, 13));
        styleButton(backButton, new Color(80, 80, 80));
        backButton.addActionListener(e -> {
            removeAll();
            setLayout(new BorderLayout());
            add(header, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            loadArtistsFromDB();
            revalidate();
            repaint();
        });

        JLabel title = new JLabel(artistName + " — Songs", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font(emojiFont, Font.BOLD, 22));

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(title, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel songsPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        songsPanel.setBackground(new Color(18, 18, 18));
        songsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Load songs for this artist
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM songs WHERE artist_id = ?")) {

            ps.setInt(1, artistId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String titleStr = rs.getString("title");
                String imagePath = rs.getString("image_path");
                String audioPath = rs.getString("audio_path");

                JPanel songCard = createSongCard(titleStr, imagePath, audioPath);
                songsPanel.add(songCard);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        JScrollPane scroll = new JScrollPane(songsPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /** Create song card **/
    private JPanel createSongCard(String title, String imagePath, String audioPath) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 240));
        card.setBackground(new Color(30, 30, 30));
        card.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 2, true));

        ImageIcon icon = loadImage(imagePath, "images/Lata_Mangeshkar.jpeg", 200, 200);
        JLabel imgLabel = new JLabel(icon);
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(emojiFont, Font.PLAIN, 15));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(30, 30, 30));

        JButton playBtn = new JButton("▶ Play");
        JButton stopBtn = new JButton("⏹ Stop");
        playBtn.setFont(new Font(emojiFont, Font.BOLD, 13));
        stopBtn.setFont(new Font(emojiFont, Font.BOLD, 13));
        styleButton(playBtn, new Color(0, 150, 136));
        styleButton(stopBtn, new Color(200, 50, 50));

        playBtn.addActionListener(e -> playAudio(audioPath));
        stopBtn.addActionListener(e -> stopAudio());

        buttonPanel.add(playBtn);
        buttonPanel.add(stopBtn);

        card.add(imgLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    /** Style button **/
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(90, 32));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Load image from resource or file with fallback **/
    private ImageIcon loadImage(String path, String defaultPath, int width, int height) {
        try {
            Image img = null;

            // Try loading from resources
            if (path != null && !path.isEmpty()) {
                URL imgURL = getClass().getClassLoader().getResource(path);
                if (imgURL != null) img = ImageIO.read(imgURL);
            }

            // Absolute file path
            if (img == null && path != null && !path.isEmpty()) {
                File file = new File(path);
                if (file.exists() && file.isFile()) img = ImageIO.read(file);
            }

            // Fallback
            if (img == null) {
                URL fallbackURL = getClass().getClassLoader().getResource(defaultPath);
                if (fallbackURL != null) img = ImageIO.read(fallbackURL);
            }

            if (img != null) return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ImageIcon();
    }

    /** Audio Controls **/
    private void playAudio(String audioPath) {
        stopAudio();
        try {
            if (audioPath == null || audioPath.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No audio file found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            File audioFile = new File(audioPath);
            if (!audioFile.exists()) {
                JOptionPane.showMessageDialog(this, "Audio not found: " + audioFile.getAbsolutePath(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Media media = new Media(audioFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) mediaPlayer.stop();
    }
}
