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

public class RuleClass1 {

    public static boolean _1(Map<String, Object> keyVal) throws Throwable {
        try {
            BigDecimal s1_age = (BigDecimal) keyVal.get("s1_age");
            if (((s1_age != null) && ((s1_age.compareTo(new BigDecimal("30").setScale(2, BigDecimal.ROUND_HALF_UP)) > 0)))) {
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
