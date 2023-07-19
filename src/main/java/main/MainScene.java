package main;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import render.RayCaster;
import render.Shader;
import util.Color;
import util.Ray;
import util.Time;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static main.KeyListener.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL20.*;
import static render.RayCaster.divideRays;

public class MainScene extends Scene{
    public float[] vertexArray = {};
    private int[] elementArray = {};
    private int vaoID, vboID, eboID;
    private Shader defaultShader;
    RayCaster rayCaster;
    Map map;
    Player player;
    public static MainScene instance = null;
    public static MainScene get() {
        if (MainScene.instance == null) {
            MainScene.instance = new MainScene();
            instance.init();
        }
        return MainScene.instance;
    }
    @Override
    public void init() {

        // initialize the map
        map = new Map("assets/maps/conversion_example.json");
        map.compile();

        // initialize the rayCaster
        rayCaster = new RayCaster(Settings.fov, Settings.renderDistance, Settings.rayCount, Settings.fadeOutDistance);

        // initialize the player
        player = new Player(map);
        player.collisionBox.size = 0.2f;

        // initialize the camera
        camera = new Camera(new Vector2f());

        // import and compile the shaders
        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();

        // -------------------------------------------
        // generate VAO, VBO, EBO and send them to GPU
        // -------------------------------------------

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
    public void update(double dt) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        handleInputEvents();

        player.updateViewAngle();
        player.handlePlayerMovement(dt, map);

        rayCaster.cast(player, map);
        rayCaster.updateMapVisibility();

        buildGraphicsArrays();

        sendGPUDataAndUnbind();

        MouseListener.endFrame();
    }
    private boolean queueJump;
    private void handleInputEvents() {
        if (isKeyReleased(GLFW_KEY_TAB)) Window.changeScene(2);

        if (KeyListener.keyBeingPressed(GLFW_KEY_LEFT_SHIFT)) {
            player.speed = 15.0f;
        } else player.speed = 10.0f;

        if ((keyPushed(GLFW_KEY_SPACE) || queueJump) && (!player.isInAir || Settings.flappyBird)) {
            player.jumpPhase = player.minPhase;
            queueJump = false;
        }

        if (keyPushed(GLFW_KEY_SPACE) && player.jumpPhase > 0.0f && player.jumpPhase < player.maxPhase) {
            queueJump = true;
        }

        if (KeyListener.keyBeingPressed(GLFW_KEY_LEFT_ALT)) {
            glfwSetInputMode(Window.get().glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else {
            glfwSetInputMode(Window.window.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            rayCaster.adjustRayCount();
        }

        updateHeldKeys();
    }
    private void buildGraphicsArrays() {
        List<Integer> elementList = new ArrayList<>();

        // create ground related vertex and element lists, add them to the final list
        List<Float> groundVertexList = groundVertexList(map);
        List<Integer> groundElementList = groundElementList(getHighestIndex(elementList));

        List<Float> vertexList = new ArrayList<>(groundVertexList);
        elementList.addAll(groundElementList);

        // create sky related vertex and element lists, add them to the final list
        List<Float> skyVertexList = skyVertexList(map);
        List<Integer> skyElementList = skyElementList(getHighestIndex(elementList) + 1);

        vertexList.addAll(skyVertexList);
        elementList.addAll(skyElementList);

        // create wall related vertex and element lists, add them to the final list
        List<Float> wallVertexList = wallVertexList(rayCaster);
        List<Integer> wallElementList = wallElementList(wallVertexList.size(), getHighestIndex(elementList) + 1);

        vertexList.addAll(wallVertexList);
        elementList.addAll(wallElementList);

        vertexArray = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertexArray[i] = vertexList.get(i);
        }

        elementArray = new int[elementList.size()];
        for (int i = 0; i < elementList.size(); i++) {
            elementArray[i] = elementList.get(i);
        }
    }
    private void sendGPUDataAndUnbind() {
        // use the shader and upload values
        defaultShader.use();
        defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());
        defaultShader.uploadFloat("uTime", (float) Time.getTime());

        // -------------------------------------------
        // generate VAO, VBO, EBO and send them to GPU
        // ------------------------------------------

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
    // ==============================
    // LISTS OF VERTEXES AND ELEMENTS
    // ==============================
    public List<Float> wallVertexList(RayCaster rayCaster) {
        List<Float> vertexList = new ArrayList<>();

        for (int depth = 0; depth < rayCaster.renderDepth; depth++) {
            List<List<Ray>> chainList = divideRays(rayCaster.rays, depth);

            for (List<Ray> rayList : chainList) {
                Ray startRay = rayList.get(0);
                Ray endRay = rayList.get(rayList.size()-1);
                float startDistance = startRay.intersectionDistanceOnRay.get(depth);
                float endDistance = endRay.intersectionDistanceOnRay.get(depth);

                int i = startRay.id;
                int di = endRay.id - startRay.id + 1;

                float screenPortion = (Window.get().width / (float) rayCaster.rayCount);
                float ys =  ((float) (Window.get().height) / 2.0f) / (startDistance); // y coordinate of start
                float ye =  ((float) (Window.get().height) / 2.0f) / (endDistance); // y coordinate of end
                float xl = (float) i * screenPortion - Window.get().width / 2.0f;
                float xr = (i + di) *  screenPortion - Window.get().width / 2.0f;

                Color startColor = startRay.colors.get(depth).shade(startDistance);
                Color endColor = endRay.colors.get(depth).shade(endDistance);

                float rs = startColor.r, gs = startColor.g, bs = startColor.b, as = startColor.a;
                float re = endColor.r, ge = endColor.g, be = endColor.b, ae = endColor.a;

                addVertex(vertexList, xl, -ys + ys * startRay.intersectedWalls.get(depth).topHeight - ys * player.posZ, 1.0f/(depth + 1), rs, gs, bs, as);
                addVertex(vertexList, xr, -ye + ye * endRay.intersectedWalls.get(depth).topHeight - ye * player.posZ, 1.0f/(depth + 1), re, ge, be, ae);
                addVertex(vertexList, xl, -ys + ys * startRay.intersectedWalls.get(depth).botHeight - ys * player.posZ, 1.0f/(depth + 1), rs, gs, bs, as);
                addVertex(vertexList, xr, -ye + ye * endRay.intersectedWalls.get(depth).botHeight - ye * player.posZ, 1.0f/(depth + 1), re, ge, be, ae);
            }
        }
        return vertexList;
    }
    public List<Integer> wallElementList(int wallVertexListLength, int firstElementIndex) {
        List<Integer> wallElementList = new ArrayList<>();
        // 2 triangles = 6 integers per 4 vertexes in the vertexArray
        for (int i = 0; i < (wallVertexListLength / (vertexVariables * 4)); i++) {
            addQuadShapeElements(wallElementList, firstElementIndex, i);
        }
        return wallElementList;
    }
    public List<Float> skyVertexList(Map map) {
        List<Float> skyVertexList = new ArrayList<>();
        float y = Window.get().height / 2.0f;
        float xl = -(Window.get().width / 2.0f);
        float xr = (Window.get().width / 2.0f);
        float r = map.skyColor.r, g = map.skyColor.g, b = map.skyColor.b, a = map.skyColor.a;

        addVertex(skyVertexList, xl, y, 0.0f, r, g, b, a); // top left
        addVertex(skyVertexList, xr, y, 0.0f, r, g, b, a); // top right
        addVertex(skyVertexList, xl, 0.0f, 0.0f, r - 0.2f, g - 0.2f, b - 0.2f, a); // bottom left
        addVertex(skyVertexList, xr, 0.0f, 0.0f, r - 0.2f, g - 0.2f, b - 0.2f, a); // bottom right
        return skyVertexList;
    }
    public List<Integer> skyElementList(int firstElementIndex) {
        List<Integer> skyElementList = new ArrayList<>();
        addQuadShapeElements(skyElementList, firstElementIndex, 0);
        return skyElementList;
    }
    public List<Float> groundVertexList(Map map) {
        List<Float> groundVertexList = new ArrayList<>();

        float y = Window.get().height / 2.0f;
        float xl = -(Window.get().width / 2.0f);
        float xr = (Window.get().width / 2.0f);
        float r = map.groundColor.r, g = map.groundColor.g, b = map.groundColor.b, a = map.groundColor.a;
        addVertex(groundVertexList, xl, 0.0f, 0.0f, Math.max(r - 0.2f, 0.0f), Math.max(g - 0.2f, 0.0f), Math.max(b - 0.2f, 0.0f), a); // top left
        addVertex(groundVertexList, xr, 0.0f, 0.0f, Math.max(r - 0.2f, 0.0f), Math.max(g - 0.2f, 0.0f), Math.max(b - 0.2f, 0.0f), a); // top right
        addVertex(groundVertexList, xl, -y, 0.0f, r, g, b, a); // bottom left
        addVertex(groundVertexList, xr, -y, 0.0f, r, g, b, a); // bottom right
        return groundVertexList;
    }
    public List<Integer> groundElementList(int firstElementIndex) {
        List<Integer> groundElementList = new ArrayList<>();
        addQuadShapeElements(groundElementList, firstElementIndex, 0);
        return groundElementList;
    }
}