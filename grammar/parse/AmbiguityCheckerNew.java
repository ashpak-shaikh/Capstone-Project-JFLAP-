/*
 *  JFLAP - Formal Languages and Automata Package
 * 
 * 
 *  Ashpak Shaikh
 *  Computer Science Department
 *  Rochester Institute of Technology
 *  June 13, 2015
 *
 */





package grammar.parse;

import grammar.Grammar;
import grammar.LambdaProductionRemover;
import grammar.Production;
import java.util.*;

/**
 * The <CODE>AmbiguityCheckerNew</CODE> is an abstract class that will perform a ambiguity checking
 * of a grammar.
 *
 * @author Ashpak Shaikh
 */

public class AmbiguityCheckerNew extends BruteParser {

    /**
     * This does nothing. One can use this constructor if one wants to call init
     * later.
     */
    protected AmbiguityCheckerNew() {
    }

    /**
     * This will instantiate a new ambiguity checker. All this does is call
     * {@link #init} with the grammar and length of the bound.
     */
    public AmbiguityCheckerNew(Grammar grammar, int length) {
        init(grammar, length);
    }

    /**
     * This factory method will return a ambiguity checker appropriate for the
     * grammar.
     *
     * @param grammar
     *            the grammar to get a brute force parser for
     * @param length
     *            the target length
     */
    public static AmbiguityCheckerNew get(Grammar grammar, int length) {

        return new AmbiguityCheckerNew(grammar, length);

    }

    /**
     * This will initialize data structures.
     */
    protected void init(Grammar grammar, int length) {


        queue.clear();

        //grammar = Unrestricted.optimize(grammar);
        //System.out.println(grammar);
        if (grammar == null) {
            System.out.println("grammar null");
            return;
        }

        queue.add(new ParseNode(grammar.getStartVariable(), P, S));

        smaller = Collections.unmodifiableSet(Unrestricted
                .smallerSymbols(grammar));


        LambdaProductionRemover remover = new LambdaProductionRemover();
        HashSet lambdaSet = remover.getCompleteLambdaSet(grammar);
        this.grammar = remover.getLambdaProductionlessGrammar(grammar,lambdaSet);
        productions = this.grammar.getProductions();
        this.length = length;
        //System.out.println(this.grammar);
    }

    /**
     * This will start the check. This method will return immediately. The
     * checking is done in a separate thread since the potential for the checking
     * to take forever on some ambiguity check exists.
     *
     * @return if the starting of the check was successful, which will not be
     *         successful if the checker is already underway, or if the checker
     *         is finished
     */
    public synchronized boolean start() {
        if (isActive() || isFinished())
            return false;
        checkThread = new Thread() {
            public void run() {
                while (checkThread != null)
                    parse();
            }
        };
        checkThread.start();
        distributeEvent(new BruteParserEvent(this, BruteParserEvent.START));
        return true;
    }

    /**
     * This will pause the checking. At the end of this method the checking thread
     * will probably not halt.
     */
    public synchronized void pause() {
        checkThread = null;
        distributeEvent(new BruteParserEvent(this, BruteParserEvent.PAUSE));
    }

    /**
     * Returns if the checker is currently in the process of checker.
     *
     * @return <CODE>true</CODE> if the check thread is currently active, or
     *         <CODE>false</CODE> if the check thread is inactive
     */
    public synchronized boolean isActive() {
        return checkThread != null;
    }

    /**
     * Returns if the checker has finished, with success or otherwise.
     *
     * @return <CODE>true</CODE> if the
     */
    public synchronized boolean isFinished() {
        return isDone;
    }

    /**
     * This returns the first answer node for the checker.
     *
     * @return the first answer node for the check, or <CODE>null</CODE> if there
     *         was no answer, or one has not been discovered yet
     */
    public synchronized ParseNode getAnswer() {
        return answer;
    }

    /**
     * This returns the second answer node for the check.
     *
     * @return the second answer node for the parse, or <CODE>null</CODE> if there
     *         was no answer, or one has not been discovered yet
     */
    public synchronized ParseNode getSecondAnswer() { return secondAnswer; }


    /**
     * Returns a list of possible one step parses for a given string. The first
     * entry is always the identity.
     */
    private List getPossibilities(String c) {
        List possibilities = new ArrayList();
        if (prederived.containsKey(c))
            return (List) prederived.get(c);
        HashSet alreadyEncountered = new HashSet();
        if (c.length() == 0) {
            possibilities.add(E);
            return possibilities;
        }
        for (int i = -1; i < productions.length; i++) {
            Production prod = i == -1 ? new Production(c.substring(0, 1), c
                    .substring(0, 1)) : productions[i];
            // Find the start of the production.
            int start = c.indexOf(prod.getLHS());
            int lengthSubs = prod.getLHS().length();
            if (start == -1)
                continue;
            List list = getPossibilities(c.substring(start + lengthSubs));
            Iterator it = list.iterator();
            String prepend = c.substring(0, start) + prod.getRHS();
            int lengthReplace = start + prod.getLHS().length();
            // Make adjustments for each entry.
            while (it.hasNext()) {
                ParseNode node = (ParseNode) it.next();
                String d = node.getDerivation();
                Production[] p = node.getProductions();
                String a = prepend + d;
                int[] s = node.getSubstitutions();
                if (i == -1) {
                    int[] newS = new int[s.length];
                    for (int j = 0; j < p.length; j++) {
                        newS[j] = s[j] + lengthReplace;
                    }
                    // Make the node with the substitution.
                    if (alreadyEncountered.add(a)) {
                        node = new ParseNode(a, p, newS);
                        possibilities.add(node);
                    }
                } else {
                    Production[] newP = new Production[p.length + 1];
                    int[] newS = new int[s.length + 1];
                    newS[0] = start;
                    newP[0] = prod;
                    for (int j = 0; j < p.length; j++) {
                        newP[j + 1] = p[j];
                        newS[j + 1] = s[j] + lengthReplace;
                    }
                    // Make the node with the substitution.
                    if (alreadyEncountered.add(a)) {
                        node = new ParseNode(a, newP, newS);
                        possibilities.add(node);
                    }
                }
            }
        }
        // prederived.put(c, possibilities);
        return possibilities;
    }

