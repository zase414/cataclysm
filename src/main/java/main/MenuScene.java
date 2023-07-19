package main;

import static main.KeyListener.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;

public class MenuScene extends Scene{
    private static MenuScene instance;
    private MapConverter mapConverter = new MapConverter();

    public MenuScene() {

    }
    public static MenuScene get() {
        if (MenuScene.instance == null) {
            MenuScene.instance = new MenuScene();
            instance.init();
        }
        return MenuScene.instance;
    }
    @Override
    public void update(double dt) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        handleInputEvents();
    }

    private void handleInputEvents() {
        if (isKeyReleased(GLFW_KEY_ENTER)) mapConverter.run();
        updateHeldKeys();
    }
}
