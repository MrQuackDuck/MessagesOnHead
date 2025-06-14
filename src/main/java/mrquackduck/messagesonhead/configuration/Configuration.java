package mrquackduck.messagesonhead.configuration;

import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public class Configuration extends ConfigurationBase {
    public Configuration(JavaPlugin plugin) {
        super(plugin);
    }

    public String textColor() {
        return getString("textColor");
    }

    public String backgroundColor() {
        return getString("backgroundColor");
    }

    public int backgroundTransparencyPercentage() {
        return getInt("backgroundTransparencyPercentage");
    }

    public boolean isBackgroundEnabled() {
        return getBoolean("backgroundEnabled");
    }

    public boolean isTimerEnabled() {
        return getBoolean("timerEnabled");
    }

    public String timerColor() {
        return getString("timerColor");
    }

    public boolean isShadowed() {
        return getBoolean("isShadowed");
    }

    public String timerFormat() {
        return getString("timerFormat");
    }

    public int minSymbolsForTimer() {
        return getInt("minSymbolsForTimer");
    }

    public int symbolsPerLine() {
        return getInt("symbolsPerLine");
    }

    public int symbolsLimit() {
        return getInt("symbolsLimit");
    }

    public boolean isLowerModeEnabled() {
        return getBoolean("lowerMode");
    }

    public long timeToExist() {
        return getLong("timeToExist");
    }

    public boolean isScalingEnabled() {
        return getBoolean("scalingEnabled");
    }

    public double scalingCoefficient() {
        return getDouble("scalingCoefficient");
    }
}