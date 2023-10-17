package ctf.ctfplugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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

            p.sendMessage("Left clicked block at " + clickedBlockLocation.getBlockX() + " " + clickedBlockLocation.getBlockY() + " " + clickedBlockLocation.getBlockZ());

            p.sendMessage(game.getRedFlagLocation() + " red flag");
            if (clickedBlockLocation.equals(game.getRedFlagLocation())) {
                p.sendMessage("Clicked on red flag");
            }

            if (clickedBlockLocation.equals(game.getBlueFlagLocation())) {
                p.sendMessage("Clicked on blue flag");
            }
        }
    }
}
