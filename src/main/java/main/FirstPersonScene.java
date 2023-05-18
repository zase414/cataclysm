package main;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import render.RayCaster;
import render.Shader;
import util.Time;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL20.*;

public class FirstPersonScene extends Scene{

    public float[] vertexArray = {};
    private int[] elementArray = {}; // ONLY COUNTER-CLOCKWISE ORDER WORKS
    private int vaoID, vboID, eboID;
    private Shader defaultShader;
    public FirstPersonScene() { }
    RayCaster rayCaster;
    Map map;
    Player player;
    int positionsSize = 3;
    int colorSize = 4;
    int floatSizeBytes = 4;
    float wallR = 1.0f;
    float wallG = 0.0f;
    float wallB = 0.0f;

    @Override
    public void init() {

        // initialize the rayCaster
        rayCaster = new RayCaster(70,1000,400);

        // initialize the map
        map = new Map("C:\\Users\\zas\\IdeaProjects\\cataclysm\\assets\\maps\\testmap.json");
        map.compile();

        // initialize the player
        player = new Player(map);

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

        player.viewAngle -= MouseListener.getDX();

        {
        if (KeyListener.isKeyPressed(GLFW_KEY_W)) {
            player.posX += player.speed * dt * Math.sin(Math.toRadians(player.viewAngle));
            player.posY += player.speed * dt * Math.cos(Math.toRadians(player.viewAngle));
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_S)) {
            player.posX -= player.speed * dt * Math.sin(Math.toRadians(player.viewAngle));
            player.posY -= player.speed * dt * Math.cos(Math.toRadians(player.viewAngle));
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_A)) {
            player.posX += player.speed * dt * Math.sin(Math.toRadians(player.viewAngle - 90));
            player.posY += player.speed * dt * Math.cos(Math.toRadians(player.viewAngle - 90));
        }
        if (KeyListener.isKeyPressed(GLFW_KEY_D)) {
            player.posX += player.speed * dt * Math.sin(Math.toRadians(player.viewAngle + 90));
            player.posY += player.speed * dt * Math.cos(Math.toRadians(player.viewAngle + 90));
        }
        } // keybinds

        {
        // update the rayCaster
        rayCaster.update(player);

        // get the distance array
        List<Float>[] distances = rayCaster.getDistanceListArray(map, player);

        // update the vertexArray and elementArray using the rayCaster
        List<Float> vertexList = new ArrayList<>();
        List<Integer> elementList = new ArrayList<>();

        // create background related lists
        List<Float> skyVertexList;
        List<Float> groundVertexList;
        List<Integer> skyElementList;
        List<Integer> groundElementList;


        skyVertexList = skyVertexList(map);
        groundVertexList = groundVertexList(map);

        skyElementList = skyElementList(0);
        groundElementList = groundElementList(Collections.max(skyElementList) + 1);

        List<Float> wallVertexList = wallVertexList(distances);
        List<Integer> wallElementList = wallElementList(wallVertexList.size(), Collections.max(groundElementList) + 1);

        vertexList.addAll(skyVertexList);
        elementList.addAll(skyElementList);
        vertexList.addAll(groundVertexList);
        elementList.addAll(groundElementList);
        vertexList.addAll(wallVertexList);
        elementList.addAll(wallElementList);

        this.vertexArray = new float[vertexList.size()];

            for (int i = 0; i < vertexList.size(); i++) {
                vertexArray[i] = vertexList.get(i);
            }

        this.elementArray = new int[elementList.size()];

            for (int i = 0; i < elementList.size(); i++) {
                elementArray[i] = elementList.get(i);
            }

        } // vertex and element array build

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

        MouseListener.endFrame();
    }

    public List<Float> wallVertexList(List<Float>[] distanceList) {

        List<Float> vertexList = new ArrayList<>();

        /*
                0-------1
                |       |
                |       |
                |       |
                |       |
                |       |
                |       |
                |       |
                |       |
                2-------3
        */

        for (int i = 0; i < rayCaster.rayCount; i++) {

            // == if the ray intersected something
            if (!distanceList[i].isEmpty()) {

                // find the smallest distance in the List
                float minDist = distanceList[i].get(0);
                for (float f:distanceList[i]) {
                    if (f < minDist) {
                        minDist = f;
                    }
                }

                float screenPortion = (Window.get().width / (float) rayCaster.rayCount);
                float y = (float) (Window.get().height / 2) / (minDist);
                float xl = (float) i * screenPortion - Window.get().width / 2.0f;
                float xr = (i + 1) * screenPortion - Window.get().width / 2.0f;

                // top left vertex (0)
                vertexList.add(xl);      //x
                vertexList.add(y);       //y
                vertexList.add(0.0f);    //z
                vertexList.add(wallR);   //r
                vertexList.add(wallG);   //g
                vertexList.add(wallB);   //b
                vertexList.add(1.0f);    //a

                // top right vertex (1)
                vertexList.add(xr);      //x
                vertexList.add(y);       //y
                vertexList.add(0.0f);    //z
                vertexList.add(wallR);   //r
                vertexList.add(wallG);   //g
                vertexList.add(wallB);   //b
                vertexList.add(1.0f);    //a

                // bottom left vertex (2)
                vertexList.add(xl);      //x
                vertexList.add(-y);      //y
                vertexList.add(0.0f);    //z
                vertexList.add(wallR);   //r
                vertexList.add(wallG+0.5f);   //g
                vertexList.add(wallB+0.5f);   //b
                vertexList.add(1.0f);    //a

                // bottom right vertex (3)
                vertexList.add(xr);      //x
                vertexList.add(-y);      //y
                vertexList.add(0.0f);    //z
                vertexList.add(wallR);   //r
                vertexList.add(wallG+0.5f);   //g
                vertexList.add(wallB+0.5f);   //b
                vertexList.add(1.0f);    //a
            }
        }


        System.out.print("{");
        for (float f:vertexList) {
            System.out.print(f + "f, ");
        }
        System.out.print("}");

        return vertexList;
    }
    public List<Integer> wallElementList(int wallVertexListLength, int highestPreviousElement) {

        // 2 triangles = 6 integers per 4 vertexes in the vertexArray

        List<Integer> elementList = new ArrayList<>();

        for (int i = 0; i < (wallVertexListLength / 28); i++) {
            // pointer
            int p = 6 * i;
            // element number shift
            int v = 4 * i;
            elementList.add(highestPreviousElement + 0 + v);
            elementList.add(highestPreviousElement + 2 + v);
            elementList.add(highestPreviousElement + 1 + v);
            elementList.add(highestPreviousElement + 1 + v);
            elementList.add(highestPreviousElement + 2 + v);
            elementList.add(highestPreviousElement + 3 + v);
        }
       return elementList;
    }

    public List<Float> skyVertexList(Map map) {
        List<Float> skyVertexList = new ArrayList<>();

        float y = Window.get().height / 2.0f;
        float xl = -(Window.get().width / 2.0f);
        float xr = (Window.get().width / 2.0f);

        // top left
        skyVertexList.add(xl);                //x
        skyVertexList.add(y);                 //y
        skyVertexList.add(0.0f);              //z
        skyVertexList.add(map.skyColor.r);    //r
        skyVertexList.add(map.skyColor.g);    //g
        skyVertexList.add(map.skyColor.b);    //b
        skyVertexList.add(map.skyColor.a);    //a

        // top right
        skyVertexList.add(xr);                //x
        skyVertexList.add(y);                 //y
        skyVertexList.add(0.0f);              //z
        skyVertexList.add(map.skyColor.r);    //r
        skyVertexList.add(map.skyColor.g);    //g
        skyVertexList.add(map.skyColor.b);    //b
        skyVertexList.add(map.skyColor.a);    //a

        // bottom left
        skyVertexList.add(xl);                                       //x
        skyVertexList.add(0.0f);                                     //y
        skyVertexList.add(0.0f);                                     //z
        skyVertexList.add(Math.max(map.skyColor.r - 0.2f, 0.0f));    //r
        skyVertexList.add(Math.max(map.skyColor.g - 0.2f, 0.0f));    //g
        skyVertexList.add(Math.max(map.skyColor.b - 0.2f, 0.0f));    //b
        skyVertexList.add(map.skyColor.r);                           //a

        // bottom right
        skyVertexList.add(xr);                                       //x
        skyVertexList.add(0.0f);                                     //y
        skyVertexList.add(0.0f);                                     //z
        skyVertexList.add(Math.max(map.skyColor.r - 0.2f, 0.0f));    //r
        skyVertexList.add(Math.max(map.skyColor.g - 0.2f, 0.0f));    //g
        skyVertexList.add(Math.max(map.skyColor.b - 0.2f, 0.0f));    //b
        skyVertexList.add(map.skyColor.a);                           //a

        return skyVertexList;
    }

    public List<Integer> skyElementList(int highestPreviousElement) {
        List<Integer> skyElementList = new ArrayList<>();
        skyElementList.add(highestPreviousElement + 0);
        skyElementList.add(highestPreviousElement + 2);
        skyElementList.add(highestPreviousElement + 1);
        skyElementList.add(highestPreviousElement + 1);
        skyElementList.add(highestPreviousElement + 2);
        skyElementList.add(highestPreviousElement + 3);
        return skyElementList;
    }
    public List<Float> groundVertexList(Map map) {
        List<Float> groundVertexList = new ArrayList<>();

        float y = Window.get().height / 2.0f;
        float xl = -(Window.get().width / 2.0f);
        float xr = (Window.get().width / 2.0f);

        // top left
        groundVertexList.add(xl);                                          //x
        groundVertexList.add(0.0f);                                        //y
        groundVertexList.add(0.0f);                                        //z
        groundVertexList.add(Math.max(map.groundColor.r - 0.2f, 0.0f));    //r
        groundVertexList.add(Math.max(map.groundColor.g - 0.2f, 0.0f));    //g
        groundVertexList.add(Math.max(map.groundColor.b - 0.2f, 0.0f));    //b
        groundVertexList.add(map.groundColor.a);                           //a

        // top right
        groundVertexList.add(xr);                                          //x
        groundVertexList.add(0.0f);                                        //y
        groundVertexList.add(0.0f);                                        //z
        groundVertexList.add(Math.max(map.groundColor.r - 0.2f, 0.0f));    //r
        groundVertexList.add(Math.max(map.groundColor.g - 0.2f, 0.0f));    //g
        groundVertexList.add(Math.max(map.groundColor.b - 0.2f, 0.0f));    //b
        groundVertexList.add(map.groundColor.a);    //a

        // bottom left
        groundVertexList.add(xl);                   //x
        groundVertexList.add(-y);                   //y
        groundVertexList.add(0.0f);                 //z
        groundVertexList.add(map.groundColor.r);    //r
        groundVertexList.add(map.groundColor.g );   //g
        groundVertexList.add(map.groundColor.b);    //b
        groundVertexList.add(map.groundColor.r);    //a

        // bottom right
        groundVertexList.add(xr);                   //x
        groundVertexList.add(-y);                   //y
        groundVertexList.add(0.0f);                 //z
        groundVertexList.add(map.groundColor.r);    //r
        groundVertexList.add(map.groundColor.g);    //g
        groundVertexList.add(map.groundColor.b);    //b
        groundVertexList.add(map.groundColor.a);    //a

        return groundVertexList;
    }

    public List<Integer> groundElementList(int highestPreviousElement) {
        List<Integer> skyElementList = new ArrayList<>();
        skyElementList.add(highestPreviousElement + 0);
        skyElementList.add(highestPreviousElement + 2);
        skyElementList.add(highestPreviousElement + 1);
        skyElementList.add(highestPreviousElement + 1);
        skyElementList.add(highestPreviousElement + 2);
        skyElementList.add(highestPreviousElement + 3);
        return skyElementList;
    }
}