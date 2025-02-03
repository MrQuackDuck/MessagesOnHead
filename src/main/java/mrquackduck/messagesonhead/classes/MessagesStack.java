package mrquackduck.messagesonhead.classes;

import mrquackduck.messagesonhead.utils.ColorUtils;
import mrquackduck.messagesonhead.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MessagesStack {
    private final Plugin plugin;
    private final Player player;
    private final List<Entity> entities = new ArrayList<>();
    private final static HashMap<UUID, MessagesStack> playersStacks = new HashMap<>();

    private final static String customEntityMetaTag = "MessagesOnHead";

    private MessagesStack(Player player, Plugin plugin) {
        Entity currentEntity = player;
        while (!currentEntity.getPassengers().isEmpty()) {
            var passengers = currentEntity.getPassengers();
            for (Entity passenger : passengers) {
                var passengerType = passenger.getType();
                if (passengerType == EntityType.AREA_EFFECT_CLOUD || passengerType == EntityType.TEXT_DISPLAY) {
                    currentEntity = passenger;
                    entities.add(passenger);
                }
                else break;
            }
        }

        this.player = player;
        this.plugin = plugin;
    }

    public static MessagesStack getMessagesStack(Player player, Plugin plugin) {
        var stack = playersStacks.get(player.getUniqueId());
        if (stack != null) return stack;

        stack = new MessagesStack(player, plugin);
        playersStacks.put(player.getUniqueId(), stack);
        return stack;
    }

    public static void resetPlayerMessageStack(Player player) {
        var playerData = playersStacks.get(player.getUniqueId());
        if (playerData != null) playerData.deleteAllRelatedEntities();
        playersStacks.remove(player.getUniqueId());
    }

    private void deleteAllRelatedEntities() {
        for (Entity entity : entities) entity.remove();
    }

    public static void cleanUp(Plugin plugin) {
        playersStacks.clear();
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.hasMetadata(customEntityMetaTag)) {
                    entity.remove();
                }
            }
        }
    }

    public void addMessage(String text) {
        var secondsToExist = calculateTimeForMessageToExist(text);

        List<String> lines = StringUtils.splitTextIntoLines(text, plugin.getConfig().getInt("symbolsPerLine"), plugin.getConfig().getInt("symbolsLimit"));
        Collections.reverse(lines); // Reverse to stack from bottom to top

        Entity currentEntityToSitOn = getEntityToSitOn();
        List<Entity> newEntities = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            boolean isLastLine = (i == 0); // Since lines are reversed, first index is actually the last line

            int middleEntitiesToSpawn = 1;
            if (currentEntityToSitOn.getType() == EntityType.PLAYER) middleEntitiesToSpawn = 2;
            if (currentEntityToSitOn.getType() == EntityType.PLAYER && !plugin.getConfig().getBoolean("lowerMode")) middleEntitiesToSpawn = 3;

            final var middleEntities = spawnMiddleEntities(middleEntitiesToSpawn);
            final var textDisplay = spawnTextDisplay(player.getLocation(), line, secondsToExist, isLastLine);
            middleEntities.get(middleEntities.size() - 1).addPassenger(textDisplay);
            currentEntityToSitOn.addPassenger(middleEntities.get(0));

            newEntities.addAll(middleEntities);
            newEntities.add(textDisplay);

            currentEntityToSitOn = textDisplay;
        }

        entities.addAll(newEntities);

        // Delay removal by 2 ticks to ensure timer reaches 0.0
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < lines.size(); i++) {
                    removeOldestMessage();
                }
            }
        }.runTaskLater(plugin, Math.round(secondsToExist * 20) + 2);
    }

    public void removeOldestMessage() {
        var entitiesToRemove = new ArrayList<Entity>();

        int index;
        for (index = 0; index < entities.size(); index++) {
            Entity entity = entities.get(index);
            entitiesToRemove.add(entity);
            if (entity.getType() == EntityType.TEXT_DISPLAY) break;
        }

        for (Entity entity : entitiesToRemove) {
            entity.remove();
            entities.remove(entity);
        }

        Entity entityToTransfer;

        try { entityToTransfer = entities.get(0); }
        catch (IndexOutOfBoundsException ex) { return; }

        var lowerMode = plugin.getConfig().getBoolean("lowerMode");
        var middleEntities = spawnMiddleEntities(lowerMode ? 1 : 2);
        entities.addAll(0, middleEntities);
        player.addPassenger(middleEntities.get(0));
        middleEntities.get(middleEntities.size() - 1).addPassenger(entityToTransfer);
    }

    private TextDisplay spawnTextDisplay(Location location, String text, double secondsToExist, boolean showTimer) {
        var config = plugin.getConfig();
        var textColor = config.getString("textColor");
        var backgroundColor = config.getString("backgroundColor");
        var backgroundTransparencyPercentage = config.getInt("backgroundTransparencyPercentage");
        var backgroundEnabled = config.getBoolean("backgroundEnabled");
        var timerEnabled = config.getBoolean("timerEnabled");
        var timerColor = config.getString("timerColor");
        if (showTimer && !timerEnabled) showTimer = false;
        var isShadowed = config.getBoolean("isShadowed");
        location.setY(255); // Setting a high initial Y for the message to appear from top

        assert textColor != null;
        assert timerColor != null;

        final var textDisplay = (TextDisplay) Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.TEXT_DISPLAY);
        textDisplay.setBillboard(Display.Billboard.VERTICAL);
        textDisplay.setRotation(location.getYaw(), 0);
        textDisplay.setDefaultBackground(!backgroundEnabled);
        if (backgroundEnabled) textDisplay.setBackgroundColor(Color.fromARGB(ColorUtils.hexToARGB(backgroundColor, backgroundTransparencyPercentage)));
        textDisplay.setShadowed(isShadowed);
        textDisplay.setLineWidth(Integer.MAX_VALUE);
        textDisplay.setMetadata(customEntityMetaTag, new FixedMetadataValue(plugin, ((TextComponent)player.displayName()).content()));

        if (showTimer) {
            new BukkitRunnable() {
                double timeLeft = secondsToExist;

                @Override
                public void run() {
                    // Allow for one extra tick to show 0.0
                    if (textDisplay.isDead() || timeLeft < -0.1) {
                        this.cancel();
                        return;
                    }

                    var timerFormat = config.getString("timerFormat");
                    assert timerFormat != null;
                    String timerText = String.format(timerFormat, Math.max(0.0, timeLeft)); // Ensure we don't show negative numbers
                    textDisplay.text(Component.text(text).color(TextColor.fromHexString(textColor))
                                    .append(Component.text(timerText).color(TextColor.fromHexString(timerColor))));

                    timeLeft -= 0.1;
                }
            }.runTaskTimer(plugin, 1, 2);
        } else {
            textDisplay.text(Component.text(text).color(TextColor.fromHexString(textColor)));
        }

        return textDisplay;
    }

    private List<Entity> spawnMiddleEntities(int count) {
        var middleEntities = new ArrayList<Entity>();
        var location = player.getLocation();
        location.setY(255); // Setting a high initial Y for the message to appear from top

        Entity previousEntity = null;
        for (int i = 0; i < count; i++) {
            var entity = Objects.requireNonNull(location.getWorld()).spawn(location, AreaEffectCloud.class);
            entity.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData());
            entity.setInvulnerable(true);
            entity.setGravity(false);
            // Adding metadata in order to distinguish from regular entities and be able to make cleanup
            entity.setMetadata(customEntityMetaTag, new FixedMetadataValue(plugin, ((TextComponent)player.displayName()).content()));
            middleEntities.add(entity);
            if (previousEntity != null) previousEntity.addPassenger(entity);
            previousEntity = entity;
        }

        return middleEntities;
    }

    private double calculateTimeForMessageToExist(String message) {
        double initialTime = plugin.getConfig().getLong("timeToExist");
        var scalingEnabled = plugin.getConfig().getBoolean("scalingEnabled");
        if (scalingEnabled) {
            var scalingCoefficient = plugin.getConfig().getDouble("scalingCoefficient");
            initialTime += (scalingCoefficient * message.length());
        }

        return initialTime;
    }

    // Gets the latest entity in the stack that new entity can be sat on
    private Entity getEntityToSitOn() {
        if (entities.isEmpty()) return player;
        else return entities.get(entities.size() - 1);
    }
}