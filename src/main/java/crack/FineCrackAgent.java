package crack;
 
import crack.transformer.MultiMethodTransformer;
import crack.transformer.SingleMethodTransformer;
import javassist.ClassPool;
import javassist.CtClass;
 
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
 
public class FineCrackAgent {
    public static void agentmain(String args, Instrumentation inst) throws Exception {
        ClassPool pool = new ClassPool(true);
        CtClass cl = pool.get("java.lang.reflect.Modifier");
        cl.getDeclaredMethod("isNative").setBody("{ return true; }");
        inst.redefineClasses(new ClassDefinition(Class.forName(cl.getName(), false, null), cl.toBytecode()));
        System.out.println(cl.getName() + " 替换完成！");
        
        CtClass cl2 = pool.get("java.lang.reflect.Method");
        cl2.getDeclaredMethod("getModifiers").setBody("{ if (getName().equalsIgnoreCase(\"decrypt\")) { return modifiers | 0x100;} return modifiers;}");
        inst.redefineClasses(new ClassDefinition[] {
                new ClassDefinition(Class.forName(cl2.getName(), false, null), cl2.toBytecode())
        });
        System.out.println(cl2.getName() + " 替换完成！");

        Class<?>[] classes = inst.getAllLoadedClasses();
        for (Class<?> clazz : classes) {
            String name = clazz.getName();
            if (name.equals("com.fr.license.selector.LicenseContext")) {
                inst.addTransformer(new SingleMethodTransformer(name, "stopLicense", 1, 1, new byte[]{-79}, null), true);
                inst.retransformClasses(clazz);
                System.out.println(name + " 替换完成！");
            }
            if (name.equals("com.fr.license.security.LicFileRegistry")) {
                inst.addTransformer(new SingleMethodTransformer(name, "check", 1, 2, new byte[]{4, -84}, null), true);
                inst.retransformClasses(clazz);
                System.out.println(name + " 替换完成！");
            }
            if (name.equals("com.fr.license.entity.AbstractLicense")) {
                inst.addTransformer(new MultiMethodTransformer(name, "support", 1, 2, new byte[]{4, -84}, null), true);
                inst.retransformClasses(clazz);
                System.out.println(name + " 替换完成！");
            }
            if (name.equals("com.fr.license.selector.EncryptedLicenseSelector")) {
                inst.addTransformer(new SingleMethodTransformer(name, "decrypt", 1, 2, new byte[]{43, -80}, null), true);
                inst.retransformClasses(clazz);
                System.out.println(name + " 替换完成！");
            }
        }
    }
}