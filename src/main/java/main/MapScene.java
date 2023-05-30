package main;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import render.RayCaster;
import render.Shader;
import util.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL20.*;
import static render.RayCaster.divideRays;

public class MapScene extends Scene{
    public float[] vertexArray = {0.0f};
    private int[] elementArray = {0}; // ONLY COUNTER-CLOCKWISE ORDER WORKS
    private int vaoID, vboID, eboID;
    private Shader defaultShader;
    RayCaster rayCaster;
    Map map;
    Player player;
    int positionsSize = 3, colorSize = 4, floatSizeBytes = 4;
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

        handleInputEvents();

        player.updateViewAngle();
        player.updatePlayerMovementVector(dt);
        player.updatePlayerPos(map);

        rayCaster.cast(player, map);
        rayCaster.updateMapVisibility();

        buildGraphicsArrays();

        sendGPUDataAndUnbind();

        MouseListener.endFrame();
    }
    private void handleInputEvents() {

        // scene switch, TAB related stuff
        try {
            if (KeyListener.isKeyPressed(GLFW_KEY_TAB)) {
                keyWasPressed.replace(GLFW_KEY_TAB, true);
            } else if (keyWasPressed.get(GLFW_KEY_TAB)) {
                Window.changeScene(1);
                keyWasPressed.replace(GLFW_KEY_TAB, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "key has not been initialized in the HashMap";
        }

        // mouse cursor state & ALT-related stuff
        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_ALT)) {
            // show mouse cursor
            glfwSetInputMode(Window.get().glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            cameraMouseControl();
        } else {
            glfwSetInputMode(Window.window.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            rayCaster.adjustRayCount();
            if (Settings.cameraFollowsPlayer) {
                cameraFollowsPlayer();
            }
        }
    }
        private void cameraMouseControl() {
            float scrollSensitivity = 0.1f;
            // map camera moving and zooming
            mapZoom = Math.min(Math.max(MouseListener.getScrollY() + mapZoom, minMapZoom), maxMapZoom);
            if (MouseListener.getScrollY() > 0) {
                camera.position.x += (MouseListener.getX() - Window.window.width / 2.0f) * MouseListener.getScrollY() * scrollSensitivity;
                camera.position.y -= (MouseListener.getY() - Window.window.height / 2.0f) * MouseListener.getScrollY() * scrollSensitivity;
            } else {
                camera.position.x += (MouseListener.getScrollY() * scrollSensitivity) * camera.position.x;
                camera.position.y += (MouseListener.getScrollY() * scrollSensitivity) * camera.position.y;
            }
        }
        private void cameraFollowsPlayer() {
            Vector2d dPos = player.movementVector;
            camera.position.x += dPos.x * mapZoom;
            camera.position.y += dPos.y * mapZoom;
        }
    private void buildGraphicsArrays() {
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
    }
    private void sendGPUDataAndUnbind() {
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
    }
    // =======================
    // LISTS OF VERTEXES AND ELEMENTS
    // =======================
    public List<Float> wallVertexList(Map map) {
        List<Float> vertexList = new ArrayList<>();

        float width = 1.0f;
        float zoom = mapZoom;

        if (Settings.mapAlwaysVisible) {
            for (Wall wall:map.walls) {
                addSquareVertexes(vertexList, wall.x1, wall.y1, 1.0f, zoom, width, wall.r, wall.g, wall.b, wall.a);
                addSquareVertexes(vertexList, wall.x2, wall.y2, 1.0f, zoom, width, wall.r, wall.g, wall.b, wall.a);
            }
        } else {
            for (Wall wall:map.walls) {
                System.out.println("min: " + wall.minVisibleT + " max: " + wall.maxVisibleT);
                float x1, x2, y1, y2;
                if (wall.minVisibleT != 2 && wall.maxVisibleT != -2) {
                    x1 = wall.coordinatesFromT(wall.minVisibleT).x;
                    y1 = wall.coordinatesFromT(wall.minVisibleT).y;
                    x2 = wall.coordinatesFromT(wall.maxVisibleT).x;
                    y2 = wall.coordinatesFromT(wall.maxVisibleT).y;
                } else continue;
                addSquareVertexes(vertexList, x1, y1, 1.0f, zoom, width, wall.r, wall.g, wall.b, wall.a);
                addSquareVertexes(vertexList, x2, y2, 1.0f, zoom, width, wall.r, wall.g, wall.b, wall.a);
            }
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