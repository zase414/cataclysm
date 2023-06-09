package main;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import util.Time;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
    public int width, height;
    public float r = 0.0f, g = 0.0f, b = 0.0f, a = 1.0f; // default background color
    public String title;
    long glfwWindow;
    public static Scene currentScene = null;
    static Window window = null;
    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "cataclysm";
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public static void changeScene(int newScene) {
        switch (newScene) {
            case 0 -> {
                System.out.println("Scene -> Menu");
                currentScene = MenuScene.get();
                // un-clip the mouse, remember the coordinates
                float mouseX = MouseListener.getX();
                float mouseY = MouseListener.getY();
                glfwSetInputMode(window.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                glfwSetCursorPos(Window.window.glfwWindow, mouseX, mouseY);
                currentScene.init();
            }
            case 1 -> {
                System.out.println("Scene -> FPV");
                // switch scene and initialize it if needed
                currentScene = MainScene.get();
                // clip the mouse to the window
                glfwSetInputMode(window.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            }
            case 2 -> {
                System.out.println("Scene -> Map");
                // switch scene and initialize it if needed
                currentScene = MapScene.get();
                // clip the mouse to the window
                glfwSetInputMode(window.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            }
            default -> {
                assert false : "Unknown scene '" + newScene + "'";
            }
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
        glfwWindowHint(GLFW_FOCUSED, 1);
        glfwWindowHint(GLFW_VISIBLE, 0);
        glfwWindowHint(GLFW_RESIZABLE, 1);
        glfwWindowHint(GLFW_MAXIMIZED, 0);
        glfwWindowHint(GLFW_DECORATED, 1);

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

        // render based on how far away the vertexes are
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);

        Window.changeScene(0);
    }
    public void loop() {

        int[] width = new int[1], height = new int[1];
        glfwGetWindowSize(glfwWindow, width, height);
        this.height = height[0];
        this.width = width[0];

        double beginTime = Time.getTime();
        double endTime;
        double dt = -1.0f;

        while (!glfwWindowShouldClose(glfwWindow)) {

            // poll events
            glfwPollEvents();

            // background color
            glClearColor(r,g,b,a);
            glClear(GL_COLOR_BUFFER_BIT);

            if (dt >= 0) {
                currentScene.update(dt);
            }

            glfwSetWindowTitle(glfwWindow, title + " (" +1.0f/dt+" fps)");

            if (KeyListener.keyBeingPressed(GLFW_KEY_F1)) {
                changeScene(0);
            }
            if (KeyListener.keyBeingPressed(GLFW_KEY_F2)) {
                changeScene(1);
            }
            if (KeyListener.keyBeingPressed(GLFW_KEY_F3)) {
                changeScene(2);
            }

            glfwSwapBuffers(glfwWindow);

            endTime = Time.getTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }
}