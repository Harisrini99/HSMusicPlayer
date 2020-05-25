package music.hs.com.materialmusicv2.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.objects.events.controllerevents.VisualizerTypeChanged;
import music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.hs.com.materialmusicv2.utils.languageutils.LocaleHelper;

import static music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils.showSelectorDialog;
import static music.hs.com.materialmusicv2.utils.fileutils.FileUtils.deleteCache;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference languagePreference = findPreference("language");
        if (languagePreference != null) {
            languagePreference.setOnPreferenceClickListener(preference -> {
                showSelectorDialog(getActivity(),
                        R.string.language,
                        R.array.Languages,
                        HSMusicApplication.getInstance().getPreferenceUtils().getLanguage(),
                        position -> {
                            HSMusicApplication.getInstance().getPreferenceUtils().setLanguage(position);
                            switch (position) {
                                case 0: {
                                    LocaleHelper.setLocale(getActivity(), "en");
                                    break;
                                }
                                case 1: {
                                    LocaleHelper.setLocale(getActivity(), "es");
                                    break;
                                }
                                default: {
                                    LocaleHelper.setLocale(getActivity(), "en");
                                    break;
                                }
                            }
                        });
                return true;
            });
        }

        Preference themePreference = findPreference("theme");
        if (themePreference != null) {
            themePreference.setOnPreferenceClickListener(preference -> {
                DialogUtils.showSelectorDialog(getActivity(),
                        R.string.theme_footer, R.array.Theme,
                        HSMusicApplication.getInstance().getPreferenceUtils().getTheme(),
                        position -> setTheme(position));
                return true;
            });
        }

        Preference imageTypePreference = findPreference("imageType");
        if (imageTypePreference != null) {
            imageTypePreference.setOnPreferenceClickListener(preference -> {
                DialogUtils.showSelectorDialog(getActivity(),
                        R.string.image_type,
                        R.array.ImageType,
                        HSMusicApplication.getInstance().getPreferenceUtils().getImageType(),
                        position -> HSMusicApplication.getInstance().getPreferenceUtils().setImageType(position));
                return true;
            });
        }

        Preference accentColorPreference = findPreference("accentColor");
        if (accentColorPreference != null) {
            accentColorPreference.setOnPreferenceClickListener(preference -> {
                DialogUtils.showColorChooserDialog(getActivity(), R.string.choose_accent_color,
                        position -> HSMusicApplication.getInstance().getPreferenceUtils().setAccentColor(position));
                return true;
            });
        }

        Preference startPagePreference = findPreference("defaultPage");
        if (startPagePreference != null) {
            startPagePreference.setOnPreferenceClickListener(preference -> {
                showSelectorDialog(getActivity(),
                        R.string.start_page,
                        R.array.DefaultPages,
                        HSMusicApplication.getInstance().getPreferenceUtils().getDefaultPage(),
                        position -> HSMusicApplication.getInstance().getPreferenceUtils().setDefaultPage(position));
                return true;
            });
        }

        Preference visualizerTypePreference = findPreference("visualizerType");
        if (visualizerTypePreference != null) {
            visualizerTypePreference.setOnPreferenceClickListener(preference -> {
                if (HSMusicApplication.getInstance().getPreferenceUtils().getDonated()) {
                    showSelectorDialog(getActivity(),
                            R.string.visualizer_type,
                            R.array.VisualizerType,
                            HSMusicApplication.getInstance().getPreferenceUtils().getVisualizerType(),
                            position -> {
                                HSMusicApplication.getInstance().getPreferenceUtils().setVisualizerType(position);
                                EventBus.getDefault().post(new VisualizerTypeChanged(position));
                            });
                } else {
                }
                return true;
            });
        }

        Preference fadePreference = findPreference("fade");
        if (fadePreference != null) {
            fadePreference.setOnPreferenceClickListener(preference ->
            {
               return true;
            });
        }




        Preference queueInNotificationPreference = findPreference("showQueueInNotification");
        if (queueInNotificationPreference != null) {
            queueInNotificationPreference.setOnPreferenceClickListener(preference -> {
                if (HSMusicApplication.getInstance().getPreferenceUtils().getDonated()) {
                    return true;
                } else {
                    ((SwitchPreference) preference).setChecked(false);
                    return false;
                }
            });
        }

        Preference showQueueInNotification = findPreference("showQueueInNotification");
        if (showQueueInNotification != null) {
            if (!HSMusicApplication.getInstance().getPreferenceUtils().getUseMediaStyleNotification()) {
                showQueueInNotification.setVisible(true);
            } else {
                showQueueInNotification.setVisible(false);
            }
        }

        Preference clearCachePreference = findPreference("clearCache");
        if (clearCachePreference != null) {
            clearCachePreference.setOnPreferenceClickListener(preference -> {
                try {
                    deleteCache(getActivity());
                    postToast(R.string.cache_cleared, getActivity(), TOAST_SUCCESS);
                } catch (Exception e) {
                    postToast(R.string.error_label, getActivity(), TOAST_ERROR);
                    e.printStackTrace();
                }
                return true;
            });
        }

        Preference nowPlayingStylePreference = findPreference("nowPlayingStyle");
        if (nowPlayingStylePreference != null) {
            nowPlayingStylePreference.setOnPreferenceClickListener(preference -> {
                showSelectorDialog(getActivity(),
                        R.string.now_playing_style,
                        R.array.NowPlayingStyle,
                        HSMusicApplication.getInstance().getPreferenceUtils().getNowPlayingStyle(),
                        position -> {
                            if (HSMusicApplication.getInstance().getPreferenceUtils().getDonated() || position == 1 || position == 2 || position == 4) {
                                HSMusicApplication.getInstance().getPreferenceUtils().setNowPlayingStyle(position);
                            } else {
                            }
                        });
                return true;
            });
        }
        Preference visualizerSpeedPreference = findPreference("visualizerSpeed");
        if (visualizerSpeedPreference != null) {
            visualizerSpeedPreference.setOnPreferenceClickListener(preference -> {
                if (HSMusicApplication.getInstance().getPreferenceUtils().getDonated()) {
                    showSelectorDialog(getActivity(),
                            R.string.visualizer_speed,
                            R.array.VisualizerUpdateInterval,
                            HSMusicApplication.getInstance().getPreferenceUtils().getVisualizerSpeed(),
                            position -> HSMusicApplication.getInstance().getPreferenceUtils().setVisualizerSpeed(position));
                    return true;
                } else {
                    return false;
                }
            });
        }
        Preference tabTitlesModePreference = findPreference("tabTitlesMode");
        if (tabTitlesModePreference != null) {
            tabTitlesModePreference.setOnPreferenceClickListener(preference -> {
                if (HSMusicApplication.getInstance().getPreferenceUtils().getDonated()) {
                    showSelectorDialog(getActivity(),
                            R.string.tab_titles_mode,
                            R.array.TabTitlesMode,
                            HSMusicApplication.getInstance().getPreferenceUtils().getTabTitlesMode(),
                            position -> HSMusicApplication.getInstance().getPreferenceUtils().setTabTitlesMode(position));
                    return true;
                } else {
                    return false;
                }
            });
        }

        if (visualizerTypePreference != null) {
            if (HSMusicApplication.getInstance().getPreferenceUtils().getEnableVisualizer()) {
                visualizerTypePreference.setVisible(true);
            } else {
                visualizerTypePreference.setVisible(false);
            }
        }

        if (visualizerSpeedPreference != null) {
            if (HSMusicApplication.getInstance().getPreferenceUtils().getEnableVisualizer()) {
                visualizerSpeedPreference.setVisible(true);
            } else {
                visualizerSpeedPreference.setVisible(false);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //For hiding the divider
        setDivider(new ColorDrawable(Color.TRANSPARENT));
        setDividerHeight(0);

        HSMusicApplication.getInstance().getPreferenceUtils().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if ("useMediaStyleNotification".equals(s)) {
            Preference showQueueInNotification = findPreference("showQueueInNotification");
            if (showQueueInNotification != null) {
                if (!sharedPreferences.getBoolean("useMediaStyleNotification", false)) {
                    showQueueInNotification.setVisible(true);
                } else {
                    showQueueInNotification.setVisible(false);
                }
            }
        } else if ("enableVisualizer".equals(s))
        {
            Preference visualizerTypePreference = findPreference("visualizerType");
            if (visualizerTypePreference != null) {
                if (sharedPreferences.getBoolean("enableVisualizer", true)) {
                    visualizerTypePreference.setVisible(true);
                } else {
                    visualizerTypePreference.setVisible(false);
                }
            }
            Preference visualizerSpeedPreference = findPreference("visualizerSpeed");
            if (visualizerSpeedPreference != null) {
                if (sharedPreferences.getBoolean("enableVisualizer", true)) {
                    visualizerSpeedPreference.setVisible(true);
                } else {
                    visualizerSpeedPreference.setVisible(false);
                }
            }
        }
        else if("circleImage".equals(s))
        {
            Preference circleImage = findPreference("circleImage");
            if (circleImage != null)
            {
                boolean t = ((SwitchPreference) circleImage).isChecked();
                HSMusicApplication.getInstance().getPreferenceUtils().setIsCircleImage(t);

            }
        }



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        HSMusicApplication.getInstance().getPreferenceUtils().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    private void setTheme(int position)
    {
            HSMusicApplication.getInstance().getPreferenceUtils().setTheme(position);

    }


}