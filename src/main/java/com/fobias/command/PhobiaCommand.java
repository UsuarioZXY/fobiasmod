package com.fobias.command;

import java.util.Arrays;
import java.util.List;

import com.fobias.phobia.Phobia;
import com.fobias.phobia.PhobiaData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * /fobia list | add <fobia> | remove <fobia> | clear
 * Sirve para probar el mod. Mas adelante se pueden ganar fobias de forma natural.
 */
public final class PhobiaCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PHOBIAS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(Phobia.values()).map(Phobia::getId), builder);

    private PhobiaCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fobia")
                .then(Commands.literal("list")
                        .executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("add")
                        .then(Commands.argument("fobia", StringArgumentType.word())
                                .suggests(SUGGEST_PHOBIAS)
                                .executes(ctx -> add(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "fobia")))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("fobia", StringArgumentType.word())
                                .suggests(SUGGEST_PHOBIAS)
                                .executes(ctx -> remove(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "fobia")))))
                .then(Commands.literal("clear")
                        .executes(ctx -> clear(ctx.getSource()))));
    }

    private static int list(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<Phobia> phobias = PhobiaData.getAll(player);

        if (phobias.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.fobias.list.empty"), false);
            return 0;
        }

        MutableComponent names = Component.empty();
        for (int i = 0; i < phobias.size(); i++) {
            if (i > 0) {
                names.append(", ");
            }
            names.append(phobias.get(i).getDisplayName());
        }
        source.sendSuccess(() -> Component.translatable("commands.fobias.list.header", names), false);
        return phobias.size();
    }

    private static int add(CommandSourceStack source, String id) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Phobia phobia = Phobia.byId(id).orElse(null);
        if (phobia == null) {
            return unknown(source, id);
        }
        if (PhobiaData.has(player, phobia)) {
            source.sendFailure(Component.translatable("commands.fobias.already", phobia.getDisplayName()));
            return 0;
        }
        PhobiaData.add(player, phobia);
        source.sendSuccess(() -> Component.translatable("commands.fobias.added", phobia.getDisplayName()), false);
        return 1;
    }

    private static int remove(CommandSourceStack source, String id) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Phobia phobia = Phobia.byId(id).orElse(null);
        if (phobia == null) {
            return unknown(source, id);
        }
        if (!PhobiaData.has(player, phobia)) {
            source.sendFailure(Component.translatable("commands.fobias.not_have", phobia.getDisplayName()));
            return 0;
        }
        PhobiaData.remove(player, phobia);
        source.sendSuccess(() -> Component.translatable("commands.fobias.removed", phobia.getDisplayName()), false);
        return 1;
    }

    private static int clear(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PhobiaData.clear(player);
        source.sendSuccess(() -> Component.translatable("commands.fobias.cleared"), false);
        return 1;
    }

    private static int unknown(CommandSourceStack source, String id) {
        source.sendFailure(Component.translatable("commands.fobias.unknown", id));
        source.sendFailure(Component.translatable("commands.fobias.available"));
        return 0;
    }
}
