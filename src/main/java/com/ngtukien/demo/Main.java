package com.ngtukien.demo;

import org.apache.catalina.startup.Tomcat;
import java.nio.file.Files;
import java.nio.file.Path;

/** Local launcher; the WAR can also be deployed to Tomcat 11. */
public class Main {
    public static Tomcat start(int port, Path webRoot) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(Files.createTempDirectory("sponsorship-tomcat-").toString());
        tomcat.setPort(port);
        tomcat.getConnector();
        var context = tomcat.addWebapp("", webRoot.toAbsolutePath().toString());
        context.setParentClassLoader(Main.class.getClassLoader());
        tomcat.start();
        return tomcat;
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("http.port", "8080"));
        Tomcat tomcat = start(port, Path.of("src/main/webapp"));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { tomcat.stop(); tomcat.destroy(); } catch (Exception ignored) { }
        }));
        tomcat.getServer().await();
    }
}