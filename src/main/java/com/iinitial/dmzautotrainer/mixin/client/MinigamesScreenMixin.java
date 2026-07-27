package com.iinitial.dmzautotrainer.mixin.client;

import com.dragonminez.client.gui.buttons.TexturedTextButton;
import com.dragonminez.client.gui.character.MinigamesScreen;
import com.dragonminez.client.gui.character.util.BaseMenuScreen;
import com.dragonminez.client.util.TextUtil;
import com.iinitial.dmzautotrainer.client.gui.SettingsScreen;
import com.iinitial.dmzautotrainer.client.session.ClientSessionState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinigamesScreen.class)
public abstract class MinigamesScreenMixin extends BaseMenuScreen {
    private static final ResourceLocation SETTINGS_TEXTURE = ResourceLocation.fromNamespaceAndPath("dmzautotrainer", "textures/gui/buttons/settingsbutton.png");
    private static final int LEFT_PANEL_X = 12;
    private static final int PANEL_WIDTH = 141;
    private static final int PANEL_HEIGHT = 213;
    private static final int BUTTON_WIDTH = 110;
    private static final int BUTTON_HEIGHT = 20;

    private TexturedTextButton settingsButton;

    protected MinigamesScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSettingsButton(CallbackInfo ci) {
        ClientSessionState.syncStatus();

        int panelCenterX = LEFT_PANEL_X + PANEL_WIDTH / 2;
        int centerY = this.getUiHeight() / 2;
        int panelY = centerY - 105;
        int bX = panelCenterX - BUTTON_WIDTH / 2;
        int bY = panelY + PANEL_HEIGHT - 30;

        settingsButton = new TexturedTextButton.Builder()
                .position(bX, bY)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .texture(SETTINGS_TEXTURE)
                .textureCoords(0, 0, 0, 0)
                .textureSize(BUTTON_WIDTH, BUTTON_HEIGHT)
                .message(Component.literal("Auto Train Settings"))
                .onPress(button -> Minecraft.getInstance().setScreen(new SettingsScreen()))
                .build();

        this.addRenderableWidget(settingsButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void slideSettingsButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.settingsButton == null) return;

        int leftOffset = this.getLeftPanelSwitchOffset(partialTick);
        int panelCenterX = LEFT_PANEL_X + PANEL_WIDTH / 2;
        int centerY = this.getUiHeight() / 2;
        int panelY = centerY - 105;

        this.settingsButton.setX(panelCenterX - BUTTON_WIDTH / 2 + leftOffset);
        this.settingsButton.setY(panelY + PANEL_HEIGHT - 30);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderCooldown(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!ClientSessionState.isSessionsEnabled()) return;

        long cooldownSeconds = ClientSessionState.getCooldownSecondsRemaining();

        int leftOffset = this.getLeftPanelSwitchOffset(partialTick);
        int panelCenterX = LEFT_PANEL_X + PANEL_WIDTH / 2;
        int centerY = this.getUiHeight() / 2;
        int panelY = centerY - 105;

        this.beginUiScale(graphics);
        graphics.pose().pushPose();
        graphics.pose().translate((float) leftOffset, 0.0F, 0.0F);

        if (cooldownSeconds > 0L) {
            TextUtil.drawCenteredStringWithBorder(
                    graphics, this.font, this.txt("On Cooldown..."),
                    panelCenterX, panelY + PANEL_HEIGHT - 50, 0xFF5555
            );
            TextUtil.drawCenteredStringWithBorder(
                    graphics, this.font, this.txt(ClientSessionState.formatDuration(cooldownSeconds)),
                    panelCenterX, panelY + PANEL_HEIGHT - 40, 0xFF5555
            );
        } else {
            TextUtil.drawCenteredStringWithBorder(
                    graphics, this.font, this.txt("Cooldown Lifted!"),
                    panelCenterX, panelY + PANEL_HEIGHT - 40, 0x55FF55
            );
        }
        graphics.pose().popPose();
        this.endUiScale(graphics);
    }
}