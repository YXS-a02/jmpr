package fur.yxs.mper.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class winMain {
    public gui g;
    public final HBox mainLayout = new HBox();
    public final VBox leftBar = new VBox(10);
    public final HBox playerAction = new HBox(10);
    public final Button playButton = new Button("▶");
    public final ComboBox<String> sourceCombo = new ComboBox<>();
    public final Button addButton = new Button("+");
    public final Label playerPic = new Label(" ");
    public final Slider volumeSlider = new Slider();
    public final Button otherButton = new Button("⋯");
    public final Button minusButton = new Button("-");
    public final ListView<String> playlistView = new ListView<>();
    public final Slider progressSlider = new Slider();
    public final Label mediaInfo = new Label("No media loaded");
    public final VBox rightBar = new VBox(8);
    public final Label progressText = new Label("00:00 / 00:00");
    public final Button removeButton = new Button("✕");
    public final HBox srcActionBar = new HBox(5);
    public final Label srcLabel = new Label("SRC");
    public final Scene scene = new Scene(mainLayout, 820, 565);

    /**
     * 创建主场景 - 所有UI组件的入口
     */
    winMain(gui G) {
        g = G;
        // 创建主布局 (对应 QHBoxLayout stretch="4,1")
        mainLayout.setSpacing(10);
        HBox.setHgrow(mainLayout, Priority.ALWAYS);
        // ========== 左侧区域 (对应 leftBar, stretch="4") ==========
        leftBar.setPadding(new Insets(10));
        leftBar.setAlignment(Pos.TOP_CENTER);
        leftBar.setPrefWidth(650);
        // 1. 播放器图片/封面区域 (对应 playerPic)
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
        progressSlider.setId("playerprogress");
        progressSlider.setOrientation(Orientation.HORIZONTAL);
        progressSlider.setMax(100);
        progressSlider.setValue(0);
        progressSlider.setShowTickLabels(false);
        progressSlider.setShowTickMarks(false);
        // 3. 播放控制栏 (对应 playerAction)
        playerAction.setAlignment(Pos.CENTER);
        playerAction.setPadding(new Insets(5, 0, 5, 0));
        // 播放/暂停按钮 (对应 playStatus)
        playButton.setId("playStatus");
        playButton.setPrefWidth(60);
        playButton.setStyle("-fx-font-size: 16px;");
        // 第二个按钮 (对应 pushButton_2)
        minusButton.setPrefWidth(60);
        minusButton.setStyle("-fx-font-size: 16px;");
        // 播放进度文本 (对应 playProgressText)
        progressText.setId("playProgressText");
        progressText.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 12px;");
        progressText.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressText, Priority.ALWAYS);
        // 音量控制滑块 (对应 voice)
        volumeSlider.setId("voice");
        volumeSlider.setOrientation(Orientation.HORIZONTAL);
        volumeSlider.setMax(100);
        volumeSlider.setValue(70);
        volumeSlider.setPrefWidth(100);
        volumeSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(volumeSlider, Priority.ALWAYS);
        playerAction.getChildren().addAll(playButton, minusButton, progressText, volumeSlider);
        // 设置 HBox 中各元素的拉伸比例 (对应原 stretch="1,1,9,9")
        HBox.setHgrow(playButton, Priority.NEVER);
        HBox.setHgrow(minusButton, Priority.NEVER);
        HBox.setHgrow(progressText, Priority.ALWAYS);
        HBox.setHgrow(volumeSlider, Priority.ALWAYS);
        // 4. 播放信息显示 (对应 Minfo)
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
        // ========== 右侧区域 (对应 rightBar, stretch="1") ==========
        rightBar.setPadding(new Insets(10));
        rightBar.setPrefWidth(240);
        rightBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 0 1px;");
        // 1. SRC 标签 (对应 label)
        srcLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5 0 5 0;");
        // 2. 源选择下拉框 (对应 srcChose)
        sourceCombo.setId("srcChose");
        sourceCombo.setPromptText("Select source");
        sourceCombo.setMaxWidth(Double.MAX_VALUE);
        // 3. 源操作按钮栏 (对应 srcAction)
        srcActionBar.setAlignment(Pos.CENTER);
        // 添加按钮 (对应 srcAdd)
        addButton.setId("srcAdd");
        addButton.setPrefWidth(60);
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // 删除按钮 (对应 srcRemove)
        removeButton.setId("srcRemove");
        removeButton.setPrefWidth(60);
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        // 其他/设置按钮 (对应 srcOther)
        otherButton.setId("srcOther");
        otherButton.setPrefWidth(60);
        otherButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        srcActionBar.getChildren().addAll(addButton, removeButton, otherButton);
        // 设置三个按钮等宽
        srcActionBar.setHgrow(addButton, Priority.ALWAYS);
        srcActionBar.setHgrow(removeButton, Priority.ALWAYS);
        srcActionBar.setHgrow(otherButton, Priority.ALWAYS);
        // 4. 播放列表视图 (对应 srcItems)
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
        // ========== 组装主布局 ==========
        mainLayout.getChildren().addAll(leftBar, rightBar);
        // 设置 HBox 中左右区域的拉伸比例
        HBox.setHgrow(leftBar, Priority.ALWAYS);
        HBox.setHgrow(rightBar, Priority.NEVER);
        // 创建场景
    }
    public void set(){
        addButton.setOnAction(e -> {
            // 添加源逻辑 - 由其他类实现
        });
    }
    public void open(){
        
    }
}