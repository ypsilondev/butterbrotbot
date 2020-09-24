package tech.ypsilon.bbbot;

public class Init {

    static void preInit(){

    }

    static void init(){

    }

    static void postInit(){

    }

    static void startupComplete(){

    }

    static void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

        }));
    }

}
