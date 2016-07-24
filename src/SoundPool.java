
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.jsfml.audio.*;

public class SoundPool {

    private final int NUMINSTANCES = 5;
    private Vector<Sound> sounds;
    private Iterator<Sound> sounditer;

    public SoundPool(Path _filepath){
        sounds = new Vector<>();

        for(int i = 0; i < NUMINSTANCES; i++){
            sounds.add(IOHandler.loadToNewBuffer(_filepath));
        }
        sounditer = sounds.iterator();
    }

    public void play(){
        Sound nxt;
        try {
            nxt = sounditer.next();
        }
        catch(NoSuchElementException e){
            sounditer = sounds.iterator();
            nxt = sounditer.next();
        }
        nxt.play();
    }

    public void setVolume(float _volume){
        for(Sound s:sounds){
            s.setVolume(_volume);
        }
    }


}