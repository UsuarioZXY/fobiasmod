package com.fobias.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fobias.entity.StalkerEntity;
import com.fobias.phobia.Phobia;
import com.fobias.phobia.PhobiaData;
import com.fobias.registry.ModEntities;
import com.fobias.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import com.fobias.FobiasMod;

/**
 * Partida del bosque oscuro (nictofobia).
 *
 * Al usar el orbe: se guarda tu inventario, te quedas solo con la linterna
 * y apareces en un bosque plano de noche. Una luz blanca (baliza) marca la
 * salida, lejos de ti. El Acechador te observa y luego te persigue.
 * Llegar a la salida = ganas. Que te atrape = pierdes. En ambos casos
 * vuelves a donde estabas con tu inventario intacto.
 */
public final class DarkForestGame {
    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation(FobiasMod.MODID, "dark_forest"));

    public enum Outcome {
        WON, CAUGHT, ABORT
    }

    /** Altura de la capa de cesped (bedrock 0, tierra 1-2, cesped 3). */
    private static final int GROUND_Y = 3;
    private static final int EXIT_MIN_DISTANCE = 100;
    private static final int EXIT_EXTRA_DISTANCE = 40;
    private static final double STALKER_DISTANCE = 34.0D;
    private static final double EXIT_REACH_SQR = 16.0D;

    private static final String PERSISTED = "PlayerPersisted";
    private static final String SAVE_KEY = "fobias_dark_save";

    private record Session(BlockPos exit, UUID stalker) {
    }

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private DarkForestGame() {
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public static boolean isInside(net.minecraft.world.entity.player.Player player) {
        return player.level().dimension().equals(DIMENSION);
    }

    public static boolean isPlaying(ServerPlayer player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    // ------------------------------------------------------------------
    // Inicio
    // ------------------------------------------------------------------

    public static void start(ServerPlayer player) {
        MinecraftServer server = player.server;
        ServerLevel forest = server.getLevel(DIMENSION);
        if (forest == null) {
            player.displayClientMessage(Component.translatable("message.fobias.world_missing"), false);
            return;
        }
        if (isPlaying(player) || isInside(player) || hasSaved(player)) {
            player.displayClientMessage(Component.translatable("message.fobias.already_playing"), false);
            return;
        }

        RandomSource random = forest.getRandom();

        // Cada partida usa una zona distinta del mundo, lejos de las demas.
        int originX = (random.nextInt(2001) - 1000) * 16;
        int originZ = (random.nextInt(2001) - 1000) * 16;
        double exitAngle = random.nextDouble() * Math.PI * 2.0D;
        double exitDistance = EXIT_MIN_DISTANCE + random.nextInt(EXIT_EXTRA_DISTANCE + 1);
        int exitX = originX + (int) Math.round(Math.cos(exitAngle) * exitDistance);
        int exitZ = originZ + (int) Math.round(Math.sin(exitAngle) * exitDistance);
        BlockPos exit = new BlockPos(exitX, GROUND_Y + 1, exitZ);

        // El acechador aparece del lado contrario a la salida.
        double stalkerAngle = exitAngle + Math.PI + (random.nextDouble() - 0.5D);
        double stalkerX = originX + Math.cos(stalkerAngle) * STALKER_DISTANCE;
        double stalkerZ = originZ + Math.sin(stalkerAngle) * STALKER_DISTANCE;

        // Genera las zonas importantes antes de usarlas.
        loadArea(forest, originX, originZ, 2);
        loadArea(forest, exitX, exitZ, 2);
        loadArea(forest, (int) stalkerX, (int) stalkerZ, 1);

        buildExit(forest, exit);
        clearBox(forest, originX - 1, GROUND_Y + 1, originZ - 1, originX + 1, GROUND_Y + 5, originZ + 1);

        StalkerEntity stalker = ModEntities.STALKER.get().create(forest);
        if (stalker == null) {
            return;
        }
        int sx = (int) Math.floor(stalkerX);
        int sz = (int) Math.floor(stalkerZ);
        clearBox(forest, sx - 1, GROUND_Y + 1, sz - 1, sx + 1, GROUND_Y + 5, sz + 1);
        stalker.moveTo(sx + 0.5D, GROUND_Y + 1, sz + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        stalker.setPersistenceRequired();

        // Guarda todo lo del jugador y lo deja solo con la linterna.
        saveState(player);
        player.getInventory().clearContent();
        player.getInventory().add(new ItemStack(ModItems.FLASHLIGHT.get()));
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.setGameMode(GameType.ADVENTURE);

        player.teleportTo(forest, originX + 0.5D, GROUND_Y + 1, originZ + 0.5D, random.nextFloat() * 360.0F, 0.0F);

        forest.addFreshEntity(stalker);
        SESSIONS.put(player.getUUID(), new Session(exit, stalker.getUUID()));

        player.displayClientMessage(Component.translatable("message.fobias.dark_start"), false);
        player.displayClientMessage(Component.translatable("message.fobias.dark_hint"), false);
    }

    // ------------------------------------------------------------------
    // Durante la partida
    // ------------------------------------------------------------------

    /** Se llama unas veces por segundo por jugador. */
    public static void tick(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }
        if (!isInside(player)) {
            // Salio del bosque por otra via (comando, etc.): se cierra la partida.
            end(player, Outcome.ABORT);
            return;
        }
        BlockPos exit = session.exit();
        if (player.distanceToSqr(exit.getX() + 0.5D, exit.getY(), exit.getZ() + 0.5D) < EXIT_REACH_SQR) {
            end(player, Outcome.WON);
        }
    }

    // ------------------------------------------------------------------
    // Final
    // ------------------------------------------------------------------

    public static void end(ServerPlayer player, Outcome outcome) {
        Session session = SESSIONS.remove(player.getUUID());
        MinecraftServer server = player.server;

        ServerLevel forest = server.getLevel(DIMENSION);
        if (session != null && forest != null) {
            Entity stalker = forest.getEntity(session.stalker());
            if (stalker != null) {
                stalker.discard();
            }
        }

        boolean inForest = isInside(player);
        CompoundTag saved = takeSaved(player);
        returnPlayer(player, saved, inForest);

        switch (outcome) {
            case WON -> {
                showTitle(player, "title.fobias.escaped", "subtitle.fobias.escaped");
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (PhobiaData.has(player, Phobia.NYCTOPHOBIA)) {
                    PhobiaData.remove(player, Phobia.NYCTOPHOBIA);
                    player.displayClientMessage(
                            Component.translatable("message.fobias.overcome", Phobia.NYCTOPHOBIA.getDisplayName()),
                            false);
                }
            }
            case CAUGHT -> {
                showTitle(player, "title.fobias.caught", "subtitle.fobias.caught");
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
                player.playNotifySound(SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 1.0F, 0.6F);
            }
            case ABORT -> player.displayClientMessage(Component.translatable("message.fobias.aborted"), false);
        }
    }

    /** Tras morir dentro del bosque: devuelve el inventario guardado. */
    public static void onRespawn(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
        CompoundTag saved = takeSaved(player);
        if (saved != null) {
            restoreInventory(player, saved);
        }
    }

    /** Si el servidor se reinicio con una partida a medias, se recupera al entrar. */
    public static void onLogin(ServerPlayer player) {
        if (!isPlaying(player) && hasSaved(player)) {
            end(player, Outcome.ABORT);
        }
    }

    // ------------------------------------------------------------------
    // Guardado y restauracion del jugador
    // ------------------------------------------------------------------

    private static boolean hasSaved(ServerPlayer player) {
        return PhobiaData.persisted(player).contains(SAVE_KEY, Tag.TAG_COMPOUND);
    }

    private static void saveState(ServerPlayer player) {
        CompoundTag saved = new CompoundTag();
        saved.put("inventory", player.getInventory().save(new ListTag()));
        saved.putInt("gamemode", player.gameMode.getGameModeForPlayer().getId());
        saved.putString("dim", player.level().dimension().location().toString());
        saved.putDouble("x", player.getX());
        saved.putDouble("y", player.getY());
        saved.putDouble("z", player.getZ());
        saved.putFloat("yRot", player.getYRot());
        saved.putFloat("xRot", player.getXRot());
        PhobiaData.persisted(player).put(SAVE_KEY, saved);
    }

    private static CompoundTag takeSaved(ServerPlayer player) {
        CompoundTag persisted = PhobiaData.persisted(player);
        if (!persisted.contains(SAVE_KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag saved = persisted.getCompound(SAVE_KEY).copy();
        persisted.remove(SAVE_KEY);
        return saved;
    }

    private static void restoreInventory(ServerPlayer player, CompoundTag saved) {
        player.getInventory().clearContent();
        player.getInventory().load(saved.getList("inventory", Tag.TAG_COMPOUND));
        player.setGameMode(GameType.byId(saved.getInt("gamemode")));
    }

    private static void returnPlayer(ServerPlayer player, CompoundTag saved, boolean teleport) {
        if (teleport) {
            MinecraftServer server = player.server;
            ServerLevel target = server.overworld();
            BlockPos spawn = target.getSharedSpawnPos();
            target.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4);
            double x = spawn.getX() + 0.5D;
            double y = target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
            double z = spawn.getZ() + 0.5D;
            float yRot = player.getYRot();
            float xRot = 0.0F;

            if (saved != null && saved.contains("dim", Tag.TAG_STRING)) {
                ResourceLocation id = ResourceLocation.tryParse(saved.getString("dim"));
                if (id != null) {
                    ServerLevel destination = server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
                    if (destination != null && !destination.dimension().equals(DIMENSION)) {
                        target = destination;
                        x = saved.getDouble("x");
                        y = saved.getDouble("y");
                        z = saved.getDouble("z");
                        yRot = saved.getFloat("yRot");
                        xRot = saved.getFloat("xRot");
                    }
                }
            }
            player.teleportTo(target, x, y, z, yRot, xRot);
        }

        if (saved != null) {
            restoreInventory(player, saved);
        }
    }

    private static void showTitle(ServerPlayer player, String titleKey, String subtitleKey) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(subtitleKey)));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(titleKey)));
    }

    // ------------------------------------------------------------------
    // Construccion de la salida
    // ------------------------------------------------------------------

    private static void loadArea(ServerLevel level, int blockX, int blockZ, int chunkRadius) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                level.getChunk(chunkX + dx, chunkZ + dz);
            }
        }
    }

    private static void clearBox(ServerLevel level, int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    /** Plataforma de piedra oscura con una baliza: su haz blanco se ve de lejos. */
    private static void buildExit(ServerLevel level, BlockPos center) {
        int x = center.getX();
        int z = center.getZ();

        clearBox(level, x - 4, GROUND_Y + 1, z - 4, x + 4, GROUND_Y + 40, z + 4);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(new BlockPos(x + dx, GROUND_Y, z + dz),
                        Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }

        level.setBlock(new BlockPos(x, GROUND_Y + 1, z), Blocks.BEACON.defaultBlockState(), Block.UPDATE_ALL);

        int[] offsets = {-2, 2};
        for (int dx : offsets) {
            for (int dz : offsets) {
                for (int dy = 1; dy <= 4; dy++) {
                    level.setBlock(new BlockPos(x + dx, GROUND_Y + dy, z + dz),
                            Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                level.setBlock(new BlockPos(x + dx, GROUND_Y + 5, z + dz),
                        Blocks.SOUL_LANTERN.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
