/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jagadeesh.t
 */
public class Conversion {

    private Stack2 s;
    private List<String> input;
    private List<String> output = new ArrayList<>();

    public Conversion(List<String> tokens) {
        input = tokens;
        s = new Stack2(tokens.size());
    }

    public List<String> inToPost() throws Exception {
        int operandCounter = 0;
        for (int i = 0; i < input.size(); i++) {
            String ch = input.get(i);
            Integer opPrec = RuleBuilder.getMapOperatorPrecTab().get(ch);
            switch (ch) {
                case "(":
                    s.push(ch);
                    break;
                case ")":
                    gotParenthesis();
                    break;
                default:
                    if (opPrec != null) {
                        //if (operandCounter < 2) {
                        gotOperator(ch);
                        //}
                    } else {
                        output.add(ch);
                        operandCounter++;
                    }
            }
        }
        while (!s.isEmpty()) {
            output.add(s.pop());
        }
        return output;
    }

    private void gotOperator(String opThis) {
        while (!s.isEmpty()) {
            String opTop = s.pop();
            if (opTop.equals("(")) {
                s.push(opTop);
                break;
            } else {
                int prec1 = RuleBuilder.getMapOperatorPrecTab().get(opThis);
                int prec2 = RuleBuilder.getMapOperatorPrecTab().get(opTop);
                if (prec2 < prec1) {
                    s.push(opTop);
                    break;
                } else {
                    output.add(opTop);
                }
            }
        }
        s.push(opThis);
    }

    private void gotParenthesis() throws Exception {
        while (!s.isEmpty()) {
            String ch = s.pop();
            if (ch.equals("(")) {
                break;
            } else {
                output.add(ch);
            }
        }
    }

}
