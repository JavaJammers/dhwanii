package com.javajammers.dhwanii;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private Pane rightPane;

    @FXML
    private ListView<String> playlistView;

    // ArrayList to store the dropped files
    private final ArrayList<File> filePlaylist = new ArrayList<>();

    public void initialize() {
        setupDragAndDrop();
    }
    private void setupDragAndDrop() {
        // Set up drag-over event
        rightPane.setOnDragOver(event -> {
            if (event.getGestureSource() != rightPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        // Set up drag-drop event
        rightPane.setOnDragDropped(this::handleDragDropped1);
    }

    private void handleDragDropped1(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> droppedFiles = db.getFiles();

            for (File file : droppedFiles) {
                // If it's a directory, get all files inside it
                if (file.isDirectory()) {
                    addFilesFromDirectory(file);
                } else {
                    filePlaylist.add(file); // Add the file to the playlist
                }
            }

            // Update the ListView with file names
            updatePlaylistView();

            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    // Recursively add files from a directory
    private void addFilesFromDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addFilesFromDirectory(file); // Recursively add files from subdirectories
                } else {
                    filePlaylist.add(file); // Add file to playlist
                }
            }
        }
    }

    // Method to update ListView with the names of files in filePlaylist
    private void updatePlaylistView() {
        ObservableList<String> fileNames = FXCollections.observableArrayList();
        for (File file : filePlaylist) {
            fileNames.add(file.getName());
        }
        playlistView.setItems(fileNames); // Set the ListView with file names
    }


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
            loadMedia(fileUri);
        }
    }

    private void loadMedia(String fileUri) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        media = new Media(fileUri);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        Scene scene = mediaView.getScene();
        mediaView.fitHeightProperty().bind(scene.heightProperty());
        mediaView.fitWidthProperty().bind(scene.widthProperty());

        mediaPlayer.setOnEndOfMedia(() -> {
            play.setText("Play");
            isPlaying = false;
            label.setText("Stopped");
        });

        mediaPlayer.setOnError(() -> {
            label.setText("Error: " + mediaPlayer.getError().getMessage());
        });

        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            isPlaying = true;
            play.setText("Pause");
            label.setText("Playing");
        });
    }

    @FXML
    public void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != mediaView && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
        event.consume();
    }

    @FXML
    public void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            File file = db.getFiles().get(0); // Get the first file
            String fileUri = file.toURI().toString();
            loadMedia(fileUri);
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }
}