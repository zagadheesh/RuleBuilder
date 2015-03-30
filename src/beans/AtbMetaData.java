/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.util.ArrayList;
import java.util.List;
import core.RuleBuilder;

/**
 *
 * @author jagadeesh.t
 */
public class AtbMetaData {

    private String sourceName;
    private Object dataType;
    private String atbName;
    private boolean multivalAction;
    private List<HeuristicAtb> heuristicAtbs;
    private List<HeuristicAtb> selectedHeuristicAtbs;
    private UserDefinedFunction function;

    public UserDefinedFunction getFunction() {
        return function;
    }

    public void setFunction(UserDefinedFunction function) {
        this.function = function;
    }

    public void addHeuristicAtbs(HeuristicAtb atb) {
        if (heuristicAtbs == null) {
            heuristicAtbs = new ArrayList<HeuristicAtb>();
        }
        heuristicAtbs.add(atb);
    }

    public void addSelectedHeuristicAtbs(HeuristicAtb atb) {
        if (selectedHeuristicAtbs == null) {
            selectedHeuristicAtbs = new ArrayList<HeuristicAtb>();
        }
        selectedHeuristicAtbs.add(atb);
    }

    public List<HeuristicAtb> getHeuristicAtbs() {
        return heuristicAtbs;
    }

    public void setHeuristicAtbs(List<HeuristicAtb> heuristicAtbs) {
        this.heuristicAtbs = heuristicAtbs;
    }

    public List<HeuristicAtb> getSelectedHeuristicAtbs() {
        return selectedHeuristicAtbs;
    }

    public void setSelectedHeuristicAtbs(List<HeuristicAtb> selectedHeuristicAtbs) {
        this.selectedHeuristicAtbs = selectedHeuristicAtbs;
    }

    public boolean isMultivalAction() {
        return multivalAction;
    }

    public void setMultivalAction(boolean multivalAction) {
        this.multivalAction = multivalAction;
    }

    public String getAtbName() {
        return atbName;
    }

    public void setAtbName(String atbName) {
        this.atbName = atbName;
    }

    public Object getDataType() {
        return dataType;
    }

    public void setDataType(Object dataType) {
        this.dataType = dataType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

}
