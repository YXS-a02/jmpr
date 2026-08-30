package fur.yxs.mper.core;

import fur.yxs.mper.core.EnvironmentDetector;
import fur.yxs.mper.ui.gui;
import fur.yxs.mper.core.Data;

public class App {
    public Data.srcList src = new Data.srcList();
    public void run(String[] args) {
        System.out.println("-START!-");
        EnvironmentDetector.Environment env = EnvironmentDetector.detectEnvironment();
        if (env == EnvironmentDetector.Environment.DESKTOP_GUI) {
            System.out.println("[]Desktop GUI");
            gui.launch(gui.class, args);
        } else{
            System.out.println("Detected environment: " + env);
        }
    }
    public void stop(){
        System.out.println("-STOP!-");
    }
}