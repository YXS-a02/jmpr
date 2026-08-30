import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;

public class server {
    private static final String BASE_PATH = "./";
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/", exchange -> {
            // 设置CORS
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            try {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = parseQuery(query);
                String action = params.getOrDefault("action", "");
                String tag = params.get("tag");
                String id = params.get("id");
                
                switch (action) {
                    case "list":
                        handleList(exchange, tag);
                        break;
                    case "info":
                        handleInfo(exchange, id);
                        break;
                    case "src":
                        handleSrc(exchange, id);
                        break;
                    case "cover":
                        handleCover(exchange, id);
                        break;
                    default:
                        sendJsonError(exchange, 400, "未知操作");
                }
            } catch (Exception e) {
                // 忽略Broken pipe异常（客户端断开连接）
                if (!(e instanceof IOException && e.getMessage().contains("Broken pipe"))) {
                    e.printStackTrace();
                    try {
                        sendJsonError(exchange, 500, "服务器错误: " + e.getMessage());
                    } catch (IOException ignored) {}
                }
            }
        });
        
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server started on http://localhost:8080/");
        System.out.println("\nAPI Endpoints:");
        System.out.println("  /?action=list&tag={tag}  - 列出标签下的文件");
        System.out.println("  /?action=info&id={item}  - 获取文件信息");
        System.out.println("  /?action=src&id={item}   - 获取源文件");
        System.out.println("  /?action=cover&id={item} - 获取封面");
        System.out.println("\nWorking directory: " + Paths.get(BASE_PATH).toAbsolutePath());
    }
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null) return params;
        
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }
    // 1. action=list - 列出文件
    private static void handleList(HttpExchange exchange, String tag) throws IOException {
        Path dir = Paths.get(BASE_PATH);
        List<Map<String, Object>> files = new ArrayList<>();
        
        if (!Files.exists(dir)) {
            sendJsonError(exchange, 404, "目录不存在");
            return;
        }
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    String fileName = entry.getFileName().toString();
                    if (tag != null && !fileName.toLowerCase().contains(tag.toLowerCase())) {
                        continue;
                    }
                    
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("id", fileName);
                    fileInfo.put("name", fileName);
                    fileInfo.put("size", Files.size(entry));
                    fileInfo.put("modified", Files.getLastModifiedTime(entry).toMillis());
                    files.add(fileInfo);
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("files", files);
        result.put("count", files.size());
        if (tag != null) result.put("tag", tag);
        
        sendJsonResponse(exchange, 200, toJson(result));
    }
    // 2. action=info - 获取文件信息
    private static void handleInfo(HttpExchange exchange, String id) throws IOException {
        if (id == null || id.isEmpty()) {
            sendJsonError(exchange, 400, "缺少id参数");
            return;
        }
        
        String fileName = sanitizeFileName(id);
        Path filePath = Paths.get(BASE_PATH, fileName);
        
        if (!Files.exists(filePath)) {
            sendJsonError(exchange, 404, "文件不存在: " + fileName);
            return;
        }
        
        Map<String, Object> info = new HashMap<>();
        info.put("id", fileName);
        info.put("name", fileName);
        info.put("size", Files.size(filePath));
        info.put("modified", Files.getLastModifiedTime(filePath).toMillis());
        info.put("path", filePath.toAbsolutePath().toString());
        info.put("exists", true);
        
        String ext = getFileExtension(fileName);
        info.put("extension", ext);
        info.put("is_image", isImageFile(ext));
        info.put("is_video", isVideoFile(ext));
        info.put("is_audio", isAudioFile(ext));
        
        sendJsonResponse(exchange, 200, toJson(info));
    }
    // 3. action=src - 获取源文件（修复非法字符问题）
    private static void handleSrc(HttpExchange exchange, String id) throws IOException {
        if (id == null || id.isEmpty()) {
            sendJsonError(exchange, 400, "缺少id参数");
            return;
        }
        
        String fileName = sanitizeFileName(id);
        Path filePath = Paths.get(BASE_PATH, fileName);
        
        if (!Files.exists(filePath)) {
            sendJsonError(exchange, 404, "文件不存在: " + fileName);
            return;
        }
        
        String ext = getFileExtension(fileName);
        String mimeType = getMimeType(ext);
        
        // 修复：对文件名进行URL编码，避免非法字符
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(Files.size(filePath)));
        // Content-Disposition 使用RFC 5987编码
        exchange.getResponseHeaders().set("Content-Disposition", 
            "inline; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
        
        try {
            exchange.sendResponseHeaders(200, Files.size(filePath));
            Files.copy(filePath, exchange.getResponseBody());
        } catch (IOException e) {
            // 客户端断开连接，忽略错误
            if (!e.getMessage().contains("Broken pipe")) {
                throw e;
            }
        } finally {
            try {
                exchange.getResponseBody().close();
            } catch (IOException ignored) {}
        }
    }
    // 4. action=cover - 获取封面（修复Broken pipe）
    private static void handleCover(HttpExchange exchange, String id) throws IOException {
        if (id == null || id.isEmpty()) {
            sendJsonError(exchange, 400, "缺少id参数");
            return;
        }
        
        String fileName = sanitizeFileName(id);
        Path filePath = Paths.get(BASE_PATH, fileName);
        
        if (!Files.exists(filePath)) {
            sendJsonError(exchange, 404, "文件不存在: " + fileName);
            return;
        }
        
        String ext = getFileExtension(fileName);
        Path coverPath = null;
        
        if (isImageFile(ext)) {
            coverPath = filePath;
        } else {
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String[] coverExtensions = {"jpg", "jpeg", "png", "gif", "webp"};
            for (String coverExt : coverExtensions) {
                Path candidate = Paths.get(BASE_PATH, baseName + "_cover." + coverExt);
                if (Files.exists(candidate)) {
                    coverPath = candidate;
                    break;
                }
            }
            
            if (coverPath == null && (isVideoFile(ext) || isAudioFile(ext))) {
                coverPath = extractCoverWithFFmpeg(filePath, baseName);
            }
        }
        
        if (coverPath == null || !Files.exists(coverPath)) {
            sendDefaultCover(exchange);
            return;
        }
        
        String coverExt = getFileExtension(coverPath.toString());
        String mimeType = getMimeType(coverExt);
        
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(Files.size(coverPath)));
        exchange.getResponseHeaders().set("Cache-Control", "max-age=3600");
        
        try {
            exchange.sendResponseHeaders(200, Files.size(coverPath));
            Files.copy(coverPath, exchange.getResponseBody());
        } catch (IOException e) {
            if (!e.getMessage().contains("Broken pipe")) {
                throw e;
            }
        } finally {
            try {
                exchange.getResponseBody().close();
            } catch (IOException ignored) {}
        }
    }
    private static Path extractCoverWithFFmpeg(Path filePath, String baseName) {
        try {
            Path coverPath = Paths.get(BASE_PATH, baseName + "_cover.jpg");
            
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", 
                "-i", filePath.toString(),
                "-an",
                "-vcodec", "mjpeg",
                "-vframes", "1",
                "-y",
                coverPath.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            
            if (Files.exists(coverPath) && Files.size(coverPath) > 0) {
                return coverPath;
            }
        } catch (Exception e) {
            System.err.println("FFmpeg提取封面失败: " + e.getMessage());
        }
        return null;
    }
    // 修复：发送默认封面时正确处理异常
    private static void sendDefaultCover(HttpExchange exchange) throws IOException {
        String base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        byte[] data = Base64.getDecoder().decode(base64);
        
        exchange.getResponseHeaders().set("Content-Type", "image/png");
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(data.length));
        exchange.getResponseHeaders().set("Cache-Control", "max-age=86400");
        
        try {
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
                os.flush();
            }
        } catch (IOException e) {
            // 客户端断开连接，忽略
            if (!e.getMessage().contains("Broken pipe")) {
                throw e;
            }
        }
    }
    private static void sendJsonResponse(HttpExchange exchange, int code, String json) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(data.length));
        
        try {
            exchange.sendResponseHeaders(code, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
                os.flush();
            }
        } catch (IOException e) {
            if (!e.getMessage().contains("Broken pipe")) {
                throw e;
            }
        }
    }
    private static void sendJsonError(HttpExchange exchange, int code, String message) throws IOException {
        String json = String.format("{\"error\":\"%s\",\"code\":%d}", message, code);
        sendJsonResponse(exchange, code, json);
    }
    private static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeJson((String) obj) + "\"";
        if (obj instanceof Number) return obj.toString();
        if (obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(entry.getKey().toString())).append("\":")
                  .append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escapeJson(obj.toString()) + "\"";
    }
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    private static String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(idx + 1).toLowerCase() : "";
    }
    private static boolean isImageFile(String ext) {
        return Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg").contains(ext);
    }
    private static boolean isVideoFile(String ext) {
        return Arrays.asList("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v").contains(ext);
    }
    private static boolean isAudioFile(String ext) {
        return Arrays.asList("mp3", "wav", "flac", "m4a", "aac", "ogg", "wma").contains(ext);
    }
    private static String getMimeType(String ext) {
        Map<String, String> mimeMap = new HashMap<>();
        mimeMap.put("jpg", "image/jpeg");
        mimeMap.put("jpeg", "image/jpeg");
        mimeMap.put("png", "image/png");
        mimeMap.put("gif", "image/gif");
        mimeMap.put("webp", "image/webp");
        mimeMap.put("svg", "image/svg+xml");
        mimeMap.put("mp4", "video/mp4");
        mimeMap.put("webm", "video/webm");
        mimeMap.put("avi", "video/x-msvideo");
        mimeMap.put("mkv", "video/x-matroska");
        mimeMap.put("mov", "video/quicktime");
        mimeMap.put("mp3", "audio/mpeg");
        mimeMap.put("wav", "audio/wav");
        mimeMap.put("flac", "audio/flac");
        mimeMap.put("m4a", "audio/mp4");
        mimeMap.put("aac", "audio/aac");
        mimeMap.put("ogg", "audio/ogg");
        mimeMap.put("txt", "text/plain");
        mimeMap.put("html", "text/html");
        mimeMap.put("css", "text/css");
        mimeMap.put("js", "application/javascript");
        mimeMap.put("json", "application/json");
        mimeMap.put("xml", "application/xml");
        mimeMap.put("pdf", "application/pdf");
        mimeMap.put("zip", "application/zip");
        return mimeMap.getOrDefault(ext, "application/octet-stream");
    }
    private static String sanitizeFileName(String fileName) {
        // 只取文件名部分，防止路径遍历
        String name = Paths.get(fileName).getFileName().toString();
        // 移除可能导致问题的字符
        return name.replaceAll("[<>\"|?*]", "_");
    }
}