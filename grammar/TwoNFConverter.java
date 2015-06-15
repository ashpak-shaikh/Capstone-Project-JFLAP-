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





package grammar;

import java.util.*;


/**
 * Converts grammars to Binaru normal form.
 * 
 * @author Ashpak Shaikh
 */

public class TwoNFConverter {

    private static List<String> variables;

    /**
     *
     * This method converts the grammar to binray normal form.
     *
     * @param grammar
     *
     * @return
     */
    public static Production[] convert(Grammar grammar) {

        variables = new ArrayList<String>(Arrays.asList(grammar.getVariables()));
        Production[] productions = grammar.getProductions();
        List<Production> newproductions = new ArrayList<Production>();
        for (Production production : productions) {
            //grammar.removeProduction(production);
            if (production.getRHS().length() > 2) {
                Production[] prods = getReplacements(production);
                for (Production p : prods)
                    newproductions.add(p);
            } else {
                newproductions.add(production);
            }
        }
        return newproductions.toArray(new Production[newproductions.size()]);
    }

    /**
     *
     * This method gives the replacement for the given production.
     *
     * @param production
     * @return
     */
    private static Production[] getReplacements(Production production) {

        List<Production> newProds = new ArrayList<Production>();
        Stack<Production> prods = new Stack<Production>();
        prods.add(production);

        while (!prods.isEmpty()) {
            Production p = prods.pop();
            if (p.getRHS().length() > 2) {
                String newVariable = createNewVariable();
                if (newVariable == null)
                    throw new NullPointerException("The variables are exhausted");
                String newRHS2 = p.getRHS().substring(1);
                String newRHS1 = p.getRHS().charAt(0) + newVariable;
                newProds.add(new Production(p.getLHS(), newRHS1));
                prods.push(new Production(newVariable, newRHS2));
            } else
                newProds.add(p);
        }

        return newProds.toArray(new Production[newProds.size()]);
    }

    /**
     *
     * This method returns the new variable used for 2-NF conversion.
     *
     * @return
     */
    private static String createNewVariable() {
        for (char c = 'A'; c <= 'Z'; c++) {
            String var = String.valueOf(c);
            if (!variables.contains(var)) {
                variables.add(var);
                return var;

            }
        }
        return null;
    }

}