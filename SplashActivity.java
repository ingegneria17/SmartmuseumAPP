package com.google.android.gms.samples.vision.barcodereader;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * PJDCC - Summary for class responsabilities.
 *
 * @author Antonio
 * @version 1.0
 * La seguente classe si occupa di visualizzare la schermata iniziale dell'APP e di far partire la MainActivity dopo 2 secondi
 */
public class SplashActivity extends AppCompatActivity {

    //private int timeout = 2000;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final int timeout = 2000;
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, timeout);

    }
}
