
import java.util.Iterator;
import java.util.Vector;

public class Section {

    private Vector<Subdivision> subs;
    private int numMeasures = 1;
    private Iterator<Subdivision> subiter;
    private Subdivision nxt;
    private int numSubs;

    public Section(int _numSubs){

        numSubs = _numSubs;
        reset();
    }

    public Section(Vector<Subdivision> _subs, int _numSubs) {
        subs = _subs;
        numSubs = _numSubs;
        subiter = subs.iterator();
    }

    public Section(Vector<Subdivision> _subs, int _numMeasures, int _numSubs) {
        subs = _subs;
        numMeasures = _numMeasures;
        subiter = subs.iterator();
        numSubs = _numSubs;
        //reset();
    }

    public void reset(){
        subs = new Vector<>();
        for(int i = 0; i < numSubs; i++){
            subs.add(new Subdivision());
        }
        subiter = subs.iterator();
    }


    public Vector<Subdivision> getSubs(){
        return subs;
    }


    public void setNumMeasures(int _num){
        numMeasures = _num;

    }

    public int getNumMeasures(){
        return numMeasures;
    }

    public void triggerNext(){
        if (subiter.hasNext()){
            nxt = subiter.next();
            nxt.trigger();
        }
        else{
            subiter = subs.iterator();
            nxt = subiter.next();
            nxt.trigger();
        }
    }

    public boolean onLast() {
        // 2 is used since the iterator goes to the next sound before a sound is played
        return nxt == subs.get(numMeasures * numSubs - 2);
    }


    public void resetIter(){
        subiter = subs.iterator();
    }

    public void appendMeasure(int _subsPerMeasure){
        for(int i = 0; i < _subsPerMeasure; i ++){
            subs.add(new Subdivision());
        }
        subiter = subs.iterator();
    }

    public void removeMeasure(int _subsPerMeasure){
        for(int i = 0; i < _subsPerMeasure; i ++){
            subs.remove(subs.size() - 1 - i);
        }
        subiter  = subs.iterator();
    }

    public void rewind(){
        subiter = subs.iterator();
    }
}
