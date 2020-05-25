package music.hs.com.materialmusicv2.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.google.android.material.appbar.AppBarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.fragments.SettingsFragment;
import music.hs.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.hs.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbarHolder)
    AppBarLayout ToolBarHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));
        setContentView(R.layout.activity_settings);

        boolean t =ThemeUtils.isThemeDarkOrBlack();

        int colorPrimary = getThemePrimaryColor(this);
        if(t)
        {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
           // if (Build.VERSION.SDK_INT >= 21)
           /// window.setStatusBarColor(getThemeAccentColor(this));
        }
        ButterKnife.bind(this);


        if (HSMusicApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(SettingsActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }


        int[] drawble = {R.drawable.ic_arrow_back_black_24dp,R.drawable.ic_arrow_back};
        ToolbarUtils.setUpToolbar(toolbar,
                R.string.settings,
                drawble,
                colorPrimary,
                SettingsActivity.this,
                this::onBackPressed);

        HSMusicApplication.getInstance().getPreferenceUtils().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsContainer, new SettingsFragment())
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        HSMusicApplication.getInstance().getPreferenceUtils().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "theme":
            case "accentColor":
            case "donated":
            case "language":
                if (sharedPreferences.getString("language", "0").equals("0")) {
                    LocaleHelper.setLocale(SettingsActivity.this, "en");
                } else {
                    LocaleHelper.setLocale(SettingsActivity.this, "es");
                }
                recreate();
                break;
        }
    }
}
