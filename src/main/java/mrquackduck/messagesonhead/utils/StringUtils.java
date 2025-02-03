package mrquackduck.messagesonhead.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static List<String> splitTextIntoLines(String text, int charsPerLine, int symbolsLimit) {
        List<String> lines = new ArrayList<>();
        String remaining = text;
        int totalChars = 0;

        while (!remaining.isEmpty()) {
            // Check if we'll exceed the symbols limit with the next line
            if (symbolsLimit != -1 && totalChars + Math.min(remaining.length(), charsPerLine) > symbolsLimit) {
                // Calculate how many characters we can still add
                int remainingSpace = symbolsLimit - totalChars;
                if (remainingSpace >= 3) {
                    // Add the last line with "..."
                    lines.add(remaining.substring(0, remainingSpace - 3) + "...");
                } else {
                    // Replace last few characters of the last line with "..."
                    String lastLine = lines.get(lines.size() - 1);
                    lines.set(lines.size() - 1, lastLine.substring(0, lastLine.length() - 3) + "...");
                }
                break;
            }

            if (remaining.length() <= charsPerLine) {
                lines.add(remaining);
                break;
            }

            int lastSpaceIndex = remaining.substring(0, charsPerLine).lastIndexOf(' ');

            if (lastSpaceIndex == -1) {
                // No spaces found, force split at CHARS_PER_LINE
                lines.add(remaining.substring(0, charsPerLine));
                totalChars += charsPerLine;
                remaining = remaining.substring(charsPerLine).trim();
            } else {
                // Split at last space
                lines.add(remaining.substring(0, lastSpaceIndex));
                totalChars += lastSpaceIndex;
                remaining = remaining.substring(lastSpaceIndex + 1).trim();
            }
        }

        return lines;
    }
}