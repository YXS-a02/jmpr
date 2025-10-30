package yxs.a02.mper.service;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FFmpegAudioPlayer {
    private Clip audioClip;
    private boolean isPlaying = false;
    private long duration = 0;
    private long currentTime = 0;
    private Timer progressTimer;
    private FloatControl volumeControl;
    private int volume = 100;
    private File tempWavFile;
    private Process ffmpegProcess;

    public FFmpegAudioPlayer() {
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

        String fileName = audioFile.getName().toLowerCase();

        // 检查是否为WAV格式，如果是则直接播放
        if (fileName.endsWith(".wav")) {
            playWavDirectly(audioFile);
            return;
        }

        // 对于非WAV格式，使用ffmpeg转换
        try {
            // 创建临时WAV文件
            tempWavFile = File.createTempFile("ffmpeg_convert_", ".wav");
            tempWavFile.deleteOnExit();

            System.out.println("使用ffmpeg转换音频文件: " + fileName);

            // 构建ffmpeg命令
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", audioFile.getAbsolutePath(),
                    "-acodec", "pcm_s16le",       // PCM 16位
                    "-ac", "2",                   // 立体声
                    "-ar", "44100",               // 采样率44.1kHz
                    "-y",                         // 覆盖输出文件
                    tempWavFile.getAbsolutePath()
            );

            // 重定向错误流到标准输出以便查看进度
            pb.redirectErrorStream(true);

            ffmpegProcess = pb.start();

            // 读取ffmpeg输出
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(ffmpegProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("ffmpeg: " + line);
                    }
                } catch (IOException e) {
                    System.err.println("读取ffmpeg输出失败: " + e.getMessage());
                }
            });
            outputReader.start();

            // 等待转换完成
            int exitCode = ffmpegProcess.waitFor();
            if (exitCode != 0) {
                throw new Exception("ffmpeg转换失败，退出代码: " + exitCode);
            }

            System.out.println("ffmpeg转换完成，播放临时文件: " + tempWavFile.getAbsolutePath());

            // 播放转换后的WAV文件
            playWavDirectly(tempWavFile);

        } catch (Exception e) {
            // 清理临时文件
            if (tempWavFile != null && tempWavFile.exists()) {
                tempWavFile.delete();
            }
            throw new Exception("使用ffmpeg处理音频失败: " + e.getMessage());
        }
    }

    public void playWavDirectly(File wavFile) throws Exception {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile);

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

            System.out.println("开始播放音频，时长: " + duration + "ms");

        } catch (UnsupportedAudioFileException e) {
            throw new Exception("不支持的WAV音频格式");
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

        // 清理ffmpeg进程
        if (ffmpegProcess != null && ffmpegProcess.isAlive()) {
            ffmpegProcess.destroy();
        }

        // 清理临时文件
        if (tempWavFile != null && tempWavFile.exists()) {
            tempWavFile.delete();
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

    // 检查ffmpeg是否可用
    public static boolean isFFmpegAvailable() {
        try {
            Process process = new ProcessBuilder("ffmpeg", "-version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("ffmpeg不可用: " + e.getMessage());
            return false;
        }
    }
}