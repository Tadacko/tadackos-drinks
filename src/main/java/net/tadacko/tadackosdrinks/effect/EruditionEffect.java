package net.tadacko.tadackosdrinks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;

public class EruditionEffect extends MobEffect {
    protected EruditionEffect(MobEffectCategory pCategory, int pColor) { super(pCategory, pColor); }

    /**
     * Utility class to track which player is currently using an enchanting table.
     * This is used by the mixins to determine if treasure enchantments should be available.
     */
    @Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class EruditionEventHandler {
        private static final ThreadLocal<Player> CURRENT_ENCHANTING_PLAYER = new ThreadLocal<>();

        // fallback defaults, overridden by config values, can't be in mixin class so they're here
        public static int eruditionFrostWalkerMinAmp = 0;
        public static int eruditionMendingMinAmp = 0;
        public static int eruditionSoulSpeedMinAmp = 1;
        public static int eruditionSwiftSneakMinAmp = 1;

        public static void setCurrentEnchantingPlayer(Player player) { CURRENT_ENCHANTING_PLAYER.set(player); }

        public static void clearCurrentEnchantingPlayer() { CURRENT_ENCHANTING_PLAYER.remove(); }

        public static Player getCurrentEnchantingPlayer() { return CURRENT_ENCHANTING_PLAYER.get(); }
    }
}
