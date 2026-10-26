package tlm.dev.basalt.demo.command;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface SenderOrder {

    void run(CommandSender sender);
}
