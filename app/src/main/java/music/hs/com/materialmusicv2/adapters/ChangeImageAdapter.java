package music.hs.com.materialmusicv2.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import music.hs.com.materialmusicv2.ChangeImage;
import music.hs.com.materialmusicv2.GlideApp;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.activities.CreateArtWork;
import music.hs.com.materialmusicv2.customviews.others.RoundedSquareImageView;
import music.hs.com.materialmusicv2.glide.PaletteBitmap;
import music.hs.com.materialmusicv2.utils.colorutils.ColorUtils;

import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;

import java.util.ArrayList;


public class ChangeImageAdapter extends BaseAdapter {

    Context context;
    static ImageView last;
    ArrayList<Integer> images;
    int color;




    public ChangeImageAdapter(Context context, ArrayList<Integer> values,int col)
    {
        this.context = context;
        this.images = values;
        this.color = col;

    }

    @Override
    public int getCount()
    {
        return images.size();
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
            convertView = inflater.inflate(R.layout.change_image_format, parent, false);

            viewHolder.image = (RoundedSquareImageView) convertView.findViewById(R.id.image);
            viewHolder.layout = (RelativeLayout) convertView.findViewById(R.id.itemOnClicker);

            result=convertView;


            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }



            viewHolder.image.setImageResource(images.get(position));
            ColorUtils.setColorFilter(color, viewHolder.image);
        CreateArtWork c = new CreateArtWork();
        ChangeImage ci = new ChangeImage(context);
          viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable d = context.getResources().getDrawable(R.drawable.ic_appintro_done_white);
                if(last != null)
                    last.setForeground(null);
                last = viewHolder.image;
                viewHolder.image.setForeground(d);

                c.setImage(images.get(position));
                ci.dismiss();

            }
        });


        return convertView;
    }
    private static class ViewHolder {

        RelativeLayout layout;
        RoundedSquareImageView image;
    }

}
