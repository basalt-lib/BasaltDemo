package tlm.dev.basalt.demo.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tlm.dev.basalt.demo.message.MessageKey;
import tlm.dev.basalt.demo.message.Messages;

public final class DungeonCommand {

    public static final String PLAY = "basalt.demo.play";
    public static final String ADMIN = "basalt.demo.admin";

    private final ModelOrders model;
    private final Messages messages;

    public DungeonCommand(ModelOrders model, Messages messages) {
        this.model = model;
        this.messages = messages;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("dungeon")
                .requires(source -> allowed(source, PLAY) || allowed(source, ADMIN))
                .then(Commands.literal("model")
                        .requires(source -> allowed(source, ADMIN))
                        .then(Commands.literal("visit").executes(context -> forSelf(context, model.visit())))
                        .then(Commands.literal("capture").executes(context -> forSender(context, model.capture())))
                        .then(Commands.literal("rebuild").executes(context -> forSender(context, model.rebuild()))))
                .build();
    }

    private int forSelf(CommandContext<CommandSourceStack> context, PlayerOrder order) {
        CommandSender sender = context.getSource().getSender();
        if (context.getSource().getExecutor() instanceof Player self) {
            order.run(sender, self);
        } else {
            messages.send(sender, MessageKey.PLAYER_NEEDED);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int forSender(CommandContext<CommandSourceStack> context, SenderOrder order) {
        order.run(context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }

    private static boolean allowed(CommandSourceStack source, String permission) {
        return source.getSender().hasPermission(permission);
    }
}
