package com.iinitial.dmzautotrainer.client.autotrainer;

import com.dragonminez.client.gui.character.minigames.BaseMinigameScreen;
import com.dragonminez.common.training.MinigameEvent;
import com.dragonminez.common.training.MinigameLogic;
import com.iinitial.dmzautotrainer.client.minigames.InputSink;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ScreenInputSink implements InputSink {
    private static final Method EMIT = lookupEmit();

    private final BaseMinigameScreen screen;
    private MinigameLogic logic;

    public ScreenInputSink(BaseMinigameScreen screen) {
        this.screen = screen;
    }

    public void attach(MinigameLogic logic) {
        this.logic = logic;
    }

    @Override
    public void key(int key, boolean down, float frac) {
        emit(MinigameEvent.key(logic.tickCount(), down, key, frac));
    }

    @Override
    public void click(double fieldX, double fieldY, float frac) {
        emit(MinigameEvent.click(logic.tickCount(), fieldX, fieldY, frac));
    }

    private void emit(MinigameEvent event) {
        try {
            EMIT.invoke(screen, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to send minigame input", e);
        }
    }

    private static Method lookupEmit() {
        try {
            Method method = BaseMinigameScreen.class.getDeclaredMethod("emit", MinigameEvent.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}