package music.hs.com.materialmusicv2.bottomsheetdialogs;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getBottomSheetDialogFragmentTheme;

public class BottomNowPlayingMenu extends BottomSheetDialogFragment {

    public String tag = "BottomNavigationDrawerFragment";

    @BindView(R.id.nowplayingview)
    public NavigationView NowPlayingView;


    private Callback callback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_now_playing_menu, container, false);
        ButterKnife.bind(this, rootView);

        if (callback != null) {
            callback.onReady(NowPlayingView);
        }

        if (getActivity() != null) {
            NowPlayingView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rounded_top_rect_16));
            NowPlayingView.getBackground().mutate().setColorFilter(ThemeUtils.getThemeWindowBackgroundColor(getActivity()), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        Drawable drawable = NowPlayingView.getItemBackground();
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(ThemeUtils.getThemeAccentColor(getActivity()), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        return rootView;
    }

    public interface Callback {
        void onReady(NavigationView NowPlayingView);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getTheme() {
        return getBottomSheetDialogFragmentTheme(getActivity());
    }
}
