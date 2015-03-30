/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import Util.RuleUtil;
import static Util.RuleUtil.isStartsAndEndsWith;
import beans.AtbMetaData;
import beans.HeuristicAtb;
import beans.MyToken;
import beans.Node;
import beans.RegularExpression;
import beans.Rule;
import beans.UserDefinedFunction;
import core.Tree;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ashiskumar.m
 */
public class RuleBuilder {

//    static String delimeters = "()><>=<=!=andor+-*/%";
    private static Map<String, Integer> mapOperatorPrecTab = new HashMap<String, Integer>();
//    static JavaCharStream charStream;
    //  static RuleExpressionProcessorTokenManager tokenMgr;
    static List<String> ambiguityOprators = new ArrayList<>();
    static Map<String, String> ambiguityOpratorMap = new LinkedHashMap<>();
    static List<String> relationalOperators = new ArrayList<>();
    static List<String> logicalOperators = new ArrayList<>();
    static List<String> airthmeticOperators = new ArrayList<>();

    static {

        relationalOperators.add(">=");
        relationalOperators.add("<=");
        relationalOperators.add("==");
        relationalOperators.add("!=");
        relationalOperators.add(">");
        relationalOperators.add("<");
        relationalOperators.add("=");
        relationalOperators.add("in");
        relationalOperators.add("notin");
        relationalOperators.add("not in");
        relationalOperators.add("like");
        relationalOperators.add("notlike");
        relationalOperators.add("not like");

        logicalOperators.add("and");
        logicalOperators.add("or");
        logicalOperators.add("&&");
        logicalOperators.add("||");

        airthmeticOperators.add("+");
        airthmeticOperators.add("-");
        airthmeticOperators.add("*");
        airthmeticOperators.add("/");
        airthmeticOperators.add("%");

        ambiguityOpratorMap.put("=", "!=");
        ambiguityOpratorMap.put("!", "!=");
        ambiguityOpratorMap.put("=", "==");
        ambiguityOpratorMap.put(">", ">=");
        ambiguityOpratorMap.put("<", "<=");
        ambiguityOpratorMap.put("in", "notin");
        ambiguityOpratorMap.put("like", "notlike");

        String[] ops = null;
//        String assignmentOperators = "+=,-=,*=,/=,%=,&=,^=,|=,<<=,>>=,>>>=";
//        String[] ops = assignmentOperators.split(",");
//        for (String operator : ops) {
//            mapOperatorPrecTab.put(operator, 1);
//        }
//        String conditionalOperator = "?,:";
//        ops = conditionalOperator.split(",");
//        for (String operator : ops) {
//            mapOperatorPrecTab.put(operator, 2);
//        }
        String logicalOrOperator = "||,or";
        // String logicalOrOperator = "or";
        ops = logicalOrOperator.split(",");
        for (String operator : ops) {
            mapOperatorPrecTab.put(operator, 3);
        }

        String logicalAndOperator = "&&,and";
        //String logicalAndOperator = "and";
        ops = logicalAndOperator.split(",");
        for (String operator : ops) {
            mapOperatorPrecTab.put(operator, 4);
        }

//        String bitwiseInclusiveOr = "|";
//        mapOperatorPrecTab.put(bitwiseInclusiveOr, 5);
//
//        String bitwiseExclusiveOr = "^";
//        mapOperatorPrecTab.put(bitwiseExclusiveOr, 6);
//
//        String bitwiseAnd = "&";
//        mapOperatorPrecTab.put(bitwiseAnd, 7);
        String relationalEqualityOperator = "=,==,!=";
        ops = relationalEqualityOperator.split(",");
        for (String operator : ops) {
            mapOperatorPrecTab.put(operator, 8);
        }

        //String lessThanGreaterThanOperator = "<,<=,>,>=,instanceof";
        String lessThanGreaterThanOperator = "<,<=,>,>=";
        ops = lessThanGreaterThanOperator.split(",");
        for (String operator : ops) {
            mapOperatorPrecTab.put(operator, 9);
        }
        String inNotInOperator = "in,notin,like,notlike";
        ops = inNotInOperator.split(",");
        for (String operator : ops) {
            mapOperatorPrecTab.put(operator, 9);
        }

//        String bitwiseShiftOperators = "<<,>>,>>>";
//        ops = bitwiseShiftOperators.split(",");
//        for (String operator : ops) {
//            mapOperatorPrecTab.put(operator, 10);
//        }
        String additionSubstractionOperators = "+,-";
        ops = additionSubstractionOperators.split(",");
        for (String operator : ops) {
            mapOperatorPrecTab.put(operator, 11);
        }

        String multiDivModOperators = "*,/,%";
        ops = multiDivModOperators.split(",");
        for (String operator : ops) {
            mapOperatorPrecTab.put(operator, 12);
        }
        String parenthesis = "(,)";
        ops = parenthesis.split(",");
        for (String operator : ops) {
            mapOperatorPrecTab.put(operator, 14);
        }

    }

    public static Map<String, Integer> getMapOperatorPrecTab() {
        return mapOperatorPrecTab;
    }

    public static void setMapOperatorPrecTab(Map<String, Integer> mapOperatorPrecTab) {
        RuleBuilder.mapOperatorPrecTab = mapOperatorPrecTab;
    }

    public static List<String> getAmbiguityOprators() {
        return ambiguityOprators;
    }

    public static void setAmbiguityOprators(List<String> ambiguityOprators) {
        RuleBuilder.ambiguityOprators = ambiguityOprators;
    }

    public static Map<String, String> getAmbiguityOpratorMap() {
        return ambiguityOpratorMap;
    }

    public static void setAmbiguityOpratorMap(Map<String, String> ambiguityOpratorMap) {
        RuleBuilder.ambiguityOpratorMap = ambiguityOpratorMap;
    }

    public static List<String> getRelationalOperators() {
        return relationalOperators;
    }

    public static void setRelationalOperators(List<String> relationalOperators) {
        RuleBuilder.relationalOperators = relationalOperators;
    }

    public static List<String> getLogicalOperators() {
        return logicalOperators;
    }

    public static void setLogicalOperators(List<String> logicalOperators) {
        RuleBuilder.logicalOperators = logicalOperators;
    }

    public static List<String> getAirthmeticOperators() {
        return airthmeticOperators;
    }

    public static void setAirthmeticOperators(List<String> airthmeticOperators) {
        RuleBuilder.airthmeticOperators = airthmeticOperators;
    }

