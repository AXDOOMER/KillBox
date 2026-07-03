import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;

import java.nio.DoubleBuffer;

/**
 * LWJGL-2-compatible Mouse shim backed by GLFW.
 */
public class Mouse {

    private static boolean grabbed = false;

    // Cursor-position tracking (updated by Display's cursor-position callback)
    static double lastCursorX = 0.0;
    static double lastCursorY = 0.0;
    static double deltaCursorX = 0.0;
    static double deltaCursorY = 0.0;

    // -------------------------------------------------------------------------
    // Called from Display.create() to seed the reference position
    // -------------------------------------------------------------------------

    static void initCursorPosition() {
        try (org.lwjgl.system.MemoryStack stack = stackPush()) {
            DoubleBuffer xb = stack.mallocDouble(1);
            DoubleBuffer yb = stack.mallocDouble(1);
            glfwGetCursorPos(Display.window, xb, yb);
            lastCursorX = xb.get(0);
            lastCursorY = yb.get(0);
        }
    }

    // Called from Display.update() before glfwPollEvents()
    static void resetDelta() {
        deltaCursorX = 0.0;
        deltaCursorY = 0.0;
    }

    // -------------------------------------------------------------------------
    // Public API (matching LWJGL 2 Mouse)
    // -------------------------------------------------------------------------

    public static boolean isGrabbed() {
        return grabbed;
    }

    public static void setGrabbed(boolean grab) {
        grabbed = grab;
        if (grab) {
            glfwSetInputMode(Display.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        } else {
            glfwSetInputMode(Display.window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
        // Reset delta so there is no jump on the next frame
        deltaCursorX = 0.0;
        deltaCursorY = 0.0;
        initCursorPosition();
    }

    /** Returns horizontal mouse movement since last frame (integer pixels). */
    public static int getDX() {
        return (int) deltaCursorX;
    }

    /**
     * Returns vertical mouse movement since last frame (integer pixels).
     * Negated to match LWJGL 2's convention (positive = up).
     */
    public static int getDY() {
        return -(int) deltaCursorY;
    }

    public static boolean isButtonDown(int button) {
        return glfwGetMouseButton(Display.window, button) == GLFW_PRESS;
    }

    public static void setCursorPosition(int x, int y) {
        glfwSetCursorPos(Display.window, x, y);
        // Update reference so the next delta starts from here
        lastCursorX  = x;
        lastCursorY  = y;
        deltaCursorX = 0.0;
        deltaCursorY = 0.0;
    }
}
