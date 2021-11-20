package com.shiveluch.sciencebox;

import android.content.Context;
import android.media.MediaPlayer;

public class Audio {

    MediaPlayer mp;
    Context context;

    public Audio(Context ct) {
        this.context = ct;
    }

    public void playbegin() {
        mp = MediaPlayer.create(context, R.raw.beginscan);
        mp.start();
    }
    public void playcode() {
        mp = MediaPlayer.create(context, R.raw.cod);
        mp.start();
    }
    public void playend() {
        mp = MediaPlayer.create(context, R.raw.endscan);
        mp.start();
    }
}