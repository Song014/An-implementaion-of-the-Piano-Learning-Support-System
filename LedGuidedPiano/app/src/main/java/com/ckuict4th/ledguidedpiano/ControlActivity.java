package com.ckuict4th.ledguidedpiano;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class ControlActivity extends AppCompatActivity implements  ScopeLogger{

    // Configuration
    public static final int PLAY_LED_DELAY = 1000;


    // Midi
    private MidiOutputPortSelector mLogSenderSelector;
    private MidiManager mMidiManager;
    private MidiReceiver mLoggingReceiver;
    private MidiFramer mConnectFramer;
    private MyDirectReceiver mDirectReceiver;

    // Layout
    ImageView imageViewScore;
    TextView textViewScoreTitle;
    TextView textViewPianoUsbLog;
    ImageButton imageButtonPlay;
    ImageButton imageButtonRepeat;
    ImageButton imageButtonGuide;
    ImageView imageMark;

    // Variable
    // 0: stop
    // 1: play
    // 2: pause
    public int playState = 0;
    public boolean isRepeatState = false;
    public int isGuideMode = 0;
    public byte[] ledId = {
            1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22,
            24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 45,
            47, 49, 51, 53, 56, 58, 59, 61, 63, 65, 67, 69,
            71, 73, 75, 77, 79, 81, 83, 85, 86, 88, 90, 92,
            94, 96, 98, 100, 102, 104, 106, 108, 110, 112, 114, 116,
            118
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // Set full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide Bottom bar
        uiWindowSetting();

        // Layout variable
        imageViewScore = findViewById(R.id.imageViewScore);
        textViewScoreTitle = findViewById(R.id.textViewScoreTitle);
        textViewPianoUsbLog = findViewById(R.id.textViewPianoUsbLog);
        imageButtonPlay = findViewById(R.id.imageButtonPlay);
        imageButtonRepeat = findViewById(R.id.imageButtonRepeat);
        imageButtonGuide = findViewById(R.id.imageButtonGuide);
        imageMark = findViewById(R.id.imageViewMark);

        // Setup MIDI
        mMidiManager = (MidiManager) getSystemService(MIDI_SERVICE);

        // Receiver that prints the messages.
        mLoggingReceiver = new LoggingReceiver(this);

        // Receivers that parses raw data into complete messages.
        mConnectFramer = new MidiFramer(mLoggingReceiver);

        // Setup a menu to select an input source.
        mLogSenderSelector = new MidiOutputPortSelector(mMidiManager, this,
                R.id.spinner_senders) {

            @Override
            public void onPortSelected(final MidiPortWrapper wrapper) {
                super.onPortSelected(wrapper);
                if (wrapper != null) {
                    //log(MidiPrinter.formatDeviceInfo(wrapper.getDeviceInfo()));
                }
            }
        };

        mDirectReceiver = new MyDirectReceiver();
        mLogSenderSelector.getSender().connect(mDirectReceiver);

        // Tell the virtual device to log its messages here..
        MidiScope.setScopeLogger(this);

        // Score data init
        Intent intent = getIntent();
        int scoreLevel = intent.getIntExtra("level", 1);
        int songNumber = intent.getIntExtra("song number", 0);

        switch(scoreLevel) {
            case 1:
                switch(songNumber) {
                    case 0:
                        ScoreData1Init();
                        break;
                }
                break;
            case 2:
                switch(songNumber) {
                    case 0:
                        ScoreData2Init();
                        break;
                }
                break;
            case 3:
                switch(songNumber) {
                    case 0:
                        ScoreData3Init();
                        break;
                }
                break;
        }
    }

    public void uiWindowSetting() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onDestroy() {
        // Turn off led strip
        ((MainActivity)MainActivity.mContext).btc.AllLedOff();

        mLogSenderSelector.onClose();
        // The scope will live on as a service so we need to tell it to stop
        // writing log messages to this Activity.
        MidiScope.setScopeLogger(null);
        super.onDestroy();
    }

    public void onButtonListClicked(View v) {
        // Turn off led strip
        ((MainActivity)MainActivity.mContext).btc.AllLedOff();
        super.onBackPressed();
    }

    public void onButtonPlayClicked(View v) {
        // Stop --> Play
        if(playState == 0) {
            // Make play state
            playState = 1;
            Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show();

            // Image change
            imageButtonPlay.setImageDrawable(getDrawable(R.drawable.button_pause));

            // Start
            StartPlayLed();
        }
        // Play --> Pause
        else if(playState == 1){
            // Make pause state
            playState = 2;
            Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();

            // Image change
            imageButtonPlay.setImageDrawable(getDrawable(R.drawable.button_play));

            // Pause
            PausePlayLed();
        }
        // Pause --> Resume
        else if(playState == 2){
            // Make play state
            playState = 1;
            Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show();

            // Image change
            imageButtonPlay.setImageDrawable(getDrawable(R.drawable.button_pause));

            // Resume
            ResumePlayLed();
        }

    }

    public void onButtonStopClicked(View v) {
        // Play --> Stop
        if(playState == 1) {
            playState = 0;
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();

            // Image change
            imageButtonPlay.setImageDrawable(getDrawable(R.drawable.button_play));

            // Stop
            StopPlayLed();
        }
        // Pause --> Stop
        else if(playState == 2) {
            playState = 0;
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();

            // Image change
            imageButtonPlay.setImageDrawable(getDrawable(R.drawable.button_play));

            // Stop
            StopPlayLed();
        }
    }

    public void onButtonRepeatClicked(View v) {
        if(isRepeatState == false) {
            isRepeatState = true;
            Toast.makeText(this, "Repeat Mode Start", Toast.LENGTH_SHORT).show();

            // Image change
            imageButtonRepeat.setImageDrawable(getDrawable(R.drawable.button_repeat_yellow));
        }
        else {
            isRepeatState = false;
            Toast.makeText(this, "Repeat Mode Stop", Toast.LENGTH_SHORT).show();

            // Image change
            imageButtonRepeat.setImageDrawable(getDrawable(R.drawable.button_repeat));
        }
    }

    public void onButtonGuideClicked(View v) {
        if(isGuideMode == 0) {
            isGuideMode = 1;
            Toast.makeText(this, "Guide Mode", Toast.LENGTH_SHORT).show();

            // Image change
            imageButtonGuide.setImageDrawable(getDrawable(R.drawable.button_guide_yellow));

            // Start
            StartGuide();
        }
        else {
            isGuideMode = 0;
            Toast.makeText(this, "Guide Mode Stop", Toast.LENGTH_SHORT).show();

            // Image change
            imageButtonGuide.setImageDrawable(getDrawable(R.drawable.button_guide));

            // Stop
            StopGuide();
        }
    }

    public void onButtonTestClicked(View v) {

    }

    //-------------------------------------------------------------------------------//
    //
    //                             CONTROL
    //
    //-------------------------------------------------------------------------------//
    public ArrayList<Byte> currentStageData = new ArrayList<>();
    public int scoreDataIdx = 0;
    public int currentStageKeyCount = 0;

    public void StartGuide() {
        ResetStageControlData();
        StartNextStage();
    }

    public void PauseGuide() {
        if(scoreDataIdx > 0) {
            scoreDataIdx--;
        }
    }

    public void ResumeGuide() {
        StartNextStage();
    }

    public void StopGuide() {
        // Turn off all led
        ((MainActivity)MainActivity.mContext).btc.AllLedOff();
        ((MainActivity)MainActivity.mContext).btc.LedUpdate();

        // Mark position
        SetMarkPosition(-100);
    }

    public void ResetStageControlData() {
        scoreDataIdx = 0;
    }

    public void StartNextStage() {
        // Turn off all led
        ((MainActivity)MainActivity.mContext).btc.AllLedOff();

        // Check for next stage existence
        if(scoreDataIdx == numOfScoreData) {
            if(isRepeatState == true) {
                scoreDataIdx = 0;
            }
            else {
                // Stop
                log("END");
                //onButtonGuideClicked(findViewById(R.id.imageButtonGuide));
                StopGuide();
                return;
            }
        }

        // Current stage list update
        currentStageKeyCount = scoreData[scoreDataIdx][0];

        currentStageData.clear();
        for(int i = 0; i < currentStageKeyCount; i++) {
            currentStageData.add(scoreData[scoreDataIdx][1 + i]);
        }

        // Update mark image position
        SetMarkPosition(scoreMarkPosition[scoreDataIdx]);

        // Score data index increase
        scoreDataIdx++;

        // Current stage led turn on
        for(int i = 0; i < currentStageData.size(); i++) {
            int id = currentStageData.get(i) - 36;
            if((id < 0) || (id > ledId.length)) {
                log("Wrong score data");
                return;
            }
            ((MainActivity)MainActivity.mContext).btc.LedControl(ledId[id], TURN_ON);
        }

        ((MainActivity)MainActivity.mContext).btc.LedUpdate();
    }

    //
    // Check the input key data when key input exists.
    // data set has 3 byte and 3rd data is key value.
    // If it has several key data, its index is 2, 5, 8, ...
    //
    public void CheckPianoKeyInput(byte[] data, int count) {
        if(isGuideMode == 1) {
            // Press motion only
            if(data[1] == 0x90) {
                return;
            }

            if(currentStageData.size() == 0) {
                return;
            }

            // Number of key input
            int numOfKey = count / 3;

            // Compare key input and current stage note data
            for(int i = 0; i < numOfKey; i++) {
                for(int j = 0; j < currentStageKeyCount; j++) {

                    if(data[2+3*i] == currentStageData.get(j)) {
                        currentStageData.remove(j);
                        currentStageKeyCount--;
                    }
                }
            }

            // Every note clear, start next stage.
            if(currentStageData.size() == 0) {
                StartNextStage();
            }
        }
    }

    public void SetMarkPosition(final int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SetMarkPositionFromUiThread(position);
            }
        });
    }

    //
    // position: dp
    //
    private void SetMarkPositionFromUiThread(int position) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int margin = Math.round(position * dm.density);
        imageMark.setX(margin);
    }


    public ArrayList<Byte> currentPlayLedStageData = new ArrayList<>();
    public int currentPlayLedStageCount = 0;
    public int playLedIdx = 0;
    public boolean isPlayLedHandlerRunning = false;

    public void StartPlayLed() {
        if(isPlayLedHandlerRunning == false)
        {
            ResetPlayLedControlData();
            isPlayLedHandlerRunning = true;
            mPlayLedHandler.sendEmptyMessage(0);
        }
    }

    public void PausePlayLed() {
        if(isPlayLedHandlerRunning == true)
        {
            isPlayLedHandlerRunning = false;
            mPlayLedHandler.removeMessages(0);

            if(playLedIdx > 0) {
                playLedIdx--;
            }
        }
    }

    public void ResumePlayLed() {
        if(isPlayLedHandlerRunning == false)
        {
            isPlayLedHandlerRunning = true;
            mPlayLedHandler.sendEmptyMessage(0);
        }
    }

    public void StopPlayLed() {
        if(isPlayLedHandlerRunning == true)
        {
            isPlayLedHandlerRunning = false;
            mPlayLedHandler.removeMessages(0);

            // Turn off all led
            ((MainActivity)MainActivity.mContext).btc.AllLedOff();
            ((MainActivity)MainActivity.mContext).btc.LedUpdate();

            // Mark position
            SetMarkPosition(-100);

            ResetPlayLedControlData();
        }
    }

    public void ResetPlayLedControlData() {
        playLedIdx = 0;
    }

    @SuppressLint("HandlerLeak")
    Handler mPlayLedHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            // Turn off all led
            ((MainActivity)MainActivity.mContext).btc.AllLedOff();

            // Check for next stage existence
            if(playLedIdx == numOfScoreData) {
                if(isRepeatState == true) {
                    playLedIdx = 0;
                }
                else {
                    // Stop
                    log("END");
                    ResetStageControlData();
                    onButtonStopClicked(findViewById(R.id.imageButtonStop));
                    return;
                }
            }

            // Current play led stage list update
            currentPlayLedStageCount = scoreData[playLedIdx][0];

            currentPlayLedStageData.clear();
            for(int i = 0; i < currentPlayLedStageCount; i++) {
                currentPlayLedStageData.add(scoreData[playLedIdx][1 + i]);
            }

            // Update mark image position
            SetMarkPosition(scoreMarkPosition[playLedIdx]);

            // Score data index increase
            playLedIdx++;

            for(int i = 0; i < currentPlayLedStageData.size(); i++) {
                int id = currentPlayLedStageData.get(i) - 36;
                if((id < 0) || (id > ledId.length)) {
                    return;
                }
                ((MainActivity)MainActivity.mContext).btc.LedControl(ledId[id], TURN_ON);
            }

            ((MainActivity)MainActivity.mContext).btc.LedUpdate();

            mPlayLedHandler.sendEmptyMessageDelayed(0, PLAY_LED_DELAY);
        }
    };

    //-------------------------------------------------------------------------------//
    //
    //                             SCORE DATA
    //
    //-------------------------------------------------------------------------------//
    public byte[][] scoreData = new byte[100][5];
    public int numOfScoreData = 0;
    public int[] scoreMarkPosition = new int[100];

    //
    // 0: count
    // 1~4: key value
    //
    public void ScoreData1Init() {
        // Title
        textViewScoreTitle.setText("Butterfly Waltz");

        // Score image
        imageViewScore.setImageResource(R.drawable.note_level_1);

        // Bar 1
        scoreData[0][0] = 1;
        scoreData[0][1] = 0x45;

        scoreData[1][0] = 1;
        scoreData[1][1] = 0x46;

        // Bar 2
        scoreData[2][0] = 2;
        scoreData[2][1] = 0x30;
        scoreData[2][2] = 0x48;
        scoreData[3][0] = 1;
        scoreData[3][1] = 0x35;
        scoreData[4][0] = 2;
        scoreData[4][1] = 0x39;
        scoreData[4][2] = 0x41;
        scoreData[5][0] = 1;
        scoreData[5][1] = 0x40;

        // Bar 3
        scoreData[6][0] = 2;
        scoreData[6][1] = 0x2D;
        scoreData[6][2] = 0x41;
        scoreData[7][0] = 1;
        scoreData[7][1] = 0x30;
        scoreData[8][0] = 2;
        scoreData[8][1] = 0x34;
        scoreData[8][2] = 0x48;

        // Bar 4
        scoreData[9][0] = 2;
        scoreData[9][1] = 0x2E;
        scoreData[9][2] = 0x48;
        scoreData[10][0] = 1;
        scoreData[10][1] = 0x32;
        scoreData[11][0] = 1;
        scoreData[11][1] = 0x4A;
        scoreData[12][0] = 2;
        scoreData[12][1] = 0x35;
        scoreData[12][2] = 0x46;
        scoreData[13][0] = 1;
        scoreData[13][1] = 0x45;

        // Bar 5
        scoreData[14][0] = 2;
        scoreData[14][1] = 0x30;
        scoreData[14][2] = 0x46;
        scoreData[15][0] = 1;
        scoreData[15][1] = 0x34;
        scoreData[16][0] = 2;
        scoreData[16][1] = 0x37;
        scoreData[16][2] = 0x45;
        scoreData[17][0] = 1;
        scoreData[17][1] = 0x46;

        // Bar 6
        scoreData[18][0] = 2;
        scoreData[18][1] = 0x30;
        scoreData[18][2] = 0x48;
        scoreData[19][0] = 1;
        scoreData[19][1] = 0x35;
        scoreData[20][0] = 2;
        scoreData[20][1] = 0x39;
        scoreData[20][2] = 0x41;
        scoreData[21][0] = 1;
        scoreData[21][1] = 0x40;

        // Bar 7
        scoreData[22][0] = 2;
        scoreData[22][1] = 0x2D;
        scoreData[22][2] = 0x41;
        scoreData[23][0] = 1;
        scoreData[23][1] = 0x30;
        scoreData[24][0] = 2;
        scoreData[24][1] = 0x34;
        scoreData[24][2] = 0x45;

        // Bar 8
        scoreData[25][0] = 2;
        scoreData[25][1] = 0x2E;
        scoreData[25][2] = 0x3E;
        scoreData[26][0] = 1;
        scoreData[26][1] = 0x32;
        scoreData[27][0] = 1;
        scoreData[27][1] = 0x35;

        // Bar 9
        scoreData[28][0] = 2;
        scoreData[28][1] = 0x30;
        scoreData[28][2] = 0x40;
        scoreData[29][0] = 1;
        scoreData[29][1] = 0x34;
        scoreData[30][0] = 2;
        scoreData[30][1] = 0x37;
        scoreData[30][2] = 0x45;
        scoreData[31][0] = 1;
        scoreData[31][1] = 0x46;

        // Bar 10
        scoreData[32][0] = 2;
        scoreData[32][1] = 0x30;
        scoreData[32][2] = 0x48;
        scoreData[33][0] = 1;
        scoreData[33][1] = 0x35;
        scoreData[34][0] = 2;
        scoreData[34][1] = 0x39;
        scoreData[34][2] = 0x41;
        scoreData[35][0] = 1;
        scoreData[35][1] = 0x40;

        // Bar 11
        scoreData[36][0] = 2;
        scoreData[36][1] = 0x2D;
        scoreData[36][2] = 0x41;
        scoreData[37][0] = 1;
        scoreData[37][1] = 0x30;
        scoreData[38][0] = 2;
        scoreData[38][1] = 0x34;
        scoreData[38][2] = 0x48;

        // Bar 12
        scoreData[39][0] = 2;
        scoreData[39][1] = 0x2E;
        scoreData[39][2] = 0x48;
        scoreData[40][0] = 1;
        scoreData[40][1] = 0x32;
        scoreData[41][0] = 1;
        scoreData[41][1] = 0x4A;
        scoreData[42][0] = 2;
        scoreData[42][1] = 0x35;
        scoreData[42][2] = 0x46;
        scoreData[43][0] = 1;
        scoreData[43][1] = 0x45;

        // Bar 13
        scoreData[44][0] = 2;
        scoreData[44][1] = 0x30;
        scoreData[44][2] = 0x46;
        scoreData[45][0] = 1;
        scoreData[45][1] = 0x34;
        scoreData[46][0] = 2;
        scoreData[46][1] = 0x37;
        scoreData[46][2] = 0x45;
        scoreData[47][0] = 1;
        scoreData[47][1] = 0x46;

        // Bar 14
        scoreData[48][0] = 2;
        scoreData[48][1] = 0x30;
        scoreData[48][2] = 0x48;
        scoreData[49][0] = 1;
        scoreData[49][1] = 0x35;
        scoreData[50][0] = 2;
        scoreData[50][1] = 0x39;
        scoreData[50][2] = 0x41;
        scoreData[51][0] = 1;
        scoreData[51][1] = 0x40;

        // Bar 15
        scoreData[52][0] = 2;
        scoreData[52][1] = 0x2D;
        scoreData[52][2] = 0x41;
        scoreData[53][0] = 1;
        scoreData[53][1] = 0x30;
        scoreData[54][0] = 2;
        scoreData[54][1] = 0x34;
        scoreData[54][2] = 0x45;

        // Bar 16
        scoreData[55][0] = 2;
        scoreData[55][1] = 0x2E;
        scoreData[55][2] = 0x3E;
        scoreData[56][0] = 1;
        scoreData[56][1] = 0x32;
        scoreData[57][0] = 1;
        scoreData[57][1] = 0x35;

        // Bar 17
        scoreData[58][0] = 2;
        scoreData[58][1] = 0x30;
        scoreData[58][2] = 0x40;
        scoreData[59][0] = 1;
        scoreData[59][1] = 0x34;
        scoreData[60][0] = 2;
        scoreData[60][1] = 0x37;
        scoreData[60][2] = 0x45;
        scoreData[61][0] = 1;
        scoreData[61][1] = 0x46;

        // Number of score data
        numOfScoreData = 62;

        // Score mark position
        scoreMarkPosition[0] = 104;
        scoreMarkPosition[1] = 116;
        scoreMarkPosition[2] = 145;
        scoreMarkPosition[3] = 161;
        scoreMarkPosition[4] = 178;
        scoreMarkPosition[5] = 188;
        scoreMarkPosition[6] = 208;
        scoreMarkPosition[7] = 222;
        scoreMarkPosition[8] = 238;
        scoreMarkPosition[9] = 260;
        scoreMarkPosition[10] = 277;
        scoreMarkPosition[11] = 287;
        scoreMarkPosition[12] = 298;
        scoreMarkPosition[13] = 308;
        scoreMarkPosition[14] = 327;
        scoreMarkPosition[15] = 343;
        scoreMarkPosition[16] = 359;
        scoreMarkPosition[17] = 370;
        scoreMarkPosition[18] = 389;
        scoreMarkPosition[19] = 405;
        scoreMarkPosition[20] = 420;
        scoreMarkPosition[21] = 430;
        scoreMarkPosition[22] = 450;
        scoreMarkPosition[23] = 466;
        scoreMarkPosition[24] = 481;
        scoreMarkPosition[25] = 504;
        scoreMarkPosition[26] = 519;
        scoreMarkPosition[27] = 534;
        scoreMarkPosition[28] = 556;
        scoreMarkPosition[29] = 572;
        scoreMarkPosition[30] = 589;
        scoreMarkPosition[31] = 599;
        scoreMarkPosition[32] = 145;
        scoreMarkPosition[33] = 161;
        scoreMarkPosition[34] = 178;
        scoreMarkPosition[35] = 188;
        scoreMarkPosition[36] = 208;
        scoreMarkPosition[37] = 222;
        scoreMarkPosition[38] = 238;
        scoreMarkPosition[39] = 260;
        scoreMarkPosition[40] = 277;
        scoreMarkPosition[41] = 287;
        scoreMarkPosition[42] = 298;
        scoreMarkPosition[43] = 308;
        scoreMarkPosition[44] = 327;
        scoreMarkPosition[45] = 343;
        scoreMarkPosition[46] = 359;
        scoreMarkPosition[47] = 370;
        scoreMarkPosition[48] = 389;
        scoreMarkPosition[49] = 405;
        scoreMarkPosition[50] = 420;
        scoreMarkPosition[51] = 430;
        scoreMarkPosition[52] = 450;
        scoreMarkPosition[53] = 466;
        scoreMarkPosition[54] = 481;
        scoreMarkPosition[55] = 504;
        scoreMarkPosition[56] = 519;
        scoreMarkPosition[57] = 534;
        scoreMarkPosition[58] = 556;
        scoreMarkPosition[59] = 572;
        scoreMarkPosition[60] = 589;
        scoreMarkPosition[61] = 599;

    }

    public void ScoreData2Init() {
        // Title
        textViewScoreTitle.setText("Moon River");

        // Score image
        imageViewScore.setImageResource(R.drawable.note_level_2);

        // Bar 1
        scoreData[0][0] = 1;
        scoreData[0][1] = 0x24;
        scoreData[1][0] = 1;
        scoreData[1][1] = 0x2B;
        scoreData[2][0] = 1;
        scoreData[2][1] = 0x34;
        scoreData[3][0] = 1;
        scoreData[3][1] = 0x2B;

        // Bar 2
        scoreData[4][0] = 1;
        scoreData[4][1] = 0x24;
        scoreData[5][0] = 1;
        scoreData[5][1] = 0x2B;
        scoreData[6][0] = 1;
        scoreData[6][1] = 0x34;
        scoreData[7][0] = 1;
        scoreData[7][1] = 0x2B;

        // Bar 3
        scoreData[8][0] = 2;
        scoreData[8][1] = 0x24;
        scoreData[8][2] = 0x43;
        scoreData[9][0] = 1;
        scoreData[9][1] = 0x2B;
        scoreData[10][0] = 1;
        scoreData[10][1] = 0x34;
        scoreData[11][0] = 1;
        scoreData[11][1] = 0x24;

        // Bar 4
        scoreData[12][0] = 1;
        scoreData[12][1] = 0x4A;
        scoreData[13][0] = 1;
        scoreData[13][1] = 0x28;
        scoreData[14][0] = 2;
        scoreData[14][1] = 0x30;
        scoreData[14][2] = 0x48;
        scoreData[15][0] = 1;
        scoreData[15][1] = 0x34;
        scoreData[16][0] = 1;
        scoreData[16][1] = 0x39;


        // Number of score data
        numOfScoreData = 17;

        // Score mark position
        scoreMarkPosition[0] = 102;
        scoreMarkPosition[1] = 126;
        scoreMarkPosition[2] = 149;
        scoreMarkPosition[3] = 188;
        scoreMarkPosition[4] = 234;
        scoreMarkPosition[5] = 259;
        scoreMarkPosition[6] = 283;
        scoreMarkPosition[7] = 324;
        scoreMarkPosition[8] = 380;
        scoreMarkPosition[9] = 405;
        scoreMarkPosition[10] = 430;
        scoreMarkPosition[11] = 472;
        scoreMarkPosition[12] = 530;
        scoreMarkPosition[13] = 548;
        scoreMarkPosition[14] = 565;
        scoreMarkPosition[15] = 582;
        scoreMarkPosition[16] = 600;
    }

    public void ScoreData3Init() {
        // Title
        textViewScoreTitle.setText("언제나 몇번이라도");

        // Score image
        imageViewScore.setImageResource(R.drawable.note_level_3);

        // Bar 1
        scoreData[0][0] = 1;
        scoreData[0][1] = 0x29;
        scoreData[1][0] = 1;
        scoreData[1][1] = 0x35;
        scoreData[2][0] = 1;
        scoreData[2][1] = 0x39;
        scoreData[3][0] = 1;
        scoreData[3][1] = 0x29;
        scoreData[4][0] = 1;
        scoreData[4][1] = 0x35;
        scoreData[5][0] = 1;
        scoreData[5][1] = 0x39;

        // Bar 2
        scoreData[6][0] = 1;
        scoreData[6][1] = 0x29;
        scoreData[7][0] = 1;
        scoreData[7][1] = 0x35;
        scoreData[8][0] = 1;
        scoreData[8][1] = 0x39;
        scoreData[9][0] = 1;
        scoreData[9][1] = 0x29;
        scoreData[10][0] = 1;
        scoreData[10][1] = 0x35;
        scoreData[11][0] = 2;
        scoreData[11][1] = 0x39;
        scoreData[11][2] = 0x41;
        scoreData[12][0] = 1;
        scoreData[12][1] = 0x43;

        // Bar 3
        scoreData[13][0] = 2;
        scoreData[13][1] = 0x29;
        scoreData[13][2] = 0x45;
        scoreData[14][0] = 1;
        scoreData[14][1] = 0x41;
        scoreData[15][0] = 2;
        scoreData[15][1] = 0x35;
        scoreData[15][2] = 0x48;
        scoreData[16][0] = 1;
        scoreData[16][1] = 0x39;
        scoreData[17][0] = 1;
        scoreData[17][1] = 0x45;
        scoreData[18][0] = 2;
        scoreData[18][1] = 0x34;
        scoreData[18][2] = 0x43;
        scoreData[19][0] = 3;
        scoreData[19][1] = 0x37;
        scoreData[19][2] = 0x3C;
        scoreData[19][3] = 0x48;
        scoreData[20][0] = 1;
        scoreData[20][1] = 0x43;

        // Bar 4
        scoreData[21][0] = 2;
        scoreData[21][1] = 0x32;
        scoreData[21][2] = 0x41;
        scoreData[22][0] = 1;
        scoreData[22][1] = 0x3E;
        scoreData[23][0] = 3;
        scoreData[23][1] = 0x35;
        scoreData[23][2] = 0x39;
        scoreData[23][3] = 0x45;
        scoreData[24][0] = 1;
        scoreData[24][1] = 0x41;
        scoreData[25][0] = 2;
        scoreData[25][1] = 0x2D;
        scoreData[25][2] = 0x40;
        scoreData[26][0] = 2;
        scoreData[26][1] = 0x34;
        scoreData[26][2] = 0x39;
        scoreData[27][0] = 1;
        scoreData[27][1] = 0x40;

        // Number of score data
        numOfScoreData = 28;

        // Score mark position
        scoreMarkPosition[0] = 105;
        scoreMarkPosition[1] = 124;
        scoreMarkPosition[2] = 142;
        scoreMarkPosition[3] = 161;
        scoreMarkPosition[4] = 179;
        scoreMarkPosition[5] = 198;
        scoreMarkPosition[6] = 224;
        scoreMarkPosition[7] = 244;
        scoreMarkPosition[8] = 264;
        scoreMarkPosition[9] = 283;
        scoreMarkPosition[10] = 303;
        scoreMarkPosition[11] = 323;
        scoreMarkPosition[12] = 335;
        scoreMarkPosition[13] = 355;
        scoreMarkPosition[14] = 368;
        scoreMarkPosition[15] = 380;
        scoreMarkPosition[16] = 400;
        scoreMarkPosition[17] = 412;
        scoreMarkPosition[18] = 424;
        scoreMarkPosition[19] = 444;
        scoreMarkPosition[20] = 463;
        scoreMarkPosition[21] = 491;
        scoreMarkPosition[22] = 504;
        scoreMarkPosition[23] = 518;
        scoreMarkPosition[24] = 544;
        scoreMarkPosition[25] = 558;
        scoreMarkPosition[26] = 579;
        scoreMarkPosition[27] = 601;
    }


    //-------------------------------------------------------------------------------//
    //
    //                             MIDI
    //
    //-------------------------------------------------------------------------------//
    class MyDirectReceiver extends MidiReceiver {
        @Override
        public void onSend(byte[] data, int offset, int count,
                           long timestamp) throws IOException {

            //String prefix = String.format("0x%08X, ", timestamp);
            //logByteArray(prefix, data, offset, count);

            // Send raw data to be parsed into discrete messages.
            //mConnectFramer.send(data, offset, count, timestamp);

            // Raw data check
            //logRawData(data, offset, count);

            // Input key check
            CheckPianoKeyInput(data, count);
        }
    }

    public void log(final String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logFromUiThread(string);
            }
        });
    }

    // Log a message to our TextView.
    // Must run on UI thread.
    private void logFromUiThread(String s) {
        // Toast log
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

        // Text view log
        //textViewPianoUsbLog.setText(s);
    }

    private void logByteArray(String prefix, byte[] value, int offset, int count) {
        StringBuilder builder = new StringBuilder(prefix);
        for (int i = 0; i < count; i++) {
            builder.append(String.format("0x%02X", value[offset + i]));
            if (i != count - 1) {
                builder.append(", ");
            }
        }
        log(builder.toString());
    }

    private void logRawData(byte[] data, int offset, int count) {
        // Raw data check
        String str = "Data: ";

        for(int i = 0; i < count; i++) {
            str += String.format("0x%02x", data[i]);
            str += ", ";
        }

        str += "\nOffset: " + offset;
        str += "\nCount: " + count;
        log(str);
    }


    //-------------------------------------------------------------------------------//
    //
    //                             BLUETOOTH
    //
    //-------------------------------------------------------------------------------//
    public static final int TURN_ON = 1;
    public static final int TURN_OFF = 0;


}
