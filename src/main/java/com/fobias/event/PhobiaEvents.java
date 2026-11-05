package com.fobias.event;

import com.fobias.FobiasMod;
import com.fobias.command.PhobiaCommand;
import com.fobias.entity.StalkerEntity;
import com.fobias.phobia.Phobia;
import com.fobias.phobia.PhobiaData;
import com.fobias.phobia.PhobiaEffects;
import com.fobias.world.DarkForestGame;
import com.fobias.world.FlashlightHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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

        // Luz de la linterna (cada 2 ticks).
        if (player.tickCount % 2 == 0) {
            FlashlightHandler.tick(player);
        }

        // Partida del bosque oscuro (cada 5 ticks).
        if (player.tickCount % 5 == 0 && player instanceof ServerPlayer serverPlayer) {
            DarkForestGame.tick(serverPlayer);
        }

        // Efectos pasivos de las fobias (una vez por segundo).
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

    /** En el bosque oscuro no aparece nada salvo el acechador. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!event.getLevel().dimension().equals(DarkForestGame.DIMENSION)) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Mob && !(entity instanceof StalkerEntity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DarkForestGame.onLogin(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FlashlightHandler.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DarkForestGame.onRespawn(player);
        }
    }
}
