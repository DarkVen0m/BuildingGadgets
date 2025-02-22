package com.direwolf20.buildinggadgets.api.building.view;

import com.direwolf20.buildinggadgets.api.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.exceptions.TemplateException;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.util.CommonUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

/**
 * A "snapshot" view of a specific buildable Template providing the ability to iterate over the represented {@link PlacementTarget}'s.
 * It also allows for translating to a specific position via {@link #translateTo(BlockPos)}.<br>
 * Furthermore an {@code IBuildView} should provide a hint for users to check the amount of blocks an {@link IBuildView} of this {@code ITemplate} is going to
 * produce at most via {@link #estimateSize()} in combination with hinting the amount of {@link UniqueItem}'s required.
 * However this is not strictly necessary and when computation might be costly it is not advised to return an accurate value.
 * <p>
 * The {@code IBuildView} is constructed given an instance of {@link IBuildContext}. This
 * context allows the {@link IBuildView} to adapt itself to the environment in which it is viewed. Therefore no assumptions may be made, that
 * 2 distinct instances of {@code IBuildView} will produce the same results even if they were constructed by the same {@link IBuildContext}.
 * <p>
 * All Methods in this class may throw an {@link IllegalStateException} if called after the {@code IBuildView} has been closed.
 * @implSpec Notice that no guarantees are made for the order in which {@link PlacementTarget}'s are produced by this {@code IBuildView}.
 * Order may be arbitrary or sorted, consult the documentation of the implementation you are currently faced with for information about traversal order.
 */
public interface IBuildView extends IPlacementSequence<PlacementTarget>, AutoCloseable {
    /**
     * Creates a {@link Stream} backed by the {@link #spliterator()} of this {@code IBuildView}.
     *
     * @return A {@link Stream} representing all positions produced by this {@code IBuildView}.
     * @throws UnsupportedOperationException if {@link #close()}  was called and concurrent Transaction execution is not permitted
     *                                       by the underlying {@link ITemplate}
     * @implSpec The {@link Stream} produced by this method must not be parallel. A user wishing for parallel traversal
     * should take a look at {@link java.util.stream.StreamSupport#stream(Spliterator, boolean)}.
     * @implNote The default implementation is equivalent to calling {@code StreamSupport.stream(view.spliterator(), false);}.
     * @see IPlacementSequence#stream()
     */
    default Stream<PlacementTarget> stream() {
        return IPlacementSequence.super.stream();
    }

    /**
     * @return An {@link Iterator} over the {@link PlacementTarget}'s of this {@code IBuildView}.
     * @throws UnsupportedOperationException if {@link #close()} was called and concurrent Transaction execution is not permitted
     *                                       by the underlying {@link ITemplate}
     * @implNote The default implementation is equivalent to calling {@code Spliterators.iterator(view.spliterator());}.
     */
    @Override
    default Iterator<PlacementTarget> iterator() {
        return Spliterators.iterator(spliterator());
    }

    /**
     * @return An {@link Spliterator} over the {@link PlacementTarget}'s of this {@code IBuildView}.
     * @throws UnsupportedOperationException if {@link #close()} was called and concurrent Transaction execution is not permitted
     *                                       by the underlying {@link ITemplate}
     */
    @Override
    Spliterator<PlacementTarget> spliterator();

    /**
     * Translates this {@code IBuildView} to the specified position.
     *
     * @param pos The position to translate to. May not be null.
     * @return The new translated {@code IBuildView}. May be the same or a new instance depending on implementation.
     * @throws NullPointerException if the given Position was null
     * @implSpec This Method may not accumulate multiple translations, but instead always set the absolute Translation performed
     * to the specified value.
     */
    IBuildView translateTo(BlockPos pos);

    /**
     * Attempts to compute the amount of required {@link UniqueItem}'s. Should never be more than might be needed,
     * but may be fewer if exact requirements are hard or expensive to compute.
     *
     * @param simulatePos nullable BlockPos used to simulate
     * @return A {@link MaterialList} representing the Item Requirements to build this {@code IBuildView}.
     */
    default MaterialList estimateRequiredItems(@Nullable Vec3d simulatePos) {
        return CommonUtils.estimateRequiredItems(this, this.getContext(), simulatePos);
    }

    default MaterialList estimateRequiredItems() {
        PlayerEntity player = getContext().getBuildingPlayer();
        return estimateRequiredItems(player != null ? new Vec3d(player.posX, player.posY, player.posZ) : null);
    }

    /**
     * Attempts to compute an estimate of how many {@link PlacementTarget}'s this {@code IBuildView} will produce.
     * Should never be smaller than the amount produced by iterating over this, but may be larger if an exact size
     * is hard or expensive to compute.
     * <p>
     * A negative value indicates that the size cannot be determined easily.
     *
     * @return A prediction of how many {@link PlacementTarget} this {@code ITemplate} is going to produce.
     * Negative if unknown or expensive to compute.
     */
    int estimateSize();

    /**
     * Calling this Method will invalidate this {@code IBuildView}. If this {@code IBuildView} was created by an {@link ITemplate} then
     * Invalidation must free this {@code IBuildViews} execution lock on any {@link com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction}
     * and may prevent further iteration over this views content if the backing {@link ITemplate} does not support concurrent
     * {@link com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction} execution.
     * <p>
     * Any calls to {@link #iterator()}, {@link #spliterator()} or {@link #stream()} may throw an {@link UnsupportedOperationException} in this case.
     * Additionally an implementation may choose to throw {@link IllegalStateException} from any other Method in this class.
     */
    @Override
    void close() throws TemplateException;

    /**
     * Performs a deep copy of this {@code TemplateView} iterating over all positions if necessary. The resulting {@code TemplateView} should not care about
     * the behaviour of the backing {@link ITemplate} and instead be independent of any resource lock's this {@code TemplateView} imposes as well as not imposing any
     * resource locks itself.
     * <p>
     * Calling
     * {@code
     * IBuildView view = template.createViewInContext(ctx);
     * IBuildView copy = view.copy();
     * view.close();
     * }
     * should ensure that the {@link ITemplate} is no longer restricted because of an open {@code TemplateView}, whilst at the same time providing access to all the positions via
     * {@code copy} in the original {@code TemplateView} in a non-lazy manner.
     * <p>
     * <b>However: be warned that this might require O(n) execution time for this Method on some implementations (with n being the total number of
     * {@link PlacementTarget PlacementTargets} produced) and will almost certainly nullify any benefits that the original {@code TemplateView} may have had by
     * using a lazy implementation.</b>
     *
     * @return A full copy of this {@code TemplateView}. Iterating over the whole {@code TemplateView} if necessary.
     */
    @Override
    IBuildView copy();

    IBuildContext getContext();
}
