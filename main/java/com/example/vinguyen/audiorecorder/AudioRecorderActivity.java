package com.example.vinguyen.audiorecorder;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AudioRecorderActivity extends Activity {

    private static final String TAG = "AudioRecordTest";
    private static final String APP_STORAGE = Environment.getExternalStorageDirectory().getAbsolutePath()
                                                + "/Audio Recorder";
    private String curRecordingFileName;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private AudioManager mAudioManager;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_audio_recorder);

        final ToggleButton mRecordButton = (ToggleButton) findViewById(R.id.record_button);
        final ToggleButton mPlayButton = (ToggleButton) findViewById(R.id.play_button);

        // create app storage folder
        File appFolder = new File(APP_STORAGE);
        if (!appFolder.exists()) {
            Log.e(TAG, "Creating ap storage forder: " + APP_STORAGE);
            Boolean ret = appFolder.mkdirs();
            if (!ret)
                Log.e(TAG, "Error while creating ap storage forder");
        }

        // Set up record Button
        mRecordButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                // Set checked state
                mPlayButton.setEnabled(!isChecked);

                // Start/stop recording
                onRecordPressed(isChecked);

            }
        });

        // Set up play Button
        mPlayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                // Set checked state
                mRecordButton.setEnabled(!isChecked);

                // Start/stop playback
                onPlayPressed(isChecked);
            }
        });

        // Get AudioManager
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus
        mAudioManager.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // First we have nothing to play, disable play Button
        mPlayButton.setEnabled(false);

        curRecordingFileName = null;

    }

    // Toggle recording
    private void onRecordPressed(boolean shouldStartRecording) {

        if (shouldStartRecording) {
            startRecording();
        } else {
            stopRecording();
        }

    }

    // Start recording with MediaRecorder
    private void startRecording() {
        // When start recording, create a file with format yyyyMMdd_HHmmss.3gp
        // y, M, d, H, m,s are year, month, day, hour, minite and second when start recording
        String currentDateAndTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        curRecordingFileName = APP_STORAGE + "/" + currentDateAndTime + ".3gp";
        Log.e(TAG, "Start recording file: " + curRecordingFileName);

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(curRecordingFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't prepare and start MediaRecorder");
        }

        mRecorder.start();
    }

    // Stop recording. Release resources
    private void stopRecording() {
        Log.e(TAG, "Stop recording file: " + curRecordingFileName);
        if (null != mRecorder) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

    }

    // Toggle playback
    private void onPlayPressed(boolean shouldStartPlaying) {

        if (shouldStartPlaying) {
            startPlaying();
        } else {
            stopPlaying();
        }

    }

    // Playback audio using MediaPlayer
    private void startPlaying() {
        if (null == curRecordingFileName)
            return;
        Log.e(TAG, "Start playing file: " + curRecordingFileName);
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(curRecordingFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't prepare and start MediaPlayer");
        }

    }

    // Stop playback. Release resources
    private void stopPlaying() {
        Log.e(TAG, "Stop playing file: " + curRecordingFileName);
        if (null != mPlayer) {
            if (mPlayer.isPlaying())
                mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

    }

    // Listen for Audio Focus changes
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {

            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mAudioManager.abandonAudioFocus(afChangeListener);

                // Stop playback, if necessary
                if (mPlayer.isPlaying())
                    stopPlaying();
            }

        }

    };

    // Release recording and playback resources, if necessary
    @Override
    public void onPause() {
        super.onPause();

        if (null != mRecorder) {
            mRecorder.release();
            mRecorder = null;
        }

        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }

    }
}
