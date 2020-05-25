package music.hs.com.materialmusicv2.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.media.audiofx.Visualizer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.objects.AudioSessionIdChanged;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.objects.events.MediaSessionData;
import music.hs.com.materialmusicv2.objects.events.MediaStates;
import music.hs.com.materialmusicv2.objects.events.PlaybackPosition;
import music.hs.com.materialmusicv2.objects.events.SongListChanged;
import music.hs.com.materialmusicv2.objects.events.SongPositionInQueue;
import music.hs.com.materialmusicv2.objects.events.controllerevents.BandLevel;
import music.hs.com.materialmusicv2.objects.events.controllerevents.BassBoostLevel;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ChangeRepeat;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ChangeShuffle;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ClearQueue;
import music.hs.com.materialmusicv2.objects.events.controllerevents.EqualizerBassboostVirtualizerPresetReverb;
import music.hs.com.materialmusicv2.objects.events.controllerevents.EquallizerEnabled;
import music.hs.com.materialmusicv2.objects.events.controllerevents.LinkVisualizer;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ListChanged;
import music.hs.com.materialmusicv2.objects.events.controllerevents.ListModified;
import music.hs.com.materialmusicv2.objects.events.controllerevents.MoveQueueItem;
import music.hs.com.materialmusicv2.objects.events.controllerevents.Play;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlayAtPosition;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlayNext;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlayPause;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlayPrevious;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PlaySongAtStart;
import music.hs.com.materialmusicv2.objects.events.controllerevents.Preset;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PresetChanged;
import music.hs.com.materialmusicv2.objects.events.controllerevents.RemoveFromQueue;
import music.hs.com.materialmusicv2.objects.events.controllerevents.Reverb;
import music.hs.com.materialmusicv2.objects.events.controllerevents.SeekTo;
import music.hs.com.materialmusicv2.objects.events.controllerevents.Timer;
import music.hs.com.materialmusicv2.objects.events.controllerevents.UnlinkVisualizer;
import music.hs.com.materialmusicv2.objects.events.controllerevents.UpdateEqualizerActivity;
import music.hs.com.materialmusicv2.objects.events.controllerevents.VirtualizerLevel;
import music.hs.com.materialmusicv2.objects.events.controllerevents.VisualizerData;
import music.hs.com.materialmusicv2.service.notificationutils.NotificationUtils;
import music.hs.com.materialmusicv2.utils.gaplessfadingmediaplayer.GaplessFadingMediaPlayer;

import static music.hs.com.materialmusicv2.utils.broadcastutils.BroadcastUtils.pauseBroadcast;
import static music.hs.com.materialmusicv2.utils.broadcastutils.BroadcastUtils.startBroadcast;
import static music.hs.com.materialmusicv2.utils.controller.Controller.addCurrentSongToFavorite;
import static music.hs.com.materialmusicv2.utils.controller.Controller.isCurrentSongFavorite;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playList;
import static music.hs.com.materialmusicv2.utils.controller.Controller.removeCurrentSongFromFavorite;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_FAVORITE;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_NEXT;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_PAUSE_OR_RESUME;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_PLAY_SONG_AT;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_PREVIOUS;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_REPEAT;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_SHUFFLE;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_SKIP2SONGS;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_SKIP3SONGS;
import static music.hs.com.materialmusicv2.utils.misc.Statics.NOTIFY_STOP;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.createPlaylist;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.getAllSongsOfFolder;

public class MusicService extends Service implements GaplessFadingMediaPlayer.StateChangeListener {

    private NotificationUtils notificationUtils;

    private int clickCounter = 0;
    private long lastClickTime = 0;

    private GaplessFadingMediaPlayer mediaPlayer;

    private boolean isTrackStopped;

    /**
     * ExecutorService
     */

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void shutExecutorServiceDown() {
        if (executorService == null) {
            return;
        }
        executorService.shutdown();
    }

    /**
     * MediaSession and AudioManager
     */

