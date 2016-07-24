import org.jsfml.audio.Sound;

public class Metronome extends Thread {

    // This class should be responsible for keeping consistent time
    // and making sure everything plays correctly

    private float tempo = 120;
    private boolean alive;
    private boolean playingTest = false;
    private long previousMetroTime = System.currentTimeMillis();
    private long currentMetroTime = previousMetroTime;
    private long previousTime = System.currentTimeMillis();
    private long currentTime = previousTime;
    private int subDivisionsPerMeasure = 16;
    private int numerator = 4;
    private Sequence seq;


    private Sound testSound = IOHandler.loadToNewBuffer(IOHandler.makeRelativePath("data","text1.wav"));

    public Metronome(Sequence _seq){
        seq = _seq;
        alive = true;
    }

    public void run(){
        while(alive){
            currentMetroTime = System.currentTimeMillis();
            if(currentMetroTime - previousMetroTime >= ((60)*(1/tempo))*1000){
                if(playingTest) {
                    testSound.play();
                }
                previousMetroTime = currentMetroTime;
            }

            currentTime = System.currentTimeMillis();

            if(currentTime - previousTime >= calculateDuration()){
                seq.triggerNext();
                previousTime = currentTime;


            }

        }
    }

    public void setTempo(int _tempo){
        tempo = (float)_tempo;
    }

    public void toggleTestSound(){
        playingTest = !playingTest; // invert
    }

    public void setSubDivisionsPerMeasure(int _subdivisions){
        subDivisionsPerMeasure = _subdivisions;
    }

    public void setNumerator(int _num){
        numerator = _num;
    }

    public float calculateDuration(){
        float a  = (60000*(float)numerator)/((float)subDivisionsPerMeasure*tempo);
        return a;
    }

    public void signalStop(){
        alive = false;
    }

}
