package com.iinitial.dmzautotrainer.client.gui;

import com.dragonminez.client.gui.buttons.TexturedTextButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SettingsButton extends TexturedTextButton {
    public SettingsButton(int x, int y, int width, int height, ResourceLocation texture, int normalU, int normalV, int hoverU, int hoverV, int textureWidth, int textureHeight, int normalTextColor, int hoverTextColor, int backgroundColor, boolean hasBackgroundColor, Component message, OnPress onPress, SoundEvent sound) {
        super(x, y, width, height, texture, normalU, normalV, hoverU, hoverV, textureWidth, textureHeight, normalTextColor, hoverTextColor, backgroundColor, hasBackgroundColor, message, onPress, sound);
    }
}
