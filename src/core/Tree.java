/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import Util.RuleUtil;
import beans.AtbMetaData;
import beans.HeuristicAtb;
import beans.Node;
import beans.RegularExpression;
import beans.UserDefinedFunction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import static core.RuleBuilder.getQualifiedAttributeName;
import static core.RuleBuilder.getQualifiedHeuristicAttributeNameKeyVal;
import static core.RuleBuilder.getQualifiedHeuristicAttributeNameRange;
import static core.RuleBuilder.getUsedAttribute;

/**
 *
 * @author jagadeesh.t
 */
public class Tree {

    private Node root;

    public Tree() {
        root = null;
    }

    public void insert(List<String> tokens) throws Exception {
        Conversion c = new Conversion(tokens);
        tokens = c.inToPost();
        Stack1 stk = new Stack1(tokens.size());
        int i = 0;
        Node newNode;
        for (String symbol : tokens) {
            Integer opPriority = RuleBuilder.getMapOperatorPrecTab().get(symbol.toLowerCase());
            if (opPriority != null) {
                if (stk.isEmpty()) {
                    throw new Exception("Invalid use of operator:" + symbol);
                }
                Node ptr1 = stk.pop();
                if (stk.isEmpty()) {
                    throw new Exception("Invalid use of operator:" + symbol);
                }
                Node ptr2 = stk.pop();
                newNode = new Node(symbol);
                newNode.leftChild = ptr2;
                newNode.rightChild = ptr1;
                stk.push(newNode);
            } else {
                newNode = new Node(symbol);
                stk.push(newNode);
            }
        }
        root = stk.pop();
        if (!stk.isEmpty()) {
            throw new Exception("Invalid Expression");
        }
    }

    public String traverse(List<AtbMetaData> listAtbMetaData,
            List<AtbMetaData> listUsedAtbs,
            List<UserDefinedFunction> listUsedFunctions, List<RegularExpression> listRegEx, String expName) throws Exception {
        StringBuilder sbRes = new StringBuilder();
        inOrder(listAtbMetaData, root, sbRes, listUsedAtbs, listUsedFunctions, listRegEx, expName);
        return sbRes.toString();
    }

