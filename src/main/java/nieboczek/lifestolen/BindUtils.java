package nieboczek.lifestolen;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class BindUtils {
    private BindUtils() {
    }

    public static String getKeyLabel(int keycode) {
        if (keycode <= 0) {
            return "None";
        }

        return switch (keycode) {
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "Shift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "Ctrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
            case GLFW.GLFW_KEY_LEFT_ALT -> "Alt";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RAlt";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW.GLFW_KEY_DELETE -> "Delete";
            case GLFW.GLFW_KEY_INSERT -> "Insert";
            case GLFW.GLFW_KEY_HOME -> "Home";
            case GLFW.GLFW_KEY_END -> "End";
            case GLFW.GLFW_KEY_PAGE_UP -> "PageUp";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PageDown";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            case GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CapsLock";
            case GLFW.GLFW_KEY_PAUSE -> "Pause";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "ScrollLock";
            default -> {
                String display = InputConstants.Type.KEYSYM.getOrCreate(keycode).getDisplayName().getString();
                yield display.isBlank() ? "KEY " + keycode : display.toUpperCase();
            }
        };
    }

    public static int getKeycode(String label) {
        String lowerCaseLabel = label.toLowerCase(Locale.ROOT);
        return switch (lowerCaseLabel) {
            case "space" -> GLFW.GLFW_KEY_SPACE;
            case "shift", "lshift" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "rshift" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "ctrl", "lctrl" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "rctrl" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "alt", "lalt" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "ralt" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "enter" -> GLFW.GLFW_KEY_ENTER;
            case "tab" -> GLFW.GLFW_KEY_TAB;
            case "backspace" -> GLFW.GLFW_KEY_BACKSPACE;
            case "del", "delete" -> GLFW.GLFW_KEY_DELETE;
            case "ins", "insert" -> GLFW.GLFW_KEY_INSERT;
            case "home" -> GLFW.GLFW_KEY_HOME;
            case "end" -> GLFW.GLFW_KEY_END;
            case "pgup", "pageup" -> GLFW.GLFW_KEY_PAGE_UP;
            case "pgdn", "pagedown" -> GLFW.GLFW_KEY_PAGE_DOWN;
            case "up" -> GLFW.GLFW_KEY_UP;
            case "down" -> GLFW.GLFW_KEY_DOWN;
            case "left" -> GLFW.GLFW_KEY_LEFT;
            case "right" -> GLFW.GLFW_KEY_RIGHT;
            case "esc", "escape" -> GLFW.GLFW_KEY_ESCAPE;
            case "caps", "capslock" -> GLFW.GLFW_KEY_CAPS_LOCK;
            case "pause" -> GLFW.GLFW_KEY_PAUSE;
            case "scrlk", "scrolllock" -> GLFW.GLFW_KEY_SCROLL_LOCK;
            case ";" -> GLFW.GLFW_KEY_SEMICOLON;
            case "'" -> GLFW.GLFW_KEY_APOSTROPHE;
            case "\\" -> GLFW.GLFW_KEY_BACKSLASH;
            case "/" -> GLFW.GLFW_KEY_SLASH;
            case "f1" -> GLFW.GLFW_KEY_F1;
            case "f2" -> GLFW.GLFW_KEY_F2;
            case "f3" -> GLFW.GLFW_KEY_F3;
            case "f4" -> GLFW.GLFW_KEY_F4;
            case "f5" -> GLFW.GLFW_KEY_F5;
            case "f6" -> GLFW.GLFW_KEY_F6;
            case "f7" -> GLFW.GLFW_KEY_F7;
            case "f8" -> GLFW.GLFW_KEY_F8;
            case "f9" -> GLFW.GLFW_KEY_F9;
            case "f10" -> GLFW.GLFW_KEY_F10;
            case "f11" -> GLFW.GLFW_KEY_F11;
            case "f12" -> GLFW.GLFW_KEY_F12;
            case "f13" -> GLFW.GLFW_KEY_F13;
            case "f14" -> GLFW.GLFW_KEY_F14;
            case "f15" -> GLFW.GLFW_KEY_F15;
            case "f16" -> GLFW.GLFW_KEY_F16;
            case "f17" -> GLFW.GLFW_KEY_F17;
            case "f18" -> GLFW.GLFW_KEY_F18;
            case "f19" -> GLFW.GLFW_KEY_F19;
            case "f20" -> GLFW.GLFW_KEY_F20;
            case "f21" -> GLFW.GLFW_KEY_F21;
            case "f22" -> GLFW.GLFW_KEY_F22;
            case "f23" -> GLFW.GLFW_KEY_F23;
            case "f24" -> GLFW.GLFW_KEY_F24;
            case "f25" -> GLFW.GLFW_KEY_F25;
            case "a" -> GLFW.GLFW_KEY_A;
            case "b" -> GLFW.GLFW_KEY_B;
            case "c" -> GLFW.GLFW_KEY_C;
            case "d" -> GLFW.GLFW_KEY_D;
            case "e" -> GLFW.GLFW_KEY_E;
            case "f" -> GLFW.GLFW_KEY_F;
            case "g" -> GLFW.GLFW_KEY_G;
            case "h" -> GLFW.GLFW_KEY_H;
            case "i" -> GLFW.GLFW_KEY_I;
            case "j" -> GLFW.GLFW_KEY_J;
            case "k" -> GLFW.GLFW_KEY_K;
            case "l" -> GLFW.GLFW_KEY_L;
            case "m" -> GLFW.GLFW_KEY_M;
            case "n" -> GLFW.GLFW_KEY_N;
            case "o" -> GLFW.GLFW_KEY_O;
            case "p" -> GLFW.GLFW_KEY_P;
            case "q" -> GLFW.GLFW_KEY_Q;
            case "r" -> GLFW.GLFW_KEY_R;
            case "s" -> GLFW.GLFW_KEY_S;
            case "t" -> GLFW.GLFW_KEY_T;
            case "u" -> GLFW.GLFW_KEY_U;
            case "v" -> GLFW.GLFW_KEY_V;
            case "w" -> GLFW.GLFW_KEY_W;
            case "x" -> GLFW.GLFW_KEY_X;
            case "y" -> GLFW.GLFW_KEY_Y;
            case "z" -> GLFW.GLFW_KEY_Z;
            case "0" -> GLFW.GLFW_KEY_0;
            case "1" -> GLFW.GLFW_KEY_1;
            case "2" -> GLFW.GLFW_KEY_2;
            case "3" -> GLFW.GLFW_KEY_3;
            case "4" -> GLFW.GLFW_KEY_4;
            case "5" -> GLFW.GLFW_KEY_5;
            case "6" -> GLFW.GLFW_KEY_6;
            case "7" -> GLFW.GLFW_KEY_7;
            case "8" -> GLFW.GLFW_KEY_8;
            case "9" -> GLFW.GLFW_KEY_9;
            default -> {
                try {
                    yield Integer.parseInt(lowerCaseLabel.replace("key ", ""));
                } catch (NumberFormatException ignored) {
                    yield 0;
                }
            }
        };
    }
}
