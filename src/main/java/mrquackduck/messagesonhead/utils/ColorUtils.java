package mrquackduck.messagesonhead.utils;

public class ColorUtils {
    public static int hexToARGB(String hex, int backgroundTransparencyPercentage) {
        // Ensure the hex string is valid
        if (hex.startsWith("#")) hex = hex.substring(1);

        if (hex.length() != 6) throw new IllegalArgumentException("Hex color must be a 6-character string.");

        // Convert hex to RGB
        int rgb = Integer.parseInt(hex, 16);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        // Calculate alpha (transparency)
        int alpha = (int) (255 * (1 - (backgroundTransparencyPercentage / 100.0)));

        // Return ARGB as a single integer
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
