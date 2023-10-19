package ctf.ctfplugin;

import ctf.ctfplugin.commands.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

public final class CTFPlugin extends JavaPlugin implements Listener {
    public static CaptureTheFlagGame game;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Create a game instance
        game = new CaptureTheFlagGame();

        // Set up game event listeners
        getServer().getPluginManager().registerEvents(new EventListener(game), this);

        // Register commands
        this.getCommand("join").setExecutor(new JoinCommand());
        this.getCommand("maps").setExecutor(new MapsCommand());
        this.getCommand("newgame").setExecutor(new NewGameCommand());
        this.getCommand("visit").setExecutor(new VisitCommand());
    }

    @Override
    public void onDisable() {
        // Unregister teams
        if (game.scoreboard != null) {
            game.scoreboard.getTeams().forEach(Team::unregister);
        }

        // Cancel the game timer if it's running
        if (game.gameTimer != null && !game.gameTimer.isCancelled()) {
            game.gameTimer.cancel();
        }
    }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        // Sync the player with the game's boss bar
        game.addPlayerToBossBar(p);
    }
}
