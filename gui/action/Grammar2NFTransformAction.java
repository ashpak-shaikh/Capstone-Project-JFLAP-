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





package gui.action;

import grammar.CNFConverter;
import grammar.Grammar;
import grammar.LambdaProductionRemover;
import grammar.Production;
import grammar.UnitProductionRemover;
import grammar.UselessProductionRemover;
import gui.environment.EnvironmentFrame;
import gui.environment.GrammarEnvironment;
import gui.environment.Universe;
import gui.environment.tag.CriticalTag;
import gui.grammar.transform.*;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JOptionPane;

/**
 * This is an action to transform a grammar to 2 NF form.
 *
 * @author Ashpak Shaikh
 */

public class Grammar2NFTransformAction extends GrammarAction {
    /**
     * Instantiates a new <CODE>Grammar2NFTransformAction</CODE>.
     *
     * @param environment
     *            the grammar environment
     */
    public Grammar2NFTransformAction(GrammarEnvironment environment) {
        super("Transform Grammar to 2NF", null);
        this.environment = environment;
        this.frame = Universe.frameForEnvironment(environment);
    }

    /**
     * Performs the action.
     */
    public void actionPerformed(ActionEvent e) {
        Grammar g = environment.getGrammar();
        if (g == null)
            return;
        TwoNFPane tp = new TwoNFPane(environment, g);
        environment.add(tp, "2NF Converter", new CriticalTag() {
        });
        environment.setActive(tp);

    }
    /** The grammar environment. */
    private GrammarEnvironment environment;

    /** The frame for the grammar environment. */
    private EnvironmentFrame frame;
}
