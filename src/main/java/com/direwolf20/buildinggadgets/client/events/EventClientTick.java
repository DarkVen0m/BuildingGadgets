package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRequestBlockMap;
import com.direwolf20.buildinggadgets.common.util.buffers.PasteToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.client.Minecraft.getInstance;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EventClientTick {

    private static int counter = 0;
    private static boolean joinedWorld;

    @SubscribeEvent
    public static void onClientTick(@SuppressWarnings("unused") TickEvent.ClientTickEvent event) {
        counter++;
        if (counter > 600 || !joinedWorld) {
            if (!joinedWorld && counter > 200)
                joinedWorld = true;

            counter = 0;
            PlayerEntity player = getInstance().player;
            if (player == null) return;

            for (int i = 0; i < 36; ++i) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (!(stack.getItem() instanceof ITemplate)) continue;

                ITemplate template = (ITemplate) stack.getItem();
                String UUID = template.getUUID(stack);
                if (UUID != null && PasteToolBufferBuilder.isUpdateNeeded(UUID, stack)) {
                    //System.out.println("BlockMap Update Needed for UUID: " + UUID + " in slot " + i);
                    PacketHandler.sendToServer(new PacketRequestBlockMap(template.getUUID(stack), !(template instanceof GadgetCopyPaste)));
                    joinedWorld = true;
                }
            }
        }
    }


// REIMPLEMENT
//    @SubscribeEvent
//    public static void onJoinWorld(@SuppressWarnings("unused") ClientConnectedToServerEvent event) {
//        joinedWorld = false;
//    }
}
