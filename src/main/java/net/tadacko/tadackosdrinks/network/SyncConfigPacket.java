package net.tadacko.tadackosdrinks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tadacko.tadackosdrinks.block.entity.*;
import net.tadacko.tadackosdrinks.effect.CharismaEffect;
import net.tadacko.tadackosdrinks.effect.EruditionEffect;
import net.tadacko.tadackosdrinks.effect.ImprovedDigestionEffect;
import net.tadacko.tadackosdrinks.item.ModItems;
import net.tadacko.tadackosdrinks.util.Tooltips;

import java.util.function.Supplier;

public class SyncConfigPacket {
    private final double ABVBeer;
    private final double ABVWine;
    private final double ABVCider;
    private final double ABVMead;
    private final double ABVSpiritLow;
    private final double ABVSpiritMid;
    private final double ABVSpiritHigh;
    private final double ABVSpiritMax;
    private final double ABVWhisky;
    private final double ABVBrandy;
    private final double ABVRum;
    private final double ABVVodka;
    private final double ABVGin;
    private final double ABVTequila;
    private final float charismaMultiplier;
    private final int stackSizeMolasses;
    private final int stackSizeKeg;
    private final int stackSizeGlass;
    private final int stackSizeDrink;
    private final boolean improvedDigestionAllowSprint;
    private final int eruditionFrostWalkerMinAmp;
    private final int eruditionMendingMinAmp;
    private final int eruditionSoulSpeedMinAmp;
    private final int eruditionSwiftSneakMinAmp;
    private final int crusherMaxProgress;
    private final int pressMaxProgress;
    private final int barrelMaxProgress;
    private final int barrelMaxAgingProgress;
    private final int potMaxProgress;
    private final int potStillMaxProgress;
    private final int columnStill2MaxProgress;
    private final int columnStill4MaxProgress;
    private final int columnStill6MaxProgress;
    private final int columnStill8MaxProgress;

    public SyncConfigPacket(double ABVBeer, double ABVWine, double ABVCider, double ABVMead, double ABVSpiritLow, double ABVSpiritMid,
                            double ABVSpiritHigh, double ABVSpiritMax, double ABVWhisky, double ABVBrandy, double ABVRum, double ABVVodka, double ABVGin,
                            double ABVTequila, float charismaMultiplier, int stackSizeMolasses, int stackSizeKeg, int stackSizeGlass, int stackSizeDrink,
                            boolean improvedDigestionAllowSprint, int eruditionFrostWalkerMinAmp, int eruditionMendingMinAmp, int eruditionSoulSpeedMinAmp,
                            int eruditionSwiftSneakMinAmp, int crusherMaxProgress, int pressMaxProgress, int barrelMaxProgress, int barrelMaxAgingProgress, int potMaxProgress, int potStillMaxProgress, int columnStill2MaxProgress, int columnStill4MaxProgress, int columnStill6MaxProgress, int columnStill8MaxProgress) {
        this.ABVBeer = ABVBeer;
        this.ABVWine = ABVWine;
        this.ABVCider = ABVCider;
        this.ABVMead = ABVMead;
        this.ABVSpiritLow = ABVSpiritLow;
        this.ABVSpiritMid = ABVSpiritMid;
        this.ABVSpiritHigh = ABVSpiritHigh;
        this.ABVSpiritMax = ABVSpiritMax;
        this.ABVWhisky = ABVWhisky;
        this.ABVBrandy = ABVBrandy;
        this.ABVRum = ABVRum;
        this.ABVVodka = ABVVodka;
        this.ABVGin = ABVGin;
        this.ABVTequila = ABVTequila;
        this.charismaMultiplier = charismaMultiplier;
        this.stackSizeMolasses = stackSizeMolasses;
        this.stackSizeKeg = stackSizeKeg;
        this.stackSizeGlass = stackSizeGlass;
        this.stackSizeDrink = stackSizeDrink;
        this.improvedDigestionAllowSprint = improvedDigestionAllowSprint;
        this.eruditionFrostWalkerMinAmp = eruditionFrostWalkerMinAmp;
        this.eruditionMendingMinAmp = eruditionMendingMinAmp;
        this.eruditionSoulSpeedMinAmp = eruditionSoulSpeedMinAmp;
        this.eruditionSwiftSneakMinAmp = eruditionSwiftSneakMinAmp;
        this.crusherMaxProgress = crusherMaxProgress;
        this.pressMaxProgress = pressMaxProgress;
        this.barrelMaxProgress = barrelMaxProgress;
        this.barrelMaxAgingProgress = barrelMaxAgingProgress;
        this.potMaxProgress = potMaxProgress;
        this.potStillMaxProgress = potStillMaxProgress;
        this.columnStill2MaxProgress = columnStill2MaxProgress;
        this.columnStill4MaxProgress = columnStill4MaxProgress;
        this.columnStill6MaxProgress = columnStill6MaxProgress;
        this.columnStill8MaxProgress = columnStill8MaxProgress;
    }

