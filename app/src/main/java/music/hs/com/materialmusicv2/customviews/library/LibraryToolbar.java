package music.hs.com.materialmusicv2.customviews.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.adjustAlpha;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;

public class LibraryToolbar extends RelativeLayout {

    private TextView title;
    private ImageButton hamburgerMenu;
    private ImageButton menu;
    private MaterialCardView cardView;
    private TextView timer;
    private ImageButton sleepButton;

    public LibraryToolbar(Context context) {
        super(context);
        init();
    }

    public LibraryToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LibraryToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_toolbar, this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        title = findViewById(R.id.title);
        hamburgerMenu = findViewById(R.id.hamburgerMenu);
        menu = findViewById(R.id.menu);
        cardView = findViewById(R.id.toolbar_card);
        timer = findViewById(R.id.timer_text);
        sleepButton = findViewById(R.id.sleepButton);
    }

    public void setTitle(String title) {
        if (this.title != null) {
            this.title.setText(title);
        }
    }

    public void setTextViewTitle(String title) {
        if (this.timer != null) {
            this.timer.setText(title);
        }
    }


    public void setVisble() {
        if (this.timer != null) {
            this.timer.setVisibility(VISIBLE);
        }
    }

    public void setGone() {
        if (this.timer != null) {
            this.timer.setVisibility(GONE);
        }
    }

    public void setColorToTimer() {
        if (this.timer != null) {
            this.timer.setTextColor(ContrastColor(ThemeUtils.getThemePrimaryColor(getContext())));
        }
    }

    public void setAnim() {
        if (this.timer != null) {
            Animation aniSlide = AnimationUtils.loadAnimation(getContext(), R.anim.blink);

            this.timer.setAnimation(aniSlide);
        }
    }



    public void setSleepButtonClick(@NonNull Runnable click)
    {
        if (sleepButton != null) {
            sleepButton.setOnClickListener(view -> click.run());
        }
    }


    public void setHamburgerMenuClick(@NonNull Runnable click)
    {
        if (hamburgerMenu != null) {
            hamburgerMenu.setOnClickListener(view -> click.run());
        }
    }

    public void setMenuClick(@NonNull Runnable click) {
        if (menu != null) {
            menu.setOnClickListener(view -> click.run());
        }
    }

    public void setTitleClick(@NonNull Runnable click) {
        if (title != null) {
            title.setOnClickListener(view -> click.run());
        }
    }

    public void setColorForSleep(int col)
    {
        setColorFilter(col,sleepButton);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        title = null;
        menu = null;
        hamburgerMenu = null;
    }

    @Override
    public void setBackgroundColor(int color) {
        if (cardView != null) {
            cardView.setCardBackgroundColor(color);
            cardView.setStrokeColor(adjustAlpha(ContrastColor(color), 0.2f));
        }

        setTextColor(ContrastColor(color), title);
        setColorFilter(ContrastColor(color), hamburgerMenu, menu,sleepButton);
    }

    public View getMenu() {
        return menu;
    }
}
