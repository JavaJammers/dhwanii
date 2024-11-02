package com.javajammers.dhwanii;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

import java.io.File;

public class PlayerController {
    @FXML
    private MediaView mediaView;
    @FXML
    private Button play, selectFile, stop;
    @FXML
    private Label label;

    private Media media;
    private MediaPlayer mediaPlayer;

    private boolean isPlaying = false;

    @FXML
    void btnPlay(ActionEvent event) {
        if (mediaPlayer != null) {
            if (!isPlaying) {
                mediaPlayer.play();
                play.setText("Pause");
                isPlaying = true;
                label.setText("Playing");
            } else {
                mediaPlayer.pause();
                play.setText("Play");
                isPlaying = false;
                label.setText("Paused");
            }
        }
    }

    @FXML
    void btnStop(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            play.setText("Play");
            isPlaying = false;
            label.setText("Stopped");
        }
    }

    @FXML
    void btnSelectMedia(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String fileUri = selectedFile.toURI().toString();
            media = new Media(fileUri);

            // Stop current player if it exists before creating a new one
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            Scene scene = mediaView.getScene();
            mediaView.fitHeightProperty().bind(scene.heightProperty());
            mediaView.fitWidthProperty().bind(scene.widthProperty());

            mediaPlayer.setOnEndOfMedia(() -> {
                play.setText("Play");
                mediaPlayer.stop();
                mediaPlayer.play();
            });

            mediaPlayer.setAutoPlay(true);
            isPlaying = true;
            play.setText("Pause");
            label.setText("Playing");
        }
    }
}
