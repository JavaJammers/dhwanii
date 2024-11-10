package com.javajammers.dhwanii;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

import java.io.File;

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
    private Button nextButton;

    @FXML
    private Button previousButton;

    @FXML
    private MediaView mediaView;

    @FXML
    private Slider progressBar;

    @FXML
    private Slider volumeSlider;

    @FXML
    private ListView<File> playlistView;

    private MediaPlayer mediaPlayer;
    private ObservableList<File> playlist;

    private final FileChooser fileChooser;

    public PlayerController() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video and Audio Files", "*.mp4", "*.m4v", "*.mkv", "*.mp3"));
        playlist = FXCollections.observableArrayList();
    }

    public void initialize() {
        root.setOnDragOver(this::handleDragOver);
        root.setOnDragDropped(this::handleDragDropped);
        playlistView.setItems(playlist);

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

        playlistView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile != null) {
                playMedia(newFile);
            }
        });
    }

    @FXML
    private void handleSelectFile() {
        File file = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        if (file != null) {
            addFileToPlaylist(file);
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

    @FXML
    private void handleNext() {
        int currentIndex = playlistView.getSelectionModel().getSelectedIndex();
        if (currentIndex < playlist.size() - 1) {
            playlistView.getSelectionModel().select(currentIndex + 1);
        }
    }

    @FXML
    private void handlePrevious() {
        int currentIndex = playlistView.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            playlistView.getSelectionModel().select(currentIndex - 1);
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
            for (File file : db.getFiles()) {
                if (isValidMediaFile(file)) {
                    addFileToPlaylist(file);
                    success = true;
                }
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private boolean isValidMediaFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".mp4") || fileName.endsWith(".m4v") || fileName.endsWith(".mkv") || fileName.endsWith(".mp3");
    }

    private void addFileToPlaylist(File file) {
        playlist.add(file);
        if (mediaPlayer == null) {
            playMedia(file);
        }
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