    public static String buildExpression(String operandName, String expression) throws Exception {
        expression = expression == null ? "" : expression;
        expression = expression.toLowerCase();
        //org.apache.log4j.Logger.getLogger(RuleBuilder.class).debug("InitialExpression:" + expression);
        List<String> tokens = new ArrayList<String>();
        List<MyToken> lTokens = splitForQuotes(expression, false);
        for (MyToken myToken : lTokens) {
            String regExp = "(?<=and|or|&&|\\|\\||[()+\\-/><=]|[*%]|==|!=|>=|<=|not like|notlike|like|not in|notin|in)|(?=and|or|&&|\\|\\||[()+\\-/><=]|[*%]|==|!=|>=|<=|not like|notlike|like|not in|notin|in)";
            if (myToken.getType() == 0) {
                String[] arrToken = myToken.getToken().split(regExp);
                for (String token : arrToken) {
                    if (token.trim().length() > 0) {
                        tokens.add(token.trim().toLowerCase());

                    }
                }
            } else if (myToken.getType() == 1) {
                tokens.add("'" + myToken.getToken().toLowerCase() + "'");
            } else if (myToken.getType() == 2) {
                tokens.add("\"" + myToken.getToken().toLowerCase() + "\"");
            } else {
                tokens.add("`" + myToken.getToken().toLowerCase() + "`");
            }
        }
        // Filter for amguity operator
        List<String> filterTokens1 = new ArrayList<>();
        StringBuilder sbOperator = new StringBuilder();
        int idx = 0;
        boolean bSkip = false;
        for (String token : tokens) {
            try {
                if (bSkip) {
                    bSkip = false;
                    continue;
                }
                String operator = ambiguityOpratorMap.get(token);
                if (operator != null) {
                    String lastToken = (idx - 1) >= 0 ? tokens.get(idx - 1) : "";
                    String nextToken = (idx + 1) < (tokens.size()) ? tokens.get(idx + 1) : "";
                    String verifyTokenBehind = lastToken + token;
                    String verifyTokenAhead = token + nextToken;
                    if (operator.equalsIgnoreCase(verifyTokenBehind)) {
                        filterTokens1.remove(filterTokens1.size() - 1);
                        filterTokens1.add(verifyTokenBehind);
                    } else if (operator.equalsIgnoreCase(verifyTokenAhead)) {
                        bSkip = true;
                        filterTokens1.add(verifyTokenAhead.toLowerCase());
                    } else {
                        filterTokens1.add(token);
                    }
                } else {
                    filterTokens1.add(token);
                }
            } finally {
                idx++;
            }
        }

        List<String> filterTokens2 = new ArrayList<>();
        StringBuilder sbOperandTmp = new StringBuilder();
        boolean bFunFlag = false;
        boolean isFirstCharAfterFun = false;
        for (String token : filterTokens1) {
            if (!bFunFlag && (token.equals("date") || token.equals("datetime"))) {
                sbOperandTmp.append(token);
                bFunFlag = true;
                isFirstCharAfterFun = true;
                continue;
            } else if (bFunFlag) {
                if (!token.equals("(") && isFirstCharAfterFun) {
                    bFunFlag = false;
                    filterTokens2.add(sbOperandTmp.toString());
                    filterTokens2.add(token);
                    sbOperandTmp.delete(0, sbOperandTmp.toString().length());
                } else if (token.equals(")")) {
                    sbOperandTmp.append(token);
                    filterTokens2.add(sbOperandTmp.toString());
                    sbOperandTmp.delete(0, sbOperandTmp.toString().length());
                    bFunFlag = false;
                } else {
                    sbOperandTmp.append(token);
                }
                isFirstCharAfterFun = false;
            } else {
                filterTokens2.add(token);
            }
        }
        if (sbOperandTmp.toString().length() > 0) {
            filterTokens2.add(sbOperandTmp.toString());
        }

        List<String> filterTokens = new ArrayList<>();
        //int lastToken = 0; //0-operand,1-operator
        int counter = 0;
        StringBuilder sbOperand = new StringBuilder();
        boolean bInFlag = false;
        //int lastToken = 0; //0-operand,1-operator
        for (String token : filterTokens2) {
            counter++;
            if (!bInFlag && (token.equals("("))) {
                if (sbOperand.toString().length() > 0) {
                    filterTokens.add(sbOperand.toString());
                    sbOperand.delete(0, sbOperand.toString().length());
                }
                filterTokens.add(token);
                continue;
            }
            if (token.equals(")")) {
                if (bInFlag) {
                    if (sbOperand.toString().length() > 0) {
                        sbOperand.append(token);
                        filterTokens.add(sbOperand.toString());
                        sbOperand.delete(0, sbOperand.toString().length());
                    } else {
                        filterTokens.add(token);
                    }
                    bInFlag = false;
                } else {
                    if (sbOperand.toString().length() > 0) {
                        filterTokens.add(sbOperand.toString());
                        sbOperand.delete(0, sbOperand.toString().length());
                    }
                    filterTokens.add(token);
                }
                continue;
            }
            if (bInFlag) {
                sbOperand.append(token);
                continue;
            }
            if (token.equalsIgnoreCase("in") || token.equalsIgnoreCase("notin")) {
                bInFlag = true;
            }
            if (sbOperand.toString().length() > 0) {
                filterTokens.add(sbOperand.toString());
                sbOperand.delete(0, sbOperand.toString().length());
            }
            filterTokens.add(token);
        }
        if (sbOperand.toString().length() > 0) {
            filterTokens.add(sbOperand.toString());
        }

        //Filter for -ve sign
        List<String> finalFilterTokens = new ArrayList<String>();

        int idxCounter = 0;
        for (String token : filterTokens) {
            String prevToken = (idxCounter - 1) >= 0 ? filterTokens.get(idxCounter - 1) : null;
            String prevToPrevToken = (idxCounter - 2) >= 0 ? filterTokens.get(idxCounter - 2) : null;

            if (prevToken != null && prevToPrevToken != null) {
                if (airthmeticOperators.contains(prevToken)
                        && mapOperatorPrecTab.get(prevToPrevToken) != null && !(prevToPrevToken.equals("(") || prevToPrevToken.equals(")"))) {
                    finalFilterTokens.remove(finalFilterTokens.size() - 1);
                    finalFilterTokens.add(prevToken + token);
                } else {
                    finalFilterTokens.add(token);
                }
            } else if (prevToken != null) {
                if (airthmeticOperators.contains(prevToken)) {
                    finalFilterTokens.remove(finalFilterTokens.size() - 1);
                    finalFilterTokens.add(prevToken + token);
                } else {
                    finalFilterTokens.add(token);
                }
            } else {
                finalFilterTokens.add(token);
            }
            idxCounter++;
        }

        int mode = 1; //1-operand,2-operator
        StringBuilder sb = new StringBuilder();
        for (String token : finalFilterTokens) {
            if (token.equals(")")) {
                sb.append(token);
                mode = 2;
                continue;
            } else if (token.equals("(")) {
                sb.append(token);
                mode = 1;
                continue;
            } else if (mode == 1) {
                Integer operator = mapOperatorPrecTab.get(token);
                if (operator != null) {
                    sb.append(" ");
                    sb.append(operandName);
                    sb.append(" ");
                    sb.append(token);
                    sb.append(" ");
                    mode = 1;
                } else {
                    mode = 2;
                    sb.append(token);
                }

            } else if (mode == 2) {
                sb.append(" ");
                sb.append(token);
                sb.append(" ");
                if (token.equals(")") || token.equals("(")) {
                    continue;
                }
                Integer operator = mapOperatorPrecTab.get(token);
                if (operator == null) {
                    throw new Exception("Invalid expression:operator not found");
                }
                mode = 1;
            }
        }

        return sb.toString().trim();
    }

