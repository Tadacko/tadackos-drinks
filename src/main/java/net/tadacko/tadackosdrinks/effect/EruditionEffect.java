package net.tadacko.tadackosdrinks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;

public class EruditionEffect extends MobEffect {
    protected EruditionEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    /**
     * Utility class to track which player is currently using an enchanting table.
     * This is used by the mixins to determine if treasure enchantments should be available.
     */
    @Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class EruditionEventHandler {
        private static final ThreadLocal<Player> CURRENT_ENCHANTING_PLAYER = new ThreadLocal<>();

        @SubscribeEvent
        public static void onMenuOpen(PlayerContainerEvent.Open event) {
            if (event.getContainer() instanceof EnchantmentMenu) setCurrentEnchantingPlayer(event.getEntity());
        }

        @SubscribeEvent
        public static void onMenuClose(PlayerContainerEvent.Close event) {
            if (event.getContainer() instanceof EnchantmentMenu) clearCurrentEnchantingPlayer();
        }

        public static void setCurrentEnchantingPlayer(Player player) { CURRENT_ENCHANTING_PLAYER.set(player); }

        public static void clearCurrentEnchantingPlayer() { CURRENT_ENCHANTING_PLAYER.remove(); }

        public static Player getCurrentEnchantingPlayer() { return CURRENT_ENCHANTING_PLAYER.get(); }
    }
}
