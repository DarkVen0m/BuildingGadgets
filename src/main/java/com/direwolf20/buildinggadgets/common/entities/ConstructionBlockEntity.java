package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.registry.OurEntities;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ConstructionBlockEntity extends EntityBase {
    private static final DataParameter<BlockPos> FIXED = EntityDataManager.createKey(ConstructionBlockEntity.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Boolean> MAKING = EntityDataManager.createKey(ConstructionBlockEntity.class, DataSerializers.BOOLEAN);

    public ConstructionBlockEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public ConstructionBlockEntity(World world, BlockPos spawnPos, boolean makePaste) {
        this(OurEntities.CONSTRUCTION_BLOCK, world);
        
        setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        targetPos = spawnPos;
        setMakingPaste(makePaste);
    }

    @Override
    protected int getMaxLife() {
        return 80;
    }

    @Override
    protected void registerData() {
        dataManager.register(FIXED, BlockPos.ZERO);
        dataManager.register(MAKING, false);
    }

    @Override
    protected boolean shouldSetDespawning() {
        if (super.shouldSetDespawning())
            return true;

        if (targetPos == null)
            return false;

        Block block = world.getBlockState(targetPos).getBlock();
        return !(block instanceof ConstructionBlock) && !(block instanceof ConstructionBlockPowder);
    }

    @Override
    protected void onSetDespawning() {
        if (targetPos != null) {
            if (!getMakingPaste()) {
                TileEntity te = world.getTileEntity(targetPos);
                if (te instanceof ConstructionBlockTileEntity) {
                    BlockData tempState = ((ConstructionBlockTileEntity) te).getConstructionBlockData();

                    boolean opaque = tempState.getState().isOpaqueCube(world, targetPos);
                    boolean neighborBrightness = false;//tempState.useNeighbourBrightness(world, targetPos); //TODO find replacement
                    //IBakedModel model;
                    //model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(tempState.getState());
                    //boolean ambient = model.isAmbientOcclusion();
                    boolean ambient = false; //TODO Find a better way to get the proper ambient Occlusion value. This is client side only so can't be done here.
                    if (opaque || neighborBrightness || ! ambient) {
                        BlockData tempSetBlock = ((ConstructionBlockTileEntity) te).getConstructionBlockData();
                        BlockData tempActualSetBlock = ((ConstructionBlockTileEntity) te).getActualBlockData();
                        world.setBlockState(targetPos, OurBlocks.constructionBlock.getDefaultState()
                                .with(ConstructionBlock.BRIGHT, ! opaque)
                                .with(ConstructionBlock.NEIGHBOR_BRIGHTNESS, neighborBrightness)
                                .with(ConstructionBlock.AMBIENT_OCCLUSION, ambient));
                        te = world.getTileEntity(targetPos);
                        if (te instanceof ConstructionBlockTileEntity) {
                            ((ConstructionBlockTileEntity) te).setBlockState(tempSetBlock, tempActualSetBlock);
                        }
                    }
                }
            } else if (world.getBlockState(targetPos) == OurBlocks.constructionBlockPowder.getDefaultState()) {
                world.setBlockState(targetPos, OurBlocks.constructionBlockDense.getDefaultState());
            }
        }
    }

    public void setMakingPaste(boolean paste) {
        dataManager.set(MAKING, paste);
    }

    public boolean getMakingPaste() {
        return dataManager.get(MAKING);
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        setMakingPaste(compound.getBoolean(NBTKeys.ENTITY_CONSTRUCTION_MAKING_PASTE));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean(NBTKeys.ENTITY_CONSTRUCTION_MAKING_PASTE, getMakingPaste());
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
