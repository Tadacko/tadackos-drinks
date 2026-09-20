package net.tadacko.tadackosdrinks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tadacko.tadackosdrinks.TadackosDrinks;
import net.tadacko.tadackosdrinks.block.ModBlocks;
import net.tadacko.tadackosdrinks.item.ModItems;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TadackosDrinks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BarleyFunctionalityHandler {
    private record PendingSignalFire(LevelAccessor level, BlockPos pos) {}
    private static final List<PendingSignalFire> PENDING_SIGNAL_FIRE = new ArrayList<>();

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide()) return;
        BlockState state = event.getState();
        Block block = state.getBlock();
        BlockPos pos = event.getPos();
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (block == ModBlocks.BARLEY_BLOCK.get() && aboveState.getBlock() instanceof CampfireBlock && !aboveState.getValue(CampfireBlock.SIGNAL_FIRE))
            PENDING_SIGNAL_FIRE.add(new PendingSignalFire(level, abovePos));
        else if (block instanceof CampfireBlock && level.getBlockState(pos.below()).getBlock() == ModBlocks.BARLEY_BLOCK.get() &&
                !state.getValue(CampfireBlock.SIGNAL_FIRE))
            level.setBlock(pos, state.setValue(CampfireBlock.SIGNAL_FIRE, true), Block.UPDATE_ALL);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING_SIGNAL_FIRE.isEmpty()) return;
        for (PendingSignalFire fix : PENDING_SIGNAL_FIRE) {
            BlockState state = fix.level().getBlockState(fix.pos());
            if (state.getBlock() instanceof CampfireBlock && !state.getValue(CampfireBlock.SIGNAL_FIRE))
                fix.level().setBlock(fix.pos(), state.setValue(CampfireBlock.SIGNAL_FIRE, true), Block.UPDATE_ALL);
        }
        PENDING_SIGNAL_FIRE.clear();
    }

    @SubscribeEvent
    public static void onFeed(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (event.getTarget() instanceof Animal animal && !(event.getTarget() instanceof AbstractHorse) && stack.getItem() == ModItems.BARLEY.get() &&
                animal.isFood(new ItemStack(Items.WHEAT))) {
            boolean clientSide = animal.level().isClientSide;
            int age = animal.getAge();
            if (!clientSide && age == 0 && animal.canFallInLove()) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                animal.setInLove(player);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            if (animal.isBaby()) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                animal.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-age), true);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(clientSide));
                return;
            }

            if (clientSide) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.CONSUME);
                return;
            }
        } else if (event.getTarget() instanceof Animal animal && !(event.getTarget() instanceof AbstractHorse) &&
                (stack.getItem() == ModItems.WHEAT_SEEDS_MALTED.get() || stack.getItem() == ModItems.WHEAT_SEEDS_CRUSHED.get() ||
                stack.getItem() == ModItems.BARLEY_SEEDS.get() || stack.getItem() == ModItems.BARLEY_SEEDS_MALTED.get() ||
                stack.getItem() == ModItems.BARLEY_SEEDS_CRUSHED.get() || stack.getItem() == ModItems.HOP_SEEDS.get() ||
                stack.getItem() == ModItems.GRAPE_SEEDS_RED.get() || stack.getItem() == ModItems.GRAPE_SEEDS_WHITE.get())) {
            if (animal.isFood(new ItemStack(Items.WHEAT_SEEDS))) {
                boolean clientSide = animal.level().isClientSide;
                int age = animal.getAge();
                if (!clientSide && age == 0 && animal.canFallInLove()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    animal.setInLove(player);
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }

                if (animal.isBaby()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    animal.ageUp(Animal.getSpeedUpSecondsWhenFeeding(-age), true);
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.sidedSuccess(clientSide));
                    return;
                }

                if (clientSide) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.CONSUME);
                    return;
                }
            } else if (animal instanceof Parrot parrot) {
                if (!parrot.isTame()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);

                    Level level = parrot.level();
                    if (!parrot.isSilent()) {
                        level.playSound((Player)null, parrot.getX(), parrot.getY(), parrot.getZ(), SoundEvents.PARROT_EAT, parrot.getSoundSource(),
                                1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
                    }

                    if (!level.isClientSide) {
                        if (level.random.nextInt(10) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(parrot, player)) {
                            parrot.tame(player);
                            level.broadcastEntityEvent(parrot, (byte)7);
                        } else level.broadcastEntityEvent(parrot, (byte)6);
                    }


                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                }
            }
        } else if (event.getTarget() instanceof AbstractHorse horse && (stack.getItem() == ModItems.BARLEY.get() ||
                stack.getItem() == ModBlocks.BARLEY_BLOCK.get().asItem()) && horse.isFood(new ItemStack(Items.HAY_BLOCK))) {
            boolean clientSide = horse.level().isClientSide;
            if (!player.getAbilities().instabuild) stack.shrink(1);
            boolean flag = false;
            float f = 0.0F;
            int i = 0;
            int j = 0;
            if (horse instanceof Llama) {
                if (stack.is(ModItems.BARLEY.get())) {
                    f = 2.0F;
                    i = 10;
                    j = 3;
                } else if (stack.is(ModBlocks.BARLEY_BLOCK.get().asItem())) {
                    f = 10.0F;
                    i = 90;
                    j = 6;
                    if (horse.isTamed() && horse.getAge() == 0 && horse.canFallInLove()) {
                        flag = true;
                        horse.setInLove(player);
                    }
                }
            } else {
                if (stack.is(ModItems.BARLEY.get())) {
                    f = 2.0F;
                    i = 20;
                    j = 3;
                } else if (stack.is(ModBlocks.BARLEY_BLOCK.get().asItem())) {
                    f = 20.0F;
                    i = 180;
                }
            }

            if (horse.getHealth() < horse.getMaxHealth() && f > 0.0F) {
                horse.heal(f);
                flag = true;
            }

            if (horse.isBaby() && i > 0) {
                horse.level().addParticle(ParticleTypes.HAPPY_VILLAGER, horse.getRandomX(1.0D), horse.getRandomY() + 0.5D,
                        horse.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
                if (!clientSide) horse.ageUp(i);
                flag = true;
            }

            if (j > 0 && (flag || !horse.isTamed()) && horse.getTemper() < horse.getMaxTemper()) {
                flag = true;
                if (!clientSide) horse.modifyTemper(j);
            }

            if (flag) horse.gameEvent(GameEvent.EAT);

            event.setCanceled(true);
            if (clientSide) event.setCancellationResult(InteractionResult.CONSUME);
            else event.setCancellationResult(flag ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        }
    }

    @SubscribeEvent
    public static void onAnimalJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Animal animal &&
                (animal instanceof Cow || animal instanceof Goat || animal instanceof Sheep || animal instanceof Chicken || animal instanceof Llama))) return;

        boolean alreadyAdded = animal.goalSelector.getAvailableGoals().stream().anyMatch(wrapped -> wrapped.getGoal() instanceof BarleyTemptGoal);
        if (!alreadyAdded) {
            if (animal instanceof Cow || animal instanceof Goat)
                animal.goalSelector.addGoal(3, new BarleyTemptGoal(animal, 1.25D, false));
            else if (animal instanceof Sheep) animal.goalSelector.addGoal(3, new BarleyTemptGoal(animal, 1.1D, false));
        }
        alreadyAdded = animal.goalSelector.getAvailableGoals().stream().anyMatch(wrapped -> wrapped.getGoal() instanceof SeedTemptGoal);
        if (!alreadyAdded) {
            if (animal instanceof Chicken)
                animal.goalSelector.addGoal(3, new SeedTemptGoal(animal, 1.0D, false));
        }
        alreadyAdded = animal.goalSelector.getAvailableGoals().stream().anyMatch(wrapped -> wrapped.getGoal() instanceof HayTemptGoal);
        if (!alreadyAdded && animal instanceof Llama) animal.goalSelector.addGoal(5, new HayTemptGoal(animal, 1.25D, false));
    }

    private static class BarleyTemptGoal extends TemptGoal {
        BarleyTemptGoal(PathfinderMob mob, double speedModifier, boolean canScare) {
            super(mob, speedModifier, Ingredient.of(ModItems.BARLEY.get()), canScare);
        }
    }

    private static class SeedTemptGoal extends TemptGoal {
        SeedTemptGoal(PathfinderMob mob, double speedModifier, boolean canScare) {
            super(mob, speedModifier, Ingredient.of(ModItems.WHEAT_SEEDS_MALTED.get(), ModItems.WHEAT_SEEDS_CRUSHED.get(), ModItems.BARLEY_SEEDS.get(),
                    ModItems.BARLEY_SEEDS_MALTED.get(), ModItems.BARLEY_SEEDS_CRUSHED.get(), ModItems.HOP_SEEDS.get(), ModItems.GRAPE_SEEDS_RED.get(),
                    ModItems.GRAPE_SEEDS_WHITE.get()), canScare);
        }
    }

    private static class HayTemptGoal extends TemptGoal {
        HayTemptGoal(PathfinderMob mob, double speedModifier, boolean canScare) {
            super(mob, speedModifier, Ingredient.of(ModBlocks.BARLEY_BLOCK.get().asItem()), canScare);
        }
    }
}
