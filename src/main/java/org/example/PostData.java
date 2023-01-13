package org.example;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


class PostData {
    private int postId;
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
    public void setPostId(int postId) {
        this.postId = postId;
    }
    public int getPostId() {
        return postId;
    }

    public PostData(String url, int postId) {
        this.url = url;
        this.postId = postId;
    }

    private PostData getPostsFromE621(String tags) throws IOException, InterruptedException, JsonProcessingException {
        String url = "https://e621.net/posts.json?tags=" + tags + "+order:random&limit=1";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        PostData postData = new PostData(url, postId);
        if (response.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.body());
            JsonNode postNode = jsonNode.get("posts");
            if (postNode.isArray() && postNode.size() > 0) {
                JsonNode sampleNode = postNode.get(0).get("file").get("url");
                String imageUrl = sampleNode.asText();
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                    connection.setRequestMethod("HEAD");
                    if (connection.getResponseCode() == 200) {
                        if (postNode.isArray() && postNode.size() > 0) {
                            JsonNode postIdNode = postNode.get(0).get("id");
                            int Id = postIdNode.asInt();
                            postId = Id;
                        }
                        postData.url=imageUrl;
                    } else {

                    }
                } catch (IOException e) {

                }
            } else {

            }
        }
        return new PostData(url, postId);
    }

}