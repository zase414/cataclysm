package main;

import static org.lwjgl.opengl.GL20.*;

public class MenuScene extends Scene{
    private static MenuScene instance;

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
    public void update(float dt) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // System.out.println(1.0f/dt+" FPS");
    }
}
