package music.hs.com.materialmusicv2.adapters;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import music.hs.com.materialmusicv2.ChangeImage;
import music.hs.com.materialmusicv2.GlideApp;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.ShowDatabaseObject;
import music.hs.com.materialmusicv2.SongMetaDataObject;
import music.hs.com.materialmusicv2.activities.CreateArtWork;
import music.hs.com.materialmusicv2.activities.EditMetaDataActivity;
import music.hs.com.materialmusicv2.activities.NowPlayingActivity;
import music.hs.com.materialmusicv2.activities.SearchSongOnlineActivity;
import music.hs.com.materialmusicv2.customviews.others.RoundedSquareImageView;
import music.hs.com.materialmusicv2.glide.PaletteBitmap;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.utils.colorutils.ColorUtils;
import music.hs.com.materialmusicv2.utils.controller.Controller;
import music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils;
import music.hs.com.materialmusicv2.utils.misc.Statics;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.fileutils.FileUtils.deleteCache;
import static music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils.deleteLyricsFromCache;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;


public class SearchSongOnlineAdapter extends BaseAdapter {

    Context context;
    ArrayList<SongMetaDataObject> slist;


    public SearchSongOnlineAdapter(Context context,ArrayList<SongMetaDataObject> l)
    {
        this.context = context;
        this.slist = l;

    }


    @Override
    public int getCount()
    {
        return slist.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        ViewHolder viewHolder;

        final View result;


        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.search_song_online_format, parent, false);
            viewHolder.layout = (RelativeLayout) convertView.findViewById(R.id.itemOnClicker);
            viewHolder.name = (TextView) convertView.findViewById(R.id.songName);
            viewHolder.artist = (TextView) convertView.findViewById(R.id.songArtist);
            viewHolder.album = (TextView) convertView.findViewById(R.id.songAlbum);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.songImage);
            viewHolder.cardView = (CardView)convertView.findViewById(R.id.card);


            result=convertView;

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        int cardBackgroundColor;
        boolean t = ThemeUtils.isThemeDarkOrBlack();
        if (t)
        {
            cardBackgroundColor = ContextCompat.getColor(context, R.color.md_grey_800);
            viewHolder.name.setTextColor(Color.WHITE);
            viewHolder.artist.setTextColor(Color.parseColor("#BDBDBD"));
            viewHolder.album.setTextColor(Color.parseColor("#BDBDBD"));

        }
        else {
            cardBackgroundColor = ContextCompat.getColor(context, R.color.md_grey_100);
            viewHolder.name.setTextColor(Color.BLACK);
            viewHolder.artist.setTextColor(Color.parseColor("#646464"));
            viewHolder.album.setTextColor(Color.parseColor("#646464"));

        }
        viewHolder.cardView.setCardBackgroundColor(cardBackgroundColor);
        viewHolder.name.setText(slist.get(position).getTrackName());
        viewHolder.artist.setText(slist.get(position).getArtistName());
        viewHolder.album.setText(slist.get(position).getAlbumName());

        Glide.with(context)
                .load(slist.get(position).getUrl())
                .into(viewHolder.image);




        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SearchSongOnlineActivity.searchSongOnlineActivity.setData(slist,position);
                }

            });


        return convertView;
    }



    private static class ViewHolder {

        RelativeLayout layout;
        TextView name;
        TextView artist;
        TextView album;
        ImageView image;
        CardView cardView;

    }


}
