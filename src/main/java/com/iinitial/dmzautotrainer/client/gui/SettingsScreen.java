package com.iinitial.dmzautotrainer.client.gui;

import com.dragonminez.client.gui.buttons.CustomTextureButton;
import com.dragonminez.client.gui.buttons.SwitchButton;
import com.dragonminez.client.gui.character.util.BaseMenuScreen;
import com.dragonminez.client.util.ScrollbarState;
import com.dragonminez.client.util.TextUtil;
import com.dragonminez.common.init.MainSounds;
import com.iinitial.dmzautotrainer.common.config.ClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SettingsScreen extends BaseMenuScreen {
    private static final ResourceLocation MENU_BIG = ResourceLocation.fromNamespaceAndPath("dragonminez", "textures/gui/menu/menubig.png");
    private static final ResourceLocation STAT_BUTTONS = ResourceLocation.fromNamespaceAndPath("dragonminez", "textures/gui/buttons/characterbuttons.png");
    private static final String[] SETTINGS = new String[]{"Auto Trainer", "Repeat Training", "Levels To Complete Training"};
    private static final int SETTINGS_ITEM_HEIGHT = 20;
    private static final int MAX_VISIBLE_SETTINGS = 7;
    private int tickCount = 0;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int holdTicks = 0;
    private int heldSettingIndex = -1;
    private int heldDelta = 0;
    private final ScrollbarState scrollBar = new ScrollbarState();
    private final List<SettingsOption> settingsOptions = new ArrayList<>();
    private final List<CustomTextureButton> decreaseButtons = new ArrayList<>();
    private final List<CustomTextureButton> increaseButtons = new ArrayList<>();
    private final List<SwitchButton> switchButtons = new ArrayList<>();

    public SettingsScreen () {
        super(Component.literal("trainer settings"));
    }

    protected void init() {
        super.init();
        this.initializeSettingsOptions();
        this.initSettingsButtons();
        this.updateSettingsList();
    }

    private void initializeSettingsOptions() {
        this.settingsOptions.clear();
        this.settingsOptions.add(new SettingsScreen.SettingsOption("enableAutoTrainer", SettingsType.BOOLEAN, ClientConfig.getAutoTrain() ? 1.0F : 0.0F, 0.0F, 1.0F, (v) -> ClientConfig.setAutoTrain(v > 0.0F)));
        this.settingsOptions.add(new SettingsScreen.SettingsOption("enableRepeatTraining", SettingsType.BOOLEAN, ClientConfig.getRepeatTraining() ? 1.0F : 0.0F, 0.0F, 1.0F, (v) -> ClientConfig.setRepeatTraining(v > 0.0F)));
        this.settingsOptions.add(new SettingsScreen.SettingsOption("levelsToComplete", SettingsType.INT, ClientConfig.getLevelsToComplete(), 1, 100, ClientConfig::setLevelsToComplete));
    }

    private void initSettingsButtons() {
        this.clearSettingsButtons();
        LivingEntity player = this.minecraft.player;
        int rightPanelX = this.getRightPanelX() - 5;
        int centerY = this.getUiHeight() / 2;
        int rightPanelY = centerY - 105;
        int startY = rightPanelY + 35;
        int visibleStart = this.scrollOffset;
        int visibleEnd = Math.min(visibleStart + 7, this.settingsOptions.size());

        for(int i = visibleStart; i < visibleEnd; ++i) {
            SettingsScreen.SettingsOption option = this.settingsOptions.get(i);
            int itemY = startY + (i - visibleStart) * 20;
            if (option.type == SettingsScreen.SettingsType.BOOLEAN) {
                boolean isOn = option.value > 0.0F;
                int switchX = rightPanelX + 65;
                int switchY = itemY + 3;
                int finalI = i;
                SwitchButton switchBtn = new SwitchButton(switchX, switchY, isOn, Component.empty(), (button) -> {
                    this.modifySettingsValue(finalI, 1);
                    ((SwitchButton)button).toggle();
                    if (isOn) {
                        player.playSound(MainSounds.SWITCH_OFF.get());
                    } else {
                        player.playSound(MainSounds.SWITCH_ON.get());
                    }

                });
                this.switchButtons.add(switchBtn);
                this.addRenderableWidget(switchBtn);
            } else {
                int finalI1 = i;
                CustomTextureButton decreaseBtn = (new CustomTextureButton.Builder()).position(rightPanelX + 25, itemY + 3).size(14, 11).texture(STAT_BUTTONS).textureCoords(142, 0, 142, 10).textureSize(10, 10).onPress((button) -> {
                    this.modifySettingsValue(finalI1, -1);
                    this.heldSettingIndex = finalI1;
                    this.heldDelta = -1;
                    this.holdTicks = 0;
                }).build();
                this.decreaseButtons.add(decreaseBtn);
                this.addRenderableWidget(decreaseBtn);
                int finalI2 = i;
                CustomTextureButton increaseBtn = (new CustomTextureButton.Builder()).position(rightPanelX + 108, itemY + 3).size(14, 11).texture(STAT_BUTTONS).textureCoords(0, 0, 0, 10).textureSize(10, 10).onPress((button) -> {
                    this.modifySettingsValue(finalI2, 1);
                    this.heldSettingIndex = finalI2;
                    this.heldDelta = 1;
                    this.holdTicks = 0;
                }).build();
                this.increaseButtons.add(increaseBtn);
                this.addRenderableWidget(increaseBtn);
            }
        }

    }

    private void clearSettingsButtons() {
        for(CustomTextureButton btn : this.decreaseButtons) {
            this.removeWidget(btn);
        }

        for(CustomTextureButton btn : this.increaseButtons) {
            this.removeWidget(btn);
        }

        for(SwitchButton btn : this.switchButtons) {
            this.removeWidget(btn);
        }

        this.decreaseButtons.clear();
        this.increaseButtons.clear();
        this.switchButtons.clear();
    }

    public void tick() {
        super.tick();
        ++this.tickCount;
        if (this.heldSettingIndex != -1) {
            ++this.holdTicks;
            if (this.holdTicks > 10 && this.holdTicks % 2 == 0) {
                this.modifySettingsValue(this.heldSettingIndex, this.heldDelta);
            }
        }
    }

    private void updateSettingsList() {
        this.maxScroll = Math.max(0, this.settingsOptions.size() - 7);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.isNotAnimating()) {
            this.renderBackground(graphics);
        }

        int uiMouseX = (int)Math.round(this.toUiX((double)mouseX));
        int uiMouseY = (int)Math.round(this.toUiY((double)mouseY));
        this.beginUiScale(graphics);
        this.applyZoom(graphics, partialTick);

        int leftOffset = this.getLeftPanelSwitchOffset(partialTick);
        graphics.pose().pushPose();
        graphics.pose().translate((float)leftOffset, 0.0F, 0.0F);
        this.renderLeftPanel(graphics, uiMouseX - leftOffset, uiMouseY);
        graphics.pose().popPose();
        int rightOffset = this.getRightPanelSwitchOffset(partialTick);
        graphics.pose().pushPose();
        graphics.pose().translate((float)rightOffset, 0.0F, 0.0F);
        this.renderRightPanel(graphics, uiMouseX - rightOffset, uiMouseY);
        graphics.pose().popPose();

        super.render(graphics, uiMouseX, uiMouseY, partialTick);
        this.endUiScale(graphics);
    }

    private int getLeftPanelX() {
        return this.getUiWidth() / 2 - 143;
    }

    private int getRightPanelX() {
        return this.getUiWidth() / 2 + 2;
    }

    private void renderLeftPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int leftPanelX = this.getLeftPanelX();
        int centerY = this.getUiHeight() / 2;
        int leftPanelY = centerY - 105;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(MENU_BIG, leftPanelX, centerY - 105, 0.0F, 0.0F, 141, 213, 256, 256);
        graphics.blit(MENU_BIG, leftPanelX + 17, centerY - 95, 142.0F, 22.0F, 107, 21, 256, 256);
        TextUtil.drawCenteredStringWithBorder(graphics, this.font, this.tr("gui.dragonminez.config.options", new Object[0]).withStyle(ChatFormatting.BOLD), leftPanelX + 70, leftPanelY + 17, -10496);
        this.renderSettingsList(graphics, leftPanelX, leftPanelY, mouseX, mouseY);
    }

    private void renderSettingsList(GuiGraphics graphics, int panelX, int panelY, int mouseX, int mouseY) {
        int startY = panelY + 35;
        int visibleStart = this.scrollOffset;
        int visibleEnd = Math.min(visibleStart + 7, SETTINGS.length);
        graphics.enableScissor(this.toScreenCoord((double)(panelX + 5)), this.toScreenCoord((double)startY), this.toScreenCoord((double)(panelX + 144)), this.toScreenCoord((double)(startY + 140)));
        graphics.pose().pushPose();
        graphics.pose().scale(0.75F, 0.75F, 0.75F);

        for(int i = visibleStart; i < visibleEnd; ++i) {
            String option = SETTINGS[i];
            int itemY = startY + (i - visibleStart) * 20;
            TextUtil.drawStringWithBorder(graphics, this.font, this.txt(option), (int)((float)(panelX + 15) / 0.75F), (int)((float)itemY / 0.75F) + 6, -1);
        }

        graphics.pose().popPose();
        graphics.disableScissor();
        this.scrollBar.update(panelX + 128, 3, startY, 140, (float)this.maxScroll);
        if (this.maxScroll > 0) {
            int scrollBarX = panelX + 128;
            int scrollBarHeight = 140;
            int totalItems = SETTINGS.length;
            graphics.fill(scrollBarX, startY, scrollBarX + 3, startY + scrollBarHeight, -13421773);
            float scrollPercent = (float)this.scrollOffset / (float)this.maxScroll;
            float visiblePercent = 7.0F / (float)totalItems;
            int indicatorHeight = Math.max(20, (int)((float)scrollBarHeight * visiblePercent));
            int indicatorY = startY + (int)((float)(scrollBarHeight - indicatorHeight) * scrollPercent);
            graphics.fill(scrollBarX, indicatorY, scrollBarX + 3, indicatorY + indicatorHeight, -5592406);
        }

    }

    private void renderRightPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int rightPanelX = this.getRightPanelX();
        int centerY = this.getUiHeight() / 2;
        int rightPanelY = centerY - 105;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(MENU_BIG, rightPanelX, centerY - 105, 0.0F, 0.0F, 141, 213, 256, 256);
        graphics.blit(MENU_BIG, rightPanelX + 17, centerY - 95, 142.0F, 22.0F, 107, 21, 256, 256);
        TextUtil.drawCenteredStringWithBorder(graphics, this.font, this.tr("gui.dragonminez.config.values", new Object[0]).withStyle(ChatFormatting.BOLD), rightPanelX + 70, rightPanelY + 17, -10496);
        this.renderSettingsValues(graphics, rightPanelX, rightPanelY);
    }

    private void renderSettingsValues(GuiGraphics graphics, int panelX, int panelY) {
        int startY = panelY + 35;
        int visibleStart = this.scrollOffset;
        int visibleEnd = Math.min(visibleStart + 7, SETTINGS.length);

        for(int i = visibleStart; i < visibleEnd; ++i) {
            SettingsOption option = (SettingsOption)this.settingsOptions.get(i);
            int itemY = startY + (i - visibleStart) * 20;
            if (option.type != SettingsType.BOOLEAN) {
                String valueText;
                valueText = String.valueOf((int)option.value);

                graphics.pose().pushPose();
                graphics.pose().scale(0.75F, 0.75F, 0.75F);
                TextUtil.drawCenteredStringWithBorder(graphics, this.font, this.txt(valueText), (int)((float)(panelX + 69) / 0.75F), (int)((float)(itemY + 5) / 0.75F), -1);
                graphics.pose().popPose();
            }
        }
    }

    private void modifySettingsValue(int index, int delta) {
        if (index >= 0 && index < this.settingsOptions.size()) {
            SettingsScreen.SettingsOption option = (SettingsScreen.SettingsOption)this.settingsOptions.get(index);
            boolean isShiftDown = Screen.hasShiftDown();
            if (option.type == SettingsScreen.SettingsType.BOOLEAN) {
                option.value = option.value > 0.0F ? 0.0F : 1.0F;
            } else if (option.type == SettingsScreen.SettingsType.INT) {
                int step = isShiftDown ? 5 : 1;
                option.value = Math.max(option.min, Math.min(option.max, option.value + (float)(delta * step)));
            }

            option.setter.accept(option.value);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double uiMouseX = this.toUiX(mouseX);
        double uiMouseY = this.toUiY(mouseY);
        int leftPanelX = this.getLeftPanelX();
        int rightPanelX = this.getRightPanelX();
        int centerY = this.getUiHeight() / 2;
        int panelY = centerY - 105;
        boolean overLeft = uiMouseX >= (double)leftPanelX && uiMouseX <= (double)(leftPanelX + 148);
        boolean overRight = uiMouseX >= (double)rightPanelX && uiMouseX <= (double)(rightPanelX + 148);
        if ((overLeft || overRight) && uiMouseY >= (double)(panelY + 40) && uiMouseY <= (double)(panelY + 219)) {
            int scrollAmount = (int)Math.signum(delta);
            this.scrollOffset = Math.max(0, Math.min(this.maxScroll, this.scrollOffset - scrollAmount));
            this.initSettingsButtons();
            return true;
        } else {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double uiMouseX = this.toUiX(mouseX);
        double uiMouseY = this.toUiY(mouseY);
        if (this.scrollBar.tryStartDrag(uiMouseX, uiMouseY)) {
            this.scrollOffset = Math.round(this.scrollBar.scrollFor(uiMouseY));
            this.initSettingsButtons();
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrollBar.isDragging()) {
            this.scrollOffset = Math.round(this.scrollBar.scrollFor(this.toUiY(mouseY)));
            this.initSettingsButtons();
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.heldSettingIndex = -1;
        if (this.scrollBar.isDragging()) {
            this.scrollBar.stopDrag();
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    private enum SettingsType {
        INT,
        BOOLEAN,
    }

    private static class SettingsOption {
        String key;
        SettingsType type;
        float value;
        float min;
        float max;
        Consumer<Float> setter;
        Runnable action;

        SettingsOption(String key, SettingsType type, float value, float min, float max, Consumer<Float> setter) {
            this.key = key;
            this.type = type;
            this.value = value;
            this.min = min;
            this.max = max;
            this.setter = setter;
        }
    }
}
