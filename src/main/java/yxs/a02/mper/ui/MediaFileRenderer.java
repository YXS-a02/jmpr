package yxs.a02.mper.ui;

import yxs.a02.mper.model.MediaFile;
import yxs.a02.mper.util.ResourceBundleManager;

import javax.swing.*;
import java.awt.*;

public class MediaFileRenderer extends DefaultListCellRenderer {
    private static final Color PRIMARY_COLOR = new Color(0, 255, 157);
    private static final Color TEXT_COLOR = Color.WHITE;

    private MediaFile currentMediaFile;

    public void setCurrentMediaFile(MediaFile file) {
        this.currentMediaFile = file;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof MediaFile) {
            MediaFile file = (MediaFile) value;
            String icon = "audio".equals(file.getType()) ?
                    ResourceBundleManager.getString("player.audio_icon") :
                    ResourceBundleManager.getString("player.video_icon");
            setText(icon + " " + file.getName());

            // 如果是当前播放的文件，加粗显示
            if (file == currentMediaFile) {
                setFont(getFont().deriveFont(Font.BOLD));
                setForeground(PRIMARY_COLOR);
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
                setForeground(TEXT_COLOR);
            }
        }

        return this;
    }
}