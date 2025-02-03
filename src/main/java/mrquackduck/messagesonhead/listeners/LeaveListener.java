package mrquackduck.messagesonhead.listeners;

import mrquackduck.messagesonhead.classes.MessagesStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        MessagesStack.resetPlayerMessageStack(event.getPlayer());
    }
}
