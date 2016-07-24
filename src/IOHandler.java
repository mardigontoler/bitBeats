
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.jsfml.audio.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class IOHandler {

    public static Sound loadToNewBuffer(Path _path){
        SoundBuffer sbf = new SoundBuffer();
        try {
            sbf.loadFromFile(_path);
        }
        catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Error: One of audio samples could not be loaded.",
                    "Error",JOptionPane.ERROR_MESSAGE);
        }

        return new Sound(sbf);

    }


    public static String getCWD(){
        String cwd = System.getProperty("user.dir");
        return cwd;
    }




    public static String loadText(Path _path){
        String content = "";
        try {
            FileReader fr = new FileReader(_path.toAbsolutePath().normalize().toString());
            BufferedReader br = new BufferedReader(fr);
            String x = "";
            while((x = br.readLine()) != null){
                content += x + "\n";
            }

            br.close();
            fr.close();

        }
        catch(IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Error: Couldn't find the README file for help....",
                    "Error",JOptionPane.ERROR_MESSAGE);
        }
        return content;
    }


    public static Path makeRelativePath(String first, String... places){
        String cwd = Paths.get(".").toAbsolutePath().normalize().toString();
        Path path = Paths.get(first, places);
        System.out.println(path.toAbsolutePath().normalize().toString());
        return path;
    }



    public static void savePathmap(HashMap<Integer, String> soundpathMap) {
        Path p = makeRelativePath("data", "samplepaths.json");
        File f = new File(p.toUri());
        try {
            FileWriter file = new FileWriter(f, false);
            JSONObject job = new JSONObject();
            for (Map.Entry<Integer, String> entry : soundpathMap.entrySet()) {
                job.put(entry.getKey(), entry.getValue());
            }
            file.write(job.toJSONString());
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void loadNewSampleToNumber(int number, HashMap<Integer, SoundPool> soundMap, HashMap<Integer, String> soundpathMap) {
        if (TechDrums.getGuiPlayStatus() == TechDrums.PLAYING) {
            JOptionPane.showMessageDialog(null, "Please stop the sequence first.", "Sorry," +
                    " I can't let you do that ...", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser jfc = new JFileChooser(IOHandler.getCWD());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV, OGG, and MP3 files", "mp3", "wav", "ogg");
        jfc.setFileFilter(filter);
        jfc.changeToParentDirectory();
        int v = jfc.showDialog(null, "Load this sample");

        File f;
        if (v == JFileChooser.APPROVE_OPTION) {
            f = jfc.getSelectedFile();
            try {
                String path = f.getAbsolutePath();
                SoundPool sp = new SoundPool(Paths.get(path));
                soundMap.put(number, sp);
                soundpathMap.put(number, path);
                IOHandler.savePathmap(soundpathMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public static void loadSamplesFromPathMap(HashMap<Integer, SoundPool> soundMap, HashMap<Integer, String> soundpathMap) {
        Path p = IOHandler.makeRelativePath("data", "samplepaths.json");
        File f = new File(p.toUri());
        String s = "";
        try {
            String temp;
            BufferedReader reader = new BufferedReader(new FileReader(f));
            while (true) {
                temp = reader.readLine();
                if (temp == null) break;
                s += temp;
            }
            reader.close();
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(s);
                JSONObject job = (JSONObject) obj;
                HashMap<Integer, String> tempMap = new HashMap<>(job);
                soundpathMap = tempMap;
                for (Map.Entry<Integer, String> entry : soundpathMap.entrySet()) {
                    String val = entry.getValue().replace("\\", "/");
                    Path path = Paths.get(val);
                    int key = Integer.parseInt(String.valueOf(entry.getKey()));
                    soundMap.put(key, new SoundPool(path));
                }

            } catch (ParseException v) {
                v.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("ERROR : Couldn't read the record of previous paths for the samples.");

        }
        for (Map.Entry<Integer, SoundPool> entry : soundMap.entrySet()) {
            System.out.println(entry.getKey() + "  :  " + entry.getValue());
        }
    }


    // JSON - ifies the entire sequence
    public static void saveSequenceState(Sequence seq){
        // first iterate through the sections, and then iterate through subdivisions, constructing a JSON object
        JSONObject job = new JSONObject();

        for (int i = 0; i < seq.getSections().size(); i++) {
            JSONObject microjob = new JSONObject();
            for (int j = 0; j < seq.getSections().get(i).getNumMeasures(); j++) {
                JSONObject atomicjob = new JSONObject();
                for(int k = 0; k < seq.getNumSubdivisions(); k++) {
                    atomicjob.put(k ,seq.getSections().get(i).getSubs().get(k + (j * seq.getNumSubdivisions())).getToggledMap());
                }
                microjob.put(j,atomicjob);
            }
            job.put(i,microjob);
        }

        // then write it to a file with the file chooser
        JFileChooser jfc = new JFileChooser(IOHandler.getCWD());
        int status = jfc.showSaveDialog(null);
        if(status == JFileChooser.APPROVE_OPTION){
            File file = jfc.getSelectedFile();
            if(file.exists()){
                if(JOptionPane.showConfirmDialog(null,"That file already exists.\nAre you sure you want to " +
                        "overwrite it?", "Careful!", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION){
                    return;
                }
            }
            try{
                FileWriter fw = new FileWriter(file, false);
                fw.write(job.toJSONString());
                fw.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static Sequence loadSequenceState(TechDrums guiReference){

        JFileChooser jfc = new JFileChooser(IOHandler.getCWD());
        int status =  jfc.showOpenDialog(null);
        int numSubdivisions = 0;

        if(status == JFileChooser.APPROVE_OPTION){
            try {
                File f = jfc.getSelectedFile();
                FileReader reader = new FileReader(f);
                JSONParser parser = new JSONParser();
                JSONObject root = (JSONObject) parser.parse(reader);

                Vector<Section> secVector = new Vector<>();

                int numSections = root.size();
                Vector<Subdivision> subVector = new Vector<>();

                for(int i = 0; i < numSections; i ++){

                    JSONObject jsonsection = (JSONObject)root.get(""+i);
                    int numMeasures = jsonsection.size();

                    for(int j = 0; j < numMeasures; j++){

                        JSONObject jsonmeasure = (JSONObject)jsonsection.get(""+j);
                        numSubdivisions = jsonmeasure.size();

                        for(int k = 0; k < numSubdivisions; k++){

                            JSONObject jsonsub = (JSONObject)jsonmeasure.get(""+k);
                            HashMap<Integer, Boolean> currentsubmap = new HashMap<>();
                            System.out.println(jsonsub == null);
                            // now iterate through all the keys and values in the json object and add them to a hashmap
                            for(Object entry : jsonsub.entrySet()){
                                int key = Integer.valueOf(((Map.Entry<String, Boolean>)entry).getKey());
                                boolean val = ((Map.Entry<String, Boolean>)entry).getValue();
                                currentsubmap.put(key, val);

                            }

                            subVector.add(new Subdivision(currentsubmap));

                        }

                    }
                    secVector.add(new Section(subVector, numMeasures, numSubdivisions));
                    subVector = new Vector<>();
                }

                Sequence seq = new Sequence(guiReference, numSubdivisions, secVector);
                return  seq;

            }

            catch(FileNotFoundException fnf){
                fnf.printStackTrace();
                JOptionPane.showMessageDialog(null,"There was a problem opening that file. Sorry.");
                return null;
            }
            catch(IOException ioe){
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(null,"There was a problem opening that file. Sorry.");
                return null;
            }
            catch(ParseException pe){
                pe.printStackTrace();
                JOptionPane.showMessageDialog(null, "That was not a valid sequence file for this software. Sorry.",
                        "Error",JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }

        return null; // the actual sequence would have only been returned if there were no errors

    }

    public static void printSequence(Sequence seq){
        for(Section s: seq.getSections()){
            for(Subdivision sub : s.getSubs()){
                for(Map.Entry<Integer, Boolean> entry : sub.getToggledMap().entrySet()){
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
                System.out.println("________________");
            }
        }
    }

}