    public static List<String> parseExpression(String expression) {
        expression = expression.toLowerCase();
        List<String> tokens = new ArrayList<String>();
        List<MyToken> lTokens = splitForQuotes(expression, false);
        for (MyToken myToken : lTokens) {
            String regExp = "(?<=and|or|&&|\\|\\||[()+\\-/><=]|[*%]|==|!=|>=|<=|not like|notlike|like|not in|notin|in)|(?=and|or|&&|\\|\\||[()+\\-/><=]|[*%]|==|!=|>=|<=|not like|notlike|like|not in|notin|in)";
            if (myToken.getType() == 0) {
                String[] arrToken = myToken.getToken().split(regExp);

                for (String token : arrToken) {
                    if (token.trim().length() > 0) {
                        tokens.add(token.trim().toLowerCase());
                    }
                }
            } else if (myToken.getType() == 1) {
                tokens.add("'" + myToken.getToken().toLowerCase() + "'");
            } else if (myToken.getType() == 2) {
                tokens.add("\"" + myToken.getToken().toLowerCase() + "\"");
            } else {
                tokens.add("`" + myToken.getToken().toLowerCase() + "`");
            }
        }
        // Filter for amguity operator
        List<String> filterTokens1 = new ArrayList<>();
        StringBuilder sbOperator = new StringBuilder();
        int idx = 0;
        boolean bSkip = false;
        for (String token : tokens) {
            try {
                if (bSkip) {
                    bSkip = false;
                    continue;
                }
                String operator = ambiguityOpratorMap.get(token);
                if (operator != null) {
                    String lastToken = (idx - 1) >= 0 ? tokens.get(idx - 1) : "";
                    String nextToken = (idx + 1) < (tokens.size()) ? tokens.get(idx + 1) : "";
                    String verifyTokenBehind = lastToken + token;
                    String verifyTokenAhead = token + nextToken;
                    if (operator.equalsIgnoreCase(verifyTokenBehind)) {
                        filterTokens1.remove(filterTokens1.size() - 1);
                        filterTokens1.add(verifyTokenBehind);
                    } else if (operator.equalsIgnoreCase(verifyTokenAhead)) {
                        bSkip = true;
                        filterTokens1.add(verifyTokenAhead.toLowerCase());
                    } else {
                        filterTokens1.add(token);
                    }
                } else {
                    filterTokens1.add(token);
                }
            } finally {
                idx++;
            }
        }

        List<String> filterTokens2 = new ArrayList<>();
        StringBuilder sbOperandTmp = new StringBuilder();
        boolean bFunFlag = false;
        boolean isFirstCharAfterFun = false;
        for (String token : filterTokens1) {
            if (!bFunFlag && (token.equals("date") || token.equals("datetime"))) {
                sbOperandTmp.append(token);
                bFunFlag = true;
                isFirstCharAfterFun = true;
                continue;
            } else if (bFunFlag) {
                if (!token.equals("(") && isFirstCharAfterFun) {
                    bFunFlag = false;
                    filterTokens2.add(sbOperandTmp.toString());
                    filterTokens2.add(token);
                    sbOperandTmp.delete(0, sbOperandTmp.toString().length());
                } else if (token.equals(")")) {
                    sbOperandTmp.append(token);
                    filterTokens2.add(sbOperandTmp.toString());
                    sbOperandTmp.delete(0, sbOperandTmp.toString().length());
                    bFunFlag = false;
                } else {
                    sbOperandTmp.append(token);
                }
                isFirstCharAfterFun = false;
            } else {
                filterTokens2.add(token);
            }
        }
        if (sbOperandTmp.toString().length() > 0) {
            filterTokens2.add(sbOperandTmp.toString());
        }
        List<String> filterTokens = new ArrayList<>();
        int lastToken = 0; //0-operand,1-operator
        int counter = 0;
        StringBuilder sbOperand = new StringBuilder();
        boolean bInFlag = false;

        for (String token : filterTokens2) {
            counter++;
            if (!bInFlag && (token.equals("("))) {
                if (sbOperand.toString().length() > 0) {
                    filterTokens.add(sbOperand.toString());
                    sbOperand.delete(0, sbOperand.toString().length());
                }
                filterTokens.add(token);
                continue;
            }
            if (token.equals(")")) {
                if (bInFlag) {
                    if (sbOperand.toString().length() > 0) {
                        sbOperand.append(token);
                        filterTokens.add(sbOperand.toString());
                        sbOperand.delete(0, sbOperand.toString().length());
                    } else {
                        filterTokens.add(token);
                    }
                    bInFlag = false;
                } else {
                    if (sbOperand.toString().length() > 0) {
                        filterTokens.add(sbOperand.toString());
                        sbOperand.delete(0, sbOperand.toString().length());
                    }
                    filterTokens.add(token);
                }
                continue;
            }
            if (bInFlag) {
                sbOperand.append(token);
                continue;
            }
            if (token.equalsIgnoreCase("in") || token.equalsIgnoreCase("notin")) {
                bInFlag = true;
            }
            if (sbOperand.toString().length() > 0) {
                filterTokens.add(sbOperand.toString());
                sbOperand.delete(0, sbOperand.toString().length());
            }
            filterTokens.add(token);
        }
        //Filter for -ve sign
        List<String> finalFilterTokens = new ArrayList<String>();

        int idxCounter = 0;
        for (String token : filterTokens) {
            String prevToken = (idxCounter - 1) >= 0 ? filterTokens.get(idxCounter - 1) : null;
            String prevToPrevToken = (idxCounter - 2) >= 0 ? filterTokens.get(idxCounter - 2) : null;

            if (prevToken != null && prevToPrevToken != null) {
                if (airthmeticOperators.contains(prevToken)
                        && mapOperatorPrecTab.get(prevToPrevToken) != null && !(prevToPrevToken.equals("(") || prevToPrevToken.equals(")"))) {
                    finalFilterTokens.remove(finalFilterTokens.size() - 1);
                    finalFilterTokens.add(prevToken + token);
                } else {
                    finalFilterTokens.add(token);
                }
            } else if (prevToken != null) {
                if (airthmeticOperators.contains(prevToken)) {
                    finalFilterTokens.remove(finalFilterTokens.size() - 1);
                    finalFilterTokens.add(prevToken + token);
                } else {
                    finalFilterTokens.add(token);
                }
            } else {
                finalFilterTokens.add(token);
            }
            idxCounter++;
        }

        return finalFilterTokens;
    }

    public static class VariableCounter {

        int counter = 0;

        public int getIncreamentedCounter() {
            return ++counter;
        }
    }

    public static Object getDataType(Node n1, Node n2, Node operator, AtbMetaData a1, AtbMetaData a2) throws Exception {
        return getDataType_(n1, n2, operator, a1, a2);

    }

