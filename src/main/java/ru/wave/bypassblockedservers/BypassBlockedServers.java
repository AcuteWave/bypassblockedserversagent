package ru.wave.bypassblockedservers;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.awt.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URI;
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

                    setMethodBodyToFalse(ctClass, "isBlockedServerHostName"); // patchy last versions
                    setMethodBodyToFalse(ctClass, "isBlockedServer"); // остальные версии patchy и netty

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

    /**
     * Вызывается только при дабл клике по файлу или при открытии через консоль (java -jar)
     *
     * <p>Нужен чтобы открыть дураку страницу с гайдом об установке, потому что этот тупень не смог догадаться прочитать инструкцию
     */
    public static void main(String[] args) {
        try {
            Desktop.getDesktop().browse(new URI("https://mineland.net/how-to-login"));
        } catch (Exception ignored) {}
        System.exit(0);
    }
}