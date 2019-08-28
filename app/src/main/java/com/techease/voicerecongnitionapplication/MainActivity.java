package com.techease.voicerecongnitionapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.log10;

public class MainActivity extends AppCompatActivity {


    MediaRecorder mRecorder;
    Thread runner;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.8;
    String strNoise;


    int timer_int = 1000;
//    final Runnable updater = new Runnable() {
//
//        public void run() {
//            updateTv();
//
//        }
//    };
//    final Handler mHandler = new Handler();

    private SpeechRecognizer speechRecognizer;

    String strUserVoice;

    boolean aBooleanSpeech = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        } else {

//            startRecorder();

            startService(new Intent(MainActivity.this,SpeechService.class));


        }

        Button button=findViewById(R.id.btn_stop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this,SpeechService.class));

            }
        });
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);


//        if (runner == null) {
//            runner = new Thread() {
//                public void run() {
//                    while (runner != null) {
//                        try {
//                            timer_int = 1000;
//                            Thread.sleep(timer_int);
//                            Log.i("Noise", "Tock");
//                        } catch (InterruptedException e) {
//                        }
//                        ;
////                        mHandler.post(updater);
//                    }
//                }
//            };
//            runner.start();
//            Log.d("Noise", "start runner()");
//        }


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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not have audio on record", Toast.LENGTH_SHORT).show();
            } else {
//                startRecorder();

                startService(new Intent(MainActivity.this,SpeechService.class));
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


        if (i > 75) {


            if (aBooleanSpeech) {


                try {
                    mRecorder.stop();
                } catch (RuntimeException e) {

                } finally {
                    mRecorder.release();
                    mRecorder = null;
                }

                speechRecognitionClass();
            }
        }


        Log.d("zma str noise", strNoise);


///get Location


    }

    private void speechRecognitionClass() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, MainActivity.this.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        speechRecognizer.startListening(intent);


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {


                Toast.makeText(MainActivity.this, "ready", Toast.LENGTH_SHORT).show();
                aBooleanSpeech = false;

            }

            @Override
            public void onBeginningOfSpeech() {

                Toast.makeText(MainActivity.this, "onBeginningOfSpeech", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRmsChanged(float rmsdB) {
//                Toast.makeText(MainActivity.this, "onRmsChanged", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onBufferReceived(byte[] buffer) {

                Toast.makeText(MainActivity.this, "onBufferReceived", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onEndOfSpeech() {
                aBooleanSpeech = true;

                Toast.makeText(MainActivity.this, "onEndOfSpeech", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(int error) {
                aBooleanSpeech = true;
                startRecorder();

                Toast.makeText(MainActivity.this, "onError", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onResults(Bundle results) {
                showResults(results);
                aBooleanSpeech = true;

                Toast.makeText(MainActivity.this, "onResults", Toast.LENGTH_SHORT).show();

                startRecorder();


            }

            @Override
            public void onPartialResults(Bundle partialResults) {

                Toast.makeText(MainActivity.this, "onPartialResults", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onEvent(int eventType, Bundle params) {

                Toast.makeText(MainActivity.this, "eventType", Toast.LENGTH_SHORT).show();


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

        if (strUserVoice.equals("testing")){


        }

    }

}