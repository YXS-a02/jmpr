package yxs.a02.mper.ui.components;

import yxs.a02.mper.model.ApiEndpoint;
import yxs.a02.mper.model.MediaFile;
import yxs.a02.mper.util.ResourceBundleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SidebarPanel extends JPanel {
    private static final Color BACKGROUND_DARK = new Color(26, 26, 26);
    private static final Color BACKGROUND_MEDIUM = new Color(37, 37, 37);
    private static final Color BACKGROUND_LIGHT = new Color(51, 51, 51);
    private static final Color PRIMARY_COLOR = new Color(0, 255, 157);
    private static final Color TEXT_COLOR = Color.WHITE;

    private JComboBox<ApiEndpoint> apiSelector;
    private JButton refreshApiBtn;
    private JButton addApiBtn;
    private JButton deleteApiBtn;
    private JList<MediaFile> fileList;
    private DefaultListModel<MediaFile> fileListModel;

    public SidebarPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_DARK);
        setPreferredSize(new Dimension(300, 600));

        // API管理
        JPanel apiPanel = createApiManagementPanel();

        // 文件列表
        JPanel fileListPanel = createFileListPanel();

        add(apiPanel, BorderLayout.NORTH);
        add(fileListPanel, BorderLayout.CENTER);
    }

    private JPanel createApiManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_MEDIUM);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(ResourceBundleManager.getString("sidebar.api_management"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);

        // API选择器
        apiSelector = new JComboBox<>();
        apiSelector.setBackground(BACKGROUND_LIGHT);
        apiSelector.setForeground(TEXT_COLOR);

        // API按钮
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        buttonPanel.setBackground(BACKGROUND_MEDIUM);

        refreshApiBtn = createStyledButton("↻", ResourceBundleManager.getString("sidebar.refresh_list"));
        deleteApiBtn = createStyledButton("×", ResourceBundleManager.getString("sidebar.delete_api"));
        addApiBtn = createStyledButton("+", ResourceBundleManager.getString("sidebar.add_api"));
        JButton editApiBtn = createStyledButton("✎", ResourceBundleManager.getString("sidebar.edit_api"));

        buttonPanel.add(refreshApiBtn);
        buttonPanel.add(deleteApiBtn);
        buttonPanel.add(addApiBtn);
        buttonPanel.add(editApiBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(apiSelector, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFileListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_DARK);

        JLabel titleLabel = new JLabel(ResourceBundleManager.getString("sidebar.media_list"));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setBackground(BACKGROUND_LIGHT);
        fileList.setForeground(TEXT_COLOR);
        fileList.setSelectionBackground(PRIMARY_COLOR);
        fileList.setSelectionForeground(Color.BLACK);
        fileList.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(280, 400));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

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
    public JComboBox<ApiEndpoint> getApiSelector() { return apiSelector; }
    public JButton getRefreshApiBtn() { return refreshApiBtn; }
    public JButton getAddApiBtn() { return addApiBtn; }
    public JButton getDeleteApiBtn() { return deleteApiBtn; }
    public JList<MediaFile> getFileList() { return fileList; }
    public DefaultListModel<MediaFile> getFileListModel() { return fileListModel; }
}