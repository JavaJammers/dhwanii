package com.javajammers.dhwanii;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.Collections;

public class PlayerController {

    @FXML
    private BorderPane root;

    @FXML
    private HBox topBox;

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
    private Button shuffleButton;

    @FXML
    private MediaView mediaView;

    @FXML
    private Slider progressBar;

    @FXML
    private Slider volumeSlider;

    @FXML
    private ListView<File> playlistView;

    @FXML
    private Label currentTimeLabel;

    @FXML
    private Label totalTimeLabel;

    @FXML
    private VBox metadataBox;

    @FXML
    private ImageView albumCover;

    @FXML
    private Label artistLabel;

    @FXML
    private Label albumLabel;

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

        playlistView.setCellFactory(param -> new ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

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

            }
        });

        playlistView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                File selectedFile = playlistView.getSelectionModel().getSelectedItem();
                if (selectedFile != null) {
                    playMedia(selectedFile);
                }
            }
        });

        playlistView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                File selectedFile = playlistView.getSelectionModel().getSelectedItem();
                if (selectedFile != null) {
                    playlist.remove(selectedFile);
                }
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

    @FXML
    private void handleShuffle() {
        Collections.shuffle(playlist);
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

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            progressBar.setValue(newTime.toSeconds());
            currentTimeLabel.setText(formatTime(newTime));
        });

        mediaPlayer.setOnReady(() -> {
            progressBar.setMax(mediaPlayer.getTotalDuration().toSeconds());
            totalTimeLabel.setText(formatTime(mediaPlayer.getTotalDuration()));

            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".mp3") || fileName.endsWith(".m4a") || fileName.endsWith(".flac")) {
                // Handle audio metadata
                if (media.getMetadata().containsKey("artist")) {
                    artistLabel.setText("Artist: " + media.getMetadata().get("artist").toString());
                } else {
                    artistLabel.setText("");
                }

                if (media.getMetadata().containsKey("album")) {
                    albumLabel.setText("Album: " + media.getMetadata().get("album").toString());
                } else {
                    albumLabel.setText("");
                }

                if (media.getMetadata().containsKey("image")) {
                    albumCover.setImage((Image) media.getMetadata().get("image"));
                } else {
                    albumCover.setImage(null);
                }

                metadataBox.setVisible(true);
                root.setCenter(metadataBox); // Display metadata box in center
            } else if (fileName.endsWith(".mp4") || fileName.endsWith(".m4v") || fileName.endsWith(".mkv")) {
                // Handle video file
                metadataBox.setVisible(false);
                artistLabel.setText("");
                albumLabel.setText("");
                albumCover.setImage(null);

                if (root.getCenter() != null) {
                    root.setCenter(null); // Remove any existing content from center
                }
                mediaView = new MediaView(mediaPlayer);
                mediaView.setPreserveRatio(true);
                mediaView.fitWidthProperty().bind(root.widthProperty().subtract(20)); // Slightly smaller width
                mediaView.fitHeightProperty().bind(root.heightProperty().subtract(controlBar.getHeight() + 40)); // Adjusted height to include padding for control bar

                BorderPane.setMargin(mediaView, new Insets(10)); // Add padding around MediaView

                root.setCenter(mediaView); // Add MediaView to the center
            }
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            mediaPlayer.setVolume(newVal.doubleValue());
        });

        mediaPlayer.play();
    }

    private String formatTime(Duration time) {
        int minutes = (int) time.toMinutes();
        int seconds = (int) time.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
