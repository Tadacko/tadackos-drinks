package net.tadacko.tadackosdrinks.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ImprovedDigestionEffect extends MobEffect {
    protected ImprovedDigestionEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ImprovedDigestionEventHandler {
        private static final Map<UUID, Float> lastExhaustion = new ConcurrentHashMap<>();

        public static float improvedDigestionMultiplier = 0.5f; // fallback default, overridden by config value

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) return;
            Player player = event.player;
            if (player.level().isClientSide) return;

            UUID id = player.getUUID();
            if (!player.hasEffect(ModEffects.IMPROVED_DIGESTION.get())) {
                lastExhaustion.remove(id);
                return;
            }

            float currentLevel = player.getFoodData().getExhaustionLevel();
            float lastLevel = lastExhaustion.getOrDefault(id, currentLevel);
            float delta = currentLevel - lastLevel;
            if (delta > 0f) {
                // amp 0: 50% slower
                // amp 1: 75% slower
                float multiplier = improvedDigestionMultiplier / (player.getEffect(ModEffects.IMPROVED_DIGESTION.get()).getAmplifier() + 1);
                float newLevel = lastLevel + delta * multiplier;
                player.getFoodData().setExhaustion(newLevel);
                lastExhaustion.put(id, newLevel);
            } else lastExhaustion.put(id, currentLevel);
        }

        // Prevents a slow memory leak from players who log out while tracked.
        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) { lastExhaustion.remove(event.getEntity().getUUID()); }
    }
}
