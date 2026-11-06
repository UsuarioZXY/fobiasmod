package com.fobias.command;

import com.fobias.item.FlashlightItem;
import com.fobias.registry.ModItems;
import com.fobias.world.PurgatoryGame;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * /nivel orbe | linterna | salir
 */
public final class LevelCommand {

    private LevelCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nivel")
                .then(Commands.literal("orbe")
                        .executes(ctx -> give(ctx.getSource(), new ItemStack(ModItems.PURGATORY_ORB.get()),
                                "commands.fobias.orb_given")))
                .then(Commands.literal("linterna")
                        .executes(ctx -> {
                            ItemStack flashlight = new ItemStack(ModItems.FLASHLIGHT.get());
                            FlashlightItem.setOn(flashlight, false);
                            return give(ctx.getSource(), flashlight, "commands.fobias.flashlight_given");
                        }))
                .then(Commands.literal("salir")
                        .executes(ctx -> leave(ctx.getSource()))));
    }

    private static int give(CommandSourceStack source, ItemStack stack, String messageKey)
            throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        source.sendSuccess(() -> Component.translatable(messageKey), false);
        return 1;
    }

    /** Sale del nivel si algo sale mal (devuelve tu inventario). */
    private static int leave(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!PurgatoryGame.isPlaying(player)) {
            source.sendFailure(Component.translatable("message.fobias.not_playing"));
            return 0;
        }
        PurgatoryGame.end(player, PurgatoryGame.Outcome.ABORT);
        return 1;
    }
}
