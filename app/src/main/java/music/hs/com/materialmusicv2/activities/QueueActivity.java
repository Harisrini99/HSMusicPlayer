package music.hs.com.materialmusicv2.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.hs.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

public class QueueActivity extends MusicPlayerActivity  {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));

        setContentView(R.layout.activity_queue);

        ButterKnife.bind(this);

        ToolbarUtils.setUpToolbar(
                toolbar,
                getString(R.string.queue),
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                ThemeUtils.getThemePrimaryColor(QueueActivity.this),
                QueueActivity.this,
                this::onBackPressed,
                false
        );

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }




}
