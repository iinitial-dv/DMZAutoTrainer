package com.iinitial.dmzautotrainer.client.gui;

import com.dragonminez.client.gui.buttons.SwitchButton;
import com.dragonminez.client.gui.character.util.BaseMenuScreen;
import com.dragonminez.client.util.TextUtil;
import com.dragonminez.common.init.MainSounds;
import com.iinitial.dmzautotrainer.common.config.ClientConfig;
import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SettingsScreen extends BaseMenuScreen {
    private static final ResourceLocation MENU_BIG = ResourceLocation.fromNamespaceAndPath("dragonminez", "textures/gui/menu/menubig.png");
    private static final String[] SETTINGS = new String[]{"Auto Trainer", "Repeat Training"};
    private static final int SETTINGS_ITEM_HEIGHT = 20;
    private static final int PANEL_HEIGHT = 213;

    private final List<SettingsOption> settingsOptions = new ArrayList<>();
    private final List<SwitchButton> switchButtons = new ArrayList<>();
    private final Map<AbstractWidget, Integer> restingX = new IdentityHashMap<>();

    public SettingsScreen() {
        super(Component.literal("trainer settings"));
    }

    @Override
    protected void init() {
        super.init();
        this.initializeSettingsOptions();
        this.initSettingsButtons();
    }

    private void initializeSettingsOptions() {
        this.settingsOptions.clear();
        ClientConfig config = ConfigManager.client();
        this.settingsOptions.add(new SettingsOption(config.isAutoTrainEnabled(), config::setAutoTrain));
        this.settingsOptions.add(new SettingsOption(config.isRepeatTrainingEnabled(), config::setRepeatTraining));
    }

    private void initSettingsButtons() {
        this.clearSettingsButtons();
        LivingEntity player = this.minecraft.player;
        int switchX = this.getRightPanelX() - 5 + 65;
        int startY = this.getPanelY() + 35;

        for (int i = 0; i < this.settingsOptions.size(); ++i) {
            SettingsOption option = this.settingsOptions.get(i);
            SwitchButton switchBtn = new SwitchButton(switchX, startY + i * SETTINGS_ITEM_HEIGHT + 3, option.value, Component.empty(), (button) -> {
                option.value = !option.value;
                option.setter.accept(option.value);
                ((SwitchButton) button).toggle();
                if (option.value) {
                    player.playSound(MainSounds.SWITCH_ON.get());
                } else {
                    player.playSound(MainSounds.SWITCH_OFF.get());
                }
            });
            this.switchButtons.add(switchBtn);
            this.restingX.put(switchBtn, switchX);
            this.addRenderableWidget(switchBtn);
        }
    }

    private void clearSettingsButtons() {
        for (SwitchButton btn : this.switchButtons) {
            this.removeWidget(btn);
        }
        this.switchButtons.clear();
        this.restingX.clear();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderMenuBackground(graphics, partialTick);

        int uiMouseX = (int) Math.round(this.toUiX(mouseX));
        int uiMouseY = (int) Math.round(this.toUiY(mouseY));
        this.beginUiScale(graphics);

        float leftOffset = this.getLeftPanelSwitchOffset(partialTick);
        graphics.pose().pushPose();
        graphics.pose().translate(leftOffset, 0.0F, 0.0F);
        this.renderLeftPanel(graphics);
        graphics.pose().popPose();

        float rightOffset = this.getRightPanelSwitchOffset(partialTick);
        graphics.pose().pushPose();
        graphics.pose().translate(rightOffset, 0.0F, 0.0F);
        this.renderRightPanel(graphics);
        graphics.pose().popPose();

        for (Map.Entry<AbstractWidget, Integer> entry : this.restingX.entrySet()) {
            slideX(entry.getKey(), entry.getValue(), rightOffset);
        }

        super.render(graphics, uiMouseX, uiMouseY, partialTick);
        this.endUiScale(graphics);
    }

    private int getLeftPanelX() {
        return this.getUiWidth() / 2 - 143;
    }

    private int getRightPanelX() {
        return this.getUiWidth() / 2 + 2;
    }

    private int getPanelY() {
        return this.getUiHeight() / 2 - 105;
    }

    private void renderLeftPanel(GuiGraphics graphics) {
        int leftPanelX = this.getLeftPanelX();
        int leftPanelY = this.getPanelY();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(graphics, MENU_BIG, leftPanelX, leftPanelY, 0, 0, 141, PANEL_HEIGHT);
        blit(graphics, MENU_BIG, leftPanelX + 17, leftPanelY + 10, 142, 22, 107, 21);
        TextUtil.drawCenteredStringWithBorder(graphics, this.font, this.tr("gui.dragonminez.config.options").withStyle(ChatFormatting.BOLD), leftPanelX + 70, leftPanelY + 17, 0xFFFFD700);

        int startY = leftPanelY + 35;
        graphics.pose().pushPose();
        graphics.pose().scale(0.75F, 0.75F, 0.75F);
        for (int i = 0; i < SETTINGS.length; ++i) {
            int itemY = startY + i * SETTINGS_ITEM_HEIGHT;
            TextUtil.drawStringWithBorder(graphics, this.font, this.txt(SETTINGS[i]), (int) ((float) (leftPanelX + 15) / 0.75F), (int) ((float) itemY / 0.75F) + 6, -1);
        }
        graphics.pose().popPose();
    }

    private void renderRightPanel(GuiGraphics graphics) {
        int rightPanelX = this.getRightPanelX();
        int rightPanelY = this.getPanelY();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(graphics, MENU_BIG, rightPanelX, rightPanelY, 0, 0, 141, PANEL_HEIGHT);
        blit(graphics, MENU_BIG, rightPanelX + 17, rightPanelY + 10, 142, 22, 107, 21);
        TextUtil.drawCenteredStringWithBorder(graphics, this.font, this.tr("gui.dragonminez.config.values").withStyle(ChatFormatting.BOLD), rightPanelX + 70, rightPanelY + 17, 0xFFFFD700);
    }

    private static class SettingsOption {
        boolean value;
        Consumer<Boolean> setter;

        SettingsOption(boolean value, Consumer<Boolean> setter) {
            this.value = value;
            this.setter = setter;
        }
    }
}