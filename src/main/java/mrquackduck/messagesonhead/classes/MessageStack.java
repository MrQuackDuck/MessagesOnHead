package mrquackduck.messagesonhead.classes;

import me.clip.placeholderapi.PlaceholderAPI;
import mrquackduck.messagesonhead.configuration.Configuration;
import mrquackduck.messagesonhead.utils.ColorUtils;
import mrquackduck.messagesonhead.utils.EntityUtils;
import mrquackduck.messagesonhead.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Represents a stack of displayed messages above player's head
 */
public class MessageStack {
    private final JavaPlugin plugin;
    private final Configuration config;
    private final Player player;
    private final List<Entity> entities = new ArrayList<>();
    private final List<DisplayedMessage> displayedMessages = new ArrayList<>();
    public static final String customEntityTag = "moh-entity";

    public MessageStack(Player player, JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = new Configuration(plugin);
        this.player = player;
        findExistingStackEntities();
    }

    private void findExistingStackEntities() {
        Entity currentEntity = player;
        while (!currentEntity.getPassengers().isEmpty()) {
            var passengers = currentEntity.getPassengers();
            var needToBreak = true;
            for (Entity passenger : passengers) {
                if (EntityUtils.hasScoreboardTagCaseInvariant(passenger, customEntityTag)) {
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
        if (text.isEmpty()) return;
        var secondsToExist = calculateTimeForMessageToExist(text);
        var minSymbolsForTimer = config.minSymbolsForTimer();

        List<String> lines = StringUtils.splitTextIntoLines(text, config.symbolsPerLine(), config.symbolsLimit());
        Collections.reverse(lines); // Reverse the stack from bottom to top

        Entity currentEntityToSitOn = getEntityToSitOn();
        List<Entity> newEntities = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            boolean isLastLine = (i == 0); // Since lines are reversed, first index points on the last line
            boolean needToShowTimer = isLastLine && text.length() >= minSymbolsForTimer;

            var middleEntityHeight = config.gapBetweenMessages();
            if (currentEntityToSitOn.getType() == EntityType.PLAYER) {
                middleEntityHeight = config.gapAboveHead();
            }

            final var middleEntity = spawnMiddleEntity(middleEntityHeight);
            final var textDisplay = spawnTextDisplay(player.getLocation(), line, secondsToExist, needToShowTimer);
            middleEntity.addPassenger(textDisplay);
            currentEntityToSitOn.addPassenger(middleEntity);

            newEntities.add(middleEntity);
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

        var nextEntity = nextDisplayedMessage.entities.get(0);
        if (nextEntity.getType() == EntityType.INTERACTION) {
            ((Interaction)nextEntity).setInteractionHeight(config.gapAboveHead());
        }

        player.addPassenger(nextEntity);
    }

    private TextDisplay spawnTextDisplay(Location location, String text, double secondsToExist, boolean showTimer) {
        if (showTimer && !config.isTimerEnabled()) showTimer = false;
        location.setY(location.y() + 50); // Setting higher Y coordinate to prevent the message appearing from bottom

        final var textDisplay = (TextDisplay) Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.TEXT_DISPLAY);
        if (config.isBackgroundEnabled()) textDisplay.setBackgroundColor(Color.fromARGB(ColorUtils.hexToARGB(config.backgroundColor(),config.backgroundTransparencyPercentage())));
        textDisplay.setDefaultBackground(!config.isBackgroundEnabled());
        textDisplay.setBillboard(config.pivotAxis());
        textDisplay.setRotation(location.getYaw(), 0);
        textDisplay.setShadowed(config.isShadowed());
        textDisplay.setLineWidth(Integer.MAX_VALUE);
        textDisplay.addScoreboardTag(customEntityTag);

        var textToBeDisplayed = Component.text(text).color(TextColor.fromHexString(config.textColor()));
        if (config.isPlaceholderApiIntegrationEnabled()) {
            text = config.lineFormat()
                    .replace("[defaultColor]", config.textColor())
                    .replace("[colorPlaceholder]", config.colorPlaceholder())
                    .replace("[message]", text);

            text = applyColorPlaceholders(text);
            textToBeDisplayed = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }

        if (showTimer) showTextDisplayWithTimer(textDisplay, textToBeDisplayed, secondsToExist);
        else textDisplay.text(textToBeDisplayed);

        return textDisplay;
    }

    private void showTextDisplayWithTimer(TextDisplay textDisplay, TextComponent textToBeDisplayed, double secondsToExist) {
        new BukkitRunnable() {
            double timeLeft = secondsToExist;

            @Override
            public void run() {
                // Allow for one extra tick to show 0.0
                if (textDisplay.isDead() || timeLeft < -0.1) {
                    this.cancel();
                    return;
                }

                var timerFormat = config.timerFormat();
                assert timerFormat != null;
                String timerText = String.format(timerFormat, Math.max(0.0, timeLeft)); // Ensure we don't show negative numbers
                textDisplay.text(textToBeDisplayed.append(Component.text(timerText).color(TextColor.fromHexString(config.timerColor()))));

                timeLeft -= 0.1;
            }
        }.runTaskTimer(plugin, 1, 2);
    }

    private String applyColorPlaceholders(String text) {
        String resolvedColorCode = PlaceholderAPI.setPlaceholders(player, config.colorPlaceholder());
        if (resolvedColorCode.isEmpty()) {
            // Remove color code if placeholder returns empty
            text = text.replace('&' + config.colorPlaceholder(), "");
        }
        else {
            // Replace placeholder with actual color code
            text = text.replace(config.colorPlaceholder(), resolvedColorCode);
        }

        text = text.replace("&&", "&");
        return text;
    }

    private Entity spawnMiddleEntity(float height) {
        var location = player.getLocation();
        location.setY(location.y() + 50); // Setting higher Y coordinate to prevent the message appearing from bottom

        var entity = Objects.requireNonNull(location.getWorld()).spawn(location, Interaction.class);
        entity.setInteractionWidth(0);
        entity.setInteractionHeight(height);
        entity.setInvulnerable(true);
        entity.setGravity(false);
        // Adding a scoreboard tag in order to distinguish from regular entity and be able to make the cleanup
        entity.addScoreboardTag(customEntityTag);

        return entity;
    }

    private double calculateTimeForMessageToExist(String message) {
        double initialTime = config.timeToExist();
        if (config.isScalingEnabled()) {
            var scalingCoefficient = config.scalingCoefficient();
            initialTime += (scalingCoefficient * message.length());
        }

        return initialTime;
    }

    // Gets the latest entity in the stack that a new entity can sit on
    private Entity getEntityToSitOn() {
        if (entities.isEmpty()) return player;
        else return entities.get(entities.size() - 1);
    }
}