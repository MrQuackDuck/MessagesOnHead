package mrquackduck.messagesonhead.listeners;

import mrquackduck.messagesonhead.classes.MessageStackRepository;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {
    private final MessageStackRepository messageStackRepository;

    public LeaveListener(MessageStackRepository messageStackRepository) {
        this.messageStackRepository = messageStackRepository;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        messageStackRepository.resetPlayerMessageStack(event.getPlayer());
    }
}
