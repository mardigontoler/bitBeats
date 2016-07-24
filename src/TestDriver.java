import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Mardi on 4/7/2016.
 */
public class TestDriver {

    public static void main(String[] args){
        testIOHandler();
    }

    public static void testMetronome(int tempo, int subdivisions){

        Sequence seq = new Sequence();
        Metronome metro = new Metronome(seq);
        metro.setTempo(tempo);
        metro.setSubDivisionsPerMeasure(subdivisions);
        metro.start();

    }

    public static void testIOHandler(){
        HashMap<Integer, Boolean> hm1 = new HashMap<>();
        HashMap<Integer, Boolean> hm2 = new HashMap<>();
        assert(hm1.equals(hm2));

        hm1.put(1,true);

        Vector<Section> sections = new Vector<>();

        Vector<Subdivision> subs = new Vector<>();
        subs.add(new Subdivision(hm1));

        sections.add(new Section(subs, 1));

        Sequence dummy = new Sequence(null,1,sections);
        IOHandler.saveSequenceState(dummy);
        Sequence n = IOHandler.loadSequenceState(null);
        hm2 = n.getSections().get(0).getSubs().get(0).getToggledMap();

        assert(hm1.equals(hm2));

    }

}
