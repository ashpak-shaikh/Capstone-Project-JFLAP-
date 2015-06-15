/*
 *  JFLAP - Formal Languages and Automata Package
 * 
 * 
 *  Susan H. Rodger
 *  Computer Science Department
 *  Duke University
 *  August 27, 2009

 *  Copyright (c) 2002-2009
 *  All rights reserved.

 *  JFLAP is open source software. Please see the LICENSE for terms.
 *
 */





package gui.grammar.parse;

import grammar.Grammar;
import grammar.parse.AmbiguityCheckerNew;
import grammar.parse.BruteParserEvent;
import grammar.parse.BruteParserListener;
import grammar.parse.ParseNode;
import gui.environment.GrammarEnvironment;
import gui.sim.multiple.InputTableModel;
import gui.tree.SelectNodeDrawer;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This is an Ambiguity Check pane.
 *
 * @author Ashpak Shaikh
 */

public class AmbigousPane extends ParsePane {

    public AmbigousPane (GrammarEnvironment environment, Grammar g)
    {
        super(environment, g);
    }
    /**
     * Instantiates a new ambiguity checker pane.
     *
     * @param environment
     *            the grammar environment
     * @param grammar
     *            the augmented grammar
     */
    public AmbigousPane(GrammarEnvironment environment, Grammar grammar, InputTableModel model) {
        super(environment, grammar);
        initView();
        myModel = model;
    }

    /**
     * Inits a parse table.
     *
     * @return a table to hold the parse table
     */
    protected JTable initParseTable() {
        return null;
    }

    /**
     * Returns the interface that holds the input area.
     */
    protected JPanel initInputPanel() {
        JPanel bigger = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;

        c.weightx = 0.0;
        panel.add(new JLabel("Enter an upper bound for length"), c);
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(inputField, c);
        inputField.addActionListener(startAction);
        // c.weightx = 0.0;
        // JButton startButton = new JButton(startAction);
        // startButton.addActionListener(listener);
        // panel.add(startButton, c);

        panel.add(progress, c);

        bigger.add(panel, BorderLayout.CENTER);
        bigger.add(initInputToolbar(), BorderLayout.NORTH);

        return bigger;
    }

    /**
     * Returns a toolbar for the parser.
     *
     * @return the toolbar for the parser
     */
    protected JToolBar initInputToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.add(startAction);
        // Set up the view customizer controls.
        toolbar.addSeparator();

