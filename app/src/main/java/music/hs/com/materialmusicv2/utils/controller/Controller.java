package music.hs.com.materialmusicv2.utils.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.media.session.PlaybackStateCompat;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;

import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.objects.events.FavoriteChanged;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ChangeRepeat;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ChangeShuffle;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ClearQueue;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ListChanged;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ListModified;
import music.hs.com.materialmusicv2.objects.events.controllerevents.MoveQueueItem;
import music.hs.com.materialmusicv2.objects.events.controllerevents.Play;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlayAtPosition;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlayNext;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlayPause;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlayPrevious;
import music.hs.com.materialmusicv2.objects.events.controllerevents.RemoveFromQueue;
import music.hs.com.materialmusicv2.objects.events.controllerevents.SeekTo;
import music.hs.com.materialmusicv2.objects.events.controllerevents.Timer;

import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.addToPlaylist;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.getPlaylistID;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.isInPlaylist;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.removeFromPlaylist;

public class Controller {
    public static void pauseOrResumePlayer()
    {
        EventBus.getDefault().post(new PlayPause());
    }

    public static void changeShuffle() {
        EventBus.getDefault().post(new ChangeShuffle());
    }

    public static void changeRepeat() {
        EventBus.getDefault().post(new ChangeRepeat());
    }

    public static void playNext() {
        EventBus.getDefault().post(new PlayNext());
    }

    public static void playPrevious() {
        EventBus.getDefault().post(new PlayPrevious());
    }

    public static void seekTo(int position) {
        EventBus.getDefault().post(new SeekTo(position));
    }

    public static void setTimer(long time) {
        EventBus.getDefault().post(new Timer(time));
    }

    public static void playList(ArrayList<Song> songs, int position)
    {
        HSMusicApplication.getInstance().getPlayingQueueManager().setSongs(songs);
        HSMusicApplication.getInstance().getPlayingQueueManager().setSongPosition(position);
        HSMusicApplication.getInstance().getPlayingQueueManager().setShuffle(false);
        HSMusicApplication.getInstance().getMediaSessionUtils().setShuffle(PlaybackStateCompat.SHUFFLE_MODE_NONE);
        EventBus.getDefault().post(new ListChanged());
        EventBus.getDefault().post(new Play());

    }

    public static void shuffleList(ArrayList<Song> songs) {
        if (songs == null) {
            return;
        }
        if (!HSMusicApplication.getInstance().getPlayingQueueManager().getShuffle()) {
            HSMusicApplication.getInstance().getPlayingQueueManager().setShuffle(true);
            HSMusicApplication.getInstance().getMediaSessionUtils().setShuffle(PlaybackStateCompat.SHUFFLE_MODE_ALL);
        }
        final ArrayList<Song> songsToBePlayed = new ArrayList<>(songs);
        final ArrayList<Song> songsWithoutShuffle = new ArrayList<>(songs);
        if (songsToBePlayed.size() > 2) {
            Collections.shuffle(songsToBePlayed);
        }
        HSMusicApplication.getInstance().getPlayingQueueManager().setSongs(songsToBePlayed);
        HSMusicApplication.getInstance().getPlayingQueueManager().setSongsWithoutShuffle(songsWithoutShuffle);
        HSMusicApplication.getInstance().getPlayingQueueManager().setSongPosition(0);
        EventBus.getDefault().post(new ListChanged());
        EventBus.getDefault().post(new Play());
    }

    public static void playSingle(Song song) {
        ArrayList<Song> songs = new ArrayList<>();
        songs.add(song);
        playList(songs, 0);
    }

    public static void playAtSongPosition(int position) {
        EventBus.getDefault().post(new PlayAtPosition(position));
    }

    public static void addListToQueue(ArrayList<Song> songs) {
        if (songs == null) {
            return;
        }
        HSMusicApplication.getInstance().getPlayingQueueManager().addToQueue(songs);
        EventBus.getDefault().post(new ListModified());
        postToast(String.format(HSMusicApplication.getInstance().getApplicationContext().getString(R.string.add_to_queue_toast), songs.size()), HSMusicApplication.getInstance().getApplicationContext(), TOAST_SUCCESS);
    }

    public static void addSongToQueue(Song song) {
        if (song == null) {
            return;
        }
        HSMusicApplication.getInstance().getPlayingQueueManager().addToQueue(song);
        EventBus.getDefault().post(new ListModified());
        postToast(String.format(HSMusicApplication.getInstance().getApplicationContext().getString(R.string.add_to_queue_toast), 1), HSMusicApplication.getInstance().getApplicationContext(), TOAST_SUCCESS);
    }

    public static void addSongToNextPosition(Song song) {
        if (song == null) {
            return;
        }
        HSMusicApplication.getInstance().getPlayingQueueManager().addToNextPosition(song);
        postToast(String.format(HSMusicApplication.getInstance().getApplicationContext().getString(R.string.playing_next), song.getName()), HSMusicApplication.getInstance().getApplicationContext(), TOAST_SUCCESS);
        EventBus.getDefault().post(new ListModified());
    }

    public static void removeSongFromQueue(int position) {
        EventBus.getDefault().post(new RemoveFromQueue(position));
    }

    public static void moveQueueItem(int from, int to) {
        EventBus.getDefault().post(new MoveQueueItem(from, to));
    }

    public static boolean hasPlaybackStartedEvenOnce() {
        return HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue() > 0;
    }

    public static Song getCurrentSong() {
        return HSMusicApplication.getInstance().getPlayingQueueManager().getCurrentSong();
    }

    public static boolean isCurrentSongFavorite(Context context) {
        if (context == null) {
            return false;
        }
        Song song = getCurrentSong();
        if (song != null) {
            long playlistID = getPlaylistID("Favorites", context);
            return isInPlaylist((int) playlistID, song.getPath(), context);
        }
        return false;
    }

    public static void addCurrentSongToFavorite(Context context) {
        if (context == null) {
            return;
        }
        Song song = getCurrentSong();
        if (song != null) {
            long playlistID = getPlaylistID("Favorites", context);
            AsyncTask.execute(() -> addToPlaylist(context.getContentResolver(), (int) song.getId(), playlistID));
        }
        EventBus.getDefault().post(new FavoriteChanged(true));
    }

    public static void removeCurrentSongFromFavorite(Context context) {
        if (context == null) {
            return;
        }
        Song song = getCurrentSong();
        if (song != null) {
            long playlistID = getPlaylistID("Favorites", context);
            removeFromPlaylist(context.getContentResolver(), (int) song.getId(), playlistID);
        }
        EventBus.getDefault().post(new FavoriteChanged(false));
    }

    public static int getSongPosition() {
        return HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition();
    }

    public static void clearQueue(int position) {
        try {
            ArrayList<Song> songs = HSMusicApplication.getInstance().getPlayingQueueManager().getSongs();
            songs = new ArrayList<>(songs.subList(0, position + 1));
            HSMusicApplication.getInstance().getPlayingQueueManager().setSongs(songs);
            EventBus.getDefault().post(new ClearQueue(position));
        } catch (Exception ignored) {
        }
    }
}
