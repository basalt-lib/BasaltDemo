package tlm.dev.basalt.demo.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface PlayerOrder {

    void run(CommandSender sender, Player player);
}
