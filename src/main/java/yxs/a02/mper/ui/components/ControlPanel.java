package yxs.a02.mper.ui.components;

import yxs.a02.mper.util.ResourceBundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ControlPanel extends JPanel {
    private static final Color BACKGROUND_MEDIUM = new Color(37, 37, 37);
    private static final Color BACKGROUND_LIGHT = new Color(51, 51, 51);
    private static final Color PRIMARY_COLOR = new Color(0, 255, 157);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(204, 204, 204);

    private JSlider progressSlider;
    private JSlider volumeSlider;
    private JComboBox<String> speedComboBox;
    private JButton playPauseBtn;
    private JButton openFileBtn;
    private JButton prevBtn;
    private JButton nextBtn;
    private JLabel currentTimeLabel;
    private JLabel durationLabel;

    public ControlPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_MEDIUM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 进度条
        progressSlider = new JSlider(0, 1000, 0);
        progressSlider.setBackground(BACKGROUND_MEDIUM);
        progressSlider.setForeground(PRIMARY_COLOR);

        // 按钮容器
        JPanel buttonPanel = createButtonPanel();

        add(progressSlider, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(BACKGROUND_MEDIUM);

        prevBtn = createStyledButton("⏮", ResourceBundleManager.getString("control.previous"));
        playPauseBtn = createStyledButton("▶", ResourceBundleManager.getString("control.play"));
        JButton stopBtn = createStyledButton("⏹", ResourceBundleManager.getString("control.stop"));
        nextBtn = createStyledButton("⏭", ResourceBundleManager.getString("control.next"));
        JButton toggleCoverBtn = createStyledButton("🖼️", ResourceBundleManager.getString("control.toggle_cover"));
        openFileBtn = createStyledButton("📁", ResourceBundleManager.getString("control.open_file"));

        // 时间显示
        currentTimeLabel = new JLabel("0:00");
        currentTimeLabel.setForeground(TEXT_SECONDARY);
        currentTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        durationLabel = new JLabel("0:00");
        durationLabel.setForeground(TEXT_SECONDARY);
        durationLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        timePanel.setBackground(BACKGROUND_MEDIUM);
        timePanel.add(currentTimeLabel);
        timePanel.add(new JLabel(" / "));
        timePanel.add(durationLabel);

        // 音量控制
        volumeSlider = new JSlider(0, 100, 80);
        volumeSlider.setPreferredSize(new Dimension(80, 20));
        volumeSlider.setBackground(BACKGROUND_MEDIUM);

        // 播放速度
        speedComboBox = new JComboBox<>(new String[]{"0.5x", "1.0x", "1.5x", "2.0x"});
        speedComboBox.setSelectedIndex(1);
        speedComboBox.setBackground(BACKGROUND_LIGHT);
        speedComboBox.setForeground(TEXT_COLOR);

        panel.add(prevBtn);
        panel.add(playPauseBtn);
        panel.add(stopBtn);
        panel.add(nextBtn);
        panel.add(toggleCoverBtn);
        panel.add(timePanel);
        panel.add(new JLabel(" 🔊 "));
        panel.add(volumeSlider);
        panel.add(new JLabel(" " + ResourceBundleManager.getString("control.speed") + ":"));
        panel.add(speedComboBox);
        panel.add(openFileBtn);

        return panel;
    }

    private JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        button.setBackground(BACKGROUND_LIGHT);
        button.setForeground(TEXT_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(70, 70, 70));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(BACKGROUND_LIGHT);
            }
        });

        return button;
    }

    // Getters for components that need to be accessed from Main
    public JSlider getProgressSlider() { return progressSlider; }
    public JSlider getVolumeSlider() { return volumeSlider; }
    public JComboBox<String> getSpeedComboBox() { return speedComboBox; }
    public JButton getPlayPauseBtn() { return playPauseBtn; }
    public JButton getOpenFileBtn() { return openFileBtn; }
    public JButton getPrevBtn() { return prevBtn; }
    public JButton getNextBtn() { return nextBtn; }
    public JLabel getCurrentTimeLabel() { return currentTimeLabel; }
    public JLabel getDurationLabel() { return durationLabel; }

    public JButton getStopButton() {
        // 返回停止按钮（在按钮面板的第3个位置）
        return (JButton) ((JPanel) playPauseBtn.getParent()).getComponent(2);
    }
}