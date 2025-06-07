package mrquackduck.messagesonhead.classes;

import mrquackduck.messagesonhead.utils.ColorUtils;
import mrquackduck.messagesonhead.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Represents a stack of displayed messages on top of player's head
 */
public class MessageStack {
    private final Plugin plugin;
    private final Player player;
    private final List<Entity> entities = new ArrayList<>();
    private final List<DisplayedMessage> displayedMessages = new ArrayList<>();
    public static final String customEntityTag = "moh-entity";

    public MessageStack(Player player, Plugin plugin) {
        this.player = player;
        this.plugin = plugin;
        findExistingStackEntities();
    }

    private void findExistingStackEntities() {
        Entity currentEntity = player;
        while (!currentEntity.getPassengers().isEmpty()) {
            var passengers = currentEntity.getPassengers();
            var needToBreak = true;
            for (Entity passenger : passengers) {
                var passengerType = passenger.getType();
                if (passengerType == EntityType.AREA_EFFECT_CLOUD || passengerType == EntityType.TEXT_DISPLAY) {
                    currentEntity = passenger;
                    entities.add(passenger);
                    needToBreak = false;
                }
            }

            if (needToBreak) break;
        }
    }

    public void deleteAllRelatedEntities() {
        for (Entity entity : entities) entity.remove();
    }

    public void pushMessage(String text) {
        var secondsToExist = calculateTimeForMessageToExist(text);
        var minSymbolsForTimer = plugin.getConfig().getInt("minSymbolsForTimer");

        List<String> lines = StringUtils.splitTextIntoLines(text, plugin.getConfig().getInt("symbolsPerLine"), plugin.getConfig().getInt("symbolsLimit"));
        Collections.reverse(lines); // Reverse to stack from bottom to top

        Entity currentEntityToSitOn = getEntityToSitOn();
        List<Entity> newEntities = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            boolean isLastLine = (i == 0); // Since lines are reversed, first index is actually the last line
            boolean needToShowTimer = isLastLine && text.length() >= minSymbolsForTimer;

            int middleEntitiesToSpawn = 1;
            if (currentEntityToSitOn.getType() == EntityType.PLAYER) middleEntitiesToSpawn = 2;
            if (currentEntityToSitOn.getType() == EntityType.PLAYER && !plugin.getConfig().getBoolean("lowerMode")) middleEntitiesToSpawn = 3;

            final var middleEntities = spawnMiddleEntities(middleEntitiesToSpawn);
            final var textDisplay = spawnTextDisplay(player.getLocation(), line, secondsToExist, needToShowTimer);
            middleEntities.get(middleEntities.size() - 1).addPassenger(textDisplay);
            currentEntityToSitOn.addPassenger(middleEntities.get(0));

            newEntities.addAll(middleEntities);
            newEntities.add(textDisplay);

            currentEntityToSitOn = textDisplay;
        }

        DisplayedMessage newDisplayedMessage = new DisplayedMessage(newEntities);
        displayedMessages.add(newDisplayedMessage);
        entities.addAll(newEntities);

        new BukkitRunnable() {
            @Override
            public void run() {
                removeDisplayedMessage(newDisplayedMessage);
            }
        }.runTaskLater(plugin, Math.round(secondsToExist * 20) + 2);
    }

    private void removeDisplayedMessage(DisplayedMessage displayedMessage) {
        if (!displayedMessages.contains(displayedMessage)) return;
        var currentDisplayedMessageIndex = displayedMessages.indexOf(displayedMessage);

        // Remove all entities in the displayed message
        for (Entity entity : displayedMessage.entities) {
            entity.remove();
            entities.remove(entity);
        }

        displayedMessages.remove(displayedMessage);

        // Return if there are no entities left
        if (entities.isEmpty()) return;

        // The current displayedMessage index turns into the next displayedMessage index because the current displayedMessage was deleted from the list
        var nextDisplayedMessageIndex = currentDisplayedMessageIndex;
        if (nextDisplayedMessageIndex >= displayedMessages.size()) return; // Return if no further displayed messages left
        var nextDisplayedMessage = displayedMessages.get(nextDisplayedMessageIndex);

        // Determining the message displayedMessage standing before in the list
        var prevDisplayedMessageIndex = nextDisplayedMessageIndex - 1;
        if (prevDisplayedMessageIndex >= 0) {
            // If there's a potential displayedMessage, transfer the message there
            var prevDisplayedMessage = displayedMessages.get(prevDisplayedMessageIndex);
            prevDisplayedMessage.entities.get(prevDisplayedMessage.entities.size() - 1).addPassenger(nextDisplayedMessage.entities.get(0));
            return;
        }

        var middleEntities = spawnMiddleEntities(plugin.getConfig().getBoolean("lowerMode") ? 1 : 2);
        middleEntities.get(middleEntities.size() - 1).addPassenger(nextDisplayedMessage.entities.get(0));
        nextDisplayedMessage.entities.addAll(0, middleEntities);

        player.addPassenger(middleEntities.get(0));
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
        location.setY(255); // Setting high Y coordinate to prevent the message appearing from bottom

        assert textColor != null;
        assert timerColor != null;

        final var textDisplay = (TextDisplay) Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.TEXT_DISPLAY);
        textDisplay.setBillboard(Display.Billboard.VERTICAL);
        textDisplay.setRotation(location.getYaw(), 0);
        textDisplay.setDefaultBackground(!backgroundEnabled);
        if (backgroundEnabled) {
            assert backgroundColor != null;
            textDisplay.setBackgroundColor(Color.fromARGB(ColorUtils.hexToARGB(backgroundColor, backgroundTransparencyPercentage)));
        }
        textDisplay.setShadowed(isShadowed);
        textDisplay.setLineWidth(Integer.MAX_VALUE);
        textDisplay.addScoreboardTag(customEntityTag);

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
        location.setY(255); // Setting high Y coordinate to prevent the message appearing from bottom

        Entity previousEntity = null;
        for (int i = 0; i < count; i++) {
            var entity = Objects.requireNonNull(location.getWorld()).spawn(location, AreaEffectCloud.class);
            entity.setParticle(Particle.BLOCK_CRACK, Material.AIR.createBlockData());
            entity.setRadius(0);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            // Adding a scoreboard tag in order to distinguish from regular entity and be able to make the cleanup
            entity.addScoreboardTag(customEntityTag);
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