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





package gui.grammar.transform;

import grammar.Grammar;
import grammar.Production;
import grammar.TwoNFConverter;
import gui.SplitPaneFactory;
import gui.environment.FrameFactory;
import gui.environment.GrammarEnvironment;
import gui.grammar.GrammarTable;
import gui.grammar.GrammarTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The pane for converting a grammar to Binary normal form.
 *
 * @author Thomas Finley
 */

public class TwoNFPane extends JPanel {
    /**
     * Instantiates a 2NF pane.
     *
     * @param environment
     *            the environment that this pane will become a part of
     * @param grammar
     *            the grammar to convert
     */
    public TwoNFPane(GrammarEnvironment environment, Grammar grammar) {
        this.environment = environment;
        this.grammar = grammar;
        mainLabel.setText("Welcome to the 2NF converter.");
        mainLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() > 10)
                    mainLabel
                            .setText("Click on me again, and I'll kick your ass.");
            }
        });
        initView();
        convertAction.setEnabled(true);
        exportAction.setEnabled(false);
        directionLabel
                .setText("Press convert.");    }

    /**
     * Initializes the GUI components of this pane.
     */
    private void initView() {
        super.setLayout(new BorderLayout());
        initGrammarTable();
        JPanel rightPanel = initRightPanel();
        JSplitPane mainSplit = SplitPaneFactory.createSplit(environment, true,
                0.4, new JScrollPane(grammarTable), rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    /**
     * Initializes the right panel.
     *
     * @return an initialized right panel
     */
    private JPanel initRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        mainLabel.setAlignmentX(0.0f);
        directionLabel.setAlignmentX(0.0f);
        right.add(mainLabel);
        right.add(directionLabel);
        right.add(new JScrollPane(editingGrammarView));

        JPanel biggie = new JPanel(new BorderLayout());
        biggie.add(right, BorderLayout.CENTER);
        JToolBar bar = new JToolBar();
        bar.add(convertAction);
        bar.addSeparator();
        bar.add(exportAction);
        biggie.add(bar, BorderLayout.NORTH);
        return biggie;
    }

    /**
     * Takes the grammar, and attempts to export it.
     */
    private void export() {
        Production[] p = editingGrammarModel.getProductions();
	/*	System.out.println("PRINTTITTING");
		for (int i=0; i<p.length; i++)
		{
			System.out.println(p[i]);
		}*/
        try {
            p = TwoNFConverter.convert(this.grammar);
        } catch (UnsupportedOperationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Grammar g = (Grammar) grammar.getClass().newInstance();
            g.addProductions(p);
            g.setStartVariable(grammar.getStartVariable());
            FrameFactory.createFrame(g);
        } catch (Throwable e) {
            System.err.println(e);
        }
    }


    /**
     * Converts the selected rows.
     */
    private void convert() {
        if (!convertAction.isEnabled())
            return;
        Production[] p = editingGrammarModel.getProductions();

            p = TwoNFConverter.convert(grammar);


        try {
            Grammar g = (Grammar) grammar.getClass().newInstance();
            g.addProductions(p);
            g.setStartVariable(grammar.getStartVariable());
            initEditingGrammarTable(g);
        } catch (Throwable e) {
            System.err.println(e);
        }

        convertAction.setEnabled(false);
        exportAction.setEnabled(true);
        directionLabel
                .setText("Conversion done.  Press \"Export\" to use.");    }

    /**
     * Initializes the editing grammar view.
     */
    private void initEditingGrammarTable(Grammar grammar) {
        Production[] ps = grammar.getProductions();
        for (int i = 0; i < ps.length; i++)
            editingGrammarModel.addProduction(ps[i]);
    }

    /**
     * Initializes a table for the grammar.
     *
     * @return a table to display the grammar
     */
    private GrammarTable initGrammarTable() {
        grammarTable = new GrammarTable(new GrammarTableModel(grammar) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        return grammarTable;
    }

    /** The environment. */
    GrammarEnvironment environment;

    /** The grammar. */
    Grammar grammar;

    /** The grammar table. */
    GrammarTable grammarTable;

    /** The grammar table. */
    GrammarTableModel editingGrammarModel = new GrammarTableModel() {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    /** The grammar table. */
    GrammarTable editingGrammarView = new GrammarTable(editingGrammarModel);

    /** The main label. */
    JLabel mainLabel = new JLabel(" ");

    /** The direction label. */
    JLabel directionLabel = new JLabel(" ");

    /** The convert action. */
    AbstractAction convertAction = new AbstractAction("Convert") {
        public void actionPerformed(ActionEvent e) {
            convert();
        }
    };

    /** The export action. */
    AbstractAction exportAction = new AbstractAction("Export") {
        public void actionPerformed(ActionEvent e) {
            export();
        }
    };
}
