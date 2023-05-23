package main;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import render.RayCaster;
import render.Shader;
import util.Ray;
import util.Time;
import util.Wall;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL20.*;

public class MapScene extends Scene{
    public float[] vertexArray = {};
    private int[] elementArray = {}; // ONLY COUNTER-CLOCKWISE ORDER WORKS
    private int vaoID, vboID, eboID;
    private Shader defaultShader;
    RayCaster rayCaster;
    Map map;
    Player player;
    int positionsSize = 3;
    int colorSize = 4;
    int floatSizeBytes = 4;
    float mapZoom = 10.0f, minMapZoom = 1.0f, maxMapZoom = 40.0f;
    public static MapScene instance = null;

    public static MapScene get() {
        if (MapScene.instance == null) {
            MapScene.instance = new MapScene();
            instance.init();
        }
        return MapScene.instance;
    }
    @Override
    public void init() {

        // initialize the rayCaster
        rayCaster = MainScene.get().rayCaster;

        // initialize the map
        map = MainScene.get().map;

        // initialize the player
        player = MainScene.get().player;

        // initialize the camera
        camera = new Camera(new Vector2f());

        // import and compile the shaders
        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();

        // ---------------------------------
        // generate VAO, VBO, EBO and send them to GPU
        // ---------------------------------

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip(); // '.flip()' makes OpenGL understand it correctly or something

        // create VBO upload vertex buffer
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

        // create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        // create EBO upload element buffer
        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_DYNAMIC_DRAW);

        // add vertex attribute pointers

        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;