    // Stuff for the possibilities.
    private static final Production[] P = new Production[0];

    private static final int[] S = new int[0];

    private static final ParseNode E = new ParseNode("", P, S);

    /**
     * The parsing method.
     */
    private synchronized void parse() {

        if (queue.isEmpty()) {
            isDone = true;
            checkThread = null;
            System.out.println("Not ambiguous");
            distributeEvent(new BruteParserEvent(this, BruteParserEvent.NONAMBIGUOUS));
            return;
        }
        // Get one element.
        ParseNode node = (ParseNode) queue.removeFirst();
        //System.out.println(node.getDerivation());

        if(node.getDerivation().length()<this.length) {
            List pos = getPossibilities(node.getDerivation());
            Iterator it = pos.iterator();
            if (it.hasNext()) it.next();
            while (it.hasNext()) {
                ParseNode pNode = (ParseNode) it.next();
                pNode = new ParseNode(pNode);
                node.add(pNode);
                queue.add(pNode);
            }
        }
        else if(node.isLeaf()){
            System.out.println("Not Ambiguous");
            isDone = true;
            checkThread = null;
            distributeEvent(new BruteParserEvent(this, BruteParserEvent.NONAMBIGUOUS));
            return;
        }
        ParseNode sec = getVisited(node);
        if(sec!=null) {
            if (node.isLeaf()) {
                System.out.println("Ambiguous for string " + node.getDerivation());
                answer = node;
                secondAnswer = sec;
                isDone = true;
                checkThread = null;
                distributeEvent(new BruteParserEvent(this, BruteParserEvent.AMBIGUOUS));
                return;
            }
        }
        else{
            nodes.add(node);
        }

    }

    /**
     *
     * This method is used for testing student grammars.
     *
     * @return
     */
    public boolean checkForTesting() {

        while (!queue.isEmpty()) {
            // Get one element.
            ParseNode node = (ParseNode) queue.removeFirst();
            //System.out.println(node.getDerivation());
            List pos = getPossibilities(node.getDerivation());
            Iterator it = pos.iterator();
            if (it.hasNext()) it.next();
            while (it.hasNext()) {
                ParseNode pNode = (ParseNode) it.next();
                pNode = new ParseNode(pNode);
                node.add(pNode);
                queue.add(pNode);
            }
                //System.out.println("Leaf " + node.getDerivation());

                if (node.getDerivation().length() > this.length) {
                    System.out.println("Not Ambiguous");
                    return false;

                }
                    ParseNode sec = getVisited(node);
                    if (sec != null) {
                        System.out.println("Ambiguous for string " + node.getDerivation());
                        return true;
                    } else
                        nodes.add(node);
            }
        System.out.println("Not ambiguous");
        return false;
    }

    /**
     *
     * This method returns a visited node to the node passed if it exists.
     *
     * @param node
     * @return
     */
    private ParseNode getVisited(ParseNode node){

        for( ParseNode n : nodes){
            if(n.getDerivation().equals(node.getDerivation()))
                return n;
        }
        return null;
    }

    /**
     * Adds a brute parser listener to this parser.
     *
     * @param listener
     *            the listener to add
     */
    public void addBruteParserListener(BruteParserListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a brute parser listener from this parser.
     *
     * @param listener
     *            the listener to remove
     */
    public void removeBruteParserListener(BruteParserListener listener) {
        listeners.remove(listener);
    }

    /**
     * Distributes a brute parser event to all listeners.
     *
     * @param event
     *            the brute parser event to distribute
     */
    protected void distributeEvent(BruteParserEvent event) {
        Iterator it = listeners.iterator();
        while (it.hasNext())
            ((BruteParserListener) it.next()).bruteParserStateChange(event);
    }

    /** The set of listeners. */
    protected Set listeners = new HashSet();

    /** This is the grammar. */
    protected Grammar grammar;

    /** The array of productions. */
    protected Production[] productions;


    /** This should be set to done when the operation has completed. */
    private boolean isDone = false;

    /**
     * This is the thread that does the parsing; if the value is <CODE>null</CODE>
     * that indicates that no parsing thread exists yet.
     */
    private Thread checkThread = null;

    /**
     * This holds those strings that have already been derived, with a map to
     * those nodes for each string.
     */
    private Map prederived = new HashMap();

    /** This holds the list of nodes for the BFS. */
    protected LinkedList queue = new LinkedList();
    /** This holds the list of nodes which is visited. */
    protected LinkedList<ParseNode> nodes = new LinkedList();
    /** The "answer" to the parse question. */
    private ParseNode answer = null;
    /** The second "answer" to the parse question. */
    private ParseNode secondAnswer = null;
    /**
     * The "smaller" set, those symbols that may possibly reduce to nothing.
     */
    protected Set smaller;
    /** The bounded length */
    private int length;

}
