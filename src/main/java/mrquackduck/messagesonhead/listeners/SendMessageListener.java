package mrquackduck.messagesonhead.listeners;

import io.papermc.paper.event.player.ChatEvent;
import mrquackduck.messagesonhead.MessagesOnHeadPlugin;
import mrquackduck.messagesonhead.classes.MessageStack;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SendMessageListener implements Listener {
    private final MessagesOnHeadPlugin plugin;

    public SendMessageListener(MessagesOnHeadPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessageSent(ChatEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("messagesonhead.show")) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        var stack = MessageStack.getMessagesStack(player, plugin);

        var plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        stack.pushMessage(plainMessage);
    }
}
