package com.iinitial.dmzautotrainer.mixin;

import com.dragonminez.client.gui.character.minigames.BaseMinigameScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseMinigameScreen.class)
public interface BaseMinigameScreenAccessor {

    @Accessor("stage")
    Enum<?> getStage();

    @Accessor("levelsCleared")
    int getLevelsCleared();
}