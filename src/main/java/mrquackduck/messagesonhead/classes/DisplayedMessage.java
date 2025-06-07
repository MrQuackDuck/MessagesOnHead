package mrquackduck.messagesonhead.classes;

import org.bukkit.entity.Entity;

import java.util.List;

/**
 * A structure that stores all entities related to a single displayed message
 */
public class DisplayedMessage {
    final List<Entity> entities;

    DisplayedMessage(List<Entity> entities) {
        this.entities = entities;
    }
}