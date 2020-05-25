package music.hs.com.materialmusicv2.utils.recyclerviewutils;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.activities.MainActivity;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;

public class RecyclerViewUtils {

    public static void setUpRecyclerView(RecyclerView recyclerView, RecyclerView.LayoutManager layoutManager, @ColorInt int tintColor) {
        if (recyclerView == null || layoutManager == null) {
            return;
        }
        recyclerView.setLayoutManager(layoutManager);

        if (recyclerView instanceof FastScrollRecyclerView) {
            FastScrollRecyclerView fastScrollRecyclerView = (FastScrollRecyclerView) recyclerView;

            fastScrollRecyclerView.setThumbInactiveColor(tintColor);
            fastScrollRecyclerView.setThumbColor(tintColor);
            fastScrollRecyclerView.setPopupBgColor(tintColor);
            fastScrollRecyclerView.setPopupTextColor(ContrastColor(tintColor));

        }
    }
}
