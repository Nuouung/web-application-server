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

public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    public void route(HttpRequest request, DataOutputStream dos) {
        if (request.getMethod().equals("GET") && request.getRequestURI().equals("/user/login.html")) loginPageGet(request, dos);
        if (request.getMethod().equals("POST") && request.getRequestURI().equals("/user/login")) signupPost(request, dos);

        if (request.getMethod().equals("GET") && request.getRequestURI().equals("/user/login_fail.html")) loginFailPageGet(request, dos);
    }

    private void loginPageGet(HttpRequest request, DataOutputStream dos) {
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

        User user = DataBase.findUserById(modelAttributes.get("userId"));
        if (user != null) {
            if (user.getPassword().equals(modelAttributes.get("password"))) {
                // 로그인 성공
                HttpResponseUtils.response302Header(dos, "/index.html", "logined=true", log);
            }
        }

        // 로그인 실패
        HttpResponseUtils.response302Header(dos, "/user/login_fail.html", "logined=false", log);
    }

    private void loginFailPageGet(HttpRequest request, DataOutputStream dos) {
        try {
            File file = new File("./webapp" + request.getRequestURI());
            byte[] body = Files.readAllBytes(file.toPath());
            HttpResponseUtils.response200Header(dos, body.length, log);
            HttpResponseUtils.responseBody(dos, body, log);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}


