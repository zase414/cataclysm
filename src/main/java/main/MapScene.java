package main;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import render.RayCaster;
import render.Shader;
import util.CollisionBox;
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
    public float[] vertexArray = {0.0f};
    private int[] elementArray = {0}; // ONLY COUNTER-CLOCKWISE ORDER WORKS
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

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (!KeyListener.isKeyPressed(GLFW_KEY_TAB) && keyWasPressed.get(GLFW_KEY_TAB)) {
            Window.changeScene(1);
            keyWasPressed.put(GLFW_KEY_TAB, false);
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_TAB)) {
            keyWasPressed.put(GLFW_KEY_TAB, true);
        }


        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_ALT)) {

            // show mouse cursor
            glfwSetInputMode(Window.get().glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

            updateMapZoom();

        } else {
            glfwSetInputMode(Window.window.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            rayCaster.rayCount = Math.max(rayCaster.rayCount + (int) MouseListener.getScrollY(), 5);
            System.out.println("Number of rays: " + rayCaster.rayCount);
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
            Vector2d dPos = new Vector2d(0.0, 0.0);
            double strafeMultiplier = 1.0;
            boolean playerIsStrafing = (KeyListener.isKeyPressed(GLFW_KEY_W) || KeyListener.isKeyPressed(GLFW_KEY_S)) && (KeyListener.isKeyPressed(GLFW_KEY_D) || KeyListener.isKeyPressed(GLFW_KEY_A));
            double movementMultipliers = player.speed * dt * strafeMultiplier;

            if (KeyListener.isKeyPressed(GLFW_KEY_W)) {
                dPos.x += movementMultipliers * Math.sin(Math.toRadians(player.viewAngle));
                dPos.y += movementMultipliers * Math.cos(Math.toRadians(player.viewAngle));
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_S)) {
                dPos.x -= movementMultipliers * Math.sin(Math.toRadians(player.viewAngle));
                dPos.y -= movementMultipliers * Math.cos(Math.toRadians(player.viewAngle));
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_A)) {
                dPos.x += movementMultipliers * Math.sin(Math.toRadians(player.viewAngle - 90));
                dPos.y += movementMultipliers * Math.cos(Math.toRadians(player.viewAngle - 90));
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_D)) {
                dPos.x += movementMultipliers * Math.sin(Math.toRadians(player.viewAngle + 90));
                dPos.y += movementMultipliers * Math.cos(Math.toRadians(player.viewAngle + 90));
            }

            if (playerIsStrafing) {
                dPos.x *= Math.sqrt(2) / 2;
                dPos.y *= Math.sqrt(2) / 2;
            }

            player.posX += dPos.x;
            // update coordinates of the collision box
            player.updateCollisionBox();
            if (CollisionBox.checkForCollisionsPlayer(map, player)) {
                //System.out.println("clipping into a wall");
                player.posX -= dPos.x;
            }
            player.posY += dPos.y;
            // update coordinates of the collision box
            player.updateCollisionBox();
            if (CollisionBox.checkForCollisionsPlayer(map, player)) {
                //System.out.println("clipping into a wall");
                player.posY -= dPos.y;
            }
            // update coordinates of the collision box
            player.updateCollisionBox();

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

            // create cast rays vertex and element lists, add them to the final list
            List<Float> rayVertexList = rayVertexList(rayCaster, player);
            List<Integer> rayElementList = rayElementList(rayVertexList.size(), getHighestIndex(elementList) + 1);

            vertexList.addAll(rayVertexList);
            elementList.addAll(rayElementList);

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

        } // graphics
        MouseListener.endFrame();
    }

    public void updateMapZoom() {
        // map camera moving and zooming
        mapZoom = Math.min(Math.max(MouseListener.getScrollY() + mapZoom, minMapZoom), maxMapZoom);

        if (MouseListener.getScrollY() > 0) {
            camera.position.x += (MouseListener.getX() - Window.window.width / 2.0f) * MouseListener.getScrollY() * 0.1f;
            camera.position.y -= (MouseListener.getY() - Window.window.height / 2.0f) * MouseListener.getScrollY() * 0.1f;
        } else {
            camera.position.x += (MouseListener.getScrollY() * 0.1f) * camera.position.x;
            camera.position.y += (MouseListener.getScrollY() * 0.1f) * camera.position.y;
        }
    }

    public List<Float> wallVertexList(Map map) {
        List<Float> vertexList = new ArrayList<>();

        float width = 1.0f;
        float zoom = mapZoom;

        for (Wall wall:map.walls) {
            addSquareVertexes(vertexList, wall.x1, wall.y1, 1.0f, zoom, width, wall.r, wall.g, wall.b, wall.a);
            addSquareVertexes(vertexList, wall.x2, wall.y2, 1.0f, zoom, width, wall.r, wall.g, wall.b, wall.a);
        }

        return vertexList;
    }
    public List<Integer> wallElementList(int wallVertexListLength, int firstElementIndex) {
        List<Integer> elementList = new ArrayList<>();

        // for every 2 squares = edges of wall
        for (int i = 0; i < (wallVertexListLength / (vertexVariables * 8)); i++) {
            addQuadBeamElements(elementList, firstElementIndex, i);
        }
        return elementList;
    }
    public List<Float> intersectionVertexList(RayCaster rayCaster) {
        List<Float> vertexList = new ArrayList<>();
        List<List<Ray>> chainList = divideRays(rayCaster.rays);
        float width = 1.0f;
        float zoom = mapZoom;

        for (List<Ray> sameWallRays : chainList) {
            Ray startRay = sameWallRays.get(0);
            Ray endRay = sameWallRays.get(sameWallRays.size() - 1);
            float startX = startRay.firstIntersection.x;
            float startY = startRay.firstIntersection.y;
            float endX = endRay.firstIntersection.x;
            float endY = endRay.firstIntersection.y;

            addSquareVertexes(vertexList, startX, startY, 9.0f, zoom, width, 0.9f, 0.9f, 0.0f, 1.0f);
            addSquareVertexes(vertexList, endX, endY, 9.0f, zoom, width, 0.9f, 0.9f, 0.0f, 1.0f);
        }
        return vertexList;
    }
    public List<Integer> intersectionElementList(int intersectionVertexListLength, int firstElementIndex) {
        List<Integer> elementList = new ArrayList<>();
        // every 28 floats = 6 values
        for (int i = 0; i < (intersectionVertexListLength / (vertexVariables * 8)); i++) {
            addQuadBeamElements(elementList, firstElementIndex, i);
        }
        return elementList;
    }
    public List<Float> playerVertexList(Player player) {
        List<Float> vertexList = new ArrayList<>();

        float size = 1.0f;
        float zoom = mapZoom;
        float z = 10.0f;
        addPlayerShapeVertexes(vertexList, player.posX, player.posY, z, player.viewAngle, zoom, size, 0.9f, 0.9f, 0.0f, 1.0f);
        return vertexList;
    }
    public List<Integer> playerElementList(int firstElementIndex) {
        List<Integer> elementList = new ArrayList<>();

        addQuadShapeElements(elementList, firstElementIndex, 0);

        return elementList;
    }
    public List<Float> rayVertexList(RayCaster rayCaster, Player player) {

        List<Float> vertexList = new ArrayList<>();
        float width = 0.2f;
        float zoom = mapZoom;

        for (Ray ray : rayCaster.rays) {
            if (ray.intersectedAnything) {
                float rayX = ray.firstIntersection.x;
                float rayY = ray.firstIntersection.y;

                addSquareVertexes(vertexList, player.posX, player.posY, 0.0f, zoom, width, 0.4f, 0.4f, 0.0f, 1.0f);
                addSquareVertexes(vertexList, rayX, rayY, 0.0f, zoom, width,0.4f, 0.4f, 0.0f, 1.0f);
            }
        }
        return vertexList;
    }
    public List<Integer> rayElementList(int rayVertexListLength, int firstElementIndex) {
        List<Integer> elementList = new ArrayList<>();
        for (int i = 0; i < (rayVertexListLength / (vertexVariables * 8)); i++) {
            addQuadBeamElements(elementList, firstElementIndex, i);
        }
        return elementList;
    }
}