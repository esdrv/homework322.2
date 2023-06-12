package com.msaggik.homework322;

import static com.msaggik.homework322.R.id.fabPlayPause;
import static com.msaggik.homework322.R.id.fabRepeat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.msaggik.homework322.R;

public class MainActivity extends AppCompatActivity implements Runnable{

    // создание полей
    private MediaPlayer mediaPlayer = new MediaPlayer(); // создание поля медиа-плеера
    private SeekBar seekBar; // создание поля SeekBar
    private boolean wasPlaying = false; // поле проигрывания аудио-файла
    private FloatingActionButton fabPlayPause; // поле кнопки проигрывания и постановки на паузу аудиофайла
    private FloatingActionButton fabBack; // поле кнопки перемотки назад аудиофайла
    private FloatingActionButton fabRepeat; // поле кнопки повтора
    private TextView seekBarHint; // поле информации у SeekBar
    private TextView metaDataAudio; // создание поля вывода информации о треке
    private String metaData; // поле для записи метаданных
    private boolean isRepeat = false; // поле выключенного повтора трека


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        fabPlayPause = findViewById(R.id.fabPlayPause);
        fabBack = findViewById(R.id.fabBack);
        fabRepeat = findViewById(R.id.fabRepeat);
        seekBarHint = findViewById(R.id.seekBarHint);
        seekBar = findViewById(R.id.seekBar);
        metaDataAudio = findViewById(R.id.metaDataAudio);
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);
               
                int timeTrack = (int) Math.ceil(progress/1000f); 

                // вывод на экран времени отсчёта трека
                if (timeTrack < 10) {
                    seekBarHint.setText("00:0" + timeTrack);
                } else if (timeTrack < 60){
                    seekBarHint.setText("00:" + timeTrack);
                } else if (timeTrack >= 60 && timeTrack < 70) {
                    seekBarHint.setText("01:0" + (timeTrack - 60));
                } else if (timeTrack >= 70) {
                    seekBarHint.setText("01:" + (timeTrack - 60));
                }
                double percentTrack = progress / (double) seekBar.getMax();
                seekBarHint.setX(seekBar.getX() + Math.round(seekBar.getWidth()*percentTrack*0.92));
                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer(); // остановка и очиска MediaPlayer
                    // назначение кнопке картинки play
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    MainActivity.this.seekBar.setProgress(0);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.INVISIBLE);
            }
           
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

       
        fabPlayPause.setOnClickListener(listener);
        fabBack.setOnClickListener(listener);
        fabRepeat.setOnClickListener(listener);
    }

    private final View.OnClickListener listener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fabPlayPause:
                    playSong();
                    break;
                case R.id.fabBack:
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                    break;
                case R.id.fabRepeat;
                    if (!isRepeat && mediaPlayer != null) {
                        mediaPlayer.setLooping(true);
                        fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.repeat));
                        isRepeat = true;
                    } else if (isRepeat && mediaPlayer != null) {
                        mediaPlayer.setLooping(false);
                        fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.repeat_off));
                        isRepeat = false;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
            }
        }
    };
    public void playSong() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                clearMediaPlayer(); 
                seekBar.setProgress(0); 
                wasPlaying = true;
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
            }

            if (!wasPlaying) {
                if (mediaPlayer == null) { 
                    mediaPlayer = new MediaPlayer();
                }
                // назначение кнопке картинки pause
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                AssetFileDescriptor descriptor = getAssets().openFd("barradeen-boku-no-love.mp3");
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                MediaMetadataRetriever mediaMetadata = new MediaMetadataRetriever();
                mediaMetadata.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                metaData = mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                metaData += "\n" + mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                mediaMetadata.release();
                metaDataAudio.setText(metaData);
                descriptor.close();
                mediaPlayer.prepare();
                mediaPlayer.setLooping(false); 
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.start();
                new Thread(this).start();
            }

            wasPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }
    private void clearMediaPlayer() {
        mediaPlayer.stop(); 
        mediaPlayer.release(); 
        mediaPlayer = null; 
    }
    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition(); 
        int total = mediaPlayer.getDuration();
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {

                Thread.sleep(1000); 
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition);

        }
    }
}