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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;


public class PostData {
    private static int postId;
    private static String url;

    public void setUrl(String url) {
        PostData.url = url;
    }
    public static String getUrl() {
        return url;
    }
    public void setPostId(int postId) {
        PostData.postId = postId;
    }
    public static int getPostId() {
        return postId;
    }

    public PostData(String url, int postId) {
        PostData.url = url;
        PostData.postId = postId;
    }
// &api_key=5985fd8c50a9c969f32688ae7a6cf66a7485b0a2e678c85aae6f8ab91da89f35&user_id=1171000
    public static PostData getPostsFromE621(String tags,int site) throws IOException, InterruptedException {
        String url = "";
        if(site==1){ url = "https://e621.net/posts.json?tags=" + tags + "+order:random&limit=1";}
        if(site==2){ url = "https://gelbooru.com/index.php?page=dapi&s=post&q=index&api_key=5985fd8c50a9c969f32688ae7a6cf66a7485b0a2e678c85aae6f8ab91da89f35&user_id=1171000&tags=felix_argyle" + tags + "+sort:random&limit=1";}

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.body());
            JsonNode postNode = jsonNode.get("posts");
            if (postNode.isArray() && postNode.size() > 0) {
                JsonNode fileNode = postNode.get(0).get("file");
                String imageUrl = fileNode.get("url").asText();
                int postId = postNode.get(0).get("id").asInt();
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                    connection.setRequestMethod("HEAD");
                    if (connection.getResponseCode() == 200) {
                        return new PostData(imageUrl, postId);
                    } else {
                        System.out.println("Reached Error 1A");
                        return new PostData(null,0 );
                    }
                } catch (IOException e) {
                    System.out.println("Reached Error 2A");
                    return new PostData(null,0 );
                }
            } else {
                System.out.println("Reached Error 3A");
                return new PostData(null,0 );
            }
        } else {
            System.out.println("Reached Error 4A");
            return new PostData(null,0 );
        }
    }

    public static PostData parseXML(String url) {
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(url);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/posts/post/id/text()");
            String postId= (String) expr.evaluate(doc, XPathConstants.STRING);
            if (postId == null) {
                System.out.println("XPath expression for post ID returned null");
                return new PostData(null,0 );
            }

            expr = xpath.compile("/posts/post/file_url/text()");
            String imageUrl= (String) expr.evaluate(doc, XPathConstants.STRING);
            if (imageUrl == null) {
                System.out.println("XPath expression for image URL returned null");
                return new PostData(null,0 );
            }

            if(postId.isBlank() || postId.isEmpty() ){
                System.out.println("Post id is empty");
                return new PostData(null,0 );
            }
            if(imageUrl.isBlank()| imageUrl.isEmpty()){
                System.out.println("Image url is empty");
                return new PostData(null,0 );
            }

            return new PostData(imageUrl, Integer.parseInt(postId));

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }


}
