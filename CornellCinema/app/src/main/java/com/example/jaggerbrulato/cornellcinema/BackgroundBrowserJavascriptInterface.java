package com.example.jaggerbrulato.cornellcinema;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by jaggerbrulato on 11/4/17.
 */

public class BackgroundBrowserJavascriptInterface {

    private MainActivity activity;

    BackgroundBrowserJavascriptInterface(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void getNumberOfMovies(String html) {
        Log.d("#OfMovies", html);
        setNumberOfMovies(Integer.valueOf(html));
    }

    @JavascriptInterface
    public void addMovie(String name, String time){
        Log.d("Movie", name);
        Log.d("Time", time);
        activity.addMovie(new MovieInfo(time, name));
        Log.d("Movies", activity.movieInfos.toString());
    }

    @JavascriptInterface
    public void setNumberOfMovies(int numberOfMovies){
        activity.setNumberOfMovies(numberOfMovies);
    }


}
