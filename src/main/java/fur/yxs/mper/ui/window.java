package fur.yxs.mper.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class window extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 创建主布局 (对应 QHBoxLayout stretch="4,1")
        HBox mainLayout = new HBox();
        mainLayout.setSpacing(10);
        HBox.setHgrow(mainLayout, Priority.ALWAYS);

        // 左侧区域 (对应 leftBar, stretch="4")
        VBox leftBar = createLeftBar();
        leftBar.setPrefWidth(650);

        // 右侧区域 (对应 rightBar, stretch="1")
        VBox rightBar = createRightBar();
        rightBar.setPrefWidth(240);

        mainLayout.getChildren().addAll(leftBar, rightBar);

        // 设置 HBox 中左右区域的拉伸比例
        HBox.setHgrow(leftBar, Priority.ALWAYS);
        HBox.setHgrow(rightBar, Priority.NEVER);

        // 创建场景
        Scene scene = new Scene(mainLayout, 820, 565);

        // 添加 CSS 样式
        //scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        // 设置窗口
        primaryStage.setTitle("Player");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(320);
        primaryStage.show();
    }

    /**
     * 创建左侧区域 - 播放器主控制区
     * 对应原 Qt 的 leftBar (QVBoxLayout)
     */
    private VBox createLeftBar() {
        VBox leftBar = new VBox(10);
        leftBar.setPadding(new Insets(10));
        leftBar.setAlignment(Pos.TOP_CENTER);

        // 1. 播放器图片/封面区域 (对应 playerPic)
        Label playerPic = new Label(" ");
        playerPic.setId("playerPic");
        playerPic.setAlignment(Pos.CENTER);
        playerPic.setPrefHeight(300);
        playerPic.setMaxWidth(Double.MAX_VALUE);
        playerPic.setMaxHeight(Double.MAX_VALUE);
        playerPic.setStyle(
                "-fx-background-color: #2c3e50;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-border-color: #34495e;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;"
        );
        VBox.setVgrow(playerPic, Priority.ALWAYS);

        // 2. 播放进度条 (对应 playerprogress)
        Slider progressSlider = new Slider();
        progressSlider.setId("playerprogress");
        progressSlider.setOrientation(Orientation.HORIZONTAL);
        progressSlider.setMax(100);
        progressSlider.setValue(0);
        progressSlider.setShowTickLabels(false);
        progressSlider.setShowTickMarks(false);

        // 3. 播放控制栏 (对应 playerAction)
        HBox playerAction = createPlayerActionBar();

        // 4. 播放信息显示 (对应 Minfo)
        Label mediaInfo = new Label("No media loaded");
        mediaInfo.setId("mediaInfo");
        mediaInfo.setStyle("-fx-text-fill: #7f8c8d; -fx-padding: 5;");
        mediaInfo.setMaxWidth(Double.MAX_VALUE);
        mediaInfo.setAlignment(Pos.CENTER);

        leftBar.getChildren().addAll(playerPic, progressSlider, playerAction, mediaInfo);

        // 设置 VBox 中各元素的拉伸比例 (对应原 stretch="4,0,0,1")
        VBox.setVgrow(playerPic, Priority.ALWAYS);
        VBox.setVgrow(progressSlider, Priority.NEVER);
        VBox.setVgrow(playerAction, Priority.NEVER);
        VBox.setVgrow(mediaInfo, Priority.SOMETIMES);

        return leftBar;
    }

    /**
     * 创建播放控制栏
     * 对应原 playerAction (QHBoxLayout stretch="1,1,9,9")
     */
    private HBox createPlayerActionBar() {
        HBox actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER);
        actionBar.setPadding(new Insets(5, 0, 5, 0));

        // 播放/暂停按钮 (对应 playStatus)
        Button playButton = new Button("▶");
        playButton.setId("playStatus");
        playButton.setPrefWidth(60);
        playButton.setStyle("-fx-font-size: 16px;");

        // 第二个按钮 (对应 pushButton_2)
        Button minusButton = new Button("-");
        minusButton.setPrefWidth(60);
        minusButton.setStyle("-fx-font-size: 16px;");

        // 播放进度文本 (对应 playProgressText)
        Label progressText = new Label("00:00 / 00:00");
        progressText.setId("playProgressText");
        progressText.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 12px;");
        progressText.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressText, Priority.ALWAYS);

        // 音量控制滑块 (对应 voice)
        Slider volumeSlider = new Slider();
        volumeSlider.setId("voice");
        volumeSlider.setOrientation(Orientation.HORIZONTAL);
        volumeSlider.setMax(100);
        volumeSlider.setValue(70);
        volumeSlider.setPrefWidth(100);
        volumeSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(volumeSlider, Priority.ALWAYS);

        actionBar.getChildren().addAll(playButton, minusButton, progressText, volumeSlider);

        // 设置 HBox 中各元素的拉伸比例 (对应原 stretch="1,1,9,9")
        HBox.setHgrow(playButton, Priority.NEVER);
        HBox.setHgrow(minusButton, Priority.NEVER);
        HBox.setHgrow(progressText, Priority.ALWAYS);
        HBox.setHgrow(volumeSlider, Priority.ALWAYS);

        return actionBar;
    }

    /**
     * 创建右侧区域 - 播放列表/源管理区
     * 对应原 rightBar (QVBoxLayout)
     */
    private VBox createRightBar() {
        VBox rightBar = new VBox(8);
        rightBar.setPadding(new Insets(10));
        rightBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 0 1px;");

        // 1. SRC 标签 (对应 label)
        Label srcLabel = new Label("SRC");
        srcLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5 0 5 0;");

        // 2. 源选择下拉框 (对应 srcChose)
        ComboBox<String> sourceCombo = new ComboBox<>();
        sourceCombo.setId("srcChose");
        sourceCombo.setPromptText("Select source");
        sourceCombo.setMaxWidth(Double.MAX_VALUE);

        // 3. 源操作按钮栏 (对应 srcAction)
        HBox srcActionBar = createSourceActionBar();

        // 4. 播放列表视图 (对应 srcItems)
        ListView<String> playlistView = new ListView<>();
        playlistView.setId("srcItems");
        playlistView.setPrefHeight(200);
        playlistView.setStyle("-fx-control-inner-background: white;");
        VBox.setVgrow(playlistView, Priority.ALWAYS);

        rightBar.getChildren().addAll(srcLabel, sourceCombo, srcActionBar, playlistView);

        // 设置 VBox 中各元素的拉伸比例
        VBox.setVgrow(srcLabel, Priority.NEVER);
        VBox.setVgrow(sourceCombo, Priority.NEVER);
        VBox.setVgrow(srcActionBar, Priority.NEVER);
        VBox.setVgrow(playlistView, Priority.ALWAYS);

        return rightBar;
    }

    /**
     * 创建源操作按钮栏
     * 对应原 srcAction (QHBoxLayout)
     */
    private HBox createSourceActionBar() {
        HBox actionBar = new HBox(5);
        actionBar.setAlignment(Pos.CENTER);

        // 添加按钮 (对应 srcAdd)
        Button addButton = new Button("+");
        addButton.setId("srcAdd");
        addButton.setPrefWidth(60);
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        // 删除按钮 (对应 srcRemove)
        Button removeButton = new Button("✕");
        removeButton.setId("srcRemove");
        removeButton.setPrefWidth(60);
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

        // 其他/设置按钮 (对应 srcOther)
        Button otherButton = new Button("⋯");
        otherButton.setId("srcOther");
        otherButton.setPrefWidth(60);
        otherButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");

        actionBar.getChildren().addAll(addButton, removeButton, otherButton);

        // 设置三个按钮等宽
        actionBar.setHgrow(addButton, Priority.ALWAYS);
        actionBar.setHgrow(removeButton, Priority.ALWAYS);
        actionBar.setHgrow(otherButton, Priority.ALWAYS);

        return actionBar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}