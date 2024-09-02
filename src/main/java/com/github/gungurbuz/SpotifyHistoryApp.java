package com.github.gungurbuz;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpotifyHistoryApp extends Application {
    private static final Logger LOGGER = Logger.getLogger(SpotifyHistoryApp.class.getName());
    private TableView<Song> songTable = new TableView<>();
    private Label mostPlayedLabel = new Label();
    private Label mostTimesPlayedLabel = new Label();
    private PieChart pieChart = new PieChart();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);

        // File Chooser button
        Button openButton = new Button("Open JSON File");
        openButton.setOnAction(e -> openFile(primaryStage));

        // Setting up the TableView
        setupTableView();

        // Layouts
        ToolBar toolBar = new ToolBar(openButton);
        SplitPane splitPane = new SplitPane(songTable, pieChart);

        root.setTop(toolBar);
        root.setCenter(splitPane);
        root.setBottom(new VBox(10, mostPlayedLabel, mostTimesPlayedLabel));

        primaryStage.setTitle("Spotify History Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open JSON File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            processJsonFile(file);
        }
    }

    private void setupTableView() {
        TableColumn<Song, String> trackNameCol = new TableColumn<>("Track Name");
        trackNameCol.setCellValueFactory(data -> data.getValue().trackNameProperty());

        TableColumn<Song, String> artistCol = new TableColumn<>("Artist");
        artistCol.setCellValueFactory(data -> data.getValue().artistProperty());

        TableColumn<Song, Integer> timesPlayedCol = new TableColumn<>("Times Played");
        timesPlayedCol.setCellValueFactory(data -> data.getValue().timesPlayedProperty().asObject());

        TableColumn<Song, Long> totalPlayedForCol = new TableColumn<>("Total Played (ms)");
        totalPlayedForCol.setCellValueFactory(data -> data.getValue().totalPlayedForProperty().asObject());

        songTable.getColumns().addAll(trackNameCol, artistCol, timesPlayedCol, totalPlayedForCol);
    }

    private void processJsonFile(File file) {
        JSONParser jsonParser = new JSONParser();
        Map<String, Song> songMap = new HashMap<>();
        Song mostPlayed = null, mostTimesPlayed = null;
        long maxLong = 0;
        int maxPlays = 0;

        try (FileReader reader = new FileReader(file)) {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;
                processSongData(songMap, jsonObject);
            }

            songTable.getItems().clear();
            songTable.getItems().addAll(songMap.values());

            pieChart.getData().clear();
            long totalListeningTime = songMap.values().stream().mapToLong(Song::getTotalPlayedFor).sum();
            for (Song song : songMap.values()) {
                pieChart.getData().add(new PieChart.Data(song.getTrackName(), (double) song.getTotalPlayedFor() / totalListeningTime));
            }

            for (Song song : songMap.values()) {
                if (song.getTotalPlayedFor() > maxLong) {
                    maxLong = song.getTotalPlayedFor();
                    mostPlayed = song;
                }
                if (song.getTimesPlayed() > maxPlays) {
                    maxPlays = song.getTimesPlayed();
                    mostTimesPlayed = song;
                }
            }

            updateHighlights(mostPlayed, mostTimesPlayed, maxPlays);
        } catch (IOException | ParseException e) {
            LOGGER.log(Level.SEVERE, "Failed to process JSON file", e);
        }
    }

    private void processSongData(Map<String, Song> songMap, JSONObject jsonObject) {
        String trackName = (String) jsonObject.get("master_metadata_track_name");
        if (trackName == null) return;
        String album = (String) jsonObject.get("master_metadata_album_album_name");
        String artist = (String) jsonObject.get("master_metadata_album_artist_name");
        long msPlayed = (long) jsonObject.get("ms_played");

        songMap.computeIfAbsent(trackName, k -> new Song(trackName, album, artist, msPlayed)).updateSong(msPlayed);
    }

    private void updateHighlights(Song mostPlayed, Song mostTimesPlayed, int maxPlays) {
        if (mostPlayed != null && mostTimesPlayed != null) {
            mostPlayedLabel.setText(String.format("Most listened to song by time: %s by %s (%.2f hr)",
                    mostPlayed.getTrackName(), mostPlayed.getArtist(), mostPlayed.getTotalPlayedFor() / 3_600_000.0));
            mostTimesPlayedLabel.setText(String.format("Most listened to song by play count: %s by %s (%d plays)",
                    mostTimesPlayed.getTrackName(), mostTimesPlayed.getArtist(), maxPlays));
        } else {
            mostPlayedLabel.setText("No song data found.");
            mostTimesPlayedLabel.setText("");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
