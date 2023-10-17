package ctf.ctfplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import static org.bukkit.Bukkit.*;

public class CaptureTheFlagGame {
    private Location redFlagLocation;
    private Location blueFlagLocation;

    private BukkitRunnable gameTimer;
    private BossBar bossBar;

    Scoreboard scoreboard;
    Team redTeam;
    Team blueTeam;
    Team spectatorTeam;

    public CaptureTheFlagGame(Location redFlagLocation, Location blueFlagLocation) {
        this.redFlagLocation = redFlagLocation;
        this.blueFlagLocation = blueFlagLocation;

        // Start the game timer
        startGameTimer();
        setUpScoreboard();
    }

    private void setUpScoreboard() {
        scoreboard = getScoreboardManager().getMainScoreboard();
        redTeam = scoreboard.registerNewTeam("Red");
        blueTeam = scoreboard.registerNewTeam("Blue");
        spectatorTeam = scoreboard.registerNewTeam("Spectator");

        // Set team options as needed
        redTeam.setPrefix(ChatColor.RED.toString());
        blueTeam.setPrefix(ChatColor.BLUE.toString());
        spectatorTeam.setPrefix(ChatColor.GRAY.toString());
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

    public boolean joinTeam(Player player, String team) {
        if (team == "red") {
            if (!redTeam.hasEntry(player.getName())) {
                if (blueTeam.getSize() <= redTeam.getSize()) {
                    redTeam.addEntry(player.getName());
                    blueTeam.removeEntry(player.getName());
                    spectatorTeam.removeEntry(player.getName());

                    player.setDisplayName(ChatColor.RED + player.getName());
                    player.setPlayerListName(ChatColor.RED + player.getName());

                    return true;
                }
            }
        } else if (team == "blue") {
            if (!blueTeam.hasEntry(player.getName())) {
                if (redTeam.getSize() <= blueTeam.getSize()) {
                    blueTeam.addEntry(player.getName());
                    redTeam.removeEntry(player.getName());
                    spectatorTeam.removeEntry(player.getName());

                    player.setDisplayName(ChatColor.BLUE + player.getName());
                    player.setPlayerListName(ChatColor.BLUE + player.getName());

                    return true;
                }
            }
        } else if (team == "spectator") {
            spectatorTeam.addEntry(player.getName());
            redTeam.removeEntry(player.getName());
            blueTeam.removeEntry(player.getName());

            player.setDisplayName(ChatColor.GRAY + player.getName());
            player.setPlayerListName(ChatColor.GRAY + player.getName());

            return true;
        }

        return false; // Player is already in a team or teams are not balanced
    }

    public void captureFlag(Player player) {
        return;
    }

    public Location getRedFlagLocation() {
        return redFlagLocation;
    }

    public Location getBlueFlagLocation() {
        return blueFlagLocation;
    }
}
