package music.hs.com.materialmusicv2.objects.events;

import java.util.ArrayList;

import music.hs.com.materialmusicv2.objects.Song;

public class SongListChanged {
    public ArrayList<Song> songs;

    public SongListChanged(ArrayList<Song> songs) {
        this.songs = songs;
    }
}
