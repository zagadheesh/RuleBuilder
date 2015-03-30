/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

/**
 *
 * @author jagadeesh.t
 */
public class UserDefinedFunction {

        //private String atbName;
    //private String sourceName;
    //private String usedAtbName;
    private String name;
    private String exparam;
    private String nameUsedInExp;
        //private List<String> args;
    //private String usedName;

//        public String getAtbName() {
//            return atbName;
//        }
//
//        public void setAtbName(String atbName) {
//            this.atbName = atbName;
//        }
//
//        public String getSourceName() {
//            return sourceName;
//        }
//
//        public void setSourceName(String sourceName) {
//            this.sourceName = sourceName;
//        }
//
//        public String getUsedAtbName() {
//            return usedAtbName;
//        }
//
//        public void setUsedAtbName(String usedAtbName) {
//            this.usedAtbName = usedAtbName;
//        }
//        public List<String> getArgs() {
//            return args;
//        }
//
//        public void setArgs(List<String> args) {
//            this.args = args;
//        }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameUsedInExp() {
        return nameUsedInExp;
    }

    public void setNameUsedInExp(String nameUsedInExp) {
        this.nameUsedInExp = nameUsedInExp;
    }

//        public String getUsedName() {
//            return usedName;
//        }
//
//        public void setUsedName(String usedName) {
//            this.usedName = usedName;
//        }
    public String getExparam() {
        return exparam;
    }

    public void setExparam(String exparam) {
        this.exparam = exparam;
    }
}
