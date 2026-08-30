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
    
    // 只支持多媒体格式
    private static final Set<String> VIDEO_FORMATS = new HashSet<>(Arrays.asList(
        "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "3gp"
    ));
    private static final Set<String> AUDIO_FORMATS = new HashSet<>(Arrays.asList(
        "mp3", "wav", "flac", "m4a", "aac", "ogg", "wma", "opus"
    ));
    private static final Set<String> MEDIA_FORMATS = new HashSet<>();
    static {
        MEDIA_FORMATS.addAll(VIDEO_FORMATS);
        MEDIA_FORMATS.addAll(AUDIO_FORMATS);
    }
    
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("avi", "video/x-msvideo");
        MIME_TYPES.put("mkv", "video/x-matroska");
        MIME_TYPES.put("mov", "video/quicktime");
        MIME_TYPES.put("wmv", "video/x-ms-wmv");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("m4v", "video/x-m4v");
        MIME_TYPES.put("3gp", "video/3gpp");
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("wav", "audio/wav");
        MIME_TYPES.put("flac", "audio/flac");
        MIME_TYPES.put("m4a", "audio/mp4");
        MIME_TYPES.put("aac", "audio/aac");
        MIME_TYPES.put("ogg", "audio/ogg");
        MIME_TYPES.put("wma", "audio/x-ms-wma");
        MIME_TYPES.put("opus", "audio/opus");
    }
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        
        server.createContext("/", exchange -> {
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
        System.out.println("🎬 Media Server started on http://localhost:8080/");
        System.out.println("\n📁 Working directory: " + Paths.get(BASE_PATH).toAbsolutePath());
        System.out.println("\n🎯 Supported formats:");
        System.out.println("  Video: " + String.join(", ", VIDEO_FORMATS));
        System.out.println("  Audio: " + String.join(", ", AUDIO_FORMATS));
        System.out.println("\n📋 API Endpoints:");
        System.out.println("  /?action=list&tag={tag}  - List media files");
        System.out.println("  /?action=info&id={item}  - Get file info");
        System.out.println("  /?action=src&id={item}   - Get media file");
        System.out.println("  /?action=cover&id={item} - Get cover image");
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
    
    // 1. 只列出多媒体文件
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
                    String ext = getFileExtension(fileName);
                    
                    // 只处理多媒体文件
                    if (!MEDIA_FORMATS.contains(ext)) {
                        continue;
                    }
                    
                    // 标签过滤
                    if (tag != null && !fileName.toLowerCase().contains(tag.toLowerCase())) {
                        continue;
                    }
                    
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("id", fileName);
                    fileInfo.put("name", fileName);
                    fileInfo.put("size", Files.size(entry));
                    fileInfo.put("modified", Files.getLastModifiedTime(entry).toMillis());
                    fileInfo.put("type", VIDEO_FORMATS.contains(ext) ? "video" : "audio");
                    fileInfo.put("extension", ext);
                    files.add(fileInfo);
                }
            }
        }
        
        // 按修改时间排序（最新的在前）
        files.sort((a, b) -> Long.compare(
            (long) b.get("modified"),
            (long) a.get("modified")
        ));
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", files);
        result.put("count", files.size());
        if (tag != null) result.put("tag", tag);
        
        sendJsonResponse(exchange, 200, toJson(result));
    }
    
    // 2. 获取文件信息
    private static void handleInfo(HttpExchange exchange, String id) throws IOException {
        if (id == null || id.isEmpty()) {
            sendJsonError(exchange, 400, "缺少id参数");
            return;
        }
        
        String fileName = sanitizeFileName(id);
        Path filePath = Paths.get(BASE_PATH, fileName);
        
        if (!Files.exists(filePath)) {
            sendJsonError(exchange, 404, "文件不存在");
            return;
        }
        
        String ext = getFileExtension(fileName);
        if (!MEDIA_FORMATS.contains(ext)) {
            sendJsonError(exchange, 400, "不支持的文件格式");
            return;
        }
        
        Map<String, Object> info = new HashMap<>();
        info.put("id", fileName);
        info.put("name", fileName);
        info.put("size", Files.size(filePath));
        info.put("modified", Files.getLastModifiedTime(filePath).toMillis());
        info.put("type", VIDEO_FORMATS.contains(ext) ? "video" : "audio");
        info.put("extension", ext);
        info.put("mime_type", MIME_TYPES.getOrDefault(ext, "application/octet-stream"));
        
        // 尝试获取媒体时长（需要ffprobe）
        long duration = getMediaDuration(filePath);
        if (duration > 0) {
            info.put("duration", duration);
        }
        
        sendJsonResponse(exchange, 200, toJson(info));
    }
    
    // 3. 获取媒体文件（支持断点续传）
    private static void handleSrc(HttpExchange exchange, String id) throws IOException {
        if (id == null || id.isEmpty()) {
            sendJsonError(exchange, 400, "缺少id参数");
            return;
        }
        
        String fileName = sanitizeFileName(id);
        Path filePath = Paths.get(BASE_PATH, fileName);
        
        if (!Files.exists(filePath)) {
            sendJsonError(exchange, 404, "文件不存在");
            return;
        }
        
        String ext = getFileExtension(fileName);
        if (!MEDIA_FORMATS.contains(ext)) {
            sendJsonError(exchange, 400, "不支持的文件格式");
            return;
        }
        
        String mimeType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
        exchange.getResponseHeaders().set("Content-Disposition", 
            "inline; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        
        // 支持断点续传
        String range = exchange.getRequestHeaders().getFirst("Range");
        long fileSize = Files.size(filePath);
        
        if (range != null) {
            long[] rangeBytes = parseRange(range, fileSize);
            if (rangeBytes != null) {
                long start = rangeBytes[0];
                long end = rangeBytes[1];
                long length = end - start + 1;
                
                exchange.getResponseHeaders().set("Content-Range", 
                    String.format("bytes %d-%d/%d", start, end, fileSize));
                exchange.getResponseHeaders().set("Content-Length", String.valueOf(length));
                exchange.sendResponseHeaders(206, length);
                
                try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r");
                     OutputStream os = exchange.getResponseBody()) {
                    raf.seek(start);
                    byte[] buffer = new byte[8192];
                    long remaining = length;
                    while (remaining > 0) {
                        int read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (read == -1) break;
                        os.write(buffer, 0, read);
                        remaining -= read;
                    }
                }
                return;
            }
        }
        
        // 完整文件
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(fileSize));
        try {
            exchange.sendResponseHeaders(200, fileSize);
            Files.copy(filePath, exchange.getResponseBody());
        } catch (IOException e) {
            if (!e.getMessage().contains("Broken pipe")) throw e;
        } finally {
            try { exchange.getResponseBody().close(); } catch (IOException ignored) {}
        }
    }
    
    // 4. 获取封面
    private static void handleCover(HttpExchange exchange, String id) throws IOException {
        if (id == null || id.isEmpty()) {
            sendJsonError(exchange, 400, "缺少id参数");
            return;
        }
        
        String fileName = sanitizeFileName(id);
        Path filePath = Paths.get(BASE_PATH, fileName);
        
        if (!Files.exists(filePath)) {
            sendJsonError(exchange, 404, "文件不存在");
            return;
        }
        
        String ext = getFileExtension(fileName);
        if (!MEDIA_FORMATS.contains(ext)) {
            sendJsonError(exchange, 400, "不支持的文件格式");
            return;
        }
        
        // 尝试获取封面
        Path coverPath = findCover(filePath);
        
        if (coverPath == null) {
            sendDefaultCover(exchange);
            return;
        }
        
        String coverExt = getFileExtension(coverPath.toString());
        String mimeType = MIME_TYPES.getOrDefault(coverExt, "image/jpeg");
        
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Content-Length", String.valueOf(Files.size(coverPath)));
        exchange.getResponseHeaders().set("Cache-Control", "max-age=86400");
        
        try {
            exchange.sendResponseHeaders(200, Files.size(coverPath));
            Files.copy(coverPath, exchange.getResponseBody());
        } catch (IOException e) {
            if (!e.getMessage().contains("Broken pipe")) throw e;
        } finally {
            try { exchange.getResponseBody().close(); } catch (IOException ignored) {}
        }
    }
    
    // 查找封面：优先找同名封面，然后尝试ffmpeg提取
    private static Path findCover(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        // 1. 查找同名封面文件
        String[] coverExtensions = {"jpg", "jpeg", "png", "gif", "webp"};
        for (String coverExt : coverExtensions) {
            Path candidate = Paths.get(BASE_PATH, baseName + "_cover." + coverExt);
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        
        // 2. 用ffmpeg提取（视频/音频）
        String ext = getFileExtension(fileName);
        if (VIDEO_FORMATS.contains(ext) || AUDIO_FORMATS.contains(ext)) {
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
            } catch (Exception ignored) {}
        }
        
        return null;
    }
    
    // 获取媒体时长
    private static long getMediaDuration(Path filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "quiet",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                filePath.toString()
            );
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                process.waitFor(1, java.util.concurrent.TimeUnit.SECONDS);
                if (line != null) {
                    return (long) Double.parseDouble(line.trim());
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }
    
    // 解析Range头
    private static long[] parseRange(String range, long fileSize) {
        if (!range.startsWith("bytes=")) return null;
        String parts = range.substring(6);
        String[] rangeParts = parts.split("-");
        try {
            long start = Long.parseLong(rangeParts[0]);
            long end = rangeParts.length > 1 && !rangeParts[1].isEmpty() 
                ? Long.parseLong(rangeParts[1]) 
                : fileSize - 1;
            if (start < 0 || end >= fileSize || start > end) return null;
            return new long[]{start, end};
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    // 发送默认封面
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
            if (!e.getMessage().contains("Broken pipe")) throw e;
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
            if (!e.getMessage().contains("Broken pipe")) throw e;
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
    
    private static String sanitizeFileName(String fileName) {
        String name = Paths.get(fileName).getFileName().toString();
        return name.replaceAll("[<>\"|?*]", "_");
    }
}