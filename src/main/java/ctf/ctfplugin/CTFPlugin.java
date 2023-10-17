package ctf.ctfplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CTFPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() { }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        disableHacks(p);
    }

    private void disableHacks(Player p) {
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setFoodLevel(0); // Prevents sprinting
    }
}
