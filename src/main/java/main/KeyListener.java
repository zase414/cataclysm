package main;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;
public class KeyListener {
    private static KeyListener instance;
    private boolean[] keyPressed = new boolean[350];
    HashMap<Integer, Boolean> heldKeys = new HashMap<>();
    private KeyListener() {

    }

    public static KeyListener get() {
        if (KeyListener.instance == null) {
            KeyListener.instance = new KeyListener();
            instance.initHeldKeys();
        }
        return KeyListener.instance;
    }

    public void initHeldKeys() {
        for (int key : new int[]{GLFW_KEY_TAB}) {
            heldKeys.put(key, false);
        }
    }

    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            get().keyPressed[key] = true;
        } else if (action == GLFW_RELEASE) {
            get().keyPressed[key] = false;
        }
    }

    public static boolean isKeyPressed(int keyCode) {
        return get().keyPressed[keyCode];
    }
}
