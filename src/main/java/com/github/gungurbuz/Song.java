package com.github.gungurbuz;

import javafx.beans.property.*;

public class Song {
    private final StringProperty trackName;
    private final StringProperty album;
    private final StringProperty artist;
    private final IntegerProperty timesPlayed;
    private final LongProperty totalPlayedFor;

    public Song(String trackName, String album, String artist, long playedFor) {
        this.trackName = new SimpleStringProperty(trackName);
        this.album = new SimpleStringProperty(album);
        this.artist = new SimpleStringProperty(artist);
        this.timesPlayed = new SimpleIntegerProperty(1);
        this.totalPlayedFor = new SimpleLongProperty(playedFor);
    }

    public String getAlbum() {
        return album.get();
    }

    public StringProperty albumProperty() {
        return album;
    }

    public String getTrackName() {
        return trackName.get();
    }

    public StringProperty trackNameProperty() {
        return trackName;
    }

    public long getTotalPlayedFor() {
        return totalPlayedFor.get();
    }

    public LongProperty totalPlayedForProperty() {
        return totalPlayedFor;
    }

    public String getArtist() {
        return artist.get();
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public int getTimesPlayed() {
        return timesPlayed.get();
    }

    public IntegerProperty timesPlayedProperty() {
        return timesPlayed;
    }

    public void updateSong(long playedFor) {
        this.timesPlayed.set(getTimesPlayed() + 1);
        this.totalPlayedFor.set(getTotalPlayedFor() + playedFor);
    }
}
