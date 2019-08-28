package com.techease.voicerecongnitionapplication;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static java.lang.Math.log10;

public class SpeechService extends Service {

    MediaPlayer mediaPlayer;
    MediaRecorder mRecorder;
    Thread runner;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.8;
    String strNoise;


    int timer_int = 1000;
    final Runnable updater = new Runnable() {

        public void run() {
            updateTv();

        }
    };
    final Handler mHandler = new Handler();

    private SpeechRecognizer speechRecognizer;

    String strUserVoice;

    boolean aBooleanSpeech = true;

    public SpeechService() {
    }

    @Override
    public void onCreate() {


        Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();

        startRecorder();


        mediaPlayer = MediaPlayer.create(this, R.raw.hello);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());


        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            timer_int = 1000;
                            Thread.sleep(timer_int);
                            Log.i("Noise", "Tock");
                        } catch (InterruptedException e) {
                        }
                        ;
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void startRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (java.io.IOException ioe) {
                android.util.Log.e("[Monkey]", "IOException: " + android.util.Log.getStackTraceString(ioe));

            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }
            try {


                mRecorder.start();

            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }
        }


    }


    public void updateTv() {

        double dNoise = soundDb(1.5);
        DecimalFormat decimalFormat = new DecimalFormat("##");
        String strNoiseDF = decimalFormat.format(dNoise);
        strNoise = strNoiseDF;

        if (strNoise.equals("-∞")) {
            strNoise = "00";
        }
        if (strNoise.equals("NaN")) {
            strNoise = "00";
        }

        int i = Integer.parseInt(strNoise);


        if (i > 81) {


            if (aBooleanSpeech) {


                try {
                    mRecorder.stop();
                } catch (RuntimeException e) {

                } finally {
                    mRecorder.release();
                    mRecorder = null;
                }

                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        aBooleanSpeech = true;
                        startRecorder();
                    }
                });

//                speechRecognitionClass();
            }
        }


        Log.d("zma str noise", strNoise);


///get Location


    }

    private void speechRecognitionClass() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        speechRecognizer.startListening(intent);


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {


                Toast.makeText(getApplicationContext(), "ready", Toast.LENGTH_SHORT).show();
                aBooleanSpeech = false;

            }

            @Override
            public void onBeginningOfSpeech() {

                Toast.makeText(getApplicationContext(), "onBeginningOfSpeech", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRmsChanged(float rmsdB) {
//                Toast.makeText(  getApplicationContext(), "onRmsChanged", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onBufferReceived(byte[] buffer) {

                Toast.makeText(getApplicationContext(), "onBufferReceived", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onEndOfSpeech() {

                Toast.makeText(getApplicationContext(), "onEndOfSpeech", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(int error) {
                aBooleanSpeech = true;
                startRecorder();

                Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onResults(Bundle results) {
                showResults(results);


                Toast.makeText(getApplicationContext(), "onResults", Toast.LENGTH_SHORT).show();



            }

            @Override
            public void onPartialResults(Bundle partialResults) {

                Toast.makeText(getApplicationContext(), "onPartialResults", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onEvent(int eventType, Bundle params) {

                Toast.makeText(getApplicationContext(), "eventType", Toast.LENGTH_SHORT).show();


            }
        });


    }


    public double soundDb(double ampl) {
        return 20 * log10(getAmplitudeEMA() / ampl);
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude());
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (0.1 - EMA_FILTER) * mEMA;
        return mEMA;
    }


    private void showResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        strUserVoice = matches.get(0);

        Toast.makeText(this, strUserVoice, Toast.LENGTH_SHORT).show();

        if (strUserVoice.equals("hello")) {

            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {


                    aBooleanSpeech = true;
                    startRecorder();
                }
            });

        }else {
            aBooleanSpeech = true;
            startRecorder();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mRecorder.stop();
        } catch (RuntimeException e) {

        } finally {
            mRecorder.release();
            mRecorder = null;
        }
    }
}