        box.setSelectedIndex(0);
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeTree((String) box.getSelectedItem());
            }
        };
        box.addActionListener(listener);
        box.setVisible(false);
        toolbar.add(box);
        toolbar.addSeparator();

        final JComboBox box1 = new JComboBox(getViewChoices());
        box1.setSelectedIndex(0);
        ActionListener listener1 = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeView((String) box1.getSelectedItem());
            }
        };
        box1.addActionListener(listener1);
        toolbar.add(box1);

        toolbar.add(new JButton(pauseResumeAction), 1);
        pauseResumeAction.setEnabled(false);
        return toolbar;
    }

    /**
     * This method is used when you want to view the other parse tree.
     *
     * @param name
     */
    private void changeTree(String name) {
        if (name.equals("First Parse")){
            treePanel.setAnswer(answer1);
        } else if (name.equals("Second Parse")) {
            treePanel.setAnswer(answer2);
        }
        while(!treePanel.next()){
        };
        treePanel.repaint();
    }


    public void parseInput(String string, AmbiguityCheckerNew newParser){
        if(string.equals("")) return;
        if (newParser == null) {
            try {
                int length;
                try{
                    length = Integer.parseInt(string);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Enter an integer",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                parser = AmbiguityCheckerNew.get(grammar, length);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Bad Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        else parser = newParser;
        final Timer timer = new Timer(10, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (parser == null)
                    return;
                progress.setText("Checker running...");
            }
        });
        parser.addBruteParserListener(new BruteParserListener() {
            public void bruteParserStateChange(BruteParserEvent e) {
                synchronized (e.getParser()) {

                    String status = null;
                    switch (e.getType()) {
                        case BruteParserEvent.START:
                            pauseResumeAction.setEnabled(true);
                            pauseResumeAction.putValue(Action.NAME, "Pause");
                            timer.start();
                            status = "Parser started.";
                            statusDisplay.setText(status);
                            break;
                        case BruteParserEvent.NONAMBIGUOUS:
                            pauseResumeAction.setEnabled(false);
                            timer.stop();
                            status = "Grammar is non-ambiguous.";
                            break;
                        case BruteParserEvent.PAUSE:
                            timer.stop();
                            pauseResumeAction.putValue(Action.NAME, "Resume");
                            pauseResumeAction.setEnabled(true);
                            status = "Checker paused.";
                            statusDisplay.setText(status);
                            break;
                        case BruteParserEvent.AMBIGUOUS:
                            pauseResumeAction.setEnabled(false);
                            stepAction.setEnabled(true);
                            timer.stop();
                            status = "Grammar is ambiguous for the String ->" + e.getParser().getAnswer().getDerivation();
                            break;

                    }
                    progress.setText(status);
                    if (parser.isFinished()) {
//						parser = null;
                        if (!e.isAmbiguous()) {
                            // Rejected!
                            treePanel.setAnswer(null);
                            treePanel.repaint();
                            stepAction.setEnabled(false);
                            statusDisplay.setText("Try another length.");
                            return;
                        }
                        TreeNode node = e.getParser().getAnswer();
                        do {
                            node = node.getParent();
                        } while (node != null);
                        node = e.getParser().getAnswer1();
                        do {
                            node = node.getParent();
                        } while (node != null);
                        box.setVisible(true);
                        statusDisplay
                                .setText("Select the different parse tree");
                        answer1 = e.getParser().getAnswer();
                        answer2 = e.getParser().getAnswer1();
                        treePanel.setAnswer(answer1);
                        while(!treePanel.next()){
                        };
                        treePanel.repaint();


                    }
                }
            }

        });
        parser.start();
    }

    /**
     * This method is called when there is new input to parse.
     *
     * @param string
     *            a new input string
     */
    public void input(String string) {
        if (parser != null) {
            parser.pause();
        }
        parseInput(string, null);
    }

    /**
     * Returns the choices for the view.
     *
     * @return an array of strings for the choice of view
     */
    protected String[] getViewChoices() {
        return new String[] { "Noninverted Tree", "Derivation Table" };
    }

    final JComboBox box = new JComboBox(new String[]{"First Parse","Second Parse"});
    /**
     * This method is called when the step button is pressed.
     */
    public boolean step() {
        return false;
    }


    /**
     * Inits a new tree panel. This overriding adds a selection node drawer so
     * certain nodes can be highlighted.
     *
     * @return a new display for the parse tree
     */
    protected JComponent initTreePanel() {
        return treePanel;
    }

    public int row = -1;
    /** The tree pane. */
    protected UnrestrictedTreePanel treePanel = new UnrestrictedTreePanel(this);

    protected UnrestrictedTreePanel treePanel1 = new UnrestrictedTreePanel(this);


    /** The selection node drawer. */
    protected SelectNodeDrawer nodeDrawer = new SelectNodeDrawer();

    /** The progress bar. */
    protected JLabel progress = new JLabel(" ");

    /** The current parser object. */
    protected AmbiguityCheckerNew parser = null;

    protected InputTableModel myModel = null;

    protected ParseNode answer1 = null;

    protected ParseNode answer2 = null;


    /** The pause/resume action. */
    protected Action pauseResumeAction = new AbstractAction("Pause") {
        public void actionPerformed(ActionEvent e) {
            synchronized (parser) {
                if (parser == null)
                    return;
                if (parser.isActive())
                    parser.pause();
                else
                    parser.start();
            }
        }
    };
}
