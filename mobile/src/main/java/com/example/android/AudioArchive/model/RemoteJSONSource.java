/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.AudioArchive.model;

import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirPutCallback;
import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.RSSFeed;
import com.einmalfel.earl.RSSItem;
import com.example.android.AudioArchive.utils.LogHelper;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to get a list of MusicTrack's based on a server-side JSON
 * configuration.
 */
public class RemoteJSONSource implements MusicProviderSource {

    private static final String TAG = LogHelper.makeLogTag(RemoteJSONSource.class);

    protected static final String CATALOG_URL =
        "http://storage.googleapis.com/automotive-media/music.json";



    private static final String JSON_MUSIC = "music";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ALBUM = "album";
    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_IMAGE = "image";
    private static final String JSON_TRACK_NUMBER = "trackNumber";
    private static final String JSON_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String JSON_DURATION = "duration";

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
//        try {
//            int slashPos = CATALOG_URL.lastIndexOf('/');
//            String path = CATALOG_URL.substring(0, slashPos + 1);
        ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();
         ArrayList<RSSItem> RssItems;
        RssItems = fetchJSONFromUrl("https://archive.org/services/collection-rss.php?collection=librivoxaudio");
        if(RssItems.size() > 0) {
            for (RSSItem Mediaitem : RssItems) {
                tracks.add(buildFromRSS(Mediaitem,"Librivox"));
            }
        }
        RssItems = fetchJSONFromUrl("https://archive.org/services/collection-rss.php?collection=stream_only");
        if(RssItems.size() > 0) {
            for (RSSItem Mediaitem : RssItems) {
                tracks.add(buildFromRSS(Mediaitem,"Random"));
            }
        }

return tracks.iterator();
    }

    private MediaMetadataCompat buildFromJSON(JSONObject json, String basePath) throws JSONException {
        String title = json.getString(JSON_TITLE);
        String album = json.getString(JSON_ALBUM);
        String artist = json.getString(JSON_ARTIST);
        String genre = json.getString(JSON_GENRE);
        String source = json.getString(JSON_SOURCE);
        String iconUrl = json.getString(JSON_IMAGE);
        int trackNumber = json.getInt(JSON_TRACK_NUMBER);
        int totalTrackCount = json.getInt(JSON_TOTAL_TRACK_COUNT);
        int duration = json.getInt(JSON_DURATION) * 1000; // ms

        LogHelper.d(TAG, "Found music track: ", json);

        // Media is stored relative to JSON file
        if (!source.startsWith("http")) {
            source = basePath + source;
        }
        if (!iconUrl.startsWith("http")) {
            iconUrl = basePath + iconUrl;
        }
        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = String.valueOf(source.hashCode());

        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        //noinspection ResourceType
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }

    private MediaMetadataCompat buildFromRSS(RSSItem rssItem,  String genre) {
        String title = rssItem.getTitle();
        String album = rssItem.categories.get(0).toString();
        String artist = "";
        if (rssItem.getDescription().contains("by")){
            StringBuffer stringBuffer = new StringBuffer(rssItem.getDescription());
            artist = stringBuffer.substring(stringBuffer.indexOf("by") + 2,stringBuffer.indexOf(".",stringBuffer.indexOf("by")));
        }

        String source = rssItem.getEnclosures().get(0).getLink();
        String iconUrl = "https://archive.org/services/get-item-image.php?identifier=afewmoreverses_1606_librivox&mediatype=audio&collection=librivoxaudio";

        String html = rssItem.getDescription();
        String imgRegex = "<[iI][mM][gG][^>]+[sS][rR][cC]\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";

        Pattern p = Pattern.compile(imgRegex);
        Matcher m = p.matcher(html);

        if (m.find()) {
            iconUrl = m.group(1);
        }
        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = String.valueOf(source.hashCode());




        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        //noinspection ResourceType
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                // .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, title)
                //  .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .build();

    }
    /**
     * Download a JSON file from a server, parse the content and return the JSON
     * object.
     *
     * @return result JSONObject containing the parsed representation.
     */
    private ArrayList<RSSItem> fetchJSONFromUrl(String urlString) {
        ArrayList<RSSItem> RssItems = new ArrayList<>();

                try {
                    InputStream inputStream = new URL(urlString).openConnection().getInputStream();
                    Feed feed = EarlParser.parseOrThrow(inputStream, 0);
                    // media and itunes RSS extensions allow to assign keywords to feed items
                    if (RSSFeed.class.isInstance(feed)) {
                        RSSFeed rssFeed = (RSSFeed) feed;
                        for (RSSItem Mediaitem : rssFeed.items) {
                            if (Mediaitem.media != null) {
                                RssItems.add(Mediaitem);
                            }
                        }
                        Reservoir.putAsync(urlString, RssItems, new ReservoirPutCallback() {
                            @Override
                            public void onSuccess() {
                                //success
                            }

                            @Override
                            public void onFailure(Exception e) {
                                //error
                            }
                        });
                    }

                } catch (Exception e) {
                    Log.v("Error Parsing Data", e + "");
                }


            return RssItems;
        }



}
