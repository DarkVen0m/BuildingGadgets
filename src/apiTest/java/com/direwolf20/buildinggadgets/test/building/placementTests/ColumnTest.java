package com.direwolf20.buildinggadgets.test.building.placementTests;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.placement.PlacementSequences.Column;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ColumnTest {

    @Test
    void columnFacingUpShouldReturnSequenceWithAccentingY() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        IPositionPlacementSequence column = new Region(BlockPos.ZERO, BlockPos.ZERO.up(4));
        Iterator<BlockPos> it = column.iterator();

        assertEquals(new BlockPos(0, 0, 0), it.next());
        assertEquals(new BlockPos(0, 1, 0), it.next());
        assertEquals(new BlockPos(0, 2, 0), it.next());
        assertEquals(new BlockPos(0, 3, 0), it.next());
        assertEquals(new BlockPos(0, 4, 0), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void columnCreatedWithFactoryMethodExtendFromShouldOffsetBaseBy1ToGivenFacing() {
        for (Direction facing : Direction.values()) {
            IPositionPlacementSequence column = Column.extendFrom(BlockPos.ZERO, facing, 15);
            Iterator<BlockPos> it = column.iterator();

            if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                for (int i = 14; i >= 0; i--) {
                    assertEquals(BlockPos.ZERO.offset(facing, i), it.next());
                }
            } else {
                for (int i = 0; i <= 14; i++) {
                    assertEquals(BlockPos.ZERO.offset(facing, i), it.next());
                }
            }

            assertFalse(it.hasNext());
        }
    }

    @Test
    void columnOnXAxisCenteredAtOriginShouldHaveAccentingX() {
        IPositionPlacementSequence column = Column.centerAt(BlockPos.ZERO, Direction.Axis.X, 5);
        Iterator<BlockPos> it = column.iterator();

        assertEquals(new BlockPos(-2, 0, 0), it.next());
        assertEquals(new BlockPos(-1, 0, 0), it.next());
        assertEquals(new BlockPos(0, 0, 0), it.next());
        assertEquals(new BlockPos(1, 0, 0), it.next());
        assertEquals(new BlockPos(2, 0, 0), it.next());
        assertFalse(it.hasNext());
    }

    @RepeatedTest(4)
    void centerAtShouldCeilDownToNearestOddNumberAsSizeRandomParameterSize() {
        int size = MathHelper.clamp(random.nextInt(8), 1, Integer.MAX_VALUE) * 2;
        IPositionPlacementSequence column = Column.centerAt(BlockPos.ZERO, Direction.Axis.Y, size);
        Iterator<BlockPos> it = column.iterator();

        for (int i = 0; i < size - 1; i++) {
            it.next();
        }
        assertFalse(it.hasNext());
    }

    private final Random random = new Random();

}
