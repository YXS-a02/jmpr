package fur.yxs.mper.ui;

import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.event.EventHandler;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class t extends Application {

    @Override
    public void start(Stage stage) {
        //绘制一个盒子
        Box box = new Box();

        //设置盒子的属性
        box.setWidth(150.0);
        box.setHeight(150.0);
        box.setDepth(100.0);

        //设置盒子的位置
        box.setTranslateX(350);
        box.setTranslateY(150);
        box.setTranslateZ(50);

        //设置文本
        Text text = new Text("输入任意字母即可旋转盒子，然后单击盒子即可停止旋转");

                //设置文本的字体
                text.setFont(Font.font(null, FontWeight.BOLD, 15));

        //设置文本的颜色
        text.setFill(Color.CRIMSON);

        //设置文本的位置
        text.setX(20);
        text.setY(50);

        //设置盒子的材质
        //PhongMaterialmaterial = new PhongMaterial();
        //material.setDiffuseColor(Color.DARKSLATEBLUE);

        //设置盒子的漫反射颜色材质
        //box.setMaterial(material);

        //设置盒子的旋转动画
        RotateTransition rotateTransition = new RotateTransition();

        //设置过渡的持续时间
        rotateTransition.setDuration(Duration.millis(1000));

        //设置过渡的节点
        rotateTransition.setNode(box);

        //设置旋转的轴
        rotateTransition.setAxis(Rotate.Y_AXIS);

        //设置旋转的角度
        rotateTransition.setByAngle(360);

        //设置过渡的循环次数
        rotateTransition.setCycleCount(50);

        //将自动反转值设置为 false
        rotateTransition.setAutoReverse(false);

        //创建文本字段
        TextField textField = new TextField();

        //设置文本字段的位置
        textField.setLayoutX(50);
        textField.setLayoutY(100);

        //处理按键类型事件
        EventHandler<KeyEvent> eventHandlerTextField = new EventHandler<>() {
            @Override
            public void handle(KeyEvent event) {
                //播放动画
                rotateTransition.play();
            }
        };
        //向文本字段添加事件处理程序
        textField.addEventHandler(KeyEvent.KEY_TYPED, eventHandlerTextField);

        //处理鼠标点击事件(在框上)
        EventHandler<javafx.scene.input.MouseEvent> eventHandlerBox =
                new EventHandler<javafx.scene.input.MouseEvent>() {

                    @Override
                    public void handle(javafx.scene.input.MouseEvent e) {
                        rotateTransition.stop();
                    }
                };
        //将事件处理程序添加到框
        box.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, eventHandlerBox);

        //创建一个 Group 对象
        Group root = new Group(box, textField, text);

        //创建一个场景对象
        Scene scene = new Scene(root, 600, 300);

        //设置相机
        PerspectiveCamera camera = new PerspectiveCamera(false);
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(0);
        scene.setCamera(camera);

        //设置舞台(Stage)标题
        stage.setTitle("事件处理程序示例");

        //将场景添加到舞台(Stage)
        stage.setScene(scene);

        //显示舞台(Stage)内容
        stage.show();
    }
    public static void main(String[] args){
        launch(args);
    }
}
 