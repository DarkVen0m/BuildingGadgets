package com.direwolf20.buildinggadgets.common.util.tools;

import com.direwolf20.buildinggadgets.client.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.modes.BuildingMode;
import com.direwolf20.buildinggadgets.common.world.FakeBuilderWorld;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.platform.GlStateManager;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractToolRender {
    protected static final BlockState BASE_STATE = Blocks.AIR.getDefaultState();

    private static Minecraft minecraft = Minecraft.getInstance();
    private static RemoteInventoryCache cacheInventory = new RemoteInventoryCache(false);

    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public void render(PlayerEntity player, ItemStack tool, float partialTick) {
        PlayerPos playerPos = new PlayerPos(player, partialTick);

        if( this.canLinkInventories() )
            this.renderLinkedInventory(tool, player.dimension, playerPos);
    }

    /**
     * We first check the tools anchor and if all else fails, we'll build our own.
     */
    protected List<BlockPos> getCoordinates(BlockState toolBlock, PlayerEntity player, ItemStack tool) {
        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, tool);
        BlockState startBlock = player.world.getBlockState(lookingAt.getPos());

        if( startBlock == BGBlocks.effectBlock.getDefaultState() || toolBlock == BASE_STATE )
            return new ArrayList<>();

        List<BlockPos> collection = GadgetUtils.getAnchor(tool);
        if( collection.size() > 0 )
            return collection;

        return BuildingMode.collectPlacementPos(player.world, player, lookingAt.getPos(), lookingAt.getFace(), tool, lookingAt.getPos());
    }

    /**
     * This could be named better but what we're doing here is attempting
     * to get the right block to use in the world, Just because we select
     * minecraft:stone doesn't always mean that is the right block.
     *
     * todo: implement this back in -> renderBlockState.getBlock().canSilkHarvest(renderBlockState, world, new BlockPos(0, 0, 0), player)
     */
    protected ItemStack getItemFromBlock(BlockState toolsBlock, PlayerEntity player) {
        ItemStack stack;

        if( false ) // fixme: we need a way of handling loot tables.
            stack = InventoryHelper.getSilkTouchDrop(toolsBlock);
        else
            stack = toolsBlock.getBlock().getPickBlock(toolsBlock, null, player.world, BlockPos.ZERO, player);

        if( !stack.isEmpty() )
            return stack;

        // Finally ensure that we actually have a block if all else fails.
        return toolsBlock.getBlock().getPickBlock(toolsBlock, null, player.world, BlockPos.ZERO, player);
    }

    private void renderLinkedInventory(ItemStack tool, DimensionType playerDimension, PlayerPos playerPos) {
        // This is problematic as you use REMOTE_INVENTORY_POS to get the dimension instead of REMOTE_INVENTORY_DIM
        ResourceLocation dim = GadgetUtils.getDIMFromNBT(tool, NBTKeys.REMOTE_INVENTORY_POS);
        BlockPos pos = GadgetUtils.getPOSFromNBT(tool, NBTKeys.REMOTE_INVENTORY_POS);

        if (dim == null || pos == null)
            return;

        DimensionType dimension = DimensionType.byName(dim);
        if(dimension == null || dimension != playerDimension )
            return;

        GlStateManager.pushMatrix();

        RenderTools.enableBlend();
        RenderTools.translateFromPlayer(playerPos, pos);
        RenderTools.setAlpha(.35f);

        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
        GlStateManager.translatef(-0.005f, -0.005f, 0.005f);
        GlStateManager.scalef(1.01f, 1.01f, 1.01f);

        RenderTools.renderHighlight(HighlightColors.YELLOW);

        getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(Blocks.YELLOW_STAINED_GLASS.getDefaultState(), 1f);
        GlStateManager.popMatrix();
    }

    public boolean canLinkInventories() {
        return false;
    }

    /**
     * To be used when sorting is required. If no sorting method is set
     * from the extending classes, we'll simply return what ever we
     * are given.
     *
     * @param playerEntity is optional as some renders require a player.
     */
    public List<BlockPos> sort(List<BlockPos> posList, PlayerEntity playerEntity) {
        return posList;
    }

    protected static void setInventoryCache(Multiset<UniqueItem> cache) {
        cacheInventory.setCache(cache);
    }
    protected static void updateInventoryCache() {
        cacheInventory.forceUpdate();
    }

    protected static RemoteInventoryCache getCacheInventory() {
        return cacheInventory;
    }

    public static BlockRenderLayer getRenderLayer() {
        return MinecraftForgeClient.getRenderLayer();
    }

    private static Minecraft getMinecraft() {
        return minecraft;
    }

    protected static FakeBuilderWorld getFakeWorld() {
        return fakeWorld;
    }

    protected static class RenderTools {
        public static final Vec3f DEFAUTL_SCALE = new Vec3f(1f, 1f, 1f);

        public static void renderHighlight(HighlightColors color) {
            getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(color.getColor(), 1f);
        }

        // todo: this might not be required. Currently only used here.
        public static void translateFromPlayer(PlayerPos from, BlockPos to) {
            GlStateManager.translated(-from.getPos().x, -from.getPos().y, -from.getPos().z);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.translatef(to.getX(), to.getY(), to.getZ());//Now move the render position to the coordinates we want to render at
        }

        public static void setAlpha(float alpha) {
            GL14.glBlendColor(1F, 1F, 1F, alpha);
        }

        public static void enableBlend() {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        }

        public static void renderBlock(BlockState state, BlockPos pos, float alpha, Vec3f scale, @Nullable Vec3f offset) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(pos.getX(), pos.getY(), pos.getZ());
            GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.scalef(scale.x, scale.y, scale.z);
            if( offset != null )
                GlStateManager.translatef(offset.x, offset.y, offset.z);

            GL14.glBlendColor(1F, 1F, 1F, alpha);

            getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(state, 1f);
            GlStateManager.popMatrix();
        }
    }

    protected enum HighlightColors {
        YELLOW(Blocks.YELLOW_STAINED_GLASS.getDefaultState()),
        RED(Blocks.RED_STAINED_GLASS.getDefaultState());

        private BlockState color;
        HighlightColors(BlockState color) {
            this.color = color;
        }

        public BlockState getColor() {
            return color;
        }
    }

    // todo: rename and find a better place to put this
    public static class PlayerPos {
        private Vec3d pos;

        public PlayerPos(PlayerEntity player, float partialTicks) {
            this.pos = new Vec3d(
                player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks,
                player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks + player.getEyeHeight(),
                player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks
            );
        }

        public Vec3d getPos() {
            return pos;
        }
    }
}
