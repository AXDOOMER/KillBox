import static org.lwjgl.glfw.GLFW.*;

/**
 * LWJGL-2-compatible Keyboard shim backed by GLFW.
 * Constants are mapped to their GLFW equivalents.
 * The event-queue emulation is simplified to one event per frame.
 */
public class Keyboard {

    // --- Key constants (GLFW values) -----------------------------------------

    public static final int KEY_ESCAPE      = GLFW_KEY_ESCAPE;
    public static final int KEY_RETURN      = GLFW_KEY_ENTER;
    public static final int KEY_BACK        = GLFW_KEY_BACKSPACE;
    public static final int KEY_TAB         = GLFW_KEY_TAB;
    public static final int KEY_SPACE       = GLFW_KEY_SPACE;

    public static final int KEY_UP          = GLFW_KEY_UP;
    public static final int KEY_DOWN        = GLFW_KEY_DOWN;
    public static final int KEY_LEFT        = GLFW_KEY_LEFT;
    public static final int KEY_RIGHT       = GLFW_KEY_RIGHT;

    public static final int KEY_LCONTROL    = GLFW_KEY_LEFT_CONTROL;
    public static final int KEY_RCONTROL    = GLFW_KEY_RIGHT_CONTROL;
    public static final int KEY_RSHIFT      = GLFW_KEY_RIGHT_SHIFT;

    public static final int KEY_F1          = GLFW_KEY_F1;
    public static final int KEY_F5          = GLFW_KEY_F5;
    public static final int KEY_F10         = GLFW_KEY_F10;
    public static final int KEY_F12         = GLFW_KEY_F12;

    // Digit row
    public static final int KEY_0 = GLFW_KEY_0;
    public static final int KEY_1 = GLFW_KEY_1;
    public static final int KEY_2 = GLFW_KEY_2;
    public static final int KEY_3 = GLFW_KEY_3;
    public static final int KEY_4 = GLFW_KEY_4;
    public static final int KEY_5 = GLFW_KEY_5;
    public static final int KEY_6 = GLFW_KEY_6;
    public static final int KEY_7 = GLFW_KEY_7;
    public static final int KEY_8 = GLFW_KEY_8;
    public static final int KEY_9 = GLFW_KEY_9;

    // Numpad digits
    public static final int KEY_NUMPAD0 = GLFW_KEY_KP_0;
    public static final int KEY_NUMPAD1 = GLFW_KEY_KP_1;
    public static final int KEY_NUMPAD2 = GLFW_KEY_KP_2;
    public static final int KEY_NUMPAD3 = GLFW_KEY_KP_3;
    public static final int KEY_NUMPAD4 = GLFW_KEY_KP_4;
    public static final int KEY_NUMPAD5 = GLFW_KEY_KP_5;
    public static final int KEY_NUMPAD6 = GLFW_KEY_KP_6;
    public static final int KEY_NUMPAD7 = GLFW_KEY_KP_7;
    public static final int KEY_NUMPAD8 = GLFW_KEY_KP_8;
    public static final int KEY_NUMPAD9 = GLFW_KEY_KP_9;

    // Letters
    public static final int KEY_A = GLFW_KEY_A;
    public static final int KEY_B = GLFW_KEY_B;
    public static final int KEY_C = GLFW_KEY_C;
    public static final int KEY_D = GLFW_KEY_D;
    public static final int KEY_E = GLFW_KEY_E;
    public static final int KEY_F = GLFW_KEY_F;
    public static final int KEY_G = GLFW_KEY_G;
    public static final int KEY_H = GLFW_KEY_H;
    public static final int KEY_I = GLFW_KEY_I;
    public static final int KEY_J = GLFW_KEY_J;
    public static final int KEY_K = GLFW_KEY_K;
    public static final int KEY_L = GLFW_KEY_L;
    public static final int KEY_M = GLFW_KEY_M;
    public static final int KEY_N = GLFW_KEY_N;
    public static final int KEY_O = GLFW_KEY_O;
    public static final int KEY_P = GLFW_KEY_P;
    public static final int KEY_Q = GLFW_KEY_Q;
    public static final int KEY_R = GLFW_KEY_R;
    public static final int KEY_S = GLFW_KEY_S;
    public static final int KEY_T = GLFW_KEY_T;
    public static final int KEY_U = GLFW_KEY_U;
    public static final int KEY_V = GLFW_KEY_V;
    public static final int KEY_W = GLFW_KEY_W;
    public static final int KEY_X = GLFW_KEY_X;
    public static final int KEY_Y = GLFW_KEY_Y;
    public static final int KEY_Z = GLFW_KEY_Z;

    // Punctuation
    public static final int KEY_PERIOD    = GLFW_KEY_PERIOD;
    public static final int KEY_SEMICOLON = GLFW_KEY_SEMICOLON;

    // --- Event emulation (set by Display's key callback) ---------------------

    /** True if any key event (press / repeat / release) occurred this frame. */
    static volatile boolean hasKeyEvent  = false;

    /** True if the event was a press or repeat; false if it was a release. */
    static volatile boolean lastKeyState = false;

    // -------------------------------------------------------------------------

    /** Returns true if the given key is currently held down. */
    public static boolean isKeyDown(int key) {
        return glfwGetKey(Display.window, key) == GLFW_PRESS;
    }

    /**
     * Emulates LWJGL 2's Keyboard.next().
     * Returns true once per frame if a key event occurred, then false.
     */
    public static boolean next() {
        if (hasKeyEvent) {
            hasKeyEvent = false;
            return true;
        }
        return false;
    }

    /**
     * Returns the state of the most recent key event:
     * true = key pressed or repeating, false = key released.
     */
    public static boolean getEventKeyState() {
        return lastKeyState;
    }
}
