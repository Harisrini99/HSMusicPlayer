package music.hs.com.materialmusicv2.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import music.hs.com.materialmusicv2.GlideApp;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;

public class NowPlayingBackgroundAdapter extends PagerAdapter {

    private ArrayList<Song> songs;
    private Context context;
    private static Context t;
    private LayoutInflater layoutInflater;
    private int screenHeight;
    private int screenWidth;


    public NowPlayingBackgroundAdapter(Context context, ArrayList<Song> songs) {
        try {
            if (songs == null) {
                this.songs = new ArrayList<>();
            } else {
                this.songs = new ArrayList<>(songs);
            }
            this.context = context;
            this.t = context;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position)
    {
        View itemView = layoutInflater.inflate( R.layout.now_playing_cricle, container, false);
        final ViewHolder viewHolder;
        viewHolder = new ViewHolder();
        viewHolder.albumArt = itemView.findViewById(R.id.albumArt);
        viewHolder.back = itemView.findViewById(R.id.back);
        Song currentSong = songs.get(position);
        RequestOptions requestOptions = new RequestOptions();
        boolean isCricle = HSMusicApplication.getInstance().getPreferenceUtils().getIsCircleImage();
        Drawable d ;
        if(isCricle)
        {
            int color = ThemeUtils.getThemeAccentColor(context);
            d = context.getResources().getDrawable(R.drawable.circle_default);
            setColorFilter(color,viewHolder.back);
            requestOptions = requestOptions.transforms(new CenterInside(), new CircleCrop());
        }
        else
        {
            /*String s = "rr_"+currentSong.getName().charAt(0);
            s = s.toLowerCase();
            Drawable dr;
            try {
                int id = context.getResources().getIdentifier(s, "drawable", context.getPackageName());
                dr = context.getResources().getDrawable(id);
            }
            catch (Exception e)
            {
                dr = context.getResources().getDrawable(R.drawable.rr_default);
            }*/
            int color = ThemeUtils.getThemeAccentColor(context);
            d = context.getResources().getDrawable(R.drawable.rr_default);
            viewHolder.back.setImageResource(Color.TRANSPARENT);
            requestOptions = requestOptions.transforms(new CenterInside(), new RoundedCorners(75));
        }
        try {
            GlideApp.with(context)
                    .load(currentSong)
                    .override(screenWidth, screenHeight)
                    .centerCrop()
                    .fitCenter()
                    .apply(requestOptions)
                   .transition(withCrossFade())
                    .error(
                            GlideApp
                                    .with(context)
                                    .load(d)
                    )
                    .into(viewHolder.albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }




        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, final int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }


    private static class ViewHolder {

        ImageView albumArt;
        ImageView back;

    }





}


