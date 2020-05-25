    package music.hs.com.materialmusicv2.adapters;

    import android.content.Context;
    import android.content.Intent;
    import android.graphics.Color;
    import android.graphics.PorterDuff;
    import android.graphics.drawable.Drawable;
    import android.text.method.ScrollingMovementMethod;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;

    import music.hs.com.materialmusicv2.ChangeImage;
    import music.hs.com.materialmusicv2.GlideApp;
    import music.hs.com.materialmusicv2.HSMusicApplication;
    import music.hs.com.materialmusicv2.R;
    import music.hs.com.materialmusicv2.ShowDatabaseObject;
    import music.hs.com.materialmusicv2.activities.CreateArtWork;
    import music.hs.com.materialmusicv2.activities.NowPlayingActivity;
    import music.hs.com.materialmusicv2.activities.ShowDatabaseActivity;
    import music.hs.com.materialmusicv2.customviews.others.RoundedSquareImageView;
    import music.hs.com.materialmusicv2.glide.PaletteBitmap;
    import music.hs.com.materialmusicv2.objects.Song;
    import music.hs.com.materialmusicv2.utils.colorutils.ColorUtils;
    import music.hs.com.materialmusicv2.utils.controller.Controller;
    import music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils;
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

    import com.bumptech.glide.request.target.ImageViewTarget;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.Query;
    import com.google.firebase.database.ValueEventListener;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.Locale;

    import static music.hs.com.materialmusicv2.utils.lyricsutils.LyricsUtils.deleteLyricsFromCache;


    public class ShowDatabaseAdapter extends BaseAdapter {

        Context context;
        static RelativeLayout last;
        ArrayList<ShowDatabaseObject> list = new ArrayList<>();
        ArrayList<ShowDatabaseObject> slist = new ArrayList<>();




        public ShowDatabaseAdapter(Context context,ArrayList<ShowDatabaseObject> l)
        {
            this.context = context;
            this.slist = l;
            for(ShowDatabaseObject o : slist)
                list.add(o);
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
                convertView = inflater.inflate(R.layout.show_database_details_format, parent, false);

                viewHolder.layout = (RelativeLayout) convertView.findViewById(R.id.itemOnClicker);
                viewHolder.name = (TextView) convertView.findViewById(R.id.songName);
                viewHolder.artist = (TextView) convertView.findViewById(R.id.songArtist);
                viewHolder.duration = (TextView) convertView.findViewById(R.id.songDuration);
                viewHolder.lyrics = (TextView) convertView.findViewById(R.id.songLyrics);
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
                viewHolder.duration.setTextColor(Color.parseColor("#BDBDBD"));

            }
            else {
                cardBackgroundColor = ContextCompat.getColor(context, R.color.md_grey_100);
                viewHolder.name.setTextColor(Color.BLACK);
                viewHolder.artist.setTextColor(Color.parseColor("#646464"));
                viewHolder.duration.setTextColor(Color.parseColor("#646464"));
                viewHolder.lyrics.setTextColor(Color.parseColor("#646464"));
                viewHolder.lyrics.setBackgroundColor(Color.parseColor("#45646464"));


            }
            viewHolder.cardView.setCardBackgroundColor(cardBackgroundColor);

            viewHolder.name.setText(slist.get(position).getName());
            viewHolder.artist.setText(slist.get(position).getArtist());
            viewHolder.duration.setText(slist.get(position).getDuration());
            viewHolder.lyrics.setText(slist.get(position).getLyrics());


            viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Song s = Controller.getCurrentSong();
                    long id = s.getId();
                    deleteLyricsFromCache(context,id);
                    LyricsUtils.deleteLyricsFromCache(context,id);
                    LyricsUtils.setLyricsInCache(context, slist.get(position).getOrgLyrics(), s.getId());
                    //Intent intent = new Intent(context, NowPlayingActivity.class);
                    ((ShowDatabaseActivity)context).finish();



                }
            });


            return convertView;
        }

        public void filter(String charText)
        {
            charText = charText.toLowerCase(Locale.getDefault());
            slist.clear();
            if (charText.length() == 0)
            {
                slist.addAll(list);
            }
            else
            {
                for (ShowDatabaseObject wp : list)
                {
                    if (wp.getName().toLowerCase(Locale.getDefault()).contains(charText) || wp.getArtist().toLowerCase(Locale.getDefault()).contains(charText))
                    {
                        slist.add(wp);
                    }
                }
            }
            notifyDataSetChanged();
        }


        private static class ViewHolder {

            RelativeLayout layout;
            TextView name;
            TextView artist;
            TextView duration;
            TextView lyrics;
            CardView cardView;

        }

    }
