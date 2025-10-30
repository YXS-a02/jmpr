package yxs.a02.mper.service;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MediaPlayer {
    private FFmpegAudioPlayer audioPlayer;
    private boolean isPlaying = false;
    private float speed = 1.0f;
    private String currentMediaUrl;
    private boolean ffmpegAvailable;
    private File tempMediaFile;

    public MediaPlayer() {
        audioPlayer = new FFmpegAudioPlayer();
        ffmpegAvailable = FFmpegAudioPlayer.isFFmpegAvailable();
        System.out.println("FFmpeg可用: " + ffmpegAvailable);

        if (!ffmpegAvailable) {
            System.err.println("警告: FFmpeg不可用，将无法播放大多数音频格式");
            System.err.println("请安装FFmpeg并确保它在系统PATH中");
        }
    }

    public void play(String mediaUrl) throws Exception {
        stop(); // 先停止当前播放

        // 检查是否为HTTP URL
        if (mediaUrl.startsWith("http")) {
            System.out.println("检测到HTTP URL，尝试流媒体播放: " + mediaUrl);
            playHttpStream(mediaUrl);
        } else {
            // 本地文件处理
            File mediaFile = new File(mediaUrl);
            if (!mediaFile.exists()) {
                throw new Exception("文件不存在: " + mediaUrl);
            }

            // 检查文件类型
            String fileName = mediaFile.getName().toLowerCase();
            if (isSupportedAudioFile(fileName)) {
                if (ffmpegAvailable) {
                    // 使用FFmpeg音频播放器播放
                    System.out.println("使用FFmpeg播放音频文件: " + fileName);
                    audioPlayer.play(mediaUrl);
                    isPlaying = true;
                    currentMediaUrl = mediaUrl;
                } else {
                    // FFmpeg不可用时，只支持WAV格式
                    if (fileName.endsWith(".wav")) {
                        System.out.println("FFmpeg不可用，尝试直接播放WAV文件: " + fileName);
                        audioPlayer.playWavDirectly(mediaFile);
                        isPlaying = true;
                        currentMediaUrl = mediaUrl;
                    } else {
                        throw new Exception("FFmpeg不可用，无法播放 " + fileName + " 格式\n请安装FFmpeg或使用WAV格式文件");
                    }
                }
            } else if (isSupportedVideoFile(fileName)) {
                // 视频文件，提示不支持
                throw new Exception("视频播放功能暂未实现。当前版本仅支持音频文件播放。");
            } else {
                throw new Exception("不支持的文件格式: " + fileName);
            }
        }
    }

    // 播放HTTP流媒体
    private void playHttpStream(String httpUrl) throws Exception {
        try {
            // 创建临时文件
            tempMediaFile = File.createTempFile("stream_", ".tmp");
            tempMediaFile.deleteOnExit();

            System.out.println("下载流媒体到临时文件: " + tempMediaFile.getAbsolutePath());

            // 下载HTTP流到临时文件
            downloadHttpStream(httpUrl, tempMediaFile);

            // 播放临时文件
            String fileName = tempMediaFile.getName().toLowerCase();
            if (isSupportedAudioFile(fileName) || isSupportedAudioFile(httpUrl)) {
                if (ffmpegAvailable) {
                    System.out.println("使用FFmpeg播放流媒体文件");
                    audioPlayer.play(tempMediaFile.getAbsolutePath());
                    isPlaying = true;
                    currentMediaUrl = httpUrl;
                } else {
                    throw new Exception("FFmpeg不可用，无法播放流媒体");
                }
            } else {
                throw new Exception("不支持的流媒体格式");
            }

        } catch (Exception e) {
            // 清理临时文件
            if (tempMediaFile != null && tempMediaFile.exists()) {
                tempMediaFile.delete();
            }
            throw new Exception("播放HTTP流媒体失败: " + e.getMessage());
        }
    }

    // 下载HTTP流到临时文件
    private void downloadHttpStream(String httpUrl, File outputFile) throws Exception {
        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "MediaPlayer/1.0");

            int responseCode = connection.getResponseCode();
            System.out.println("HTTP响应码: " + responseCode);

            if (responseCode == 200) {
                inputStream = connection.getInputStream();

                // 获取内容长度（如果可用）
                String contentLength = connection.getHeaderField("Content-Length");
                if (contentLength != null) {
                    long length = Long.parseLong(contentLength);
                    System.out.println("流媒体大小: " + length + " 字节 (" + (length / 1024 / 1024) + " MB)");
                }

                // 下载到临时文件
                Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("下载完成，文件大小: " + outputFile.length() + " 字节");

            } else {
                throw new Exception("HTTP错误: " + responseCode + " - " + connection.getResponseMessage());
            }

        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (Exception e) {}
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isSupportedAudioFile(String fileName) {
        // 使用ffmpeg后支持几乎所有音频格式
        return fileName.endsWith(".wav") || fileName.endsWith(".mp3") ||
                fileName.endsWith(".aiff") || fileName.endsWith(".au") ||
                fileName.endsWith(".aif") || fileName.endsWith(".snd") ||
                fileName.endsWith(".flac") || fileName.endsWith(".m4a") ||
                fileName.endsWith(".aac") || fileName.endsWith(".ogg") ||
                fileName.endsWith(".wma") || fileName.endsWith(".ape") ||
                fileName.contains(".mp3"); // 对于HTTP URL，检查是否包含.mp3
    }

    private boolean isSupportedVideoFile(String fileName) {
        return fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
                fileName.endsWith(".mkv") || fileName.endsWith(".mov") ||
                fileName.endsWith(".wmv") || fileName.endsWith(".flv") ||
                fileName.endsWith(".webm") || fileName.endsWith(".m4v");
    }

    public void pause() {
        if (isPlaying) {
            audioPlayer.pause();
            isPlaying = false;
        }
    }

    public void resume() {
        if (!isPlaying && currentMediaUrl != null) {
            audioPlayer.resume();
            isPlaying = true;
        }
    }

    public void stop() {
        audioPlayer.stop();
        isPlaying = false;
        currentMediaUrl = null;

        // 清理临时文件
        if (tempMediaFile != null && tempMediaFile.exists()) {
            tempMediaFile.delete();
            tempMediaFile = null;
        }
    }

    public void setVolume(int volume) {
        audioPlayer.setVolume(volume);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        System.out.println("播放速度设置为: " + speed + "x");
        // 注意：当前版本不支持改变播放速度
    }

    public void setPosition(float position) {
        audioPlayer.setPosition(position);
    }

    public boolean isPlaying() {
        return isPlaying && audioPlayer.isPlaying();
    }

    public long getDuration() {
        return audioPlayer.getDuration();
    }

    public long getCurrentTime() {
        return audioPlayer.getCurrentTime();
    }

    public void release() {
        stop();
        audioPlayer.release();
    }

    public boolean isFfmpegAvailable() {
        return ffmpegAvailable;
    }
}