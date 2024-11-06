package com.javajammers.dhwanii;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerController {

    @FXML
    private BorderPane root;

    @FXML
    private VBox controlBar;

    @FXML
    private Button selectFileButton;

    @FXML
    private Button playPauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private MediaView mediaView;

    @FXML
    private Slider progressBar;

    @FXML
    private Slider volumeSlider;

    private MediaPlayer mediaPlayer;

    private final FileChooser fileChooser;

    public PlayerController() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.m4v", "*.mkv", "*.mp3"));
    }

    public void initialize() {
        root.setOnDragOver(this::handleDragOver);
        root.setOnDragDropped(this::handleDragDropped);

        progressBar.setOnMousePressed(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(progressBar.getValue() / progressBar.getMax()));
            }
        });

        progressBar.setOnMouseDragged(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(progressBar.getValue() / progressBar.getMax()));
            }
        });
    }

    @FXML
    private void handleSelectFile() {
        File file = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        if (file != null) {
            playMedia(file);
        }
    }

    @FXML
    private void handlePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        }
    }

    @FXML
    private void handleStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != root && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            playMedia(file);
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void playMedia(File file) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.fitWidthProperty().bind(root.widthProperty());
        mediaView.fitHeightProperty().bind(root.heightProperty().subtract(100));

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            progressBar.setValue(newTime.toSeconds());
        });

        mediaPlayer.setOnReady(() -> {
            progressBar.setMax(mediaPlayer.getTotalDuration().toSeconds());
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            mediaPlayer.setVolume(newVal.doubleValue());
        });

        mediaPlayer.play();
    }
}