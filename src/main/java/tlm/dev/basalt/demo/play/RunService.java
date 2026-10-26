package tlm.dev.basalt.demo.play;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tlm.dev.basalt.api.HostAreaException;
import tlm.dev.basalt.api.VirtualWorld;
import tlm.dev.basalt.demo.message.MessageKey;
import tlm.dev.basalt.demo.message.Messages;
import tlm.dev.basalt.demo.port.Entry;
import tlm.dev.basalt.demo.port.Refusal;
import tlm.dev.basalt.demo.port.RunBook;
import tlm.dev.basalt.demo.port.RunView;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RunService {

    private final RunBook book;
    private final RunWorlds worlds;
    private final ReturnPoints returns;
    private final PlayTools tools;

    public RunService(RunBook book, RunWorlds worlds, ReturnPoints returns, PlayTools tools) {
        this.book = book;
        this.worlds = worlds;
        this.returns = returns;
        this.tools = tools;
    }

    public void start(CommandSender sender, Player player) {
        Messages messages = tools.messages();
        Entry entry = book.open(player.getUniqueId(), worlds.monsterCount(), tools.clock().instant());
        if (entry instanceof Entry.Refused refused) {
            refuse(sender, refused.reason(), player, player);
            return;
        }
        RunView run = ((Entry.Admitted) entry).run();
        long start = System.nanoTime();
        VirtualWorld world;
        long created;
        long placed;
        try {
            world = worlds.create(run.id());
            created = System.nanoTime();
            worlds.place(world);
            placed = System.nanoTime();
            worlds.spawnMonsters(world, tools.settings().monsterHealth(), messages.render(MessageKey.MONSTER_NAME));
        } catch (HostAreaException | IllegalArgumentException | IllegalStateException error) {
            worlds.close(run.id());
            book.cancel(run.id());
            messages.send(sender, MessageKey.START_FAILED, unparsed("reason", String.valueOf(error.getMessage())));
            return;
        }
        long spawned = System.nanoTime();
        if (!enter(player, world)) {
            worlds.close(run.id());
            book.cancel(run.id());
            messages.send(sender, MessageKey.ENTRY_REFUSED, unparsed("player", player.getName()));
            return;
        }
        long entered = System.nanoTime();
        tools.logger().info(String.format(Locale.ROOT,
                "Run %s of %s started in %.2f ms on the main thread: world %.2f, place %.2f, monsters %.2f, entry %.2f",
                run.id().toString().substring(0, 8), player.getName(), millis(start, entered),
                millis(start, created), millis(created, placed), millis(placed, spawned), millis(spawned, entered)));
        messages.send(player, MessageKey.START_STARTED, unparsed("zombies", Integer.toString(run.monstersLeft())));
        if (sender != player) {
            messages.send(sender, MessageKey.START_DONE, unparsed("player", player.getName()));
        }
    }

    public void join(CommandSender sender, Player member, Player player) {
        Messages messages = tools.messages();
        Entry entry = book.join(member.getUniqueId(), player.getUniqueId());
        if (entry instanceof Entry.Refused refused) {
            refuse(sender, refused.reason(), player, member);
            return;
        }
        RunView run = ((Entry.Admitted) entry).run();
        Optional<VirtualWorld> world = worlds.world(run.id());
        if (world.isEmpty() || !enter(player, world.get())) {
            book.leave(player.getUniqueId());
            messages.send(sender, MessageKey.ENTRY_REFUSED, unparsed("player", player.getName()));
            return;
        }
        String leader = name(run.leader());
        messages.send(player, MessageKey.JOIN_JOINED, unparsed("leader", leader));
        for (UUID other : run.members()) {
            Player online = Bukkit.getPlayer(other);
            if (online != null && online != player) {
                messages.send(online, MessageKey.JOIN_ARRIVED, unparsed("player", player.getName()));
            }
        }
        if (sender != player) {
            messages.send(sender, MessageKey.JOIN_DONE, unparsed("player", player.getName()), unparsed("leader", leader));
        }
    }

    public void list(CommandSender sender) {
        Messages messages = tools.messages();
        List<RunView> runs = book.runs();
        TagResolver open = unparsed("worlds", Long.toString(worlds.openWorlds()));
        if (runs.isEmpty()) {
            messages.send(sender, MessageKey.LIST_EMPTY, open);
            return;
        }
        messages.send(sender, MessageKey.LIST_HEADER, unparsed("runs", Integer.toString(runs.size())), open);
        for (RunView run : runs) {
            messages.send(sender, MessageKey.LIST_ENTRY, describe(run));
        }
    }

    public void status(CommandSender sender, Player player) {
        Messages messages = tools.messages();
        TagResolver who = TagResolver.resolver(
                unparsed("player", player.getName()),
                unparsed("world", player.getWorld().getName()));
        Optional<RunView> run = book.runOf(player.getUniqueId());
        if (run.isEmpty()) {
            messages.send(sender, MessageKey.STATUS_NONE, who);
        } else {
            messages.send(sender, MessageKey.STATUS_RUN, who, describe(run.get()));
        }
    }

    private boolean enter(Player player, VirtualWorld world) {
        returns.remember(player);
        if (player.teleport(worlds.spawn())) {
            try {
                world.addViewer(player);
                return true;
            } catch (IllegalStateException refused) {
                tools.logger().warning("Could not show the dungeon to " + player.getName() + ": " + refused.getMessage());
                player.teleport(returns.take(player.getUniqueId()));
                return false;
            }
        }
        returns.take(player.getUniqueId());
        return false;
    }

    private void refuse(CommandSender sender, Refusal reason, Player player, Player member) {
        Messages messages = tools.messages();
        switch (reason) {
            case ALREADY_IN_RUN -> messages.send(sender, MessageKey.RUN_ALREADY, unparsed("player", player.getName()));
            case TOO_MANY_RUNS -> messages.send(sender, MessageKey.START_TOO_MANY,
                    unparsed("max", Integer.toString(tools.settings().maxRuns())));
            case NOT_IN_RUN -> messages.send(sender, MessageKey.JOIN_NO_RUN, unparsed("player", member.getName()));
            case RUN_FULL -> messages.send(sender, MessageKey.JOIN_FULL, unparsed("leader", leaderOf(member)),
                    unparsed("max", Integer.toString(tools.settings().maxPlayers())));
            case RUN_FINISHED -> messages.send(sender, MessageKey.JOIN_FINISHED, unparsed("leader", leaderOf(member)));
        }
    }

    private TagResolver describe(RunView run) {
        Messages messages = tools.messages();
        return TagResolver.resolver(
                unparsed("leader", name(run.leader())),
                unparsed("players", run.members().stream().map(RunService::name).collect(Collectors.joining(","))),
                unparsed("zombies", Integer.toString(run.monstersLeft())),
                Placeholder.component("door", messages.render(run.doorOpen() ? MessageKey.WORD_OPEN : MessageKey.WORD_CLOSED)),
                Placeholder.component("finished", messages.render(run.finished() ? MessageKey.WORD_YES : MessageKey.WORD_NO)));
    }

    private String leaderOf(Player member) {
        return book.runOf(member.getUniqueId()).map(run -> name(run.leader())).orElse(member.getName());
    }

    private static String name(UUID player) {
        Player online = Bukkit.getPlayer(player);
        return online != null ? online.getName() : player.toString().substring(0, 8);
    }

    private static TagResolver unparsed(String key, String value) {
        return Placeholder.unparsed(key, value);
    }

    private static double millis(long from, long to) {
        return (to - from) / 1_000_000.0;
    }
}
