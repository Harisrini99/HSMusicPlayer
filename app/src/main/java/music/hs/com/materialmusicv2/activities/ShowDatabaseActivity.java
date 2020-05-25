package music.hs.com.materialmusicv2.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import music.hs.com.materialmusicv2.HSMusicApplication;
import music.hs.com.materialmusicv2.R;
import music.hs.com.materialmusicv2.ShowDatabaseObject;
import music.hs.com.materialmusicv2.adapters.ShowDatabaseAdapter;
import music.hs.com.materialmusicv2.objects.Song;
import music.hs.com.materialmusicv2.utils.controller.Controller;
import music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.hs.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;
import static music.hs.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class ShowDatabaseActivity extends AppCompatActivity implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener
{



    private GridView gridView;
    private ArrayList<ShowDatabaseObject> list = new ArrayList<>();
    ShowDatabaseAdapter showDatabaseAdapter;
    ImageButton BackToolbar;
    TextView Title;
    int colorAccent;
    int colorContrast;
    EditText Search;
    ImageButton SearchButton;
    RelativeLayout SearchLayout;
    RelativeLayout ToolbarLayout;
    ImageButton CancelButton;
    MaterialCardView ToolBarCard;
    SwipeRefreshLayout SwipeRefreshLayout;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeUtils.getTheme(this));
        setContentView(R.layout.activity_show_database);


        boolean t = ThemeUtils.isThemeDarkOrBlack();


        int colorPrimary = getThemePrimaryColor(this);
        if (t) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }


        if (HSMusicApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(this, ContrastColor(colorPrimary) == Color.BLACK);
        }



        gridView = (GridView) findViewById(R.id.songGrid);
        BackToolbar = (ImageButton) findViewById(R.id.back_toolbar);
        CancelButton = (ImageButton) findViewById(R.id.cancelButton);
        Title = (TextView)findViewById(R.id.title);
        Search = (EditText)findViewById(R.id.search);
        SearchButton =(ImageButton)findViewById(R.id.search_button);
        SearchLayout = (RelativeLayout)findViewById(R.id.searchLayout);
        ToolbarLayout = (RelativeLayout)findViewById(R.id.toolbarLayout);
        ToolBarCard = (MaterialCardView) findViewById(R.id.toolbar_card);
        SwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        SearchLayout.setVisibility(View.GONE);
        SwipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        SwipeRefreshLayout.setColorSchemeColors(Color.BLACK);
        SwipeRefreshLayout.setOnRefreshListener(this);
        SwipeRefreshLayout.setRefreshing(true);
        SearchButton.setVisibility(View.INVISIBLE);



        Search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showDatabaseAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        colorAccent = MainActivity.colorAccent;
        colorPrimary = MainActivity.colorPrimary;
        colorContrast = ContrastColor(colorPrimary);

        setColorFilter(colorAccent, BackToolbar,SearchButton);
        SearchButton.setOnClickListener(this);
        BackToolbar.setOnClickListener(this);
        CancelButton.setOnClickListener(this);
        Title.setTextColor(colorContrast);




        DatabaseReference imagesQuery = FirebaseDatabase.getInstance().getReference().child("Song Details");
        imagesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    HashMap<String, String> map = (HashMap<String, String>) dataSnapshot1.getValue();
                    String name = map.get("Name");
                    String artist = map.get("Artist");
                    String duration = map.get("Duration");
                    String lyrics = map.get("Lyrics");
                    ShowDatabaseObject o = new ShowDatabaseObject(name,artist,duration,lyrics);
                    list.add(o);
                }
                showDatabaseAdapter = new ShowDatabaseAdapter(ShowDatabaseActivity.this,list);
                gridView.setAdapter(showDatabaseAdapter);
                SearchButton.setVisibility(View.VISIBLE);
                ToolBarCard.setCardBackgroundColor(colorAccent);
                ToolbarLayout.setVisibility(View.GONE);
                ToolBarCard.setStrokeWidth(0);
                SearchLayout.setVisibility(View.VISIBLE);
                Song s = Controller.getCurrentSong();
                String t = s.getName().substring(0,4);
                Search.setText(t);

                SwipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onClick(View v) {

        if(v == BackToolbar)
        {
            onBackPressed();
        }

        if(v == SearchButton)
        {
            ToolBarCard.setCardBackgroundColor(colorAccent);
            ToolbarLayout.setVisibility(View.GONE);
            Search.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }
            ToolBarCard.setStrokeWidth(0);
            circleReveal(R.id.searchLayout, 1, true, true);
        }
        if( v == CancelButton)
        {
            SearchLayout.setVisibility(View.GONE);
            ToolBarCard.setStrokeWidth(1);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
            }
            ToolBarCard.setCardBackgroundColor(Color.TRANSPARENT);
            ToolbarLayout.setVisibility(View.VISIBLE);
        }

    }

    public void circleReveal(int viewID, int posFromRight, boolean containsOverflow, final boolean isShow) {
        final View myView = findViewById(viewID);
        final View view = findViewById(R.id.toolbar);
        int width = myView.getWidth();

        if (posFromRight > 0)
            width -= (posFromRight * getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)) - (getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) / 2);
        if (containsOverflow)
            width -= getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material);

        int cx = width;
        int cy = myView.getHeight() / 2;

        Animator anim;
        if (isShow)
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, (float) width);
        else
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, (float) width, 0);

        anim.setDuration((long) 220);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // make the view visible and start the animation
        if (isShow) {
            myView.setVisibility(View.VISIBLE);
        }
        // start the animation
        anim.start();


    }

    private void loadData()
    {
        DatabaseReference imagesQuery = FirebaseDatabase.getInstance().getReference().child("Song Details");
        imagesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    HashMap<String, String> map = (HashMap<String, String>) dataSnapshot1.getValue();
                    String name = map.get("Name");
                    String artist = map.get("Artist");
                    String duration = map.get("Duration");
                    String lyrics = map.get("Lyrics");
                    ShowDatabaseObject o = new ShowDatabaseObject(name,artist,duration,lyrics);
                    list.add(o);
                }
                showDatabaseAdapter = new ShowDatabaseAdapter(ShowDatabaseActivity.this,list);
                gridView.setAdapter(showDatabaseAdapter);
                SwipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRefresh()
    {
        SwipeRefreshLayout.setRefreshing(true);
        gridView.setAdapter(null);
        loadData();
    }
}
