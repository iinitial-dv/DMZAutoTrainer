package com.iinitial.dmzautotrainer.client.gui;

import com.dragonminez.Reference;
import com.dragonminez.client.gui.character.util.BaseMenuScreen;
import com.dragonminez.client.gui.hud.HudRender;
import com.dragonminez.client.util.ScrollbarState;
import com.dragonminez.client.util.TextUtil;
import com.dragonminez.common.init.MainSounds;
import com.iinitial.dmzautotrainer.common.config.ClientConfig;
import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SettingsScreen extends BaseMenuScreen {
    private static final ResourceLocation MENU_BIG = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/menu/menubig.png");
    private static final ResourceLocation STAT_BUTTONS = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/buttons/characterbuttons.png");

    private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP = 35;
    private static final int LIST_HEIGHT = 140;
    private static final int LIST_CLIP_WIDTH = 131;
    private static final float TEXT_SCALE = 0.75f;

    private final ScrollbarState scrollBar = new ScrollbarState().step(ROW_HEIGHT).minThumb(20);
    private final List<SettingsOption> settingsOptions = new ArrayList<>();
    private int holdTicks = 0;
    private int heldSettingIndex = -1;
    private int heldDelta = 0;

    public SettingsScreen() {
        super(Component.literal("trainer settings"));
    }

    @Override
    protected void init() {
        super.init();
        this.initializeSettingsOptions();
    }

    private void initializeSettingsOptions() {
        this.settingsOptions.clear();
        ClientConfig config = ConfigManager.client();
        this.settingsOptions.add(new SettingsOption("Auto Trainer", SettingsType.BOOLEAN, config.isAutoTrainEnabled() ? 1.0F : 0.0F, 0.0F, 1.0F, v -> config.setAutoTrain(v > 0.0F)));
        this.settingsOptions.add(new SettingsOption("Repeat Training", SettingsType.BOOLEAN, config.isRepeatTrainingEnabled() ? 1.0F : 0.0F, 0.0F, 1.0F, v -> config.setRepeatTraining(v > 0.0F)));
        this.settingsOptions.add(new SettingsOption("Levels To Complete Training", SettingsType.INT, config.getLevelsToComplete(), 1, 100, config::setLevelsToComplete));
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

    private int getLeftPanelX() {
        return this.getUiWidth() / 2 - 143;
    }

    private int getRightPanelX() {
        return this.getUiWidth() / 2 + 2;
    }

    private int getPanelY() {
        return this.getUiHeight() / 2 - 105;
    }

    private int listTop() {
        return this.getPanelY() + LIST_TOP;
    }

    private void updateLayout() {
        int leftX = this.getLeftPanelX();
        this.scrollBar.layout(leftX + 5, this.listTop(), LIST_CLIP_WIDTH, LIST_HEIGHT, this.settingsOptions.size() * ROW_HEIGHT).barAt(leftX + 128);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderMenuBackground(graphics, partialTick);

        int uiMouseX = (int) Math.round(this.toUiX(mouseX));
        int uiMouseY = (int) Math.round(this.toUiY(mouseY));

        this.updateLayout();
        this.beginUiScale(graphics);

        float leftOffset = this.getLeftPanelSwitchOffset(partialTick);
        float rightOffset = this.getRightPanelSwitchOffset(partialTick);

        graphics.pose().pushPose();
        graphics.pose().translate(leftOffset, 0.0F, 0.0F);
        this.renderLeftPanel(graphics, uiMouseX - Math.round(leftOffset), uiMouseY);
        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().translate(rightOffset, 0.0F, 0.0F);
        this.renderRightPanel(graphics, uiMouseX - Math.round(rightOffset), uiMouseY);
        graphics.pose().popPose();

        super.render(graphics, uiMouseX, uiMouseY, partialTick);
        this.endUiScale(graphics);
    }

    private void renderLeftPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int panelX = this.getLeftPanelX();
        int panelY = this.getPanelY();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(graphics, MENU_BIG, panelX, panelY, 0, 0, 141, 213);
        blit(graphics, MENU_BIG, panelX + 17, panelY + 10, 142, 22, 107, 21);

        TextUtil.drawCenteredStringWithBorder(graphics, this.font, this.tr("gui.dragonminez.config.options").withStyle(ChatFormatting.BOLD),
                panelX + 70, panelY + 17, 0xFFFFD700);

        int top = this.listTop();
        this.scrollBar.beginClip(graphics, panelX + 5, LIST_CLIP_WIDTH, false);
        for (int i = 0; i < this.settingsOptions.size(); ++i) {
            float y = top + i * ROW_HEIGHT - this.scrollBar.scroll();
            if (!this.scrollBar.isVisible(top + i * ROW_HEIGHT, ROW_HEIGHT)) continue;

            boolean hovered = this.isRowHovered(i, mouseX, mouseY, panelX + 8, panelX + 126)
                    || this.isRowHovered(i, mouseX + this.getLeftPanelX() - this.getRightPanelX(), mouseY, panelX + 8, panelX + 126);
            if (hovered) HudRender.rect(graphics, panelX + 9, y, 117, ROW_HEIGHT - 1, 0x16FFFFFF);
            this.drawScaledText(graphics, this.txt(this.settingsOptions.get(i).label), panelX + 15, y + 6, 0xFFFFFFFF, false);
        }
        this.scrollBar.endClip(graphics);
        this.scrollBar.renderBar(graphics, mouseX, mouseY);
    }

    private void renderRightPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int panelX = this.getRightPanelX();
        int panelY = this.getPanelY();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        blit(graphics, MENU_BIG, panelX, panelY, 0, 0, 141, 213);
        blit(graphics, MENU_BIG, panelX + 17, panelY + 10, 142, 22, 107, 21);

        TextUtil.drawCenteredStringWithBorder(graphics, this.font, this.tr("gui.dragonminez.config.values").withStyle(ChatFormatting.BOLD),
                panelX + 70, panelY + 17, 0xFFFFD700);

        int top = this.listTop();
        this.scrollBar.beginClip(graphics, panelX + 5, LIST_CLIP_WIDTH, false);
        for (int i = 0; i < this.settingsOptions.size(); ++i) {
            float y = top + i * ROW_HEIGHT - this.scrollBar.scroll();
            if (!this.scrollBar.isVisible(top + i * ROW_HEIGHT, ROW_HEIGHT)) continue;

            boolean hovered = this.isRowHovered(i, mouseX, mouseY, panelX + 8, panelX + 132)
                    || this.isRowHovered(i, mouseX + this.getRightPanelX() - this.getLeftPanelX(), mouseY, panelX + 8, panelX + 132);
            if (hovered) HudRender.rect(graphics, panelX + 15, y, 111, ROW_HEIGHT - 1, 0x16FFFFFF);
            this.renderValue(graphics, this.settingsOptions.get(i), panelX, y, mouseX, mouseY, this.isInList(mouseY));
        }
        this.scrollBar.endClip(graphics);
    }

    private void renderValue(GuiGraphics graphics, SettingsOption option, int panelX, float y, int mouseX, int mouseY, boolean interactable) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (option.type == SettingsType.BOOLEAN) {
            blit(graphics, STAT_BUTTONS, panelX + 60, y + 3, 122, option.value > 0.0F ? 0 : 10, 20, 10);
            return;
        }

        boolean overDecrease = interactable && inside(mouseX, mouseY, panelX + 20, y + 3, 14, 11);
        boolean overIncrease = interactable && inside(mouseX, mouseY, panelX + 103, y + 3, 14, 11);
        blit(graphics, STAT_BUTTONS, panelX + 20, y + 3, 142, overDecrease ? 10 : 0, 10, 10);
        blit(graphics, STAT_BUTTONS, panelX + 103, y + 3, 0, overIncrease ? 10 : 0, 10, 10);
        this.drawScaledText(graphics, this.txt(String.valueOf((int) option.value)), panelX + 69, y + 5, 0xFFFFFFFF, true);
    }

    private void drawScaledText(GuiGraphics graphics, Component text, float x, float y, int color, boolean centered) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        if (centered) TextUtil.drawCenteredStringWithBorder(graphics, this.font, text, 0, 0, color);
        else TextUtil.drawStringWithBorder(graphics, this.font, text, 0, 0, color);
        graphics.pose().popPose();
    }

    private static boolean inside(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private boolean isInList(double mouseY) {
        int top = this.listTop();
        return mouseY >= top && mouseY < top + LIST_HEIGHT;
    }

    private boolean isRowHovered(int index, double mouseX, double mouseY, int left, int right) {
        if (mouseX < left || mouseX >= right || !this.isInList(mouseY)) return false;
        float y = this.listTop() + index * ROW_HEIGHT - this.scrollBar.scroll();
        return mouseY >= y && mouseY < y + ROW_HEIGHT;
    }

    private int rowAt(double mouseX, double mouseY) {
        int leftX = this.getLeftPanelX();
        int rightX = this.getRightPanelX();
        for (int i = 0; i < this.settingsOptions.size(); ++i) {
            if (this.isRowHovered(i, mouseX, mouseY, leftX + 8, leftX + 126) || this.isRowHovered(i, mouseX, mouseY, rightX + 8, rightX + 132)) return i;
        }
        return -1;
    }

    private void modifySettingsValue(int index, int delta) {
        if (index < 0 || index >= this.settingsOptions.size()) return;

        SettingsOption option = this.settingsOptions.get(index);
        if (option.type == SettingsType.BOOLEAN) {
            option.value = option.value > 0.0F ? 0.0F : 1.0F;
        } else {
            int step = Screen.hasShiftDown() ? 5 : 1;
            option.value = Mth.clamp(option.value + delta * step, option.min, option.max);
        }
        option.setter.accept(option.value);
    }

    private void playUi(SoundEvent sound) {
        if (this.minecraft != null) this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double uiMouseX = this.toUiX(mouseX);
        double uiMouseY = this.toUiY(mouseY);
        int panelY = this.getPanelY();

        boolean overPanels = uiMouseX >= this.getLeftPanelX() && uiMouseX <= this.getRightPanelX() + 141;
        if (overPanels && uiMouseY >= panelY && uiMouseY <= panelY + 213) {
            this.scrollBar.scrollWheel(delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double uiMouseX = this.toUiX(mouseX);
        double uiMouseY = this.toUiY(mouseY);

        if (this.scrollBar.mouseClicked(uiMouseX, uiMouseY, button)) return true;

        if (button == 0) {
            int index = this.rowAt(uiMouseX, uiMouseY);
            if (index >= 0 && this.handleRowClick(index, uiMouseX, uiMouseY)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleRowClick(int index, double mouseX, double mouseY) {
        SettingsOption option = this.settingsOptions.get(index);
        int panelX = this.getRightPanelX();
        float y = this.listTop() + index * ROW_HEIGHT - this.scrollBar.scroll();

        if (option.type == SettingsType.BOOLEAN) {
            if (!inside(mouseX, mouseY, panelX + 56, y, 28, ROW_HEIGHT)) return false;
            this.modifySettingsValue(index, 1);
            this.playUi(option.value > 0.0F ? MainSounds.SWITCH_ON.get() : MainSounds.SWITCH_OFF.get());
            return true;
        }

        int delta = inside(mouseX, mouseY, panelX + 18, y + 1, 18, 15) ? -1
                : inside(mouseX, mouseY, panelX + 101, y + 1, 18, 15) ? 1 : 0;
        if (delta == 0) return false;

        this.playUi(MainSounds.UI_NAVE_COOLDOWN.get());
        this.heldSettingIndex = index;
        this.heldDelta = delta;
        this.holdTicks = 0;
        this.modifySettingsValue(index, delta);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrollBar.mouseDragged(this.toUiX(mouseX), this.toUiY(mouseY))) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.heldSettingIndex = -1;
        if (this.scrollBar.mouseReleased()) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private enum SettingsType {
        INT,
        BOOLEAN,
    }

    private static class SettingsOption {
        final String label;
        final SettingsType type;
        final float min;
        final float max;
        final Consumer<Float> setter;
        float value;

        SettingsOption(String label, SettingsType type, float value, float min, float max, Consumer<Float> setter) {
            this.label = label;
            this.type = type;
            this.value = value;
            this.min = min;
            this.max = max;
            this.setter = setter;
        }
    }
}