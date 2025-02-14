package com.direwolf20.buildinggadgets.common.util.helpers;

import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * SortingHelper
 *
 * The sorter class is made to contain all sorts of different sorting algorithms
 *
 * Please use subclasses when more than one sorting method is used for a
 * specific data type / class. For example our static Blocks class used
 * for sorting any data related to a Minecraft Block
 */
public class SortingHelper {

    public static class Blocks {

        /**
         *
         * @param list an unsorted {@link BlockPos} Collection
         * @param player the player
         * @return a sorted by distance {@link List} of {@link BlockPos}
         */
        public static List<BlockPos> byDistance(Iterable<BlockPos> list, PlayerEntity player) {
            List<BlockPos> sortedList = new ArrayList<>();

            Double2ObjectMap<BlockPos> rangeMap = new Double2ObjectArrayMap<>();
            DoubleSortedSet distances = new DoubleRBTreeSet();

            double  x = player.posX,
                    y = player.posY + player.getEyeHeight(),
                    z = player.posZ;

            list.forEach(pos -> {
                double distance = pos.distanceSq(x, y, z, true);

                rangeMap.put(distance, pos);
                distances.add(distance);
            } );

            for (double dist : distances) {
                sortedList.add(rangeMap.get(dist));
            }

            return sortedList;
        }

    }


}
