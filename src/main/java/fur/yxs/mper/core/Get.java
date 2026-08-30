package fur.yxs.mper.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.*;

public class Get {
    static String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    public static String listItems(String api) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(api + "?action=list"))
                .header("User-Agent", UA)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static void main(String[] args) {
        try {
            String a = listItems("http://127.0.0.1");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(a);
            JsonNode f = rootNode.get("items");
            List<MediaFile> l = new ArrayList<>();
            for (JsonNode file : f) {
                l.add(mapper.treeToValue(file,MediaFile.class));
                //System.out.println(file.get("name").asText() + " (" + file.get("type").asText() + ")");
            }
            System.out.println(l.get(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class MediaFile{
        public String name;
        public String path;
        public int size;
        public String type;
    }
}