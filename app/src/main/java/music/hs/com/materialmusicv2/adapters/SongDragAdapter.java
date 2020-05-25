package music.hs.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

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
import music.hs.com.materialmusicv2.GlideApp;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.activities.EditMetaDataActivity;
import music.hs.com.materialmusicv2.bottomsheetdialogs.BottomPlaylistMenu;
import music.hs.com.materialmusicv2.glide.PaletteBitmap;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.utils.bitmaputils.BitmapUtils;
import music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.hs.com.materialmusicv2.utils.fileutils.FileUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.utils.controller.Controller.addSongToQueue;
import static music.hs.com.materialmusicv2.utils.controller.Controller.playList;
import static music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.hs.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.movePlaylistItem;
import static music.hs.com.materialmusicv2.utils.queryutils.QueryUtils.removeFromPlaylist;
import static music.hs.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeTextColorPrimary;

public class SongDragAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private ArrayList<Song> songs;
    private int pixels;
    private Activity activity;
    private static int pos;
    private int imageType;
    private BottomPlaylistMenu bottomPlaylistMenu;
    private long ID;

    public SongDragAdapter(ArrayList<Song> songs, Activity activity, long ID) {
        this.songs = new ArrayList<>(songs);
        this.activity = activity;
        this.ID = ID;

        if (activity != null) {
            pixels = (int) (40 * activity.getResources().getDisplayMetrics().density);
        }

        this.imageType = HSMusicApplication.getInstance().getPreferenceUtils().getImageType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item_drag, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        try {
            holder.albumArt.setImageDrawable(null);
            final Song currentSong = songs.get(position);


            try {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(24));

                holder.target = GlideApp.with(activity)
                        .as(PaletteBitmap.class)
                        .load(currentSong)
                        .override(pixels, pixels)
                        .error(R.drawable.rr_default)
                        .apply(requestOptions)
                        .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                            @Override
                            protected void setResource(PaletteBitmap resource) {
                                if (resource != null && resource.bitmap != null) {
                                    holder.albumArt.setImageBitmap(resource.bitmap);
                                    holder.albumArt.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_album_art));
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.songName.setText(currentSong.getName());
            holder.songArtist.setText(currentSong.getArtist());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        try {
            GlideApp.with(activity).clear(holder.target);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onViewRecycled(viewHolder);
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        try {
            return songs.get(i).getName().substring(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return " ";
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.albumArt)
        ImageView albumArt;
        @BindView(R.id.songName)
        TextView songName;
        @BindView(R.id.songArtist)
        TextView songArtist;
        @BindView(R.id.menu)
        ImageButton menu;
        @BindView(R.id.dragHandle)
        ImageView dragHandle;
        @Nullable

        Target<PaletteBitmap> target;

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnLongClick({R.id.songItemBackground})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.songItemBackground, R.id.menu})
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.songItemBackground: {
                    playList(songs, ViewHolder.this.getAdapterPosition());
                    break;
                }
                case R.id.menu: {
                    final int position = ViewHolder.this.getAdapterPosition();
                    pos = position;
                    openNavDrawer();
                   /* PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_playlist_songs, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final int position = ViewHolder.this.getAdapterPosition();
                        final Song song = songs.get(position);

                        }
                        return true;
                    });
                    popupMenu.show();*/
                    break;
                }
            }
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }

    public boolean moveItem(int from, int to) {
        if (movePlaylistItem(activity.getContentResolver(), ID, from, to)) {
            songs.add(to, songs.remove(from));
            notifyItemMoved(from, to);
            return true;
        }
        return false;
    }



    public void openNavDrawer()
    {
        bottomPlaylistMenu = new BottomPlaylistMenu();
        bottomPlaylistMenu.setCallback(this::setUpNavigationView);
        bottomPlaylistMenu.show(((FragmentActivity) activity).getSupportFragmentManager(), bottomPlaylistMenu.tag);
    }

    private void setUpNavigationView(BottomNavigationView navigationView) {
        if (navigationView == null) {
            return;
        }


        final Song song = songs.get(pos);

        Bitmap b =  getImage(song.getPath());
        int textColorPrimary = ThemeUtils.getThemeTextColorPrimary(activity);
        bottomPlaylistMenu.setHeader(song.getName(),song.getArtist(),b,textColorPrimary);

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
            if (bottomPlaylistMenu != null) {
                bottomPlaylistMenu.dismiss();
            }
            switch (item.getItemId()) {
                case R.id.action_add_to_queue: {
                    addSongToQueue(song);
                    break;
                }
                case R.id.action_remove_from_playlist: {
                    removeFromPlaylist(activity.getContentResolver(), (int) song.getId(), ID);
                    postToast(R.string.removed_from_playlist, activity, TOAST_SUCCESS);
                    songs.remove(pos);
                    notifyItemRemoved(pos);
                    break;
                }
                case R.id.action_edit: {
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
                                        notifyItemRemoved(pos);
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
}
