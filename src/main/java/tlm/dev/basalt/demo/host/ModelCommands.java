package tlm.dev.basalt.demo.host;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tlm.dev.basalt.api.Blueprint;
import tlm.dev.basalt.demo.message.MessageKey;
import tlm.dev.basalt.demo.message.Messages;
import tlm.dev.basalt.demo.port.RunBook;

import java.util.Locale;
import java.util.function.Supplier;

public final class ModelCommands {

    private final ModelService model;
    private final Messages messages;
    private final RunBook book;

    public ModelCommands(ModelService model, Messages messages, RunBook book) {
        this.model = model;
        this.messages = messages;
        this.book = book;
    }

    public void visit(CommandSender sender, Player player) {
        if (book.runOf(player.getUniqueId()).isPresent()) {
            messages.send(sender, MessageKey.MODEL_IN_RUN);
            return;
        }
        if (player.teleport(model.entry())) {
            messages.send(player, MessageKey.MODEL_VISIT);
        } else {
            messages.send(sender, MessageKey.MODEL_VISIT_REFUSED);
        }
    }

    public void capture(CommandSender sender) {
        timed(sender, MessageKey.MODEL_CAPTURED, model::capture);
    }

    public void rebuild(CommandSender sender) {
        timed(sender, MessageKey.MODEL_REBUILT, model::rebuild);
    }

    private void timed(CommandSender sender, MessageKey done, Supplier<Blueprint> action) {
        long start = System.nanoTime();
        Blueprint blueprint;
        try {
            blueprint = action.get();
        } catch (IllegalArgumentException | IllegalStateException error) {
            messages.send(sender, MessageKey.MODEL_FAILED, Placeholder.unparsed("reason", String.valueOf(error.getMessage())));
            return;
        }
        double millis = (System.nanoTime() - start) / 1_000_000.0;
        messages.send(sender, done,
                Placeholder.unparsed("blocks", Integer.toString(blueprint.blockCount())),
                Placeholder.unparsed("time", String.format(Locale.ROOT, "%.1f", millis)));
    }
}
