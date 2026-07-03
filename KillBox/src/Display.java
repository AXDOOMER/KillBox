import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

/**
 * LWJGL-2-compatible Display shim backed by GLFW.
 * All methods are static, mirroring the LWJGL 2 Display API.
 */
public class Display {

    // package-visible so Keyboard / Mouse can access the handle
    static long window = NULL;

    private static int width = 640;
    private static int height = 480;
    private static boolean resized = false;
    private static boolean fullscreen = false;
    private static int windowedWidth = 640;
    private static int windowedHeight = 480;
    private static String title = "KillBox";
    private static boolean vsync = false;
    private static boolean resizable = false;

    // Initialise GLFW as soon as the class is loaded
    static {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialise GLFW");
        }
    }

    // -----------------------------------------------------------------------
    // Pre-create configuration (called before create())
    // -----------------------------------------------------------------------

    public static void setDisplayMode(DisplayMode mode) {
        width  = mode.getWidth();
        height = mode.getHeight();
        if (!fullscreen) {
            windowedWidth  = width;
            windowedHeight = height;
        }
        if (window != NULL && !fullscreen) {
            glfwSetWindowSize(window, width, height);
        }
    }

    public static void setFullscreen(boolean fs) {
        if (window == NULL) {
            fullscreen = fs;
            return;
        }
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        if (fs) {
            glfwSetWindowMonitor(window, monitor, 0, 0,
                    vidmode.width(), vidmode.height(), vidmode.refreshRate());
            width  = vidmode.width();
            height = vidmode.height();
        } else {
            glfwSetWindowMonitor(window, NULL, 50, 50,
                    windowedWidth, windowedHeight, GLFW_DONT_CARE);
            width  = windowedWidth;
            height = windowedHeight;
        }
        // Re-apply vsync – mode changes reset the swap interval
        glfwSwapInterval(vsync ? 1 : 0);
        fullscreen = fs;
    }

    public static void setTitle(String t) {
        title = t;
        if (window != NULL) {
            glfwSetWindowTitle(window, t);
        }
    }

    public static void setVSyncEnabled(boolean v) {
        vsync = v;
        if (window != NULL) {
            glfwSwapInterval(v ? 1 : 0);
        }
    }

    public static void setResizable(boolean r) {
        resizable = r;
        if (window != NULL) {
            glfwSetWindowAttrib(window, GLFW_RESIZABLE, r ? GLFW_TRUE : GLFW_FALSE);
        }
    }

    // -----------------------------------------------------------------------
    // create() / destroy()
    // -----------------------------------------------------------------------

    /** Creates the GLFW window and makes the OpenGL context current. */
    public static void create() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
        // Request a legacy OpenGL 2.1 context so that fixed-function APIs
        // (glBegin/End, glAlphaFunc, etc.) are available on macOS.
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        long monitor = NULL;
        if (fullscreen) {
            monitor = glfwGetPrimaryMonitor();
            GLFWVidMode vm = glfwGetVideoMode(monitor);
            width  = vm.width();
            height = vm.height();
        }

        window = glfwCreateWindow(width, height, title, monitor, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Centre the window on screen when running in windowed mode
        if (!fullscreen) {
            GLFWVidMode vm = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window,
                    (vm.width()  - width)  / 2,
                    (vm.height() - height) / 2);
        }

        // Framebuffer resize callback
        glfwSetFramebufferSizeCallback(window, (win, fw, fh) -> {
            width  = fw;
            height = fh;
            resized = true;
        });

        // Key callback – used for menu event detection (see Keyboard)
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            Keyboard.hasKeyEvent  = true;
            Keyboard.lastKeyState = (action != GLFW_RELEASE);
        });

        // Cursor position callback – used for mouse delta (see Mouse)
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            Mouse.deltaCursorX += xpos - Mouse.lastCursorX;
            Mouse.deltaCursorY += ypos - Mouse.lastCursorY;
            Mouse.lastCursorX   = xpos;
            Mouse.lastCursorY   = ypos;
        });

        glfwMakeContextCurrent(window);

        // LWJGL 3 requires GL capabilities to be created for this context
        GL.createCapabilities();

        glfwSwapInterval(vsync ? 1 : 0);

        // Initialise mouse reference position to avoid a jump on first frame
        Mouse.initCursorPosition();

        glfwShowWindow(window);

        // Query actual framebuffer dimensions (may differ from window on HiDPI)
        try (org.lwjgl.system.MemoryStack stack = stackPush()) {
            IntBuffer pw = stack.mallocInt(1);
            IntBuffer ph = stack.mallocInt(1);
            glfwGetFramebufferSize(window, pw, ph);
            width  = pw.get(0);
            height = ph.get(0);
        }
    }

    public static void destroy() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    // -----------------------------------------------------------------------
    // Per-frame update
    // -----------------------------------------------------------------------

    public static void update() {
        glfwSwapBuffers(window);
        // Reset per-frame state before polling so callbacks accumulate fresh data
        Keyboard.hasKeyEvent = false;
        Mouse.resetDelta();
        glfwPollEvents();
    }

    // -----------------------------------------------------------------------
    // Query methods
    // -----------------------------------------------------------------------

    public static boolean isCloseRequested() {
        return glfwWindowShouldClose(window);
    }

    public static boolean wasResized() {
        if (resized) {
            resized = false;
            return true;
        }
        return false;
    }

    public static int getWidth()  { return width;  }
    public static int getHeight() { return height; }

    /** Returns a DisplayMode representing the current window/framebuffer size. */
    public static DisplayMode getDisplayMode() {
        return new DisplayMode(width, height);
    }

    /** Returns a DisplayMode representing the primary monitor's resolution. */
    public static DisplayMode getDesktopDisplayMode() {
        GLFWVidMode vm = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return new DisplayMode(vm.width(), vm.height());
    }
}
