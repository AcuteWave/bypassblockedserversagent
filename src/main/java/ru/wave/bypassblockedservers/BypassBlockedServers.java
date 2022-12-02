package ru.wave.bypassblockedservers;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class BypassBlockedServers implements ClassFileTransformer {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        instrumentation.addTransformer(new BypassBlockedServers());
    }

    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingTransformed, ProtectionDomain protectionDomain, byte[] bytes) {
        try {
            if (className != null) {
                className = className.replace("/", ".");
                ClassPool pool = ClassPool.getDefault();
                if (className.equals("io.netty.bootstrap.Bootstrap") || className.equals("com.mojang.patchy.BlockedServers")) {
                    pool.appendClassPath(new ByteArrayClassPath(className, bytes));
                    CtClass ctClass = pool.get(className);

                    setMethodBodyToFalse(ctClass, "isBlockedServerHostName");
                    setMethodBodyToFalse(ctClass, "isBlockedServer");

                    return ctClass.toBytecode();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void setMethodBodyToFalse(CtClass ctClass, String methodName) {
        try {
            CtMethod ctMethod = ctClass.getMethod(methodName, "(Ljava/lang/String;)Z");
            ctMethod.setBody("{ return false; }");
        } catch (Exception ignored) {}
    }
}