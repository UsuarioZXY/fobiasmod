package com.fobias.event;

import com.fobias.FobiasMod;
import com.fobias.command.LevelCommand;
import com.fobias.entity.StalkerEntity;
import com.fobias.world.FlashlightHandler;
import com.fobias.world.PurgatoryGame;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FobiasMod.MODID)
public class GameEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LevelCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        // Luz de la linterna (cada 2 ticks).
        if (player.tickCount % 2 == 0) {
            FlashlightHandler.tick(player);
        }

        // Partida del nivel (cada 5 ticks).
        if (player.tickCount % 5 == 0) {
            PurgatoryGame.tick(player);
        }
    }

    /** En el Purgatorio no aparece nada salvo el Sonriente. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!event.getLevel().dimension().equals(PurgatoryGame.DIMENSION)) {
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
            PurgatoryGame.onLogin(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FlashlightHandler.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PurgatoryGame.onRespawn(player);
        }
    }
}
