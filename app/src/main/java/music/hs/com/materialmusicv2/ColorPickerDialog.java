package music.hs.com.materialmusicv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import music.hs.com.materialmusicv2.activities.CreateArtWork;
import music.hs.com.materialmusicv2.utils.colorutils.ColorUtils;

public class ColorPickerDialog extends Dialog implements View.OnClickListener
{

    public ColorPickerDialog(@NonNull Context context) {
        super(context);
    }

    ColorPicker picker;
    SVBar svBar;
    OpacityBar opacityBar;
    ValueBar valueBar;

    ImageView centerImage;
    TextView TopText;
    TextView DownText;

    String TopTextName;
    String DownTextName;
    Button SelectButton;

    int TColor;
    int BGColor;
    SaturationBar saturationBar;

    CreateArtWork c;
    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_picker);


        c = new CreateArtWork();
        saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        SelectButton = (Button)findViewById(R.id.selectButton);
        SelectButton.setOnClickListener(this);
         picker = (ColorPicker) findViewById(R.id.picker);
        svBar = (SVBar) findViewById(R.id.svbar);
        opacityBar = (OpacityBar) findViewById(R.id.opacitybar);
        valueBar = (ValueBar) findViewById(R.id.valuebar);

        centerImage = (ImageView)findViewById(R.id.centerImage);
        TopText = (TextView)findViewById(R.id.toptext);
        DownText = (TextView)findViewById(R.id.downText);

        TopTextName = c.TopTextName;
        DownTextName = c.DownTextName;
        if(TopTextName.length() > 0)
            TopText.setText(TopTextName);
        if(DownTextName.length() > 0)
        DownText.setText(DownTextName);


        mode = c.mode;
        TopText.setTextColor(c.TextColor);
        DownText.setTextColor(c.TextColor);
        ColorUtils.setColorFilter(c.TextColor, centerImage);
        centerImage.setBackgroundColor(c.BGColor);



        picker.addSVBar(svBar);
        picker.addSaturationBar(saturationBar);

        picker.addOpacityBar(opacityBar);
        picker.addValueBar(valueBar);
        picker.getColor();
        picker.setOldCenterColor(picker.getColor());
       // picker.setOnColorChangedListener(this);

        picker.setShowOldCenterColor(false);
        //adding onChangeListeners to bars

        picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {

                if(mode == 0)
                {
                    TopText.setTextColor(color);
                    DownText.setTextColor(color);
                    ColorUtils.setColorFilter(color, centerImage);
                    TColor = color;

                }
                else
                {
                    centerImage.setBackgroundColor(color);
                    BGColor = color;
                }

            }
        });


    }

    @Override
    public void onClick(View v)
    {

        if(v == SelectButton)
        {
            if(mode == 0)
            {
                c.TextColor = TColor;
                c.setTextColor(TColor);
            }
            else
            {

                c.BGColor = BGColor;
                c.setBGColor(BGColor);
            }
            dismiss();
        }
    }
}
