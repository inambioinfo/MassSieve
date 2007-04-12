/*
 * Experiment.java
 *
 * Created on July 11, 2006, 3:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gov.nih.nimh.mass_sieve.gui;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import gov.nih.nimh.mass_sieve.*;
import gov.nih.nimh.mass_sieve.io.FileInformation;
import gov.nih.nimh.mass_sieve.io.ParseFile;
import gov.nih.nimh.mass_sieve.io.SetLexer;
import gov.nih.nimh.mass_sieve.io.SetParser;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import org.biojavax.bio.seq.RichSequence;
import prefuse.Display;

/**
 *
 * @author slotta
 */
public class ExperimentPanel extends JPanel {
    
    private PeptideCollection pepCollection, pepCollectionOriginal;
    private DefaultTreeModel treeModelClusters, treeModelPeptides, treeModelPeptideHits, treeModelProteins, treeModelParsimony;
    private double omssaCutoff, mascotCutoff, xtandemCutoff;
    private double omssaCutoffOrig, mascotCutoffOrig, xtandemCutoffOrig;
    private String filterText;
    private boolean useIonIdent, useIndeterminates, filterPeptides, filterProteins, filterCoverage;
    private int pHitCutoffCount, peptideCutoffCount, coverageCutoffAmount;
    private ArrayList<File> allFiles;
    private ArrayList<FileInformation> fileInfos;
    private ButtonGroup buttonGroupTreeSource;
    private FilterPreferencesDialog prefDialog;
    private SummaryDialog summaryDialog;
    private JFileChooser jFileChooserLoad;
    private JOptionPane jOptionPaneAbout;
    private JScrollPane jScrollPaneBottom, jScrollPaneLeft;
    private JSplitPane jSplitPaneMain, jSplitPaneSecondary;
    private JToggleButton jToggleButtonClusters, jToggleButtonParsimony, jToggleButtonPeptides, jToggleButtonProteins, jToggleButtonPeptideHits;
    private JTree jTreeMain;
    private MassSieveFrame msFrame;
    private String lowerFrameTitle, upperFrameTitle;
    
    /** Creates a new instance of Experiment */
    public ExperimentPanel(MassSieveFrame frm) {
        msFrame = frm;
        initComponents();
        jFileChooserLoad.setMultiSelectionEnabled(true);
        allFiles = new ArrayList<File>();
        fileInfos = new ArrayList<FileInformation>();
        omssaCutoff = 0.05;
        mascotCutoff = 0.05;
        xtandemCutoff = 0.05;
        omssaCutoffOrig = omssaCutoff;
        mascotCutoffOrig = mascotCutoff;
        xtandemCutoffOrig = xtandemCutoff;
        useIonIdent = true;
        filterText = "o+m+x";
        useIndeterminates = true;
        filterPeptides = false;
        filterProteins = false;
        pHitCutoffCount = 1;
        peptideCutoffCount = 1;
        cleanDisplay();
    }
    
    public void cloneFilterSettings(ExperimentPanel fromExp) {
        this.setOmssaCutoff(fromExp.getOmssaCutoff());
        this.setMascotCutoff(fromExp.getMascotCutoff());
        this.setXtandemCutoff(fromExp.getXtandemCutoff());
        this.setUseIonIdent(fromExp.getUseIonIdent());
        this.setFilterText(fromExp.getFilterText());
        this.setUseIndeterminates(fromExp.getUseIndeterminates());
        this.setFilterPeptides(fromExp.getFilterPeptides());
        this.setFilterProteins(fromExp.getFilterProteins());
        this.setPHitCutoffCount(fromExp.getPHitCutoffCount());
        this.setPeptideCutoffCount(fromExp.getPeptideCutoffCount());
    }
    
