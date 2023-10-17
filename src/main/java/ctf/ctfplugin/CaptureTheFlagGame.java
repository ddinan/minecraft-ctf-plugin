package ctf.ctfplugin;

import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.createBossBar;
import static org.bukkit.Bukkit.getOnlinePlayers;

public class CaptureTheFlagGame {
    private Map<Player, Boolean> redTeam = new HashMap<>();
    private Map<Player, Boolean> blueTeam = new HashMap<>();
    private Location redFlagLocation;
    private Location blueFlagLocation;

    private BukkitRunnable gameTimer;
    private BossBar bossBar;

    public CaptureTheFlagGame(Location redFlagLocation, Location blueFlagLocation) {
        this.redFlagLocation = redFlagLocation;
        this.blueFlagLocation = blueFlagLocation;

        // Start the game timer
        startGameTimer();
    }

    private void startGameTimer() {
        bossBar = createBossBar("Time Left: 5:00", BarColor.GREEN, BarStyle.SOLID);
        bossBar.setProgress(1.0);

        for (Player player : getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        gameTimer = new BukkitRunnable() {
            int timeLeft = 300; // 300 seconds = 5 minutes

            @Override
            public void run() {
                if (timeLeft > 0) {
                    timeLeft--;
                    updateBossBar(timeLeft);
                } else {
                    // Reset the game after the timer is over
                    resetGame();
                }
            }
        };

        gameTimer.runTaskTimer(CTFPlugin.getPlugin(CTFPlugin.class), 20, 20); // Run every second
    }

    public void addPlayerToBossBar(Player player) {
        bossBar.addPlayer(player);
    }

    private void updateBossBar(int timeLeft) {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;

        String timeString = String.format("%d:%02d", minutes, seconds);
        bossBar.setTitle("Time Left: " + timeString);
        bossBar.setProgress((double) timeLeft / 300);
    }

    private void resetGame() {
        // Reset game state, clear flags, etc.

        // Restart the timer
        gameTimer.cancel();
        startGameTimer();
    }


    public void joinRedTeam(Player player) {
        redTeam.put(player, false);
    }

    public void joinBlueTeam(Player player) {
        blueTeam.put(player, false);
    }

    public void captureFlag(Player player) {
        if (redTeam.containsKey(player) && !redTeam.get(player)) {
            redTeam.put(player, true);
        } else if (blueTeam.containsKey(player) && !blueTeam.get(player)) {
            blueTeam.put(player, true);
        }
    }

    public boolean isRedFlagCaptured() {
        return redTeam.values().stream().allMatch(captured -> captured);
    }

    public boolean isBlueFlagCaptured() {
        return blueTeam.values().stream().allMatch(captured -> captured);
    }

    public Location getRedFlagLocation() {
        return redFlagLocation;
    }

    public Location getBlueFlagLocation() {
        return blueFlagLocation;
    }
}
