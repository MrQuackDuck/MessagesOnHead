package mrquackduck.messagesonhead.classes;

import org.bukkit.entity.Entity;

import java.util.List;

public class MessageGroup {
    final List<Entity> entities;
    final int lineCount;

    MessageGroup(List<Entity> entities, int lineCount) {
        this.entities = entities;
        this.lineCount = lineCount;
    }
}