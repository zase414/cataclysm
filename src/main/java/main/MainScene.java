package main;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import render.RayCaster;
import render.Shader;
import render.Texture;
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
    private Shader defaultShader;
    private float[] vertexArray = {};
    private int[] elementArray = {};
    private int vaoID, vboID, eboID;
    private Texture texture;
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

        // import texture
        texture = new Texture("assets/textures/image.png");

        // initialize the map
        map = MenuScene.get().map;
        map.compile();

        // initialize the rayCaster
        rayCaster = new RayCaster(Settings.fov, Settings.renderDistance, Settings.rayCount, Settings.fadeOutDistance);

        // initialize the player
        player = new Player(map);

        // initialize the camera
        camera = new Camera(new Vector2f());

        // import and compile the shaders
        defaultShader = new Shader("assets/shaders/color-based.glsl");
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

        int vertexSizeBytes = (positionsSize + colorSize + uvSize) * floatSizeBytes;

        // position attributes
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        // color attributes
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, (long) positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);

        // texture attributes
        glVertexAttribPointer(2, uvSize, GL_FLOAT, false, vertexSizeBytes, (long) (positionsSize + colorSize) * floatSizeBytes);
        glEnableVertexAttribArray(2);
    }
    @Override
    public void update(float dt) {
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
        if (isKeyReleased(Settings.changeScene)) Window.changeScene(2);

        if (keyBeingPressed(Settings.sprint)) {
            player.speed = 6.0f;
        } else player.speed = 4.0f;

        if ((keyInitialPress(Settings.jump) || queueJump) && (!player.isInAir || Settings.flappyBird)) {
            player.jumpPhase = player.minPhase;
            queueJump = false;
        }

        if (keyInitialPress(Settings.jump) && player.jumpPhase > 0.0f && player.jumpPhase < player.maxPhase) {
            queueJump = true;
        }

        if (keyBeingPressed(Settings.cursor)) {
            glfwSetInputMode(Window.get().glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else {
            glfwSetInputMode(Window.window.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            rayCaster.adjustRayCount();
        }

        if (isKeyReleased(GLFW_KEY_ESCAPE)) Window.changeScene(0);

        updateHeldKeys();
    }
    private void buildGraphicsArrays() {
        List<Integer> elementList = new ArrayList<>();

        // create ground related vertex and element lists, add them to the final list
        List<Float> groundVertexList = groundVertexList();
        List<Integer> groundElementList = groundElementList(getHighestIndex(elementList));

        List<Float> vertexList = new ArrayList<>(groundVertexList);
        elementList.addAll(groundElementList);

        // create sky related vertex and element lists, add them to the final list
        List<Float> skyVertexList = skyVertexList();
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
        // use the shader, upload texture and values
        defaultShader.use();

        defaultShader.uploadTexture("TEX_SAMPLER", 0);
        glActiveTexture(GL_TEXTURE0);
        texture.bind();

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
        int vertexSizeBytes = (positionsSize + colorSize + uvSize) * floatSizeBytes;

        // position attributes
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        // color attributes
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, (long) positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);

        // texture attributes
        glVertexAttribPointer(2, uvSize, GL_FLOAT, false, vertexSizeBytes, (long) (positionsSize + colorSize) * floatSizeBytes);
        glEnableVertexAttribArray(2);

        // draw
        glDrawElements(GL_QUADS, elementArray.length, GL_UNSIGNED_INT, 0);

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

                float minT = startRay.intersectionRelDistanceOnWall.get(depth);
                float maxT = endRay.intersectionRelDistanceOnWall.get(depth);

                int i = startRay.id;
                int di = endRay.id - startRay.id + 1;

                float screenPortion = (Window.get().width / (float) rayCaster.rayCount);
                float ys =  ((float) (Window.get().height) / 2.0f) / (startDistance); // y coordinate of start
                float ye =  ((float) (Window.get().height) / 2.0f) / (endDistance); // y coordinate of end
                float xl = (float) i * screenPortion - Window.get().width / 2.0f;
                float xr = (i + di) *  screenPortion - Window.get().width / 2.0f;

                // for textures
                //Color startColor = new Color().texFade(startDistance);
                //Color endColor = new Color().texFade(endDistance);

                //for colors
                Color startColor = startRay.colors.get(depth).shade(startDistance);
                Color endColor = endRay.colors.get(depth).shade(endDistance);

                float rs = startColor.r, gs = startColor.g, bs = startColor.b, as = startColor.a;
                float re = endColor.r, ge = endColor.g, be = endColor.b, ae = endColor.a;


                /*
                    0----1
                    |    |
                    |    |
                    2----3
                    |    |
                    |    |
                    4----5
                */

                addWallVertices(vertexList, xl, -ys + ys * startRay.intersectedWalls.get(depth).topHeight - ys * player.posZ, 1.0f/(depth + 1), rs, gs, bs, as, 0, 0, minT, maxT);
                addWallVertices(vertexList, xr, -ye + ye * endRay.intersectedWalls.get(depth).topHeight - ye * player.posZ,   1.0f/(depth + 1), re, ge, be, ae, 0, 1, minT, maxT);
                addWallVertices(vertexList, xl, -ys + ys * startRay.intersectedWalls.get(depth).botHeight - ys * player.posZ, 1.0f/(depth + 1), rs, gs, bs, as, 0, 2, minT, maxT);
                addWallVertices(vertexList, xr, -ye + ye * endRay.intersectedWalls.get(depth).botHeight - ye * player.posZ,   1.0f/(depth + 1), re, ge, be, ae, 0, 3, minT, maxT);
            }
        }
        return vertexList;
    }
    public List<Integer> wallElementList(int wallVertexListLength, int firstElementIndex) {
        List<Integer> wallElementList = new ArrayList<>();
        // 1 quad = 4 integers per 4 vertexes in the vertexArray
        for (int i = 0; i < (wallVertexListLength / (vertexVariables * 4)); i++) {
            addQuadShapeElements(wallElementList, firstElementIndex, i);
        }
        return wallElementList;
    }
    public List<Float> skyVertexList() {
        List<Float> skyVertexList = new ArrayList<>();
        float y = Window.get().height / 2.0f;
        float xl = -(Window.get().width / 2.0f);
        float xr = (Window.get().width / 2.0f);

        // for color shader
        float r = map.skyColor.r, g = map.skyColor.g, b = map.skyColor.b, a = map.skyColor.a;

        addVertex(skyVertexList, xl, y,       0.0f, r,g,b,a, 1, 0); // top left
        addVertex(skyVertexList, xr, y,       0.0f, r,g,b,a, 1, 1); // top right
        addVertex(skyVertexList, xl, 0.0f, 0.0f, r,g,b,a, 1, 2); // bottom left
        addVertex(skyVertexList, xr, 0.0f, 0.0f, r,g,b,a, 1, 3); // bottom right
        return skyVertexList;
    }
    public List<Integer> skyElementList(int firstElementIndex) {
        List<Integer> skyElementList = new ArrayList<>();
        addQuadShapeElements(skyElementList, firstElementIndex, 0);
        return skyElementList;
    }
    public List<Float> groundVertexList() {
        List<Float> groundVertexList = new ArrayList<>();

        float y = Window.get().height / 2.0f;
        float xl = -(Window.get().width / 2.0f);
        float xr = (Window.get().width / 2.0f);

        // for color shader
        float r = map.groundColor.r, g = map.groundColor.g, b = map.groundColor.b, a = map.groundColor.a;

        addVertex(groundVertexList, xl, 0.0f, 0.0f, r,g,b,a,  2, 0); // top left
        addVertex(groundVertexList, xr, 0.0f, 0.0f, r,g,b,a, 2, 1); // top right
        addVertex(groundVertexList, xl, -y, 0.0f,      r,g,b,a, 2, 2); // bottom left
        addVertex(groundVertexList, xr, -y, 0.0f,      r,g,b,a, 2, 3); // bottom right
        return groundVertexList;
    }
    public List<Integer> groundElementList(int firstElementIndex) {
        List<Integer> groundElementList = new ArrayList<>();
        addQuadShapeElements(groundElementList, firstElementIndex, 0);
        return groundElementList;
    }
}