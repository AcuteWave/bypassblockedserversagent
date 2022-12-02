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
                if (className.equals("io.netty.bootstrap.Bootstrap") || className.equals("com.mojang.patchy.BlockedServers") || className.contains("BlockedServers")) {
                    pool.appendClassPath(new ByteArrayClassPath(className, bytes));
                    CtClass ctClass = pool.get(className);
                    setMethodBodyToFalse(ctClass, "isBlockedServerHostName");
                    setMethodBodyToFalse(ctClass, "isBlockedServer");
                    setMethodBodyToFalse(ctClass, "test");
                    return ctClass.toBytecode();
                } else if (className.equals("com.mojang.patchy.MojangBlockListSupplier") || className.equals("BlockListSupplier")) {
                    pool.appendClassPath(new ByteArrayClassPath(className, bytes));
                    CtClass ctClass = pool.get(className);
                    CtMethod ctMethod = ctClass.getMethod("createBlockList", "");
                    ctMethod.setBody("{ return null; }");
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void setMethodBodyToFalse(CtClass ctClass, String methodName) {
        try {
            CtMethod ctMethod = ctClass.getMethod(methodName, "(Ljava/lang/String;)Z");
            ctMethod.setBody("{ return false; }");
        } catch (Exception ignored) {
        }
    }
}