package mrquackduck.messagesonhead;

import com.tchristofferson.configupdater.ConfigUpdater;
import mrquackduck.messagesonhead.classes.MessageStackRepository;
import mrquackduck.messagesonhead.commands.MohCommand;
import mrquackduck.messagesonhead.listeners.LeaveListener;
import mrquackduck.messagesonhead.listeners.SendMessageListener;
import mrquackduck.messagesonhead.utils.MessageColorizer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MessagesOnHeadPlugin extends JavaPlugin {
    private Logger logger;
    private MessageStackRepository messageStackRepository;
    private static Map<String, String> messages = new HashMap<>();

    @Override
    public void onEnable() {
        // Setting up a logger
        logger = getLogger();

        // Setup message stack repository
        this.messageStackRepository = new MessageStackRepository(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new SendMessageListener(messageStackRepository), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(messageStackRepository), this);

        // Starting the plugin
        try { start(); }
        catch (RuntimeException e) { getLogger().log(Level.SEVERE, e.getMessage()); }

        // Registering commands
        Objects.requireNonNull(getServer().getPluginCommand("messagesonhead")).setExecutor(new MohCommand(this, messageStackRepository));
    }

    private void start() {
        // Save default configuration
        saveDefaultConfig();

        // Updating the config with missing key-pairs (and removing redundant ones if present)
        File configFile = new File(getDataFolder(), "config.yml");
        try { ConfigUpdater.update(this, "config.yml", configFile, new ArrayList<>()); }
        catch (IOException e) { e.printStackTrace(); }

        // Reloading the messages from config
        setupMessages();

        // Remove all entities related to the plugin
        messageStackRepository.cleanUp();
    }

    public void reload() {
        // Reloading the config
        reloadConfig();

        // Starting the plugin again
        start();

        logger.info("Plugin restarted!");
    }

    private void setupMessages() {
        messages = new HashMap<>();

        // Getting the messages from the config
        ConfigurationSection configSection = getConfig().getConfigurationSection("messages");
        if (configSection != null) {
            // Adding these messages to dictionary
            Map<String, Object> messages = configSection.getValues(true);
            for (Map.Entry<String, Object> pair : messages.entrySet()) {
                MessagesOnHeadPlugin.messages.put(pair.getKey(), pair.getValue().toString());
            }
        }

        saveDefaultConfig();
    }

    // Returns a message from the config by key
    public static String getMessage(String key) {
        if (messages == null) return String.format("Message %s wasn't found (messages list is null)", key);
        if (messages.get(key) == null) return String.format("Message %s wasn't found", key);

        return MessageColorizer.colorize(messages.get(key).replace("<prefix>", messages.get("prefix")));
    }
}
