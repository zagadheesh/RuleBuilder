/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.util.List;

/**
 *
 * @author jagadeesh.t
 */
public class RuleUtil {

    public static boolean isStartsWithRelationalOperator(String operator, List<String> relationalOperators) {
        if (operator == null) {
            return false;
        }
        operator = operator.toLowerCase();
        int index = 0;
        if (operator.startsWith("(")) {

            char c[] = operator.toCharArray();
            for (int i = 0; i < c.length; i++) {
                char cha = c[i];
                if (cha == '(') {
                    index++;
                }
            }
        }
        if (index != 0) {
            operator = operator.substring(0, index);

        }
        for (String string : relationalOperators) {
            if (operator.startsWith(string)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isStartsWithAirthmeticOperator(String operator, List<String> airthmeticOperators) {
        if (operator == null) {
            return false;
        }
        operator = operator.toLowerCase();
        for (String string : airthmeticOperators) {
            if (operator.startsWith(string)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isStartsWithLogicalOperator(String operator, List<String> logicalOperators) {
        if (operator == null) {
            return false;
        }
        operator = operator.toLowerCase();
        for (String string : logicalOperators) {
            if (operator.startsWith(string)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isStartsAndEndsWith(String source, String str) {
        try {
            if (source.startsWith(str)) {
                if (source.endsWith(str) && source.charAt(source.length() - (str.length() + 1)) != '\\') {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static boolean isStartsWith(String source, String str) {
        if (source.startsWith(str)) {
            return true;
        }
        return false;
    }

    public static boolean isEndsWith(String source, String str) {
        try {
            if (source.endsWith(str) && source.charAt(source.length() - str.length()) != '\\') {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

}
