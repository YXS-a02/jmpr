package fur.yxs.mper.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class gui extends Application{
    public winMain wm;
    public winAddSrc was;
    @Override
    public void start(Stage primaryStage) {
        this.wm = new winMain(this);
        this.was = new winAddSrc(this);
        // 设置窗口
        primaryStage.setTitle("Player");
        primaryStage.setScene(wm.scene);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(320);
        primaryStage.show();
    }  
}
