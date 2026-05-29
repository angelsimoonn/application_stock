package com.example.application_stock.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application_stock.R;

public class AcercaDeActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca_de);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnStop = findViewById(R.id.btnStop);

        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
            }
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                Toast.makeText(this, R.string.play_audio, Toast.LENGTH_SHORT).show();
            }
        });

        btnStop.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                Toast.makeText(this, R.string.stop_audio, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnWeb).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/angelsimoonn"));
            startActivity(intent);
        });

        findViewById(R.id.btnShare).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta app de gestión de stock!");
            startActivity(Intent.createChooser(intent, getString(R.string.share_app)));
        });

        findViewById(R.id.btnContact).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:soporte@appstock.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta AppStock");
            startActivity(intent);
        });

        findViewById(R.id.btnCall).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:900123456"));
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}