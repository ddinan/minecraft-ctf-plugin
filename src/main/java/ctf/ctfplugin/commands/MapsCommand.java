package ctf.ctfplugin.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getWorlds;

public class MapsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        String availableMaps = getWorlds().stream().map(World::getName).collect(Collectors.joining(", "));
        player.sendMessage("Available maps: " + availableMaps);

        return true;
    }
}
