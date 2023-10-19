package ctf.ctfplugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.FileUtil;

import java.io.*;
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
    public static Location spawnPosition;

    public static Player redFlagCarrier = null;
    public static Player blueFlagCarrier = null;

    public static BukkitRunnable gameTimer;
    private static BossBar bossBar;

    public static Scoreboard scoreboard;
    public static Team redTeam;
    public static Team blueTeam;
    public static Team spectatorTeam;

    public static World world;

    public CaptureTheFlagGame() {
        world = chooseRandomMap();

        System.out.println("Chosen World: " + world);
        startGame();
    }

    static void startGame() {
        backupWorld(new File("maps/" + world.getName()), new File("map_backups/" + world.getName())); // Make a copy of the world
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
            redFlagPosition = parseLocation(properties.getProperty("redFlagPosition"), false);
            blueFlagPosition = parseLocation(properties.getProperty("blueFlagPosition"), false);

            respawnFlag("red");
            respawnFlag("blue");

            redSpawnPosition = parseLocation(properties.getProperty("redSpawnPosition"), true);
            blueSpawnPosition = parseLocation(properties.getProperty("blueSpawnPosition"), true);
            spawnPosition = parseLocation(properties.getProperty("spawnPosition"), true);
            world.setSpawnLocation((int)spawnPosition.getX(), (int)spawnPosition.getY(), (int)spawnPosition.getZ()); // Update the world's spawn point
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

    private static Location parseLocation(String input, Boolean offset) {
        String[] parts = input.split(",");

        double offsetAmount = offset ? 0.5 : 0; // Use 0.5 to offset the location to its center
        double x = Double.parseDouble(parts[0]) + offsetAmount;
        double y = Double.parseDouble(parts[1]) + offsetAmount;
        double z = Double.parseDouble(parts[2]) + offsetAmount;

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

        // Teleport players to the new world
        for (Player player : getOnlinePlayers()) {
            player.teleport(newWorld.getSpawnLocation());
        }

        backupWorld(new File("map_backups/" + world.getName()), new File("maps/" + world.getName())); // Restore the world before restarting the game

        world = newWorld;

        gameTimer.cancel(); // Cancel the timer

        startGame(); // Restart the game loop
    }

    public static void backupWorld(File source, File destination) {
        // Unload the world
        String worldName = source.getName();
        World world = getWorld(worldName);

        if (world != null) {
            getServer().unloadWorld(world, false);
        }

        // Perform the backup
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdir();
            }

            File[] files = source.listFiles();

            for (File file : files) {
                File toFile = new File(destination, file.getName());

                FileUtil.copy(file, toFile);

                if (file.isDirectory()) {
                    backupWorld(file, toFile);
                }
            }
        }

        else {
            FileUtil.copy(source, destination);
        }

        // Reload the world if it was unloaded
        if (world != null) {
            getServer().createWorld(new WorldCreator(worldName));
        }
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

                    disableHacks(player);
                    player.teleport(redSpawnPosition); // Respawn the player at the team's spawn point

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

                    disableHacks(player);
                    player.teleport(blueSpawnPosition); // Respawn the player at the team's spawn point

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

            enableHacks(player);
            player.teleport(world.getSpawnLocation()); // Respawn the player at the world's spawn point

            return true;
        }

        return false; // Player is already in a team or teams are not balanced
    }

    private void enableHacks(Player p) {
        p.setAllowFlight(true);
        p.setFoodLevel(20);
    }

    private void disableHacks(Player p) {
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setFoodLevel(0); // Prevents sprinting
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
