package com.google.android.gms.samples.vision.barcodereader;

import android.content.DialogInterface;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;


/**
 * PJDCC - Summary for class responsabilities.
 *
 * @author Antonio
 * @version 1.0
 * La seguente classe si occupa di visualizzare i dati relativi alle Schede Reperto e ne consente la riproduzione audio
 */
public class SchedeActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    /**
     * id passato dal qr attraverso il quale verranno estratte le informazioni
     */
    String id_scheda_reperto = "";
    String datiRic = "";

    TextView textView;
    TextToSpeech tts;
    FloatingActionButton btnSpeak;
    Boolean riproduzione_in_corso = false;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schede);

        //Recupero il valore dell'id rilevato dal barcode
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id_scheda_reperto = extras.getString("id_scheda_reperto");
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            getId();
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unsupported Encoding Exception");
        }

        btnSpeak = (FloatingActionButton) findViewById(R.id.fab);
        tts = new TextToSpeech(this,this);
        riproduzione_in_corso = false;
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if(riproduzione_in_corso==false) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SchedeActivity.this);
                    alertDialogBuilder.setMessage("Attivare riproduzione Audio?");
                    alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            textView.setText(datiRic);
                            speakOut();
                            riproduzione_in_corso = true;
                        }
                    })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    riproduzione_in_corso = false;
                                }
                            });
                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }

                else {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SchedeActivity.this);
                    alertDialogBuilder.setMessage("Disattivare Audio?");
                    alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id) {
                            textView.setText(datiRic);
                            tts.stop();
                            riproduzione_in_corso = false;
                        }
                    })

                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    riproduzione_in_corso = true;
                                }
                            });

                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }

            }
        });

    }

    /**
     *
     * @throws UnsupportedEncodingException
     */
    public void getId() throws UnsupportedEncodingException {
        String data = URLEncoder.encode("id_scheda_reperto", "UTF-8") + "=" + URLEncoder.encode(id_scheda_reperto, "UTF-8");

        final int SBSIZE = 200;
        String result = "";
        BufferedReader reader = null;

        TextView name = null;
        TextView autoreView = null;
        TextView descrExt = null;


        name = (TextView) findViewById(R.id.nome);
        autoreView = (TextView) findViewById(R.id.autore);
        descrExt = (TextView) findViewById(R.id.descrExt);


        // Send data
        try {
            URL url = new URL("http://luglio-94.000webhostapp.com/wp-content/plugins/wp-scheda-reperto/interaction.php");

            // Send POST data request
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);

            System.out.println(data);

            wr.flush();

            // Get the server response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder(SBSIZE);
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line);
            }

            result = sb.toString();
            System.out.println(result);

        } catch (MalformedURLException e) {
            System.err.println("Malformed URL Exception");
        } catch (IOException e) {
            System.err.println("IO Exception");
        }

        //Parsing
        try{
            JSONObject jo = new JSONObject(result);

            if("Attenzione".equals(jo.names().getString(0))){
                TextView error;
                error = (TextView) findViewById(R.id.error);
                error.setText("Spiacente la scheda non è disponibile");
            }
            else {

                String opera = jo.names().getString(0);
                String aut = jo.names().getString(1);
                String des = jo.names().getString(2);

                String nome = jo.getString("nome");
                String autore = jo.getString("autore");
                String descrizioneEstesa = jo.getString("descrizioneEstesa");
                name.setText(opera + ": " + nome);
                autoreView.setText(aut + ": " + autore);
                descrExt.setText(des + ": " + descrizioneEstesa);

                datiRic = opera + " " + nome + " " + aut + autore + " " + des + " " + descrizioneEstesa;
                textView = (TextView) findViewById(R.id.textView);
                textView.setVisibility(TextView.INVISIBLE);
                textView.setText(datiRic);
            }

        } catch (JSONException e) {
            System.err.println("JSON Exception");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                System.err.println("IO Exception");
            }
        }

    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    /**
     *
     * @param status
     */
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            //int result = tts.setLanguage(Locale.US);
            int result = tts.setLanguage(Locale.ITALIAN);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    /**
     * Metodo che specifica la stringa da riprodurre
     */
    private void speakOut() {
        String text = textView.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

}




