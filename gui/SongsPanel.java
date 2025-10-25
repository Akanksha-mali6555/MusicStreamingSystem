package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import model.Song;
import java.io.File;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SongsPanel extends JPanel {

    private MediaPlayer mediaPlayer; // Current playing media

    public SongsPanel(List<Song> songs) {
        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));

        // Initialize JavaFX runtime
        new JFXPanel();

        // Header label
        JLabel titleLabel = new JLabel("\uD83C\uDFB5 Songs"); // 🎵 Songs
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Grid panel for songs
        JPanel gridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        gridPanel.setBackground(new Color(18, 18, 18));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add song cards
        for (Song s : songs) {
            JPanel card = createSongCard(s);
            gridPanel.add(card);
        }

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createSongCard(Song song) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 30, 30));
        card.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Song image
        try {
            ImageIcon icon = new ImageIcon(song.getImagePath());
            Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(img));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            card.add(imageLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Song info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(30, 30, 30));

        JLabel title = new JLabel(song.getTitle());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel artist = new JLabel(song.getArtist());
        artist.setForeground(Color.LIGHT_GRAY);
        artist.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        artist.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton stopButton = new JButton("\u23F9 Stop"); // ⏹ Stop
        styleButton(stopButton, new Color(50, 50, 50));
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.addActionListener(e -> {
            if (mediaPlayer != null) mediaPlayer.stop();
        });

        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(title);
        infoPanel.add(artist);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(stopButton);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        card.add(infoPanel, BorderLayout.SOUTH);

        // Click card to play song
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                playSong(song);
            }
        });

        return card;
    }

    private void playSong(Song song) {
        if (mediaPlayer != null) mediaPlayer.stop();
        try {
            File audioFile = new File(song.getAudioPath());
            if (!audioFile.exists()) return;
            Media media = new Media(audioFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    

}
