package com.iinitial.dmzautotrainer.mixin;

import com.dragonminez.client.gui.buttons.TexturedTextButton;
import com.dragonminez.client.gui.character.MinigamesScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinigamesScreen.class)
public abstract class MinigamesScreenMixin {

    static {
        System.out.println("DMZAutoTrainer: MinigamesScreenMixin loaded");
    }

    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("dragonminez", "textures/gui/buttons/menubuttons.png");
    private TexturedTextButton settingsButton;

    protected MinigamesScreenMixin(Component title) {
        //super(title);
        super();

    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSettingsButton (CallbackInfo ci) {
        System.out.println("DMZAutoTrainer: MinigamesScreen init injected");
        settingsButton = new TexturedTextButton.Builder()
                .position(50, 50)
                .size(150,20)
                .texture(BUTTON_TEXTURE)
                .textureCoords(0, 50, 0, 50)
                .textureSize(150, 20)
                .message(Component.literal("Auto Train Settings"))
                .onPress(button -> {
                    System.out.println("Pressed! Yay!");
                })
                .build();

        //this.addRenderableWidget(settingsButton);
    }
}
