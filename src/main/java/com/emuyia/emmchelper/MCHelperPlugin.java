package com.emuyia.emmchelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor; // Added import
import org.bukkit.configuration.file.FileConfiguration; // Added import
import org.bukkit.configuration.file.YamlConfiguration; // Added import
import org.bukkit.entity.Player; // Added import
import org.bukkit.event.EventHandler; // + Add
import org.bukkit.event.EventPriority;    // + Add
import org.bukkit.event.Listener; // + Add
import org.bukkit.event.inventory.InventoryClickEvent; // + Add
import org.jetbrains.annotations.NotNull; // + Add

import com.emuyia.emmchelper.abilities.AerialAffinityAbility; // + Add
import com.emuyia.emmchelper.abilities.AerialExhaustionAbility; // + Add this import
import com.emuyia.emmchelper.abilities.BetterIronArmour; // + Add this import
import com.emuyia.emmchelper.abilities.BetterIronWeapons;
import com.emuyia.emmchelper.abilities.EmeraldDiamondConverterAbility;
import com.emuyia.emmchelper.abilities.GoldCopperConverterAbility;
import com.emuyia.emmchelper.abilities.IronCoalConverterAbility;
import com.emuyia.emmchelper.abilities.LapisRedstoneConverterAbility; // + Import existing ability
import com.emuyia.emmchelper.abilities.MaxHealthEightHeartsAbility;
import com.emuyia.emmchelper.abilities.MaxHealthFiveHeartsAbility;
import com.emuyia.emmchelper.abilities.MaxHealthFourHeartsAbility;
import com.emuyia.emmchelper.abilities.MaxHealthNineHeartsAbility;
import com.emuyia.emmchelper.abilities.MaxHealthOneHeartAbility;
import com.emuyia.emmchelper.abilities.MaxHealthSevenHeartsAbility; // + Import the new Sculk Vein ability
import com.emuyia.emmchelper.abilities.MaxHealthSixHeartsAbility;
import com.emuyia.emmchelper.abilities.MaxHealthThreeHeartsAbility;
import com.emuyia.emmchelper.abilities.MaxHealthTwoHeartsAbility; // + Add new import
import com.emuyia.emmchelper.abilities.NoInventoryWhileFlyingAbility;
import com.emuyia.emmchelper.abilities.PlaceGlowLichenAbility; // + Import GoldCopperConverterAbility
import com.emuyia.emmchelper.abilities.PlacePoppyAbility;   // + Import IronCoalConverterAbility
import com.emuyia.emmchelper.abilities.PlaceSculkVeinAbility; // + Import BetterIronArmour
import com.emuyia.emmchelper.abilities.ToggleFlyAbility; // + Import BetterIronWeapons
import com.emuyia.emmchelper.abilities.ToggleInvisibilityAbility; // + Import PlacePoppyAbility
import com.emuyia.emmchelper.commands.CancelCommand;
import com.emuyia.emmchelper.commands.ConfirmCommand;
import com.emuyia.emmchelper.commands.CooldownCheckCommand;
import com.emuyia.emmchelper.commands.RequestCommand; // + Import new ability
import com.starshootercity.OriginsAddon; // + Import new ability
import com.starshootercity.abilities.types.Ability;
import com.starshootercity.util.config.ConfigManager;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MCHelperPlugin extends OriginsAddon implements Listener { // Ensure Listener is implemented if you kept the test handlers

    // Enum to represent the type of reset a player is confirming
    public enum PendingResetType {
        FREE_RESET, // Resetting for free after cooldown
        PAID_RESET_ON_COOLDOWN // Resetting for XP while still on cooldown
    }

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, PendingResetType> pendingResetTypes = new HashMap<>(); // Changed from Set<UUID> pendingResets

    private FileConfiguration playerDataConfig = null;
    private File playerDataFile = null;

    public int xpCost;
    public long resetCooldownMillis;
    public String originClearCommandTemplate;

    // Prefix for OriginReset commands (using ChatColor for existing command structure)
    public static final String ORIGIN_RESET_MSG_PREFIX = ChatColor.GOLD + "[OriginReset] " + ChatColor.RESET;
    public static final String MSG_NO_PERMISSION = ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "You don't have permission to use this command.";
    public static final String MSG_PLAYER_ONLY = ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "This command can only be run by a player.";

    // Prefix for new Ability messages (using Adventure Component)
    public static final Component ABILITY_MSG_PREFIX = Component.text("[").color(NamedTextColor.GOLD)
            .append(Component.text("MCHelper").color(NamedTextColor.YELLOW))
            .append(Component.text("] ").color(NamedTextColor.GOLD));

    private ToggleFlyAbility toggleFlyAbility;
    private ToggleInvisibilityAbility toggleInvisibilityAbility;
    private NoInventoryWhileFlyingAbility noInventoryWhileFlyingAbility;
    private AerialAffinityAbility aerialAffinityAbility; // + Declare new ability instance
    private AerialExhaustionAbility aerialExhaustionAbility; // + Declare new ability instance
    private PlaceGlowLichenAbility placeGlowLichenAbility; // + Declare new ability instance
    private PlaceSculkVeinAbility placeSculkVeinAbility; // + Declare field for the new Sculk Vein ability
    private EmeraldDiamondConverterAbility emeraldDiamondConverterAbility; // + Declare new ability
    private LapisRedstoneConverterAbility lapisRedstoneConverterAbility; // + Declare LapisRedstoneConverterAbility
    private GoldCopperConverterAbility goldCopperConverterAbility; // + Declare GoldCopperConverterAbility
    private IronCoalConverterAbility ironCoalConverterAbility;     // + Declare IronCoalConverterAbility
    private BetterIronArmour betterIronArmourAbility; // + Declare BetterIronArmour
    private BetterIronWeapons betterIronWeaponsAbility; // + Declare BetterIronWeapons
    private PlacePoppyAbility placePoppyAbility; // + Declare PlacePoppyAbility

    // + Declare fields for new health abilities
    private MaxHealthOneHeartAbility maxHealthOneHeartAbility;
    private MaxHealthTwoHeartsAbility maxHealthTwoHeartsAbility;
    private MaxHealthThreeHeartsAbility maxHealthThreeHeartsAbility;
    private MaxHealthFourHeartsAbility maxHealthFourHeartsAbility;
    private MaxHealthFiveHeartsAbility maxHealthFiveHeartsAbility;
    private MaxHealthSixHeartsAbility maxHealthSixHeartsAbility;
    private MaxHealthSevenHeartsAbility maxHealthSevenHeartsAbility;
    private MaxHealthEightHeartsAbility maxHealthEightHeartsAbility;
    private MaxHealthNineHeartsAbility maxHealthNineHeartsAbility;

    @Override
    public void onRegister() {
        getLogger().info("MCHelperPlugin registering...");

        // Load configuration first
        loadPluginConfig();
        saveDefaultConfig();
        reloadPlayerData();
        loadPlayerData();

        // Instantiate ToggleFlyAbility first
        this.toggleFlyAbility = new ToggleFlyAbility(this);
        // Correct the constructor call here by passing 'this'
        this.toggleInvisibilityAbility = new ToggleInvisibilityAbility(this);

        // Pass the instance to the dependent abilities' constructors
        this.noInventoryWhileFlyingAbility = new NoInventoryWhileFlyingAbility(this, this.toggleFlyAbility);
        this.aerialAffinityAbility = new AerialAffinityAbility(this, this.toggleFlyAbility);
        this.aerialExhaustionAbility = new AerialExhaustionAbility(this, this.toggleFlyAbility);

        this.placeGlowLichenAbility = new PlaceGlowLichenAbility(this);
        getServer().getPluginManager().registerEvents(this.placeGlowLichenAbility, this);

        this.placeSculkVeinAbility = new PlaceSculkVeinAbility(this);
        getServer().getPluginManager().registerEvents(this.placeSculkVeinAbility, this);

        this.emeraldDiamondConverterAbility = new EmeraldDiamondConverterAbility();
        getServer().getPluginManager().registerEvents(this.emeraldDiamondConverterAbility, this);

        this.lapisRedstoneConverterAbility = new LapisRedstoneConverterAbility();
        getServer().getPluginManager().registerEvents(this.lapisRedstoneConverterAbility, this);

        this.goldCopperConverterAbility = new GoldCopperConverterAbility();
        getServer().getPluginManager().registerEvents(this.goldCopperConverterAbility, this);

        this.ironCoalConverterAbility = new IronCoalConverterAbility();
        getServer().getPluginManager().registerEvents(this.ironCoalConverterAbility, this);

        this.betterIronArmourAbility = new BetterIronArmour();
        getServer().getPluginManager().registerEvents(this.betterIronArmourAbility, this);

        this.betterIronWeaponsAbility = new BetterIronWeapons(this);
        getServer().getPluginManager().registerEvents(this.betterIronWeaponsAbility, this);

        this.placePoppyAbility = new PlacePoppyAbility(this);
        getServer().getPluginManager().registerEvents(this.placePoppyAbility, this);

        // Instantiate ALL Max Health abilities
        getLogger().info("[MCHelperDEBUG] Attempting to instantiate Max Health abilities...");
        this.maxHealthOneHeartAbility = new MaxHealthOneHeartAbility(this);
        this.maxHealthTwoHeartsAbility = new MaxHealthTwoHeartsAbility(this);
        this.maxHealthThreeHeartsAbility = new MaxHealthThreeHeartsAbility(this);
        this.maxHealthFourHeartsAbility = new MaxHealthFourHeartsAbility(this);
        this.maxHealthFiveHeartsAbility = new MaxHealthFiveHeartsAbility(this);
        this.maxHealthSixHeartsAbility = new MaxHealthSixHeartsAbility(this);
        this.maxHealthSevenHeartsAbility = new MaxHealthSevenHeartsAbility(this);
        this.maxHealthEightHeartsAbility = new MaxHealthEightHeartsAbility(this);
        this.maxHealthNineHeartsAbility = new MaxHealthNineHeartsAbility(this);
        getLogger().info("[MCHelperDEBUG] Max Health abilities instantiation complete.");

        // Register commands using the names from plugin.yml
        getCommand("requestoriginreset").setExecutor(new RequestCommand(this));
        getCommand("confirmoriginreset").setExecutor(new ConfirmCommand(this));
        getCommand("canceloriginreset").setExecutor(new CancelCommand(this));
        getCommand("originresetcooldown").setExecutor(new CooldownCheckCommand(this));

        // Register this plugin's listener if it has any @EventHandlers
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("emMCHelper has been enabled and registered with Origins-Reborn!");
    }

    // Removed duplicate onEnable method

    @Override
    public void onDisable() { // Kept the first onDisable, removed the duplicate
        getLogger().info("emMCHelper disabling...");
        if (aerialAffinityAbility != null) {
            aerialAffinityAbility.cancelTask(); // Corrected method name
        }
        if (aerialExhaustionAbility != null) {
            aerialExhaustionAbility.cancelTask(); // Corrected method name
        }
        savePlayerData();
        getLogger().info("emMCHelper has been disabled.");
    }

    @Override
    public @NotNull String getNamespace() { // Kept the first, removed duplicate
        return "emmchelper";
    }

    @Override
    public @NotNull List<Ability> getRegisteredAbilities() { // Kept the first, removed duplicate
        // Make sure to return all abilities that should be recognized by Origins.
        return List.of(
                toggleFlyAbility,
                toggleInvisibilityAbility,
                noInventoryWhileFlyingAbility,
                aerialAffinityAbility,
                aerialExhaustionAbility,
                placeGlowLichenAbility,
                placeSculkVeinAbility, // + Add the new Sculk Vein ability to the list
                emeraldDiamondConverterAbility, // + Add EmeraldIronConverterAbility to the list
                lapisRedstoneConverterAbility, // + Add LapisRedstoneConverterAbility to the list
                goldCopperConverterAbility, // + Add GoldCopperConverterAbility to the list
                ironCoalConverterAbility,   // + Add IronCoalConverterAbility to the list
                betterIronArmourAbility,   // + Add BetterIronArmourAbility
                betterIronWeaponsAbility,  // + Add BetterIronWeaponsAbility
                placePoppyAbility, // + Add PlacePoppyAbility to the list
                // + Add new health abilities to the list
                maxHealthOneHeartAbility,
                maxHealthTwoHeartsAbility,
                maxHealthThreeHeartsAbility,
                maxHealthFourHeartsAbility,
                maxHealthFiveHeartsAbility,
                maxHealthSixHeartsAbility,
                maxHealthSevenHeartsAbility,
                maxHealthEightHeartsAbility,
                maxHealthNineHeartsAbility
        );
    }

    private void loadPluginConfig() {
        FileConfiguration config = getConfig();
        xpCost = config.getInt("xp-cost", 30);
        long cooldownSeconds = config.getLong("reset-cooldown-seconds", TimeUnit.DAYS.toSeconds(7));
        resetCooldownMillis = TimeUnit.SECONDS.toMillis(cooldownSeconds);
        originClearCommandTemplate = config.getString("origin-clear-command-template", "origin clear %player% origin");
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
        savePlayerData();
    }

    public void removeCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
        savePlayerData();
    }

    // --- Pending Reset Management ---
    public boolean hasPendingReset(Player player) {
        return pendingResetTypes.containsKey(player.getUniqueId());
    }

    public void addPendingReset(Player player, PendingResetType type) {
        pendingResetTypes.put(player.getUniqueId(), type);
    }

    public PendingResetType getPendingResetType(Player player) {
        return pendingResetTypes.get(player.getUniqueId());
    }

    public void removePendingReset(Player player) {
        pendingResetTypes.remove(player.getUniqueId());
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
            getPlayerData().set("cooldowns", null);
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
                    if (endTime > System.currentTimeMillis()) {
                        cooldowns.put(uuid, endTime);
                    }
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid UUID string in playerdata.yml: " + uuidString);
                }
            });
        }
        getLogger().info("Loaded " + cooldowns.size() + " active player cooldowns.");
    }

    // --- Config Helper Methods for Abilities (Now Generic) ---
    public <T> void registerAbilityConfigOption(Key abilityKey, String path, List<String> description, ConfigManager.SettingType<T> type, T defaultValue) {
        String fullPath = "abilities." + abilityKey.value() + "." + path;
        FileConfiguration config = getConfig();
        if (!config.isSet(fullPath)) {
            config.set(fullPath, defaultValue); // Bukkit's config should handle various types for T
        }
        // Consider calling saveConfig() here or in onDisable/onRegister if defaults are set.
    }

    @SuppressWarnings("unchecked") // Suppress warning for the cast from Object
    public <T> T getAbilityConfigOption(Key abilityKey, String path, ConfigManager.SettingType<T> type, T codeDefaultValue) {
        String fullPath = "abilities." + abilityKey.value() + "." + path;
        FileConfiguration config = getConfig();
        Object value = config.get(fullPath);

        if (value == null) {
            return codeDefaultValue;
        }
        try {
            // This cast assumes that the type T stored in the config is directly castable.
            // For more complex types or if SettingType<T> implies conversion, this might need more logic.
            return (T) value;
        } catch (ClassCastException e) {
            getLogger().warning("Config value at " + fullPath + " is of wrong type. Expected compatible with " +
                                (codeDefaultValue != null ? codeDefaultValue.getClass().getName() : "provided type T") +
                                ", got " + value.getClass().getName() + ". Returning default.");
            return codeDefaultValue;
        }
    }

    // saveDefaultConfig() is called in onRegister.
    // The custom onReload method was removed as it wasn't overriding anything from OriginsAddon
    // and super.onReload() was causing an error. If reload functionality is needed,
    // it should be implemented based on how OriginsAddon handles reloads or via Bukkit events.

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnyInventoryClickForTest(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            //getLogger().info("[MCHelperTEST_CLICK] InventoryClickEvent detected in MCHelperPlugin for " + player.getName() + ". Slot: " + event.getSlot());
        }
    }
}
