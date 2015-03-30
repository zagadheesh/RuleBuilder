/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jagadeesh.t
 */
public class HeuristicAtb {

    private String name;
    private List<String> beans;
    private boolean rangeBased;
    private List<String> selectedBeans;

    public List<String> getSelectedBean() {
        return selectedBeans;
    }

    public void setSelectedBean(List<String> selectedBeans) {
        this.selectedBeans = selectedBeans;
    }

    public List<String> getBeans() {
        return beans;
    }

    public void setBeans(List<String> beans) {
        this.beans = beans;
    }

    public void addToSelectedBeans(String bean) {
        if (selectedBeans == null) {
            selectedBeans = new ArrayList<>();
        }
        selectedBeans.add(bean);
    }

    public void addToBeans(String bean) {
        if (beans == null) {
            beans = new ArrayList<>();
        }
        beans.add(bean);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRangeBased() {
        return rangeBased;
    }

    public void setRangeBased(boolean rangeBased) {
        this.rangeBased = rangeBased;
    }

}
