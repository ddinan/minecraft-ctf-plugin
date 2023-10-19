package ctf.ctfplugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

import static ctf.ctfplugin.CaptureTheFlagGame.blueTeam;
import static ctf.ctfplugin.CaptureTheFlagGame.redTeam;
import static org.bukkit.Bukkit.getWorld;

public class EventListener implements Listener {
    private final CaptureTheFlagGame game;
    private HashMap<Player, Location> activeTNTs = new HashMap<>();

    public EventListener(CaptureTheFlagGame game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (!redTeam.hasEntry(p.getName()) && !blueTeam.hasEntry(p.getName())) {
                p.sendMessage("Please join a team to build.");
                event.setCancelled(true);
                return;
            }

            Location clickedBlockLocation = event.getClickedBlock().getLocation();

            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock != null && clickedBlock.getType() == Material.TNT) {
                event.setCancelled(true);
                return;
            }

            if (clickedBlockLocation.equals(game.redFlagLocation)) {
                if (blueTeam.hasEntry(p.getName())) {
                    if (game.redFlagCarrier == null) {
                        game.redFlagCarrier = p;

                        String message = ChatColor.BLUE + "" + ChatColor.BOLD + p.getName() + ChatColor.RESET + ChatColor.GRAY + " has taken the " + ChatColor.RED + ChatColor.BOLD + "red " + ChatColor.RESET + ChatColor.GRAY + "flag!";
                        game.messageWorld(getWorld("world"), message);
                        return;
                    }
                }

                else if (redTeam.hasEntry(p.getName())) {
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
                }

                else if (blueTeam.hasEntry(p.getName())) {
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

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!redTeam.hasEntry(player.getName()) && !blueTeam.hasEntry(player.getName())) {
            player.sendMessage("Please join a team to build.");
            event.setCancelled(true);
            return;
        }

        Block placedBlock = event.getBlock();
        Location placedLocation = placedBlock.getLocation();

        if (placedBlock.getType() == Material.TNT) {
            if (activeTNTs.containsKey(player)) {
                //player.sendMessage(ChatColor.RED + "You can only have one active TNT block at a time.");
                event.setCancelled(true);
            }

            else {
                activeTNTs.put(player, placedLocation);
            }
        }

        else if (placedBlock.getType() == Material.PURPLE_WOOL && activeTNTs.containsKey(player)) {
            explodeTNT(player, placedLocation);
        }
    }

    private void explodeTNT(Player player, Location location) {
        location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 16, 0.2, 0.2, 0.2, 0.1);
        removeBlocksAroundTNT(activeTNTs.get(player)); // Explode all blocks in a 2x2 radius
        activeTNTs.remove(player);

        // TODO: Kill enemies in range

        // TODO: Defuse mines
    }

    public void removeBlocksAroundTNT(Location location) {
        World world = location.getWorld();

        int centerX = location.getBlockX();
        int centerY = location.getBlockY();
        int centerZ = location.getBlockZ();

        // Remove placed TNT on explode
        Block block = world.getBlockAt(centerX, centerY, centerZ);
        block.setType(Material.AIR);

        int radius = 2;

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    block = world.getBlockAt(x, y, z);

                    // Check if the block is not bedrock or TNT
                    if (block.getType() != Material.BEDROCK && block.getType() != Material.TNT) {
                        block.setType(Material.AIR); // Remove the block
                    }
                }
            }
        }
    }

}
