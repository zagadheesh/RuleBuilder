/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genrules;

/**
 *
 * @author jagadeesh.t
 */
import java.util.*;
import java.lang.*;
import java.math.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RuleClass2 {

    public static boolean _1(Map<String, Object> keyVal) throws Throwable {
        try {
            String s1_name = (String) keyVal.get("s1_name");
            if (((s1_name != null) && ((s1_name.equalsIgnoreCase("abc"))))) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
    }

    public static boolean _2(Map<String, Object> keyVal) throws Throwable {
        try {
            String s1_name = (String) keyVal.get("s1_name");
            if (((s1_name != null) && ((s1_name.equalsIgnoreCase("abc"))))) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
    }
}
