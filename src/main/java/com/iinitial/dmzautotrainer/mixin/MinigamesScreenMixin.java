package com.iinitial.dmzautotrainer.mixin;

import com.dragonminez.client.gui.buttons.CustomTextureButton;
import com.dragonminez.client.gui.buttons.TexturedTextButton;
import com.dragonminez.client.gui.character.MinigamesScreen;
import com.dragonminez.client.gui.character.minigames.ControlGameScreen;
import com.dragonminez.client.gui.character.minigames.GravityGameScreen;
import com.dragonminez.client.gui.character.minigames.MemoryGameScreen;
import com.dragonminez.client.gui.character.minigames.PrecisionGameScreen;
import com.dragonminez.client.gui.character.minigames.RythmGameScreen;
import com.dragonminez.client.gui.character.util.BaseMenuScreen;
import com.dragonminez.client.gui.character.util.ScaledScreen;
import com.dragonminez.client.util.ScrollbarState;
import com.dragonminez.client.util.TextUtil;
import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.config.TrainingConfig;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.C2S.SummonPlayerShadowDummyC2S;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;

import com.iinitial.dmzautotrainer.client.gui.SettingsScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinigamesScreen.class)
public abstract class MinigamesScreenMixin extends Screen {
    private static final ResourceLocation SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("dmzautotrainer", "textures/gui/buttons/settingsbutton.png");
    private TexturedTextButton settingsButton;

    protected MinigamesScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSettingsButton (CallbackInfo ci) {
        int centerX = this.width / 2;   int centerY = this.height / 2;;
        int bWidth = 110;               int bHeight = 16;
        int bX = centerX - bWidth;      int bY = 5;

        settingsButton = new TexturedTextButton.Builder().position(bX, bY).size(bWidth,bHeight).texture(SETTINGS_TEXTURE).textureCoords(0, 0, 0, 0).textureSize(bWidth, bHeight).message(Component.literal("Auto Train Settings"))
                .onPress(button -> {
                    Minecraft.getInstance().setScreen(new SettingsScreen());
                })
                .build();

        this.addRenderableWidget(settingsButton);
    }
}
