package mrquackduck.messagesonhead.classes;

import mrquackduck.messagesonhead.utils.EntityUtils;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

import static mrquackduck.messagesonhead.classes.MessageStack.customEntityTag;

public class MessageStackRepository {
    private final JavaPlugin plugin;
    private final HashMap<UUID, MessageStack> playersStacks = new HashMap<>();

    public MessageStackRepository(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public MessageStack getMessageStack(Player player) {
        var stack = playersStacks.get(player.getUniqueId());
        if (stack != null) return stack;

        stack = new MessageStack(player, plugin);
        playersStacks.put(player.getUniqueId(), stack);
        return stack;
    }

    public void resetPlayerMessageStack(Player player) {
        var playerMessageStack = playersStacks.get(player.getUniqueId());
        if (playerMessageStack != null) playerMessageStack.deleteAllRelatedEntities();
        playersStacks.remove(player.getUniqueId());
    }

    public void cleanUp() {
        playersStacks.clear();
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (EntityUtils.hasScoreboardTagCaseInvariant(entity, customEntityTag)) {
                    entity.remove();
                }
            }
        }
    }
}
