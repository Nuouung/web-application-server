package webserver;

import com.google.common.collect.Maps;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class HttpRequest {

    private String method;
    private String requestURI;
    private String httpVersion;

    private Map<String, String> queryStringMap;

    private Map<String, String> headers;

    private String body;

    private Map<String, String> modelAttributes;

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String line = br.readLine();
        parseFirstHeader(line);

        parseHeaders(br, line);

        if (headers.get("Content-Length") != null) {
            body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
            modelAttributes = parseValues(body);
        }
    }

    private void parseHeaders(BufferedReader br,String line) throws IOException {
        headers = new HashMap<>();

        while (!line.equals("")) {
            if (line.split(": ").length > 1)
                headers.put(line.split(": ")[0], line.split(": ")[1]);
            else if (line.split(":").length > 1)
                headers.put(line.split(":")[0], line.split(":")[1]);

            line = br.readLine();
        }
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
                parseValues(sArray[1].split("\\?")[1]) :
                new HashMap<>();    }

    private Map<String, String> parseValues(String target) {
        if (target == null || target.equals(""))
            return Maps.newHashMap();

        Map<String, String> queryStringMap = new HashMap<>();

        String[] tokens = target.split("&");
        for (String value : tokens)
            queryStringMap.put(value.split("=")[0], value.split("=")[1]);

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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getModelAttributes() {
        return modelAttributes;
    }
}
