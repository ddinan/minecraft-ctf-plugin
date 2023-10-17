package ctf.ctfplugin.commands;

import ctf.ctfplugin.CTFPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("red")) {
            if (CTFPlugin.game.joinTeam(player, "red")) {
                player.sendMessage(ChatColor.GREEN + "You joined the red team.");
            } else {
                player.sendMessage(ChatColor.RED + "You are already in a team.");
            }
        } else if (args[0].equalsIgnoreCase("blue")) {
            if (CTFPlugin.game.joinTeam(player, "blue")) {
                player.sendMessage(ChatColor.GREEN + "You joined the blue team.");
            } else {
                player.sendMessage(ChatColor.RED + "You are already in a team.");
            }
        } else if (args[0].equalsIgnoreCase("spectator")) {
            if (CTFPlugin.game.joinTeam(player, "spectator")) {
                player.sendMessage(ChatColor.GREEN + "You joined the spectator team.");
            } else {
                player.sendMessage(ChatColor.RED + "You are already in a team.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid team. Use /join [red|blue|spectator]");
        }

        return true;
    }
}