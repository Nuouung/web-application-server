package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private String method;
    private String requestURI;
    private String httpVersion;

    private Map<String, String> queryStringMap;

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String firstLine = br.readLine();
        parseFirstHeader(firstLine);
    }

    private void parseFirstHeader(String line) {
        String[] sArray = line.split(" ");
        method = sArray[0];

        requestURI = sArray[1].split("\\?")[0];
        queryStringMap = parseQueryString(sArray);

        httpVersion = sArray[2];
    }

    private Map<String, String> parseQueryString(String[] sArray) {
        return (sArray[1].split("\\?").length > 1) ?
                parseQueryString(sArray[1].split("\\?")[1]) :
                new HashMap<>();
    }

    private Map<String, String> parseQueryString(String target) {
        Map<String, String> queryStringMap = new HashMap<>();

        String[] queryStrings = target.split("&");
        for (String queryString : queryStrings)
            queryStringMap.put(queryString.split("=")[0], queryString.split("=")[1]);

        return queryStringMap;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getQueryStringMap() {
        return queryStringMap;
    }
}
