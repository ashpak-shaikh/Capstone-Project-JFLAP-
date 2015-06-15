package grammar.cfg;

import grammar.Grammar;
import grammar.Production;
import grammar.parse.AmbiguityCheckerNew;

import java.io.*;

/**
 * Created by Ashpak Shaikh on 4/28/15.
 * 
 * This class is used to test the ambiguity checker 
 * for student grammar.
 * 
 * 
 */
public class TestAmbiguity {


    public static void main(String arg[]) throws IOException {
        for(int i =1; i<=350; i++) {
            System.out.print("student-" + i + "_01.cfg - ");
            checkFile(new File("alphabet-01/student-" + i + ".cfg"));
        }
        for(int i =1; i<=93; i++) {
            System.out.print("student-" + i + "_a.cfg - ");
            checkFile(new File("alphabet-a/student-" + i + ".cfg"));
        }
        for(int i =1; i<=1346; i++) {
            System.out.print("student-" + i + "_ab.cfg - ");
            checkFile(new File("alphabet-ab/student-" + i + ".cfg"));
        }
        for(int i =1; i<=103; i++) {
            System.out.print("student-" + i + "_abc.cfg - ");
            checkFile(new File("alphabet-abc/student-" + i + ".cfg"));
        }
    }

    private static void checkFile(File fin) throws IOException {

        FileInputStream fis = new FileInputStream(fin);
        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        Grammar grammar = new ContextFreeGrammar();
        String line = null;
        String prevlhs = "";
        String startVariable = "";
        int count = 0;
        while ((line = br.readLine()) != null) {
            String rule[] = line.split(":");
            String lhs = rule[0].replaceAll("\\s+", "").replaceAll("\"","");
            String rhs = rule[1].replaceAll("\\s+", "").replaceAll("\"", "").replace(";","");
            if(lhs.equals(""))
                lhs = prevlhs;
            else
                prevlhs = lhs;
            grammar.addProduction(new Production(lhs,rhs));
            if(count == 0) {
                count++;
                startVariable = lhs;
            }
        }
        grammar.setStartVariable(startVariable);
        AmbiguityCheckerNew ambiguityChecker = new AmbiguityCheckerNew(grammar,8);
        ambiguityChecker.checkForTesting();
        br.close();
    }


}

