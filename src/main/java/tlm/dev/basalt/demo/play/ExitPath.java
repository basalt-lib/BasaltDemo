package tlm.dev.basalt.demo.play;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tlm.dev.basalt.api.VirtualWorld;
import tlm.dev.basalt.api.event.VirtualViewerRemoveEvent.Reason;
import tlm.dev.basalt.demo.lifecycle.Lifecycle;
import tlm.dev.basalt.demo.message.MessageKey;
import tlm.dev.basalt.demo.message.Messages;
import tlm.dev.basalt.demo.port.RunBook;
import tlm.dev.basalt.demo.port.RunView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ExitPath implements Lifecycle {

    private final RunBook book;
    private final RunWorlds worlds;
    private final ReturnPoints returns;
    private final Messages messages;

    public ExitPath(RunBook book, RunWorlds worlds, ReturnPoints returns, Messages messages) {
        this.book = book;
        this.worlds = worlds;
        this.returns = returns;
        this.messages = messages;
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public void onDisable() {
        for (RunView run : book.runs()) {
            for (UUID member : run.members()) {
                Player player = Bukkit.getPlayer(member);
                if (player != null) {
                    leaveRun(player, Reason.WORLD_CLOSED);
                }
            }
        }
    }

    public void request(CommandSender sender, Player player) {
        if (book.runOf(player.getUniqueId()).isEmpty()) {
            messages.send(sender, MessageKey.LEAVE_NOT_IN_RUN, Placeholder.unparsed("player", player.getName()));
            return;
        }
        leave(player);
        if (sender != player) {
            messages.send(sender, MessageKey.LEAVE_DONE, Placeholder.unparsed("player", player.getName()));
        }
    }

    public void exit(Player player, VirtualWorld world, Reason reason) {
        Optional<UUID> run = worlds.runOf(world);
        Optional<RunView> mine = book.runOf(player.getUniqueId());
        if (run.isEmpty() || mine.isEmpty() || !mine.get().id().equals(run.get())) {
            return;
        }
        leaveRun(player, reason);
    }

    private void leave(Player player) {
        Optional<VirtualWorld> world = book.runOf(player.getUniqueId()).flatMap(run -> worlds.world(run.id()));
        if (world.isPresent() && world.get().hasViewer(player)) {
            world.get().removeViewer(player);
        } else {
            leaveRun(player, Reason.REQUESTED);
        }
    }

    private void leaveRun(Player player, Reason reason) {
        Optional<RunView> left = book.leave(player.getUniqueId());
        if (left.isEmpty()) {
            return;
        }
        RunView after = left.get();
        Location back = returns.take(player.getUniqueId());
        switch (reason) {
            case REQUESTED -> {
                player.teleport(back);
                messages.send(player, MessageKey.LEAVE_LEFT);
            }
            case WORLD_CLOSED -> {
                player.teleport(back);
                messages.send(player, MessageKey.RETURN_BACK);
            }
            case REAL_WORLD_CHANGE -> messages.send(player, MessageKey.LEAVE_LEFT);
            case DISCONNECT -> {
                if (!player.teleport(back)) {
                    returns.keepForReturn(player.getUniqueId(), back);
                }
            }
        }
        if (reason == Reason.WORLD_CLOSED) {
            return;
        }
        tellOthers(after.members(), player);
        if (after.empty()) {
            worlds.close(after.id());
        }
    }

    private void tellOthers(List<UUID> members, Player gone) {
        for (UUID member : members) {
            Player online = Bukkit.getPlayer(member);
            if (online != null) {
                messages.send(online, MessageKey.LEAVE_GONE, Placeholder.unparsed("player", gone.getName()));
            }
        }
    }
}