    private void inOrder(List<AtbMetaData> listAtbMetaData,
            Node localRoot, StringBuilder sbRes,
            List<AtbMetaData> listUsedAtbs,
            List<UserDefinedFunction> listUsedFunctions, List<RegularExpression> listUsedRegExs, String expName) throws Exception {
        Stack<Node> stack = new Stack<>();
        Node current = localRoot;
        int counter = 0;
        Node prevNode = null;
        //stack.push(current);
        boolean bIsConditionProcessed = false;
        RuleBuilder.VariableCounter varCounter = new RuleBuilder.VariableCounter();
        String lastOperator = "";
        boolean bLogicalOperatorFound = false;
        while (!stack.isEmpty() || current != null) {
            counter++;
            while (current != null) {
                if (current.rightChild != null) {
                    stack.push(current.rightChild);
                }
                stack.push(current);
                current = current.leftChild;
            }

            current = stack.pop();
            if (current.rightChild != null) {
                if (!stack.isEmpty()) {
                    Node top = stack.peek();
                    if (current.rightChild == top) {
                        stack.pop();
                        stack.push(current);
                        current = current.rightChild;
                        continue;
                    }
                }
            }
            Integer priority = RuleBuilder.getMapOperatorPrecTab().get(current.data);
            if (priority != null) {
                if (!current.isVisited()) {
                    Node leftOperand = current.leftChild;
                    Node rightOperand = current.rightChild;
                    if (leftOperand != null && rightOperand != null) {
                        if (leftOperand == null || rightOperand == null) {
                            throw new Exception("Invalid use of operator");
                        }
                        bIsConditionProcessed = true;
                        String relationalOperator = current.data;
                        List<AtbMetaData> listAtbs = new ArrayList<>();
                        AtbMetaData usedAtbCur = getUsedAttribute(listAtbMetaData, leftOperand.data, leftOperand);
                        AtbMetaData usedAtbRight = getUsedAttribute(listAtbMetaData, rightOperand.data, rightOperand);
                        if (usedAtbCur != null) {
                            listUsedAtbs.add(usedAtbCur);
                        }
                        if (usedAtbRight != null) {
                            listUsedAtbs.add(usedAtbRight);
                        }
                        if ((RuleBuilder.getLogicalOperators().contains(current.data.toLowerCase()))) {
                            bLogicalOperatorFound = true;
                            int length = leftOperand.getVariables() != null ? leftOperand.getVariables().size() : 0;
                            if (length > 0) {
                                StringBuilder sbIfNullCond = new StringBuilder();
                                sbIfNullCond.append("(");
                                int trCounter = 0;
                                for (String parameterName : leftOperand.getVariables()) {
                                    trCounter++;
                                    sbIfNullCond.append(parameterName).append("!=").append("null");
                                    if (trCounter < length) {
                                        sbIfNullCond.append(" && ");
                                    }
                                }
                                sbIfNullCond.append(")");
                                leftOperand.expData = "(" + sbIfNullCond.toString() + " && (" + leftOperand.expData + "))";
                            }
                            length = rightOperand.getVariables() != null ? rightOperand.getVariables().size() : 0;
                            if (length > 0) {
                                StringBuilder sbIfNullCond = new StringBuilder();
                                sbIfNullCond.append("(");
                                int trCounter = 0;
                                for (String parameterName : rightOperand.getVariables()) {
                                    trCounter++;
                                    sbIfNullCond.append(parameterName).append("!=").append("null");
                                    if (trCounter < length) {
                                        sbIfNullCond.append(" && ");
                                    }
                                }
                                sbIfNullCond.append(")");
                                rightOperand.expData = "(" + sbIfNullCond.toString() + " && (" + rightOperand.expData + "))";
                            }
                        }
                        prepareCondition(leftOperand, current, rightOperand, usedAtbCur, usedAtbRight, listUsedFunctions, listUsedRegExs, varCounter, expName);
                        if (usedAtbCur != null) {
                            current.addToVariables(usedAtbCur.getSourceName() + "_" + usedAtbCur.getAtbName());
                            if (usedAtbCur.getSelectedHeuristicAtbs() != null) {
                                List<HeuristicAtb> list = usedAtbCur.getSelectedHeuristicAtbs();
                                for (HeuristicAtb heuristicAtb : list) {
                                    if (heuristicAtb.isRangeBased()) {
                                        List<String> beans = heuristicAtb.getSelectedBean();
                                        if (beans != null && beans.size() > 0) {
                                            for (String bean : beans) {
                                                String haR1 = usedAtbCur.getSourceName() + "_" + heuristicAtb.getName() + "_" + bean + "_r1";
                                                String haR2 = usedAtbCur.getSourceName() + "_" + heuristicAtb.getName() + "_" + bean + "_r2";
                                                current.addToVariables(haR1);
                                                current.addToVariables(haR2);
                                            }
                                        }
                                    } else {
                                        List<String> beans = heuristicAtb.getSelectedBean();
                                        if (beans != null && beans.size() > 0) {
                                            for (String bean : beans) {
                                                String haKeyVal = usedAtbCur.getSourceName() + "_" + heuristicAtb.getName() + "_" + bean;
                                                current.addToVariables(haKeyVal);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (usedAtbRight != null) {
                            current.addToVariables(usedAtbRight.getSourceName() + "_" + usedAtbRight.getAtbName());
                            if (usedAtbRight.getSelectedHeuristicAtbs() != null) {
                                List<HeuristicAtb> list = usedAtbRight.getSelectedHeuristicAtbs();
                                for (HeuristicAtb heuristicAtb : list) {
                                    if (heuristicAtb.isRangeBased()) {
                                        List<String> beans = heuristicAtb.getSelectedBean();
                                        if (beans != null && beans.size() > 0) {
                                            for (String bean : beans) {
                                                String haR1 = usedAtbRight.getSourceName() + "_" + heuristicAtb.getName() + "_" + bean + "_r1";
                                                String haR2 = usedAtbRight.getSourceName() + "_" + heuristicAtb.getName() + "_" + bean + "_r2";
                                                current.addToVariables(haR1);
                                                current.addToVariables(haR2);
                                            }
                                        }
                                    } else {
                                        List<String> beans = heuristicAtb.getSelectedBean();
                                        if (beans != null && beans.size() > 0) {
                                            for (String bean : beans) {
                                                String haKeyVal = usedAtbRight.getSourceName() + "_" + heuristicAtb.getName() + "_" + bean;
                                                current.addToVariables(haKeyVal);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        current.expData = "(" + current.expData + ")";
                    } else {
                        throw new Exception("Invalid Expression");
                    }
                    current.setVisited(true);
                    leftOperand.setVisited(true);
                    rightOperand.setVisited(true);
                    lastOperator = current.data;
                } else {
                    bIsConditionProcessed = false;
                }
            } else {
                bIsConditionProcessed = false;
            }
//                current.setVisited(true);
            prevNode = current;
            current = null;
            //current = current.rightChild;
        }

        if (!bIsConditionProcessed) {
            throw new Exception("Invalid Expression");
        } else {
            if (!(lastOperator.equals("<") || lastOperator.equals("<=")
                    || lastOperator.equals(">") || lastOperator.equals(">=")
                    || lastOperator.equals("==") || lastOperator.equals("!=") || lastOperator.equals(".contains")
                    || lastOperator.equals("and") || lastOperator.equals("or") || lastOperator.equals("&&")
                    || lastOperator.equals("||") || lastOperator.equals("in") || lastOperator.equals("notin")
                    || lastOperator.equals("like") || lastOperator.equals("notlike"))) {

                throw new Exception("Invalid Expression:Expression root must be relational or logical oprator");
            }
        }
        if (!bLogicalOperatorFound) {
            int length = localRoot.getVariables() != null ? localRoot.getVariables().size() : 0;
            if (length > 0) {
                StringBuilder sbIfNullCond = new StringBuilder();
                sbIfNullCond.append("(");
                int trCounter = 0;
                for (String parameterName : localRoot.getVariables()) {
                    trCounter++;
                    sbIfNullCond.append(parameterName).append("!=").append("null");
                    if (trCounter < length) {
                        sbIfNullCond.append(" && ");
                    }
                }
                sbIfNullCond.append(")");
                localRoot.expData = "(" + sbIfNullCond.toString() + " && (" + localRoot.expData + "))";
            }
        }
        sbRes.append(localRoot.expData);
        //inorderRecursiveTraversal(localRoot, sbRes);
    }

    public String getBigDecimalExpForROperator(String operator) throws Exception {
        if (operator.equals(">")) {
            return ">0";
        } else if (operator.equals("<")) {
            return "<0";
        } else if (operator.equals(">=")) {
            return ">=0";
        } else if (operator.equals("<=")) {
            return "<=0";
        } else {
            throw new Exception("Invalid relational operator for Big decimal expression:" + operator);
        }
    }

    public String getBigDecimalExpForAirOperator(String operand1, String operand2, String operator) throws Exception {
        if (operator.equals("+")) {
            return operand1 + ".add(" + operand2 + ").setScale(2, BigDecimal.ROUND_HALF_UP)";
        } else if (operator.equals("-")) {
            return operand1 + ".subtract(" + operand2 + ").setScale(2, BigDecimal.ROUND_HALF_UP)";
        } else if (operator.equals("*")) {
            return operand1 + ".multiply(" + operand2 + ").setScale(2, BigDecimal.ROUND_HALF_UP)";
        } else if (operator.equals("/")) {
            return operand1 + ".divide(" + operand2 + ").setScale(2, BigDecimal.ROUND_HALF_UP)";
        } else if (operator.equals("%")) {
            return operand1 + ".remainder(" + operand2 + ").setScale(2, BigDecimal.ROUND_HALF_UP)";
        } else {
            throw new Exception("Invalid arithmetic operator for Big decimal expression:" + operator);
        }
    }

    public void prepareCondition(Node operand1,
            Node operator, Node operand2,
            AtbMetaData opeAtb1,
            AtbMetaData opeAtb2,
            List<UserDefinedFunction> listFunctions,
            List<RegularExpression> listRegExs,
            RuleBuilder.VariableCounter funCounter, String expName) throws Exception {

        operator.data = operator.data.toLowerCase();
        operand1.data = operand1.data.toLowerCase();
        operand2.data = operand2.data.toLowerCase();
        //System.out.println("Detail:" + operand1.data + ":" + operand2.data + ":" + operator.data);
        operator.data = RuleBuilder.truncateQuotes(operator.data);
        operand1.data = RuleBuilder.truncateQuotes(operand1.data);
        operand2.data = RuleBuilder.truncateQuotes(operand2.data);

        //System.out.println("Detail:" + operand1.data + ":" + operand2.data + ":" + operator.data);
        if (operand1.isVisited() && operand2.isVisited() && (operator.data.equals("or") || operator.data.equals("and") || operator.data.equals("&&") || operator.data.equals("||"))) {
            if (operator.data.equals("or")) {
                operator.data = "||";
            }
            if (operator.data.equals("and")) {
                operator.data = "&&";
            }
            StringBuilder sbExp = new StringBuilder();
            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, null));
            sbExp.append(operator.data);
            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, null));
            operator.expData = sbExp.toString();
            return;
        }
        Object dataType = RuleBuilder.getDataType(operand1, operand2, operator, opeAtb1, opeAtb2);

        operator.setDataType(dataType);
        if (opeAtb1 != null) {
            opeAtb1.setDataType(dataType);
        }
        if (opeAtb2 != null) {
            opeAtb2.setDataType(dataType);
        }

        if (dataType instanceof Double) {
            if (opeAtb1 != null && opeAtb1.getFunction() != null) {
                throw new Exception("Function not supported for numeric data type");
            }
            if (opeAtb2 != null && opeAtb2.getFunction() != null) {
                throw new Exception("Function not supported for numeric data type");
            }
            if (operator.data.equals("=") || operator.data.equals("==")) {
                if (operator.data.equals("=")) {
                    operator.data = "==";
                }
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb1.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }
                    List<String> beans = opeAtb1.getSelectedHeuristicAtbs().get(0).getBeans();
                    if (beans != null && beans.contains(operand2.data)) {
                        opeAtb1.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(operand2.data);
                        StringBuilder sb = new StringBuilder();
                        if (opeAtb1.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, operand2.data)[0]);
                            sb.append(")");
                            sb.append(">=0");
                            sb.append("&&");
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, operand2.data)[1]);
                            sb.append(")");
                            sb.append("<=0");
                        } else {
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb1, operand2.data));
                            sb.append(")");
                            sb.append("==0");
                        }
                        operator.setExpData(sb.toString());
                    } else {
                        throw new Exception("Bean does not exists with a name:" + operand2.data + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                    }
                } else if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb2.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }
                    List<String> beans = opeAtb2.getSelectedHeuristicAtbs().get(0).getBeans();
                    if (beans != null && beans.contains(operand2.data)) {
                        opeAtb2.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(operand1.data);
                        StringBuilder sb = new StringBuilder();
                        if (opeAtb2.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb2, operand1.data)[0]);
                            sb.append(")");
                            sb.append(">=0");
                            sb.append("&&");
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb2, operand1.data)[1]);
                            sb.append(")");
                            sb.append("<=0");
                        } else {
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb2, operand1.data));
                            sb.append(")");
                            sb.append("==0");
                        }
                        operator.setExpData(sb.toString());
                    } else {
                        throw new Exception("Bean does not exists with a name:" + operand1.data + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                    }
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb2 != null && opeAtb1.isMultivalAction() && opeAtb2.isMultivalAction()) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".containsAll(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                    } else if (opeAtb2 != null && opeAtb2.isMultivalAction()) {
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb2 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareTo(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append("==0");
                    } else if (opeAtb1 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append("==0");
                    } else if (opeAtb2 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(")");
                        sbExp.append("==0");
                    } else {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append("==0");
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("in")) {
                operator.data = "==";
                if (!(operand2.data.startsWith("(") && operand2.data.endsWith(")"))) {
                    throw new Exception("Syntax Error: `in` operator must have opening and closing brackets");
                }
                operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb1.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }

                    List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                    StringBuilder sbExpression = new StringBuilder();
                    int nCount = 1;
                    int nSize = listVals.size();
                    for (String val : listVals) {
                        List<String> beans = opeAtb1.getSelectedHeuristicAtbs().get(0).getBeans();
                        if (beans != null && beans.contains(val)) {
                            opeAtb1.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(val);
                            if (opeAtb1.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                                sbExpression.append("(");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareTo(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, val)[0]);
                                sbExpression.append(")");
                                sbExpression.append(">=0");
                                sbExpression.append("&&");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareTo(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, val)[1]);
                                sbExpression.append(")");
                                sbExpression.append("<=0");
                                sbExpression.append(")");
                                if (nCount != nSize) {
                                    sbExpression.append("||");
                                }
                            } else {
                                sbExpression.append("(");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareTo(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb1, val));
                                sbExpression.append(")");
                                sbExpression.append("==0");
                                sbExpression.append(")");
                                if (nCount != nSize) {
                                    sbExpression.append("||");
                                }
                            }
                        } else {
                            throw new Exception("Bean does not exists with a name:" + val + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                        }
                        nCount++;
                    }
                    operator.setExpData(sbExpression.toString());
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".contains(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("||");
                            }
                            count++;
                        }
                    } else if (opeAtb1 != null) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".compareTo(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            sbExp.append("==0");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("||");
                            }
                            count++;
                        }
                    } else {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".compareTo(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            sbExp.append("==0");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("||");
                            }
                            count++;
                        }
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("!=")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb1.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }
                    List<String> beans = opeAtb1.getSelectedHeuristicAtbs().get(0).getBeans();
                    if (beans != null && beans.contains(operand2.data)) {
                        opeAtb1.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(operand2.data);
                        StringBuilder sb = new StringBuilder();
                        if (opeAtb1.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                            sb.append("!(");
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, operand2.data)[0]);
                            sb.append(")");
                            sb.append(">=0");
                            sb.append("&&");
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, operand2.data)[1]);
                            sb.append(")");
                            sb.append("<=0");
                            sb.append(")");
                        } else {
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb1, operand2.data));
                            sb.append(")");
                            sb.append("!=0");
                        }
                        operator.setExpData(sb.toString());
                    } else {
                        throw new Exception("Bean does not exists with a name:" + operand2.data + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                    }
                } else if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb2.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }
                    List<String> beans = opeAtb2.getSelectedHeuristicAtbs().get(0).getBeans();
                    if (beans != null && beans.contains(operand2.data)) {
                        opeAtb2.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(operand1.data);
                        StringBuilder sb = new StringBuilder();
                        if (opeAtb2.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                            sb.append("!(");
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb2, operand1.data)[0]);
                            sb.append(")");
                            sb.append(">=0");
                            sb.append("&&");
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb2, operand1.data)[1]);
                            sb.append(")");
                            sb.append("<=0");
                            sb.append(")");

                        } else {
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareTo(");
                            sb.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb2, operand1.data));
                            sb.append(")");
                            sb.append("!=0");
                        }
                        operator.setExpData(sb.toString());
                    } else {
                        throw new Exception("Bean does not exists with a name:" + operand1.data + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                    }
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb2 != null && opeAtb1.isMultivalAction() && opeAtb2.isMultivalAction()) {
                        sbExp.append("!" + getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".containsAll(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        sbExp.append("!" + getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                    } else if (opeAtb2 != null && opeAtb2.isMultivalAction()) {
                        sbExp.append("!" + getQualifiedAttributeName(opeAtb2));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb2 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareTo(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append("!=0");

                    } else if (opeAtb1 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append("!=0");

                    } else if (opeAtb2 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(")");
                        sbExp.append("!=0");

                    } else {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append("!=0");
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("notin")) {
                if (operator.data.equals("notin")) {
                    operator.data = "!=";
                }
                if (!(operand2.data.startsWith("(") && operand2.data.endsWith(")"))) {
                    throw new Exception("Syntax Error: `notin` operator must have opening and closing brackets");
                }
                operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb1.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }

                    List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                    StringBuilder sbExpression = new StringBuilder();
                    int nCount = 1;
                    int nSize = listVals.size();
                    for (String val : listVals) {
                        List<String> beans = opeAtb1.getSelectedHeuristicAtbs().get(0).getBeans();
                        if (beans != null && beans.contains(val)) {
                            opeAtb1.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(val);
                            if (opeAtb1.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                                sbExpression.append("!(");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareTo(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, val)[0]);
                                sbExpression.append(")");
                                sbExpression.append(">=0");
                                sbExpression.append("&&");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareTo(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, val)[1]);
                                sbExpression.append(")");
                                sbExpression.append("<=0");
                                sbExpression.append(")");
                                if (nCount != nSize) {
                                    sbExpression.append("&&");
                                }
                            } else {
                                sbExpression.append("!(");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareTo(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb1, val));
                                sbExpression.append(")");
                                sbExpression.append("==0");

                                sbExpression.append(")");
                                if (nCount != nSize) {
                                    sbExpression.append("&&");
                                }
                            }
                        } else {
                            throw new Exception("Bean does not exists with a name:" + val + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                        }
                        nCount++;
                    }
                    operator.setExpData(sbExpression.toString());
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("!" + getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".contains(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("&&");
                            }
                            count++;
                        }
                    } else if (opeAtb1 != null) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".compareTo(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            sbExp.append("!=0");

                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("&&");
                            }
                            count++;
                        }
                    } else {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".compareTo(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            sbExp.append("!=0");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("&&");
                            }
                            count++;
                        }
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("<") || operator.data.equals("<=")
                    || operator.data.equals(">") || operator.data.equals(">=")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if ((opeAtb1 != null && opeAtb1.isMultivalAction()) || (opeAtb2 != null && opeAtb2.isMultivalAction())) {
                    throw new Exception("<,<=,>,>= relational operators can not be applied on multi value fields");
                }
                StringBuilder sb = new StringBuilder();
                StringBuilder sbExp = new StringBuilder();
                if (opeAtb1 != null && opeAtb2 != null) {
                    sbExp.append(getQualifiedAttributeName(opeAtb1));
                    sbExp.append(".compareTo(");
                    sbExp.append(getQualifiedAttributeName(opeAtb2));
                    sbExp.append(")");
                    sbExp.append(getBigDecimalExpForROperator(operator.data));

                } else if (opeAtb1 != null) {
                    sbExp.append(getQualifiedAttributeName(opeAtb1));
                    sbExp.append(".compareTo(");
                    sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                    sbExp.append(")");
                    sbExp.append(getBigDecimalExpForROperator(operator.data));

                } else if (opeAtb2 != null) {
                    sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                    sbExp.append(".compareTo(");
                    sbExp.append(getQualifiedAttributeName(opeAtb2));
                    sbExp.append(")");
                    sbExp.append(getBigDecimalExpForROperator(operator.data));

                } else {
                    sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                    //sbExp.append(operator.data);
                    sbExp.append(".compareTo(");
                    sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                    sbExp.append(")");
                    sbExp.append(getBigDecimalExpForROperator(operator.data));
                }
                operator.setExpData(sbExp.toString());
            } else if (operator.data.equals("and") || operator.data.equals("or")
                    || operator.data.equals("&&") || operator.data.equals("||")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (operator.data.equals("and")) {
                    operator.data = "&&";
                } else if (operator.data.equals("or")) {
                    operator.data = "||";
                }
                if (!((operand1.data.equals("<") || operand1.data.equals("<=")
                        || operand1.data.equals(">") || operand1.data.equals(">=")
                        || operand1.data.equals("==") || operand1.data.equals("!=") || operand1.data.equals(".contains")
                        || operand1.data.equals("&&") || operand1.data.equals("||") || operand1.data.equals("and") || operand1.data.equals("or") || operand1.data.equals("like") || operand1.data.equals("notlike") || operand1.data.equals("in") || operand1.data.equals("notin"))
                        && (operand2.data.equals("<") || operand2.data.equals("<=")
                        || operand2.data.equals(">") || operand2.data.equals(">=")
                        || operand2.data.equals("==") || operand2.data.equals("!=") || operand2.data.equals(".contains")
                        || operand2.data.equals("&&") || operand2.data.equals("||") || operand2.data.equals("and") || operand2.data.equals("or")
                        || operand2.data.equals("like") || operand2.data.equals("notlike") || operand2.data.equals("in") || operand2.data.equals("notin")))) {
                    throw new Exception("Invalid use of logical operator");
                }
                operator.setExpData(operand1.getExpData() + " " + operator.data + " " + operand2.getExpData());
            } else if (operator.data.equals("+") || operator.data.equals("-") || operator.data.equals("*") || operator.data.equals("/") || operator.data.equals("%")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if ((opeAtb1 != null && opeAtb1.isMultivalAction()) || (opeAtb2 != null && opeAtb2.isMultivalAction())) {
                    throw new Exception("Airithmatic opearator not allowed in multi value attributes");
                }
                StringBuilder sb = new StringBuilder();
                StringBuilder sbExp = new StringBuilder();
                if (opeAtb1 != null && opeAtb2 != null) {
                    sbExp.append(getBigDecimalExpForAirOperator(getQualifiedAttributeName(opeAtb1), getQualifiedAttributeName(opeAtb2), operator.data));
                } else if (opeAtb1 != null) {
                    sbExp.append(getBigDecimalExpForAirOperator(getQualifiedAttributeName(opeAtb1), RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType), operator.data));
                } else if (opeAtb2 != null) {
                    sbExp.append(getBigDecimalExpForAirOperator(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType), getQualifiedAttributeName(opeAtb2), operator.data));
                } else {
                    sbExp.append(getBigDecimalExpForAirOperator(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType), RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType), operator.data));
                }
                operator.setExpData(sbExp.toString());
            } else {
                throw new Exception("Unsupported operator for numeric data");
            }
        } else if (dataType instanceof String) {
            if (opeAtb1 != null && opeAtb1.getFunction() != null) {
                throw new Exception("Function not supported for string data type");
            }
            if (opeAtb2 != null && opeAtb2.getFunction() != null) {
                throw new Exception("Function not supported for string data type");
            }
            if (operator.data.equals("=") || operator.data.equals("==")) {
                if (operator.data.equals("=")) {
                    operator.data = "==";
                }
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb1.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }
                    List<String> beans = opeAtb1.getSelectedHeuristicAtbs().get(0).getBeans();
                    if (beans != null && beans.contains(operand2.data)) {
                        opeAtb1.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(operand2.data);
                        StringBuilder sb = new StringBuilder();
                        if (opeAtb1.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareToIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, operand2.data)[0]);
                            sb.append(")>=0");
                            sb.append("&&");
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareToIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, operand2.data)[1]);
                            sb.append(")<=0");
                        } else {
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".equalsIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb1, operand2.data));
                            sb.append(")");
                        }
                        operator.setExpData(sb.toString());
                    } else {
                        throw new Exception("Bean does not exists with a name:" + operand2.data + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                    }
                } else if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb2.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }
                    List<String> beans = opeAtb2.getSelectedHeuristicAtbs().get(0).getBeans();
                    if (beans != null && beans.contains(operand2.data)) {
                        opeAtb2.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(operand1.data);
                        StringBuilder sb = new StringBuilder();
                        if (opeAtb2.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareToIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb2, operand1.data)[0]);
                            sb.append(")>=0");
                            sb.append("&&");
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareToIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb2, operand1.data)[1]);
                            sb.append(")<=0");
                        } else {
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".equalsIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb2, operand1.data));
                            sb.append(")");
                        }
                        operator.setExpData(sb.toString());
                    } else {
                        throw new Exception("Bean does not exists with a name:" + operand1.data + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                    }
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb2 != null && opeAtb1.isMultivalAction() && opeAtb2.isMultivalAction()) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".containsAll(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                    } else if (opeAtb2 != null && opeAtb2.isMultivalAction()) {
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb2 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".equalsIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(")");

                    } else if (opeAtb1 != null) {
                        sbExp.append(RuleBuilder.prepareRegEx(operand2, operand2.data, listRegExs, funCounter, expName));
                        sbExp.append(".matcher(");
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(")");
                        sbExp.append(".");
                        sbExp.append("matches()");

                    } else if (opeAtb2 != null) {
                        sbExp.append(RuleBuilder.prepareRegEx(operand1, operand1.data, listRegExs, funCounter, expName));
                        sbExp.append(".matcher(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append(".");
                        sbExp.append("matches()");
                    } else {
                        sbExp.append(RuleBuilder.prepareRegEx(operand1, operand1.data, listRegExs, funCounter, expName));
                        sbExp.append(".matcher(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append(".");
                        sbExp.append("matches()");
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("in")) {

                if (!(operand2.data.startsWith("(") && operand2.data.endsWith(")"))) {
                    throw new Exception("Syntax Error: `in` operator must have opening and closing brackets");
                }
                operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb1.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }

                    List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                    StringBuilder sbExpression = new StringBuilder();
                    int nCount = 1;
                    int nSize = listVals.size();
                    for (String val : listVals) {
                        List<String> beans = opeAtb1.getSelectedHeuristicAtbs().get(0).getBeans();
                        if (beans != null && beans.contains(val)) {
                            opeAtb1.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(val);
                            if (opeAtb1.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                                sbExpression.append("(");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareToIgnoreCase(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, val)[0]);
                                sbExpression.append(")>=0");
                                sbExpression.append("&&");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareToIgnoreCase(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, val)[1]);
                                sbExpression.append(")<=0");
                                sbExpression.append(")");
                                if (nCount != nSize) {
                                    sbExpression.append("||");
                                }
                            } else {
                                sbExpression.append("(");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".equalsIgnoreCase(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb1, val));
                                sbExpression.append(")");
                                sbExpression.append(")");
                                if (nCount != nSize) {
                                    sbExpression.append("||");
                                }
                            }
                        } else {
                            throw new Exception("Bean does not exists with a name:" + val + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                        }
                        nCount++;
                    }
                    operator.setExpData(sbExpression.toString());
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".contains(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("||");
                            }
                            count++;
                        }
                    } else if (opeAtb1 != null) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".equalsIgnoreCase(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("||");
                            }
                            count++;
                        }
                    } else {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".equalsIgnoreCase(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("||");
                            }
                            count++;
                        }
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("like")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid operator `like` for heuristic data comparison");
                } else if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid operator `like` for heuristic data comparison");
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb2 != null && opeAtb1.isMultivalAction() && opeAtb2.isMultivalAction()) {
                        throw new Exception("Invalid operator `like` for Multi val attributes");
                    } else if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        throw new Exception("Invalid operator `like` for Multi val attributes");
                    } else if (opeAtb2 != null && opeAtb2.isMultivalAction()) {
                        throw new Exception("Invalid operator `like` for Multi val attributes");
                    } else if (opeAtb1 != null) {

                        if (RuleUtil.isStartsAndEndsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".indexOf(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")>=0");
                        } else if (RuleUtil.isStartsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(1, operand2.data.length());
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".endsWith(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                        } else if (RuleUtil.isEndsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(0, (operand2.data.length() - 1));
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".startsWith(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                        } else {
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".equalsIgnoreCase(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                        }

                    } else {
                        if (RuleUtil.isStartsAndEndsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".indexOf(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")>=0");
                        } else if (RuleUtil.isStartsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(1, operand2.data.length());
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".endsWith(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                        } else if (RuleUtil.isEndsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(0, operand2.data.length() - 1);
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".startsWith(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                        } else {
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".equalsIgnoreCase(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                        }
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("!=")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb1.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }
                    List<String> beans = opeAtb1.getSelectedHeuristicAtbs().get(0).getBeans();
                    if (beans != null && beans.contains(operand2.data)) {
                        opeAtb1.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(operand2.data);
                        StringBuilder sb = new StringBuilder();
                        if (opeAtb1.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                            sb.append("!(");
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareToIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, operand2.data)[0]);
                            sb.append(")>=0");
                            sb.append("&&");
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".compareToIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, operand2.data)[1]);
                            sb.append(")<=0");
                            sb.append(")");
                        } else {
                            sb.append("!(");
                            sb.append(getQualifiedAttributeName(opeAtb1));
                            sb.append(".equalsIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb1, operand2.data));
                            sb.append(")");
                            sb.append(")");
                        }
                        operator.setExpData(sb.toString());
                    } else {
                        throw new Exception("Bean does not exists with a name:" + operand2.data + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                    }
                } else if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb2.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }
                    List<String> beans = opeAtb2.getSelectedHeuristicAtbs().get(0).getBeans();
                    if (beans != null && beans.contains(operand2.data)) {
                        opeAtb2.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(operand1.data);
                        StringBuilder sb = new StringBuilder();
                        if (opeAtb2.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                            sb.append("!(");
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareToIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb2, operand1.data)[0]);
                            sb.append(")<0");
                            sb.append("&&");
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".compareToIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameRange(opeAtb2, operand1.data)[1]);
                            sb.append(")>0");
                            sb.append(")");

                        } else {
                            sb.append("!(");
                            sb.append(getQualifiedAttributeName(opeAtb2));
                            sb.append(".equalsIgnoreCase(");
                            sb.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb2, operand1.data));
                            sb.append(")");
                            sb.append(")");
                        }
                        operator.setExpData(sb.toString());
                    } else {
                        throw new Exception("Bean does not exists with a name:" + operand1.data + ":AvailBeans:" + (beans != null ? beans.toString() : ""));
                    }
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb2 != null && opeAtb1.isMultivalAction() && opeAtb2.isMultivalAction()) {
                        sbExp.append("!" + getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".containsAll(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        sbExp.append("!" + getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                    } else if (opeAtb2 != null && opeAtb2.isMultivalAction()) {
                        sbExp.append("!" + getQualifiedAttributeName(opeAtb2));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb2 != null) {
                        sbExp.append("!(");
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".equalsIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(")");
                        sbExp.append(")");
                    } else if (opeAtb1 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append("!=");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                    } else if (opeAtb2 != null) {
                        sbExp.append("!(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(".equalsIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(")");
                        sbExp.append(")");
                    } else {
                        sbExp.append("!(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".equalsIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append(")");
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("notin")) {
                if (operator.data.equals("notin")) {
                    operator.data = "!=";
                }
                if (!(operand2.data.startsWith("(") && operand2.data.endsWith(")"))) {
                    throw new Exception("Syntax Error: `notin` operator must have opening and closing brackets");
                }
                operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    if (opeAtb1.isMultivalAction()) {
                        throw new Exception("Operation on heuristic data not allowed for a multivalue attribute");
                    }

                    List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                    StringBuilder sbExpression = new StringBuilder();
                    int nCount = 1;
                    int nSize = listVals.size();
                    for (String val : listVals) {
                        List<String> beans = opeAtb1.getSelectedHeuristicAtbs().get(0).getBeans();
                        if (beans != null && beans.contains(val)) {
                            opeAtb1.getSelectedHeuristicAtbs().get(0).addToSelectedBeans(val);
                            if (opeAtb1.getSelectedHeuristicAtbs().get(0).isRangeBased()) {
                                sbExpression.append("!(");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareToIgnoreCase(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, val)[0]);
                                sbExpression.append(")>=0");
                                sbExpression.append("&&");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".compareToIgnoreCase(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameRange(opeAtb1, val)[1]);
                                sbExpression.append(")<=0");
                                sbExpression.append(")");
                                if (nCount != nSize) {
                                    sbExpression.append("&&");
                                }
                            } else {
                                sbExpression.append("!(");
                                sbExpression.append(getQualifiedAttributeName(opeAtb1));
                                sbExpression.append(".equalsIgnoreCase(");
                                sbExpression.append(getQualifiedHeuristicAttributeNameKeyVal(opeAtb1, val));
                                sbExpression.append(")");
                                sbExpression.append(")");
                                if (nCount != nSize) {
                                    sbExpression.append("&&");
                                }
                            }
                        } else {
                            throw new Exception("Bean does not exists with a name:" + val + ":AvailBeans:" + (beans != null ? beans.toString() : ""));

                        }
                        nCount++;
                    }
                    operator.setExpData(sbExpression.toString());
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("!" + getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".contains(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("&&");
                            }
                            count++;
                        }
                    } else if (opeAtb1 != null) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("!(");
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".equalsIgnoreCase(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("&&");
                            }
                            count++;
                        }
                    } else {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("!(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".equalsIgnoreCase(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, val, dataType));
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("&&");
                            }
                            count++;
                        }
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("notlike")) {
//                    if (!(operand2.data.startsWith("'") && operand2.data.endsWith("'"))) {
//                        throw new Exception("Syntax Error: `notlike` operator must have opening and closing brackets");
//                    }
//                    operand2.data = operand2.data.substring(1, operand2.data.length() - 1);

                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid operator `notlike` for heuristic data comparison");
                } else if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid operator `notlike` for heuristic data comparison");
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb2 != null && opeAtb1.isMultivalAction() && opeAtb2.isMultivalAction()) {
                        throw new Exception("Invalid operator `notlike` for Multi val attributes");
                    } else if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        throw new Exception("Invalid operator `notlike` for Multi val attributes");
                    } else if (opeAtb2 != null && opeAtb2.isMultivalAction()) {
                        throw new Exception("Invalid operator `notlike` for Multi val attributes");
                    } else if (opeAtb1 != null) {

                        if (RuleUtil.isStartsAndEndsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                            sbExp.append("!(");
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".indexOf(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")>=0");
                            sbExp.append(")");

                        } else if (RuleUtil.isStartsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(1, operand2.data.length());
                            sbExp.append("!(");
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".endsWith(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                        } else if (RuleUtil.isEndsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(0, (operand2.data.length() - 1));
                            sbExp.append("!(");
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".startsWith(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                        } else {
                            sbExp.append("!(");
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".equalsIgnoreCase(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                        }

                    } else {
                        if (RuleUtil.isStartsAndEndsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                            sbExp.append("!(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".indexOf(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")>=0");
                            sbExp.append(")");
                        } else if (RuleUtil.isStartsWith(operand2.data, "%")) {
                            operand2.data = operand2.data.substring(1, operand2.data.length());
                            sbExp.append("!(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".endsWith(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                        } else if (RuleUtil.isEndsWith(operand2.data, "%")) {
                            sbExp.append("!(");
                            operand2.data = operand2.data.substring(0, operand2.data.length() - 1);
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".startsWith(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                        } else {
                            sbExp.append("!(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                            sbExp.append(".equalsIgnoreCase(");
                            sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                            sbExp.append(")");
                            sbExp.append(")");
                        }
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("<") || operator.data.equals("<=")
                    || operator.data.equals(">") || operator.data.equals(">=")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if ((opeAtb1 != null && opeAtb1.isMultivalAction()) || (opeAtb2 != null && opeAtb2.isMultivalAction())) {
                    throw new Exception("<,<=,>,>= relational operators can not be applied on multi value fields");
                }
                StringBuilder sb = new StringBuilder();
                StringBuilder sbExp = new StringBuilder();
                if (operator.data.equals("<")) {
                    if (opeAtb1 != null && opeAtb2 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append("<0");
                    } else if (opeAtb1 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append("<0");
                    } else if (opeAtb2 != null) {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append("<0");
                    } else {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append("<0");
                    }
                } else if (operator.data.equals("<=")) {
                    if (opeAtb1 != null && opeAtb2 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append("<=0");
                    } else if (opeAtb1 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append("<=0");
                    } else if (opeAtb2 != null) {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append("<=0");
                    } else {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append("<=0");
                    }
                } else if (operator.data.equals(">")) {
                    if (opeAtb1 != null && opeAtb2 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append(">0");
                    } else if (opeAtb1 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append(">0");
                    } else if (opeAtb2 != null) {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append(">0");
                    } else {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append(">0");
                    }
                } else {
                    if (opeAtb1 != null && opeAtb2 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append(">=0");
                    } else if (opeAtb1 != null) {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append(">=0");
                    } else if (opeAtb2 != null) {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                        sbExp.append(">=0");
                    } else {
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                        sbExp.append(".compareToIgnoreCase(");
                        sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                        sbExp.append(")");
                        sbExp.append(">=0");
                    }
                }
                operator.setExpData(sbExp.toString());
            } else if (operator.data.equals("and") || operator.data.equals("or")
                    || operator.data.equals("&&") || operator.data.equals("||")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (operator.data.equals("and")) {
                    operator.data = "&&";
                } else if (operator.data.equals("or")) {
                    operator.data = "||";
                }
                if (!((operand1.data.equals("<") || operand1.data.equals("<=")
                        || operand1.data.equals(">") || operand1.data.equals(">=")
                        || operand1.data.equals("==") || operand1.data.equals("!=") || operand1.data.equals(".contains")
                        || operand1.data.equals("&&") || operand1.data.equals("||") || operand1.data.equals("and")
                        || operand1.data.equals("or") || operand1.data.equals("like") || operand1.data.equals("notlike") || operand1.data.equals("in") || operand1.data.equals("notin"))
                        && (operand2.data.equals("<") || operand2.data.equals("<=")
                        || operand2.data.equals(">") || operand2.data.equals(">=")
                        || operand2.data.equals("==") || operand2.data.equals("!=") || operand2.data.equals(".contains")
                        || operand2.data.equals("&&") || operand2.data.equals("||") || operand2.data.equals("and") || operand2.data.equals("or")
                        || operand2.data.equals("like") || operand2.data.equals("notlike") || operand2.data.equals("in") || operand2.data.equals("notin")))) {
                    throw new Exception("Invalid use of logical operator");
                }
                operator.setExpData(operand1.getExpData() + " " + operator.data + " " + operand2.getExpData());
            } else if (operator.data.equals("+") || operator.data.equals("-") || operator.data.equals("*") || operator.data.equals("/") || operator.data.equals("%")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if ((opeAtb1 != null && opeAtb1.isMultivalAction()) || (opeAtb2 != null && opeAtb2.isMultivalAction())) {
                    throw new Exception("Airithmatic opearator not allowed in multi value attributes");
                }
                if (operator.data.equals("-") || operator.data.equals("*") || operator.data.equals("/") || operator.data.equals("%")) {
                    throw new Exception("-,*,/,% Airthmetic operators are not allowed on string data type");
                }
                StringBuilder sb = new StringBuilder();
                StringBuilder sbExp = new StringBuilder();
                if (opeAtb1 != null && opeAtb2 != null) {

                    sbExp.append(getQualifiedAttributeName(opeAtb1));
                    sbExp.append(operator.data);
                    sbExp.append(getQualifiedAttributeName(opeAtb2));
                } else if (opeAtb1 != null) {
                    sbExp.append(getQualifiedAttributeName(opeAtb1));
                    sbExp.append(operator.data);
                    sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                } else if (opeAtb2 != null) {
                    sbExp.append(getQualifiedAttributeName(opeAtb2));
                    sbExp.append(operator.data);
                    sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                } else {
                    sbExp.append(RuleBuilder.prepareDataTypeVal(operand1, operand1.data, dataType));
                    sbExp.append(operator.data);
                    sbExp.append(RuleBuilder.prepareDataTypeVal(operand2, operand2.data, dataType));
                }
                operator.setExpData(sbExp.toString());
            } else {
                throw new Exception("Unsupported operator for string data");
            }
        } else if (dataType instanceof Date) {
            if (operator.data.equals("=") || operator.data.equals("==")) {
                if (operator.data.equals("=")) {
                    operator.data = "==";
                }
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Operation on heuristic attirbute dont support date data type");
                } else if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Operation on heuristic attirbute dont support date data type");
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb2 != null && opeAtb1.isMultivalAction() && opeAtb2.isMultivalAction()) {
                        if (opeAtb1.getFunction() != null || opeAtb2.getFunction() != null) {
                            throw new Exception("Function not allowed on multivalue attributes");
                        }
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".containsAll(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        if (opeAtb1.getFunction() != null) {
                            throw new Exception("Function not allowed on multivalue attributes");
                        }
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter));
                        sbExp.append(")");
                    } else if (opeAtb2 != null && opeAtb2.isMultivalAction()) {
                        if (opeAtb2.getFunction() != null) {
                            throw new Exception("Function not allowed on multivalue attributes");
                        }
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb2 != null) {
                        if (opeAtb1.getFunction() != null) {
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, opeAtb1, funCounter));
                        } else {
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                        }
                        sbExp.append(".compareTo(");
                        if (opeAtb2.getFunction() != null) {
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, opeAtb2, funCounter));
                        } else {
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                        }
                        sbExp.append(")");
                        sbExp.append("==0");
                    } else if (opeAtb1 != null) {
                        if (opeAtb1.getFunction() != null) {
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, opeAtb1, funCounter));
                        } else {
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                        }
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter));
                        sbExp.append(")");
                        sbExp.append("==0");
                    } else if (opeAtb2 != null) {
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                        sbExp.append(".compareTo(");
                        if (opeAtb2.getFunction() != null) {
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, opeAtb2, funCounter));
                        } else {
                            sbExp.append(getQualifiedAttributeName(opeAtb2));
                        }
                        sbExp.append(")");
                        sbExp.append("==0");
                    } else {
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter));
                        sbExp.append(")");
                        sbExp.append("==0");
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("in")) {
                operator.data = "==";
                if (!(operand2.data.startsWith("(") && operand2.data.endsWith(")"))) {
                    throw new Exception("Syntax Error: `in` operator must have opening and closing brackets");
                }
                operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Operation on heuristic attirbute dont support date data type");
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        if (opeAtb1.getFunction() != null) {
                            throw new Exception("Function not allowed on multivalue attributes");
                        }
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                            sbExp.append(".contains(");
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, val, listFunctions, null, funCounter));
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("||");
                            }
                            count++;
                        }
                    } else if (opeAtb1 != null) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            if (opeAtb1.getFunction() != null) {
                                RuleBuilder.prepareDateDataTypeVal(operand1, val, listFunctions, opeAtb1, funCounter);
                            } else {
                                sbExp.append(getQualifiedAttributeName(opeAtb1));
                            }
                            sbExp.append(".compareTo(");
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, val, listFunctions, null, funCounter));
                            sbExp.append(")==0");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("||");
                            }
                            count++;
                        }
                    } else {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                            sbExp.append(".compareTo(");
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, val, listFunctions, null, funCounter));
                            sbExp.append(")==0");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("||");
                            }
                            count++;
                        }
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("!=")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Operation on heuristic attirbute dont support date data type");
                } else if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Operation on heuristic attirbute dont support date data type");
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb2 != null && opeAtb1.isMultivalAction() && opeAtb2.isMultivalAction()) {
                        if (opeAtb1.getFunction() != null || opeAtb2.getFunction() != null) {
                            throw new Exception("Function not allowed on multivalue attributes");
                        }
                        sbExp.append("!" + getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".containsAll(");
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        if (opeAtb1.getFunction() != null) {
                            throw new Exception("Function not allowed on multivalue attributes");
                        }
                        sbExp.append("!" + getQualifiedAttributeName(opeAtb1));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter));
                        sbExp.append(")");
                    } else if (opeAtb2 != null && opeAtb2.isMultivalAction()) {
                        if (opeAtb2.getFunction() != null || opeAtb2.getFunction() != null) {
                            throw new Exception("Function not allowed on multivalue attributes");
                        }
                        sbExp.append("!" + getQualifiedAttributeName(opeAtb2));
                        sbExp.append(".contains(");
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                        sbExp.append(")");
                    } else if (opeAtb1 != null && opeAtb2 != null) {
                        if (opeAtb1.getFunction() != null) {
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, opeAtb1, funCounter));
                        } else {
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                        }
                        sbExp.append(".compareTo(");
                        if (opeAtb2.getFunction() != null) {
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, opeAtb2, funCounter));
                        } else {
                            sbExp.append(getQualifiedAttributeName(opeAtb2));
                        }
                        sbExp.append(")!=0");
                    } else if (opeAtb1 != null) {
                        if (opeAtb1.getFunction() != null) {
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, opeAtb1, funCounter));
                        } else {
                            sbExp.append(getQualifiedAttributeName(opeAtb1));
                        }
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter));
                        sbExp.append(")!=0");
                    } else if (opeAtb2 != null) {
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                        sbExp.append(".compareTo(");
                        if (opeAtb2.getFunction() != null) {
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, opeAtb2, funCounter));
                        } else {
                            sbExp.append(getQualifiedAttributeName(opeAtb2));
                        }
                        sbExp.append(")!=0");
                    } else {
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                        sbExp.append(".compareTo(");
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter));
                        sbExp.append(")!=0");
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("notin")) {
                if (operator.data.equals("notin")) {
                    operator.data = "!=";
                }
                if (!(operand2.data.startsWith("(") && operand2.data.endsWith(")"))) {
                    throw new Exception("Syntax Error: `notin` operator must have opening and closing brackets");
                }
                operand2.data = operand2.data.substring(1, operand2.data.length() - 1);
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Operation on heuristic attirbute dont support date data type");
                } else {
                    StringBuilder sbExp = new StringBuilder();
                    if (opeAtb1 != null && opeAtb1.isMultivalAction()) {
                        if (opeAtb1.getFunction() != null) {
                            throw new Exception("Function not allowed on multivalue attributes");
                        }
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            if (opeAtb1.getFunction() != null) {
                                sbExp.append("!" + RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, opeAtb1, funCounter));
                            } else {
                                sbExp.append("!" + getQualifiedAttributeName(opeAtb1));
                            }
                            sbExp.append(".contains(");
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, val, listFunctions, null, funCounter));
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("&&");
                            }
                            count++;
                        }
                    } else if (opeAtb1 != null) {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            if (opeAtb1 != null && opeAtb1.getFunction() != null) {
                                sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, opeAtb1, funCounter));
                            } else {
                                sbExp.append(getQualifiedAttributeName(opeAtb1));
                            }
                            sbExp.append(".compareTo(");
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, val, listFunctions, null, funCounter));
                            sbExp.append(")!=0");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("&&");
                            }
                            count++;
                        }
                    } else {
                        List<String> listVals = RuleBuilder.splitForIn(operand2.data);
                        int count = 1;
                        int size = listVals.size();
                        for (String val : listVals) {
                            sbExp.append("(");
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                            sbExp.append(".compareTo(");
                            sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, val, listFunctions, null, funCounter));
                            sbExp.append(")!=0");
                            sbExp.append(")");
                            if (count != size) {
                                sbExp.append("&&");
                            }
                            count++;
                        }
                    }
                    operator.setExpData(sbExp.toString());
                }
            } else if (operator.data.equals("<") || operator.data.equals("<=")
                    || operator.data.equals(">") || operator.data.equals(">=")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if ((opeAtb1 != null && opeAtb1.isMultivalAction()) || (opeAtb2 != null && opeAtb2.isMultivalAction())) {
                    throw new Exception("<,<=,>,>= relational operators can not be applied on multi value fields");
                }
                StringBuilder sb = new StringBuilder();
                StringBuilder sbExp = new StringBuilder();
                if (opeAtb1 != null && opeAtb2 != null) {
                    if (opeAtb1.getFunction() != null) {
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, opeAtb1, funCounter));
                    } else {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                    }
                    sbExp.append(".compareTo(");
                    if (opeAtb2.getFunction() != null) {
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, opeAtb2, funCounter));
                    } else {
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                    }
                    sbExp.append(")");
                    sbExp.append(getBigDecimalExpForROperator(operator.data));
                } else if (opeAtb1 != null) {
                    if (opeAtb1.getFunction() != null) {
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, opeAtb1, funCounter));
                    } else {
                        sbExp.append(getQualifiedAttributeName(opeAtb1));
                    }
                    sbExp.append(".compareTo(");
                    sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter));
                    sbExp.append(")");
                    sbExp.append(getBigDecimalExpForROperator(operator.data));
                } else if (opeAtb2 != null) {
                    sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                    sbExp.append(".compareTo(");
                    if (opeAtb2.getFunction() != null) {
                        sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, opeAtb2, funCounter));
                    } else {
                        sbExp.append(getQualifiedAttributeName(opeAtb2));
                    }
                    sbExp.append(")");
                    sbExp.append(getBigDecimalExpForROperator(operator.data));
                } else {
                    sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter));
                    sbExp.append(".compareTo(");
                    sbExp.append(RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter));
                    sbExp.append(")");
                    sbExp.append(getBigDecimalExpForROperator(operator.data));
                }
                operator.setExpData(sbExp.toString());
            } else if (operator.data.equals("and") || operator.data.equals("or")
                    || operator.data.equals("&&") || operator.data.equals("||")) {
                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (operator.data.equals("and")) {
                    operator.data = "&&";
                } else if (operator.data.equals("or")) {
                    operator.data = "||";
                }
                if (!((operand1.data.equals("<") || operand1.data.equals("<=")
                        || operand1.data.equals(">") || operand1.data.equals(">=")
                        || operand1.data.equals("==") || operand1.data.equals("!=") || operand1.data.equals(".contains")
                        || operand1.data.equals("&&") || operand1.data.equals("||") || operand1.data.equals("and") || operand1.data.equals("or")
                        || operand1.data.equals("like") || operand1.data.equals("notlike") || operand1.data.equals("in") || operand1.data.equals("notin"))
                        && (operand2.data.equals("<") || operand2.data.equals("<=")
                        || operand2.data.equals(">") || operand2.data.equals(">=")
                        || operand2.data.equals("==") || operand2.data.equals("!=") || operand2.data.equals(".contains")
                        || operand2.data.equals("&&") || operand2.data.equals("||") || operand2.data.equals("and") || operand2.data.equals("or")
                        || operand2.data.equals("like") || operand2.data.equals("notlike") || operand2.data.equals("in") || operand2.data.equals("notin")))) {
                    throw new Exception("Invalid use of logical operator");
                }
                operator.setExpData(operand1.getExpData() + " " + operator.data + " " + operand2.getExpData());
            } else if (operator.data.equals("+") || operator.data.equals("-") || operator.data.equals("*") || operator.data.equals("/") || operator.data.equals("%")) {
                StringBuilder sbExp = new StringBuilder();

                if (opeAtb1 != null && opeAtb1.getSelectedHeuristicAtbs() != null && opeAtb1.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if (opeAtb2 != null && opeAtb2.getSelectedHeuristicAtbs() != null && opeAtb2.getSelectedHeuristicAtbs().size() > 0) {
                    throw new Exception("Invalid relational operator for heuristic data");
                }
                if ((opeAtb1 != null && opeAtb1.isMultivalAction()) || (opeAtb2 != null && opeAtb2.isMultivalAction())) {
                    throw new Exception("Airithmatic opearator not allowed in multi value attributes");
                }
                if (opeAtb1 != null && opeAtb2 != null) {
                    sbExp.append(getBigDecimalExpForAirOperator(getQualifiedAttributeName(opeAtb1), getQualifiedAttributeName(opeAtb2), operator.data));
                } else if (opeAtb1 != null) {
                    if (opeAtb1.getFunction() != null) {
                        sbExp.append(getBigDecimalExpForAirOperator(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, opeAtb1, funCounter), RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter), operator.data));
                    } else {
                        sbExp.append(getBigDecimalExpForAirOperator(getQualifiedAttributeName(opeAtb1), RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter), operator.data));
                    }
                } else if (opeAtb2 != null) {
                    if (opeAtb2.getFunction() != null) {
                        sbExp.append(getBigDecimalExpForAirOperator(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter), RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, opeAtb2, funCounter), operator.data));
                    } else {
                        sbExp.append(getBigDecimalExpForAirOperator(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter), getQualifiedAttributeName(opeAtb2), operator.data));
                    }
                } else {
                    sbExp.append(getBigDecimalExpForAirOperator(RuleBuilder.prepareDateDataTypeVal(operand1, operand1.data, listFunctions, null, funCounter), RuleBuilder.prepareDateDataTypeVal(operand2, operand2.data, listFunctions, null, funCounter), operator.data));
                }
                operator.setExpData(sbExp.toString());
            } else {
                throw new Exception("Unsupported operator for date data type");
            }
        }
    }

    public void inorderRecursiveTraversal(Node localRoot, StringBuilder sbRes) {
        if (localRoot != null) {
            if (localRoot.leftChild == null && localRoot.rightChild == null) {
                if (localRoot.getExpData() != null) {
                    sbRes.append(localRoot.getExpData());
                }
            } else {
                sbRes.append("(");
                inorderRecursiveTraversal(localRoot.leftChild, sbRes);
                if (localRoot.getExpData() != null) {
                    sbRes.append(localRoot.getExpData());
                }
                //sbRes.append(localRoot.data);
                inorderRecursiveTraversal(localRoot.rightChild, sbRes);
                sbRes.append(")");
            }
        }
    }
//        private void postOrder(Node localRoot, StringBuilder sbRes) {
//            if (localRoot != null) {
//                postOrder(localRoot.leftChild, sbRes);
//                postOrder(localRoot.rightChild, sbRes);
//                sbRes.append(localRoot.data);
//            }
//        }

}
