package main;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import util.Time;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
    int width, height;
    private float r, g, b, a;
    private String title;
    private long glfwWindow;
    private static Scene currentScene = null;

    private static Window window = null;
    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "cataclysm";
        r = 1;
        g = 0;
        b = 0;
        a = 1;
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public static void changeScene(int newScene) {
        switch (newScene) {
            case 0:
                currentScene = new MenuScene();
                //currentScene.init();
                break;
            case 1:
                currentScene = new LevelEditorScene();
                break;
            case 2:
                currentScene = new LevelScene();
                break;
            default:
                assert false : "Unknown scene '" + newScene + "'";
        }
    }

    public void run() {
        System.out.println("LWJGL initialized, ver: " + Version.getVersion());
        init();
        loop();

        // free the mem
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // end glfw
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        // err callback
        GLFWErrorCallback.createPrint(System.err).set();

        // GLFW init
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW initialization err");
        }

        // GLFW config
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, 0);
        glfwWindowHint(GLFW_RESIZABLE, 1);
        glfwWindowHint(GLFW_MAXIMIZED, 0);

        // window id creation
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (glfwWindow == NULL) {
            throw new IllegalStateException("GLFW window creation err");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        glfwMakeContextCurrent(glfwWindow);

        // vsync
        glfwSwapInterval(1);

        // make window visible
        glfwShowWindow(glfwWindow);

        // enable bindings
        GL.createCapabilities();

        Window.changeScene(0);
    }
    public void loop() {
        float beginTime = Time.getTime();
        float endTime = Time.getTime();
        float dt = -1.0f;

        while (!glfwWindowShouldClose(glfwWindow)) {

            // poll events
            glfwPollEvents();

            // background color
            glClearColor(r,g,b,a);
            glClear(GL_COLOR_BUFFER_BIT);



            if (dt >= 0) {
                currentScene.update(dt);
            }






            glfwSwapBuffers(glfwWindow);

            endTime = Time.getTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }
}