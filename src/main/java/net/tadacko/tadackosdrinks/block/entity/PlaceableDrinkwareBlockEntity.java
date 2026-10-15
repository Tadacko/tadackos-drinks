package net.tadacko.tadackosdrinks.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.tadacko.tadackosdrinks.block.DrinkVariant;

public class PlaceableDrinkwareBlockEntity extends BlockEntity {
    private ItemStack stored = ItemStack.EMPTY;
    private DrinkVariant drinkVariant = DrinkVariant.BEER_EMPTY;

    public PlaceableDrinkwareBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.PLACEABLE_DRINKWARE.get(), pos, state); }

    public ItemStack getStoredStack() { return stored; }

    public DrinkVariant getDrinkVariant() { return drinkVariant; }

    public void setStoredStackAndDrinkVariant(ItemStack stack, DrinkVariant variant) {
        stored = (stack == null) ? ItemStack.EMPTY : stack.copy();
        if (drinkVariant != variant) drinkVariant = variant;
        if (level != null && !level.isClientSide) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Stored")) this.stored = ItemStack.of(tag.getCompound("Stored"));
        else this.stored = ItemStack.EMPTY;
        if (tag.contains("drink_variant", Tag.TAG_STRING))
            try { drinkVariant = DrinkVariant.valueOf(tag.getString("drink_variant")); } catch (IllegalArgumentException ignored) {}
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.stored != null && !this.stored.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            this.stored.save(itemTag);
            tag.put("Stored", itemTag);
        }
        tag.putString("drink_variant", drinkVariant.name());
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); } // reuses saveAdditional

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
