package mrquackduck.messagesonhead.configuration;

import org.bukkit.entity.Display;
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

    public float gapBetweenMessages() {
        return (float)getDouble("gapBetweenMessages");
    }

    public float gapAboveHead() {
        return (float)getDouble("gapAboveHead");
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

    public boolean isPlaceholderApiIntegrationEnabled() {
        return getBoolean("placeholderApiIntegration");
    }

    public String colorPlaceholder() {
        return getString("colorPlaceholder");
    }

    public String lineFormat() {
        return getString("lineFormat");
    }

    public Display.Billboard pivotAxis() {
        return getEnumValue("pivotAxis", Display.Billboard.class, Display.Billboard.VERTICAL);
    }
}