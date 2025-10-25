package gui;

import javazoom.jl.player.advanced.AdvancedPlayer;
import java.io.FileInputStream;

public class AudioPlayer {
    private static AudioPlayer instance;

    private AdvancedPlayer player;
    private Thread playThread;
    private String currentFile;
    private int pausedOnFrame;

    private AudioPlayer() {}

    public static AudioPlayer getInstance() {
        if (instance == null) {
            instance = new AudioPlayer();
        }
        return instance;
    }

    // Play song from start
    public void playSong(String filePath) {
        stop(); // stop any previous playback
        currentFile = filePath;
        pausedOnFrame = 0;
        startPlayerFrom(pausedOnFrame);
    }

    // Resume song from paused position
    public void resume() {
        if (currentFile != null && pausedOnFrame > 0) {
            startPlayerFrom(pausedOnFrame);
        }
    }

    private void startPlayerFrom(int startFrame) {
        playThread = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(currentFile)) {
                player = new AdvancedPlayer(fis);
                player.setPlayBackListener(new javazoom.jl.player.advanced.PlaybackListener() {
                    @Override
                    public void playbackFinished(javazoom.jl.player.advanced.PlaybackEvent evt) {
                        pausedOnFrame = evt.getFrame();
                    }
                });
                player.play(startFrame, Integer.MAX_VALUE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        playThread.start();
    }

    public void pause() {
        if (player != null) {
            player.close();
        }
    }

    public void stop() {
        pause();
        pausedOnFrame = 0;
        currentFile = null;
    }
}
