package fr.enssat.cheikhsalioundiaye.playervideoenrichi;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    VideoView videoView; //ELement de l'IHM qui correspond a la vidéo
    LinearLayout linearButton; //Element de l'IHM qui correspond a l'endroit où sont placés les bouttons
    WebView webView; // Element de l'IHM qui correspond a la page web
    URL_Position url_set = null; // Classe url qui correspond a l'url actuel
    Handler threadHandler = new Handler(); // handler qui permet de lancer le thread qui va rafraichir la page web en fonction du temps
    List<URL_Position> tab_url_position = new ArrayList<>(); // List des classes URL_position qui correspond a toutes les correspondances

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Récupération de l'élément vidéoview
        videoView = (VideoView) findViewById(R.id.video_view);
        //Set l'url de la vidéo buck bunny
        videoView.setVideoPath("https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4");
        //lancement de la vidéo
        videoView.start();
        //ajout d'un media controller pour pouvoir avancer dans la vidéo faire pause et relancer la vidéo
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        //récupération de l'element linearbutton
        linearButton = (LinearLayout) findViewById(R.id.linearButton);
        //suppression de tous les éléments dans le layout linearbutton
        linearButton.removeAllViewsInLayout();
        //récupération de l'élément webview
        webView = (WebView) findViewById(R.id.webView);
        //ajout d'un client custom pour la webview
        webView.setWebViewClient(new CustomWebViewClient());
        //récupération des settings de la web view
        WebSettings webSetting = webView.getSettings();
        //ajout de quelques settings
        webSetting.setJavaScriptEnabled(true);
        webSetting.setDisplayZoomControls(true);
        //appel a notre fonction qui permet de créer les chapitres
        createChapitre();
        //appel a notre fonction qui permet de créer les url
        createUrl();
        //Création de la class qui vas etre appelé toutes les secondes pour rafraichir la page web en fonction des URL_position
        UpdatePageWeb updatePageWeb= new UpdatePageWeb();
        //lancement différé de la fonction run de UpdatePageWeb dans 1000 ms
        threadHandler.postDelayed(updatePageWeb,1000);
    }


    //Fonction qui permet de parser le JSON des urls pour l'ajouter a tab_url_position
    public void createUrl(){
        try {
            //Création de l'objet JSON en parsant la ressource R.raw.url
            JSONObject obj = new JSONObject(loadJSONFromAsset(R.raw.url));
            //récupération de l'array "urls" contenu dans le JSONobjet
            JSONArray m_jArry = obj.getJSONArray("urls");
            //parcourt du tableau m_jArry
            for (int i = 0; i < m_jArry.length(); i++) {
                //récupération de l'objet
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                //récupération des éléments contenus dans l'objet
                String url_of_page = jo_inside.getString("url");
                int start = jo_inside.getInt("start");
                //Création de notre propre classe URL_Position
                //url position permet de changer de postion dans la webVieuw (voir update page web)
                URL_Position url_position = new URL_Position(url_of_page, start);
                //Ajout dans tab url position de l'élément créé
                tab_url_position.add(url_position);
            }
        }
        catch (org.json.JSONException e)
        {
            e.printStackTrace();
        }
    }

    //Fonction qui permet de parser le JSON des chapitres afin de créer les boutons chapitres
    public void createChapitre(){
        try {
            //Création de l'objet JSON en parsant la ressource R.raw.chapitre
            //recuperation du fichier chapitre.json
            JSONObject obj = new JSONObject(loadJSONFromAsset(R.raw.chapitre));
            //récupération de l'array "chapitres" contenu dans le JSONobjet
            JSONArray m_jArry = obj.getJSONArray("chapitres");
            //parcourt du tableau m_jArry
            for (int i = 0; i < m_jArry.length(); i++) {
                //récupération de l'objet
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                //récupération des éléments contenus dans l'objet
                String name_of_chapitre = jo_inside.getString("name");
                int seek = jo_inside.getInt("seek");
                //appel a la fonction create button qui va créer les boutons avec les spécificités récupérées.
                createButton(name_of_chapitre, seek);
            }
        }
        catch (org.json.JSONException e)
        {
            e.printStackTrace();
        }
    }

    //Permet de lire un fichier json pour le convertir en String a partir d'un ID ressource
    public String loadJSONFromAsset(int ressource) {
        String json = null;
        try {
            //Ouverture de la ressource
            InputStream is = this.getResources().openRawResource(ressource);
            //récupération de la taille du fichier.
            int size = is.available();
            //création du buffer
            byte[] buffer = new byte[size];
            //lecture dans le buffer
            is.read(buffer);
            //fermeture du flux
            is.close();
            //convertion en String
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    //fonction qui permet de créer un bouton chapitre
    void createButton(String name, final int seek)
    {
        //création du bouton
        Button button = new Button(this);
        //ajout du texte du bouton grace au paramètre name
        button.setText(name);
        //création des paramètres afin que le bouton prenne un maximum de place
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        //ajout d'un event lors du click
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //appel la fonction qui va permettre de deplacer la vidéo a la 'seek' seconde
                goChapitre(seek);

            }
        });
        //ajout du bouton a la LinearLayout
        linearButton.addView(button, params);

    }

    //fonction qui permet de se déplacer dans la vidéo seek = le nombre de seconde de la vidéo
    void goChapitre(int seek) {
        videoView.seekTo(seek * 1000);
    }

    //classe qui permet de changer de page, elle est appelé toutes les secondes.
    class UpdatePageWeb implements Runnable {
        public void run(){
            //position en secondes de la vidéo actuel
            int currentPosition = videoView.getCurrentPosition() / 1000;
            //correspond au temps de début maximum sous la current position
            int start_max = -1;
            //Correspond a l'url qui est le plus élévé sous la current position
            URL_Position url_max = null;
            //on parcourt toutes les urls
            for (int i = 0; i < tab_url_position.size(); i++) {
                //url a la position i
                URL_Position url_position = tab_url_position.get(i);
                //si le temps de l'url est plus grand que le plus grand temps trouvé précédemment et qu'elle est inférieur a la current position
                if (start_max < url_position.start && currentPosition >= url_position.start) {
                    //on remplace le temps max et l'url max par l'url qui correspond au if
                    start_max = url_position.start;
                    url_max = url_position;
                }
            }
            //si l'url trouvé est différent de celui qui était actuellement affiché on change l'url
            if (url_max != url_set)
            {
                url_set = url_max;
                //permet de load la nouvelle page web
                webView.loadUrl(url_set.URL);
            }
            //prochain appel de la fonction dans 1000 ms donc 1 seconde
            threadHandler.postDelayed(this, 1000);

        }
    }

    //custom class de client webview qui hérite de webviewclient
    //permet de ne pas ouvrir de navigateur externe
    private class CustomWebViewClient extends WebViewClient {
        //override de la fonction loadUrl
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}