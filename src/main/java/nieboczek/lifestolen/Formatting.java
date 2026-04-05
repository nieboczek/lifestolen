package nieboczek.lifestolen;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class Formatting {
    private Formatting() {
    }

    public static MutableComponent toComponent(String text) {
        return Component.literal(text);
    }

    public static MutableComponent red(String text) {
        return toComponent(text).withColor(0xFF3636);
    }

    public static MutableComponent green(String text) {
        return toComponent(text).withColor(0x00FF00);
    }

    public static MutableComponent darkGray(String text) {
        return toComponent(text).withColor(0x404040);
    }

    public static MutableComponent purple(String text) {
        return toComponent(text).withColor(0xA842ED);
    }

    public static MutableComponent niceBlue(String text) {
        return toComponent(text).withColor(0xBBAAE0);
    }

    public static MutableComponent gradientText(String text, int start, int end) {
        MutableComponent result = Component.empty();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            int r = (int) (((start >> 16) & 0xFF) * (1 - ratio) + ((end >> 16) & 0xFF) * ratio);
            int g = (int) (((start >> 8) & 0xFF) * (1 - ratio) + ((end >> 8) & 0xFF) * ratio);
            int b = (int) ((start & 0xFF) * (1 - ratio) + (end & 0xFF) * ratio);

            int rgb = (r << 16) | (g << 8) | b;
            result.append(Component.literal(String.valueOf(text.charAt(i))).withColor(rgb));
        }
        return result;
    }

    public static final class Options {
        public final int color;
        public final boolean bold;
        public final boolean italic;
        public final boolean underline;

        public Options(int color, boolean bold, boolean italic, boolean underline) {
            this.color = color;
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
        }

        public MutableComponent apply(String text) {
            return Component.literal(text).withColor(color);
        }

        public MutableComponent apply(Component component) {
            return component.copy().withColor(color);
        }
    }
}
