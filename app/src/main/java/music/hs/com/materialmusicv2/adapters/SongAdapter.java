package music.hs.com.materialmusicv2.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
//import music.symphony.com.materialmusicv2.GlideApp;
import io.gresse.hugo.vumeterlibrary.VuMeterView;
import music.hs.com.materialmusicv2.GlideApp;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.activities.EditMetaDataActivity;
import music.hs.com.materialmusicv2.adapters.diffcallbacks.SongDiffCallback;
import music.hs.com.materialmusicv2.bottomsheetdialogs.BottomMenuSongs;
import music.hs.com.materialmusicv2.glide.PaletteBitmap;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.objects.events.AlbumArt;
import music.hs.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.hs.com.materialmusicv2.objects.events.PlaybackPosition;
import music.hs.com.materialmusicv2.objects.events.PlaybackState;
import music.hs.com.materialmusicv2.utils.bitmaputils.BitmapUtils;
import music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.hs.com.materialmusicv2.utils.fileutils.FileUtils;
import music.hs.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.hs.com.materialmusicv2.utils.controller.Controller.addSongToNextPosition;
import static music.hs.com.materialmusicv2.utils.controller.Controller.addSongToQueue;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playList;
import static music.hs.com.materialmusicv2.utils.controller.Controller.shuffleList;
import static music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.hs.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeTextColorPrimary;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter
{

    private ArrayList<Song> songs;
    private Activity activity;
    private BottomMenuSongs bottomMenuSongs;
    private int pos;
    private long currentSongID;
    private boolean isPlaying = false;
    private boolean showShuffleAll;
    private  boolean isAnimation;


    public SongAdapter(ArrayList<Song> songs, Activity activity, boolean showShuffleAll,boolean isAnimation) {
        this.songs = new ArrayList<>(songs);
        this.activity = activity;
        this.showShuffleAll = showShuffleAll;
        this.isAnimation = isAnimation;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
            return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        Song currentSong = songs.get(position - (showShuffleAll ? 1 : 0));
        long thisId = currentSong.getId();

        try {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(24));
            holder.target = GlideApp
                    .with(activity)
                    .as(PaletteBitmap.class)
                    .load(currentSong)
                    .error(R.drawable.rr_default)
                    .apply(requestOptions)
                    .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            try {
                                if (resource != null && resource.bitmap != null) {
                                    holder.albumArt.setImageBitmap(resource.bitmap);
                                    //holder.albumArt.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_album_art));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();

        }

            holder.songName.setText(currentSong.getName());
            holder.songArtist.setText(currentSong.getArtist());

            if (currentSongID == thisId && isAnimation)
            {
                holder.songName.setTextColor(ThemeUtils.getThemeAccentColor(activity));
                holder.vuaAnimation.setVisibility(View.VISIBLE);
                setColorFilter(ThemeUtils.getThemeAccentColor(activity), holder.menu);
            }
            else
                {
                    holder.songName.setTextColor(ContrastColor(ThemeUtils.getThemePrimaryColor(activity)));
                    holder.vuaAnimation.setVisibility(View.INVISIBLE);
                    setColorFilter(ContrastColor(ThemeUtils.getThemePrimaryColor(activity)), holder.menu);
                }
            if (isPlaying) {
                holder.vuaAnimation.resume(true);
            } else {
                holder.vuaAnimation.pause();
            }


    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getItemViewType() == 1) {
            ViewHolder holder = (ViewHolder) viewHolder;
            try {
               GlideApp.with(activity).clear(holder.target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onViewRecycled(viewHolder);
    }

    @Override
    public int getItemCount() {
        return songs.size() + (showShuffleAll ? 1 : 0);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        if (showShuffleAll && i == 0) {
            i = 1;
        }
        if (songs != null && songs.get(i - (showShuffleAll ? 1 : 0)) != null && songs.get(i - (showShuffleAll ? 1 : 0)).getName() != null) {
            return songs.get(i - (showShuffleAll ? 1 : 0)).getName().substring(0, 1);
        }
        return "";

    }

    @Override
    public int getItemViewType(int position) {
        return showShuffleAll ? (position == 0 ? 0 : 1) : 1;
    }

    public void changeSongs(ArrayList<Song> songs)
    {
        if (songs == null) {
            return;
        }
        ArrayList<Song> diffUtilOldSongs = new ArrayList<>(this.songs);
        ArrayList<Song> diffUtilNewSongs = new ArrayList<>(songs);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SongDiffCallback(diffUtilNewSongs, diffUtilOldSongs));
        diffResult.dispatchUpdatesTo(this);
        this.songs = new ArrayList<>(songs);
    }



    class ViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.albumArt)
        ImageView albumArt;
        @BindView(R.id.songName)
        TextView songName;
        @BindView(R.id.songArtist)
        TextView songArtist;
        @BindView(R.id.menu)
        ImageButton menu;
        @BindView(R.id.vumeter)
        VuMeterView vuaAnimation;


        Target<PaletteBitmap> target;
        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnLongClick({R.id.songItemBackground})
        public boolean onLongClick() {
            if (menu != null) {
                menu.callOnClick();
            }
            return true;
        }

        @SuppressLint("RestrictedApi")
        @OnClick({R.id.songItemBackground, R.id.menu})
        public void onClick(View view)
        {
            if (activity == null) {
                return;
            }
            switch (view.getId())
            {
                case R.id.songItemBackground:
                    {
                        playList(songs, ViewHolder.this.getAdapterPosition() - (showShuffleAll ? 1 : 0));
                        break;
                    }
                case R.id.menu: {
                    pos = ViewHolder.this.getAdapterPosition() - (showShuffleAll ? 1 : 0);
                    openNavDrawer();
                    break;
                }
            }
        }
    }

    class ViewHolderSongHeader extends RecyclerView.ViewHolder {

        @BindView(R.id.shuffleAll)
        MaterialButton shuffleAll;

        ViewHolderSongHeader(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick({R.id.shuffleAll})
        public void OnClick(View view) {
            if (view.getId() == R.id.shuffleAll) {
                shuffleList(songs);
            }
        }
    }

    public void clear() {
        if (songs != null) {
            songs.clear();
            songs = null;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        activity = null;
    }

    public void openNavDrawer()
    {
        bottomMenuSongs = new BottomMenuSongs();
        bottomMenuSongs.setCallback(this::setUpNavigationView);
        bottomMenuSongs.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomMenuSongs.tag);
    }

    private void setUpNavigationView(BottomNavigationView navigationView) {
        if (navigationView == null) {
            return;
        }


        final Song song = songs.get(pos);

        Bitmap b =  getImage(song.getPath());
        int textColorPrimary = ThemeUtils.getThemeTextColorPrimary(activity);
        bottomMenuSongs.setHeader(song.getName(),song.getArtist(),b,textColorPrimary);

        int[][] state = new int[][]
                {
                new int[]{android.R.attr.state_checked},
                new int[]{android.R.attr.state_enabled},
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_pressed}
        };

        int[] color = new int[]{
                getThemeTextColorPrimary(activity),
                getThemeTextColorPrimary(activity),
                getThemeTextColorPrimary(activity),
                getThemeTextColorPrimary(activity),
                getThemeTextColorPrimary(activity)
        };

        ColorStateList colorStateList = new ColorStateList(state, color);

        navigationView.setItemIconTintList(colorStateList);
        navigationView.setItemTextColor(colorStateList);

        navigationView.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(activity));


        navigationView.setOnNavigationItemSelectedListener(item -> {
            if (bottomMenuSongs != null) {
                bottomMenuSongs.dismiss();
            }

                switch (item.getItemId()) {
                    case R.id.action_play_next: {
                        addSongToNextPosition(song);
                        break;
                    }
                    case R.id.action_add_to_playlist: {
                        showAddToPlaylistDialog(activity, song, true);
                        break;
                    }
                    case R.id.action_add_to_queue: {
                        addSongToQueue(song);
                        break;
                    }

                    case R.id.action_edit:
                    {
                        Intent intent = new Intent(activity, EditMetaDataActivity.class);
                        intent.putExtra("PATH", songs.get(pos).getPath());
                        intent.putExtra("ID", songs.get(pos).getId());
                        intent.putExtra("AlBUM_ID", songs.get(pos).getAlbumId());
                        activity.startActivity(intent);
                        break;
                    }
                    case R.id.action_share: {
                        shareSong(activity, song.getPath());
                        break;
                    }
                    case R.id.action_set_as_ringtone: {
                        setAudioAsRingtone(new File(song.getPath()), activity);
                        break;
                    }
                    case R.id.action_delete: {
                        DialogUtils.showYesNoDialog(activity,
                                R.string.are_you_sure,
                                R.string.sure_deleting,
                                new DialogUtils.OnYesNoSelectedListener() {
                                    @Override
                                    public void onYesSelected() {
                                        if (FileUtils.deleteFile(songs.get(pos).getPath(), activity)) {
                                            songs.remove(pos);
                                            notifyItemRemoved(pos );
                                        }
                                    }

                                    @Override
                                    public void onNoSelected() {
                                    }
                                });
                        break;
                    }
                }

            return true;
        });

    }

    private Bitmap getImage(String path) {
            int pixels = activity.getResources().getDisplayMetrics().widthPixels;
            File audio = new File(path);
            AudioFile f = null;
            Artwork artwork = null;
            Bitmap bitmap = null;
            try {
                f = AudioFileIO.read(audio);
                artwork = f.getTag().getFirstArtwork();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (f != null) {
                if (artwork != null) {
                    byte[] data;
                    data = artwork.getBinaryData();
                    if (data != null) {
                        bitmap = BitmapUtils.decodeByteArray(data, data.length, pixels, pixels, Bitmap.Config.ARGB_8888);
                    } else {
                        bitmap = BitmapUtils.decodeResource(activity.getResources(), R.drawable.ic_blank_album_art, pixels, pixels, Bitmap.Config.ARGB_8888);
                    }
                } else {
                    bitmap = BitmapUtils.decodeResource(activity.getResources(), R.drawable.ic_blank_album_art, pixels, pixels, Bitmap.Config.ARGB_8888);
                }
            }
         return bitmap;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackState playbackState)
    {
        isPlaying = playbackState.state;
        ArrayList<Song> s = QueryUtils.getAllSongs(activity.getContentResolver(), HSMusicApplication.getInstance().getPreferenceUtils().getSongSortOrder());
        for(int i=0;i<s.size();i++)
        {
            if(s.get(i).getId() == currentSongID) {
                if(isAnimation) {
                    notifyItemChanged(i);
                }
                break;
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackPosition playbackPosition)
    {
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(CurrentPlayingSong currentPlayingSong)
    {
        long t = currentPlayingSong.id;
        currentSongID =t;
        if(isAnimation) {
            notifyDataSetChanged();
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(AlbumArt albumArt) {

    }

}