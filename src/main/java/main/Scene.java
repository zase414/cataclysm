package main;

import java.util.Collections;
import java.util.List;

public abstract class Scene {
    protected Camera camera;
    int positionsSize = 3;
    int colorSize = 4;
    int vertexVariables = colorSize + positionsSize;
    int floatSizeBytes = 4;
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
    public static void addVertex(List<Float> vertexList, float x, float y, float z, float r, float g, float b, float a) {
        for (float v : new float[]{x,y,z,r,g,b,a}) {
            vertexList.add(v);
        }
    }
    public static void addSquareVertexes(List<Float> vertexList, float x, float y, float z, float zoom, float size, float r, float g, float b, float a) {
        for (float v1 :     new float[]{x * zoom - size, x * zoom + size}) {
            for (float v2 : new float[]{y * zoom - size, y * zoom + size}) {
                addVertex(vertexList, v1, v2, z, r, g, b, a);
            }
        }
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
        for (int v : new int[]{0,2,1,1,2,3}) {
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
        addVertex(vertexList, leftX,   leftY,   z, r, g, b, a);
        addVertex(vertexList, tipX,    tipY,    z, r, g, b, a);
        addVertex(vertexList, centerX, centerY, z, r, g, b, a);
        addVertex(vertexList, rightX,  rightY,  z, r, g, b, a);
    }
    public static void addQuadBeamElements(List<Integer> elementList, int firstElementIndex, int i) {
        /*
                0----1---------4----5
                |   /|         |   /|
                |  / |         |  / |
                | /  |         | /  |
                |/   |         |/   |
                2----3---------6----7
        */
        int k = 8 * i;
        for (int v : new int[]{2,1,0,1,2,3,  6,5,4,5,6,7,  0,4,5,5,1,0,  0,2,6,6,4,0,  2,6,7,7,3,2,  3,7,5,5,1,3}) {
            elementList.add(firstElementIndex + v + k);
        }
    }
}
