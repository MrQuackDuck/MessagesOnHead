package mrquackduck.messagesonhead.listeners;

import io.papermc.paper.event.player.ChatEvent;
import mrquackduck.messagesonhead.MessagesOnHeadPlugin;
import mrquackduck.messagesonhead.configuration.Configuration;
import mrquackduck.messagesonhead.services.MessageStackRepository;
import mrquackduck.messagesonhead.configuration.Permissions;
import mrquackduck.messagesonhead.utils.ColorUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

public class SendMessageListener implements Listener {
    private final MessageStackRepository messageStackRepository;
    private final MessagesOnHeadPlugin plugin;

    public SendMessageListener(MessagesOnHeadPlugin plugin, MessageStackRepository messageStackRepository) {
        this.plugin = plugin;
        this.messageStackRepository = messageStackRepository;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessageSent(ChatEvent event) {
        Configuration config = new Configuration(plugin);
        Player player = event.getPlayer();

        if (!player.hasPermission(Permissions.SHOW)) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (config.hideWhenInvisible() && (player.isInvisible() || player.hasPotionEffect(PotionEffectType.INVISIBILITY))) return;

        var messageStack = messageStackRepository.getMessageStack(player);

        var plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        var sanitizedMessage = ColorUtils.removeColorCodes(plainMessage);
        messageStack.pushMessage(sanitizedMessage);
    }
}
