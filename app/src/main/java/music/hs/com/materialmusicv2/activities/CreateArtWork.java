package music.hs.com.materialmusicv2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;

import butterknife.ButterKnife;
import music.hs.com.materialmusicv2.ChangeImage;
import music.hs.com.materialmusicv2.ColorPickerDialog;
import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.adapters.ChangeImageAdapter;
import music.hs.com.materialmusicv2.utils.colorutils.ColorUtils;
import music.hs.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.hs.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class CreateArtWork extends AppCompatActivity {

    private static EditText TopText;
    private static EditText DownText;
    private ExtendedFloatingActionButton Done;
    private static ImageView CenterImage;
    private static RelativeLayout Template;
    private Toolbar toolbar;
    private static ImageView TextColorImage;
    private static ImageView BgColorImage;
    public static String TopTextName;
    public static String DownTextName;
    public static int mode;
    public static int TextColor;
    public static int BGColor;
    private static ImageView ChangeCenterImage;
    private ConstraintLayout Background;

    private TextView TextColorTextview;
    private TextView BgColorTextview;
    private TextView ImageTextview;
    int colorAccent;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.createArtworkTheme);
        setTheme(getTheme());
        setContentView(R.layout.activity_create_art_work);

        Template = (RelativeLayout)findViewById(R.id.templateLayout);
        TopText = (EditText) findViewById(R.id.toptext);
        DownText = (EditText) findViewById(R.id.downText);
        CenterImage = (ImageView) findViewById(R.id.centerImage);
        ChangeCenterImage = (ImageView) findViewById(R.id.changeCenterImage);
        Done = (ExtendedFloatingActionButton) findViewById(R.id.done);
        TextColorImage = (ImageView) findViewById(R.id.textColorImage);
        BgColorImage = (ImageView) findViewById(R.id.bgColorImage);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Background = (ConstraintLayout)findViewById(R.id.background);

        TextColorTextview = (TextView)findViewById(R.id.textColor);
        BgColorTextview = (TextView)findViewById(R.id.bgColor);
        ImageTextview = (TextView)findViewById(R.id.changeCenterImageText);




        boolean t = ThemeUtils.isThemeDarkOrBlack();


        int colorPrimary = getThemePrimaryColor(this);
        if (t) {
            int[] drawble = {R.drawable.ic_arrow_back_black_24dp,R.drawable.ic_arrow_back_white_24dp};
            ToolbarUtils.setUpToolbar(toolbar,
                    "Create Artwork",
                    drawble,
                    Color.TRANSPARENT,
                    this,
                    this::onBackPressed);

            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            BgColorImage.setBackgroundColor(Color.parseColor("#FF3A3A3A"));
            CenterImage.setBackgroundColor(Color.parseColor("#FF3A3A3A"));
            TextColorTextview.setTextColor(Color.WHITE);
            BgColorTextview.setTextColor(Color.WHITE);
            ImageTextview.setTextColor(Color.WHITE);


        }
        else
        {
            int[] drawble = {R.drawable.ic_arrow_back_black_24dp,R.drawable.ic_arrow_back_white_24dp};
            ToolbarUtils.setUpToolbar(toolbar,
                    "Create Artwork",
                    drawble,
                    Color.WHITE,
                    this,
                    this::onBackPressed);

            Background.setBackgroundColor(Color.WHITE);
            BgColorImage.setBackgroundColor(Color.parseColor("#B8B8B8"));
            CenterImage.setBackgroundColor(Color.parseColor("#B8B8B8"));
            TextColorTextview.setTextColor(Color.BLACK);
            BgColorTextview.setTextColor(Color.BLACK);
            ImageTextview.setTextColor(Color.BLACK);

        }


        if (HSMusicApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(this, ContrastColor(colorPrimary) == Color.BLACK);
        }






        int pos = HSMusicApplication.getInstance().getPreferenceUtils().getAccentColor();
        ArrayList<Integer> accentColors = ThemeUtils.getAllAccentColors();
        colorAccent = accentColors.get(pos);

        TextColor = colorAccent;
        BGColor = Color.parseColor("#FF3A3A3A");

        TopTextName = "YOUR TEXT HERE";
        DownTextName = "YOUR TEXT HERE";

        //int colorAccent = ThemeUtils.getThemeAccentColor(this);
        TextColorImage.setBackgroundColor(colorAccent);
        TopText.setHintTextColor(colorAccent);
        DownText.setHintTextColor(colorAccent);
        setTextColor(colorAccent);


       // Paint mTextPaint = new Paint();


    }


    public void done(View view)
    {
            TopText.setEnabled(false);
            DownText.setEnabled(false);
            Template.setDrawingCacheEnabled(true);
            Bitmap bitmap = Template.getDrawingCache();
            saveImage(bitmap,"Artwork");

    }

    public void changeTextColor(View view)
    {
        ArrayList<Integer> accentColors = ThemeUtils.getAllAccentColors();
        /*DialogUtils.showColorChooserDialog(this, R.string.choose_accent_color,position ->
                setTextColor(accentColors.get(position))); */

        TopTextName = TopText.getText().toString();
        DownTextName = DownText.getText().toString();
        mode = 0;
        openColorPicker();


    }

    public void changeBGColor(View view)
    {
        ArrayList<Integer> accentColors = ThemeUtils.getAllAccentColors();
        /*(DialogUtils.showColorChooserDialog(this, R.string.choose_accent_color,position ->

        {
            CenterImage.setBackgroundColor(accentColors.get(position));
            BgColorImage.setBackgroundColor(accentColors.get(position));
        }); */
        TopTextName = TopText.getText().toString();
        DownTextName = DownText.getText().toString();
        mode = 1;
        openColorPicker();




    }



    private void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "HSMusicPlayer");
        directory.mkdirs();
        File path;
        File l[] = directory.listFiles();
        int len;
        String filename;
        if (l != null)
        {
            len = l.length;
            if (len != 0)
                filename = "image(" + Integer.toString(len) + ")";
            else
                filename = "image";
            path = new File(Environment.getExternalStorageDirectory() + File.separator + "HSMusicPlayer" + File.separator + filename);
            ;
        } else
            {
            filename = "image";
            path = new File(Environment.getExternalStorageDirectory() + File.separator + "HSMusicPlayer" + File.separator + filename);

        }
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "HSMusicPlayer");
        filename = filename +".jpg";
        File file = new File(dir, filename);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            galleryAddPic(dir);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Image Downloaded");
            builder.setMessage(path.getAbsolutePath().toString());
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(colorAccent);
            positiveButton.setBackgroundColor(Color.TRANSPARENT);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void openColorPicker()
    {
        ColorPickerDialog cdd = new ColorPickerDialog(this);
        cdd.show();
        WindowManager.LayoutParams layoutParams = cdd.getWindow().getAttributes();
        layoutParams.x = 100; // left margin
        cdd.getWindow().setAttributes(layoutParams);
        cdd.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }


    public void changeImage(View view)
    {
        ChangeImage cdd = new ChangeImage(this);
        cdd.show();
        WindowManager.LayoutParams layoutParams = cdd.getWindow().getAttributes();
        layoutParams.x = 100; // left margin
        cdd.getWindow().setAttributes(layoutParams);
        cdd.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

        public void setBGColor(int color)
    {
        BGColor = color;
        CenterImage.setBackgroundColor(color);
        BgColorImage.setBackgroundColor(color);
    }

    public void setTextColor(int col)
    {
        TextColor = col;
        ColorUtils.setColorFilter(col,CenterImage,ChangeCenterImage);
        TopText.setTextColor(col);
        DownText.setTextColor(col);
        TextColorImage.setBackgroundColor(col);
        TopText.setHintTextColor(col);
        DownText.setHintTextColor(col);

    }

    public void setImage(int img)
    {
        CenterImage.setImageResource(img);
        ChangeCenterImage.setImageResource(img);
        ColorUtils.setColorFilter(TextColor,CenterImage,ChangeCenterImage);
    }

    private void galleryAddPic(File path)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //File f = new File(path);
        Uri contentUri = Uri.fromFile(path);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }







}
