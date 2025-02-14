package com.direwolf20.buildinggadgets.api.building.view;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.IBlockProvider;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.util.CommonUtils;
import com.direwolf20.buildinggadgets.api.util.MappingSpliterator;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiPredicate;

/**
 * Execution context that uses {@link IPositionPlacementSequence} and {@link IBlockProvider} in combination to filter the
 * unusable positions.
 * <p>
 * Testing is done with the predicate produced with {@link #validatorFactory}. If the predicate returns {@code true},
 * the position will be kept and returned in the iterator. If the predicate returns {@code false} on the other hand, the
 * position will be voided.
 *
 * @implNote Execution context in Strategy Pattern
 */
public class SimpleBuildView implements IBuildView {
    private final IPositionPlacementSequence positions;
    private IBlockProvider<?> blocks;
    private final IValidatorFactory validatorFactory;
    private IBuildContext context;
    private BlockPos start;
    @Nullable
    private MaterialList materials;

    /**
     * @see SimpleBuildView#SimpleBuildView(IPositionPlacementSequence, IBlockProvider, IValidatorFactory, IBuildContext, BlockPos)
     *
     * @param positions     List of Block Positions
     * @param blocks        List of Block Providers
     * @param context       Build Context
     */
    public SimpleBuildView(IPositionPlacementSequence positions, IBlockProvider<?> blocks, IBuildContext context) {
        this(positions, blocks, (world, stack, player, initial) -> (pos, state) -> true, context, null);
    }

    /**
     * Note that it is assumed that this method will return a block provider which uses the first value returned by
     * {@link #positions} as translation.
     *
     * @param positions        Positions
     * @param blocks           List of blocks
     * @param validatorFactory Creates predicate for determining whether a position should be used or not
     * @param buildContext     Build context
     * @param start            Starting BlockPos
     */
    public SimpleBuildView(IPositionPlacementSequence positions, IBlockProvider<?> blocks, IValidatorFactory validatorFactory, IBuildContext buildContext, @Nullable BlockPos start) {
        this.positions = positions;
        this.blocks = Objects.requireNonNull(blocks, "Cannot have a SimpleBuildView without IBlockProvider!");
        this.validatorFactory = validatorFactory;
        if (buildContext.getBuildingPlayer() == null)
            BuildingGadgetsAPI.LOG.warn("Constructing SimpleBuildView without a player. This may lead to errors down the line, if the used IValidatorFactory doesn't handle null Players!");
        this.context = buildContext;
        this.start = start;
        this.materials = null;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return new MappingSpliterator<>(getFilteredSequence().spliterator(), pos -> new PlacementTarget(pos, getBlockProvider().at(pos)));
    }

    @Override
    public IBuildView translateTo(BlockPos pos) {
        blocks = blocks.translate(pos);
        materials = null;
        return this;
    }

    @Nullable
    @Override
    public MaterialList estimateRequiredItems(@Nullable Vec3d simulatePos) {
        if (materials == null)
            materials = IBuildView.super.estimateRequiredItems(simulatePos);
        return materials;
    }

    @Override
    public int estimateSize() {
        return getBoundingBox().size();
    }

    @Override
    public void close() {

    }

    @Override
    public IBuildView copy() {
        return new SimpleBuildView(getPositionSequence(), getBlockProvider(), getValidatorFactory(), context, start);
    }

    @Override
    public Region getBoundingBox() {
        return getPositionSequence().getBoundingBox();
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return getPositionSequence().mayContain(x, y, z);
    }

    /**
     * Wrap raw sequence ({@link #getPositionSequence()}) so that the new spliterator only returns positions passing the
     * test of {@link #getValidatorFactory()} with the given World object.
     *
     * @return {@link Spliterator} that wraps {@code getPositionSequence().spliterator()}
     */
    private IPositionPlacementSequence getFilteredSequence() {
        BiPredicate<BlockPos, BlockData> validator = validatorFactory.createValidatorFor(context.getWorld(), context.getUsedStack(), context.getBuildingPlayer(), start);
        return CommonUtils.validatePositionData(getPositionSequence(), validator, getBlockProvider()::at);
    }

    /**
     * @see IPositionPlacementSequence#collect()
     *
     * @return Filtered Block ImmutableList
     */
    public ImmutableList<BlockPos> collectFilteredSequence() {
        return ImmutableList.copyOf(getFilteredSequence());
    }

    public IPositionPlacementSequence getPositionSequence() {
        return positions;
    }

    public IBlockProvider getBlockProvider() {
        return blocks;
    }

    public IValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

    @Override
    public IBuildContext getContext() {
        return context;
    }
}
