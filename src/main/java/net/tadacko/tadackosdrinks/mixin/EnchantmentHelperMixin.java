package net.tadacko.tadackosdrinks.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.tadacko.tadackosdrinks.effect.EruditionEffect;
import net.tadacko.tadackosdrinks.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/** Mixin into EnchantmentHelper to add treasure enchantments to the available list */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"), cancellable = true)
    private static void addTreasureEnchantments(int level, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        Player player = EruditionEffect.EruditionEventHandler.getCurrentEnchantingPlayer();

        if (player == null) return;
        if (!player.hasEffect(ModEffects.ERUDITION.get())) return;

        int effectAmplifier = player.getEffect(ModEffects.ERUDITION.get()).getAmplifier();

        List<EnchantmentInstance> originalList = cir.getReturnValue();
        List<EnchantmentInstance> newList = new ArrayList<>(originalList);
        boolean isBook = stack.is(Items.BOOK);

        if (effectAmplifier == 0) {
            if (EruditionEffect.EruditionEventHandler.eruditionFrostWalkerMinAmp == 0 &&
                    (Enchantments.FROST_WALKER.canEnchant(stack) || (isBook && Enchantments.FROST_WALKER.isAllowedOnBooks()))) {
                int enchLevel = getEnchantmentLevel(Enchantments.FROST_WALKER, level);
                newList.add(new EnchantmentInstance(Enchantments.FROST_WALKER, enchLevel));
            }

            if (EruditionEffect.EruditionEventHandler.eruditionMendingMinAmp == 0 &&
                    (Enchantments.MENDING.canEnchant(stack) || (isBook && Enchantments.MENDING.isAllowedOnBooks())))
                newList.add(new EnchantmentInstance(Enchantments.MENDING, 1));

            if (EruditionEffect.EruditionEventHandler.eruditionSoulSpeedMinAmp == 0 &&
                    (Enchantments.SOUL_SPEED.canEnchant(stack) || (isBook && Enchantments.SOUL_SPEED.isAllowedOnBooks()))) {
                int enchLevel = getEnchantmentLevel(Enchantments.SOUL_SPEED, level);
                newList.add(new EnchantmentInstance(Enchantments.SOUL_SPEED, enchLevel));
            }

            if (EruditionEffect.EruditionEventHandler.eruditionSwiftSneakMinAmp == 0 &&
                    (Enchantments.SWIFT_SNEAK.canEnchant(stack) || (isBook && Enchantments.SWIFT_SNEAK.isAllowedOnBooks()))) {
                int enchLevel = getEnchantmentLevel(Enchantments.SWIFT_SNEAK, level);
                newList.add(new EnchantmentInstance(Enchantments.SWIFT_SNEAK, enchLevel));
            }
        }

        if (effectAmplifier == 1) {
            if (EruditionEffect.EruditionEventHandler.eruditionFrostWalkerMinAmp <= 1 &&
                    (Enchantments.FROST_WALKER.canEnchant(stack) || (isBook && Enchantments.FROST_WALKER.isAllowedOnBooks()))) {
                int enchLevel = getEnchantmentLevel(Enchantments.FROST_WALKER, level);
                newList.add(new EnchantmentInstance(Enchantments.FROST_WALKER, enchLevel));
            }

            if (EruditionEffect.EruditionEventHandler.eruditionMendingMinAmp <= 1 &&
                    (Enchantments.MENDING.canEnchant(stack) || (isBook && Enchantments.MENDING.isAllowedOnBooks())))
                newList.add(new EnchantmentInstance(Enchantments.MENDING, 1));

            if (EruditionEffect.EruditionEventHandler.eruditionSoulSpeedMinAmp <= 1 &&
                    (Enchantments.SOUL_SPEED.canEnchant(stack) || (isBook && Enchantments.SOUL_SPEED.isAllowedOnBooks()))) {
                int enchLevel = getEnchantmentLevel(Enchantments.SOUL_SPEED, level);
                newList.add(new EnchantmentInstance(Enchantments.SOUL_SPEED, enchLevel));
            }

            if (EruditionEffect.EruditionEventHandler.eruditionSwiftSneakMinAmp <= 1 &&
                    (Enchantments.SWIFT_SNEAK.canEnchant(stack) || (isBook && Enchantments.SWIFT_SNEAK.isAllowedOnBooks()))) {
                int enchLevel = getEnchantmentLevel(Enchantments.SWIFT_SNEAK, level);
                newList.add(new EnchantmentInstance(Enchantments.SWIFT_SNEAK, enchLevel));
            }
        }

        if (newList.size() > originalList.size()) cir.setReturnValue(newList);
    }

    /**
     * Calculate the appropriate enchantment level based on the enchanting power
     * This scales the level from 1 to max based on how powerful the enchantment is
     */
    private static int getEnchantmentLevel(Enchantment enchantment, int power) {
        int maxLevel = enchantment.getMaxLevel();
        if (maxLevel == 1) return 1;

        // Special handling for Swift Sneak which has very high cost requirements
        // The 'power' parameter here seems to be lower than expected, so we adjust thresholds
        if (enchantment == Enchantments.SWIFT_SNEAK) {
            // Adjusted thresholds based on observed power values (7-35 range)
            if (power >= 30) return 3;  // Top enchanting slot
            else if (power >= 20) return 2;  // Middle slot
            else return 1;  // Bottom slot
        }

        // Check what level is appropriate for this power level
        // Start from level 1 and increase until we find the highest level we can offer
        int appropriateLevel = 1;

        for (int testLevel = 1; testLevel <= maxLevel; testLevel++) {
            int minCost = enchantment.getMinCost(testLevel);
            int maxCost = enchantment.getMaxCost(testLevel);

            // If the power falls within the range for this level, use it
            if (power >= minCost && power <= maxCost + 50) appropriateLevel = testLevel;
        }

        return appropriateLevel;
    }
}