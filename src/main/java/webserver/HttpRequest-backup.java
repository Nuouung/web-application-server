//package webserver;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Queue;
//
//public class HttpRequest {
//
//    private String method;
//    private String requestURI;
//    private String httpVersion;
//    private Map<String, String> headerMap;
//    private String body;
//
//    public HttpRequest(InputStream in) throws IOException {
//        BufferedReader br = new BufferedReader(new InputStreamReader(in));
//
//        int lengthOfBytes = in.available();
//        Queue<Byte> queue = new LinkedList<>();
//        headerMap = new HashMap<>();
//
//        boolean isHeader = true;
//        int lineNumber = 0;
//        while (true) {
//            StringBuffer stringBuffer = new StringBuffer();
//            while (true) {
//                int ch = br.read();
//                if (ch < 0 || ch == '\n') break;
//
//                stringBuffer.append((char) ch);
//            }
//
//            String line = stringBuffer.toString();
//
//            if (isHeader) {
//                if (line.e)
//
//                continue;
//            }
//        }
//
//        // header
//        while ((line = br.readLine()) != null) {
//            lineNumber++;
//
//            if (lineNumber == 1) {
//                parseFirstHeader(line);
//                continue;
//            }
//
//            if (line.split(": ").length > 1)
//                headerMap.put(line.split(": ")[0], line.split(": ")[1]);
//            else if (line.split(":").length > 1)
//                headerMap.put(line.split(":")[0], line.split(":")[1]);
//        }
//
//        // body
//        StringBuilder sb = new StringBuilder();
////        while ((line = br.readLine()) != null && !"".equals(line)) {
////            sb.append(line);
////        }
////
////        body = sb.toString();
//    }
//
//    private void parseFirstHeader(String line) {
//        String[] sArray = line.split(" ");
//        method = sArray[0];
//        requestURI = sArray[1];
//        httpVersion = sArray[2];
//    }
//
//    public String getMethod() {
//        return method;
//    }
//
//    public String getRequestURI() {
//        return requestURI;
//    }
//
//    public String getHttpVersion() {
//        return httpVersion;
//    }
//
//    public Map<String, String> getHeaderMap() {
//        return headerMap;
//    }
//
//    public String getBody() {
//        return body;
//    }
//}
