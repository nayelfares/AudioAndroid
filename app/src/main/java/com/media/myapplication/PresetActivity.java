package com.media.myapplication;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import org.jtransforms.fft.DoubleFFT_1D;


public class PresetActivity  implements SeekBar.OnSeekBarChangeListener {
    public static final int SEEKMAX = 10;

    int bands[];
    static int trackID, recordID;
    boolean isRecording = false;
    boolean humanOnly = false;

    AudioRecord arec;
    AudioTrack atrack;
    Bitmap bitmap;
    int bufferSize;

    int FFT_SIZE;
    DoubleFFT_1D mFFT;
    double[] audioBuffer;
    float bandValues[];
    VerticalSeekBar vsb[];
    boolean beep[];
    AudioServiceTask audioServiceTask;
     PresetActivity(Context context) {
        bufferSize = 4096;
        bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);


        bands = new int[SEEKMAX];
        bandValues = new float[SEEKMAX];
        vsb = new VerticalSeekBar[SEEKMAX];
        beep = new boolean[SEEKMAX];



        for (int i = 0; i < SEEKMAX; i++) {
            vsb[i] = new VerticalSeekBar(context);
            vsb[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));

            vsb[i].setOnSeekBarChangeListener(this);
        }


        for (int i = 0; i < SEEKMAX; i++) {
            vsb[i].setProgressAndThumb(vsb[i].getMax() / 2);
        }
    }



     class AudioServiceTask extends AsyncTask<Void, double[], Integer> {

        @Override
        protected Integer doInBackground(Void... arg0) {
            arec = new AudioRecord(MediaRecorder.AudioSource.MIC, 11025,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            atrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 11025,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                    AudioTrack.MODE_STREAM);
            atrack.setPlaybackRate(11025);
            trackID = atrack.getAudioSessionId();
            recordID = arec.getAudioSessionId();

            short[] buffer = new short[bufferSize / 2];
            arec.startRecording();
            atrack.play();


            FFT_SIZE = 2048 / 2;
            mFFT = new DoubleFFT_1D(FFT_SIZE);
            audioBuffer = new double[2048];

            while (isRecording) {
                int bufferReadResult = arec.read(buffer, 0, bufferSize / 2);

                buffer = applyFilter(buffer, bufferReadResult, 11025);

                atrack.write(buffer, 0, bufferSize / 2);

            }

            return 1;
        }
        private short[] applyFilter(short[] buffer, int bufferReadResult,
                                    int sampleRate) {

            double[] window = new double[bufferReadResult];
            for (int i = 0; i < bufferReadResult; i++) {
                window[i] = 0.54 - 0.46 * Math.cos(i * 2 * Math.PI/ (bufferReadResult - 1)); // hamming window
                audioBuffer[i] = (buffer[i] * window[i]) / 32768.0;
            }

            mFFT.realForward(audioBuffer);


            for (int fftBin = 0; fftBin < FFT_SIZE; fftBin++) {

                float frequency = (float) (fftBin * sampleRate)
                        / (float) FFT_SIZE;
                boolean flag = false;

                float minFreq = 80;
                float maxFreq = 1499;

                if (frequency > minFreq && frequency < maxFreq)
                    flag = true;

                if (flag) {
                    int real = 2 * fftBin;
                    int imaginary = 2 * fftBin + 1;
                    int i = (int)Math.floor((frequency-80)/142);



                    if (!beep[i]) {
                        audioBuffer[real] = audioBuffer[real] * bandValues[i];
                        audioBuffer[imaginary] = audioBuffer[imaginary] * bandValues[i];
                    } else {
                        audioBuffer[real] = 5 * bandValues[i];
                        audioBuffer[imaginary] = 5 * bandValues[i];
                    }
                }
            }

            if(humanOnly) {
                for (int fftBin = 0; fftBin < FFT_SIZE; fftBin++) {

                    float frequency = (float) (fftBin * sampleRate)
                            / (float) FFT_SIZE;
                    boolean flag = false;

                    float minFreq = 80;
                    float maxFreq = 1499;

                    if (frequency < minFreq || frequency > maxFreq)
                        flag = true;

                    if (flag) {
                        int real = 2 * fftBin;
                        int imaginary = 2 * fftBin + 1;
                        audioBuffer[real] = audioBuffer[real] * 0.25;
                        audioBuffer[imaginary] = audioBuffer[imaginary] * 0.25;
                    }
                }
            }



            publishProgress(audioBuffer);

            mFFT.realInverse(audioBuffer, true);

            for (int i = 0; i < bufferReadResult; i++) {
                window[i] = 0.54 - 0.46 * Math.cos(i * 2 * Math.PI/ (bufferReadResult - 1)); // hamming window
                buffer[i] = (short) (audioBuffer[i] * 32768 / window[i]);
            }

            return buffer;
        }


        @Override
        protected void onPostExecute(Integer result) {
            arec.stop();
            arec.release();
            atrack.stop();
            atrack.release();

        }
    }



    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub
        for (int i = 0; i < SEEKMAX; i++) {
            if (arg0 == vsb[i]) {
                bandValues[i] = (float)arg1 / (float)(vsb[i].getMax() / 2);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
        for(int i=0; i<SEEKMAX; i++) {
            if(arg0 == vsb[i]) {
                beep[i] = true;
            }
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        for(int i=0; i<SEEKMAX; i++) {
            if(arg0 == vsb[i]) {
                beep[i] = false;
            }
        }
    }

    public void start(){
         audioServiceTask=new AudioServiceTask();
                    if (!isRecording) {
                        isRecording = true;
                        audioServiceTask.execute();
                    }
                 else {
                    isRecording = false;
                    arec.stop();
                    atrack.stop();
                    audioServiceTask=null;
                }

    }
}
