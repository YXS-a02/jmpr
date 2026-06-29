package fur.yxs.mper.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class gui extends Application{
    @Override
    public void start(Stage primaryStage) {
        winMain wm = new  winMain(this);
        winAddSrc was = new  winAddSrc(this);
        // 设置窗口
        primaryStage.setTitle("Player");
        primaryStage.setScene(wm.scene);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(320);
        primaryStage.show();
    }  
}