    public static void encode(SyncConfigPacket pkt, FriendlyByteBuf buf) {
        buf.writeDouble(pkt.ABVBeer);
        buf.writeDouble(pkt.ABVWine);
        buf.writeDouble(pkt.ABVCider);
        buf.writeDouble(pkt.ABVMead);
        buf.writeDouble(pkt.ABVSpiritLow);
        buf.writeDouble(pkt.ABVSpiritMid);
        buf.writeDouble(pkt.ABVSpiritHigh);
        buf.writeDouble(pkt.ABVSpiritMax);
        buf.writeDouble(pkt.ABVWhisky);
        buf.writeDouble(pkt.ABVBrandy);
        buf.writeDouble(pkt.ABVRum);
        buf.writeDouble(pkt.ABVVodka);
        buf.writeDouble(pkt.ABVGin);
        buf.writeDouble(pkt.ABVTequila);
        buf.writeFloat(pkt.charismaMultiplier);
        buf.writeInt(pkt.stackSizeMolasses);
        buf.writeInt(pkt.stackSizeKeg);
        buf.writeInt(pkt.stackSizeGlass);
        buf.writeInt(pkt.stackSizeDrink);
        buf.writeBoolean(pkt.improvedDigestionAllowSprint);
        buf.writeInt(pkt.eruditionFrostWalkerMinAmp);
        buf.writeInt(pkt.eruditionMendingMinAmp);
        buf.writeInt(pkt.eruditionSoulSpeedMinAmp);
        buf.writeInt(pkt.eruditionSwiftSneakMinAmp);
        buf.writeInt(pkt.crusherMaxProgress);
        buf.writeInt(pkt.pressMaxProgress);
        buf.writeInt(pkt.barrelMaxProgress);
        buf.writeInt(pkt.barrelMaxAgingProgress);
        buf.writeInt(pkt.potMaxProgress);
        buf.writeInt(pkt.potStillMaxProgress);
        buf.writeInt(pkt.columnStill2MaxProgress);
        buf.writeInt(pkt.columnStill4MaxProgress);
        buf.writeInt(pkt.columnStill6MaxProgress);
        buf.writeInt(pkt.columnStill8MaxProgress);
    }

    public static SyncConfigPacket decode(FriendlyByteBuf buf) {
        return new SyncConfigPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readFloat(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(final SyncConfigPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isClient()) {
                Tooltips.setABVValues(packet.ABVBeer, packet.ABVWine, packet.ABVCider, packet.ABVMead, packet.ABVSpiritLow, packet.ABVSpiritMid,
                        packet.ABVSpiritHigh, packet.ABVSpiritMax, packet.ABVWhisky, packet.ABVBrandy, packet.ABVRum, packet.ABVVodka, packet.ABVGin,
                        packet.ABVTequila);
                ImprovedDigestionEffect.ImprovedDigestionEventHandler.improvedDigestionAllowSprint = packet.improvedDigestionAllowSprint;
            }
            CharismaEffect.CharismaEventHandler.charismaMultiplier = packet.charismaMultiplier;
            ModItems.stackSizeMolasses = packet.stackSizeMolasses;
            ModItems.stackSizeKeg = packet.stackSizeKeg;
            ModItems.stackSizeGlass = packet.stackSizeGlass;
            ModItems.stackSizeDrink = packet.stackSizeDrink;
            EruditionEffect.EruditionEventHandler.eruditionFrostWalkerMinAmp = packet.eruditionFrostWalkerMinAmp;
            EruditionEffect.EruditionEventHandler.eruditionMendingMinAmp = packet.eruditionMendingMinAmp;
            EruditionEffect.EruditionEventHandler.eruditionSoulSpeedMinAmp = packet.eruditionSoulSpeedMinAmp;
            EruditionEffect.EruditionEventHandler.eruditionSwiftSneakMinAmp = packet.eruditionSwiftSneakMinAmp;
            ManualCrusherBlockEntity.crusherMaxProgress = packet.crusherMaxProgress;
            ManualPressBlockEntity.pressMaxProgress = packet.pressMaxProgress;
            FermentingBarrelBlockEntity.barrelMaxProgress = packet.barrelMaxProgress;
            FermentingBarrelBlockEntity.barrelMaxAgingProgress = packet.barrelMaxAgingProgress;
            CopperPotBlockEntity.potMaxProgress = packet.potMaxProgress;
            PotStillBlockEntity.potStillMaxProgress = packet.potStillMaxProgress;
            ColumnStillBlockEntity.columnStill2MaxProgress = packet.columnStill2MaxProgress;
            ColumnStillBlockEntity.columnStill4MaxProgress = packet.columnStill4MaxProgress;
            ColumnStillBlockEntity.columnStill6MaxProgress = packet.columnStill6MaxProgress;
            ColumnStillBlockEntity.columnStill8MaxProgress = packet.columnStill8MaxProgress;
        });
        ctx.setPacketHandled(true);
    }
}
