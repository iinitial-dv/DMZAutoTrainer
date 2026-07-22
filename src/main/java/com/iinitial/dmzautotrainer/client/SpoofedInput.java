package com.iinitial.dmzautotrainer.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpoofedInput {
    private static final Set<Integer> heldKeys = ConcurrentHashMap.newKeySet();

    public static void press(int keyCode) {
        heldKeys.add(keyCode);
    }

    public static void release(int keyCode) {
        heldKeys.remove(keyCode);
    }

    public static boolean isHeld(int keyCode) {
        return heldKeys.contains(keyCode);
    }
}