        // position attributes
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        // color attributes
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, (long) positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }
    @Override
    public void update(float dt) {

        if (!KeyListener.isKeyPressed(GLFW_KEY_TAB)) {
            canSwitchScene = true;
        } else if (KeyListener.isKeyPressed(GLFW_KEY_TAB) && canSwitchScene) {
            MainScene.get().canSwitchScene = false;
            Window.changeScene(1);
        }

        // show mouse cursor
        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_ALT)) {

            glfwSetInputMode(Window.get().glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

            // map camera moving and zooming
            mapZoom = Math.min(Math.max(MouseListener.getScrollY() + mapZoom, minMapZoom), maxMapZoom);
            if (MouseListener.getScrollY() > 0) {
                camera.position.x += (MouseListener.getX() - Window.window.width / 2.0f) * MouseListener.getScrollY() * 0.1f;
                camera.position.y -= (MouseListener.getY() - Window.window.height / 2.0f) * MouseListener.getScrollY() * 0.1f;
            } else {
                camera.position.x += (MouseListener.getScrollY() * 0.1f) * camera.position.x;
                camera.position.y += (MouseListener.getScrollY() * 0.1f) * camera.position.y;
            }

        } else {
            glfwSetInputMode(Window.window.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }

        // ==== debug ==== show cursor coordinates
        //System.out.println(MouseListener.getX() + " | " + MouseListener.getY());

        {
            if (glfwGetInputMode(Window.get().glfwWindow, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {
                // player view angle change
                player.viewAngle -= Settings.mouseSensitivity * MouseListener.getDX();
                //System.out.println(player.viewAngle);
            }

            // vector of movement
            double playerDX = 0.0;
            double playerDY = 0.0;
            double strafeMultiplier = 1.0;
            boolean playerIsStrafing = (KeyListener.isKeyPressed(GLFW_KEY_W) || KeyListener.isKeyPressed(GLFW_KEY_S)) && (KeyListener.isKeyPressed(GLFW_KEY_D) || KeyListener.isKeyPressed(GLFW_KEY_A));
            double movementMultipliers = player.speed * dt * strafeMultiplier;

            if (KeyListener.isKeyPressed(GLFW_KEY_W)) {
                playerDX += movementMultipliers * Math.sin(Math.toRadians(player.viewAngle));
                playerDY += movementMultipliers * Math.cos(Math.toRadians(player.viewAngle));
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_S)) {
                playerDX -= movementMultipliers * Math.sin(Math.toRadians(player.viewAngle));
                playerDY -= movementMultipliers * Math.cos(Math.toRadians(player.viewAngle));
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_A)) {
                playerDX += movementMultipliers * Math.sin(Math.toRadians(player.viewAngle - 90));
                playerDY += movementMultipliers * Math.cos(Math.toRadians(player.viewAngle - 90));
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_D)) {
                playerDX += movementMultipliers * Math.sin(Math.toRadians(player.viewAngle + 90));
                playerDY += movementMultipliers * Math.cos(Math.toRadians(player.viewAngle + 90));
            }

            if (playerIsStrafing) {
                playerDX *= Math.sqrt(2) / 2;
                playerDY *= Math.sqrt(2) / 2;
            }

            player.posX += playerDX;
            // update coordinates of the collision box
            player.collisionBox.update(player);
            if (player.collisionBox.checkForCollisionsPlayer(map, player)) {
                //System.out.println("clipping into a wall");
                player.posX -= playerDX;
            }
            player.posY += playerDY;
            // update coordinates of the collision box
            player.collisionBox.update(player);
            if (player.collisionBox.checkForCollisionsPlayer(map, player)) {
                //System.out.println("clipping into a wall");
                player.posY -= playerDY;
            }
            // update coordinates of the collision box
            player.collisionBox.update(player);

            // ==== debug ==== show speed
            //System.out.println(Math.sqrt(playerDX * playerDX + playerDY  * playerDY));

        } // player movement

        {
            // update the rayCaster == update distances to walls
            rayCaster.cast(player, map);

            List<Float> vertexList = new ArrayList<>();
            List<Integer> elementList = new ArrayList<>();

            // create wall related vertex and element lists, add them to the final list
            List<Float> wallVertexList = wallVertexList(map);
            List<Integer> wallElementList = wallElementList(wallVertexList.size(), getHighestIndex(elementList));

            vertexList.addAll(wallVertexList);
            elementList.addAll(wallElementList);

            // create intersection related vertex and element lists, add them to the final list
            List<Float> intersectionVertexList = intersectionVertexList(rayCaster);
            List<Integer> intersectionElementList = intersectionElementList(intersectionVertexList.size(), getHighestIndex(elementList) + 1);

            vertexList.addAll(intersectionVertexList);
            elementList.addAll(intersectionElementList);

            // create player related vertex and element lists, add them to the final list
            List<Float> playerVertexList = playerVertexList(player);
            List<Integer> playerElementList = playerElementList(getHighestIndex(elementList) + 1);

            vertexList.addAll(playerVertexList);
            elementList.addAll(playerElementList);


            // make it an array
            this.vertexArray = new float[vertexList.size()];

            for (int i = 0; i < vertexList.size(); i++) {
                vertexArray[i] = vertexList.get(i);
            }

            this.elementArray = new int[elementList.size()];

            for (int i = 0; i < elementList.size(); i++) {
                elementArray[i] = elementList.get(i);
            }

        } // vertex and element array build

        {
            // use the shader and upload values
            defaultShader.use();
            defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
            defaultShader.uploadMat4f("uView", camera.getViewMatrix());
            defaultShader.uploadFloat("uTime", Time.getTime());

            // ---------------------------------
            // generate VAO, VBO, EBO and send them to GPU
            // ---------------------------------

            glBindVertexArray(vaoID);

            // create a float buffer of vertices
            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
            vertexBuffer.put(vertexArray).flip(); // '.flip()' makes OpenGL understand it correctly or something

            // create VBO upload vertex buffer
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

            // create the indices and upload
            IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
            elementBuffer.put(elementArray).flip();

            // create EBO upload element buffer
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_DYNAMIC_DRAW);

            // add vertex attribute pointers
            int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;

            // position attributes
            glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
            glEnableVertexAttribArray(0);

            // color attributes
            glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, (long) positionsSize * floatSizeBytes);
            glEnableVertexAttribArray(1);

            // draw
            glDrawElements(GL_TRIANGLES, this.elementArray.length, GL_UNSIGNED_INT, 0);

            vertexArray = null;
            elementArray = null;

            // unbind everything
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glBindVertexArray(0);
            defaultShader.detach();

            glDeleteBuffers(vaoID);
            glDeleteBuffers(vboID);
            glDeleteBuffers(eboID);

        } // graphics
        MouseListener.endFrame();
    }

    public List<Float> wallVertexList(Map map) {

        List<Float> vertexList = new ArrayList<>();

        float width = 1.0f;
        float zoom = mapZoom;

        for (Wall wall:map.walls) {

            addRectangleToVertexList(vertexList, wall.x1, wall.y1, zoom, width, 1.0f, 1.0f, 1.0f, 1.0f);
            addRectangleToVertexList(vertexList, wall.x2, wall.y2, zoom, width, 1.0f, 1.0f, 1.0f, 1.0f);
        }

        return vertexList;
    }
    public List<Integer> wallElementList(int wallVertexListLength, int firstElementIndex) {

        List<Integer> elementList = new ArrayList<>();

        // for every 2 squares = edges of wall
        for (int i = 0; i < (wallVertexListLength / 54); i++) {
            int v = 8 * i;
            // x1, y1
            elementList.add(firstElementIndex + 2 + v);
            elementList.add(firstElementIndex + 1 + v);
            elementList.add(firstElementIndex + 0 + v);

            elementList.add(firstElementIndex + 1 + v);
            elementList.add(firstElementIndex + 2 + v);
            elementList.add(firstElementIndex + 3 + v);

            // x2, y2
            elementList.add(firstElementIndex + 6 + v);
            elementList.add(firstElementIndex + 5 + v);
            elementList.add(firstElementIndex + 4 + v);

            elementList.add(firstElementIndex + 5 + v);
            elementList.add(firstElementIndex + 6 + v);
            elementList.add(firstElementIndex + 7 + v);

            // connection
            elementList.add(firstElementIndex + 0 + v);
            elementList.add(firstElementIndex + 4 + v);
            elementList.add(firstElementIndex + 5 + v);

            elementList.add(firstElementIndex + 5 + v);
            elementList.add(firstElementIndex + 1 + v);
            elementList.add(firstElementIndex + 0 + v);

            elementList.add(firstElementIndex + 0 + v);
            elementList.add(firstElementIndex + 2 + v);
            elementList.add(firstElementIndex + 6 + v);

            elementList.add(firstElementIndex + 6 + v);
            elementList.add(firstElementIndex + 4 + v);
            elementList.add(firstElementIndex + 0 + v);

        }

        return elementList;
    }

    public List<Float> intersectionVertexList(RayCaster rayCaster) {

        List<Float> vertexList = new ArrayList<>();

        float width = 1.0f;
        float zoom = mapZoom;

        for (Ray ray:rayCaster.rays) {
            if (ray.intersectedSomething) {
                float startX = ray.intersectionWall.x;
                float startY = ray.intersectionWall.y;

                addRectangleToVertexList(vertexList, startX, startY, zoom, width, 1.0f, 0.2f, 0.2f, 1.0f);
            }
        }

       /* for (float f: vertexList) {
            System.out.print(f + " ");
        }*/

        return vertexList;
    }
    public List<Integer> intersectionElementList(int intersectionVertexListLength, int firstElementIndex) {

        // every 4 vertexes = 3 items
        // i.e. every 28 floats = 3 items

        List<Integer> elementList = new ArrayList<>();

        for (int i = 0; i < (intersectionVertexListLength / 28); i++) {
            // vertexes in a rectangle
            int v = 4 * i;
            elementList.add(firstElementIndex + 2 + v);
            elementList.add(firstElementIndex + 1 + v);
            elementList.add(firstElementIndex + 0 + v);

            elementList.add(firstElementIndex + 1 + v);
            elementList.add(firstElementIndex + 2 + v);
            elementList.add(firstElementIndex + 3 + v);
        }

        return elementList;
    }

    public List<Float> playerVertexList(Player player) {

        List<Float> vertexList = new ArrayList<>();

        float width = player.collisionBox.size * mapZoom;
        float zoom = mapZoom;

                addRectangleToVertexList(vertexList, player.posX, player.posY, zoom, width, 0.2f, 0.2f, 1.0f, 1.0f);

       /* for (float f: vertexList) {
            System.out.print(f + " ");
        }*/

        return vertexList;
    }

    public List<Integer> playerElementList(int firstElementIndex) {

        // every 4 vertexes = 3 items
        // i.e. every 28 floats = 3 items

        List<Integer> elementList = new ArrayList<>();

        elementList.add(firstElementIndex + 2);
        elementList.add(firstElementIndex + 1);
        elementList.add(firstElementIndex + 0);

        elementList.add(firstElementIndex + 1);
        elementList.add(firstElementIndex + 2);
        elementList.add(firstElementIndex + 3);


        return elementList;
    }


    public void addRectangleToVertexList(List<Float> vertexList, float x, float y, float zoom, float width, float r, float g, float b, float a) {

        vertexList.add(x * zoom - width);              // x
        vertexList.add(y * zoom + width);              // y
        vertexList.add(0.0f);                          // z
        vertexList.add(r);                          // r
        vertexList.add(g);                          // g
        vertexList.add(b);                          // b
        vertexList.add(a);                          // a

        vertexList.add(x * zoom + width);              // x
        vertexList.add(y * zoom + width);              // y
        vertexList.add(0.0f);                          // z
        vertexList.add(r);                          // r
        vertexList.add(g);                          // g
        vertexList.add(b);                          // b
        vertexList.add(a);                          // a

        vertexList.add(x * zoom - width);              // x
        vertexList.add(y * zoom - width);              // y
        vertexList.add(0.0f);                          // z
        vertexList.add(r);                          // r
        vertexList.add(g);                          // g
        vertexList.add(b);                          // b
        vertexList.add(a);                          // a

        vertexList.add(x * zoom + width);              // x
        vertexList.add(y * zoom - width);              // y
        vertexList.add(0.0f);                          // z
        vertexList.add(r);                          // r
        vertexList.add(g);                          // g
        vertexList.add(b);                          // b
        vertexList.add(a);                          // a
    }
}