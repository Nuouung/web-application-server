package webserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpResponseUtils;
import webserver.HttpRequest;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CssController {

    private static final Logger log = LoggerFactory.getLogger(CssController.class);

    public void route(HttpRequest request, DataOutputStream dos) {
        if (request.getMethod().equals("GET") && request.getRequestURI().endsWith(".css")) cssGet(request, dos);
    }

    private void cssGet(HttpRequest request, DataOutputStream dos) {
        try {
            File file = new File("./webapp" + request.getRequestURI());
            byte[] body = Files.readAllBytes(file.toPath());
            HttpResponseUtils.response200CssHeader(dos, body.length, log);
            HttpResponseUtils.responseBody(dos, body, log);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
