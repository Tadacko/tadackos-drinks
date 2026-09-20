package net.tadacko.tadackosdrinks.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.tadacko.tadackosdrinks.effect.EruditionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Captures the owning player of an EnchantmentMenu at construction, then scopes
 * EruditionEventHandler's "current enchanting player" tightly around each
 * slotsChanged call. This replaces open/close-event-based tracking, which breaks
 * with multiple concurrent players (see EruditionEventHandler).
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    @Unique
    private Player tadackosdrinks$owner;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void tadackosdrinks$captureOwner(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        this.tadackosdrinks$owner = inventory.player;
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void tadackosdrinks$pushOwner(Container container, CallbackInfo ci) {
        EruditionEffect.EruditionEventHandler.setCurrentEnchantingPlayer(this.tadackosdrinks$owner);
    }

    @Inject(method = "slotsChanged", at = @At("RETURN"))
    private void tadackosdrinks$popOwner(Container container, CallbackInfo ci) {
        EruditionEffect.EruditionEventHandler.clearCurrentEnchantingPlayer();
    }

    @Inject(method = "clickMenuButton", at = @At("HEAD"))
    private void tadackosdrinks$pushOwnerOnClick(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        EruditionEffect.EruditionEventHandler.setCurrentEnchantingPlayer(player);
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void tadackosdrinks$popOwnerOnClick(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        EruditionEffect.EruditionEventHandler.clearCurrentEnchantingPlayer();
    }
}