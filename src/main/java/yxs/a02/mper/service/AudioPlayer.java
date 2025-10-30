package yxs.a02.mper.service;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class AudioPlayer {
    private Clip audioClip;
    private boolean isPlaying = false;
    private long duration = 0;
    private long currentTime = 0;
    private Timer progressTimer;
    private FloatControl volumeControl;
    private int volume = 100;

    public AudioPlayer() {
        progressTimer = new Timer(100, e -> {
            if (audioClip != null && isPlaying) {
                currentTime = audioClip.getMicrosecondPosition() / 1000;
                if (currentTime >= duration) {
                    stop();
                }
            }
        });
    }

    public void play(String filePath) throws Exception {
        stop(); // 停止当前播放

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }

        // 检查文件格式并尝试播放
        String fileName = audioFile.getName().toLowerCase();

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            // 获取支持的音频格式
            AudioFormat format = audioStream.getFormat();
            System.out.println("音频格式: " + format);

            // 如果格式不支持，尝试转换
            if (!isFormatSupported(format)) {
                System.out.println("格式不支持，尝试转换...");
                audioStream = convertAudioFormat(audioStream);
            }

            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);

            // 获取音量控制
            if (audioClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(volume); // 应用当前音量
            }

            duration = audioClip.getMicrosecondLength() / 1000;
            currentTime = 0;

            audioClip.start();
            isPlaying = true;
            progressTimer.start();

            System.out.println("开始播放音频: " + filePath + ", 时长: " + duration + "ms");

        } catch (UnsupportedAudioFileException e) {
            throw new Exception("不支持的音频格式: " + fileName +
                    "\n支持格式: WAV, AIFF, AU, 某些MP3(需要额外编解码器)");
        } catch (LineUnavailableException e) {
            throw new Exception("音频线路不可用: " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("读取文件失败: " + e.getMessage());
        }
    }

    private boolean isFormatSupported(AudioFormat format) {
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        return AudioSystem.isLineSupported(info);
    }

    private AudioInputStream convertAudioFormat(AudioInputStream audioStream)
            throws Exception {
        AudioFormat sourceFormat = audioStream.getFormat();

        // 转换为PCM格式
        AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(),
                16,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2,
                sourceFormat.getSampleRate(),
                false
        );

        return AudioSystem.getAudioInputStream(targetFormat, audioStream);
    }

    public void pause() {
        if (audioClip != null && isPlaying) {
            audioClip.stop();
            isPlaying = false;
            progressTimer.stop();
        }
    }

    public void resume() {
        if (audioClip != null && !isPlaying) {
            audioClip.start();
            isPlaying = true;
            progressTimer.start();
        }
    }

    public void stop() {
        if (audioClip != null) {
            audioClip.stop();
            audioClip.close();
            isPlaying = false;
            currentTime = 0;
            progressTimer.stop();
        }
    }

    public void setVolume(int volume) {
        this.volume = volume;
        if (volumeControl != null) {
            // 将音量从0-100转换为分贝值（-80到0）
            float dB = (float) (Math.log(volume / 100.0) / Math.log(10.0) * 20.0);
            dB = Math.max(volumeControl.getMinimum(), Math.min(volumeControl.getMaximum(), dB));
            volumeControl.setValue(dB);
        }
    }

    public void setPosition(float position) {
        if (audioClip != null && duration > 0) {
            long newTime = (long) (duration * position);
            audioClip.setMicrosecondPosition(newTime * 1000);
            currentTime = newTime;
        }
    }

    public boolean isPlaying() {
        return isPlaying && audioClip != null && audioClip.isRunning();
    }

    public long getDuration() {
        return duration;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void release() {
        stop();
        if (audioClip != null) {
            audioClip.close();
        }
        progressTimer.stop();
    }
}