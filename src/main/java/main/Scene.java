package main;

import java.util.Collections;
import java.util.List;

public abstract class Scene {
    boolean canSwitchScene = false;
    protected Camera camera;
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

}
