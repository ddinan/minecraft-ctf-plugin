package ctf.ctfplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static org.bukkit.Bukkit.*;

public class CaptureTheFlagGame {
    public static boolean roundInProgress = false;

    public static Location redFlagPosition;
    public static Location blueFlagPosition;
    public static Location redSpawnPosition;
    public static Location blueSpawnPosition;

    public static Player redFlagCarrier = null;
    public static Player blueFlagCarrier = null;

    public static BukkitRunnable gameTimer;
    private static BossBar bossBar;

    public static Scoreboard scoreboard;
    public static Team redTeam;
    public static Team blueTeam;
    public static Team spectatorTeam;

    public static World world;

    public CaptureTheFlagGame(Location redFlagPosition, Location blueFlagPosition) {
        this.redFlagPosition = redFlagPosition;
        this.blueFlagPosition = blueFlagPosition;

        world = chooseRandomMap();

        startGame();
    }

    static void startGame() {
        setUpTeams();
        setUpMap();
        startGameTimer(); // Start the game timer
        roundInProgress = true;
    }

    private static void setUpTeams() {
        scoreboard = getScoreboardManager().getMainScoreboard();

        redTeam = scoreboard.getTeam("Red");

        if (redTeam == null) {
            redTeam = scoreboard.registerNewTeam("Red");
            redTeam.setPrefix(ChatColor.RED.toString());
        }

        blueTeam = scoreboard.getTeam("Blue");

        if (blueTeam == null) {
            blueTeam = scoreboard.registerNewTeam("Blue");
            blueTeam.setPrefix(ChatColor.BLUE.toString());
        }

        spectatorTeam = scoreboard.getTeam("Spectator");

        if (spectatorTeam == null) {
            spectatorTeam = scoreboard.registerNewTeam("Spectator");
            spectatorTeam.setPrefix(ChatColor.GRAY.toString());
        }
    }

    private static void setUpMap() {
        Properties properties = new Properties();
        FileInputStream input = null;

        try {
            // Load the properties file for the specific map
            input = new FileInputStream("./maps/" + world.getName() + ".properties");
            properties.load(input);

            // Place flags in the right spot
            redFlagPosition = parseLocation(properties.getProperty("redFlagPosition"));
            blueFlagPosition = parseLocation(properties.getProperty("blueFlagPosition"));

            respawnFlag("red");
            respawnFlag("blue");

            redSpawnPosition = parseLocation(properties.getProperty("redSpawnPosition"));
            blueSpawnPosition = parseLocation(properties.getProperty("blueSpawnPosition"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Location parseLocation(String input) {
        String[] parts = input.split(",");
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        return new Location(world, x, y, z);
    }

    private static void startGameTimer() {
        if (bossBar != null) {
            bossBar.removeAll(); // Remove the old BossBar
        }

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
                }

                else {
                    // Reset the game after the timer is over
                    endGame(null);
                }
            }
        };

        gameTimer.runTaskTimer(CTFPlugin.getPlugin(CTFPlugin.class), 20, 20); // Run every second
    }

    public void addPlayerToBossBar(Player player) {
        bossBar.addPlayer(player);
    }

    private static void updateBossBar(int timeLeft) {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;

        String timeString = String.format("%d:%02d", minutes, seconds);
        bossBar.setTitle("Time Left: " + timeString);
        bossBar.setProgress((double) timeLeft / 300);
    }

    public static void endGame(World newWorld) {
        // Reset game state, clear flags, etc.
        roundInProgress = false;
        redFlagCarrier = null;
        blueFlagCarrier = null;

        // If no map was specified, choose a random map to start the game on
        if (newWorld == null) {
            newWorld = chooseRandomMap();
        }

        world = newWorld;

        // Teleport players to the new world
        for (Player player : getOnlinePlayers()) {
            player.teleport(newWorld.getSpawnLocation());
        }

        gameTimer.cancel(); // Cancel the timer

        startGame(); // Restart the game loop
    }

    static World chooseRandomMap() {
        // Randomly choose a new world to start the game on
        List<World> loadedWorlds = getWorlds();

        if (!loadedWorlds.isEmpty()) {
            return loadedWorlds.get(new Random().nextInt(loadedWorlds.size()));
        }

        return null;
    }

    public boolean joinTeam(Player player, String team) {
        if (team.equalsIgnoreCase("red")) {
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
        }

        else if (team.equalsIgnoreCase("blue")) {
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
        }

        else if (team.equalsIgnoreCase("spectator")) {
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
        if (redTeam.hasEntry(player.getName())) {
            respawnFlag("blue");
        }

        else if (blueTeam.hasEntry(player.getName())) {
            respawnFlag("red");
        }

        // TODO: Increment points, and update game score
    }

    static void respawnFlag(String team) {
        Location location = new Location(world, 0, 0, 0);
        Material material = Material.MAGMA_BLOCK;

        if (team.equalsIgnoreCase("red")) {
            location = redFlagPosition;
            material = Material.RED_WOOL;
            redFlagCarrier = null;
        }

        else if (team.equalsIgnoreCase("blue")) {
            location = blueFlagPosition;
            material = Material.BLUE_WOOL;
            blueFlagCarrier = null;
        }

        Block block = location.getBlock();
        block.setType(material);
    }

    public void messageWorld(World world, String message) {
        for (Player player : world.getPlayers()) {
            player.sendMessage(message);
        }
    }
}
