package fur.yxs.mper.core;
import java.awt.GraphicsEnvironment;

public class EnvironmentDetector {
    /**
     * 环境类型枚举
     */
    public enum Environment {
        DESKTOP_GUI,      // 桌面图形环境
        TERMINAL_ONLY,    // 纯终端
        TERMUX,           // Termux
        SSH_SESSION,      // SSH 会话
        HEADLESS          // 无头服务器
    }
    public static Environment detectEnvironment() {
        // 检测 Termux
        if (isTermux()) {
            return Environment.TERMUX;
        }
        // 检测 SSH 会话
        if (isSSHSession()) {
            return Environment.SSH_SESSION;
        }
        // 检测图形环境
        if (!isHeadless() && hasDisplay()) {
            return Environment.DESKTOP_GUI;
        }
        // 检测其他终端
        if (isHeadless() || !hasDisplay()) {
            return Environment.HEADLESS;
        }
        return Environment.TERMINAL_ONLY;
    }
    private static boolean isTermux() {
        String termuxVersion = System.getenv("TERMUX_VERSION");
        String prefix = System.getenv("PREFIX");
        if (termuxVersion != null && !termuxVersion.isEmpty()) {
            return true;
        }
        if (prefix != null && prefix.contains("com.termux")) {
            return true;
        }
        // 检查 /data/data/com.termux 路径
        String javaHome = System.getProperty("java.home");
        if (javaHome != null && javaHome.contains("com.termux")) {
            return true;
        }
        return false;
    }
    private static boolean isSSHSession() {
        // SSH 相关的环境变量
        String[] sshEnvVars = {
            "SSH_CLIENT", "SSH_TTY", "SSH_CONNECTION",
            "SSH_SESSION_ID", "SSH_ORIGINAL_COMMAND"
        };
        for (String var : sshEnvVars) {
            if (System.getenv(var) != null) {
                return true;
            }
        }
        // 检查终端类型
        String term = System.getenv("TERM");
        if (term != null && term.contains("xterm") && System.console() != null) {
            // 进一步检查是否有图形环境
            if (System.getenv("DISPLAY") == null) {
                return true;
            }
        }
        return false;
    }
    private static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }
    private static boolean hasDisplay() {
        // Windows 总是有图形（除非是 Server Core）
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return true;
        }
        // Linux/Unix 检查 DISPLAY
        String display = System.getenv("DISPLAY");
        if (display != null && !display.isEmpty() && !display.equals(":0")) {
            return true;
        }
        return false;
    }
    /**
     * 是否有可用的控制台（终端）
     */
    public static boolean hasConsole() {
        return System.console() != null;
    }
}