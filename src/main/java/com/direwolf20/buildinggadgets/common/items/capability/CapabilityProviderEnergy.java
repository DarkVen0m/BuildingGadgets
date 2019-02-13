package com.direwolf20.buildinggadgets.common.items.capability;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.tools.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.IntSupplier;

public class CapabilityProviderEnergy implements ICapabilityProvider {
    private ItemStack stack;
    private IntSupplier energyCapacity;

    private final LazyOptional<ItemEnergyForge> energyCapability = LazyOptional.of(() -> new ItemEnergyForge(stack, energyCapacity));

    public CapabilityProviderEnergy(ItemStack stack, IntSupplier energyCapacity) {
        this.stack = stack;
        this.energyCapacity = energyCapacity;
    }

    // @todo: reimplement @since 1.13.x removed as of 1.13?
//    @Override
//    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
//        return capability == CapabilityEnergy.ENERGY && SyncedConfig.poweredByFE ;
//    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        return cap == CapabilityEnergy.ENERGY && SyncedConfig.poweredByFE ? energyCapability.cast() : LazyOptional.empty();
    }

    @Nonnull
    public static LazyOptional<IEnergyStorage> getCap(ItemStack stack) {
        LazyOptional<IEnergyStorage> energy = stack.getCapability(CapabilityEnergy.ENERGY);
        if (!energy.isPresent())
            throw new IllegalArgumentException("CapabilityEnergy could not be retrieved for " + GadgetUtils.getStackErrorSuffix(stack));

        return energy;
    }

    @Nullable
    public static IEnergyStorage getCapOrNull(ItemStack stack) {
        LazyOptional<IEnergyStorage> energy = stack.getCapability(CapabilityEnergy.ENERGY);
        return energy.isPresent() ? energy.orElseThrow(CapabilityNotPresentException::new) : null;
    }

    @Nullable
    public static boolean hasCap(ItemStack stack) {
        return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();
    }
}
