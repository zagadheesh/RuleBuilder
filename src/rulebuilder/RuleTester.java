/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rulebuilder;

import core.DynamicClassLoader;
import core.RuleBuilder;
import beans.AtbMetaData;
import beans.HeuristicAtb;
import beans.RegularExpression;
import beans.Rule;
import beans.UserDefinedFunction;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static core.RuleBuilder.mergeAtbMetaData;

/**
 *
 * @author ashiskumar.m
 */
public class RuleTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            List<AtbMetaData> list = new ArrayList<AtbMetaData>();
            AtbMetaData atb1 = new AtbMetaData();
            atb1.setSourceName("s1");
            atb1.setAtbName("date1");
            atb1.setDataType(new Date());
            atb1.setMultivalAction(false);

            AtbMetaData atb2 = new AtbMetaData();
            atb2.setSourceName("s1");
            atb2.setAtbName("rev");
            atb2.setDataType(new Long(-1));

            AtbMetaData atb3 = new AtbMetaData();
            atb3.setSourceName("s2");
            atb3.setAtbName("atb1");
            atb3.setDataType(new Long(1));
            atb3.setMultivalAction(false);

            AtbMetaData atb4 = new AtbMetaData();
            atb4.setSourceName("s1");
            atb4.setAtbName("a1");
            atb4.setDataType(new Long(-1));

            AtbMetaData atb5 = new AtbMetaData();
            atb5.setSourceName("s1");
            atb5.setAtbName("a2");
            atb5.setDataType(new Long(-1));

            HeuristicAtb ht = new HeuristicAtb();
            ht.setName("h1");
            ht.setRangeBased(true);
            List<String> list1 = new ArrayList<String>();
            list1.add("high");
            list1.add("medium");
            list1.add("low");
            ht.setBeans(list1);
            atb4.addHeuristicAtbs(ht);

            list.add(atb1);
            list.add(atb2);
            list.add(atb3);
            list.add(atb4);
            list.add(atb5);

            //String exp = "`test.a1``test.a2``test.a5``test.a6``test3.a1BI``test3.a3VC`";
            int code = '`';
            System.out.println("code:" + code);
            //String exp = "(10+(20+5))>(3+(5+5))";
            //(`pb_unified_user_profile.first_name`==name1)&&(`pb_unified_user_profile.total_aircel_og_usage`==28)&&(`pb_unified_user_profile.aircel_to_other_local_sms`-3)
            //String exp = ">(`s1.a2`)*(20/100))";
            String exp = "10/25>45";

            // String exp1 = buildExpression("s1.a1", exp);
            // System.out.println("Exp1:" + exp1);
            //Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
            List<AtbMetaData> listUsedAtbs = new ArrayList<AtbMetaData>();
            List<UserDefinedFunction> listUsedFunctions = new ArrayList<UserDefinedFunction>();
            List<RegularExpression> listUsedRegExs = new ArrayList<RegularExpression>();
            String javaExp = RuleBuilder.generateExpression("test", list, exp, listUsedAtbs, listUsedFunctions, listUsedRegExs);
            listUsedAtbs = mergeAtbMetaData(listUsedAtbs);
            //javaExp = RuleBuilder.generateExpression(list, exp, listUsedAtbs, listUsedFunctions);
            System.out.println("Java Exp:" + javaExp);
            System.out.println("Used Atbs:" + listUsedAtbs);
            System.out.println("Used Functions:" + listUsedFunctions);

//            List<AtbMetaData> list = new ArrayList<AtbMetaData>();
//            AtbMetaData atb1 = new AtbMetaData();
//            atb1.setSourceName("s1");
//            atb1.setAtbName("atb1");
//            atb1.setDataType(new Double(-1));
//            atb1.setMultivalAction(true);
//
//            HeuristicAtb ht = new HeuristicAtb();
//            ht.setName("h1");
//            ht.setRangeBased(true);
//
//            ht.addToSelectedBeans("high");
//            ht.addToSelectedBeans("midium");
//            ht.addToSelectedBeans("low1");
//            atb1.addHeuristicAtbs(ht);
//
//            HeuristicAtb ht1 = new HeuristicAtb();
//            ht1.setName("h1");
//            ht1.setRangeBased(true);
//            ht1.addToSelectedBeans("high");
//            ht1.addToSelectedBeans("midium");
//            ht1.addToSelectedBeans("low");
//            atb1.addHeuristicAtbs(ht1);
//            atb1.addSelectedHeuristicAtbs(ht);
//
//            AtbMetaData atb2 = new AtbMetaData();
//            atb2.setSourceName("s1");
//            atb2.setAtbName("atb1");
//            atb2.setDataType(new Double(-1));
//            atb2.setMultivalAction(true);
//            atb2.addSelectedHeuristicAtbs(ht1);
//
//            list.add(atb1);
//            list.add(atb2);
//            List<AtbMetaData> l = mergeAtbMetaData(list);
//            System.out.println(l.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void main1(String[] args) {
        // TODO code application logic here
        try {
            String exp = "name like name";
            List<AtbMetaData> srcAtbs = new ArrayList<AtbMetaData>();

            AtbMetaData atbMeta = new AtbMetaData();
            atbMeta.setAtbName("name");
            atbMeta.setDataType(new String(""));
            atbMeta.setSourceName("s1");
            srcAtbs.add(atbMeta);

            List<AtbMetaData> usedAttrsList = new ArrayList<AtbMetaData>();
            List<UserDefinedFunction> usedFunctionsList = new ArrayList<UserDefinedFunction>();
            List<RegularExpression> usedRegExprsList = new ArrayList<RegularExpression>();

            String javaExp = RuleBuilder.generateExpression(1 + "_abc", srcAtbs, exp, usedAttrsList, usedFunctionsList, usedRegExprsList);
            usedAttrsList = RuleBuilder.mergeAtbMetaData(usedAttrsList);

            Rule rule = new Rule();
            rule.setAtbs(usedAttrsList);
            rule.setFunctions(usedFunctionsList);
            rule.setJavaExp(javaExp);
            rule.setRegExs(usedRegExprsList);
            rule.setRuleExp(javaExp);
            rule.setRuleId(1);
            List<Rule> rules = new ArrayList<Rule>();
            rules.add(rule);

            Rule rule1 = new Rule();
            rule1.setAtbs(usedAttrsList);
            rule1.setFunctions(usedFunctionsList);
            rule1.setJavaExp(javaExp);
            rule1.setRegExs(usedRegExprsList);
            rule1.setRuleExp(javaExp);
            rule1.setRuleId(2);

            rules.add(rule1);

            String javaCode = RuleBuilder.generateJavaCode(rules, "MyRuleClass");
            System.out.println("Code:" + javaCode);

            Class ruleHandler = DynamicClassLoader.loadClass("_" + "MyRuleClass", javaCode);
            Class[] arr = new Class[1];
            arr[0] = Map.class;
            Map<String, Object> valMap = new HashMap<String, Object>();
            valMap.put("s1_name", new String("name"));
            boolean bRes = (Boolean) DynamicClassLoader.executeMethod(ruleHandler, "_" + 1, arr, valMap);
            System.out.println("Res:" + bRes);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

}
