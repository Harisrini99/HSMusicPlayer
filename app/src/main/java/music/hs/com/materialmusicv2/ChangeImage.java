package music.hs.com.materialmusicv2;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import music.hs.com.materialmusicv2.activities.CreateArtWork;
import music.hs.com.materialmusicv2.adapters.ChangeImageAdapter;

public class ChangeImage extends Dialog implements View.OnClickListener//implements AdapterView.OnItemClickListener//implements View.OnClickListener
{

    private GridView gridView;
    private ArrayList<Integer> images ;
    private CreateArtWork c;
    private ChangeImageAdapter changeImageAdapter;
    private Button SelectButton;

    public ChangeImage(@NonNull Context context) {
        super(context);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_image);

        c = new CreateArtWork();

        images = new ArrayList<>();
        images.add(R.drawable.ic_template_cd);
        images.add(R.drawable.ic_favorite_black_24dp);
        images.add(R.drawable.ic_favorite_border_black_24dp);
        images.add(R.drawable.ic_home);
        images.add(R.drawable.ic_shuffle_black_24dp);
        images.add(R.drawable.ic_repeat_black_24dp);
        images.add(R.drawable.ic_album_black_24dp);
        images.add(R.drawable.ic_audiotrack_black_24dp);
        images.add(R.drawable.ic_blacklist);
        images.add(R.drawable.ic_cd);
        images.add(R.drawable.ic_check_black_24dp);
        images.add(R.drawable.ic_close_black_24dp);
        images.add(R.drawable.ic_delete_black_24dp);
        images.add(R.drawable.ic_drag_handle_black_24dp);
        images.add(R.drawable.ic_edit_black);
        images.add(R.drawable.ic_equalizer_black_24dp);
        images.add(R.drawable.ic_error_outline_white_24dp);
        images.add(R.drawable.ic_folder_black_24dp);
        images.add(R.drawable.ic_info_outline_black_24dp);
        images.add(R.drawable.ic_keyboard_black_24dp);
        images.add(R.drawable.ic_genre_black_24dp);
        images.add(R.drawable.ic_library_music_black_24dp);
        images.add(R.drawable.ic_history_black_24dp);
        images.add(R.drawable.ic_most_played_black_24dp);
        images.add(R.drawable.ic_notifications_black_24dp);
        images.add(R.drawable.ic_mail_outline_black_24dp);
        images.add(R.drawable.ic_lyrics_black_24dp);
        images.add(R.drawable.ic_open_playlist_black_24dp);
        images.add(R.drawable.ic_pause_black_24dp);
        images.add(R.drawable.ic_person_black_24dp);
        images.add(R.drawable.ic_play_arrow_black_24dp);
        images.add(R.drawable.ic_search_black_24dp);
        images.add(R.drawable.ic_save_black_24dp);
        images.add(R.drawable.ic_share_black_24dp);
        images.add(R.drawable.ic_queue_music_black_24dp);
        images.add(R.drawable.ic_settings_black_24dp);
        images.add(R.drawable.ic_skip_next_black_24dp);
        images.add(R.drawable.ic_skip_previous_black_24dp);
        images.add(R.drawable.ic_sort_black_24dp);
        images.add(R.drawable.ic_star_border_black_24dp);
        images.add(R.drawable.ic_start_page_logo);
        images.add(R.drawable.ic_timer_black_24dp);
        images.add(R.drawable.ic_twitter_bird);
        images.add(R.drawable.ic_paint);
        images.add(R.drawable.ic_thumb);


        gridView = (GridView) findViewById(R.id.imageGrid);
        SelectButton = (Button)findViewById(R.id.selectButton);
        SelectButton.setOnClickListener(this);

            changeImageAdapter = new ChangeImageAdapter(getContext(),images,c.TextColor);
        gridView.setAdapter(changeImageAdapter);






    }




    @Override
    public void onClick(View v) {
        if(v == SelectButton)
        {
            dismiss();
        }
    }
}
