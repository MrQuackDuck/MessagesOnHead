package mrquackduck.messagesonhead.listeners;

import io.papermc.paper.event.player.ChatEvent;
import mrquackduck.messagesonhead.classes.MessageStackRepository;
import mrquackduck.messagesonhead.utils.ColorUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SendMessageListener implements Listener {
    private final MessageStackRepository messageStackRepository;

    public SendMessageListener(MessageStackRepository messageStackRepository) {
        this.messageStackRepository = messageStackRepository;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessageSent(ChatEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("messagesonhead.show")) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        var messageStack = messageStackRepository.getMessageStack(player);

        var plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        var sanitizedMessage = ColorUtils.removeColorCodes(plainMessage);
        messageStack.pushMessage(sanitizedMessage);
    }
}
