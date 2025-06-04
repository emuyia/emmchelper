package com.emuyia.emmchelper; // Match your package

import java.io.File; // Import your command classes
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.emuyia.emmchelper.commands.CancelCommand;
import com.emuyia.emmchelper.commands.ConfirmCommand;
import com.emuyia.emmchelper.commands.CooldownCheckCommand;
import com.emuyia.emmchelper.commands.RequestCommand;
import com.emuyia.emmchelper.abilities.ToggleFlyAbility;
import com.emuyia.emmchelper.abilities.ToggleInvisibilityAbility;
import com.starshootercity.OriginsAddon;
import com.starshootercity.abilities.Ability;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MCHelperPlugin extends OriginsAddon {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> pendingResets = new HashSet<>();

    private FileConfiguration playerDataConfig = null;
    private File playerDataFile = null;

    // Configurable values
    public int xpCost;
    public long resetCooldownMillis;
    public String originClearCommandTemplate;

    // Message Keys (for potential future localization or easier config)
    public static final String MSG_PREFIX = ChatColor.GOLD + "[OriginReset] " + ChatColor.RESET;
    public static final String MSG_NO_PERMISSION = MSG_PREFIX + ChatColor.RED + "You don't have permission to use this command.";
    public static final String MSG_PLAYER_ONLY = MSG_PREFIX + ChatColor.RED + "This command can only be run by a player.";
    // ... more messages can be defined here

    @Override
    public void onRegister() {
        // Load configuration
        saveDefaultConfig(); // Creates config.yml if it doesn't exist
        loadPluginConfig();

        // Register commands
        this.getCommand("requestoriginreset").setExecutor(new RequestCommand(this));
        this.getCommand("confirmoriginreset").setExecutor(new ConfirmCommand(this));
        this.getCommand("canceloriginreset").setExecutor(new CancelCommand(this));
        this.getCommand("originresetcooldown").setExecutor(new CooldownCheckCommand(this));

        // Load player data (cooldowns)
        loadPlayerData();

        getLogger().info("emMCHelper has been enabled and registered with Origins-Reborn!");
    }

    @Override
    public void onDisable() {
        // Save player data (cooldowns)
        savePlayerData();
        getLogger().info("emMCHelper has been disabled!");
    }

    // Add OriginsAddon required methods
    @Override
    public @NotNull String getNamespace() {
        return "emmchelper"; // Your unique addon namespace
    }

    @Override
    public @NotNull List<Ability> getRegisteredAbilities() {
        return List.of(
                new ToggleFlyAbility(this),
                new ToggleInvisibilityAbility(this)
        );
    }

    private void loadPluginConfig() {
        FileConfiguration config = getConfig();
        xpCost = config.getInt("xp-cost", 30);
        long cooldownSeconds = config.getLong("reset-cooldown-seconds", TimeUnit.DAYS.toSeconds(1)); // Default 1 day
        resetCooldownMillis = TimeUnit.SECONDS.toMillis(cooldownSeconds);
        originClearCommandTemplate = config.getString("origin-clear-command-template", "origin clear %player% origin");

        // You can add more messages here to load from config.yml if desired
        // e.g., MSG_COOLDOWN_ACTIVE = config.getString("messages.cooldown-active", "Default message...");
    }

    // --- Cooldown Management ---
    public boolean isOnCooldown(Player player) {
        return cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public long getRemainingCooldown(Player player) {
        if (!isOnCooldown(player)) {
            return 0;
        }
        return cooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
    }

    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + resetCooldownMillis);
        savePlayerData(); // Save immediately or batch saves
    }

    public void removeCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
        savePlayerData();
    }

    // --- Pending Reset Management ---
    public boolean hasPendingReset(Player player) {
        getLogger().info("Checking pending reset for: " + player.getName() + " (UUID: " + player.getUniqueId() + ")");
        boolean has = pendingResets.contains(player.getUniqueId());
        getLogger().info("Player " + player.getName() + " has pending reset: " + has + ". Set size: " + pendingResets.size() + ". Contents: " + pendingResets);
        return has;
    }

    public void addPendingReset(Player player) {
        getLogger().info("Attempting to add pending reset for: " + player.getName() + " (UUID: " + player.getUniqueId() + ")");
        boolean added = pendingResets.add(player.getUniqueId());
        getLogger().info("Player " + player.getName() + " added to pendingResets: " + added + ". Set size: " + pendingResets.size() + ". Contents: " + pendingResets);
    }

    public void removePendingReset(Player player) {
        getLogger().info("Attempting to remove pending reset for: " + player.getName() + " (UUID: " + player.getUniqueId() + ")");
        boolean removed = pendingResets.remove(player.getUniqueId());
        getLogger().info("Player " + player.getName() + " removed from pendingResets: " + removed + ". Set size: " + pendingResets.size() + ". Contents: " + pendingResets);
    }

    // --- XP Management ---
    public boolean hasEnoughXP(Player player) {
        return player.getLevel() >= xpCost;
    }

    public void deductXP(Player player) {
        if (hasEnoughXP(player)) {
            player.setLevel(player.getLevel() - xpCost);
        }
    }

    // --- Execute Origin Clear Command ---
    public void executeOriginClearCommand(Player player) {
        String commandToExecute = originClearCommandTemplate.replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
    }


    // --- Player Data Persistence (for cooldowns) ---
    public void reloadPlayerData() {
        if (playerDataFile == null) {
            playerDataFile = new File(getDataFolder(), "playerdata.yml");
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public FileConfiguration getPlayerData() {
        if (playerDataConfig == null) {
            reloadPlayerData();
        }
        return playerDataConfig;
    }

    public void savePlayerData() {
        if (playerDataConfig == null || playerDataFile == null) {
            return;
        }
        try {
            // Clear existing data in config before saving current map
            getPlayerData().set("cooldowns", null); // Clear the section

            for (Map.Entry<UUID, Long> entry : cooldowns.entrySet()) {
                getPlayerData().set("cooldowns." + entry.getKey().toString(), entry.getValue());
            }
            getPlayerData().save(playerDataFile);
        } catch (IOException ex) {
            getLogger().severe("Could not save player data to " + playerDataFile + ": " + ex.getMessage());
        }
    }

    public void loadPlayerData() {
        FileConfiguration data = getPlayerData();
        if (data.contains("cooldowns")) {
            data.getConfigurationSection("cooldowns").getKeys(false).forEach(uuidString -> {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    long endTime = data.getLong("cooldowns." + uuidString);
                    if (endTime > System.currentTimeMillis()) { // Only load active cooldowns
                        cooldowns.put(uuid, endTime);
                    }
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid UUID string in playerdata.yml: " + uuidString);
                }
            });
        }
        getLogger().info("Loaded " + cooldowns.size() + " active player cooldowns.");
    }
}
