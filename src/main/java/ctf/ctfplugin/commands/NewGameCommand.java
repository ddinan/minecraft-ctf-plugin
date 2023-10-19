package ctf.ctfplugin.commands;

import ctf.ctfplugin.CaptureTheFlagGame;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getWorld;

public class NewGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            return false;
        }

        String worldName = args[0];
        World newWorld = getWorld(worldName);

        if (worldName.equalsIgnoreCase("hub")) {
            player.sendMessage("Cannot start a new game in the hub world.");
            return true;
        }

        if (newWorld != null) {
            CaptureTheFlagGame.endGame(newWorld);
            player.sendMessage("Started a new game in world " + worldName);
        }

        else {
            player.sendMessage("The specified world does not exist.");
        }

        return true;
    }
}
