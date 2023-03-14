package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpResponseUtils;
import webserver.controller.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    private final IndexController indexController = new IndexController();
    private final SignupController signupController = new SignupController();
    private final LoginController loginController = new LoginController();
    private final UserController userController = new UserController();
    private final CssController cssController = new CssController();

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에서 구현하면 된다.
            HttpRequest httpRequest = new HttpRequest(in);
            DataOutputStream dos = new DataOutputStream(out);

            // 위에서부터 우선적으로 적용 (ex. 라우팅 경로가 겹치는 경우 위의 것이 우선적으로 적용됨)
            indexController.route(httpRequest, dos);
            signupController.route(httpRequest, dos);
            loginController.route(httpRequest, dos);
            userController.route(httpRequest, dos);
            cssController.route(httpRequest, dos);

            // default
            List<String> userInfoList = DataBase.findAll().stream().map(User::toString).collect(Collectors.toList());

            StringBuilder sb = new StringBuilder();
            for (String userInfo : userInfoList) {
                sb.append(userInfo).append("\n").append("\n").append("\n");
            }

            byte[] body = sb.toString().getBytes();
            HttpResponseUtils.response200Header(dos, body.length, log);
            HttpResponseUtils.responseBody(dos, body, log);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
