package tlm.dev.basalt.demo.play;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tlm.dev.basalt.api.VirtualWorld;
import tlm.dev.basalt.demo.message.MessageKey;
import tlm.dev.basalt.demo.message.Messages;
import tlm.dev.basalt.demo.message.TimeText;
import tlm.dev.basalt.demo.port.RunBook;
import tlm.dev.basalt.demo.port.RunView;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class RunProgress {

    private static final long TICKS_PER_SECOND = 20;

    private final RunBook book;
    private final RunWorlds worlds;
    private final PlayTools tools;

    public RunProgress(RunBook book, RunWorlds worlds, PlayTools tools) {
        this.book = book;
        this.worlds = worlds;
        this.tools = tools;
    }

    public void monsterDied(VirtualWorld world) {
        Optional<UUID> run = worlds.runOf(world);
        if (run.isEmpty()) {
            return;
        }
        Messages messages = tools.messages();
        switch (book.monsterDied(run.get())) {
            case COUNTED -> {
                int left = book.run(run.get()).map(RunView::monstersLeft).orElse(0);
                world.viewers().forEach(viewer -> messages.send(viewer, MessageKey.MONSTER_DOWN,
                        Placeholder.unparsed("zombies", Integer.toString(left))));
            }
            case DOOR_OPENED -> {
                worlds.openDoor(run.get());
                world.viewers().forEach(viewer -> messages.send(viewer, MessageKey.DOOR_OPENED));
            }
            case IGNORED -> {
            }
        }
    }

    public boolean allowsBreak(VirtualWorld world, Player player, Location block) {
        Optional<UUID> run = worlds.runOf(world);
        if (run.isEmpty()) {
            return true;
        }
        if (!worlds.isExit(block)) {
            return false;
        }
        Optional<Duration> took = book.finish(run.get(), tools.clock().instant());
        if (took.isEmpty()) {
            if (book.run(run.get()).filter(RunView::doorOpen).isEmpty()) {
                tools.messages().send(player, MessageKey.EXIT_LOCKED);
            }
            return false;
        }
        finish(world, run.get(), took.get());
        return true;
    }

    public boolean allowsPlace(VirtualWorld world, Player player) {
        if (worlds.runOf(world).isEmpty()) {
            return true;
        }
        tools.messages().send(player, MessageKey.BUILD_REFUSED);
        return false;
    }

    private void finish(VirtualWorld world, UUID run, Duration took) {
        Messages messages = tools.messages();
        String time = TimeText.clock(took);
        int delay = tools.settings().returnDelaySeconds();
        Title title = Title.title(messages.render(MessageKey.FINISH_TITLE),
                messages.render(MessageKey.FINISH_SUBTITLE, Placeholder.unparsed("time", time)));
        for (Player viewer : world.viewers()) {
            viewer.showTitle(title);
            messages.send(viewer, MessageKey.FINISH_MESSAGE,
                    Placeholder.unparsed("time", time), Placeholder.unparsed("delay", Integer.toString(delay)));
        }
        tools.logger().info("Run " + run.toString().substring(0, 8) + " cleared in " + time);
        worlds.closeLater(run, delay * TICKS_PER_SECOND);
    }
}
