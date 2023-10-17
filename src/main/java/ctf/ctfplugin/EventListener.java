package ctf.ctfplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static ctf.ctfplugin.CaptureTheFlagGame.blueTeam;
import static ctf.ctfplugin.CaptureTheFlagGame.redTeam;
import static org.bukkit.Bukkit.getWorld;

public class EventListener implements Listener {
    private final CaptureTheFlagGame game;

    public EventListener(CaptureTheFlagGame game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Location clickedBlockLocation = event.getClickedBlock().getLocation();

            if (clickedBlockLocation.equals(game.redFlagLocation)) {
                if (blueTeam.hasEntry(p.getName())) {
                    if (game.redFlagCarrier == null) {
                        game.redFlagCarrier = p;

                        String message = ChatColor.BLUE + "" + ChatColor.BOLD + p.getName() + ChatColor.RESET + ChatColor.GRAY + " has taken the " + ChatColor.RED + ChatColor.BOLD + "red " + ChatColor.RESET + ChatColor.GRAY + "flag!";
                        game.messageWorld(getWorld("world"), message);
                        return;
                    }
                } else if (redTeam.hasEntry(p.getName())) {
                    if (game.blueFlagCarrier == p) {
                        game.captureFlag(p);

                        String message = ChatColor.RED + "" + ChatColor.BOLD + p.getName() + ChatColor.RESET + ChatColor.GRAY + " has captured the " + ChatColor.BLUE + ChatColor.BOLD + "blue " + ChatColor.RESET + ChatColor.GRAY + "flag!";
                        game.messageWorld(getWorld("world"), message);

                        event.setCancelled(true);
                        return;
                    }
                }

                event.setCancelled(true);
            }

            else if (clickedBlockLocation.equals(game.blueFlagLocation)) {
                if (redTeam.hasEntry(p.getName())) {
                    if (game.blueFlagCarrier == null) {
                        game.blueFlagCarrier = p;

                        String message = ChatColor.RED + "" + ChatColor.BOLD + p.getName() + ChatColor.RESET + ChatColor.GRAY + " has taken the " + ChatColor.BLUE + ChatColor.BOLD + "blue " + ChatColor.RESET + ChatColor.GRAY + "flag!";
                        game.messageWorld(getWorld("world"), message);
                        return;
                    }
                } else if (blueTeam.hasEntry(p.getName())) {
                    if (game.redFlagCarrier == p) {
                        game.captureFlag(p);

                        String message = ChatColor.BLUE + "" + ChatColor.BOLD + p.getName() + ChatColor.RESET + ChatColor.GRAY + " has captured the " + ChatColor.RED + ChatColor.BOLD + "red " + ChatColor.RESET + ChatColor.GRAY + "flag!";
                        game.messageWorld(getWorld("world"), message);

                        event.setCancelled(true);
                        return;
                    }
                }

                event.setCancelled(true);
            }
        }
    }
}
