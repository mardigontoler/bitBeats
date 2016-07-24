import java.util.Iterator;
import java.util.Vector;

public class Sequence {

    public static final int MAXNUMSECTIONS = 10;

    private Vector<Section> sections;
    private int numSections = 1;
    private int tempo = 120;
    private Metronome metro;
    private int numSubdivisions;
    private Section currentSection;
    private Iterator<Section> secIter;
    private TechDrums gui;

    private boolean nextSectionQueued = false;

    public Sequence(TechDrums _gui, int numSubs){
        metro = new Metronome(this);
        gui = _gui;
        numSubdivisions = numSubs;
        sections = new Vector<>();
        sections.add(new Section(numSubdivisions));
        metro.start();
        secIter = sections.iterator();
        currentSection = secIter.next();
    }

    public Sequence(TechDrums _gui, int numSubs, Vector<Section> _sections){
        metro = new Metronome(this);
        gui = _gui;
        numSubdivisions = numSubs;
        sections = _sections;
        numSections = _sections.size();
        metro.start();
        secIter = sections.iterator();
        currentSection = secIter.next();
    }

    // dummy constructor for testing
    public Sequence(){

    }


    public void setNewSubdivisions(int _num){
        numSubdivisions = _num;
        Vector<Section> newSections = new Vector<>();
        for(Section s:sections){
            newSections.add(new Section(_num));
        }
        sections = newSections;
        currentSection = sections.get(0);
        secIter = sections.iterator();
    }

    public int getNumSubdivisions(){
        return numSubdivisions;
    }

    public void addSectionAfter(int index) throws Exception{
        numSections ++;
        sections.add(new Section(numSubdivisions));
        if(numSections > MAXNUMSECTIONS ){
            throw new Exception("HEY!!! There was a big problem adding or removing sections." +
                    "\nThe button events were not dealt with properly...");
        }
    }

    public void removeSection(int index) throws Exception{
        numSections --;
        sections.remove(index);
        if(numSections < 1){
            throw new Exception("HEY!!! There was a big problem adding or removing sections." +
                    "\nThe button events were not dealt with properly...");
        }
    }

    public void appendMeasure(){
        currentSection.appendMeasure(numSubdivisions);
    }

    public void removeMeasure(){
        currentSection.removeMeasure(numSubdivisions);
    }

    public void setTempo(int _tempo){
        tempo = _tempo;
        metro.setTempo(_tempo);
    }

    public Vector<Section> getSections(){
        return sections;
    }

    public void triggerNext() {
        if (gui != null) {
            gui.updateMisc(); // updates things like the status label and whatever shows whether a

            metro.setSubDivisionsPerMeasure(numSubdivisions);
            if (TechDrums.getGuiPlayStatus() == TechDrums.PLAYING) {
                if (currentSection.onLast()) {
                    System.out.println("on last");
                    currentSection.triggerNext();
                    if (nextSectionQueued) {
                        if (secIter.hasNext()) {
                            currentSection = secIter.next();
                        } else {
                            secIter = sections.iterator();
                            currentSection = secIter.next();
                        }
                        nextSectionQueued = false;
                    }
                } else { // the current section isn't on its last subdivisions
                    currentSection.triggerNext();
                }
            }

            if (TechDrums.getGuiPlayStatus() == TechDrums.STOPPED) {

            }
        }
    }



    public void changeCurrentIndex(int ind){
        currentSection = sections.get(ind);
        secIter = sections.iterator();
        while(true){
            if(secIter.next().equals(currentSection)){
                break;
            }
        }
        currentSection.rewind();
    }

    public void toggleQueue(){
        nextSectionQueued = (!nextSectionQueued &&
                TechDrums.getGuiPlayStatus() == TechDrums.PLAYING);
    }

    public int getCurrentSectionIndex(){
        for(int i = 0; i < numSections; i ++){
            if(sections.get(i) == currentSection){
                return i;
            }
        }
        return -1;
    }

    public Section getCurrentSection(){
        return currentSection;
    }

    public boolean getNextSectionQueued(){
        return nextSectionQueued;
    }

    public void rewindCurrentSection(){
        currentSection.rewind();
    }




    public int getNumSections(){
        return numSections;
    }

    public void endMetronome(){
        metro.signalStop();
    }



}
