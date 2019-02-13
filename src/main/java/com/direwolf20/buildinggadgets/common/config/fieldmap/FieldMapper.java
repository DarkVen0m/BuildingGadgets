package com.direwolf20.buildinggadgets.common.config.fieldmap;

import com.direwolf20.buildinggadgets.common.config.PatternList;
<<<<<<< HEAD
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
=======

>>>>>>> 7e6c7a3a9739c1d5729472f553b8eba2c2aca491
import java.util.function.Function;

/**
 * Class representing a bijective Function and it's reverse Function used to Mapping Field Types to other Types which can be
 * synced more easily.
 * The Value produced by {@link #mapToSync(Object)}will then be given to a {@link ITypeSerializer} to be serialized.
 * The value given to {@link #mapToField(Object)} will be the deserialized Value from an appropriate {@link ITypeSerializer}.
 * @param <FieldVal> The Type of Field this Mapper maps to
 * @param <SyncedVal> The Type of Synced Value this Mapper maps to
 */
public class FieldMapper<FieldVal, SyncedVal> {
    //public static final String BLOCK_LIST_MAPPER_ID = "Block List Mapper";
    public static final String PATTERN_LIST_MAPPER_ID = "Pattern List Mapper";
<<<<<<< HEAD

    public static final FieldMapper<Object,Object> GENERIC_IDENTITY_MAPPER = id();
    public static final FieldMapper<ImmutableList<Block>, List<? extends String>> BLOCK_LIST_MAPPER = of(
            (list) -> list.stream().map((b) -> Objects.requireNonNull(b.getRegistryName()).toString()).collect(ImmutableList.toImmutableList()),
            (strings) -> strings.stream().map(ResourceLocation::new).map(ForgeRegistries.BLOCKS::getValue).collect(ImmutableList.toImmutableList()));
    public static final FieldMapper<PatternList, List<? extends String>> PATTERN_LIST_MAPPER = of(
            PatternList::toList,
            PatternList::ofResourcePattern);

    private final Function<FieldVal,SyncedVal> fieldToSync;
    private final Function<SyncedVal,FieldVal> syncToField;

    public static <F> FieldMapper<F,F> id() {
        return of(Function.identity(),Function.identity());
=======
    /*@SuppressWarnings("unchecked")
    public static final FieldMapper<ImmutableList, String[]> BLOCK_LIST_MAPPER = of(
            (list) -> ((ImmutableList<Block>)list).stream().map((b) -> Objects.requireNonNull(b.getRegistryName()).toString()).toArray(String[]::new),
            (strings) -> Stream.of(strings).map(ResourceLocation::new).map(ForgeRegistries.BLOCKS::getValue).collect(ImmutableList.toImmutableList()),
            ImmutableList.class,String[].class);*/
    public static final FieldMapper<PatternList, String[]> PATTERN_LIST_MAPPER = of(
            PatternList::toArray, PatternList::ofResourcePattern,
            PatternList.class, String[].class);

    private final Function<FieldVal, SyncedVal> fieldToSync;
    private final Function<SyncedVal, FieldVal> syncToField;
    private final Class<FieldVal> fieldType;
    private final Class<SyncedVal> syncedType;

    public static <F> FieldMapper<F, F> id(Class<F> theClass) {
        return of(Function.identity(), Function.identity(), theClass, theClass);
>>>>>>> 7e6c7a3a9739c1d5729472f553b8eba2c2aca491
    }

    public static <FieldVal, SyncedVal> FieldMapper<FieldVal, SyncedVal> of(Function<FieldVal, SyncedVal> fieldToSync, Function<SyncedVal, FieldVal> syncToField, Class<FieldVal> fieldType, Class<SyncedVal> syncedType) {
        return new FieldMapper<FieldVal, SyncedVal>(fieldToSync, syncToField, fieldType, syncedType) {
        };
    }

    private FieldMapper(Function<FieldVal, SyncedVal> fieldToSync, Function<SyncedVal, FieldVal> syncToField, Class<FieldVal> fieldType, Class<SyncedVal> syncedType) {
        this.fieldToSync = fieldToSync;
        this.syncToField = syncToField;
        this.fieldType = fieldType;
        this.syncedType = syncedType;
    }

    public SyncedVal mapToSync(FieldVal val) {
        return fieldToSync.apply(val);
    }

    public FieldVal mapToField(SyncedVal val) {
        return syncToField.apply(val);
    }

    public Class<FieldVal> getFieldType() {
        return fieldType;
    }

    public Class<SyncedVal> getSyncedType() {
        return syncedType;
    }
}
