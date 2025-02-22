package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class TemplateManager extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TemplateManager(Properties builder) {
        super(builder);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TooltipTranslation.DONOTUSE_TEXT.componentTranslation().setStyle(Styles.RED));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) {
            GadgetUtils.dropTileEntityInventory(worldIn, pos);
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return OurBlocks.OurTileEntities.TEMPLATE_MANAGER_TYPE.create();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        // Only execute on the server
        if (worldIn.isRemote) {
            return true;
        }
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TemplateManagerTileEntity)) {
            return false;
        }
        TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer((PlayerEntity) player);
        for (int i = 0; i <= 1; i++) {
            ItemStack itemStack = container.getSlot(i).getStack();
            if (!(itemStack.getItem() instanceof ITemplate)) continue;

            ITemplate template = (ITemplate) itemStack.getItem();
            String UUID = template.getUUID(itemStack);
            if (UUID == null) continue;

            CompoundNBT tagCompound = template.getWorldSave(worldIn).getCompoundFromUUID(UUID);
            if (tagCompound != null) {
                PacketHandler.sendTo(new PacketBlockMap(tagCompound), (ServerPlayerEntity) player);
            }
        }
        GuiMod.TEMPLATE_MANAGER.openContainer(player, worldIn, pos);
        return true;
    }
}
