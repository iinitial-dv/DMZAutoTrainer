package com.iinitial.dmzautotrainer.mixin;

import com.dragonminez.client.gui.buttons.TexturedTextButton;
import com.dragonminez.client.gui.character.MinigamesScreen;
import com.dragonminez.client.gui.character.util.BaseMenuScreen;
import com.iinitial.dmzautotrainer.client.gui.SettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinigamesScreen.class)
public abstract class MinigamesScreenMixin extends BaseMenuScreen {
    private static final ResourceLocation SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("dmzautotrainer", "textures/gui/buttons/settingsbutton.png");
    private TexturedTextButton settingsButton;

    protected MinigamesScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSettingsButton (CallbackInfo ci) {
        int centerX = this.getUiWidth() / 2;    int centerY = this.getUiHeight() / 2;
        int bWidth = 110;                       int bHeight = 20;
        int bX = centerX - bWidth / 2;          int bY = centerY - 120;

        settingsButton = new TexturedTextButton.Builder().position(bX, bY).size(bWidth,bHeight).texture(SETTINGS_TEXTURE).textureCoords(0, 0, 0, 0).textureSize(bWidth, bHeight).message(Component.literal("Auto Train Settings"))
                .onPress(button -> {
                    Minecraft.getInstance().setScreen(new SettingsScreen());
                }).build();

        this.addRenderableWidget(settingsButton);
    }
}
