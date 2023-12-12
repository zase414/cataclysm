package main;

import java.util.Collections;
import java.util.List;

public abstract class Scene {
    static int texturePoolSize = 4;
    protected Camera camera;
    int positionsSize = 3;
    int colorSize = 4;
    int uvSize = 2;
    int vertexVariables = colorSize + positionsSize + uvSize;
    int floatSizeBytes = Float.BYTES;
    public Scene() {

    }
    public void init() {

    }
    public abstract void update(float dt);
    public int getHighestIndex(List<Integer> elementList) {
        int index = 0;
        if (!elementList.isEmpty()) index = Collections.max(elementList);
        return index;
    }
    public static void addVertex(List<Float> vertexList, float x, float y, float z, float r, float g, float b, float a, int texture, int texPos) {

        float texX;
        float texY = 0.0f;

        /* texPos:
                0----1
                |   /|
                |  / |
                | /  |
                |/   |
                2----3
        */

        switch (texPos) {
            case 0 -> texX = ( (float) texture) / texturePoolSize;
            case 1 -> texX = ( (float) (texture + 1)) / texturePoolSize;
            case 2 -> {
                texX = ( (float) texture) / texturePoolSize;
                texY = 1.0f;
            }
            case 3 -> {
                texX = ( (float) (texture + 1)) / texturePoolSize;
                texY = 1.0f;
            }
            default -> throw new IllegalStateException("Unexpected texture position on vertex: " + texPos);
        }
        for (float v : new float[]{x,y,z,r,g,b,a,texX,texY}) {
            vertexList.add(v);
        }
    }

    public static void addWallVertices(List<Float> vertexList, float x, float y, float z, float r, float g, float b, float a, int texture, int texPos, float minT, float maxT) {

        float texX = 0.0f;
        float texY = 0.0f;

        /* texPos:
                0----1
                |   /|
                |  / |
                | /  |
                |/   |
                2----3
        */

        switch (texPos) {
            case 0 -> texX = ((float) texture + minT) / texturePoolSize;
            case 1 -> texX = ((float) texture + maxT) / texturePoolSize;
            case 2 -> {
                texX = ((float) texture + minT) / texturePoolSize;
                texY = 1.0f;
            }
            case 3 -> {
                texX = ((float) texture + maxT) / texturePoolSize;
                texY = 1.0f;
            }

        }
        for (float v : new float[]{x, y, z, r, g, b, a, texX, texY}) {
            vertexList.add(v);
        }
    }
    public static void addSquareVertices(List<Float> vertexList, float x, float y, float z, float zoom, float size, float r, float g, float b, float a) {
        addVertex(vertexList, x * zoom - size, y * zoom - size, z, r, g, b, a, -1, 0);
        addVertex(vertexList, x * zoom - size, y * zoom + size, z, r, g, b, a, -1, 0);
        addVertex(vertexList, x * zoom + size, y * zoom - size, z, r, g, b, a, -1, 0);
        addVertex(vertexList, x * zoom + size, y * zoom + size, z, r, g, b, a, -1, 0);
    }
    public static void addQuadShapeElements(List<Integer> elementList, int firstElementIndex, int i) {
        /*
                0----1
                |   /|
                |  / |
                | /  |
                |/   |
                2----3
        */
        int k = i * 4;
        for (int v : new int[]{0,2,3,1}) {
            elementList.add(firstElementIndex + v + k);
        }
    }
    public static void addPlayerShapeVertexes(List<Float> vertexList, float x, float y, float z, float angle, float zoom, float size, float r, float g, float b, float a) {
        float angleRad = (float) Math.toRadians(angle);
        double dAngle = 2.2;
        float leftX =  (x + size * (float) Math.sin(angleRad - dAngle)) * zoom;
        float leftY =  (y + size * (float) Math.cos(angleRad - dAngle)) * zoom;
        float tipX =   (x + size * (float) Math.sin(angleRad))          * zoom;
        float tipY =   (y + size * (float) Math.cos(angleRad))          * zoom;
        float rightX = (x + size * (float) Math.sin(angleRad + dAngle)) * zoom;
        float rightY = (y + size * (float) Math.cos(angleRad + dAngle)) * zoom;
        float centerX = x * zoom;
        float centerY = y * zoom;
        addVertex(vertexList, leftX,   leftY,   z, r, g, b, a, -1, 0);
        addVertex(vertexList, tipX,    tipY,    z, r, g, b, a, -1, 0);
        addVertex(vertexList, centerX, centerY, z, r, g, b, a, -1, 0);
        addVertex(vertexList, rightX,  rightY,  z, r, g, b, a, -1, 0);
    }
    public static void addQuadBeamElements(List<Integer> elementList, int firstElementIndex, int i) {
        /*
                0----1---------4----5
                |    |         |    |
                |    |         |    |
                |    |         |    |
                |    |         |    |
                2----3---------6----7
        */
        int k = 8 * i;
        for (int v : new int[]{1,2,6,5,0,3,7,4}) {
            elementList.add(firstElementIndex + v + k);
        }
    }
}
