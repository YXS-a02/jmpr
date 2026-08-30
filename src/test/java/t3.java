package fur.yxs.mper.ui;

import javafx.application.Application;
//import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
//import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
//import ga.a;

// 继承Application抽象类，重新start方法
public class t3 extends Application {
    /**
     * @param primaryStage 主窗口
     */
    @Override
    public void start(Stage primaryStage) {
        // 设置一个场景，场景里添加一个树形组件图，先创建一个标签
        Label label = new Label("Hello JavaFx!");
        Button playButton = new Button("Play");
        ProgressBar pb = new ProgressBar();
        playButton.setOnMouseClicked((event -> System.out.println("Hello")));

        // 创建布局，将标签放入布局里，BorderPane布局把场景划分为上下左右中，默认加入的控件在中间位置
        //HBox box = new HBox();
        BorderPane pane = new BorderPane();
        pane.setCenter(label);
        pane.setTop(playButton);
        pane.setBottom(pb);

        // 创建场景，将布局放入场景里，设置宽度和高度
        Scene scene = new Scene(pane, 300, 300);
        scene.setFill(Color.BLUE);
        // 将场景设置到窗口里
        primaryStage.setScene(scene);
        // 设置标题
        primaryStage.setTitle("我是窗口");
        primaryStage.show();
    }
    @Override
    public void stop(){
        System.out.println("-STOP!-");
    }
}