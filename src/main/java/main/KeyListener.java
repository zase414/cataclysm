package main;

import static org.lwjgl.glfw.GLFW.*;
public class KeyListener {
    private static KeyListener instance;
    public boolean[] keyPressed = new boolean[350];
    private boolean[] previouslyHeldKeys = new boolean[350];
    private KeyListener() {

    }
    public static KeyListener get() {
        if (KeyListener.instance == null) {
            KeyListener.instance = new KeyListener();
        }
        return KeyListener.instance;
    }
    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            get().keyPressed[key] = true;
        } else if (action == GLFW_RELEASE) {
            get().keyPressed[key] = false;
        }
    }

    public static boolean keyBeingPressed(int keyCode) {
        return get().keyPressed[keyCode];
    }
    public static boolean isKeyReleased(int key) {
        return !get().keyPressed[key] && get().previouslyHeldKeys[key];
    }
    public static boolean keyPushed(int key) {
        return keyBeingPressed(key) && !get().previouslyHeldKeys[key];
    }
    public static void updateHeldKeys() {
        for (int i = 0; i < get().keyPressed.length; i++) {
            if (get().keyPressed[i]) {
                get().previouslyHeldKeys[i] = true;
            } else {
                get().previouslyHeldKeys[i] = false;
            }
        }
    }
}
