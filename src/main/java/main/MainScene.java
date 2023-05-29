package main;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import render.RayCaster;
import render.Shader;
import util.CollisionBox;
import util.Ray;
import util.Time;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL20.*;

public class MainScene extends Scene{
    public float[] vertexArray = {1.0f};
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
        keyWasPressed.put(GLFW_KEY_TAB, false);

        // initialize the map
        map = new Map("C:\\Users\\zas\\IdeaProjects\\cataclysm\\assets\\maps\\testmap.json");
        map.compile();

        // initialize the rayCaster
        rayCaster = new RayCaster(Settings.fov, Settings.renderDistance, Settings.rayCount, Settings.fadeOutDistance);

        // initialize the player
        player = new Player(map);
        player.collisionBox.size = 0.5f;

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

        rayCaster.rayCount = Math.max(rayCaster.rayCount + (int) MouseListener.getScrollY(), 5);
        System.out.println("Number of rays: " + rayCaster.rayCount);
        try {
            if (KeyListener.isKeyPressed(GLFW_KEY_TAB)) {
                keyWasPressed.replace(GLFW_KEY_TAB, true);
            } else if (keyWasPressed.get(GLFW_KEY_TAB)) {
                Window.changeScene(2);
                keyWasPressed.replace(GLFW_KEY_TAB, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "key has not been initialized in the HashMap";
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_ALT)) {
            glfwSetInputMode(Window.get().glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else {
            glfwSetInputMode(Window.window.glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }

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
            player.updateCollisionBox();
            if (CollisionBox.checkForCollisionsPlayer(map, player)) {
                //System.out.println("clipping into a wall");
                player.posX -= playerDX;
            }
            player.posY += playerDY;
            // update coordinates of the collision box
            player.updateCollisionBox();
            if (CollisionBox.checkForCollisionsPlayer(map, player)) {
                //System.out.println("clipping into a wall");
                player.posY -= playerDY;
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

            // create ground related vertex and element lists, add them to the final list
            List<Float> groundVertexList = groundVertexList(map);
            List<Integer> groundElementList = groundElementList(getHighestIndex(elementList));

            vertexList.addAll(groundVertexList);
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

    public List<Float> wallVertexList(RayCaster rayCaster) {

        List<Float> vertexList = new ArrayList<>();
        List<List<Ray>> chainList = divideRays(rayCaster.rays);

        for (List<Ray> rayList : chainList) {
            Ray startRay = rayList.get(0);
            Ray endRay = rayList.get(rayList.size()-1);
            float startDistance = startRay.distanceToWall;
            float endDistance = endRay.distanceToWall;
            int i = startRay.id;
            int di = endRay.id - startRay.id + 1;

            float screenPortion = (Window.get().width / (float) rayCaster.rayCount);
            float ys = (float) (Window.get().height) / (startDistance); // y coordinate of start
            float ye = (float) (Window.get().height) / (endDistance);
            float xl = (float) i * screenPortion - Window.get().width / 2.0f;
            float xr = (i + di) * screenPortion - Window.get().width / 2.0f;
            float rs = startRay.r, gs = startRay.g, bs = startRay.b, as = startRay.a;
            float re = endRay.r, ge = endRay.g, be = endRay.b, ae = endRay.a;

            addVertex(vertexList, xl, ys, 1/startDistance, rs, gs, bs, as);
            addVertex(vertexList, xr, ye, 1/endDistance, re, ge, be, ae);
            addVertex(vertexList, xl, -ys, 1/startDistance, rs, gs, bs, as);
            addVertex(vertexList, xr, -ye, 1/endDistance, re, ge, be, ae);
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