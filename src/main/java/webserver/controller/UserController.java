package webserver.controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.HttpResponseUtils;
import webserver.HttpRequest;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public void route(HttpRequest request, DataOutputStream dos) {
        if (request.getMethod().equals("GET") && request.getRequestURI().equals("/user/list")) userListGet(request, dos);
    }

    private void userListGet(HttpRequest request, DataOutputStream dos) {
        Map<String, String> headers = request.getHeaders();

        Map<String, String> cookies = HttpRequestUtils.parseCookies(headers.get("Cookie"));
        if (cookies.get("logined") != null && cookies.get("logined").equals("true")) {
            String userListHtml = getUserListHtml();

            HttpResponseUtils.response200Header(dos, userListHtml.length(), log);
            HttpResponseUtils.responseBody(dos, userListHtml.getBytes(), log);

            return;
        }

        // 로그인 한 상태가 아니라면 로그인 페이지로 이동
        HttpResponseUtils.response302Header(dos, "/user/login.html", log);
    }

    private static String getUserListHtml() {
        List<User> userList = new ArrayList<>(DataBase.findAll());

        StringBuilder sb = new StringBuilder();

        sb.append("<div>");
        for (User user : userList) {
            sb.append("<p>")
                    .append("userId = ").append(user.getUserId()).append("\n")
                    .append("name = ").append(user.getName()).append("\n")
                    .append("email = ").append(user.getEmail()).append("\n")
                    .append("\n\n")
                    .append("</p>");
        }
        sb.append("</div>");

        return sb.toString();
    }
}