    public static Object getDataType_(Node n1, Node n2, Node operator, AtbMetaData a1, AtbMetaData a2) throws Exception {
        Object ope1DataType = null;
        Object ope2DataType = null;
        boolean ope1DataTypeThroughRandomCheck = false;
        boolean ope2DataTypeThroughRandomCheck = false;
        if (n1.isVisited() && n1.getDataType() != null) {
            ope1DataType = n1.getDataType();
        } else if (a1 != null) {
            ope1DataType = a1.getDataType();
        } else {
            String data = n1.data;
            if (a2 != null && a2.getSelectedHeuristicAtbs() != null && a2.getSelectedHeuristicAtbs().size() > 0) {
                if (a2.getDataType() instanceof Long || a2.getDataType() instanceof Double) {
                    ope1DataType = new Double(-1);
                } else {
                    ope1DataType = ope2DataType;
                }
            } else {
                UserDefinedFunction fun = getDateFunction(data);
                if (fun != null) {
                    ope1DataType = new Date();
                } else {
                    Date dt = getParsedDate(data);
                    if (dt != null) {
                        ope1DataType = new Date();
                    } else if (operator.data.equalsIgnoreCase("like") || operator.data.equalsIgnoreCase("notlike")) {
                        ope1DataType = new String();
                    } else if (operator.data.equalsIgnoreCase("in")
                            || operator.data.equalsIgnoreCase("notin")) {
                        if ((data.startsWith("("))) {
                            data = data.substring(1, data.length() - 1);
                        }
                        if ((data.endsWith(")"))) {
                            data = data.substring(1, data.length() - 1);
                        }
                        List<String> lVal = splitForIn(data);
                        boolean isDoubleExists = false;
                        boolean isError = false;
                        boolean isDate = false;
                        for (String val : lVal) {
                            try {
                                Date date = getParsedDate(val);
                                if (date != null) {
                                    isDate = true;
                                } else {
                                    if (isDate) {
                                        throw new Exception("Incompatible data type:Date-Non Date");
                                    }
                                }
                            } catch (Exception e) {
                                if (isDate) {
                                    throw e;
                                }
                            }
                        }
                        boolean isDateExists = false;
                        for (String val : lVal) {
                            try {
                                Long.parseLong(val);
                                continue;
                            } catch (Exception ex) {
                            }
                            try {
                                Date date = getParsedDate(val);
                                if (date != null) {
                                    isDateExists = true;
                                }
                                continue;
                            } catch (Exception ex) {
                            }
                            try {
                                Double.parseDouble(val);
                                isDoubleExists = true;
                                continue;
                            } catch (Exception ex) {
                                isError = true;
                            }
                        }
                        if (isError) {
                            ope1DataType = new String();
                        } else if (isDateExists) {
                            ope1DataType = new Date();
                        } else if (isDoubleExists) {
                            ope1DataType = new Double(-1);
                        } else {
                            ope1DataType = new Long(-1);
                        }
                    } else {
                        boolean isDoubleExists = false;
                        boolean isError = false;
                        try {
                            Long.parseLong(data);
                        } catch (Exception ex) {
                            isError = true;
                        }
                        if (isError) {
                            try {
                                Double.parseDouble(data);
                                isDoubleExists = true;
                                isError = false;
                            } catch (Exception ex) {
                                isError = true;
                            }
                        }
                        if (isError) {
                            ope1DataType = new String();
                        } else if (isDoubleExists) {
                            ope1DataType = new Double(-1);
                        } else {
                            ope1DataType = new Long(-1);
                        }
                    }
                    ope1DataTypeThroughRandomCheck = true;
                }
            }
        }

        if (n2.isVisited() && n2.getDataType() != null) {
            ope2DataType = n2.getDataType();
        } else if (a2 != null) {
            ope2DataType = a2.getDataType();
        } else {
            String data = n2.data;
            if (a1 != null && a1.getSelectedHeuristicAtbs() != null && a1.getSelectedHeuristicAtbs().size() > 0) {
                if (a1.getDataType() instanceof Long || a1.getDataType() instanceof Double) {
                    ope2DataType = new Double(-1);
                } else {
                    ope2DataType = ope1DataType;
                }
            } else {

                UserDefinedFunction fun = getDateFunction(data);
                if (fun != null) {
                    ope2DataType = new Date();
                } else {
                    Date dt = getParsedDate(data);
                    if (dt != null) {
                        ope2DataType = new Date();
                    } else if (operator.data.equalsIgnoreCase("like") || operator.data.equalsIgnoreCase("notlike")) {
                        ope2DataType = new String();
                    } else if (operator.data.equalsIgnoreCase("in")
                            || operator.data.equalsIgnoreCase("notin")) {
                        if ((data.startsWith("("))) {
                            data = data.substring(1, data.length() - 1);
                        }
                        if ((data.endsWith(")"))) {
                            data = data.substring(1, data.length() - 1);
                        }
                        List<String> lVal = splitForIn(data);
                        boolean isDoubleExists = false;
                        boolean isError = false;
                        boolean isDate = false;
                        for (String val : lVal) {
                            try {
                                Date date = getParsedDate(val);
                                if (date != null) {
                                    isDate = true;
                                } else {
                                    if (isDate) {
                                        throw new Exception("Incompatible data type:Date-Non Date");
                                    }
                                }
                            } catch (Exception e) {
                                if (isDate) {
                                    throw e;
                                }
                            }
                        }
                        boolean isDateExists = false;
                        for (String val : lVal) {
                            try {
                                Long.parseLong(val);
                                continue;
                            } catch (Exception ex) {
                            }
                            try {
                                Date date = getParsedDate(val);
                                if (date != null) {
                                    isDateExists = true;
                                }
                                continue;
                            } catch (Exception ex) {
                            }
                            try {
                                Double.parseDouble(val);
                                isDoubleExists = true;
                                continue;
                            } catch (Exception ex) {
                                isError = true;
                            }

                        }
                        if (isError) {
                            ope2DataType = new String();
                        } else if (isDateExists) {
                            ope2DataType = new Date();
                        } else if (isDoubleExists) {
                            ope2DataType = new Double(-1);
                        } else {
                            ope2DataType = new Long(-1);
                        }
                    } else {
                        boolean isDoubleExists = false;
                        boolean isError = false;
                        try {
                            Long.parseLong(data);
                        } catch (Exception ex) {
                            isError = true;
                        }
                        if (isError) {
                            try {
                                Double.parseDouble(data);
                                isDoubleExists = true;
                                isError = false;
                            } catch (Exception ex) {
                                isError = true;
                            }
                        }
                        if (isError) {
                            ope2DataType = new String();
                        } else if (isDoubleExists) {
                            ope2DataType = new Double(-1);
                        } else {
                            ope2DataType = new Long(-1);
                        }
                    }
                    ope2DataTypeThroughRandomCheck = true;
                }
            }
        }

        if (ope1DataType instanceof Long) {
            ope1DataType = new Double(-1);
        }
        if (ope2DataType instanceof Long) {
            ope2DataType = new Double(-1);
        }
        if (ope1DataType instanceof Double) {
            if (ope2DataType instanceof Double) {
                return new Double(-1);
            } else {
                if (ope1DataTypeThroughRandomCheck && ope2DataTypeThroughRandomCheck) {
                    if (ope2DataType instanceof Double) {
                        return new Double(-1);
                    } else {
                        return new String();
                    }
                } else {
                    if (ope2DataType instanceof Double) {
                        return new Double(-1);
                    } else {
                        throw new Exception("Incompatible data type:Numeric-Non numeric");
                    }
                }
            }
        }
        if (ope1DataType instanceof Date) {
            if (ope2DataType instanceof Date) {
                return new Date(-1);
            } else {
                if (ope1DataTypeThroughRandomCheck && ope2DataTypeThroughRandomCheck) {
                    return new String();
                } else {
                    throw new Exception("Incompatible data type:Date-Non Date");
                }
            }
        }
        if (ope1DataType instanceof String) {
            if (ope2DataType instanceof String) {
                return new String();
            } else {
                if (ope1DataTypeThroughRandomCheck && ope2DataTypeThroughRandomCheck) {
                    return new String();
                } else if (ope1DataTypeThroughRandomCheck) {
                    throw new Exception("Incompatible data type:String-Non String");
                } else if (ope2DataTypeThroughRandomCheck) {
                    return new String();
                } else {
                    throw new Exception("Incompatible data type:String-Non String");
                }
            }
        }
        throw new Exception("Invalid data type:");
    }

    public static UserDefinedFunction getDateFunction(String exp) {
        exp = exp.toLowerCase();

        if (exp.equals("date()")) {
            UserDefinedFunction fun = new UserDefinedFunction();
            fun.setName("date()");
            return fun;
        } else if (exp.equals("datetime()")) {
            UserDefinedFunction fun = new UserDefinedFunction();
            fun.setName("datetime()");
            return fun;
        } else if (isDateExp(exp)) {
            UserDefinedFunction fun = new UserDefinedFunction();
            if (exp.endsWith("d")) {
                fun.setName("d");
                fun.setExparam(exp.substring(0, exp.length() - 1));
            } else if (exp.endsWith("h")) {
                fun.setName("h");
                fun.setExparam(exp.substring(0, exp.length() - 1));
            } else if (exp.endsWith("m")) {
                fun.setName("m");
                fun.setExparam(exp.substring(0, exp.length() - 1));
            } else if (exp.endsWith("s")) {
                fun.setName("s");
                fun.setExparam(exp.substring(0, exp.length() - 1));
            } else {
                fun.setName("d");
                fun.setExparam(exp);
            }
            return fun;
        } else {
            return null;
        }
    }

    public static String getQualifiedAttributeName(AtbMetaData atb) {
        if (atb == null) {
            return "";
        }
        return atb.getSourceName() + "_" + atb.getAtbName();
    }

    public static String getQualifiedDateFunctionName(RuleBuilder.VariableCounter counter) {
        return "fundate" + counter.getIncreamentedCounter();
    }

    public static String[] getQualifiedHeuristicAttributeNameRange(AtbMetaData atb, String selectedBean) {
        String[] arr = new String[2];
        if (atb == null) {
            arr[0] = "";
            arr[1] = "";
            return arr;
        }

        arr[0] = atb.getSourceName() + "_" + atb.getSelectedHeuristicAtbs().get(0).getName() + "_" + selectedBean + "_r1";
        arr[1] = atb.getSourceName() + "_" + atb.getSelectedHeuristicAtbs().get(0).getName() + "_" + selectedBean + "_r2";
        return arr;
    }

    public static String getQualifiedHeuristicAttributeNameKeyVal(AtbMetaData atb, String selectedBean) {
        if (atb == null) {
            return "";
        }
        return atb.getSourceName() + "_" + atb.getSelectedHeuristicAtbs().get(0).getName() + "_" + selectedBean;

    }

