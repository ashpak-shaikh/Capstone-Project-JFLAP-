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
 * Converts grammars to Chomsky normal form.
 * 
 * @author Thomas Finley
 */

public class TwoNFConverter {
    /**
     * Instantiates a new chomsky normal converter.
     *
     * @param grammar the grammar to convert
     *
     */

    private static List<String> variables;

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