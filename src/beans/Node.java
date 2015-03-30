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
public class Node {
    
    

        public String data;
        public String expData;
        public Node leftChild;
        public Node rightChild;
        public boolean visited;
        public Object dataType;
        public List<String> variables;

        public List<String> getVariables() {
            return variables;
        }

        public void setVariables(List<String> variables) {
            this.variables = variables;
        }

        public void addToVariables(String variable) {
            if (variables == null) {
                variables = new ArrayList<String>();
            }
            variables.add(variable);
        }

        public void clearVariables(String variable) {
            if (variables != null) {
                variables.clear();
            }
        }

        public String getExpData() {
            return expData;
        }

        public void setExpData(String expData) {
            this.expData = expData;
        }

        public Object getDataType() {
            return dataType;
        }

        public void setDataType(Object dataType) {
            this.dataType = dataType;
        }

        public boolean isVisited() {
            return visited;
        }

        public void setVisited(boolean visited) {
            this.visited = visited;
        }

        public Node(String x) {
            data = x;
        }

        public void displayNode() {
            System.out.print(data);
        }
    
    
}
