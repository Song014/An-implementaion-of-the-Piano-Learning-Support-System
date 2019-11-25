package com.ckuict4th.ledguidedpiano;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SongSelectionActivity extends AppCompatActivity {

    static final String[] list_level1 = {"Butterfly Waltz"};
    static final String[] list_level2 = {"Moon River"};
    static final String[] list_level3 = {"언제나 몇번이라도"};

    private ListView listViewSongList;
    private int scoreLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_selection);

        // Set full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide Bottom bar
        uiWindowSetting();

        // List view
        listViewSongList = findViewById(R.id.listViewSongList);
        listViewSongList.setOnItemClickListener(songListClickListener);
    }

    public void uiWindowSetting() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    public void onButtonListClicked(View v) {
        super.onBackPressed();
    }

    public void onButtonLevel1Clicked(View v) {
        scoreLevel = 1;
        ShowScoreListLevel1();
    }

    public void onButtonLevel2Clicked(View v) {
        scoreLevel = 2;
        ShowScoreListLevel2();
    }

    public void onButtonLevel3Clicked(View v) {
        scoreLevel = 3;
        ShowScoreListLevel3();
    }

    public void ShowScoreListLevel1() {
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list_level1);
        listViewSongList.setAdapter(arrayAdapter);
    }

    public void ShowScoreListLevel2() {
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list_level2);
        listViewSongList.setAdapter(arrayAdapter);
    }

    public void ShowScoreListLevel3() {
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list_level3);
        listViewSongList.setAdapter(arrayAdapter);
    }

    ListView.OnItemClickListener songListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(SongSelectionActivity.this, ControlActivity.class);
            intent.putExtra("level", scoreLevel);
            intent.putExtra("song number", i);
            startActivityForResult(intent, 1);
        }
    };
}
