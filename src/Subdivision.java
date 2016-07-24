
import java.util.HashMap;
import java.util.Map;

public class Subdivision {

    private static final int MAXSAMPLES = 5;
    private HashMap<Integer, Boolean> toggledMap = new HashMap<>();

    public Subdivision() {
        reset();
    }

    public Subdivision(HashMap<Integer, Boolean> _toggledMap) {
        toggledMap = _toggledMap;
    }


    public void reset() {
        toggledMap.clear();
        for (int i = 1; i <= MAXSAMPLES; i++) {
            toggledMap.put(i, false);
        }
    }

    public HashMap<Integer, Boolean> getToggledMap() {
        return toggledMap;
    }

    public void trigger() {
        int k;
        boolean v;
        for (Map.Entry<Integer, Boolean> e : toggledMap.entrySet()) {
            v = e.getValue();
            if (v == true) {
                try {
                    TechDrums.playSample(e.getKey());
                } catch (Exception b) {
                    System.out.println("Couldn't play sample number " + e.getKey());
                }
            }
        }
    }
}