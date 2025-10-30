package yxs.a02.mper;

import yxs.a02.mper.model.*;
import yxs.a02.mper.service.*;
import yxs.a02.mper.ui.*;
import yxs.a02.mper.ui.components.*;
import yxs.a02.mper.util.ResourceBundleManager;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

// 主应用程序类
public class Main extends JFrame {
    // UI Components
    private JPanel contentPane;
    private PlayerPanel playerPanel;
    private ControlPanel controlPanel;
    private SidebarPanel sidebarPanel;
    private MediaFileRenderer fileListRenderer;

    // Data
    private List<MediaFile> mediaFiles = new ArrayList<>();
    private List<ApiEndpoint> apiEndpoints = new ArrayList<>();
    private MediaFile currentMediaFile;
    private ApiEndpoint currentApi;
    private int currentFileIndex = -1;

    // Services
    private DatabaseManager databaseManager;
    private ApiClient apiClient;
    private MediaPlayer mediaPlayer;

    // UI State
    private boolean isPlaying = false;
    private Timer progressTimer;
    private boolean coverVisible = true;

    // Constants
    private static final Color BACKGROUND_DARK = new Color(26, 26, 26);

    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 强制设置UTF-8编码
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.stdout.encoding", "UTF-8");
        System.setProperty("sun.stderr.encoding", "UTF-8");

