package mrquackduck.messagesonhead.configuration;

import org.bukkit.entity.Display;
import org.bukkit.plugin.java.JavaPlugin;

public class Configuration extends ConfigurationBase {
    public Configuration(JavaPlugin plugin) {
        super(plugin);
    }

    public int symbolsPerLine() {
        return getInt("symbolsPerLine");
    }

    public int symbolsLimit() {
        return getInt("symbolsLimit");
    }

    public long timeToExist() {
        return getLong("timeToExist");
    }

    public boolean visibleToSender() {
        return getBoolean("visibleToSender");
    }

    public boolean isScalingEnabled() {
        return getBoolean("scalingEnabled");
    }

    public double scalingCoefficient() {
        return getDouble("scalingCoefficient");
    }

    public float gapBetweenMessages() {
        return (float)getDouble("gapBetweenMessages");
    }

    public float gapAboveHead() {
        return (float)getDouble("gapAboveHead");
    }

    public String textColor() {
        return getString("textColor");
    }

    public boolean isTimerEnabled() {
        return getBoolean("timerEnabled");
    }

    public int minSymbolsForTimer() {
        return getInt("minSymbolsForTimer");
    }

    public String timerFormat() {
        return getString("timerFormat");
    }

    public String timerColor() {
        return getString("timerColor");
    }

    public boolean isBackgroundEnabled() {
        return getBoolean("backgroundEnabled");
    }

    public String backgroundColor() {
        return getString("backgroundColor");
    }

    public int backgroundTransparencyPercentage() {
        return getInt("backgroundTransparencyPercentage");
    }

    public boolean isShadowed() {
        return getBoolean("isShadowed");
    }

    public Display.Billboard pivotAxis() {
        return getEnumValue("pivotAxis", Display.Billboard.class, Display.Billboard.VERTICAL);
    }

    public boolean isPlaceholderApiIntegrationEnabled() {
        return getBoolean("placeholderApiIntegration");
    }

    public String colorPlaceholder() {
        return getString("colorPlaceholder");
    }

    public String lineFormat() {
        return getString("lineFormat");
    }
}