    private void cleanDisplay() {
        pepCollectionOriginal = new PeptideCollection();
        
        DefaultMutableTreeNode root=new DefaultMutableTreeNode("No data");
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        treeModelClusters = treeModel;
        treeModelProteins = treeModel;
        treeModelPeptides = treeModel;
        treeModelParsimony = treeModel;
        jTreeMain.setModel(treeModel);
        jTreeMain.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jSplitPaneSecondary.setTopComponent(null);
        jScrollPaneBottom.setViewportView(null);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        buttonGroupTreeSource = new ButtonGroup();
        jFileChooserLoad = new JFileChooser();
        jOptionPaneAbout = new JOptionPane();
        jSplitPaneMain = new JSplitPane();
        jScrollPaneLeft = new JScrollPane();
        jTreeMain = new JTree();
        jSplitPaneSecondary = new JSplitPane();
        jScrollPaneBottom = new JScrollPane();
        jToggleButtonClusters = new JToggleButton("Clusters");
        jToggleButtonPeptides = new JToggleButton("Peptides");
        jToggleButtonPeptideHits = new JToggleButton("Peptide Hits");
        jToggleButtonProteins = new JToggleButton("Proteins");
        jToggleButtonParsimony = new JToggleButton("Parsimony");
        
        jFileChooserLoad.setDialogTitle("Open Files");
        jSplitPaneMain.setBorder(null);
        jSplitPaneMain.setDividerLocation(175);
        jSplitPaneMain.setDividerSize(5);
        jSplitPaneMain.setMinimumSize(new java.awt.Dimension(0, 0));
        jTreeMain.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeMainValueChanged(evt);
            }
        });
        
        jScrollPaneLeft.setViewportView(jTreeMain);
        
        jSplitPaneMain.setLeftComponent(jScrollPaneLeft);
        
        //jSplitPaneSecondary.setDividerLocation(610);
        jSplitPaneSecondary.setDividerLocation(0.6);
        jSplitPaneSecondary.setDividerSize(5);
        jSplitPaneSecondary.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPaneSecondary.setMinimumSize(new java.awt.Dimension(0, 0));
        jSplitPaneSecondary.setBottomComponent(jScrollPaneBottom);
        
        jSplitPaneMain.setRightComponent(jSplitPaneSecondary);
        
        buttonGroupTreeSource.add(jToggleButtonClusters);
        jToggleButtonClusters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonClustersActionPerformed(evt);
            }
        });
        
        buttonGroupTreeSource.add(jToggleButtonPeptides);
        jToggleButtonPeptides.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonPeptidesActionPerformed(evt);
            }
        });
        
        buttonGroupTreeSource.add(jToggleButtonPeptideHits);
        jToggleButtonPeptideHits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonPeptideHitsActionPerformed(evt);
            }
        });
        
        buttonGroupTreeSource.add(jToggleButtonProteins);
        jToggleButtonProteins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonProteinsActionPerformed(evt);
            }
        });
        
        buttonGroupTreeSource.add(jToggleButtonParsimony);
        jToggleButtonParsimony.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonParsimonyActionPerformed(evt);
            }
        });
        
        toolbar.add(new JLabel("View elements by:"));
        toolbar.addSeparator();
        toolbar.add(jToggleButtonPeptideHits);
        toolbar.addSeparator();
        toolbar.add(jToggleButtonPeptides);
        toolbar.addSeparator();
        toolbar.add(jToggleButtonProteins);
        toolbar.addSeparator();
        toolbar.add(jToggleButtonClusters);
        toolbar.addSeparator();
        toolbar.add(jToggleButtonParsimony);
        
        add(toolbar, BorderLayout.NORTH);
        add(jSplitPaneMain, BorderLayout.CENTER);
    }
    
    public void showPreferences() {
        if (prefDialog == null) {
            prefDialog = new FilterPreferencesDialog(this);
        }
        prefDialog.setOmssaCutoff(omssaCutoff);
        prefDialog.setXtandemCutoff(xtandemCutoff);
        prefDialog.setMascotCutoff(mascotCutoff);
        prefDialog.setUseIonIdent(useIonIdent);
        prefDialog.setPepFilterField(filterText);
        prefDialog.setUseIndeterminates(useIndeterminates);
        prefDialog.setFilterPeptides(filterPeptides);
        prefDialog.setPepHitCount(pHitCutoffCount);
        prefDialog.setFilterProteins(filterProteins);
        prefDialog.setPeptideCount(peptideCutoffCount);
        prefDialog.setVisible(true);
    }
    
    public void showSummary() {
        if (summaryDialog == null) {
            summaryDialog = new SummaryDialog(this);
        }
        summaryDialog.setFileInformation(fileInfos);
        summaryDialog.setVisible(true);
    }
    
    private PeptideCollection FilterBySearchProgram(PeptideCollection pc) {
        PeptideCollection result;
        StringReader setDescription = new StringReader(filterText);
        SetLexer lexer = new SetLexer(setDescription);
        SetParser parser = new SetParser(lexer);
        parser.setPeptideCollection(pc);
        try {
            result = parser.expr();
            result.updatePeptideHits();
            return result;
        } catch (TokenStreamException ex) {
            ex.printStackTrace();
        } catch (RecognitionException ex) {
            ex.printStackTrace();
        }
        return new PeptideCollection();
    }
    
    public void recomputeCutoff() {
        PeptideCollection pepFiltered;
        if (useIndeterminates) {
            pepFiltered = pepCollectionOriginal;
        } else {
            pepFiltered = pepCollectionOriginal.getNonIndeterminents();
        }
        pepFiltered = pepFiltered.getCutoffCollection(omssaCutoff, mascotCutoff, xtandemCutoff, useIonIdent);
        pepCollection = FilterBySearchProgram(pepFiltered);
        if (filterPeptides) {
            pepCollection = pepCollection.getPeptidesByHits(pHitCutoffCount);
        }
        pepCollection.createProteinList();
        if (filterProteins) {
            pepCollection = pepCollection.filterByPeptidePerProtein(peptideCutoffCount);
        }
        if (filterCoverage) {
            pepCollection.updateClusters();
            pepCollection = pepCollection.filterByProteinCoverage(coverageCutoffAmount);
        }
        pepCollection.updateClusters();
        
        updateDisplay();
    }
    
    public void addFiles(final File files[]) {
        String exp_name = this.getName();
        for (File f:files) {
            HashSet<String> acceptedProteins = new HashSet<String>();
            allFiles.add(f);
            String filename = f.getName();
            //String filepath = f.getPath();
            //ParseFile pf = new ParseFile(filepath,ExperimentPanel.this);
            ParseFile pf = new ParseFile(f, ExperimentPanel.this);
            for (PeptideHit p:pf.getPeptideHits()) {
                p.setExperiment(exp_name);
                p.setSourceFile(filename);
                boolean usePepHit = false;
                switch (p.getSourceType()) {
                    case MASCOT:
                        if (p.getExpect() < mascotCutoff) usePepHit = true;
                        break;
                    case OMSSA:
                        if (p.getExpect() < omssaCutoff) usePepHit = true;
                        break;
                    case XTANDEM:
                        if (p.getExpect() < xtandemCutoff) usePepHit = true;
                        break;
                }
                if (usePepHit) {
                    pepCollectionOriginal.addPeptideHit(p);
                    acceptedProteins.addAll(p.getProteinNames());
                }
            }
            HashMap<String, RichSequence> pDB = pf.getProteinDB();
            for (String pName:pDB.keySet()) {
                if (acceptedProteins.contains(pName)) {
                    msFrame.addProtein(pName, pDB.get(pName));
                }
            }
            fileInfos.add(pf.getFileInformation());
        }
        recomputeCutoff();
    }
    
    public void addPeptideHits(ArrayList<PeptideHit> pHits) {
        for (PeptideHit p:pHits) {
            pepCollectionOriginal.addPeptideHit(p);
        }
        recomputeCutoff();
    }
    
    public void reloadFiles() {
        if ((omssaCutoff > omssaCutoffOrig) ||
                (mascotCutoff > mascotCutoffOrig) ||
                (xtandemCutoff > xtandemCutoffOrig)) {
            cleanDisplay();
            if (omssaCutoff > omssaCutoffOrig) omssaCutoffOrig = omssaCutoff;
            if (mascotCutoff > mascotCutoffOrig) mascotCutoffOrig = mascotCutoff;
            if (xtandemCutoff > xtandemCutoffOrig) xtandemCutoffOrig = xtandemCutoff;
            new Thread(new Runnable() {
                public void run() {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    File f[] = new File[allFiles.size()];
                    addFiles(allFiles.toArray(f));
                    
                    setCursor(null);
                }
            }).start();
        } else {
            recomputeCutoff();
        }
    }
    
    private void jTreeMainValueChanged(javax.swing.event.TreeSelectionEvent evt) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        jTreeMain.getLastSelectedPathComponent();
        
        if (node == null) return;
        Object nodeInfo = node.getUserObject();
        //System.out.println(nodeInfo.getClass());
        if (nodeInfo instanceof Peptide) {
            Peptide p = (Peptide)nodeInfo;
            PeptideCollection pc = pepCollection.getCluster(p.getCluster());
            Display display = pc.getGraphDisplay(msFrame.getGraphLayout(), this, p.getSequence());
            jSplitPaneSecondary.setTopComponent(display);
            upperFrameTitle = "Cluster " + p.getCluster();
            showPeptide(p);
            jSplitPaneSecondary.setDividerLocation(0.5);
        }
        if (nodeInfo instanceof Protein) {
            Protein p = (Protein)nodeInfo;
            PeptideCollection pc = pepCollection.getCluster(p.getCluster());
            Display display = pc.getGraphDisplay(msFrame.getGraphLayout(), this, p.getName());
            jSplitPaneSecondary.setTopComponent(display);
            upperFrameTitle = "Cluster " + p.getCluster();
            showProtein(p);
        }
        if (nodeInfo instanceof PeptideCollection) {
            PeptideCollection pc = (PeptideCollection)nodeInfo;
            Display display = pc.getGraphDisplay(msFrame.getGraphLayout(), this, null);
            jSplitPaneSecondary.setTopComponent(display);
            upperFrameTitle = "Cluster " + pc.getClusterNum();
            jSplitPaneSecondary.setBottomComponent(null);
            jSplitPaneSecondary.setDividerLocation(0.5);
        }
        if (nodeInfo instanceof ListPanel) {
            ListPanel lp = (ListPanel)nodeInfo;
            jSplitPaneSecondary.setTopComponent(lp.createTable());
            upperFrameTitle = lp.getName();
            jSplitPaneSecondary.setBottomComponent(null);
            jSplitPaneSecondary.setDividerLocation(0.5);
        }
        if (jSplitPaneSecondary.getTopComponent() instanceof Display) {
            new Thread(new Runnable() {
                public void run() {
                    Display display = (Display)jSplitPaneSecondary.getTopComponent();
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {}
                    MouseEvent mEvt = new MouseEvent(display, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis()+5000, MouseEvent.BUTTON2_MASK, 10,10,1,false,MouseEvent.BUTTON2);
                    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(mEvt);
                }
            }).start();
        }
        
    }
    
    public int getDisplayHeight() {
        return jSplitPaneSecondary.getHeight();
    }
    
    public int getDisplayWidth() {
        return jSplitPaneSecondary.getWidth();
    }
    
    public void showPeptide(Peptide p) {
        jSplitPaneSecondary.setBottomComponent(p.getInfoPanel(this));
        jSplitPaneSecondary.setDividerLocation(0.5);
        lowerFrameTitle = "Peptide " + p.getSequence();
    }
    
    public void showProtein(Protein p) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        JPanel seqPanel;
        if (msFrame.getUseDigest()) {
            seqPanel = p.getSequenceDisplay(msFrame.getDigestName(), jSplitPaneSecondary.getWidth());
        } else {
            seqPanel = p.getSequenceDisplay(jSplitPaneSecondary.getWidth());
        }
        ListPanel lp = new ListPanel(this);
        lp.addProteinPeptideHitList(p.getPeptideHitList());
        jPanel.add(seqPanel);
        JScrollPane lps = lp.createTable();
        lps.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jPanel.add(lps);
        jSplitPaneSecondary.setBottomComponent(new JScrollPane(jPanel));
        jSplitPaneSecondary.setDividerLocation(0.5);
        lowerFrameTitle = "Protein " + p.getName();
    }
    
    public void showClusterLower(int i) {
        PeptideCollection pc = pepCollection.getCluster(i);
        jSplitPaneSecondary.setBottomComponent(pc.getGraphDisplay(msFrame.getGraphLayout(), this, null));
        jSplitPaneSecondary.setDividerLocation(0.5);
        lowerFrameTitle = "Cluster " + i + " Graph";
        new Thread(new Runnable() {
            public void run() {
                Display display = (Display)jSplitPaneSecondary.getBottomComponent();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {}
                MouseEvent mEvt = new MouseEvent(display, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis()+5000, MouseEvent.BUTTON2_MASK, 10,10,1,false,MouseEvent.BUTTON2);
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(mEvt);
            }
        }).start();
    }
    
    public void showProteinsLower(HashSet<Protein> pSet) {
        ArrayList<Protein> pList = new ArrayList<Protein>(pSet);
        Collections.sort(pList);
        ListPanel cPanel = new ListPanel(this);
        cPanel.addProteinList(pList, pepCollection.getExperimentSet(), true);
        jSplitPaneSecondary.setBottomComponent(cPanel.createTable());
        jSplitPaneSecondary.setDividerLocation(0.5);
        lowerFrameTitle = "Protein List";
    }
    
    public void detachUpperWindow() {
        if (jSplitPaneSecondary.getTopComponent() != null) {
            JFrame frame = new JFrame("Experiment " +  this.getName() + ": " + upperFrameTitle);
            //frame.setDefaultLookAndFeelDecorated(true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(jSplitPaneSecondary.getTopComponent());
            frame.pack();
            frame.setVisible(true);
            jSplitPaneSecondary.setTopComponent(null);
        }
    }
    
    public void detachLowerWindow() {
        if (jSplitPaneSecondary.getBottomComponent() != null) {
            JFrame frame = new JFrame("Experiment " +  this.getName() + ": " + lowerFrameTitle);
            //frame.setDefaultLookAndFeelDecorated(true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(jSplitPaneSecondary.getBottomComponent());
            frame.pack();
            frame.setVisible(true);
            jSplitPaneSecondary.setBottomComponent(null);
        }
    }
    
    private void jToggleButtonClustersActionPerformed(java.awt.event.ActionEvent evt) {
        jTreeMain.setModel(treeModelClusters);
        jTreeMain.setSelectionRow(0);
    }
    
    private void jToggleButtonProteinsActionPerformed(java.awt.event.ActionEvent evt) {
        jTreeMain.setModel(treeModelProteins);
        jTreeMain.setSelectionRow(0);
    }
    
    private void jToggleButtonPeptidesActionPerformed(java.awt.event.ActionEvent evt) {
        jTreeMain.setModel(treeModelPeptides);
        jTreeMain.setSelectionRow(0);
    }
    
    private void jToggleButtonPeptideHitsActionPerformed(java.awt.event.ActionEvent evt) {
        jTreeMain.setModel(treeModelPeptideHits);
        jTreeMain.setSelectionRow(0);
    }
    
    private void jToggleButtonParsimonyActionPerformed(java.awt.event.ActionEvent evt) {
        jTreeMain.setModel(treeModelParsimony);
        jTreeMain.setSelectionRow(0);
    }
    
    private void updateDisplay() {
        treeModelPeptideHits = new DefaultTreeModel(pepCollection.getPeptideHitsTree(null,this));
        treeModelPeptides = new DefaultTreeModel(pepCollection.getPeptideTree(null,this));
        treeModelProteins = new DefaultTreeModel(pepCollection.getProteinTree(null,this));
        treeModelClusters = new DefaultTreeModel(pepCollection.getClusterTree(this));
        treeModelParsimony = new DefaultTreeModel(pepCollection.getParsimonyTree(null,this));
        jTreeMain.setModel(treeModelParsimony);
        jToggleButtonParsimony.setSelected(true);
        jTreeMain.setSelectionRow(0);
        System.err.println("PepCollectionOrig: " + pepCollectionOriginal.getPeptideHits().size());
        System.err.println("PepCollection: " + pepCollection.getPeptideHits().size());
        //pepCollectionOriginal = null;
        //pepCollection.cleanMemory();
        System.gc();
    }
    
    public HashMap<String, Protein> getProteins() {
        return pepCollection.getMinProteins();
    }
    
    public PeptideCollection getPepCollection() {
        return pepCollection;
    }
    
    public void setOmssaCutoff(String s) {
        omssaCutoff = Double.parseDouble(s);
    }
    
    public void setMascotCutoff(String s) {
        mascotCutoff = Double.parseDouble(s);
    }
    
    public void setOmssaCutoff(double d) {
        omssaCutoff = d;
    }
    
    public void setMascotCutoff(double d) {
        mascotCutoff = d;
    }
    
    public void setUseIonIdent(boolean b) {
        useIonIdent = b;
        if (useIonIdent) {
            mascotCutoff = 0.05;
        }
    }
    
    public void setXtandemCutoff(String s) {
        xtandemCutoff = Double.parseDouble(s);
    }
    
    public void setXtandemCutoff(double d) {
        xtandemCutoff = d;
    }
    
    public void setFilterText(String s) {
        filterText = s;
    }
    public Frame getParentFrame() {
        return (Frame)msFrame;
    }
    
    public MassSieveFrame getMassSieveFrame() {
        return msFrame;
    }
    
    public double getOmssaCutoff() {
        return omssaCutoff;
    }
    
    public double getMascotCutoff() {
        return mascotCutoff;
    }
    
    public double getXtandemCutoff() {
        return xtandemCutoff;
    }
    
    public String getFilterText() {
        return filterText;
    }
    
    public boolean getUseIonIdent() {
        return useIonIdent;
    }
    
    public void setUseIndeterminates(boolean b) {
        useIndeterminates = b;
    }
    
    public void setFilterPeptides(boolean b) {
        filterPeptides = b;
    }
    
    public void setFilterProteins(boolean b) {
        filterProteins = b;
    }
    public void setFilterCoverage(boolean b) {
        filterCoverage = b;
    }
    
    public void setPHitCutoffCount(int i) {
        pHitCutoffCount = i;
    }
    
    public void setPeptideCutoffCount(int i) {
        peptideCutoffCount = i;
    }
    public void setCoverageCutoffAmount(int i) {
        coverageCutoffAmount = i;
    }
    
    public boolean getUseIndeterminates() {
        return useIndeterminates;
    }
    
    public boolean getFilterPeptides() {
        return filterPeptides;
    }
    
    public boolean getFilterProteins() {
        return filterProteins;
    }
    public boolean getFilterCoverage() {
        return filterCoverage;
    }
    
    public int getPHitCutoffCount() {
        return pHitCutoffCount;
    }
    
    public int getPeptideCutoffCount() {
        return peptideCutoffCount;
    }
    public int getCoverageCutoffAmount() {
        return coverageCutoffAmount;
    }
}