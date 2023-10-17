package ctf.ctfplugin;

import ctf.ctfplugin.commands.JoinCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CTFPlugin extends JavaPlugin implements Listener {
    public static CaptureTheFlagGame game;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        Location redFlagLocation = new Location(getServer().getWorld("world"), 76, 74, -5); // Set the coordinates
        Location blueFlagLocation = new Location(getServer().getWorld("world"), 78, 74, -5); // Set the coordinates

        // Create a game instance
        game = new CaptureTheFlagGame(redFlagLocation, blueFlagLocation);

        // Set up game event listeners
        getServer().getPluginManager().registerEvents(new EventListener(game), this);

        // Register commands
        this.getCommand("join").setExecutor(new JoinCommand());
    }

    @Override
    public void onDisable() { }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        disableHacks(p);

        // Add the player to the Boss Bar when they join
        game.addPlayerToBossBar(p);
    }

    private void disableHacks(Player p) {
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setFoodLevel(0); // Prevents sprinting
    }
}
