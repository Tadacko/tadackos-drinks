package net.tadacko.tadackosdrinks.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.tadacko.tadackosdrinks.effect.ImprovedDigestionEffect;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "hasEnoughFoodToStartSprinting", at = @At("HEAD"), cancellable = true)
    private void removeSprintRestriction(CallbackInfoReturnable<Boolean> cir) {
        if (!ImprovedDigestionEffect.ImprovedDigestionEventHandler.improvedDigestionAllowSprint) return;
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.hasEffect(ModEffects.IMPROVED_DIGESTION.get())) cir.setReturnValue(true);
    }
}