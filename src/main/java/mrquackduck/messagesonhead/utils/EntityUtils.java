package mrquackduck.messagesonhead.utils;

import org.bukkit.entity.Entity;

public class EntityUtils {
    public static boolean hasScoreboardTagCaseInvariant(Entity entity, String tag) {
        for (String scoreboardTag : entity.getScoreboardTags()) {
            if (scoreboardTag.equalsIgnoreCase(tag)) {
                return true;
            }
        }

        return false;
    }
}
