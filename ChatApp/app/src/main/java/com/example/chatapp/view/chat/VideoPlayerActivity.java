package com.example.chatapp.view.chat;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";

    private VideoView videoView;
    private ProgressBar progressBar;
    private FrameLayout playerRoot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        playerRoot = findViewById(R.id.playerRoot);
        videoView = findViewById(R.id.videoView);
        progressBar = findViewById(R.id.progressBar);

        String videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        if (videoUrl == null || videoUrl.isBlank()) {
            Toast.makeText(this, "Khong tim thay duong dan video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        playVideo(videoUrl);
    }

    private void playVideo(String videoUrl) {
        progressBar.setVisibility(View.VISIBLE);

        videoView.setOnPreparedListener(mp -> {
            updateVideoLayout(mp);
            progressBar.setVisibility(View.GONE);
            mp.setOnInfoListener((mediaPlayer, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    progressBar.setVisibility(View.GONE);
                }
                return false;
            });
            videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Khong phat duoc video (" + what + ")", Toast.LENGTH_LONG).show();
            return true;
        });

        videoView.setVideoURI(Uri.parse(videoUrl));
        videoView.requestFocus();
        videoView.start();
    }

    private void updateVideoLayout(MediaPlayer mediaPlayer) {
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        int containerWidth = playerRoot.getWidth();
        int containerHeight = playerRoot.getHeight();

        if (videoWidth <= 0 || videoHeight <= 0 || containerWidth <= 0 || containerHeight <= 0) {
            return;
        }

        float videoRatio = (float) videoWidth / videoHeight;
        float containerRatio = (float) containerWidth / containerHeight;

        int targetWidth;
        int targetHeight;
        if (videoRatio > containerRatio) {
            targetWidth = containerWidth;
            targetHeight = (int) (containerWidth / videoRatio);
        } else {
            targetHeight = containerHeight;
            targetWidth = (int) (containerHeight * videoRatio);
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(targetWidth, targetHeight);
        params.gravity = Gravity.CENTER;
        videoView.setLayoutParams(params);
    }
}
