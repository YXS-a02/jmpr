package yxs.a02.mper.ui.components;

import yxs.a02.mper.util.ResourceBundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PlayerPanel extends JPanel {
    private static final Color BACKGROUND_DARK = new Color(26, 26, 26);
    private static final Color BACKGROUND_MEDIUM = new Color(37, 37, 37);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(204, 204, 204);

    private JLabel mediaTitle;
    private JLabel mediaMeta;
    private JTextArea id3Info;
    private JLabel coverLabel;

    public PlayerPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_DARK);

        // 视频显示区域
        JPanel displayPanel = createDisplayPanel();

        // 媒体信息面板
        JPanel infoPanel = createInfoPanel();

        add(displayPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setPreferredSize(new Dimension(700, 400));
        panel.setBorder(BorderFactory.createLineBorder(new Color(51, 51, 51), 2));

        // 封面层（用于音频文件）
        coverLabel = new JLabel(ResourceBundleManager.getString("player.audio_icon"), SwingConstants.CENTER);
        coverLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        coverLabel.setForeground(new Color(102, 102, 102));
        coverLabel.setOpaque(true);
        coverLabel.setBackground(Color.BLACK);
        panel.add(coverLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(BACKGROUND_MEDIUM);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout(0, 10));
        textPanel.setBackground(BACKGROUND_MEDIUM);

        mediaTitle = new JLabel(ResourceBundleManager.getString("player.title"));
        mediaTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mediaTitle.setForeground(TEXT_COLOR);

        mediaMeta = new JLabel(ResourceBundleManager.getString("player.meta"));
        mediaMeta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mediaMeta.setForeground(TEXT_SECONDARY);

        id3Info = new JTextArea();
        id3Info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        id3Info.setForeground(TEXT_SECONDARY);
        id3Info.setBackground(BACKGROUND_MEDIUM);
        id3Info.setEditable(false);
        id3Info.setBorder(new EmptyBorder(10, 0, 0, 0));
        id3Info.setLineWrap(true);
        id3Info.setWrapStyleWord(true);

        textPanel.add(mediaTitle, BorderLayout.NORTH);
        textPanel.add(mediaMeta, BorderLayout.CENTER);
        textPanel.add(new JScrollPane(id3Info), BorderLayout.SOUTH);

        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    // Getters for components that need to be accessed from Main
    public JLabel getMediaTitle() { return mediaTitle; }
    public JLabel getMediaMeta() { return mediaMeta; }
    public JTextArea getId3Info() { return id3Info; }
    public JLabel getCoverLabel() { return coverLabel; }
}