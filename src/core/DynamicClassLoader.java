/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 *
 * @author ashiskumar.m
 */
public class DynamicClassLoader {

    public static Class loadClass(String className, String javaCode) throws Exception {
        MemoryClassLoader mcl = new MemoryClassLoader(className, javaCode);
        Class c = mcl.loadClass(className);
        return c;
    }

    public static Object executeMethod(Class c, String methodName, Class[] paramType, Object objParam) throws Throwable {
        return c.getMethod(methodName, paramType).invoke(null, objParam);
    }

    public static void main(String[] args) {
        String code = "import java.util.*;import java.lang.*;import java.math.*;import com.imi.sdh.reco.core.*;public class pbpromo{public static boolean _1(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_arpu=(Double)keyVal.get(\"punjabprofile_arpu\");if((punjabprofile_arpu<=10.0d)){return true;}else {return false;}}public static boolean _2(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_arpu=(Double)keyVal.get(\"punjabprofile_arpu\");if(((punjabprofile_arpu>10.0d)&&(punjabprofile_arpu<50.0d))){return true;}else {return false;}}public static boolean _3(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_arpu=(Double)keyVal.get(\"punjabprofile_arpu\");if((punjabprofile_arpu>=50.0d)){return true;}else {return false;}}public static boolean _4(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_prepaid_balance=(Double)keyVal.get(\"punjabprofile_prepaid_balance\");if((punjabprofile_prepaid_balance<=10.0d)){return true;}else {return false;}}public static boolean _5(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_prepaid_balance=(Double)keyVal.get(\"punjabprofile_prepaid_balance\");if((punjabprofile_prepaid_balance>10.0d)){return true;}else {return false;}}public static boolean _6(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_data_usage=(Double)keyVal.get(\"punjabprofile_data_usage\");if((punjabprofile_data_usage<50.0d)){return true;}else {return false;}}public static boolean _7(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_isd_mou=(Double)keyVal.get(\"punjabprofile_isd_mou\");if(((punjabprofile_isd_mou>0.0d)&&(punjabprofile_isd_mou<10.0d))){return true;}else {return false;}}public static boolean _8(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_tot_sms=(Double)keyVal.get(\"punjabprofile_tot_sms\");if((punjabprofile_tot_sms>100.0d)){return true;}else {return false;}}public static boolean _9(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_tot_sms=(Double)keyVal.get(\"punjabprofile_tot_sms\");if((punjabprofile_tot_sms<100.0d)){return true;}else {return false;}}public static boolean _10(KeyVal<String,Object> keyVal) throws Throwable{double punjabprofile_tot_sms=(Double)keyVal.get(\"punjabprofile_tot_sms\");if((punjabprofile_tot_sms==0.0d)){return true;}else {return false;}}}";
        try {
            DynamicClassLoader.loadClass("pbpromo", code);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class Source extends SimpleJavaFileObject {

    private final String content;

    Source(String name, Kind kind, String content) {
        super(URI.create("memo:///" + name.replace('.', '/') + kind.extension), kind);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignore) {
        return this.content;
    }
}

class Output extends SimpleJavaFileObject {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    Output(String name, Kind kind) {
        super(URI.create("memo:///" + name.replace('.', '/') + kind.extension), kind);
    }

    byte[] toByteArray() {
        return this.baos.toByteArray();
    }

    @Override
    public ByteArrayOutputStream openOutputStream() {
        return this.baos;
    }
}

class MemoryFileManager extends ForwardingJavaFileManager {

    public final Map map = new HashMap();

    MemoryFileManager(JavaCompiler compiler) {
        super(compiler.getStandardFileManager(null, null, null));
    }

    @Override
    public Output getJavaFileForOutput(Location location, String name, Kind kind, FileObject source) {
        Output mc = new Output(name, kind);
        this.map.put(name, mc);
        return mc;
    }
}

class MemoryClassLoader extends ClassLoader {

    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final MemoryFileManager manager = new MemoryFileManager(this.compiler);

    public MemoryClassLoader(String classname, String filecontent) {
        this(Collections.singletonMap(classname, filecontent));
    }

    public MemoryClassLoader(Map map) {
        List list = new ArrayList();
        for (Object obj : map.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            list.add(new Source((String) entry.getKey(), Kind.SOURCE, (String) entry.getValue()));
        }
        this.compiler.getTask(null, this.manager, null, null, null, list).call();
    }

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        synchronized (this.manager) {
            Output mc = (Output) this.manager.map.remove(name);
            if (mc != null) {
                byte[] array = mc.toByteArray();
                return defineClass(name, array, 0, array.length);
            }
        }
        return super.findClass(name);
    }
}
