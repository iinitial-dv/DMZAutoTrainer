package com.iinitial.dmzautotrainer.mixin;

import com.dragonminez.client.gui.character.minigames.ControlGameScreen;
import com.iinitial.dmzautotrainer.client.SpoofedInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ControlGameScreen.class, remap = false)
public class ControlGameScreenMixin {
    @Redirect(
            method = "isHeld",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetKey(JI)I")
    )
    private int redirectGlfwGetKey(long window, int key) {
        if (SpoofedInput.isHeld(key)) {
            return GLFW.GLFW_PRESS;
        }
        return GLFW.glfwGetKey(window, key);
    }
}