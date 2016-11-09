package com.example.android.AudioArchive.model;

import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;

/**
 * Created by HemantSingh on 09/11/16.
 */

public class Channel {
    public ArrayList<MediaMetadataCompat> tracks;

    public ArrayList<MediaMetadataCompat> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<MediaMetadataCompat> tracks) {
        this.tracks = tracks;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String channelName;

    public Channel() {
    }

    public Channel(ArrayList<MediaMetadataCompat> tracks, String channelName) {
        this.tracks = tracks;
        this.channelName = channelName;
    }

}
