package com.fobias.event;

import com.fobias.FobiasMod;
import com.fobias.command.PhobiaCommand;
import com.fobias.phobia.Phobia;
import com.fobias.phobia.PhobiaData;
import com.fobias.phobia.PhobiaEffects;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FobiasMod.MODID)
public class PhobiaEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PhobiaCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }

        Player player = event.player;

        // Se revisa una vez por segundo (20 ticks) para no gastar rendimiento.
        if (player.tickCount % 20 != 0) {
            return;
        }
        if (player.isCreative() || player.isSpectator() || !player.isAlive()) {
            return;
        }

        for (Phobia phobia : Phobia.values()) {
            if (PhobiaData.has(player, phobia)) {
                PhobiaEffects.apply(player, phobia);
            }
        }
    }
}
