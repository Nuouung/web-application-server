package webserver.controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpResponseUtils;
import webserver.HttpRequest;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class SignupController {

    private static final Logger log = LoggerFactory.getLogger(SignupController.class);

    public void route(HttpRequest request, DataOutputStream dos) {
        if (request.getMethod().equals("GET") && request.getRequestURI().equals("/user/form.html")) signupPageGet(request, dos);
        if (request.getMethod().equals("POST") && request.getRequestURI().equals("/user/create")) signupPost(request, dos);
    }

    private void signupPageGet(HttpRequest request, DataOutputStream dos) {
        try {
            File file = new File("./webapp" + request.getRequestURI());
            byte[] body = Files.readAllBytes(file.toPath());
            HttpResponseUtils.response200Header(dos, body.length, log);
            HttpResponseUtils.responseBody(dos, body, log);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void signupPost(HttpRequest request, DataOutputStream dos) {
        Map<String, String> modelAttributes = request.getModelAttributes();
        User user = new User(modelAttributes.get("userId"), modelAttributes.get("password"), modelAttributes.get("name"), modelAttributes.get("email"));
        DataBase.addUser(user);

//        String location = request.getHeaders().get("Host") + "/index.html";
        HttpResponseUtils.response302Header(dos, "/index.html", log);
    }
}


