/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.util.List;

/**
 *
 * @author jagadeesh.t
 */
public class Rule {

    private String ruleName;
    private String ruleExp;
    private String javaExp;
    private long ruleId;
    private List<AtbMetaData> atbs;
    private List<UserDefinedFunction> functions;
    private List<RegularExpression> regExs;

    public List<RegularExpression> getRegExs() {
        return regExs;
    }

    public void setRegExs(List<RegularExpression> regExs) {
        this.regExs = regExs;
    }

    public List<AtbMetaData> getAtbs() {
        return atbs;
    }

    public void setAtbs(List<AtbMetaData> atbs) {
        this.atbs = atbs;
    }

    public List<UserDefinedFunction> getFunctions() {
        return functions;
    }

    public void setFunctions(List<UserDefinedFunction> functions) {
        this.functions = functions;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public String getJavaExp() {
        return javaExp;
    }

    public void setJavaExp(String javaExp) {
        this.javaExp = javaExp;
    }

    public String getRuleExp() {
        return ruleExp;
    }

    public void setRuleExp(String ruleExp) {
        this.ruleExp = ruleExp;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

}
