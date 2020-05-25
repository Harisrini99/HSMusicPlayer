package music.hs.com.materialmusicv2.bottomsheetdialogs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.customviews.others.RoundedSquareImageView;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getBottomSheetDialogFragmentTheme;

public class BottomPlaylistMenu extends BottomSheetDialogFragment {

    public String tag = "BottomNavigationDrawerFragment";

    @BindView(R.id.menu_songs1)
    public BottomNavigationView navigationView1;
    @BindView(R.id.menu_songs2)
    public BottomNavigationView navigationView2;

    @BindView(R.id.sheetBackground)
    LinearLayout SheetBack;

    @BindView(R.id.albumArtHeader)
    RoundedSquareImageView Albumart;
    @BindView(R.id.songNameHeader)
    TextView SongNameHeader;
    @BindView(R.id.artistNameHeader)
    TextView ArtistNameHeader;

    private Callback callback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_playlist_menu, container, false);
        ButterKnife.bind(this, rootView);

        if (callback != null)
        {
            callback.onReady(navigationView1);
            callback.onReady(navigationView2);
        }

        if (getActivity() != null)
        {

            SheetBack.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rounded_top_rect_16));
           SheetBack.getBackground().mutate().setColorFilter(ThemeUtils.getThemeWindowBackgroundColor(getActivity()), android.graphics.PorterDuff.Mode.SRC_IN);
           // navigationView2.getBackground().mutate().setColorFilter(ThemeUtils.getThemeWindowBackgroundColor(getActivity()), android.graphics.PorterDuff.Mode.SRC_IN);
        }


        return rootView;
    }

    public interface Callback {
        void onReady(BottomNavigationView navigationView);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setHeader(String name, String artist, Bitmap bitmap,int color)
    {
        SongNameHeader.setTextColor(color);
        SongNameHeader.setText(name);

        ArtistNameHeader.setText(artist);
        ArtistNameHeader.setTextColor(color);

        Albumart.setCornerRadius(50);
        Albumart.setRoundedCorners(RoundedSquareImageView.CORNER_ALL);
        Albumart.setImageBitmap(bitmap);

    }

    @Override
    public int getTheme() {
        return getBottomSheetDialogFragmentTheme(getActivity());
    }
}
