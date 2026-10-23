package com.iinitial.dmzautotrainer.client.minigames;
public interface InputSink {
    void key(int key, boolean down, float frac);

    void click(double fieldX, double fieldY, float frac);

    default void tap(int key, float frac) {
        key(key, true, frac);
        key(key, false, frac);
    }
}