    //MediaSession
    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
            String intentAction = mediaButtonIntent.getAction();
            if (intentAction != null) {
                if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction) && mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) != null) {
                    KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                    if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK) {
                        int action = event.getAction();
                        if (action == KeyEvent.ACTION_DOWN) {
                            if (clickCounter == 0) {
                                lastClickTime = System.currentTimeMillis();
                            }
                        }
                        if (action == KeyEvent.ACTION_UP) {
                            clickCounter++;
                            long action_up = System.currentTimeMillis();
                            long timeOut = 750;
                            if (action_up - lastClickTime > timeOut) {
                                clickCounter = 0;
                            }
                            if (clickCounter == 1 || clickCounter == 2) {
                                final Handler handler = new Handler();
                                handler.postDelayed(() -> {
                                    try {
                                        if (clickCounter == 1) {
                                            if (isPlaying()) {
                                                pausePlayer();
                                            } else {
                                                resumePlayer();
                                            }
                                        } else if (clickCounter == 2) {
                                            playNext();
                                        }
                                        clickCounter = 0;
                                    } catch (Exception e) {
                                        playNext();
                                        e.printStackTrace();
                                    }
                                }, 750);
                            } else if (clickCounter == 3) {
                                setSongPosition(HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition() - 1);
                                if (HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition() < 0) {
                                    setSongPosition(HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue() - 1);
                                }
                                playSong();
                                clickCounter = 0;
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onPause() {
            pausePlayer();
        }

        @Override
        public void onPlay() {
            resumePlayer();
        }

        @Override
        public void onStop() {
            if (!isPlaying()) {
                stopPlayback();
            }
        }

        @Override
        public void onSkipToNext() {
            playNext();
        }

        @Override
        public void onSkipToPrevious() {
            playPrevious();
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
        }

        @Override
        public void onSeekTo(long pos) {
            seek((int) pos);
        }

        @Override
        public void onSkipToQueueItem(long id) {
            setSongPosition((int) id);
            playSong();
        }

    };

    private void initMediaSession() {
        HSMusicApplication.getInstance().getMediaSessionUtils().setPlaybackState(PlaybackStateCompat.STATE_STOPPED, getCurrentPlaybackPosition());
        HSMusicApplication.getInstance().getMediaSessionUtils().setCallback(mediaSessionCallback);
    }

    //AudioManager
    private AudioManager audioManager;
    private boolean isPausedByTransientLossOfFocus = false;

    private void initAudioManager() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (mediaPlayer != null) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (isPausedByTransientLossOfFocus) {
                            isPausedByTransientLossOfFocus = false;
                            if (!isPlaying()) {
                                resumePlayer();
                            }
                        }
                        setVolume(1.0f);
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        isPausedByTransientLossOfFocus = false;
                        pausePlayer();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        isPausedByTransientLossOfFocus = true;
                        pausePlayer();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        isPausedByTransientLossOfFocus = false;
                        if (mediaPlayer.isPlaying()) {
                            setVolume(0.1f);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

    /**
     * MediaPlayer
     */

    private MediaPlayer.OnCompletionListener completionListener = mp -> onCompletionOfMediaPlayer();

    private MediaPlayer.OnErrorListener errorListener = (mp, what, extra) -> true;

    /**
     * BroadcastReceiver
     */

    private MusicPlayerBroadcastReceiver musicPlayerBroadcastReceiver = new MusicPlayerBroadcastReceiver();

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackPosition playbackPosition) {
        HSMusicApplication.getInstance().getWidgetUtils().onPlaybackPositionChanged(playbackPosition);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SongPositionInQueue songPositionInQueue) {
        HSMusicApplication.getInstance().getWidgetUtils().onPositionChanged();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SongListChanged songList) {
        if (mediaPlayer != null) {
            mediaPlayer.setCurrentPosition(getSongPosition());
        }
        HSMusicApplication.getInstance().getWidgetUtils().onSongListChanged();
        saveSongs();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(MediaSessionData mediaSessionData) {
        HSMusicApplication.getInstance().getWidgetUtils().onMetadataChanged(mediaSessionData);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(MediaStates mediaStates) {
        HSMusicApplication.getInstance().getWidgetUtils().onActionsChanged(mediaStates);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayPause playPause) {
        if (isPlaying()) {
            pausePlayer();
        } else {
            resumePlayer();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChangeShuffle changeShuffle) {
        changeShuffle();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChangeRepeat changeRepeat) {
        changeRepeat();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayNext playNext) {
        playNext();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayPrevious playPrevious) {
        playPrevious();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SeekTo seekTo) {
        seek(seekTo.position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Timer timer) {
        new Handler().postDelayed(this::stopPlayback, timer.time);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ListChanged listChanged) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        HSMusicApplication.getInstance().getMediaSessionUtils().setQueue(getQueueItemListFromSongs());
        saveSongs();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Play play) {
        if (mediaPlayer != null) {
            mediaPlayer.setCurrentPosition(-2);
        }
        playSong();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlayAtPosition playAtPosition) {
        setSongPosition(playAtPosition.position);
        playSong();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ListModified listModified) {
        if (mediaPlayer != null) {
            mediaPlayer.setCurrentPosition(getSongPosition());
            mediaPlayer.prepareNextMediaPlayer(getSongPosition());
        }
        HSMusicApplication.getInstance().getMediaSessionUtils().setQueue(getQueueItemListFromSongs());
        saveSongs();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RemoveFromQueue removeFromQueue) {
        removeSongFromQueue(removeFromQueue.position);
        HSMusicApplication.getInstance().getMediaSessionUtils().setQueue(getQueueItemListFromSongs());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MoveQueueItem moveQueueItem) {
        moveItem(moveQueueItem.from, moveQueueItem.to);
        HSMusicApplication.getInstance().getMediaSessionUtils().setQueue(getQueueItemListFromSongs());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEqualizerActivity updateEqualizerActivity) {
        if (equalizer == null) {
            setUpEqualizer();
        }
        EventBus.getDefault().post(new EqualizerBassboostVirtualizerPresetReverb(
                getEnabled(),
                getNumberOfBands(),
                getBandLevelRange(),
                getCenterFrequencies(),
                getBandLevels(),
                getPresets(),
                getCurrentPreset(),
                getBassBoostLevel(),
                getVirtualizerLevel(),
                getPresetReverbs(),
                getCurrentPresetReverb()
        ));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EquallizerEnabled equallizerEnabled) {
        setEnabled(equallizerEnabled.enabled);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BandLevel bandLevel) {
        setBandLevel(bandLevel.band, bandLevel.level);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Preset preset) {
        setPreset(preset.preset);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Reverb reverb) {
        setCurrentPresetReverb(reverb.reverb);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BassBoostLevel bassBoostLevel) {
        setBassBoostLevel(bassBoostLevel.level);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VirtualizerLevel virtualizerLevel) {
        setVirtualizerLevel(virtualizerLevel.level);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaySongAtStart playSongAtStart) {
        handlePlayback(playSongAtStart.path);
        EventBus.getDefault().removeStickyEvent(PlaySongAtStart.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LinkVisualizer linkVisualizer) {
        addVisualizerConnection();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UnlinkVisualizer unlinkVisualizer) {
        removeVisualizerConnection();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ClearQueue clearQueue) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AudioSessionIdChanged audioSessionIdChanged) {
        setUpEqualizer();
        setUpBassboostAndVirtualizer();
        setUpPresetReverb();
    }

    private void handlePlayback(String path) {
        File parentFile = new File(path);
        parentFile = parentFile.getParentFile();
        ArrayList<Song> songs = null;
        if (parentFile != null) {
            songs = getAllSongsOfFolder(getContentResolver(), MediaStore.Audio.Media.TITLE, parentFile.getAbsolutePath());
        }
        int pos = -1;
        if (songs != null) {
            if (songs.size() > 0) {
                for (int i = 0; i < songs.size(); i++) {
                    Song song = songs.get(i);
                    if (song.getPath().equals(path)) {
                        pos = i;
                        break;
                    }
                }
                playList(songs, pos);
            } else {
                postToast(R.string.cant_play_song, getApplicationContext(), TOAST_ERROR);
            }
        }
    }

    public class MusicPlayerBroadcastReceiver extends BroadcastReceiver {

        public MusicPlayerBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case NOTIFY_PAUSE_OR_RESUME: {
                        if (isPlaying())
                        {
                            pausePlayer();
                        }
                        else
                            {

                            resumePlayer();
                        }
                        break;
                    }
                    case NOTIFY_NEXT: {
                        playNext();
                        break;
                    }
                    case NOTIFY_PREVIOUS: {
                        playPrevious();
                        break;
                    }
                    case NOTIFY_REPEAT: {
                        changeRepeat();
                        break;
                    }
                    case NOTIFY_SKIP2SONGS: {
                        setSongPosition(getSongPosition() + 2);
                        playSong();
                        break;
                    }
                    case NOTIFY_SKIP3SONGS: {
                        setSongPosition(getSongPosition() + 3);
                        playSong();
                        break;
                    }
                    case NOTIFY_PLAY_SONG_AT: {
                        setSongPosition(intent.getIntExtra("position", 0));
                        playSong();
                        break;
                    }
                    case NOTIFY_SHUFFLE: {
                        changeShuffle();
                        break;
                    }
                    case NOTIFY_STOP: {
                        stopPlayback();
                        break;
                    }
                    case NOTIFY_FAVORITE: {
                        createPlaylist("Favorites", getApplicationContext());
                        if (isCurrentSongFavorite(getApplicationContext())) {
                            removeCurrentSongFromFavorite(getApplicationContext());
                        } else {
                            addCurrentSongToFavorite(getApplicationContext());
                        }
                        break;
                    }
                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY: {
                        if (HSMusicApplication.getInstance().getPreferenceUtils().getPauseOnDisconnect()) {
                            stopPlayback();
                        }
                        break;
                    }
                }
            }
        }
    }

    private void connectReceiver() {
        if (musicPlayerBroadcastReceiver == null) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NOTIFY_PAUSE_OR_RESUME);
        intentFilter.addAction(NOTIFY_NEXT);
        intentFilter.addAction(NOTIFY_PREVIOUS);
        intentFilter.addAction(NOTIFY_REPEAT);
        intentFilter.addAction(NOTIFY_SKIP2SONGS);
        intentFilter.addAction(NOTIFY_SKIP3SONGS);
        intentFilter.addAction(NOTIFY_SHUFFLE);
        intentFilter.addAction(NOTIFY_STOP);
        intentFilter.addAction(NOTIFY_FAVORITE);
        intentFilter.addAction(NOTIFY_PLAY_SONG_AT);
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(musicPlayerBroadcastReceiver, intentFilter);
    }

    /**
     * Settings
     */

    private void readSettings() {
        HSMusicApplication.getInstance().getPlayingQueueManager().setShuffle(HSMusicApplication.getInstance().getPreferenceUtils().getShuffle());
        HSMusicApplication.getInstance().getPlayingQueueManager().setRepeat(HSMusicApplication.getInstance().getPreferenceUtils().getRepeat());
        HSMusicApplication.getInstance().getPlayingQueueManager().setSongPosition(HSMusicApplication.getInstance().getPreferenceUtils().getSongPosition());
        HSMusicApplication.getInstance().getMediaSessionUtils().setRepeat(HSMusicApplication.getInstance().getPlayingQueueManager().getRepeat());
        HSMusicApplication.getInstance().getMediaSessionUtils().setShuffle(HSMusicApplication.getInstance().getPlayingQueueManager().getShuffle() ? PlaybackStateCompat.SHUFFLE_MODE_ALL : PlaybackStateCompat.SHUFFLE_MODE_NONE);
    }

    /**
     * Binder
     */

    private final IBinder iBinder = new MusicServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    private class MusicServiceBinder extends Binder {
    }

    @Override
    public void onTrackResumed() {
        try {
            startBroadcast(getCurrentSong(), getApplicationContext(), getCurrentPlaybackPosition());
            HSMusicApplication.getInstance().getMediaSessionUtils().setPlaybackState(PlaybackStateCompat.STATE_PLAYING, getCurrentPlaybackPosition());
            if (isTrackStopped) {
                notificationUtils = new NotificationUtils();
                notificationUtils.init(MusicService.this, getApplicationContext());
                isTrackStopped = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTrackPaused() {
        try {
            pauseBroadcast(getCurrentSong(), getApplicationContext(), getCurrentPlaybackPosition());
            HSMusicApplication.getInstance().getMediaSessionUtils().setPlaybackState(PlaybackStateCompat.STATE_PAUSED, getCurrentPlaybackPosition());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTrackStopped() {
        HSMusicApplication.getInstance().getMediaSessionUtils().setPlaybackState(PlaybackStateCompat.STATE_STOPPED, getCurrentPlaybackPosition());
        isTrackStopped = true;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate() {
        super.onCreate();

        readSettings();

        isTrackStopped = true;

        initAudioManager();
        initMediaSession();
        initMediaPlayer();
        connectReceiver();

        readSongs();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void setVolume(float volume) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void resumePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public void pausePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            if (!isPausedByTransientLossOfFocus) {
                abandonAudioFocus();
            }
        }
    }

    public void setList(ArrayList<Song> songs) {
        HSMusicApplication.getInstance().getPlayingQueueManager().setSongs(songs);
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        HSMusicApplication.getInstance().getMediaSessionUtils().setQueue(getQueueItemListFromSongs());
        saveSongs();
    }

    public void changeShuffle() {
        HSMusicApplication.getInstance().getPlayingQueueManager().changeShuffle();
        HSMusicApplication.getInstance().getMediaSessionUtils().setShuffle(HSMusicApplication.getInstance().getPlayingQueueManager().getShuffle() ? PlaybackStateCompat.SHUFFLE_MODE_ALL : PlaybackStateCompat.SHUFFLE_MODE_NONE);
        HSMusicApplication.getInstance().getMediaSessionUtils().setQueue(getQueueItemListFromSongs());
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.setCurrentPosition(getSongPosition());
            mediaPlayer.prepareNextMediaPlayer(getSongPosition());
        }
        saveSongs();
    }

    public Song getCurrentSong() {
        return HSMusicApplication.getInstance().getPlayingQueueManager().getCurrentSong();
    }

    public void changeRepeat() {
        HSMusicApplication.getInstance().getPlayingQueueManager().changeRepeat();
        HSMusicApplication.getInstance().getMediaSessionUtils().setRepeat(HSMusicApplication.getInstance().getPlayingQueueManager().getRepeat());
        HSMusicApplication.getInstance().getPreferenceUtils().setRepeat(HSMusicApplication.getInstance().getPlayingQueueManager().getRepeat());
    }

    public void playSong() {
        if (HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue() > 0) {
            if (mediaPlayer != null) {
                mediaPlayer.playSong(HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition());
            }
            HSMusicApplication.getInstance().getPreferenceUtils().modifySongPositionAndLastPlayed(getSongPosition(), getCurrentSong());
            HSMusicApplication.getInstance().getMediaSessionUtils().setMetadata(
                    getApplicationContext(),
                    getCurrentSong(),
                    getSongPosition(),
                    getSongs().size()
            );
        }
    }

    public void playPrevious() {
        if (getCurrentPlaybackPosition() <= 5000) {
            if (HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition() - 1 < 0) {
                setSongPosition(HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue() - 1);
            } else {
                setSongPosition(HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition() - 1);
            }
            playSong();
        } else {
            seek(0);
        }
    }

    public int getCurrentPlaybackPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPlaybackPosition();
        } else {
            return 0;
        }
    }

    public void playNext() {
        if (HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition() + 1 >= HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue()) {
            setSongPosition(0);
        } else {
            setSongPosition(HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition() + 1);
        }
        playSong();
    }

    public void seek(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            HSMusicApplication.getInstance().getMediaSessionUtils().setPlaybackPosition(getCurrentPlaybackPosition());
        }
    }

    public void onCompletionOfMediaPlayer() {
        switch (HSMusicApplication.getInstance().getPlayingQueueManager().getRepeat()) {
            case PlaybackStateCompat.REPEAT_MODE_NONE: {
                if (HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition() == HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue() - 1) {
                    onTrackPaused();
                    seek(0);
                } else {
                    playNext();
                }
                break;
            }
            case PlaybackStateCompat.REPEAT_MODE_ALL: {
                playNext();
                break;
            }
            case PlaybackStateCompat.REPEAT_MODE_ONE: {
                playSong();
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(musicPlayerBroadcastReceiver);
            stopPlayback();

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }

            HSMusicApplication.getInstance().getMediaSessionUtils().release();
            HSMusicApplication.getInstance().getWidgetUtils().nullify();
            HSMusicApplication.getInstance().nullify();

            shutExecutorServiceDown();

            if (notificationUtils != null) {
                notificationUtils.nullify();
                notificationUtils = null;
            }

            isTrackStopped = true;

            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        if (notificationUtils != null) {
            notificationUtils.nullify();
            isTrackStopped = true;
        }
    }

    public ArrayList<Song> getSongs() {
        return HSMusicApplication.getInstance().getPlayingQueueManager().getSongs();
    }

    public int getSongPosition() {
        return HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition();
    }

    public void setSongPosition(int songPosition) {
        HSMusicApplication.getInstance().getPlayingQueueManager().setSongPosition(songPosition);
    }

    public void readSongs() {
        try {
            HSMusicApplication.getInstance().getPreferenceUtils().readSongs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.reset();
        prepare();
    }

    public void prepare() {
        if (HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue() > 0) {
            if (mediaPlayer != null) {
                mediaPlayer.setCurrentPosition(getSongPosition());
                mediaPlayer.prepareTwoMediaPlayers(getSongPosition(), false, false);
            }
            HSMusicApplication.getInstance().getMediaSessionUtils().setMetadata(
                    getApplicationContext(),
                    getCurrentSong(),
                    getSongPosition(),
                    getSongs().size()
            );
        }
    }

    public int getAudioSessionId()
    {
        if (mediaPlayer != null)
        {
            HSMusicApplication.getInstance().getMediaSessionUtils().setAudioID(mediaPlayer.getAudioSessionId());
            return mediaPlayer.getAudioSessionId();
        } else {
            return -1;
        }
    }

    public void moveItem(int from, int to) {
        try {
            if (HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue() > 0) {
                Song song = getCurrentSong();
                HSMusicApplication.getInstance().getPlayingQueueManager().move(from, to);
                setSongPosition(HSMusicApplication.getInstance().getPlayingQueueManager().getSongs().indexOf(song));
                if (mediaPlayer != null) {
                    mediaPlayer.setCurrentPosition(getSongPosition());
                    mediaPlayer.prepareNextMediaPlayer(getSongPosition());
                }
                saveSongs();
            }
        } catch (IndexOutOfBoundsException ioobe) {
            ioobe.printStackTrace();
        }
    }

    public void removeSongFromQueue(int position) {
        try {
            if (position == HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition()) {
                if (HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue() == 1) {
                    postToast(R.string.cant_remove_from_queue_1_song, getApplicationContext(), TOAST_ERROR);
                    return;
                }
            }
            HSMusicApplication.getInstance().getPlayingQueueManager().remove(position);
            if (position == HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition()) {
                playSong();
            } else if (mediaPlayer != null) {
                mediaPlayer.reset();
            }
            if (position < HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition()) {
                HSMusicApplication.getInstance().getPlayingQueueManager().setSongPosition(HSMusicApplication.getInstance().getPlayingQueueManager().getSongPosition() - 1);
            }
            saveSongs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveSongs() {
        try {
            HSMusicApplication.getInstance().getPreferenceUtils().setSongs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void abandonAudioFocus() {
        if (audioManager != null && onAudioFocusChangeListener != null) {
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }
    }

    private void initMediaPlayer() {
        mediaPlayer = new GaplessFadingMediaPlayer();
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.setOnErrorListener(errorListener);
        mediaPlayer.setStateChangeListener(this);
        mediaPlayer.setAudioManager(audioManager);
        mediaPlayer.setOnAudioFocusChangeListener(onAudioFocusChangeListener);
    }

    private List<MediaSessionCompat.QueueItem> getQueueItemListFromSongs() {
        List<MediaSessionCompat.QueueItem> queueItems = new ArrayList<>();
        for (int i = 0; i < HSMusicApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue(); i++) {
            Song currentSong = HSMusicApplication.getInstance().getPlayingQueueManager().getSongs().get(i);
            queueItems.add(new MediaSessionCompat.QueueItem(new MediaDescriptionCompat.Builder()
                    .setTitle(currentSong.getName())
                    .setSubtitle(currentSong.getArtist())
                    .setMediaId(String.valueOf(currentSong.getId()))
                    .build(), i));
        }
        return queueItems;
    }

    /*
     * Equalizer, Bassboost, Virtualizer & PresetReverb
     */

    private static final int PRIORITY = 1000000;

    private Equalizer equalizer = null;

    private void setUpEqualizer() {
        try {
            if (equalizer != null) {
                equalizer.release();
                equalizer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            equalizer = new Equalizer(PRIORITY, getAudioSessionId());
            equalizer.setEnabled(HSMusicApplication.getInstance().getPreferenceUtils().getEqualizerEnabled());
            if (HSMusicApplication.getInstance().getPreferenceUtils().getPreset() == -1) {
                for (int i = 0; i < getNumberOfBands(); i++) {
                    int bandLevel = HSMusicApplication.getInstance().getPreferenceUtils().getEqualizerBandLevel(i);
                    if (bandLevel != -1) {
                        setBandLevel((short) i, (short) bandLevel);
                    }
                }
            } else {
                equalizer.usePreset((short) HSMusicApplication.getInstance().getPreferenceUtils().getPreset());
            }
        } catch (Exception e) {
            equalizer = null;
            e.printStackTrace();
        }
    }

    public short getNumberOfBands() {
        if (equalizer != null) {
            return equalizer.getNumberOfBands();
        }
        return -1;
    }

    public short[] getBandLevelRange() {
        if (equalizer != null) {
            return equalizer.getBandLevelRange();
        }
        return null;
    }

    public short getBandLevel(short band) {
        if (equalizer != null) {
            return equalizer.getBandLevel(band);
        }
        return 0;
    }

    public short[] getBandLevels() {
        int numberOfBands = getNumberOfBands();
        short[] bandLevels = new short[numberOfBands];
        for (int i = 0; i < numberOfBands; i++) {
            bandLevels[i] = getBandLevel((short) i);
        }
        return bandLevels;
    }

    public void setBandLevel(short band, short level) {
        if (equalizer != null) {
            equalizer.setBandLevel(band, level);
            HSMusicApplication.getInstance().getPreferenceUtils().setEqualizerBandLevel(band, level);
        }
    }

    public int getCenterFrequency(short band) {
        if (equalizer != null) {
            return equalizer.getCenterFreq(band);
        }
        return -1;
    }

    public int[] getCenterFrequencies() {
        int numberOfBands = getNumberOfBands();
        int[] centerFrequencies = new int[numberOfBands];
        for (int i = 0; i < numberOfBands; i++) {
            centerFrequencies[i] = getCenterFrequency((short) i);
        }
        return centerFrequencies;
    }

    public void setEnabled(boolean enabled) {
        if (equalizer != null) {
            equalizer.setEnabled(enabled);
        }
        if (bassBoost != null) {
            bassBoost.setEnabled(enabled);
        }
        if (virtualizer != null) {
            virtualizer.setEnabled(enabled);
        }
        HSMusicApplication.getInstance().getPreferenceUtils().setEqualizerEnabled(enabled);
    }

    public boolean getEnabled() {
        return equalizer != null && equalizer.getEnabled();
    }

    public String[] getPresets() {
        if (equalizer == null) {
            return null;
        }
        String[] presets = new String[equalizer.getNumberOfPresets() + 1];
        presets[0] = "Custom";
        for (int i = 0; i < equalizer.getNumberOfPresets(); i++) {
            presets[i + 1] = equalizer.getPresetName((short) i);
        }
        return presets;
    }

    public int getCurrentPreset() {
        if (equalizer == null) {
            return 0;
        }
        return equalizer.getCurrentPreset() + 1;
    }

    public void setPreset(int preset) {
        if (equalizer == null) {
            return;
        }
        if (preset == 0) {
            for (int i = 0; i < getNumberOfBands(); i++) {
                int bandLevel = HSMusicApplication.getInstance().getPreferenceUtils().getEqualizerBandLevel(i);
                if (bandLevel != -1) {
                    setBandLevel((short) i, (short) bandLevel);
                }
            }
        } else {
            equalizer.usePreset((short) (preset - 1));
            HSMusicApplication.getInstance().getPreferenceUtils().setPreset(preset - 1);
        }

        EventBus.getDefault().post(new PresetChanged(getBandLevels(), getBandLevelRange()));
    }

    private BassBoost bassBoost;

    private Virtualizer virtualizer;

    private void setUpBassboostAndVirtualizer() {
        try {
            if (bassBoost != null) {
                bassBoost.release();
                bassBoost = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (virtualizer != null) {
                virtualizer.release();
                virtualizer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            bassBoost = new BassBoost(PRIORITY, getAudioSessionId());
            bassBoost.setEnabled(HSMusicApplication.getInstance().getPreferenceUtils().getEqualizerEnabled());
            bassBoost.setStrength((short) HSMusicApplication.getInstance().getPreferenceUtils().getBassBoostLevel());
            virtualizer = new Virtualizer(PRIORITY, getAudioSessionId());
            virtualizer.setEnabled(HSMusicApplication.getInstance().getPreferenceUtils().getEqualizerEnabled());
            virtualizer.setStrength((short) HSMusicApplication.getInstance().getPreferenceUtils().getVirtualizerLevel());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBassBoostLevel(int level) {
        if (bassBoost != null) {
            bassBoost.setStrength((short) level);
        }
        HSMusicApplication.getInstance().getPreferenceUtils().setBassBoostLevel(level);
    }

    public void setVirtualizerLevel(int level) {
        if (virtualizer != null) {
            virtualizer.setStrength((short) level);
        }
        HSMusicApplication.getInstance().getPreferenceUtils().setVirtualizerLevel(level);
    }

    public int getBassBoostLevel() {
        if (bassBoost != null) {
            return bassBoost.getRoundedStrength();
        }
        return 0;
    }

    public int getVirtualizerLevel() {
        if (virtualizer != null) {
            return virtualizer.getRoundedStrength();
        }
        return 0;
    }

    private PresetReverb presetReverb;

    private void setUpPresetReverb() {
        try {
            presetReverb = new PresetReverb(PRIORITY, getAudioSessionId());
            presetReverb.setEnabled(true);
            setCurrentPresetReverb(HSMusicApplication.getInstance().getPreferenceUtils().getPresetReverb());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getPresetReverbs() {
        return new String[]{
                "None",
                "Small Room",
                "Medium Room",
                "Large Room",
                "Medium Hall",
                "Large Hall",
                "Plate"
        };
    }

    public int getCurrentPresetReverb() {
        if (presetReverb == null) {
            return 0;
        }
        return presetReverb.getPreset();
    }

    public void setCurrentPresetReverb(int reverb) {
        if (presetReverb != null) {
            presetReverb.setPreset((short) reverb);
        }
        HSMusicApplication.getInstance().getPreferenceUtils().setPresetReverb(reverb);
    }

    private Visualizer visualizer = null;

    private int visualizerConnections = 0;

    private void setUpVisualizer() {
        try {
            if (visualizer != null) {
                visualizer.release();
                visualizer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            visualizer = new Visualizer(getAudioSessionId());
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    EventBus.getDefault().post(new VisualizerData(waveform));
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

                }
            }, Visualizer.getMaxCaptureRate(), true, false);
            visualizer.setEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseVisualizer() {
        try {
            if (visualizer != null) {
                visualizer.release();
                visualizer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addVisualizerConnection() {
        if (visualizerConnections == 0) {
            setUpVisualizer();
        }
        visualizerConnections++;
        if (visualizer != null) {
            visualizer.setEnabled(true);
        }
    }

    private void removeVisualizerConnection() {
        if (visualizerConnections > 0) {
            visualizerConnections--;
            if (visualizer != null && visualizerConnections == 0) {
                releaseVisualizer();
            }
        }
    }
}