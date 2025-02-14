package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.api.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

/**
 * Instances of this class represent a 3D-Template for any kind of structure.
 * An {@code ITemplate} therefore has the following responsibilities:
 * <ul>
 * <li>Provide a possibility to iterate over all placement information contained in this {@code ITemplate} via {@link #createViewInContext(IBuildOpenOptions)} )}.
 * <li>Optionally an {@code ITemplate} may choose to provide a possibility for modifying the represented structure
 * via an {@link ITemplateTransaction} created by {@link #startTransaction()}.
 * <li>Provide a boundingBox, which will enclose all positions produced by this {@code ITemplate} via {@link IBuildView}.
 * <li>Provide a possibility to serialize this ITemplate via an corresponding {@link ITemplateSerializer}.
 * </ul>
 * <p>
 * Here is a small example of how to iterate over all non-Air Blocks. Of course if a world is available the alternative {@link net.minecraft.block.BlockState#isAir(IBlockReader, BlockPos)}
 * should be used in conjunction with passing it to the {@link SimpleBuildContext}.<br>
 * {@code
 * ITemplate template = ...;
 * IBuildContext ctx = SimpleBuildContext.builder().build();
 * template.createViewInContext(ctx).stream().filter(t -> !t.getData().getState().isAir()).forEach(t -> System.out.println("Non Air block found at "+t.getPos()+"!"));
 * }
 * @implSpec Notice that it is not a responsibility of this class to handle placement or modification in any way.
 */
public interface ITemplate {

    /**
     * @return The {@link ITemplateSerializer} responsible for serializing this Template.
     */
    ITemplateSerializer getSerializer();

    /**
     * Creates a new {@link IBuildView} for iteration over this {@code ITemplate}. The returned {@link IBuildView} may be used on a
     * different {@link Thread} then the one it was created on. Additionally parallel iteration on multiple {@link IBuildView} is explicitly supported,
     * and an implementation must perform any required synchronisation.<br>
     * However it is not required to support executing an {@link ITemplateTransaction} whilst iterating over an {@link IBuildView} and the <b>{@link ITemplateTransaction}</b>
     * should throw an exception in this case.
     * @param openOptions The {@link IBuildOpenOptions} in which this {@code ITemplate} should be viewed - contain the {@link IBuildContext}
     * @return An {@link IBuildView} representing the actual Data of this {@code ITemplate} in a certain {@link IBuildContext}.
     */
    @Nullable
    IBuildView createViewInContext(IBuildOpenOptions openOptions);

    /**
     * Creates a new {@link ITemplateTransaction} for modifying this {@code ITemplate}. The created {@link ITemplateTransaction}
     * will only modify modify this {@code ITemplate} when {@link ITemplateTransaction#execute(IBuildContext)} is called.
     * Therefore iteration on an {@link IBuildView} of this {@code ITemplate} must still be permitted even when an {@link ITemplateTransaction} has been created.
     * It is upon the {@link ITemplateTransaction} to fail if multiple {@link ITemplateTransaction} attempt to execute in parallel or
     * this {@code ITemplate} currently has active {@link IBuildView IBuildViews}.
     * <p>
     * An implementation is not required to support modification via an {@link ITemplateTransaction}. As a result this method may
     * return null if it is not supported. Furthermore an implementation may choose not to support multiple {@link ITemplateTransaction}'s at the same
     * time and therefore return null if an {@link ITemplateTransaction} has been created, but not yet been executed.
     * @return A new {@link ITemplateTransaction} for this {@code ITemplate} or null if not supported.
     */
    @Nullable
    ITemplateTransaction startTransaction();

}
