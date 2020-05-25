package music.hs.com.materialmusicv2.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.HSMusicApplication;

import static music.hs.com.materialmusicv2.utils.misc.Etc.TOAST_NORMAL;
import static music.hs.com.materialmusicv2.utils.misc.Etc.postToast;

public class HSIntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SliderPage frontPage = new SliderPage();
        frontPage.setTitle("HS Music");
        frontPage.setDescription(getString(R.string.about_symphony_text));
        frontPage.setImageDrawable(R.drawable.start_page_logo);
        frontPage.setBgColor(ContextCompat.getColor(getApplicationContext(), R.color.start_Page_color));
        addSlide(AppIntro2Fragment.newInstance(frontPage));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SliderPage permissionsPage = new SliderPage();
            permissionsPage.setTitle(getString(R.string.permission));
            permissionsPage.setTitleColor(Color.WHITE);
            permissionsPage.setDescription(getString(R.string.permissions_text));
            permissionsPage.setDescColor(Color.WHITE);
            permissionsPage.setImageDrawable(R.drawable.permission);
            permissionsPage.setBgColor(ContextCompat.getColor(getApplicationContext(), R.color.start_Page_color));
            addSlide(AppIntro2Fragment.newInstance(permissionsPage));
            askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        showSkipButton(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        if (!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {
            postToast(R.string.kindly_grant_permissions, getApplicationContext(), TOAST_NORMAL);
            new Handler().postDelayed(() -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0), 500);
        } else {
            HSMusicApplication.getInstance().getPreferenceUtils().setIsIntroShown(true);
            finish();
        }
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}