    public static boolean isDateExp(String exp) {
        try {
            //Pattern pattern = Pattern.compile("^(\\d+.*|-\\d+.*)");
//            Pattern pattern = Pattern.compile("^(\\+?\\d+.*|-?\\d+.*|\\d+.*)y$", Pattern.CASE_INSENSITIVE);
//            Matcher matcher = pattern.matcher(exp);
//            if (matcher.matches()) {
//                return true;
//            }
//            pattern = Pattern.compile("^(\\+?\\d+.*|-?\\d+.*|\\d+.*)m$", Pattern.CASE_INSENSITIVE);
//            matcher = pattern.matcher(exp);
//            if (matcher.matches()) {
//                return true;
//            }
//            Pattern pattern = Pattern.compile("(\\d+.*)", Pattern.CASE_INSENSITIVE);
//            Matcher matcher = pattern.matcher(exp);
//            if (matcher.matches()) {
//                return true;
//            }
            Pattern pattern = Pattern.compile("^(\\+?\\d+.*|-?\\d+.*|\\d+.*)d$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(exp);
            if (matcher.matches()) {
                return true;
            }
            pattern = Pattern.compile("^(\\+?\\d+.*|-?\\d+.*|\\d+.*)h$", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(exp);
            if (matcher.matches()) {
                return true;
            }
            pattern = Pattern.compile("^(\\+?\\d+.*|-?\\d+.*|\\d+.*)m$", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(exp);
            if (matcher.matches()) {
                return true;
            }

            pattern = Pattern.compile("^(\\+?\\d+.*|-?\\d+.*|\\d+.*)s$", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(exp);
            if (matcher.matches()) {
                return true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    public static Date getParsedDate(String date) {
        long lTime = -1;
        boolean errorOccured = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/SSS");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss-SSS");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss");
            Date dt = sdf.parse(date);
            return dt;
        } catch (Exception ex) {
        }

        return null;
    }

    public static boolean isFunctionExistsOnAttribute(String leafExp, String sourceName, String atbName) {
        sourceName = sourceName == null ? "" : sourceName;
        atbName = atbName == null ? "" : atbName;
        if (leafExp.startsWith("datetime(") && leafExp.endsWith(")")) {
            leafExp = leafExp.substring("datetime(".length(), leafExp.length() - 1);
        }
        if ((leafExp.startsWith("date(") && leafExp.endsWith(")"))) {
            leafExp = leafExp.substring("date(".length(), leafExp.length() - 1);
        }

        if (leafExp != null && leafExp.trim().length() > 0) {
            if (RuleUtil.isStartsAndEndsWith(leafExp, "`")) {
                leafExp = leafExp.substring(1, leafExp.length() - 1);
            }
            if ((sourceName + "." + atbName).equalsIgnoreCase(leafExp)) {
                return true;
            } else if (atbName.equalsIgnoreCase(leafExp)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static AtbMetaData getUsedAttribute(List<AtbMetaData> listSurceAtbs, String leafExp, Node node) throws Exception {
        if (node.isVisited()) {
            return null;
        }
        List<AtbMetaData> listUsedAtbs = new ArrayList<>();
        if (isStartsAndEndsWith(leafExp, "'")) {
            leafExp = leafExp.substring(1, leafExp.length() - 1);
            return null;
        }
        if (isStartsAndEndsWith(leafExp, "\"")) {
            leafExp = leafExp.substring(1, leafExp.length() - 1);
            return null;
        }

        boolean bMandatoryAtb = false;

        UserDefinedFunction udf = null;
        if (leafExp.startsWith("datetime(") && leafExp.endsWith(")")) {
            leafExp = leafExp.substring("datetime(".length(), leafExp.length() - 1);
            if (leafExp.trim().length() > 0) {
                bMandatoryAtb = true;
            }
            udf = getDateFunction("datetime()");
        }
        if ((leafExp.startsWith("date(") && leafExp.endsWith(")"))) {
            leafExp = leafExp.substring("date(".length(), leafExp.length() - 1);
            if (leafExp.trim().length() > 0) {
                bMandatoryAtb = true;
            }
            udf = getDateFunction("date()");
        }

        if (isStartsAndEndsWith(leafExp, "`")) {
            leafExp = leafExp.substring(1, leafExp.length() - 1);
            bMandatoryAtb = true;
        }
        if (leafExp != null && leafExp.trim().length() > 0) {
            for (AtbMetaData atbMetaData : listSurceAtbs) {
                AtbMetaData atb = new AtbMetaData();
                atb.setAtbName(atbMetaData.getAtbName());
                atb.setSourceName(atbMetaData.getSourceName());
                atb.setDataType(atbMetaData.getDataType());
                atb.setHeuristicAtbs(atbMetaData.getHeuristicAtbs());
                atb.setMultivalAction(atbMetaData.isMultivalAction());

                if (leafExp.equalsIgnoreCase(atbMetaData.getAtbName().toLowerCase())) {
                    listUsedAtbs.add(atb);
                } else if (leafExp.equalsIgnoreCase((atbMetaData.getSourceName() + "." + atbMetaData.getAtbName()).toLowerCase())) {
                    listUsedAtbs.add(atb);
                } else {
                    List<HeuristicAtb> listAtbs = atbMetaData.getHeuristicAtbs();
                    if (listAtbs != null) {
                        for (HeuristicAtb heuristicAtb : listAtbs) {
                            if (heuristicAtb.getName().equalsIgnoreCase(leafExp) || leafExp.equalsIgnoreCase(atbMetaData.getSourceName() + "." + heuristicAtb.getName())) {
                                atb.addSelectedHeuristicAtbs(heuristicAtb);
                                listUsedAtbs.add(atb);
                            }
                        }
                    }
                }
            }
        }
        if (listUsedAtbs.size() > 1) {
            throw new Exception("Ambiguity Error");
        }
        if (bMandatoryAtb && (listUsedAtbs == null || listUsedAtbs.size() == 0)) {
            throw new Exception("Unknown attribute :" + leafExp);
        }
        if (udf != null && listUsedAtbs.size() > 0) {
            listUsedAtbs.get(0).setFunction(udf);
        }
        return listUsedAtbs.size() == 0 ? null : listUsedAtbs.get(0);
    }

    public static AtbMetaData[] getSources(Map<String, Map<String, Object>> atbMap, Map<String, Map<String, Object>> atbSourceMap, String atbName) {
        List<AtbMetaData> sourceList = new ArrayList<AtbMetaData>();
        Set<String> parentSet = atbMap.keySet();
        for (String key : parentSet) {
            Map<String, Object> map = atbMap.get(key);
            Object dataType = map.get(atbName.toLowerCase());
            if (dataType != null) {
                AtbMetaData atb = new AtbMetaData();
                atb.setAtbName(atbName);
                atb.setSourceName(key);
                atb.setDataType(dataType);
                sourceList.add(atb);
            }
        }
        parentSet = atbSourceMap.keySet();
        for (String key : parentSet) {
            Map<String, Object> map = atbSourceMap.get(key);
            Object dataType = map.get(atbName.toLowerCase());
            if (dataType != null) {
                AtbMetaData atb = new AtbMetaData();
                atb.setAtbName(atbName.toLowerCase());
                atb.setSourceName(key);
                atb.setDataType(dataType);
                sourceList.add(atb);
            }
        }
        return sourceList.toArray(new AtbMetaData[0]);
    }

    public static List<AtbMetaData> mergeAtbMetaData(List<AtbMetaData> listUsedAtbs) throws Exception {
        List<AtbMetaData> listMergedAtbMetaData = new ArrayList<AtbMetaData>();
        for (AtbMetaData atbMetaData : listUsedAtbs) {
            AtbMetaData existAtb = null;
            for (AtbMetaData atbMetaData1 : listMergedAtbMetaData) {
                if (atbMetaData.getSourceName().equalsIgnoreCase(atbMetaData1.getSourceName()) && atbMetaData.getAtbName().equalsIgnoreCase(atbMetaData1.getAtbName())) {
                    existAtb = atbMetaData1;
                    break;
                }
            }
            if (existAtb != null) {
                if (existAtb.getDataType() instanceof Double) {
                    if (!(atbMetaData.getDataType() instanceof Double)) {
                        throw new Exception("DataType Mismatch");
                    }
                } else if (existAtb.getDataType() instanceof String) {
                    if (!(atbMetaData.getDataType() instanceof String)) {
                        throw new Exception("DataType Mismatch");
                    }
                } else {
                    if (!(atbMetaData.getDataType() instanceof Date)) {
                        throw new Exception("DataType Mismatch");
                    }
                }
                List<HeuristicAtb> list = atbMetaData.getSelectedHeuristicAtbs();
                List<HeuristicAtb> list1 = existAtb.getSelectedHeuristicAtbs();
                if (list != null && list.size() > 0) {
                    for (HeuristicAtb heuristicAtb : list) {
                        HeuristicAtb hExists = null;
                        if (list1 != null && list1.size() > 0) {
                            for (HeuristicAtb heuristicAtb1 : list1) {
                                if (heuristicAtb.getName().equalsIgnoreCase(heuristicAtb1.getName())) {
                                    hExists = heuristicAtb1;
                                    break;
                                }
                            }
                        }
                        if (hExists != null) {
                            List<String> beans = heuristicAtb.getSelectedBean();
                            if (beans != null && beans.size() > 0) {
                                for (String bean : beans) {
                                    List<String> beans1 = hExists.getSelectedBean();
                                    boolean bBeanExists = false;
                                    if (beans1 != null && beans1.size() > 0) {

                                        for (String bean1 : beans1) {
                                            if (bean.equalsIgnoreCase(bean1)) {
                                                bBeanExists = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!bBeanExists) {
                                        hExists.addToSelectedBeans(bean);
                                    }
                                }
                            }
                        } else {
                            existAtb.addSelectedHeuristicAtbs(heuristicAtb);
                        }
                    }
                }
            } else {
                listMergedAtbMetaData.add(atbMetaData);
            }
        }
        return listMergedAtbMetaData;
    }

    public static String generateJavaCode(List<Rule> ruleSet, String ruleClassName) throws Exception {
        StringBuilder sbCode = new StringBuilder();

        sbCode.append("import java.util.*;");
        sbCode.append("import java.lang.*;");
        sbCode.append("import java.math.*;");
        sbCode.append("import java.util.regex.Pattern;");
        sbCode.append("import java.util.regex.Matcher;");
        sbCode.append("public class _" + ruleClassName + "{");
        boolean isRegHexExists = false;
        for (Rule rule : ruleSet) {
            List<RegularExpression> list = rule.getRegExs();
            if (list != null && list.size() > 0) {
                isRegHexExists = true;
                break;
            }
        }
        if (isRegHexExists) {
            for (Rule rule : ruleSet) {
                List<RegularExpression> list = rule.getRegExs();
                if (list != null && list.size() > 0) {
                    for (RegularExpression regularExpression : list) {
                        Pattern pattern = Pattern.compile("^.txt$", Pattern.CASE_INSENSITIVE);
                        sbCode.append("public static Pattern");
                        sbCode.append(" " + regularExpression.getPatternName() + " ");
                        sbCode.append("=");
                        sbCode.append("Pattern.compile(");
                        sbCode.append("\"").append(regularExpression.getRegEx()).append("\"");
                        sbCode.append(",");
                        sbCode.append("Pattern.CASE_INSENSITIVE);");
                    }
                }
            }
        }

        for (Rule rule : ruleSet) {
            sbCode.append("public static boolean _" + rule.getRuleId());
            sbCode.append("(Map<String,Object> keyVal) throws Throwable");
            sbCode.append("{");
            sbCode.append("try{");
            int dateCalCounter = 0;
            int dateTimeCalCounter = 0;
            if (rule.getFunctions() != null && rule.getFunctions().size() > 0) {
                List<UserDefinedFunction> functions = rule.getFunctions();

                for (UserDefinedFunction userDefinedFunction : functions) {
                    if (userDefinedFunction.getName().equalsIgnoreCase("date()")) {
                        if (dateCalCounter == 0) {
                            sbCode.append("Calendar cDate = Calendar.getInstance();");
                            sbCode.append("cDate.set(Calendar.HOUR, 00);");
                            sbCode.append("cDate.set(Calendar.MINUTE, 00);");
                            sbCode.append("cDate.set(Calendar.SECOND, 00);");
                            sbCode.append("cDate.set(Calendar.MILLISECOND, 00);");
                        }
                        sbCode.append("BigDecimal ");
                        sbCode.append(userDefinedFunction.getNameUsedInExp());
                        sbCode.append("=");
                        sbCode.append("new BigDecimal(cDate.getTime().getTime()+\"\").setScale(2, BigDecimal.ROUND_HALF_UP);");
                        dateCalCounter++;
                    } else if (userDefinedFunction.getName().equalsIgnoreCase("datetime()")) {
                        if (dateTimeCalCounter == 0) {
                            sbCode.append("Calendar cDateTime = Calendar.getInstance();");
                        }
                        sbCode.append("BigDecimal ");
                        sbCode.append(userDefinedFunction.getNameUsedInExp());
                        sbCode.append("=");
                        sbCode.append("new BigDecimal(cDateTime.getTime().getTime()+\"\").setScale(2, BigDecimal.ROUND_HALF_UP);");
                        dateTimeCalCounter++;
                    } else if (userDefinedFunction.getName().equalsIgnoreCase("d")) {
                        sbCode.append("BigDecimal ");
                        sbCode.append(userDefinedFunction.getNameUsedInExp());
                        sbCode.append("=");
                        sbCode.append("new BigDecimal(" + userDefinedFunction.getExparam() + "*24*60*60*1000+\"\").setScale(2, BigDecimal.ROUND_HALF_UP);");
                    } else if (userDefinedFunction.getName().equalsIgnoreCase("h")) {
                        sbCode.append("BigDecimal ");
                        sbCode.append(userDefinedFunction.getNameUsedInExp());
                        sbCode.append("=");
                        sbCode.append("new BigDecimal(" + userDefinedFunction.getExparam() + "*60*60*1000+\"\").setScale(2, BigDecimal.ROUND_HALF_UP);");
                    } else if (userDefinedFunction.getName().equalsIgnoreCase("m")) {
                        sbCode.append("BigDecimal ");
                        sbCode.append(userDefinedFunction.getNameUsedInExp());
                        sbCode.append("=");
                        sbCode.append("new BigDecimal(" + userDefinedFunction.getExparam() + "*60*1000+\"\").setScale(2, BigDecimal.ROUND_HALF_UP);");
                    } else if (userDefinedFunction.getName().equalsIgnoreCase("s")) {
                        sbCode.append("BigDecimal ");
                        sbCode.append(userDefinedFunction.getNameUsedInExp());
                        sbCode.append("=");
                        sbCode.append("new BigDecimal(" + userDefinedFunction.getExparam() + "*1000+\"\").setScale(2, BigDecimal.ROUND_HALF_UP);");
                    }
                }
            }
            if (rule.getAtbs() != null && rule.getAtbs().size() > 0) {
                for (AtbMetaData atb : rule.getAtbs()) {
                    if (atb.getDataType() instanceof Double) {
                        String usedVarName = atb.getSourceName() + "_" + atb.getAtbName();
                        if (atb.isMultivalAction()) {
                            sbCode.append("List<BigDecimal> " + usedVarName);
                            sbCode.append("=");
                            sbCode.append("(List<BigDecimal>)keyVal.get(\"" + usedVarName + "\");");
                        } else {
                            sbCode.append("BigDecimal " + usedVarName);
                            sbCode.append("=");
                            sbCode.append("(BigDecimal)keyVal.get(\"" + usedVarName + "\");");
                        }
                        if (atb.getSelectedHeuristicAtbs() != null && atb.getSelectedHeuristicAtbs().size() > 0) {
                            for (HeuristicAtb ha : atb.getSelectedHeuristicAtbs()) {
                                if (ha.isRangeBased()) {
                                    List<String> beans = ha.getSelectedBean();
                                    if (beans != null && beans.size() > 0) {
                                        for (String bean : beans) {
                                            String haR1 = atb.getSourceName() + "_" + ha.getName() + "_" + bean + "_r1";
                                            String haR2 = atb.getSourceName() + "_" + ha.getName() + "_" + bean + "_r2";
                                            sbCode.append("BigDecimal " + haR1);
                                            sbCode.append("=");
                                            sbCode.append("(BigDecimal)keyVal.get(\"" + haR1 + "\");");
                                            sbCode.append("BigDecimal " + haR2);
                                            sbCode.append("=");
                                            sbCode.append("(BigDecimal)keyVal.get(\"" + haR2 + "\");");
                                        }
                                    }
                                } else {
                                    List<String> beans = ha.getSelectedBean();
                                    if (beans != null && beans.size() > 0) {
                                        for (String bean : beans) {
                                            String haKeyVal = atb.getSourceName() + "_" + ha.getName() + "_" + bean;
                                            sbCode.append("BigDecimal " + haKeyVal);
                                            sbCode.append("=");
                                            sbCode.append("(BigDecimal)keyVal.get(\"" + haKeyVal + "\");");
                                        }
                                    }
                                }
                            }
                        }
                    } else if (atb.getDataType() instanceof String) {
                        String usedVarName = atb.getSourceName() + "_" + atb.getAtbName();
                        if (atb.isMultivalAction()) {
                            sbCode.append("List<String> " + usedVarName);
                            sbCode.append("=");
                            sbCode.append("(List<String>)keyVal.get(\"" + usedVarName + "\");");
                        } else {
                            sbCode.append("String " + usedVarName);
                            sbCode.append("=");
                            sbCode.append("(String)keyVal.get(\"" + usedVarName + "\");");
                        }
                        if (atb.getSelectedHeuristicAtbs() != null && atb.getSelectedHeuristicAtbs().size() > 0) {
                            for (HeuristicAtb ha : atb.getSelectedHeuristicAtbs()) {
                                if (ha.isRangeBased()) {
                                    List<String> beans = ha.getSelectedBean();
                                    if (beans != null && beans.size() > 0) {
                                        for (String bean : beans) {
                                            String haR1 = atb.getSourceName() + "_" + ha.getName() + "_" + bean + "_r1";
                                            String haR2 = atb.getSourceName() + "_" + ha.getName() + "_" + bean + "_r2";
                                            sbCode.append("String " + haR1);
                                            sbCode.append("=");
                                            sbCode.append("(String)keyVal.get(\"" + haR1 + "\");");
                                            sbCode.append("String " + haR2);
                                            sbCode.append("=");
                                            sbCode.append("(String)keyVal.get(\"" + haR2 + "\");");
                                        }
                                    }
                                } else {
                                    List<String> beans = ha.getSelectedBean();
                                    if (beans != null && beans.size() > 0) {
                                        for (String bean : beans) {
                                            String haKeyVal = atb.getSourceName() + "_" + ha.getName() + "_" + bean;
                                            sbCode.append("String " + haKeyVal);
                                            sbCode.append("=");
                                            sbCode.append("(String)keyVal.get(\"" + haKeyVal + "\");");
                                        }
                                    }
                                }
                            }
                        }
                    } else if (atb.getDataType() instanceof Date) {
                        if (atb.getFunction() != null) {
                            UserDefinedFunction udf = atb.getFunction();
                            if (udf.getName().equalsIgnoreCase("date()")) {
                                String usedVarName = atb.getSourceName() + "_" + atb.getAtbName() + "_date";
                                sbCode.append("BigDecimal " + udf.getNameUsedInExp());
                                sbCode.append("=");
                                sbCode.append("(BigDecimal)keyVal.get(\"" + usedVarName + "\");");
                            } else if (udf.getName().equalsIgnoreCase("datetime()")) {
                                String usedVarName = atb.getSourceName() + "_" + atb.getAtbName() + "_datetime";
                                sbCode.append("BigDecimal ");
                                sbCode.append(udf.getNameUsedInExp());
                                sbCode.append("=");
                                sbCode.append("(BigDecimal)keyVal.get(\"" + usedVarName + "\");");
                            } else {
                                throw new Exception("Invalid function" + udf.getName() + ":On date type attribute");
                            }
                        } else {
                            String usedVarName = atb.getSourceName() + "_" + atb.getAtbName();
                            if (atb.isMultivalAction()) {
                                sbCode.append("List<BigDecimal> " + usedVarName);
                                sbCode.append("=");
                                sbCode.append("(List<BigDecimal>)keyVal.get(\"" + usedVarName + "\");");
                            } else {
                                sbCode.append("BigDecimal " + usedVarName);
                                sbCode.append("=");
                                sbCode.append("(BigDecimal)keyVal.get(\"" + usedVarName + "\");");
                            }
                        }
                    }
                }
            }
            sbCode.append("if(" + rule.getJavaExp() + ")");
            sbCode.append("{").append("return true;").append("}");
            sbCode.append("else {return false;}");
            sbCode.append("}");

            sbCode.append("catch(Throwable th)");
            sbCode.append("{");
            sbCode.append("th.printStackTrace();");
            sbCode.append("return false;");
            sbCode.append("}");
            sbCode.append("}");

        }
        sbCode.append("}");
        return sbCode.toString();
    }

    public static String generateExpression(String expName, List<AtbMetaData> listAtbMetaData,
            String expression, List<AtbMetaData> listUsedAtbs,
            List<UserDefinedFunction> listUsedFunctions, List<RegularExpression> listRegEx) throws Exception {
        expName = expName == null ? "" : expName.trim();
        expName = expName.replaceAll(" ", "");
//        org.apache.log4j.Logger.getLogger(RuleBuilder.class).debug("Expression:" + expression);
        if (expression == null || expression.trim().length() <= 0) {
            throw new Exception("No tokens available");
        }
        expression = expression.toLowerCase();
        Tree t1 = new Tree();
        List<String> tokens = parseExpression(expression);
        if (tokens == null || tokens.size() < 3) {
            throw new Exception("Not enough tokens available");
        }
        t1.insert(tokens);
        return t1.traverse(listAtbMetaData, listUsedAtbs, listUsedFunctions, listRegEx, expName);
    }

    public static String truncateQuotes(String sourceStr) {
        if (sourceStr == null) {
            return null;
        }
        List<MyToken> listTemp = new ArrayList<MyToken>();
        char[] cArr = sourceStr.toCharArray();
        StringBuilder sb = new StringBuilder();

        boolean bStartOfToken = false;
        char prevchar = '\0';
        int count = 0;
        for (char c : cArr) {
            count++;
            if (count == 1) {
                if (c == '\'' || c == '\"' || c == '`') {
                    continue;
                } else {
                    sb.append(c);
                }
            } else if (count == cArr.length) {
                if (c == '\'' || c == '\"' || c == '`') {
                    if (prevchar == '\\') {
                        //sb.deleteCharAt(sb.length() - 1);
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
            prevchar = c;
        }
        return sb.toString();
    }

    private static List<MyToken> splitForQuotes(String sourceStr, boolean removeEscapeChar) {
        if (sourceStr == null) {
            return null;
        }
        List<MyToken> listTemp = new ArrayList<MyToken>();
        char[] cArr = sourceStr.toCharArray();
        StringBuilder sb = new StringBuilder();

        boolean bStartOfToken = false;
        char prevchar = '\0';
        for (char c : cArr) {
            if (prevchar != '\\') {
                if (c == '\'' || c == '\"' || c == '`') {
                    if (!bStartOfToken) {
                        if (sb.toString().length() > 0) {
                            MyToken tok = new MyToken();
                            tok.setToken(sb.toString());
                            tok.setType(0);
                            listTemp.add(tok);
                        }
                        sb = new StringBuilder();
                        bStartOfToken = true;
                    } else {
                        MyToken tok = new MyToken();
                        if (c == '\'') {
                            tok.setType(1);
                        } else if (c == '"') {
                            tok.setType(2);
                        } else {
                            tok.setType(3);
                        }
                        tok.setToken(sb.toString());
                        listTemp.add(tok);
                        bStartOfToken = false;
                        sb = new StringBuilder();
                    }
                }
            }
            if (c == '\'' || c == '\"' || c == '`') {
                if (prevchar == '\\') {
                    if (removeEscapeChar) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
            prevchar = c;
        }
        if (sb.toString().length() > 0) {
            MyToken tok = new MyToken();
            tok.setType(0);
            tok.setToken(sb.toString());
            listTemp.add(tok);
            sb.delete(0, sb.toString().length());
        }
        return listTemp;
    }

    public static List<String> splitForIn(String sourceStr) {

        if (sourceStr == null) {
            return null;
        }

        List<String> listTemp = new ArrayList<>();
        char[] cArr = sourceStr.toCharArray();
        StringBuilder sb = new StringBuilder();

        boolean bStartOfToken = false;
        char prevchar = '\0';
        for (char c : cArr) {
            if (prevchar != '\\') {
                if (c == '\'' || c == '\"') {
                    if (!bStartOfToken) {
                        bStartOfToken = true;
                        if (sb.toString().length() > 0) {
                            listTemp.add(sb.toString());
                        }
                        sb = new StringBuilder();
                    } else {
                        listTemp.add(sb.toString());
                        sb = new StringBuilder();
                        bStartOfToken = false;
                    }
                }
            }
            if (c == '\'' || c == '\"') {
                if (prevchar == '\\') {
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
            prevchar = c;
        }
        if (sb.toString().length() > 0) {
            listTemp.add(sb.toString());
            sb.delete(0, sb.toString().length());
        }

        bStartOfToken = false;
        ArrayList<String> listTokens = new ArrayList<String>();
        for (String s : listTemp) {
            cArr = s.toCharArray();
            prevchar = '\0';
            for (char c : cArr) {
                if (prevchar != '\\') {
                    if (c == ',') {
                        listTokens.add(sb.toString());
                        sb = new StringBuilder();
                        bStartOfToken = false;
                    }
                }
                if (c == ',') {
                    if (prevchar == '\\') {
                        sb.deleteCharAt(sb.length() - 1);
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
                prevchar = c;
            }
        }

        if (sb.toString().length() > 0) {
            if (sb.toString().endsWith(",")) {
                int indexDelimiter = sb.toString().toLowerCase().lastIndexOf(",");
                if (indexDelimiter != -1) {
                    if (indexDelimiter > 0) {
                        String escapeChar = sb.substring(indexDelimiter - 1, indexDelimiter);
                        if (escapeChar.equals("\\")) {
                            sb.deleteCharAt(indexDelimiter - 1);
                            String source = sb.toString();
                            listTokens.add(source);
                        } else {
                            String source = sb.substring(0, (sb.length() - (",").length()));
                            listTokens.add(source);
                        }
                    } else {
                        String source = sb.substring(0, (sb.length() - (",").length()));
                        listTokens.add(source);
                    }
                } else {
                    listTokens.add(sb.toString());
                    sb.delete(0, sb.toString().length());
                }
            } else {
                listTokens.add(sb.toString());
                sb.delete(0, sb.toString().length());
            }
        }
        return listTokens;
    }

    public static String prepareRegEx(Node node, String sourceStr,
            List<RegularExpression> regExs, RuleBuilder.VariableCounter counter, String expName) throws Exception {
        if (node.isVisited()) {
            return node.expData;
        }
        if (mapOperatorPrecTab.get(sourceStr) != null) {
            return sourceStr;
        }

        sourceStr = truncateQuotes(sourceStr);
        RegularExpression regEx = new RegularExpression();
        regEx.setPatternName("regEx_" + expName + "_" + counter.getIncreamentedCounter());
        regEx.setRegEx(sourceStr);
        regExs.add(regEx);
        try {
            Pattern.compile(sourceStr).matcher("test").matches();
        } catch (Exception ex) {
            throw new Exception("Invalid Regular Expression:" + ex.getMessage());
        }
        return regEx.getPatternName();
    }

    public static String prepareDateDataTypeVal(Node node, String sourceStr,
            List<UserDefinedFunction> funs, AtbMetaData atb, RuleBuilder.VariableCounter counter) throws Exception {
        if (node.isVisited()) {
            return node.expData;
        }
        if (mapOperatorPrecTab.get(sourceStr) != null) {
            return sourceStr;
        }
        sourceStr = truncateQuotes(sourceStr);
        if (atb == null || atb.getFunction() == null) {
            UserDefinedFunction fun1 = RuleBuilder.getDateFunction(sourceStr);
            if (fun1 != null) {
                fun1.setNameUsedInExp(getQualifiedDateFunctionName(counter));
                funs.add(fun1);
                return fun1.getNameUsedInExp();
            } else {
                Date dt = getParsedDate(sourceStr);
                if (dt == null) {
                    throw new Exception("Invalid date value");
                }
                StringBuilder sb = new StringBuilder();
                sb.append("new BigDecimal(\"" + dt.getTime() + "\").setScale(2, BigDecimal.ROUND_HALF_UP)");
                return sb.toString();
            }
        } else {
            UserDefinedFunction udf = atb.getFunction();
            udf.setNameUsedInExp(getQualifiedDateFunctionName(counter));
            return udf.getNameUsedInExp();
        }
    }

    public static String prepareDataTypeVal(Node node, String sourceStr, Object dataType) throws Exception {
        if (node.isVisited()) {
            return node.expData;
        }
        if (mapOperatorPrecTab.get(sourceStr) != null) {
            return sourceStr;
        }
        if (dataType instanceof Long) {
            StringBuilder sb = new StringBuilder();
            sb.append("new BigDecimal(\"" + sourceStr + "\").setScale(2, BigDecimal.ROUND_HALF_UP)");
            return sb.toString();
        } else if (dataType instanceof Double) {
            StringBuilder sb = new StringBuilder();
            sb.append("new BigDecimal(\"" + sourceStr + "\").setScale(2, BigDecimal.ROUND_HALF_UP)");
            return sb.toString();
        } else if (dataType instanceof Date) {
            if (sourceStr.toLowerCase().startsWith("fundate")) {
                return sourceStr;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("new BigDecimal(\"" + sourceStr + "\").setScale(2, BigDecimal.ROUND_HALF_UP)");
            return sb.toString();
        } else if (dataType instanceof String) {
            List<String> listTemp = new ArrayList<>();
            char[] cArr = sourceStr.toCharArray();
            StringBuilder sb = new StringBuilder();

            boolean bStartOfToken = false;
            char prevchar = '\0';
            for (char c : cArr) {
                if (prevchar != '\\') {
                    if (c == '\'' || c == '\"') {
                        if (!bStartOfToken) {
                            bStartOfToken = true;
                        } else {
                            listTemp.add(sb.toString());
                            sb = new StringBuilder();
                            bStartOfToken = false;
                        }
                    }
                }
                if (c == '\'' || c == '\"') {
                    if (prevchar == '\\') {
                        //sb.deleteCharAt(sb.length() - 1);
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
                prevchar = c;
            }
            if (sb.toString().length() > 0) {
                listTemp.add(sb.toString());
                sb.delete(0, sb.toString().length());
            }

            bStartOfToken = false;
            ArrayList<String> listTokens = new ArrayList<String>();
            for (String s : listTemp) {
                sb.append("\"");
                sb.append(s);
                sb.append("\"");
            }
            return sb.toString();
        } else {
            throw new Exception("Invalid DataType");
        }
    }
}
