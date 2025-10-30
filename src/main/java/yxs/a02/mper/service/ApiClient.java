package yxs.a02.mper.service;

import yxs.a02.mper.model.MediaFile;
import yxs.a02.mper.model.ID3Info;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ApiClient {
    private CloseableHttpClient httpClient;
    private ObjectMapper objectMapper;

    public ApiClient() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }

    public List<MediaFile> getMediaFiles(String apiUrl) throws Exception {
        // 构建PHP API兼容的URL
        String url = apiUrl + "?event=lmf";
        System.out.println("请求媒体文件列表: " + url);

        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("API响应状态码: " + statusCode);
            //System.out.println("API响应内容: " + responseBody);

            if (statusCode == 200) {
                try {
                    // 解析PHP API的响应结构
                    Map<String, Object> responseMap = objectMapper.readValue(responseBody,
                            new TypeReference<Map<String, Object>>() {});

                    //System.out.println("解析后的响应Map: " + responseMap);

                    if (responseMap.containsKey("files")) {
                        List<Map<String, Object>> filesList = (List<Map<String, Object>>) responseMap.get("files");
                        List<MediaFile> mediaFiles = new ArrayList<>();

                        System.out.println("找到 " + filesList.size() + " 个文件");

                        for (int i = 0; i < filesList.size(); i++) {
                            Map<String, Object> fileMap = filesList.get(i);
                            //System.out.println("文件 " + i + " 的原始数据: " + fileMap);

                            MediaFile mediaFile = new MediaFile();

                            // 处理name字段
                            if (fileMap.containsKey("name")) {
                                mediaFile.setName((String) fileMap.get("name"));
                            } else {
                                System.err.println("文件 " + i + " 缺少name字段");
                                continue; // 跳过这个文件
                            }

                            // 处理path字段
                            if (fileMap.containsKey("path")) {
                                mediaFile.setPath((String) fileMap.get("path"));
                            } else {
                                // 如果没有path，使用name作为path
                                mediaFile.setPath(mediaFile.getName());
                            }

                            // 处理type字段
                            if (fileMap.containsKey("type")) {
                                mediaFile.setType((String) fileMap.get("type"));
                            } else {
                                // 根据文件扩展名推断类型
                                String fileName = mediaFile.getName().toLowerCase();
                                if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
                                        fileName.endsWith(".flac") || fileName.endsWith(".m4a")) {
                                    mediaFile.setType("audio");
                                } else {
                                    mediaFile.setType("video");
                                }
                            }

                            // 处理size字段
                            if (fileMap.containsKey("size")) {
                                Object sizeObj = fileMap.get("size");
                                if (sizeObj instanceof Integer) {
                                    mediaFile.setSize(((Integer) sizeObj).longValue());
                                } else if (sizeObj instanceof Long) {
                                    mediaFile.setSize((Long) sizeObj);
                                } else {
                                    System.err.println("文件 " + i + " 的size字段类型未知: " + sizeObj.getClass());
                                    mediaFile.setSize(0L);
                                }
                            } else {
                                mediaFile.setSize(0L);
                            }

                            // 处理duration字段
                            if (fileMap.containsKey("duration")) {
                                Object durationObj = fileMap.get("duration");
                                if (durationObj instanceof Integer) {
                                    mediaFile.setDuration(((Integer) durationObj).doubleValue());
                                } else if (durationObj instanceof Double) {
                                    mediaFile.setDuration((Double) durationObj);
                                } else if (durationObj instanceof Float) {
                                    mediaFile.setDuration(((Float) durationObj).doubleValue());
                                } else {
                                    System.err.println("文件 " + i + " 的duration字段类型未知: " + durationObj.getClass());
                                }
                            }

                            mediaFiles.add(mediaFile);
                            //System.out.println("解析后的文件: " + mediaFile.getName() + " | " + mediaFile.getPath() + " | " + mediaFile.getType());
                        }

                        if (mediaFiles.isEmpty()) {
                            System.err.println("警告: 解析后文件列表为空");
                        }

                        return mediaFiles;
                    } else if (responseMap.containsKey("error")) {
                        throw new Exception("API错误: " + responseMap.get("error"));
                    } else {
                        throw new Exception("API响应格式错误，缺少'files'字段。响应: " + responseBody);
                    }
                } catch (Exception e) {
                    System.err.println("解析API响应时出错: " + e.getMessage());
                    e.printStackTrace();
                    throw e; // 重新抛出异常，让调用者处理
                }
            } else {
                System.err.println("API请求失败 (" + statusCode + ")，使用模拟数据");
                return getFallbackMediaFiles();
            }
        } catch (Exception e) {
            System.err.println("API请求异常: " + e.getMessage());
            e.printStackTrace();
            return getFallbackMediaFiles();
        }
    }

    public MediaFile getMediaInfo(String apiUrl, String filePath) throws Exception {
        String encodedPath = URLEncoder.encode(filePath, "UTF-8");
        String url = apiUrl + "/media/info?file=" + encodedPath;
        System.out.println("请求媒体信息: " + url);

        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            if (statusCode == 200) {
                return objectMapper.readValue(responseBody, MediaFile.class);
            } else {
                System.err.println("获取媒体信息失败 (" + statusCode + ")，使用默认信息");
                return createDefaultMediaFile(filePath);
            }
        } catch (Exception e) {
            System.err.println("获取媒体信息异常: " + e.getMessage());
            return createDefaultMediaFile(filePath);
        }
    }

    public ID3Info getID3Info(String apiUrl, String filePath) throws Exception {
        String encodedPath = URLEncoder.encode(filePath, "UTF-8");
        String url = apiUrl + "/media/id3?file=" + encodedPath;
        System.out.println("请求ID3信息: " + url);

        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            if (statusCode == 200) {
                return objectMapper.readValue(responseBody, ID3Info.class);
            } else {
                System.err.println("获取ID3信息失败 (" + statusCode + ")，使用模拟信息");
                return createFallbackID3Info(filePath);
            }
        } catch (Exception e) {
            System.err.println("获取ID3信息异常: " + e.getMessage());
            return createFallbackID3Info(filePath);
        }
    }

    public String getMediaStreamUrl(String apiUrl, String filePath) {
        try {
            String encodedPath = URLEncoder.encode(filePath, "UTF-8");
            return apiUrl + "?event=mf&file=" + encodedPath;
        } catch (Exception e) {
            System.err.println("构建流媒体URL失败: " + e.getMessage());
            return apiUrl + "?event=mf&file=" + filePath;
        }
    }

    public boolean testConnection(String apiUrl) {
        try {
            String url = apiUrl + "/health";
            System.out.println("测试API连接: " + url);

            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                return statusCode == 200;
            }
        } catch (Exception e) {
            System.err.println("API连接测试失败: " + e.getMessage());
            return false;
        }
    }

    // Fallback 方法
    private List<MediaFile> getFallbackMediaFiles() {
        List<MediaFile> files = new ArrayList<>();
        files.add(new MediaFile("示例音频.mp3", "/music/sample.mp3", "audio", 1024000));
        files.add(new MediaFile("示例视频.mp4", "/video/sample.mp4", "video", 5120000));
        return files;
    }

    private MediaFile createDefaultMediaFile(String filePath) {
        MediaFile file = new MediaFile();
        file.setName(filePath.substring(filePath.lastIndexOf("/") + 1));
        file.setPath(filePath);
        file.setType(filePath.endsWith(".mp3") || filePath.endsWith(".flac") || filePath.endsWith(".wav") ? "audio" : "video");
        file.setSize(1024000);
        file.setDuration(180.0); // 3分钟
        return file;
    }

    private ID3Info createFallbackID3Info(String filePath) {
        if (filePath.endsWith(".mp3") || filePath.endsWith(".flac")) {
            ID3Info info = new ID3Info();
            info.setTitle("示例歌曲");
            info.setArtist("测试艺术家");
            info.setAlbum("示例专辑");
            info.setYear("2024");
            info.setGenre("流行");
            return info;
        }
        return null;
    }

    public void close() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}