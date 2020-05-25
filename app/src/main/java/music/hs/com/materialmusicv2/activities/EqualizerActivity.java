package music.hs.com.materialmusicv2.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.sdsmdg.harjot.crollerTest.Croller;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.tankery.lib.circularseekbar.CircularSeekBar;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.customviews.others.CustomColorSwitchCompat;
import music.hs.com.materialmusicv2.objects.events.controllerevents.BandLevel;
import music.hs.com.materialmusicv2.objects.events.controllerevents.BassBoostLevel;
import music.hs.com.materialmusicv2.objects.events.controllerevents.EqualizerBassboostVirtualizerPresetReverb;
import music.hs.com.materialmusicv2.objects.events.controllerevents.EquallizerEnabled;
import music.hs.com.materialmusicv2.objects.events.controllerevents.Preset;
import music.hs.com.materialmusicv2.objects.events.controllerevents.PresetChanged;
import music.hs.com.materialmusicv2.objects.events.controllerevents.Reverb;
import music.hs.com.materialmusicv2.objects.events.controllerevents.UpdateEqualizerActivity;
import music.hs.com.materialmusicv2.objects.events.controllerevents.VirtualizerLevel;
import music.hs.com.materialmusicv2.utils.colorutils.ColorUtils;
import music.hs.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.hs.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.adjustAlpha;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class EqualizerActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.preset)
    MaterialSpinner preset;
    @BindView(R.id.reverb)
    MaterialSpinner reverb;
    @BindView(R.id.volumenSeekBar)
    SeekBar VolumeSeekBar;
    @BindView(R.id.bassBar)
    Croller BassBar;
    @BindView(R.id.virtualizerBar)
    Croller VirtualizerBar;


    private SeekBar[] seekBars;
    private TextView[] textview;
    private AudioManager audioManager;
    private SettingsContentObserver settingsContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));

        setContentView(R.layout.activity_equalizer);

        ButterKnife.bind(this);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        int colorPrimary = getThemePrimaryColor(this);

        if (HSMusicApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(EqualizerActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        ToolbarUtils.setUpToolbar(toolbar,
                R.string.equalizer,
                new int[]{R.drawable.ic_arrow_back, R.drawable.ic_arrow_back},
                colorPrimary,
                EqualizerActivity.this,
                this::onBackPressed);

        setUpVolume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        getContentResolver().unregisterContentObserver(settingsContentObserver);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    CustomColorSwitchCompat customColorSwitchCompat = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            menu.clear();
        }

        boolean t =ThemeUtils.isThemeDarkOrBlack();
        if(t)
        {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        try {
            getMenuInflater().inflate(R.menu.menu_equalizer, menu);
            MenuItem equalizerSwitch = menu != null ? menu.findItem(R.id.action_equalizer_switch) : null;
            if (equalizerSwitch != null)
            {
                equalizerSwitch.setActionView(R.layout.switch_layout);
                customColorSwitchCompat = (CustomColorSwitchCompat) equalizerSwitch.getActionView();
                int accentColor = getThemeAccentColor(EqualizerActivity.this);
                int c =(accentColor & 0x00FFFFFF) | 0x40000000;
                customColorSwitchCompat.setBgOnColor(c);
                customColorSwitchCompat.setToggleOnColor(accentColor);
                customColorSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        EventBus.getDefault().post(new EquallizerEnabled(isChecked));
                    }
                });
                //customColorSwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> EventBus.getDefault().post(new EquallizerEnabled(b)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventBus.getDefault().post(new UpdateEqualizerActivity());

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpBands(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        try {

                seekBars = new SeekBar[ebvpr.numberOfBands];
                textview = new TextView[ebvpr.numberOfBands];
                short[] bandLevelRange = ebvpr.bandLevelRange;
                for (int i = 0; i < ebvpr.numberOfBands; i++) {
                    int centerFrequency = (ebvpr.centerFrequency[i] / 1000);
                    String button_string = "seekBar" + (i+1);
                    String text_string = "text" + (i+1);
                    int resourceID = getResources().getIdentifier(button_string, "id",
                            getPackageName());
                    seekBars[i] = (SeekBar)findViewById(resourceID);
                    int ID = getResources().getIdentifier(text_string, "id",
                            getPackageName());
                    textview[i]  = (TextView)findViewById(ID);
                    textview[i].setText(String.format(getString(centerFrequency > 999 ? R.string.freq_text_greater_than_ninenininine : R.string.freq_text_less_than_onezerozerozero), centerFrequency > 999 ? centerFrequency / 1000 : centerFrequency));
                   // ColorUtils.setColorFilter(getThemeAccentColor(EqualizerActivity.this), seekBars[i]);
                    seekBars[i].setMax(bandLevelRange[1] - bandLevelRange[0]);
                    int t = bandLevelRange[1] - bandLevelRange[0];
                    seekBars[i].setProgress(ebvpr.bandLevel[i] - bandLevelRange[0]);
                     int finalI = i;
                    seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                            if (b) {
                                EventBus.getDefault().post(new BandLevel((short) finalI, (short) (progress + bandLevelRange[0])));
                                if (preset != null) {
                                    preset.setSelectedIndex(0);
                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpPreset(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        try {
            if (preset != null) {
                preset.setItems(ebvpr.presets);
                preset.setSelectedIndex(ebvpr.currentPreset);
                preset.setOnItemSelectedListener((MaterialSpinner.OnItemSelectedListener<String>) (view, position, id, item) -> EventBus.getDefault().post(new Preset(position)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpBassBoostAndVirtualizer(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        int colorAccent = ThemeUtils.getThemeAccentColor(this);
        try {
            if(BassBar != null)
            {

                BassBar.setProgressSecondaryColor(adjustAlpha(colorAccent, 0.2f));
                BassBar.setMax(1000);
                BassBar.setProgress(ebvpr.bassBoostLevel);
                BassBar.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        EventBus.getDefault().post(new BassBoostLevel((int) progress));

                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (VirtualizerBar != null)
            {
                VirtualizerBar.setProgressSecondaryColor(adjustAlpha(colorAccent, 0.2f));
                VirtualizerBar.setMax(1000);
                VirtualizerBar.setProgress(ebvpr.bassBoostLevel);
                VirtualizerBar.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                    @Override
                    public void onProgressChanged(int progress) {
                        // use the progress
                        EventBus.getDefault().post(new VirtualizerLevel((int) progress));

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpReverb(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        try {
            if (reverb != null) {
                reverb.setItems(ebvpr.reverbs);
                reverb.setSelectedIndex(ebvpr.currentReverb);
                reverb.setOnItemSelectedListener((MaterialSpinner.OnItemSelectedListener<String>) (view, position, id, item) -> EventBus.getDefault().post(new Reverb(position)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpVolume() {
        if (VolumeSeekBar == null) {
            return;
        }

        try {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            int colorAccent = getThemeAccentColor(EqualizerActivity.this);

            VolumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            VolumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            VolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    if (audioManager != null) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) progress, 0);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


            settingsContentObserver = new SettingsContentObserver(new Handler());
            getContentResolver().registerContentObserver(
                    android.provider.Settings.System.CONTENT_URI, true,
                    settingsContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SettingsContentObserver extends ContentObserver {

        SettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateVolume();
        }
    }

    private void updateVolume() {
        try {
            if (VolumeSeekBar != null) {
                VolumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                VolumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        customColorSwitchCompat.setChecked(ebvpr.enabled);
        setUpBands(ebvpr);
        setUpPreset(ebvpr);
        setUpBassBoostAndVirtualizer(ebvpr);
        setUpReverb(ebvpr);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PresetChanged presetChanged) {
        if (seekBars != null) {
            for (int i = 0; i < seekBars.length; i++) {
                seekBars[i].setProgress(presetChanged.bandLevel[i] - presetChanged.bandLevelRange[0]);
            }
        }
    }




}
