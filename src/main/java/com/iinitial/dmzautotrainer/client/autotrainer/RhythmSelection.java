package com.iinitial.dmzautotrainer.client.autotrainer;

import com.dragonminez.client.gui.character.minigames.BaseMinigameScreen;

import java.lang.reflect.Method;
import java.util.List;

public final class RhythmSelection {
    public record Choice(String trackKey, int difficulty, int variant) {}

    private RhythmSelection() {
    }

    public static Choice capture(BaseMinigameScreen screen) {
        Object track = Reflect.invoke(screen, "selectedTrack");
        String key = track == null ? "" : (String) Reflect.invoke(track, "key");
        return new Choice(key, (int) Reflect.get(screen, "difficulty"), (int) Reflect.get(screen, "variant"));
    }

    public static boolean restore(BaseMinigameScreen screen, Choice choice) {
        List<?> tracks = (List<?>) Reflect.get(screen, "tracks");
        int wanted = -1;
        for (int i = 0; i < tracks.size(); i++) {
            if (choice.trackKey().equals(Reflect.invoke(tracks.get(i), "key"))) {
                wanted = i;
                break;
            }
        }
        if (wanted < 0) {
            return false;
        }

        if (wanted != (int) Reflect.get(screen, "trackIndex")) {
            call(screen, "selectTrack", wanted);
            call(screen, "setVariant", choice.variant());
            call(screen, "setDifficulty", choice.difficulty());
        }
        return true;
    }

    private static void call(Object target, String name, int value) {
        try {
            Method method = findMethod(target.getClass(), name);
            method.setAccessible(true);
            method.invoke(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to call " + name, e);
        }
    }

    private static Method findMethod(Class<?> type, String name) throws NoSuchMethodException {
        for (Class<?> clazz = type; clazz != null; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredMethod(name, int.class);
            } catch (NoSuchMethodException ignored) {}
        }
        throw new NoSuchMethodException(name);
    }
}