        java.awt.EventQueue.invokeLater(() -> {
            try {
                Main frame = new Main();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Main() {
        initializeServices();
        initializeUI();
        setupEventListeners();
        loadInitialData();
    }

    private void initializeServices() {
        databaseManager = new DatabaseManager();
        apiClient = new ApiClient();
        mediaPlayer = new MediaPlayer();
    }

    private void initializeUI() {
        setTitle(ResourceBundleManager.getString("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1200, 700);
        setMinimumSize(new Dimension(1000, 600));

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(10, 10));
        contentPane.setBackground(BACKGROUND_DARK);
        setContentPane(contentPane);

        createMainLayout();
    }

    private void createMainLayout() {
        // 主分割面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(800);
        mainSplitPane.setBackground(BACKGROUND_DARK);

        // 左侧 - 播放器区域
        JPanel playerSection = createPlayerSection();
        mainSplitPane.setLeftComponent(playerSection);

        // 右侧 - 侧边栏
        mainSplitPane.setRightComponent(sidebarPanel);

        contentPane.add(mainSplitPane, BorderLayout.CENTER);
    }

    private JPanel createPlayerSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_DARK);

        // 初始化UI组件
        playerPanel = new PlayerPanel();
        controlPanel = new ControlPanel();

        panel.add(playerPanel, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupEventListeners() {
        // 播放/暂停按钮
        controlPanel.getPlayPauseBtn().addActionListener(e -> togglePlayPause());

        // 停止按钮
        controlPanel.getStopButton().addActionListener(e -> stopPlayback());

        // 上一首/下一首按钮
        controlPanel.getPrevBtn().addActionListener(e -> playPrevious());
        controlPanel.getNextBtn().addActionListener(e -> playNext());

        // 打开本地文件
        controlPanel.getOpenFileBtn().addActionListener(e -> openLocalFile());

        // 文件列表选择
        sidebarPanel.getFileList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                MediaFile selectedFile = sidebarPanel.getFileList().getSelectedValue();
                if (selectedFile != null) {
                    playMediaFile(selectedFile);
                }
            }
        });

        // 进度条
        controlPanel.getProgressSlider().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentMediaFile != null) {
                    float position = controlPanel.getProgressSlider().getValue() / 1000.0f;
                    mediaPlayer.setPosition(position);
                    updateProgress();
                }
            }
        });

        // 音量控制
        controlPanel.getVolumeSlider().addChangeListener(e -> {
            mediaPlayer.setVolume(controlPanel.getVolumeSlider().getValue());
        });

        // 播放速度
        controlPanel.getSpeedComboBox().addActionListener(e -> {
            String selectedSpeed = (String) controlPanel.getSpeedComboBox().getSelectedItem();
            if (selectedSpeed != null) {
                float speed = Float.parseFloat(selectedSpeed.replace("x", ""));
                mediaPlayer.setSpeed(speed);
            }
        });

        // API选择
        sidebarPanel.getApiSelector().addActionListener(e -> {
            ApiEndpoint selectedApi = (ApiEndpoint) sidebarPanel.getApiSelector().getSelectedItem();
            if (selectedApi != null) {
                currentApi = selectedApi;
                refreshFileList();
            }
        });

        // API管理按钮
        sidebarPanel.getRefreshApiBtn().addActionListener(e -> refreshFileList());
        sidebarPanel.getAddApiBtn().addActionListener(e -> showAddApiDialog());
        sidebarPanel.getDeleteApiBtn().addActionListener(e -> deleteCurrentApi());

        // 编辑API按钮（从侧边栏获取编辑按钮）
        JButton editApiBtn = findEditApiButton();
        if (editApiBtn != null) {
            editApiBtn.addActionListener(e -> showEditApiDialog());
        }

        // API连接测试（双击API选择器）
        sidebarPanel.getApiSelector().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    testApiConnection();
                }
            }
        });

        // 进度更新定时器
        progressTimer = new Timer(100, e -> updateProgress());
        progressTimer.start();

        // 双击全屏
        contentPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleFullscreen();
                }
            }
        });

        // 设置文件列表渲染器
        fileListRenderer = new MediaFileRenderer();
        sidebarPanel.getFileList().setCellRenderer(fileListRenderer);
    }

    private JButton findEditApiButton() {
        // 在API按钮面板中查找编辑按钮（第4个按钮）
        Component[] components = ((JPanel) sidebarPanel.getRefreshApiBtn().getParent()).getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if ("✎".equals(button.getText())) {
                    return button;
                }
            }
        }
        return null;
    }

    private void loadInitialData() {
        // 从数据库加载API端点
        apiEndpoints = databaseManager.getApiEndpoints();
        refreshApiSelector();

        // 设置默认API（如果可用）
        if (!apiEndpoints.isEmpty()) {
            currentApi = apiEndpoints.get(0);
            // 不设置选择器的选中项，因为refreshApiSelector已经默认选中第一个
            JComboBox<ApiEndpoint> apiSelector = sidebarPanel.getApiSelector();
            ActionListener[] listeners = apiSelector.getActionListeners();
            for (ActionListener listener : listeners) {
                apiSelector.removeActionListener(listener);
            }
            apiSelector.setSelectedItem(currentApi);
            for (ActionListener listener : listeners) {
                apiSelector.addActionListener(listener);
            }
            refreshFileList();
        }
    }

    private void refreshApiSelector() {
        sidebarPanel.getApiSelector().removeAllItems();
        for (ApiEndpoint endpoint : apiEndpoints) {
            sidebarPanel.getApiSelector().addItem(endpoint);
        }
    }

    private void refreshFileList() {
        if (currentApi == null) return;

        new SwingWorker<List<MediaFile>, Void>() {
            @Override
            protected List<MediaFile> doInBackground() throws Exception {
                return apiClient.getMediaFiles(currentApi.getUrl());
            }

            @Override
            protected void done() {
                try {
                    List<MediaFile> files = get();
                    mediaFiles.clear();
                    sidebarPanel.getFileListModel().clear();

                    for (MediaFile file : files) {
                        mediaFiles.add(file);
                        sidebarPanel.getFileListModel().addElement(file);
                    }

                    if (files.isEmpty()) {
                        showMessage(ResourceBundleManager.getString("message.no_media_files"));
                    } else {
                        showMessage(MessageFormat.format(
                                ResourceBundleManager.getString("message.loaded_files"),
                                files.size()
                        ));
                    }
                } catch (Exception e) {
                    showError(MessageFormat.format(
                            ResourceBundleManager.getString("message.load_failed"),
                            e.getMessage()
                    ));
                }
            }
        }.execute();
    }

    private void playMediaFile(MediaFile file) {
        currentMediaFile = file;
        currentFileIndex = mediaFiles.indexOf(file);

        // 更新文件列表渲染器的当前文件
        fileListRenderer.setCurrentMediaFile(file);
        sidebarPanel.getFileList().repaint();

        try {
            // 更新媒体信息
            updateMediaInfo(file);

            // 获取音频文件的ID3信息
            if ("audio".equals(file.getType())) {
                try {
                    ID3Info id3InfoData = apiClient.getID3Info(currentApi.getUrl(), file.getPath());
                    updateID3Info(id3InfoData);
                } catch (Exception e) {
                    System.err.println("获取ID3信息失败: " + e.getMessage());
                    // 使用模拟ID3信息
                    ID3Info fallbackInfo = new ID3Info();
                    fallbackInfo.setTitle(file.getName().replace(".mp3", ""));
                    fallbackInfo.setArtist("unknown");
                    fallbackInfo.setAlbum("unknown");
                    updateID3Info(fallbackInfo);
                }
            } else {
                playerPanel.getId3Info().setText(ResourceBundleManager.getString("player.video_no_id3"));
            }

            // 更新封面显示
            updateCoverDisplay(file);

            // 播放媒体 - 简化逻辑
            String mediaUrl;
            if (currentApi != null && isApiFilePath(file.getPath())) {
                // API返回的文件，使用API流媒体URL
                String baseUrl = getApiBaseUrl(currentApi.getUrl());
                mediaUrl = apiClient.getMediaStreamUrl(baseUrl, file.getPath());
                System.out.println("使用API流媒体URL: " + mediaUrl);
            } else {
                // 本地文件
                mediaUrl = file.getPath();
                System.out.println("使用本地文件: " + mediaUrl);
            }

            if (!mediaUrl.isEmpty()) {
                mediaPlayer.play(mediaUrl);
                isPlaying = true;
                controlPanel.getPlayPauseBtn().setText("⏸");

                // 添加到播放历史
                databaseManager.addPlaybackHistory(file.getPath(), file.getName());
            }

            // 更新文件列表选择
            sidebarPanel.getFileList().setSelectedValue(file, true);

        } catch (Exception e) {
            showError(MessageFormat.format(
                    ResourceBundleManager.getString("message.play_failed"),
                    e.getMessage()
            ));
            e.printStackTrace();
        }
    }

    // 获取API基础URL（移除参数）
    private String getApiBaseUrl(String apiUrl) {
        if (apiUrl == null || apiUrl.isEmpty()) {
            return apiUrl;
        }

        if (apiUrl.contains("?")) {
            return apiUrl.split("\\?")[0];
        }
        return apiUrl;
    }

    // 判断是否为API文件路径
    private boolean isApiFilePath(String filePath) {
        // API返回的文件路径通常是相对路径或简单的文件名
        return !new File(filePath).isAbsolute() &&
                !filePath.startsWith("http") &&
                !filePath.startsWith("file://");
    }

    // 测试API媒体可用性
    private boolean testApiMediaAvailability(String mediaUrl) {
        try {
            // 对于HTTP URL，发送HEAD请求测试
            if (mediaUrl.startsWith("http")) {
                java.net.URL url = new java.net.URL(mediaUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                int responseCode = connection.getResponseCode();
                System.out.println("API媒体测试响应码: " + responseCode);

                return responseCode == 200;
            }
            return true; // 对于非HTTP URL，假设可用
        } catch (Exception e) {
            System.err.println("API媒体测试失败: " + e.getMessage());
            return false;
        }
    }

    // 查找本地媒体文件
    private String findLocalMediaFile(MediaFile apiFile) {
        String fileName = apiFile.getName();
        System.out.println("查找本地文件: " + fileName);

        // 搜索常见目录
        String[] testDirs = {
                System.getProperty("user.home") + "/Music",
                System.getProperty("user.home") + "/Desktop",
                System.getProperty("user.home") + "/Downloads",
                System.getProperty("user.dir") + "/test_media",
                System.getProperty("user.dir")
        };

        // 首先查找完全匹配的文件名
        for (String dir : testDirs) {
            File testDir = new File(dir);
            if (testDir.exists() && testDir.isDirectory()) {
                File[] files = testDir.listFiles((d, name) ->
                        name.equals(fileName));

                if (files != null && files.length > 0) {
                    String filePath = files[0].getAbsolutePath();
                    System.out.println("找到完全匹配的本地文件: " + filePath);
                    return filePath;
                }
            }
        }

        // 如果没有完全匹配，查找相同扩展名的文件
        String fileExtension = getFileExtension(fileName);
        if (!fileExtension.isEmpty()) {
            for (String dir : testDirs) {
                File testDir = new File(dir);
                if (testDir.exists() && testDir.isDirectory()) {
                    File[] files = testDir.listFiles((d, name) ->
                            name.toLowerCase().endsWith("." + fileExtension.toLowerCase()));

                    if (files != null && files.length > 0) {
                        String filePath = files[0].getAbsolutePath();
                        System.out.println("找到扩展名匹配的本地文件: " + filePath);
                        return filePath;
                    }
                }
            }
        }

        System.out.println("未找到对应的本地文件");
        return "";
    }

    // 获取文件扩展名
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private String getTestMediaFilePath(MediaFile file) {
        // 搜索常见目录中的音频文件
        String[] testDirs = {
                System.getProperty("user.home") + "/Music",
                System.getProperty("user.home") + "/Desktop",
                System.getProperty("user.home") + "/Downloads",
                System.getProperty("user.dir") + "/test_media",
                System.getProperty("user.dir")
        };

        // 支持的音频格式（优先级排序）
        String[] audioExtensions = {".wav", ".mp3", ".flac", ".aiff", ".aif"};

        for (String dir : testDirs) {
            File testDir = new File(dir);
            if (testDir.exists() && testDir.isDirectory()) {
                for (String ext : audioExtensions) {
                    File[] audioFiles = testDir.listFiles((d, name) ->
                            name.toLowerCase().endsWith(ext));
                    if (audioFiles != null && audioFiles.length > 0) {
                        String filePath = audioFiles[0].getAbsolutePath();
                        System.out.println("找到测试音频文件: " + filePath);
                        return filePath;
                    }
                }
            }
        }

        // 如果没有找到音频文件，创建测试目录
        File testDir = new File(System.getProperty("user.dir") + "/test_media");
        if (!testDir.exists()) {
            testDir.mkdirs();
            showMessage("请将音频文件(WAV, MP3, FLAC等)放入: " + testDir.getAbsolutePath());
        }

        showMessage(ResourceBundleManager.getString("message.no_audio_files"));
        return "";
    }

    private void playPrevious() {
        if (mediaFiles.isEmpty() || currentFileIndex == -1) return;

        int newIndex = (currentFileIndex - 1 + mediaFiles.size()) % mediaFiles.size();
        playMediaFile(mediaFiles.get(newIndex));
    }

    private void playNext() {
        if (mediaFiles.isEmpty() || currentFileIndex == -1) return;

        int newIndex = (currentFileIndex + 1) % mediaFiles.size();
        playMediaFile(mediaFiles.get(newIndex));
    }

    private void togglePlayPause() {
        if (currentMediaFile == null) {
            showMessage(ResourceBundleManager.getString("message.select_file_first"));
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            controlPanel.getPlayPauseBtn().setText("▶");
            isPlaying = false;
        } else {
            try {
                mediaPlayer.resume();
                controlPanel.getPlayPauseBtn().setText("⏸");
                isPlaying = true;
            } catch (Exception e) {
                showError(MessageFormat.format(
                        ResourceBundleManager.getString("message.play_failed"),
                        e.getMessage()
                ));
                e.printStackTrace();
            }
        }
    }

    private void stopPlayback() {
        mediaPlayer.stop();
        isPlaying = false;
        controlPanel.getPlayPauseBtn().setText("▶");
        controlPanel.getProgressSlider().setValue(0);
        controlPanel.getCurrentTimeLabel().setText("0:00");
    }

    private void openLocalFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // 支持更多音频格式
        FileNameExtensionFilter audioFilter = new FileNameExtensionFilter(
                "音频文件 (*.wav, *.mp3, *.aiff, *.au, *.aif, *.snd, *.flac, *.m4a, *.aac, *.ogg)",
                "wav", "mp3", "aiff", "au", "aif", "snd", "flac", "m4a", "aac", "ogg");

        // 视频文件过滤器
        FileNameExtensionFilter videoFilter = new FileNameExtensionFilter(
                "视频文件 (*.mp4, *.avi, *.mkv, *.mov, *.wmv, *.flv)",
                "mp4", "avi", "mkv", "mov", "wmv", "flv");

        // 所有支持的文件
        FileNameExtensionFilter allFilter = new FileNameExtensionFilter(
                "所有支持的文件",
                "wav", "mp3", "aiff", "au", "aif", "snd", "flac", "m4a", "aac", "ogg",
                "mp4", "avi", "mkv", "mov", "wmv", "flv");

        fileChooser.addChoosableFileFilter(audioFilter);
        fileChooser.addChoosableFileFilter(videoFilter);
        fileChooser.addChoosableFileFilter(allFilter);
        fileChooser.setFileFilter(allFilter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            playLocalFile(selectedFile);
        }
    }

    private void playLocalFile(File file) {
        try {
            // 创建本地媒体文件对象
            MediaFile localFile = new MediaFile();
            localFile.setName(file.getName());
            localFile.setPath(file.getAbsolutePath());
            localFile.setType(getFileType(file));
            localFile.setSize(file.length());

            // 尝试获取音频时长（简化处理）
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                long frames = audioStream.getFrameLength();
                double durationInSeconds = (frames + 0.0) / audioStream.getFormat().getFrameRate();
                localFile.setDuration(durationInSeconds);
                audioStream.close();
            } catch (Exception e) {
                localFile.setDuration(0.0); // 无法获取时长
            }

            // 添加到文件列表
            mediaFiles.add(localFile);
            sidebarPanel.getFileListModel().addElement(localFile);

            // 播放文件
            playMediaFile(localFile);

            showMessage(MessageFormat.format(
                    ResourceBundleManager.getString("message.local_file_added"),
                    file.getName()
            ));
        } catch (Exception e) {
            showError(MessageFormat.format(
                    ResourceBundleManager.getString("message.local_file_failed"),
                    e.getMessage()
            ));
        }
    }

    private String getFileType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".wav") || name.endsWith(".mp3") ||
                name.endsWith(".aiff") || name.endsWith(".au") ||
                name.endsWith(".aif") || name.endsWith(".snd") ||
                name.endsWith(".flac") || name.endsWith(".m4a") ||
                name.endsWith(".aac") || name.endsWith(".ogg")) {
            return "audio";
        } else {
            return "video";
        }
    }

    private void showAddApiDialog() {
        JTextField nameField = new JTextField();
        JTextField urlField = new JTextField("http://");

        Object[] message = {
                ResourceBundleManager.getString("dialog.api_name") + ":", nameField,
                ResourceBundleManager.getString("dialog.api_url") + ":", urlField
        };

        int option = JOptionPane.showConfirmDialog(this, message,
                ResourceBundleManager.getString("dialog.add_api"),
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String url = urlField.getText().trim();

            if (!name.isEmpty() && !url.isEmpty()) {
                ApiEndpoint endpoint = new ApiEndpoint(name, url);
                if (databaseManager.addApiEndpoint(endpoint)) {
                    apiEndpoints = databaseManager.getApiEndpoints();
                    refreshApiSelector();
                    sidebarPanel.getApiSelector().setSelectedItem(endpoint);
                    showMessage(ResourceBundleManager.getString("message.api_add_success"));
                } else {
                    showError(ResourceBundleManager.getString("message.api_add_failed"));
                }
            } else {
                showError(ResourceBundleManager.getString("message.complete_api_info"));
            }
        }
    }

    private void showEditApiDialog() {
        ApiEndpoint selectedApi = (ApiEndpoint) sidebarPanel.getApiSelector().getSelectedItem();
        if (selectedApi == null) {
            showMessage("请先选择要编辑的API");
            return;
        }

        JTextField nameField = new JTextField(selectedApi.getName());
        JTextField urlField = new JTextField(selectedApi.getUrl());

        Object[] message = {
                "API名称:", nameField,
                "API URL:", urlField
        };

        int option = JOptionPane.showConfirmDialog(this, message,
                "编辑API",
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String url = urlField.getText().trim();

            if (!name.isEmpty() && !url.isEmpty()) {
                selectedApi.setName(name);
                selectedApi.setUrl(url);
                if (databaseManager.updateApiEndpoint(selectedApi)) {
                    apiEndpoints = databaseManager.getApiEndpoints();
                    refreshApiSelector();
                    sidebarPanel.getApiSelector().setSelectedItem(selectedApi);
                    showMessage("API更新成功");
                } else {
                    showError("API更新失败");
                }
            } else {
                showError("请填写完整的API信息");
            }
        }
    }

    private void testApiConnection() {
        ApiEndpoint selectedApi = (ApiEndpoint) sidebarPanel.getApiSelector().getSelectedItem();
        if (selectedApi == null) {
            showMessage("请先选择要测试的API");
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return apiClient.testConnection(selectedApi.getUrl());
            }

            @Override
            protected void done() {
                try {
                    boolean isOnline = get();
                    if (isOnline) {
                        showMessage("API连接测试成功！");
                    } else {
                        showError("API连接测试失败，请检查URL和网络连接");
                    }
                } catch (Exception e) {
                    showError("API连接测试异常: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void deleteCurrentApi() {
        ApiEndpoint selectedApi = (ApiEndpoint) sidebarPanel.getApiSelector().getSelectedItem();
        if (selectedApi == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                MessageFormat.format(
                        ResourceBundleManager.getString("dialog.delete_api_confirm"),
                        selectedApi.getName()
                ),
                ResourceBundleManager.getString("dialog.confirm_delete"),
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (databaseManager.deleteApiEndpoint(selectedApi.getId())) {
                apiEndpoints = databaseManager.getApiEndpoints();
                refreshApiSelector();
                if (!apiEndpoints.isEmpty()) {
                    sidebarPanel.getApiSelector().setSelectedIndex(0);
                    currentApi = apiEndpoints.get(0);
                } else {
                    currentApi = null;
                }
                showMessage(ResourceBundleManager.getString("message.api_delete_success"));
            } else {
                showError(ResourceBundleManager.getString("message.api_delete_failed"));
            }
        }
    }

    private void updateProgress() {
        if (currentMediaFile != null) {
            long currentTime = mediaPlayer.getCurrentTime();
            long duration = mediaPlayer.getDuration();

            if (duration > 0) {
                float progress = (float) currentTime / duration;
                controlPanel.getProgressSlider().setValue((int) (progress * 1000));

                controlPanel.getCurrentTimeLabel().setText(formatTime(currentTime));
                controlPanel.getDurationLabel().setText(formatTime(duration));
            }
        }
    }

    private void updateMediaInfo(MediaFile file) {
        playerPanel.getMediaTitle().setText(file.getName());

        String sizeMB = new DecimalFormat("#.##").format(file.getSize() / (1024.0 * 1024.0));
        String durationText = file.getDuration() != null ? formatTime(file.getDuration().longValue() * 1000) : "--";

        playerPanel.getMediaMeta().setText(String.format("%s | %s MB | %s",
                file.getType().toUpperCase(), sizeMB, durationText));
    }

    private void updateID3Info(ID3Info info) {
        if (info == null) {
            playerPanel.getId3Info().setText(ResourceBundleManager.getString("player.no_id3_info"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (info.getTitle() != null) sb.append("title: ").append(info.getTitle()).append("\n");
        if (info.getArtist() != null) sb.append("艺术家: ").append(info.getArtist()).append("\n");
        if (info.getAlbum() != null) sb.append("专辑: ").append(info.getAlbum()).append("\n");
        if (info.getYear() != null) sb.append("年份: ").append(info.getYear()).append("\n");
        if (info.getGenre() != null) sb.append("流派: ").append(info.getGenre());

        playerPanel.getId3Info().setText(sb.toString());
    }

    private void updateCoverDisplay(MediaFile file) {
        if ("audio".equals(file.getType())) {
            playerPanel.getCoverLabel().setText(ResourceBundleManager.getString("player.audio_icon"));
            playerPanel.getCoverLabel().setVisible(coverVisible);
        } else {
            playerPanel.getCoverLabel().setVisible(false);
        }
    }

    private void toggleFullscreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        if (gd.getFullScreenWindow() == null) {
            // 进入全屏
            dispose();
            setUndecorated(true);
            gd.setFullScreenWindow(this);
        } else {
            // 退出全屏
            gd.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            setVisible(true);
        }
    }

    private String formatTime(double milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return MessageFormat.format(
                ResourceBundleManager.getString("control.time_format"),
                minutes, String.format("%02d", seconds)
        );
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message,
                ResourceBundleManager.getString("dialog.info"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                ResourceBundleManager.getString("dialog.error"),
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dispose() {
        // 清理资源
        if (progressTimer != null) {
            progressTimer.stop();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        try {
            if (apiClient != null) {
                apiClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.dispose();
    }

    // 初始化sidebarPanel
    {
        sidebarPanel = new SidebarPanel();
    }
}