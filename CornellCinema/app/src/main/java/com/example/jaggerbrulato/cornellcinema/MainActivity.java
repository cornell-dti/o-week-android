package com.example.jaggerbrulato.cornellcinema;

import android.graphics.Movie;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private int numberOfMovies = -1;
    public ArrayList<MovieInfo> movieInfos = new ArrayList<>();
    public WebView browser;
    private ViewGroup movieList;
    private ArrayAdapter<String> listAdapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        movieList = findViewById(R.id.movieList);

        final WebView webView = (WebView) findViewById(R.id.browser);
        this.browser = webView;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new BackgroundBrowserJavascriptInterface(this), "HtmlViewer");

        webView.setWebViewClient(new WebViewClient() {
            @JavascriptInterface
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.HtmlViewer.getNumberOfMovies" +
                        "(" +
                        "document.querySelectorAll('.event-summary').length" +
                        ");");
                while(numberOfMovies == -1){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("HTML", "WAITING");
                }
                for(int i=0; i<numberOfMovies; i++){
                    view.loadUrl("javascript:window.HtmlViewer.addMovie" +
                            "(" +
                            "document.getElementsByClassName('event-summary')["+String.valueOf(i)+"].innerHTML," +
                            "document.getElementsByClassName('event-time')["+String.valueOf(i)+"].innerHTML" +
                            ");");
                    Log.d("DEBUG", "CALLEDDDDDD");
                }
                while(movieInfos.size()<numberOfMovies){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                buildRows();
            }});

        webView.loadUrl("https://calendar.google.com/calendar/htmlembed?src=jaggerbrulato%40gmail.com&amp;ctz=America/New_York");
    }


    public void addMovie(MovieInfo movieInfo){
        movieInfos.add(movieInfo);
    }

    public void setNumberOfMovies(int numberOfMovies){
        this.numberOfMovies = numberOfMovies;

    }

    public void buildRows(){

        ArrayList<String> movieNames = new ArrayList<>();
        ArrayList<String> movieTimes = new ArrayList<>();
        for(MovieInfo info : movieInfos){
            movieNames.add(info.name);
            movieTimes.add(info.time);
        }

        Log.d("DEBUG", String.valueOf(movieNames.size()));

        for(int i = 0; i<movieNames.size(); i++){
            addRow(movieTimes.get(i), movieNames.get(i), movieList);
        }

    }

    public void addRow(String time, String name, ViewGroup movieList){
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.simple_row, movieList, false);
        constructRow(time, name, layout);
        movieList.addView(layout);
    }

    public void constructRow(String time, String name, ConstraintLayout parentLayout){
        TextView textView = (TextView) parentLayout.findViewById(R.id.movieTime);
        TextView textView2 = (TextView) parentLayout.findViewById(R.id.movieName);

        textView.setText(time);
        textView2.setText(name);
    }

}
