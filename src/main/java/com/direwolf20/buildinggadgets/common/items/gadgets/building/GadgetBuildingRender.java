package com.direwolf20.buildinggadgets.common.items.gadgets.building;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.util.CapabilityUtil;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper;
import com.direwolf20.buildinggadgets.common.util.tools.AbstractToolRender;
import com.mojang.blaze3d.platform.GlStateManager;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GadgetBuildingRender extends AbstractToolRender {
    @Override
    public void render(ItemStack tool, float partialTick) {
        super.render(tool, partialTick);

        BlockState toolBlock = GadgetUtils.getToolBlock(tool);

        PlayerPos playerPos = new PlayerPos(getPlayer(), partialTick);
        List<BlockPos> coordinates = getCoordinates(toolBlock, getPlayer(), tool);
        if( coordinates.size() == 0 )
            return;

        ItemStack stack = this.getItemFromBlock(toolBlock, getPlayer());
        int toolEnergy  = ((GadgetGeneric) tool.getItem()).getStoredEnergy(getPlayer(), tool);
        long hasBlocks  = InventoryHelper.countItemWithPaste(stack, getPlayer(), getCacheInventory());

        List<BlockPos> sortedCoordinates = this.sort(coordinates, getPlayer());

        // Prepare our fake world.
        getFakeWorld().setWorldAndState(getPlayer().world, toolBlock, coordinates);

        // Prepare our render
        GlStateManager.pushMatrix();
        RenderTools.enableBlend();

        GlStateManager.translated(-playerPos.getPos().x, -playerPos.getPos().y, -playerPos.getPos().z);
        sortedCoordinates.forEach( pos -> {
            BlockState blockState = BASE_STATE;
            if (getFakeWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES)
                blockState = toolBlock;

            RenderTools.renderBlock(blockState, pos, .55f, RenderTools.DEFAUTL_SCALE, null);
        });

        // Finally show any blocks that can't be built by overlaying a red overlay on them.
        LazyOptional<IEnergyStorage> energy = CapabilityUtil.EnergyUtil.getCap(tool);

        for(BlockPos pos: coordinates) {
            hasBlocks--;
            if (energy.isPresent())
                toolEnergy -= ((GadgetGeneric) tool.getItem()).getEnergyCost(tool);
            else
                toolEnergy -= ((GadgetGeneric) tool.getItem()).getDamageCost(tool);

            if (hasBlocks < 0 || toolEnergy < 0)
                RenderTools.renderBlock(HighlightColors.RED.getColor(), pos, .35f, new Vec3f(1.01f, 1.01f, 1.01f), new Vec3f(-.005f, -.005f, .005f));
        }

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(getRenderLayer());

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public List<BlockPos> sort(List<BlockPos> posList, PlayerEntity player) {
        return SortingHelper.Blocks.byDistance(posList, player);
    }

    @Override
    public boolean canLinkInventories() {
        return true;
    }
}
