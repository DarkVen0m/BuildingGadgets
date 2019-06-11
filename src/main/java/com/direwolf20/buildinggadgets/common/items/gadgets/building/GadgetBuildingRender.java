package com.direwolf20.buildinggadgets.common.items.gadgets.building;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.tools.AbstractToolRender;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class GadgetBuildingRender extends AbstractToolRender {
    @Override
    public void render(PlayerEntity player, ItemStack tool, float partialTick) {
        ItemStack heldItem = GadgetBuilding.getGadget(player);
        if (heldItem.isEmpty()) return;

        super.render(player, tool, partialTick);

        BlockState toolBlock = GadgetUtils.getToolBlock(tool);
        List<BlockPos> coordinates = getCoordinates(toolBlock, player, heldItem);
        if( coordinates.size() == 0 )
            return;

        ItemStack stack = this.getItemFromBlock(toolBlock, player);
        int toolEnergy  = GadgetGeneric.getStoredEnergy(player, tool);

        long hasBlocks = InventoryHelper.countItem(heldItem, player, getCacheInventory());
        hasBlocks = hasBlocks + InventoryHelper.countPaste(player);
    }

    @Override
    public boolean canLinkInventories() {
        return true;
    }
}
