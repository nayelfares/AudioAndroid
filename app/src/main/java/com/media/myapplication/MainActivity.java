package com.media.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String extStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String filePath = extStoragePath + "/myAudio.3gp";
    private Button mBtnRecord;
    private Button mBtnPlay;
    private Button button;
    private Boolean isRecording = false;
    private Boolean isPlaying = false;
    private  PresetActivity preset;
    private MediaRecorder recorder;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnRecord = findViewById(R.id.button_record);
        mBtnPlay = findViewById(R.id.button_play);
        button = findViewById(R.id.button);
        mBtnRecord.setOnClickListener(this);
        preset= new PresetActivity(this);
        mBtnPlay.setOnClickListener(this);

        button.setOnClickListener(this);



    }



    @Override
    public void onClick(View v) {
        if (v == mBtnRecord) {
            if (isRecording == false) { //�}�l����
                isRecording = true;
                mBtnRecord.setText("�������");
                try {
                    recordAudio();
                } catch (IOException e) {

                }
            } else { //�������
                isRecording = false;
                mBtnRecord.setText("����");
                stopRecord();
            }
        } else if (v == mBtnPlay) {
            if (!isPlaying) { //�}�l����
                isPlaying = true;
                mBtnPlay.setText("Pause");
                playAudio();
            } else { //�����
                isPlaying = false;
                mBtnPlay.setText("Resume");
                stopAudio();
            }
        }
        else if (v==button){
            preset.start();
        }
    }

    private void playAudio() {
        player = new MediaPlayer();
        try {

            player.setDataSource(filePath);
            player.prepare();
            player.start();
        } catch (IOException e) {

        }
    }

    private void stopAudio() {
        //player.pause();
        player.stop();
    }

    private void recordAudio() throws IOException{
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            throw new IOException("SD card is not mounted");
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(filePath);
        recorder.prepare();
        recorder.start();
    }

    private void stopRecord() {
        recorder.stop();
        recorder.release();
    }
}
