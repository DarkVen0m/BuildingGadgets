package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.blocks.BlockMap;
import com.direwolf20.buildinggadgets.common.util.blocks.BlockMapIntState;
import com.direwolf20.buildinggadgets.common.util.buffers.PasteToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateManagerCommands {
    private static final Set<Item> allowedItemsRight = Stream.of(Items.PAPER, OurItems.template).collect(Collectors.toSet());

    public static void loadTemplate(TemplateManagerContainer container, PlayerEntity player) {
        ItemStack itemStack0 = container.getSlot(0).getStack();
        ItemStack itemStack1 = container.getSlot(1).getStack();
        if (!(itemStack0.getItem() instanceof ITemplate) || !(allowedItemsRight.contains(itemStack1.getItem()))) {
            return;
        }
        ITemplate template = (ITemplate) itemStack0.getItem();
        if (itemStack1.getItem().equals(Items.PAPER)) return;
        World world = player.world;

        BlockPos startPos = template.getStartPos(itemStack1);
        BlockPos endPos = template.getEndPos(itemStack1);
        Multiset<UniqueItem> tagMap = template.getItemCountMap(itemStack1);
        String UUIDTemplate = OurItems.template.getUUID(itemStack1);
        if (UUIDTemplate == null) return;

        WorldSave worldSave = WorldSave.getWorldSave(world);
        WorldSave templateWorldSave = WorldSave.getTemplateWorldSave(world);
        CompoundNBT tagCompound;

        template.setStartPos(itemStack0, startPos);
        template.setEndPos(itemStack0, endPos);
        template.setItemCountMap(itemStack0, tagMap);
        String UUID = template.getUUID(itemStack0);

        if (UUID == null) return;

        CompoundNBT templateTagCompound = templateWorldSave.getCompoundFromUUID(UUIDTemplate);
        tagCompound = templateTagCompound.copy();
        template.incrementCopyCounter(itemStack0);
        tagCompound.putInt(NBTKeys.TEMPLATE_COPY_COUNT, template.getCopyCounter(itemStack0));
        tagCompound.putString(NBTKeys.TEMPLATE_UUID, template.getUUID(itemStack0));
        tagCompound.putString(NBTKeys.TEMPLATE_OWNER, player.getName().getString());
        if (template.equals(OurItems.gadgetCopyPaste)) {
            worldSave.addToMap(UUID, tagCompound);
        } else {
            templateWorldSave.addToMap(UUID, tagCompound);
            Template.setName(itemStack0, Template.getName(itemStack1));
        }
        container.putStackInSlot(0, itemStack0);
        PacketHandler.sendTo(new PacketBlockMap(tagCompound), (ServerPlayerEntity) player);
    }

    public static void saveTemplate(TemplateManagerContainer container, PlayerEntity player, String templateName) {
        ItemStack itemStack0 = container.getSlot(0).getStack();
        ItemStack itemStack1 = container.getSlot(1).getStack();

        if (itemStack0.isEmpty() && itemStack1.getItem() instanceof Template && !templateName.isEmpty()) {
            Template.setName(itemStack1, templateName);
            container.putStackInSlot(1, itemStack1);
            return;
        }

        if (!(itemStack0.getItem() instanceof ITemplate) || !(allowedItemsRight.contains(itemStack1.getItem()))) {
            return;
        }
        ITemplate template = (ITemplate) itemStack0.getItem();
        World world = player.world;
        ItemStack templateStack;
        if (itemStack1.getItem().equals(Items.PAPER)) {
            templateStack = new ItemStack(OurItems.template, 1);
            container.putStackInSlot(1, templateStack);
        }
        if (!(container.getSlot(1).getStack().getItem().equals(OurItems.template))) return;
        templateStack = container.getSlot(1).getStack();
        WorldSave worldSave = WorldSave.getWorldSave(world);
        WorldSave templateWorldSave = WorldSave.getTemplateWorldSave(world);
        CompoundNBT templateTagCompound;

        String UUID = template.getUUID(itemStack0);
        String UUIDTemplate = OurItems.template.getUUID(templateStack);
        if (UUID == null) return;
        if (UUIDTemplate == null) return;

        boolean isTool = itemStack0.getItem().equals(OurItems.gadgetCopyPaste);
        CompoundNBT tagCompound = isTool ? worldSave.getCompoundFromUUID(UUID) : templateWorldSave.getCompoundFromUUID(UUID);
        templateTagCompound = tagCompound.copy();
        template.incrementCopyCounter(templateStack);
        templateTagCompound.putInt(NBTKeys.TEMPLATE_COPY_COUNT, template.getCopyCounter(templateStack));
        templateTagCompound.putString(NBTKeys.TEMPLATE_UUID, OurItems.template.getUUID(templateStack));

        templateWorldSave.addToMap(UUIDTemplate, templateTagCompound);
        BlockPos startPos = template.getStartPos(itemStack0);
        BlockPos endPos = template.getEndPos(itemStack0);
        Multiset<UniqueItem> tagMap = template.getItemCountMap(itemStack0);
        template.setStartPos(templateStack, startPos);
        template.setEndPos(templateStack, endPos);
        template.setItemCountMap(templateStack, tagMap);
        if (isTool) {
            Template.setName(templateStack, templateName);
        } else {
            if (templateName.isEmpty()) {
                Template.setName(templateStack, Template.getName(itemStack0));
            } else {
                Template.setName(templateStack, templateName);
            }
        }
        container.putStackInSlot(1, templateStack);
        PacketHandler.sendTo(new PacketBlockMap(templateTagCompound), (ServerPlayerEntity) player);
    }

    public static void pasteTemplate(TemplateManagerContainer container, PlayerEntity player, CompoundNBT sentTagCompound, String templateName) {
        ItemStack itemStack1 = container.getSlot(1).getStack();

        if (!(allowedItemsRight.contains(itemStack1.getItem()))) {
            return;
        }

        World world = player.world;
        ItemStack templateStack;
        if (itemStack1.getItem().equals(Items.PAPER)) {
            templateStack = new ItemStack(OurItems.template, 1);
            container.putStackInSlot(1, templateStack);
        }
        if (!(container.getSlot(1).getStack().getItem().equals(OurItems.template))) return;
        templateStack = container.getSlot(1).getStack();

        WorldSave templateWorldSave = WorldSave.getTemplateWorldSave(world);
        Template template = OurItems.template;
        String UUIDTemplate = template.getUUID(templateStack);
        if (UUIDTemplate == null) return;

        CompoundNBT templateTagCompound;

        templateTagCompound = sentTagCompound.copy();
        BlockPos startPos = GadgetUtils.getPOSFromNBT(templateTagCompound, NBTKeys.GADGET_START_POS);
        BlockPos endPos = GadgetUtils.getPOSFromNBT(templateTagCompound, NBTKeys.GADGET_END_POS);
        template.incrementCopyCounter(templateStack);
        templateTagCompound.putInt(NBTKeys.TEMPLATE_COPY_COUNT, template.getCopyCounter(templateStack));
        templateTagCompound.putString(NBTKeys.TEMPLATE_UUID, template.getUUID(templateStack));
        //GadgetUtils.writePOSToNBT(templateTagCompound, startPos, NBTKeys.GADGET_START_POS, 0);
        //GadgetUtils.writePOSToNBT(templateTagCompound, endPos, NBTKeys.GADGET_START_POS, 0);
        //Map<UniqueItem, Integer> tagMap = GadgetUtils.nbtToItemCount((NBTTagList) templateTagCompound.getTag("itemcountmap"));
        //templateTagCompound.removeTag("itemcountmap");

        ListNBT MapIntStateTag = (ListNBT) templateTagCompound.get(NBTKeys.MAP_PALETTE);

        BlockMapIntState mapIntState = new BlockMapIntState();
        mapIntState.getIntStateMapFromNBT(MapIntStateTag);
        mapIntState.makeStackMapFromStateMap(player);
        templateTagCompound.put(NBTKeys.MAP_INT_STACK, mapIntState.putIntStackMapIntoNBT());
        templateTagCompound.putString(NBTKeys.TEMPLATE_OWNER, player.getName().getString());

        Multiset<UniqueItem> itemCountMap = HashMultiset.create();
        Map<BlockState, UniqueItem> intStackMap = mapIntState.intStackMap;
        List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(templateTagCompound);
        for (BlockMap blockMap : blockMapList) {
            UniqueItem uniqueItem = intStackMap.get(blockMap.state.getState());
            if (!(uniqueItem == null)) {
                List<ItemStack> drops = Block.getDrops(blockMap.state.getState(), (ServerWorld) world, blockMap.pos, world.getTileEntity(blockMap.pos), player, ItemStack.EMPTY);
                int neededItems = 0;
                for (ItemStack drop : drops) {
                    if (drop.getItem().equals(uniqueItem.getItem())) {
                        neededItems++;
                    }
                }
                if (neededItems == 0) {
                    neededItems = 1;
                }
                if (uniqueItem.getItem() != Items.AIR) {
                    itemCountMap.add(uniqueItem,neededItems);
                }
            }
        }

        templateWorldSave.addToMap(UUIDTemplate, templateTagCompound);


        template.setStartPos(templateStack, startPos);
        template.setEndPos(templateStack, endPos);

        template.setItemCountMap(templateStack, itemCountMap);
        Template.setName(templateStack, templateName);
        container.putStackInSlot(1, templateStack);
        PacketHandler.sendTo(new PacketBlockMap(templateTagCompound), (ServerPlayerEntity) player);
    }

    public static void copyTemplate(TemplateManagerContainer container) {
        ItemStack itemStack0 = container.getSlot(0).getStack();
        if (itemStack0.getItem() instanceof ITemplate) {
            CompoundNBT tagCompound = PasteToolBufferBuilder.getTagFromUUID(OurItems.gadgetCopyPaste.getUUID(itemStack0));
            if (tagCompound == null) {
                Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent("message.gadget.copyfailed").getUnformattedComponentText()), false);
                return;
            }
            CompoundNBT newCompound = new CompoundNBT();
            newCompound.putIntArray(NBTKeys.MAP_INDEX2STATE_ID, tagCompound.getIntArray(NBTKeys.MAP_INDEX2STATE_ID));
            newCompound.putIntArray(NBTKeys.MAP_INDEX2POS, tagCompound.getIntArray(NBTKeys.MAP_INDEX2POS));
            newCompound.put(NBTKeys.MAP_PALETTE, tagCompound.getCompound(NBTKeys.MAP_PALETTE));
            GadgetUtils.writePOSToNBT(newCompound, GadgetUtils.getPOSFromNBT(tagCompound, NBTKeys.GADGET_START_POS), NBTKeys.GADGET_START_POS, DimensionType.OVERWORLD);
            GadgetUtils.writePOSToNBT(newCompound, GadgetUtils.getPOSFromNBT(tagCompound, NBTKeys.GADGET_END_POS), NBTKeys.GADGET_END_POS, DimensionType.OVERWORLD);
            //Map<UniqueItem, Integer> tagMap = GadgetCopyPaste.getItemCountMap(itemStack0);
            //NBTTagList tagList = GadgetUtils.itemCountToNBT(tagMap);
            //newCompound.setTag("itemcountmap", tagList);
            try {
                if (GadgetUtils.getPasteStream(newCompound, tagCompound.getString(NBTKeys.TEMPLATE_NAME)) != null) {
                    Minecraft.getInstance().keyboardListener.setClipboardString(newCompound.toString());
                    Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + new TranslationTextComponent("message.gadget.copysuccess").getUnformattedComponentText()), false);
                } else {
                    pasteIsTooLarge();
                }
            } catch (IOException e) {
                BuildingGadgets.LOG.error("Failed to evaluate template network size. Template will be considered too large.", e);
                pasteIsTooLarge();
            }
        }
    }

    private static void pasteIsTooLarge() {
        Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent("message.gadget.pastetoobig").getUnformattedComponentText()), false);
    }
}