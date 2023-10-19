package ctf.ctfplugin.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getWorld;

public class VisitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            return false;
        }

        String worldName = args[0];

        World targetWorld = getWorld(worldName);

        if (targetWorld == null) {
            player.sendMessage("The specified world does not exist.");
            return true;
        }

        player.teleport(targetWorld.getSpawnLocation());
        player.sendMessage("Teleported to " + worldName);

        return true;
    }
}
