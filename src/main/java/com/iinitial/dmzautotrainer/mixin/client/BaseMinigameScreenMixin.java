package com.iinitial.dmzautotrainer.mixin.client;

import com.dragonminez.client.gui.character.minigames.BaseMinigameScreen;
import com.iinitial.dmzautotrainer.client.session.ClientSessionState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BaseMinigameScreen.class, remap = false)
public abstract class BaseMinigameScreenMixin extends Screen {
    protected BaseMinigameScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void endSessionWhenEscaping(int keyCode, int scanCode, int modifiers,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            ClientSessionState.endSessionEarly();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderSessionTimer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                    CallbackInfo ci) {
        long sessionSeconds = ClientSessionState.getSessionSecondsRemaining();
        if (sessionSeconds <= 0L) {
            return;
        }

        graphics.drawCenteredString(
                this.font,
                Component.literal("Training time remaining: " + ClientSessionState.formatDuration(sessionSeconds)),
                this.width / 2,
                62,
                0xFFD54F
        );
    }
}
