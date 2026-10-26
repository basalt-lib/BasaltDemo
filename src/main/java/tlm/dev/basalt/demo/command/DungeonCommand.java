package tlm.dev.basalt.demo.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tlm.dev.basalt.demo.message.MessageKey;
import tlm.dev.basalt.demo.message.Messages;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class DungeonCommand {

    public static final String PLAY = "basalt.demo.play";
    public static final String ADMIN = "basalt.demo.admin";

    private static final String MEMBER = "member";
    private static final String PLAYER = "player";

    private final RunOrders runs;
    private final ModelOrders model;
    private final Messages messages;

    public DungeonCommand(RunOrders runs, ModelOrders model, Messages messages) {
        this.runs = runs;
        this.model = model;
        this.messages = messages;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("dungeon")
                .requires(source -> allowed(source, PLAY) || allowed(source, ADMIN))
                .then(Commands.literal("start")
                        .executes(context -> forSelf(context, runs.start()))
                        .then(otherPlayer().executes(context -> forNamed(context, runs.start()))))
                .then(Commands.literal("join")
                        .then(Commands.argument(MEMBER, StringArgumentType.word())
                                .suggests(this::onlineNames)
                                .executes(context -> join(context, false))
                                .then(otherPlayer().executes(context -> join(context, true)))))
                .then(Commands.literal("leave")
                        .executes(context -> forSelf(context, runs.leave()))
                        .then(otherPlayer().executes(context -> forNamed(context, runs.leave()))))
                .then(Commands.literal("list")
                        .executes(context -> forSender(context, runs.list())))
                .then(Commands.literal("status")
                        .executes(context -> forSelf(context, runs.status()))
                        .then(otherPlayer().executes(context -> forNamed(context, runs.status()))))
                .then(Commands.literal("model")
                        .requires(source -> allowed(source, ADMIN))
                        .then(Commands.literal("visit").executes(context -> forSelf(context, model.visit())))
                        .then(Commands.literal("capture").executes(context -> forSender(context, model.capture())))
                        .then(Commands.literal("rebuild").executes(context -> forSender(context, model.rebuild()))))
                .build();
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> otherPlayer() {
        return Commands.argument(PLAYER, StringArgumentType.word())
                .requires(source -> allowed(source, ADMIN))
                .suggests(this::onlineNames);
    }

    private int forSelf(CommandContext<CommandSourceStack> context, PlayerOrder order) {
        Player self = self(context);
        if (self != null) {
            order.run(context.getSource().getSender(), self);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int forNamed(CommandContext<CommandSourceStack> context, PlayerOrder order) {
        Player player = named(context, PLAYER);
        if (player != null) {
            order.run(context.getSource().getSender(), player);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int forSender(CommandContext<CommandSourceStack> context, SenderOrder order) {
        order.run(context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }

    private int join(CommandContext<CommandSourceStack> context, boolean forOther) {
        Player member = named(context, MEMBER);
        if (member == null) {
            return Command.SINGLE_SUCCESS;
        }
        Player player = forOther ? named(context, PLAYER) : self(context);
        if (player != null) {
            runs.join().run(context.getSource().getSender(), member, player);
        }
        return Command.SINGLE_SUCCESS;
    }

    private Player self(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getExecutor() instanceof Player self) {
            return self;
        }
        messages.send(context.getSource().getSender(), MessageKey.PLAYER_NEEDED);
        return null;
    }

    private Player named(CommandContext<CommandSourceStack> context, String argument) {
        CommandSender sender = context.getSource().getSender();
        String name = StringArgumentType.getString(context, argument);
        if (!PlayerNames.valid(name)) {
            messages.send(sender, MessageKey.PLAYER_INVALID, Placeholder.unparsed("name", name));
            return null;
        }
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) {
            messages.send(sender, MessageKey.PLAYER_UNKNOWN, Placeholder.unparsed("name", name));
        }
        return player;
    }

    private CompletableFuture<Suggestions> onlineNames(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String typed = builder.getRemainingLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(typed)) {
                builder.suggest(player.getName());
            }
        }
        return builder.buildFuture();
    }

    private static boolean allowed(CommandSourceStack source, String permission) {
        return source.getSender().hasPermission(permission);
    }
}
