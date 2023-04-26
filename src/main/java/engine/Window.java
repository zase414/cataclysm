package engine;

import main.KeyListener;
import main.MouseListener;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


import static org.lwjgl.glfw.GLFW.*;

public class Window {
    int width, height;
    private String title;
    private long glfwWindow;

    private static Window window = null;
    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "Cataclysm";
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
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
    }
    public void loop() {
        while (!glfwWindowShouldClose(glfwWindow)) {

            // poll events
            glfwPollEvents();

            // background color
            glClearColor(0.0f,0.0f,0.0f,1.0f);

            if (KeyListener.isKeyPressed(GLFW_KEY_SPACE)) {
                System.out.println("spacebar is pressed");
            }

            glClear(GL_COLOR_BUFFER_BIT);

            glfwSwapBuffers(glfwWindow);

        }
    }
}