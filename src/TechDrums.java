import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class TechDrums implements KeyListener {
    private JPanel guipanel;
    private JButton btnLoad;
    private JButton btnSave;
    private JButton btnHelp;
    private JSlider volumeSlider;
    private JButton btnPlay;
    private JButton btnStop;
    private JButton btnSample1;
    private JButton btnSample2;
    private JButton btnSample3;
    private JButton btnSample4;
    private JButton btnSample5;
    private JButton btnSample6;
    private JRadioButton rad1;
    private JRadioButton rad2;
    private JRadioButton rad3;
    private JRadioButton rad4;
    private JRadioButton rad5;
    private JTabbedPane sectionTabs;
    private JButton btnAddSection;
    private JPanel defaultTab;
    private JButton btnrRemoveSection;
    private JComboBox tempoBox;
    private JButton btnColor;
    private JButton btnAddMeasure;
    private JButton btnRemoveMeasure;
    private JComboBox<Integer> subdivisionsBox;
    private JButton btnNew;
    private JRadioButton rad6;
    private JLabel jlStatus;
    private JLabel jlQueuedIndicator;

    private int currentSelectedSampleNum;


    private JFrame root;
    private Sequence seq;

    public static final int PLAYING = 1;
    public static final int STOPPED = 0;
    private static int guiPlayStatus = STOPPED;

    private static final int TEMPOINTERVAL = 5;

    private static HashMap<Integer, SoundPool> soundMap = new HashMap();
    private static HashMap<Integer, String> soundpathMap = new HashMap<>();

    public TechDrums(JFrame _root) {


        // standard values for regular time
        subdivisionsBox.addItem(4);
        subdivisionsBox.addItem(8);
        subdivisionsBox.addItem(16);
        subdivisionsBox.addItem(32);
        subdivisionsBox.addItem(12);
        subdivisionsBox.addItem(24);

        //soundMap.put(2,new SoundPool(Paths.get("C:\\Users\Mardi\\51es\audio\\fx\laserPush.wav"));
        //soundMap.put(2,new SoundPool(Paths.get("C:/Users/Mardi/51/res/audio/fx/laserPush.wav")));

        IOHandler.loadSamplesFromPathMap(soundMap, soundpathMap);

        defaultTab = new JPanel();
        root = _root;
        seq = new Sequence(this, (Integer) subdivisionsBox.getSelectedItem());

        guipanel.addKeyListener(this);
        System.out.println(guipanel.isFocusable());

        btnLoad.addActionListener(e -> {
            if (TechDrums.getGuiPlayStatus() == TechDrums.PLAYING) {
                JOptionPane.showMessageDialog(null, "Please stop the sequence first.", "Sorry," +
                        " I can't let you do that ...", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Sequence newseq = IOHandler.loadSequenceState(this);
            if (newseq != null) {
                seq.endMetronome();
                seq = newseq;
            }

            resetGui();
            seq.rewindCurrentSection();


        });

        btnSave.addActionListener(e -> {
            IOHandler.saveSequenceState(seq);
        });

        btnHelp.addActionListener(e -> {
            JFrame help = new JFrame("Help Me");
            help.setContentPane(new HelpPopup().helppanel);
            help.pack();
            help.setResizable(true);
            help.setVisible(true);

        });

        btnColor.addActionListener(e -> {
            JFrame col = new JFrame("Choose your background color");
            col.setContentPane(new BGChooser(_root, col).bgpanel);
            col.pack();
            col.setVisible(true);
        });

        volumeSlider.addChangeListener(e -> {
            System.out.println("Changing the volume to :" + volumeSlider.getValue());
            for (Map.Entry<Integer, SoundPool> x : soundMap.entrySet()) {
                x.getValue().setVolume(volumeSlider.getValue());
            }


        });

        btnAddSection.addActionListener(e -> {
            if (TechDrums.getGuiPlayStatus() == TechDrums.PLAYING) {
                JOptionPane.showMessageDialog(null, "Please stop the sequence first.", "Sorry," +
                        " I can't let you do that ...", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int num = seq.getNumSections();
            int selectedIndex = sectionTabs.getSelectedIndex();
            try {
                if (num < Sequence.MAXNUMSECTIONS) {
                    seq.addSectionAfter(selectedIndex);
                    JPanel jp = makePopulatedPanel(seq.getNumSubdivisions());
                    sectionTabs.add(jp, selectedIndex + 1);
                    renameSections();
                    sectionTabs.setSelectedIndex(selectedIndex + 1);
                }
            } catch (Exception b) {
                b.printStackTrace();
                System.exit(1);
            }
        });

        btnrRemoveSection.addActionListener(e -> {
            if (TechDrums.getGuiPlayStatus() == TechDrums.PLAYING) {
                JOptionPane.showMessageDialog(null, "Please stop the sequence first.", "Sorry," +
                        " I can't let you do that ...", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int selectedIndex = sectionTabs.getSelectedIndex();
            int num = seq.getNumSections();
            try {
                if (num > 1) {
                    seq.removeSection(selectedIndex);
                    sectionTabs.remove(selectedIndex);
                    renameSections();
                }
            } catch (Exception b) {
                b.printStackTrace();
                System.exit(1);
            }
        });


        btnAddMeasure.addActionListener(e -> {
            if (TechDrums.getGuiPlayStatus() == TechDrums.PLAYING) {
                JOptionPane.showMessageDialog(null, "Please stop the sequence first.", "Sorry," +
                        " I can't let you do that ...", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Section sec = seq.getCurrentSection();
            if (sec.getNumMeasures() < 10) {
                addMeasure();
                refreshButtons();
            }
        });

        btnRemoveMeasure.addActionListener(e -> {
            if (TechDrums.getGuiPlayStatus() == TechDrums.PLAYING) {
                JOptionPane.showMessageDialog(null, "Please stop the sequence first.", "Sorry," +
                        " I can't let you do that ...", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Section sec = seq.getCurrentSection();
            if (sec.getNumMeasures() > 1) {
                removeMeasure();
                refreshButtons();
            }
        });


        tempoBox.addItemListener(e -> {
            if (TechDrums.getGuiPlayStatus() == TechDrums.PLAYING) {
                JOptionPane.showMessageDialog(null, "Please stop the sequence first.", "Sorry," +
                        " I can't let you do that ...", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            seq.setTempo((Integer) (tempoBox.getSelectedItem()));
        });

        tempoBox.addItem(120);
        tempoBox.setSelectedIndex(0);
        for (int i = 250; i > TEMPOINTERVAL; i -= TEMPOINTERVAL) {
            tempoBox.addItem(i);
        }

        btnPlay.addActionListener(e -> {
            //seq.rebuild();

            guiPlayStatus = PLAYING;
        });

        btnStop.addActionListener(e -> {
            guiPlayStatus = STOPPED;
            seq.rewindCurrentSection();
            //seq.rebuild();
        });

        rad1.addActionListener(e -> {
            selectNewSampleNum(1);
        });

        rad2.addActionListener(e -> {
            selectNewSampleNum(2);
        });

        rad3.addActionListener(e -> {
            selectNewSampleNum(3);
        });

        rad4.addActionListener(e -> {
            selectNewSampleNum(4);
        });

        rad5.addActionListener(e -> {
            selectNewSampleNum(5);
        });



        btnSample1.addActionListener(e -> {
            IOHandler.loadNewSampleToNumber(1, soundMap, soundpathMap);
        });

        btnSample2.addActionListener(e -> {
            IOHandler.loadNewSampleToNumber(2, soundMap, soundpathMap);
        });

        btnSample3.addActionListener(e -> {
            IOHandler.loadNewSampleToNumber(3, soundMap, soundpathMap);
        });

        btnSample4.addActionListener(e -> {
            IOHandler.loadNewSampleToNumber(4, soundMap, soundpathMap);
        });

        btnSample5.addActionListener(e -> {
            IOHandler.loadNewSampleToNumber(5, soundMap, soundpathMap);
        });


        sectionTabs.addChangeListener(e -> {
            int i = sectionTabs.getSelectedIndex();
            if (guiPlayStatus == STOPPED && sectionTabs.getTabCount() > 0 && i >= 0) {
                seq.changeCurrentIndex(i);
                refreshButtons();
            }

        });


        subdivisionsBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {

                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int confirmed = JOptionPane.showConfirmDialog(null, "Are you sure you want to change " +
                            " the subdivisions for this sequence?\n\nThis will erase all current data and you will have to start" +
                            " from scratch..", "CHANGING SUBDIVISIONS", JOptionPane.YES_NO_OPTION);

                    if (confirmed == JOptionPane.OK_OPTION) {
                        setNewSubdivisions((Integer) subdivisionsBox.getSelectedItem());
                    }

                }
            }
        });

        addMeasure();


    }


    public void resetGui() {
        // remove all the current tabs
        sectionTabs.removeAll();

        // iterate through every section in the sequence, adding a newly created tab for each
        for (int i = 0; i < seq.getSections().size(); i++) {
            Section s = seq.getSections().get(i);

            // create a panel for the section
            JPanel jp = makePopulatedPanel(seq.getNumSubdivisions(), s.getNumMeasures());

            // add that panel to a new tab
            sectionTabs.add(jp);
        }

        // is everything named and labelled correctly now?
        renameSections();
        refreshButtons();
    }


    public void selectNewSampleNum(int _num) {
        currentSelectedSampleNum = _num;
        try {
            soundMap.get(_num).play();
        } catch (NullPointerException e) {
            // it's fine if that sound can't be played. it might just be unset
        }
    }

    public void addMeasure() {
        int selectedInd = sectionTabs.getSelectedIndex();
        Section sec = seq.getSections().get(selectedInd);
        sec.setNumMeasures(sec.getNumMeasures() + 1);
        seq.appendMeasure();

        JPanel jp = makePopulatedPanel(seq.getNumSubdivisions(), sec.getNumMeasures());
        jp.validate();
        sectionTabs.setComponentAt(sectionTabs.getSelectedIndex(), jp);
    }

    public void removeMeasure() {
        int selectedInd = sectionTabs.getSelectedIndex();
        Section sec = seq.getSections().get(selectedInd);
        sec.setNumMeasures(sec.getNumMeasures() - 1);
        seq.removeMeasure();
        JPanel jp = makePopulatedPanel(seq.getNumSubdivisions(), sec.getNumMeasures());
        jp.validate();
        sectionTabs.setComponentAt(sectionTabs.getSelectedIndex(), jp);

    }


    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("BitBeats");
        frame.setContentPane(new TechDrums(frame).guipanel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(true);
        frame.setVisible(true);

    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        seq.toggleQueue();

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void renameSections() {
        for (int i = 0; i < sectionTabs.getTabCount(); i++) {
            sectionTabs.setTitleAt(i, "Section " + (i + 1));
        }
    }

    public void setNewSubdivisions(int _num) {
        seq.setNewSubdivisions(_num);
        for (int i = 0; i < sectionTabs.getTabCount(); i++) {
            JPanel jp = makePopulatedPanel(_num);
            sectionTabs.setComponentAt(i, jp);
            seq.getSections().get(i).setNumMeasures(1);
        }
        sectionTabs.validate();
    }

    public JPanel makePopulatedPanel(int _num, int... args) {
        JPanel jp;
        JButton btn;
        int measures = 1;
        if (args.length >= 1) {
            measures = args[0];
        }
        jp = new JPanel(new GridLayout(measures, _num));
        for (int j = 0; j < _num * measures; j++) {
            btn = new JButton("");

            btn.setFocusable(false);
            btn.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    respondToSequence(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

            });


            jp.add(btn);
            jp.validate();

        }

        return jp;

    }

    public static void playSample(int _sampleNumber) {
        soundMap.get(_sampleNumber).play();
    }

    public void respondToSequence(MouseEvent e) {
        JButton source = (JButton) e.getSource();
        int selectedInd = sectionTabs.getSelectedIndex();
        JPanel current = (JPanel) (sectionTabs.getSelectedComponent());
        Section sec = seq.getSections().get(selectedInd);
        Vector<Subdivision> sds = sec.getSubs();

        // figure out which subdivision corresponds with the clicked button
        int subIndex = 0;
        for (int i = 0; i < seq.getNumSubdivisions() * sec.getNumMeasures(); i++) {
            if (current.getComponent(i).equals(e.getSource())) {
                subIndex = i;
            }
        }

        if (currentSelectedSampleNum != 0) {
            //TODO: toggle the numbers on and off, and then send the list of current numbers to the actual subdivision
            HashMap<Integer, Boolean> subMap = sds.get(subIndex).getToggledMap();
            subMap.put(currentSelectedSampleNum, !subMap.get(currentSelectedSampleNum));
            String modifiedText = "";
            for (Map.Entry<Integer, Boolean> x : subMap.entrySet()) {
                if (x.getValue() == true) {
                    modifiedText += x.getKey();
                } else {
                    modifiedText += " ";
                }
            }
            source.setBorder(null);
            source.setText(modifiedText);
        }
    }

    public void refreshButtons() {
        int selectedInd = sectionTabs.getSelectedIndex();
        JPanel current = (JPanel) (sectionTabs.getSelectedComponent());
        Section sec = seq.getSections().get(selectedInd);
        Vector<Subdivision> sds = sec.getSubs();

        for (int i = 0; i < sds.size(); i++) {
            String modifiedText = "";
            HashMap<Integer, Boolean> subMap = sds.get(i).getToggledMap();
            for (Map.Entry<Integer, Boolean> x : subMap.entrySet()) {
                if (x.getValue()) {
                    modifiedText += x.getKey();
                } else {
                    modifiedText += " ";
                }
            }
            JButton jb = (JButton) current.getComponent(i);
            jb.setBorder(null);
            jb.setText(modifiedText);
        }
    }


    public static int getGuiPlayStatus() {
        return guiPlayStatus;
    }


    public void updateMisc() {
        btnPlay.setEnabled(guiPlayStatus != PLAYING); // enabled if the sequence is playing, disabled if not
        if (guiPlayStatus == PLAYING) {
            jlStatus.setText("Playing Section : " + (seq.getCurrentSectionIndex() + 1));
        } else {
            jlStatus.setText("");
        }

        if (seq.getNextSectionQueued()) {
            jlQueuedIndicator.setText("NEXT SECTION QUEUED!");
        } else {
            jlQueuedIndicator.setText("");
        }

    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        guipanel = new JPanel();
        guipanel.setLayout(new GridBagLayout());
        guipanel.setFocusable(true);
        guipanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
        final JPanel spacer1 = new JPanel();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.ipady = 50;
        guipanel.add(spacer1, gbc);
        btnSample4 = new JButton();
        btnSample4.setFocusable(false);
        btnSample4.setText("Sample 4");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnSample4, gbc);
        btnSample5 = new JButton();
        btnSample5.setFocusable(false);
        btnSample5.setText("Sample 5");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnSample5, gbc);
        btnSample1 = new JButton();
        btnSample1.setFocusable(false);
        btnSample1.setText("Sample 1");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnSample1, gbc);
        btnSample2 = new JButton();
        btnSample2.setFocusable(false);
        btnSample2.setText("Sample 2");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnSample2, gbc);
        btnSample3 = new JButton();
        btnSample3.setFocusable(false);
        btnSample3.setText("Sample 3");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnSample3, gbc);
        rad1 = new JRadioButton();
        rad1.setFocusable(false);
        rad1.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        guipanel.add(rad1, gbc);
        rad2 = new JRadioButton();
        rad2.setFocusable(false);
        rad2.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        guipanel.add(rad2, gbc);
        rad3 = new JRadioButton();
        rad3.setFocusable(false);
        rad3.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        guipanel.add(rad3, gbc);
        rad4 = new JRadioButton();
        rad4.setFocusable(false);
        rad4.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        guipanel.add(rad4, gbc);
        rad5 = new JRadioButton();
        rad5.setFocusable(false);
        rad5.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        guipanel.add(rad5, gbc);
        volumeSlider = new JSlider();
        volumeSlider.setAutoscrolls(false);
        volumeSlider.setExtent(0);
        volumeSlider.setFocusable(false);
        volumeSlider.setInverted(false);
        volumeSlider.setMajorTickSpacing(20);
        volumeSlider.setMaximumSize(new Dimension(75, 1000));
        volumeSlider.setMinimumSize(new Dimension(25, 200));
        volumeSlider.setMinorTickSpacing(10);
        volumeSlider.setOrientation(1);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setToolTipText("Volume");
        volumeSlider.setValue(100);
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 200;
        gbc.insets = new Insets(10, 10, 50, 10);
        guipanel.add(volumeSlider, gbc);
        sectionTabs = new JTabbedPane();
        sectionTabs.setFocusable(false);
        sectionTabs.setName("section1");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 13;
        gbc.fill = GridBagConstraints.BOTH;
        guipanel.add(sectionTabs, gbc);
        defaultTab = new JPanel();
        defaultTab.setLayout(new BorderLayout(0, 0));
        sectionTabs.addTab("Section 1", defaultTab);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 14;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 25;
        guipanel.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 3;
        gbc.gridwidth = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 450;
        guipanel.add(spacer3, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("       Volume");
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 50;
        gbc.ipady = 20;
        gbc.insets = new Insets(25, 0, 0, 0);
        guipanel.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Tempo (bpm):");
        gbc = new GridBagConstraints();
        gbc.gridx = 12;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.ipadx = 100;
        guipanel.add(label2, gbc);
        tempoBox = new JComboBox();
        tempoBox.setFocusable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(tempoBox, gbc);
        btnColor = new JButton();
        btnColor.setFocusable(false);
        btnColor.setText("Change Color");
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnColor, gbc);
        btnPlay = new JButton();
        btnPlay.setFocusable(false);
        btnPlay.setText("Play");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnPlay, gbc);
        btnStop = new JButton();
        btnStop.setFocusable(false);
        btnStop.setText("Stop");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnStop, gbc);
        btnAddSection = new JButton();
        btnAddSection.setFocusable(false);
        btnAddSection.setText("Add Section");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnAddSection, gbc);
        btnrRemoveSection = new JButton();
        btnrRemoveSection.setFocusable(false);
        btnrRemoveSection.setText("Remove Section");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnrRemoveSection, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 50;
        guipanel.add(spacer4, gbc);
        btnAddMeasure = new JButton();
        btnAddMeasure.setFocusable(false);
        btnAddMeasure.setText("Add Measure");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnAddMeasure, gbc);
        btnRemoveMeasure = new JButton();
        btnRemoveMeasure.setFocusable(false);
        btnRemoveMeasure.setText("Remove Measure");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnRemoveMeasure, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 25;
        guipanel.add(spacer5, gbc);
        final JLabel label3 = new JLabel();
        label3.setFocusable(false);
        label3.setHorizontalAlignment(4);
        label3.setHorizontalTextPosition(4);
        label3.setText("Subdivisions : ");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.ipadx = 150;
        guipanel.add(label3, gbc);
        subdivisionsBox = new JComboBox();
        subdivisionsBox.setFocusable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 50;
        guipanel.add(subdivisionsBox, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 11;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        guipanel.add(spacer6, gbc);
        btnHelp = new JButton();
        btnHelp.setFocusable(false);
        btnHelp.setText("Help");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnHelp, gbc);
        btnSave = new JButton();
        btnSave.setEnabled(true);
        btnSave.setFocusable(false);
        btnSave.setText("Save");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnSave, gbc);
        btnLoad = new JButton();
        btnLoad.setEnabled(true);
        btnLoad.setFocusable(false);
        btnLoad.setText("Load");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnLoad, gbc);
        btnNew = new JButton();
        btnNew.setEnabled(false);
        btnNew.setText("New");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        guipanel.add(btnNew, gbc);
        jlStatus = new JLabel();
        jlStatus.setText("Stopped");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        guipanel.add(jlStatus, gbc);
        jlQueuedIndicator = new JLabel();
        jlQueuedIndicator.setText("Queued :");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        guipanel.add(jlQueuedIndicator, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(rad1);
        buttonGroup.add(rad2);
        buttonGroup.add(rad3);
        buttonGroup.add(rad4);
        buttonGroup.add(rad5);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return guipanel;
    }
}