package main;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import render.RayCaster;
import render.Shader;
import util.Color;
import util.Ray;
import util.Time;
import util.Wall;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static main.KeyListener.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL20.*;
import static render.RayCaster.divideRays;

public class MenuScene extends Scene{

    private Shader defaultShader;
    private float[] vertexArray = {};
    private int[] elementArray = {};
    private int vaoID, vboID, eboID;
    RayCaster rayCaster;
    Map map;
    List<Map> maps = new ArrayList<>();
    int currentMapIndex;
    Player player;
    Map.MapConverter mapConverter;
    public static MenuScene instance = null;
    public static MenuScene get() {
        if (MenuScene.instance == null) {
            MenuScene.instance = new MenuScene();
            instance.init();
        }
        return MenuScene.instance;
    }
    public static String[] readMapFiles() {
        File mapFolder = new File("assets/maps");
        File[] mapFiles = mapFolder.listFiles();
        assert mapFiles != null;
        String[] filepaths = new String[mapFiles.length];
        for (int i = 0; i < filepaths.length; i++) {
            filepaths[i] = mapFiles[i].getAbsolutePath();
        }
        return filepaths;
    }
    public void initPlayer() {
        player = new Player(map);
        player.posY -= 70f;
        player.posZ += 40f;
    }
    @Override
    public void init() {
        // init the map list
        for (String s : readMapFiles()) {
            Map map = new Map(s);
            map.compile();
            maps.add(map);
            System.out.println(s);
        }

        // initialize the map converter
        mapConverter = new Map.MapConverter();

        // initialize the first map
        map = maps.get(0);
        map.compile();

        currentMapIndex = 0;

        // initialize the rayCaster
        rayCaster = new RayCaster(Settings.fov, Settings.renderDistance, Settings.rayCount, Settings.fadeOutDistance);

        // initialize the player
        initPlayer();

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
    public void update(double dt) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        handleInputEvents();

        rayCaster.cast(player, map);

        buildGraphicsArrays();

        sendGPUDataAndUnbind();

        MouseListener.endFrame();
    }
    private void handleInputEvents() {
        if (isKeyReleased(GLFW_KEY_TAB)) {

            if (maps.size() - currentMapIndex > 1) {
                currentMapIndex++;
            } else {
                currentMapIndex = 0;
            }
            map = maps.get(currentMapIndex);
            MainScene.get().map = map;
            MainScene.get().player = new Player(map);
            MapScene.get().map = map;
            MapScene.get().player = new Player(map);

            System.out.println("map " + (currentMapIndex + 1) + "/" + maps.size());
        }

        if (isKeyReleased(GLFW_KEY_ENTER)) mapConverter.run();

        if (isKeyReleased(GLFW_KEY_SPACE)) Window.changeScene(1);

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
        List<Float> skyVertexList = skyVertexList();
        List<Integer> skyElementList = skyElementList(getHighestIndex(elementList) + 1);

        vertexList.addAll(skyVertexList);
        elementList.addAll(skyElementList);

        // create wall related vertex and element lists, add them to the final list
        List<Float> wallVertexList = wallVertexList(rayCaster);
        List<Integer> wallElementList = wallElementList(wallVertexList.size(), getHighestIndex(elementList) + 1);

        vertexList.addAll(wallVertexList);
        elementList.addAll(wallElementList);

        // create map preview related vertex and element lists, add them to the final list
        List<Float> wallVertexListTopView = wallVertexListTopView(map);
        List<Integer> wallElementListTopView = wallElementListTopView(wallVertexList.size(), getHighestIndex(elementList) + 1);

        vertexList.addAll(wallVertexListTopView);
        elementList.addAll(wallElementListTopView);


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
        glDrawElements(GL_QUADS, this.elementArray.length, GL_UNSIGNED_INT, 0);

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

                addVertex(vertexList, xl, -ys + ys * startRay.intersectedWalls.get(depth).topHeight - ys * player.posZ, 1.0f/(depth + 1), rs, gs, bs, as, 0, 0);
                addVertex(vertexList, xr, -ye + ye * endRay.intersectedWalls.get(depth).topHeight - ye * player.posZ, 1.0f/(depth + 1), re, ge, be, ae, 0, 1);
                addVertex(vertexList, xl, -ys + ys * startRay.intersectedWalls.get(depth).botHeight - ys * player.posZ, 1.0f/(depth + 1), rs, gs, bs, as, 0, 2);
                addVertex(vertexList, xr, -ye + ye * endRay.intersectedWalls.get(depth).botHeight - ye * player.posZ, 1.0f/(depth + 1), re, ge, be, ae ,0, 3);
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
    public List<Float> skyVertexList() {
        List<Float> skyVertexList = new ArrayList<>();
        float y = Window.get().height / 2.0f;
        float xl = -(Window.get().width / 2.0f);
        float xr = (Window.get().width / 2.0f);
        float r = map.skyColor.r, g = map.skyColor.g, b = map.skyColor.b, a = map.skyColor.a;

        addVertex(skyVertexList, xl, y, 0.0f, r, g, b, a, 1, 0); // top left
        addVertex(skyVertexList, xr, y, 0.0f, r, g, b, a, 1, 1); // top right
        addVertex(skyVertexList, xl, 0.0f, 0.0f, r - 0.2f, g - 0.2f, b - 0.2f, a, 1, 2); // bottom left
        addVertex(skyVertexList, xr, 0.0f, 0.0f, r - 0.2f, g - 0.2f, b - 0.2f, a, 1, 3); // bottom right
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
        addVertex(groundVertexList, xl, 0.0f, 0.0f, Math.max(r - 0.2f, 0.0f), Math.max(g - 0.2f, 0.0f), Math.max(b - 0.2f, 0.0f), a, 2, 0); // top left
        addVertex(groundVertexList, xr, 0.0f, 0.0f, Math.max(r - 0.2f, 0.0f), Math.max(g - 0.2f, 0.0f), Math.max(b - 0.2f, 0.0f), a, 2, 1); // top right
        addVertex(groundVertexList, xl, -y, 0.0f, r, g, b, a, 2, 2); // bottom left
        addVertex(groundVertexList, xr, -y, 0.0f, r, g, b, a , 2, 3); // bottom right
        return groundVertexList;
    }
    public List<Integer> groundElementList(int firstElementIndex) {
        List<Integer> groundElementList = new ArrayList<>();
        addQuadShapeElements(groundElementList, firstElementIndex, 0);
        return groundElementList;
    }
    public List<Float> wallVertexListTopView(Map map) {
        List<Float> vertexList = new ArrayList<>();

        float width = 1.0f;
        float zoom = 3.0f;

        for (Wall wall:map.walls) {
            float offsetX = 0.0f;
            float offsetY = 50.0f;
            addSquareVertexes(vertexList, wall.x1 + offsetX, wall.y1 + offsetY, 1.0f, zoom, width, wall.color.r, wall.color.g, wall.color.b, wall.color.a);
            addSquareVertexes(vertexList, wall.x2 + offsetX, wall.y2 + offsetY, 1.0f, zoom, width, wall.color.r, wall.color.g, wall.color.b, wall.color.a);
        }
        return vertexList;
    }
    public List<Integer> wallElementListTopView(int wallVertexListLength, int firstElementIndex) {
        List<Integer> elementList = new ArrayList<>();

        // for every 2 squares = edges of wall
        for (int i = 0; i < (wallVertexListLength / (vertexVariables * 8)); i++) {
            addQuadBeamElements(elementList, firstElementIndex, i);
        }
        return elementList;
    }
}