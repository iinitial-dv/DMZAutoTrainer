package com.iinitial.dmzautotrainer.client.gui;

import com.dragonminez.client.gui.buttons.CustomTextureButton;
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
import net.minecraft.client.gui.screens.Screen;
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
    private static final ResourceLocation STAT_BUTTONS = ResourceLocation.fromNamespaceAndPath("dragonminez", "textures/gui/buttons/characterbuttons.png");
    private static final String[] SETTINGS = new String[]{"Auto Trainer", "Repeat Training", "Levels To Complete Training"};
    private static final int SETTINGS_ITEM_HEIGHT = 20;
    private static final int PANEL_HEIGHT = 213;

    private int holdTicks = 0;
    private int heldSettingIndex = -1;
    private int heldDelta = 0;
    private final List<SettingsOption> settingsOptions = new ArrayList<>();
    private final List<CustomTextureButton> decreaseButtons = new ArrayList<>();
    private final List<CustomTextureButton> increaseButtons = new ArrayList<>();
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
        this.settingsOptions.add(new SettingsOption(SettingsType.BOOLEAN, config.isAutoTrainEnabled() ? 1.0F : 0.0F, 0.0F, 1.0F, (v) -> config.setAutoTrain(v > 0.0F)));
        this.settingsOptions.add(new SettingsOption(SettingsType.BOOLEAN, config.isRepeatTrainingEnabled() ? 1.0F : 0.0F, 0.0F, 1.0F, (v) -> config.setRepeatTraining(v > 0.0F)));
        this.settingsOptions.add(new SettingsOption(SettingsType.INT, config.getLevelsToComplete(), 1, 100, config::setLevelsToComplete));
    }

    private void initSettingsButtons() {
        this.clearSettingsButtons();
        LivingEntity player = this.minecraft.player;
        int rightPanelX = this.getRightPanelX() - 5;
        int startY = this.getPanelY() + 35;

        for (int i = 0; i < this.settingsOptions.size(); ++i) {
            SettingsOption option = this.settingsOptions.get(i);
            int itemY = startY + i * SETTINGS_ITEM_HEIGHT;
            int index = i;
            if (option.type == SettingsType.BOOLEAN) {
                boolean isOn = option.value > 0.0F;
                SwitchButton switchBtn = new SwitchButton(rightPanelX + 65, itemY + 3, isOn, Component.empty(), (button) -> {
                    this.modifySettingsValue(index, 1);
                    ((SwitchButton) button).toggle();
                    if (isOn) {
                        player.playSound(MainSounds.SWITCH_OFF.get());
                    } else {
                        player.playSound(MainSounds.SWITCH_ON.get());
                    }
                });
                this.switchButtons.add(switchBtn);
                this.restingX.put(switchBtn, rightPanelX + 65);
                this.addRenderableWidget(switchBtn);
            } else {
                CustomTextureButton decreaseBtn = new CustomTextureButton.Builder().position(rightPanelX + 25, itemY + 3).size(14, 11).texture(STAT_BUTTONS).textureCoords(142, 0, 142, 10).textureSize(10, 10).onPress((button) -> {
                    this.modifySettingsValue(index, -1);
                    this.heldSettingIndex = index;
                    this.heldDelta = -1;
                    this.holdTicks = 0;
                }).build();
                this.decreaseButtons.add(decreaseBtn);
                this.restingX.put(decreaseBtn, rightPanelX + 25);
                this.addRenderableWidget(decreaseBtn);

                CustomTextureButton increaseBtn = new CustomTextureButton.Builder().position(rightPanelX + 108, itemY + 3).size(14, 11).texture(STAT_BUTTONS).textureCoords(0, 0, 0, 10).textureSize(10, 10).onPress((button) -> {
                    this.modifySettingsValue(index, 1);
                    this.heldSettingIndex = index;
                    this.heldDelta = 1;
                    this.holdTicks = 0;
                }).build();
                this.increaseButtons.add(increaseBtn);
                this.restingX.put(increaseBtn, rightPanelX + 108);
                this.addRenderableWidget(increaseBtn);
            }
        }
    }

    private void clearSettingsButtons() {
        for (CustomTextureButton btn : this.decreaseButtons) {
            this.removeWidget(btn);
        }
        for (CustomTextureButton btn : this.increaseButtons) {
            this.removeWidget(btn);
        }
        for (SwitchButton btn : this.switchButtons) {
            this.removeWidget(btn);
        }

        this.decreaseButtons.clear();
        this.increaseButtons.clear();
        this.switchButtons.clear();
        this.restingX.clear();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.heldSettingIndex != -1) {
            ++this.holdTicks;
            if (this.holdTicks > 10 && this.holdTicks % 2 == 0) {
                this.modifySettingsValue(this.heldSettingIndex, this.heldDelta);
            }
        }
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

        int startY = rightPanelY + 35;
        for (int i = 0; i < this.settingsOptions.size(); ++i) {
            SettingsOption option = this.settingsOptions.get(i);
            if (option.type == SettingsType.BOOLEAN) continue;

            int itemY = startY + i * SETTINGS_ITEM_HEIGHT;
            graphics.pose().pushPose();
            graphics.pose().scale(0.75F, 0.75F, 0.75F);
            TextUtil.drawCenteredStringWithBorder(graphics, this.font, this.txt(String.valueOf((int) option.value)), (int) ((float) (rightPanelX + 69) / 0.75F), (int) ((float) (itemY + 5) / 0.75F), -1);
            graphics.pose().popPose();
        }
    }

    private void modifySettingsValue(int index, int delta) {
        if (index >= 0 && index < this.settingsOptions.size()) {
            SettingsOption option = this.settingsOptions.get(index);
            if (option.type == SettingsType.BOOLEAN) {
                option.value = option.value > 0.0F ? 0.0F : 1.0F;
            } else if (option.type == SettingsType.INT) {
                int step = Screen.hasShiftDown() ? 5 : 1;
                option.value = Math.max(option.min, Math.min(option.max, option.value + (float) (delta * step)));
            }

            option.setter.accept(option.value);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.heldSettingIndex = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private enum SettingsType {
        INT,
        BOOLEAN,
    }

    private static class SettingsOption {
        SettingsType type;
        float value;
        float min;
        float max;
        Consumer<Float> setter;

        SettingsOption(SettingsType type, float value, float min, float max, Consumer<Float> setter) {
            this.type = type;
            this.value = value;
            this.min = min;
            this.max = max;
            this.setter = setter;
        }
    }
}