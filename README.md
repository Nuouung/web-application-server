# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer2 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답

* 처음에는 inputStream안에 들어 있는 데이터가 어떤 모양인지 확인해보고 싶어 아래와 같이 코드를 썼다. 그리고 코드를 돌리고 inputStream 내부에 들어 있는 데이터는 HTTP 요청 메시지인 것을 확인했다.
```
int available = in.available();
byte[] request = new byte[in.available()];
for (int i = 0; i < request.length; i++) {
    request[i] = (byte) in.read();
}

String requestString = new String(request);
System.out.println("======================");
System.out.println(requestString);
System.out.println("======================");
```
* 다음으로는 HttpRequest라는 별도의 클래스를 만들어 해당 클래스에 http 요청에 대한 데이터를 바인딩할 생각을 했다. http 메소드, url 등의 정보를 객체에 바인딩해서 사용하면 훨씬 효율적이라는 판단이었다. 이 과정에 BufferedReader를 사용했는데, 그때부터 서버가 이상동작하기 시작했다. 브라우저에서 요청을 보내면 응답을 받지 못하고 계속 대기를 하고 있는 것이었다.
* 디버깅 등을 통해 원인을 파악해보니 데이터 바인딩을 위해 사용했던 BufferedReader의 readLine 메소드에서 문제가 나고 있음을 발견했다. 아래처럼 코드를 작성한 것인데, http 요청 메시지에 EOF(End of File) 라인이 존재하지 않아 buffered reader가 하염없이 다음 메시지를 기다리기에 발생한 대기현상이었다.
```
String line;
while ((line = br.readLine()) != null) { // br.readLine()에서 다음 인풋을 받기 위해 무한대기한다.
    // http 메시지를 클래스 필드에 바인딩하는 내부 로직
}
```
* 결국 HttpRequest 클래스를 주석처리한 후 아래와 같이 코드를 써 GET 메소드로 /index.html에 접근 시 index.html 파일을 넘겨주는 것으로 작업을 했다.
```
BufferedReader br = new BufferedReader(new InputStreamReader(in));
DataOutputStream dos = new DataOutputStream(out);

String firstLine = br.readLine();

if (firstLine.split(" ")[1].equals("/index.html") && firstLine.split(" ")[0].equals("GET")) {
    File file = new File("[경로]\\webapp\\index.html");
    byte[] body = Files.readAllBytes(file.toPath());
    response200Header(dos, body.length);
    responseBody(dos, body);
    return;
}

```

### 요구사항 2 - get 방식으로 회원가입

* 요구사항 1에서 개선사항이 조금 생겼다. index 페이지를 요청받는 정보와 회원가입 데이터를 요청받는 정보들이 분기되는 등 서버에 요구되는 요청의 스펙이 많아지면서 RequestHandler에 요청 응답을 전담하는 로직을 작성하는 기존의 방식이 너무 지저분하게 느껴진 것이었다. controller라는 패키지를 새로 만든 후 각각의 기능을 담당하는 별도의 컨트롤러 객체를 생성해주었다. 그리고 RequestHandler에서는 아래와 같이 컨트롤러 객체를 호출하는 방식으로 적용했다.
```
private final IndexController indexController = new IndexController();
private final SignupController signupController = new SignupController();

...

// 위에서부터 우선적으로 적용 (ex. 라우팅 경로가 겹치는 경우 위의 것이 우선적으로 적용됨)
indexController.route(httpRequest, dos);
signupController.route(httpRequest, dos);
```
* 두 번째 개선사항은 GET 방식의 요청에서 쿼리파라미터가 들어오는 스펙이 새로 생기면서 HttpRequest 객체를 수정한 부분이었다. HttpRequest 객체에서 HTTP 요청 메시지를 파싱할 때, 첫 번째 줄에 쿼리파라미터를 파악해 객체 내에 필드로 바인딩하는 방식을 적용했다. 이를 위해 queryStringMap이라는 HashMap 필드를 적용했는데, 쿼리스트링을 객체 내에 담는 과정은 쉬우나 디버깅 툴을 이용하지 않으면 특정 시점에 HttpRequest가 어떤 쿼리 스트링을 가지고 있는지 파악하기 어려우므로 꺼내 쓰는게 상당히 부담될 것 같은 느낌이 들었다. 이 부분은 코드가 확장되면서 개선의 여지가 필요할 것으로 보인다.
```
private Map<String, String> queryStringMap;

...

queryStringMap = parseQueryString(sArray);

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
```
* 위와 같이 코드를 개선하고 새로 만든 signupController에서 회원가입에 필요한 로직을 작성했다. 회원가입 form 페이지로 이동하는 메소드와, 회원가입을 수행하는 메소드 두 개를 작성했고 회원가입 수행 시에는 RequestHandler에서 생성한 HttpRequest 객체 내부의 회원 정보를 queryStringMap에서 추출해 도메인 객체에 바인딩 하는 작업을 해주었다. 이후 도메인 객체는 DataBase 클래스에 의해 메모리상에 저장되는 방식으로 회원가입을 완료하는 방식으로 진행되었다.
```
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
    Map<String, String> queryStringMap = request.getQueryStringMap();
    User user = new User(queryStringMap.get("userId"), queryStringMap.get("password"), queryStringMap.get("name"), queryStringMap.get("email"));
    DataBase.addUser(user);
}
```

### 요구사항 3 - post 방식으로 회원가입

* 기존에 요구사항 1번 작업을 할 때 만났던 br.readLine() 관련 문제를 다시 만났다. while 문에 들어간 readLine()이 다음 커멘드가 들어올 떄까지 무한 대기에 빠진 것. 다시 한번 웹서핑을 해보고 이번에는 chatGPT에게 해결법을 물어보면서까지 디버깅을 시도했다. 그러나 실패.
* 결국 책에 있는 힌트 코드를 보고 코드 리펙토링을 시도했다. while (!line.equals(""))로 두고 while문 내부에서 br.readLine으로 최신화해주면 무한 대기에 빠지지 않는다는 상태를 확인했지만, 영... 찝찝한 마음을 감출 수는 없었다. 그러나 이렇다 할 방법이 없었으므로, 그냥 넘어갔다.
* HttpRequest 클래스에 Http 요청 메시지의 바디 데이터를 기록할 수 있는 로직을 추가적으로 작성했다. content-length가 있는 경우에만 바인딩하도록 작업했는데, 아마 데이터가 json으로 들어오거나 raw로 들어오거나 하는 상황에서는 nullPointerException이 날 확률이 매우 매우 높다(아마 form 형태가 아니면 전부 에러가 날 거다). 일단은 실력 부족으로 리펙토링 할 엄두가 나지 않아서... 이것도 패스
* 코드는 아래와 같은 부분이 추가되었다.
```
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

...

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

...

private Map<String, String> parseValues(String target) { // 여기에서 아마 에러가 날거다.
    if (target == null || target.equals(""))
        return Maps.newHashMap();

    Map<String, String> queryStringMap = new HashMap<>();

    String[] tokens = target.split("&");
    for (String value : tokens)
        queryStringMap.put(value.split("=")[0], value.split("=")[1]);

    return queryStringMap;
}
```

* HttpRequest를 어찌어찌 작성하고, 정신력이 소진된 너덜너덜한 상태로 html form 부분을 get에서 post로 바꾼 후 SignupController 부분을 수정해주었다. 
```
private void signupPost(HttpRequest request, DataOutputStream dos) {
    Map<String, String> modelAttributes = request.getModelAttributes();
    User user = new User(modelAttributes.get("userId"), modelAttributes.get("password"), modelAttributes.get("name"), modelAttributes.get("email"));
    DataBase.addUser(user);
}
```

* 느낀 점) 멀티쓰레드 환경인 탓에 static 코드들에 대한 동시성 문제도 걱정이 되고(실제로 로그를 보면 쓰레드 몇개가 왔다갔다하며 무작위로 로그를 찍는다), 내가 파악하지 못한 이상한 네트워크 요청(빈값으로 자꾸 요청이 온다. 뭔지 모르겠다. 또한 브라우저로 접근했을 때의 요청과 postman으로 요청했을 때의 요청값?이 다르다. 브라우저 접근 시에는 HttpRequest에 에러가 안뜨는데 postman으로 요청을 하면 백이면 백 에러가 남..), BufferedReader 등의 자바 IO의 일관적이지 않은 처리 과정(아마 내가 잘 모르는 부분이 있어서 발생하는 것이겠지만)이 내 지식 수준을 넘어서는 것들이라 쉽게 손을 댈 수 없을 뿐더러 작은 에러라도 나면 쉽게 건들지 못하고 몇시간을 쏟아야 하는 상황이 많이 답답했다. 에러는 왜 이리 많이 나며 또 파악하기 어려운지... 내 자신이 한없이 작아지는 느낌을 많이 받았다.

### 요구사항 4 - redirect 방식으로 이동
* 솔직히 금방 할 수 있을 것 같았다. 컨트롤러를 요구사항 3번에서 작성을 해놨고, 302 리다이렉트 명령어만 넣어서 응답으로 보내주면 브라우저에서 자동으로 리다이렉트를 할테니 코드만 몇줄 적으면 되겠지 싶었다.
* 그런데 생각보다 오래 걸렸을 뿐더러, 아직도 이해되지 않는 이슈를 만났다. 자바의 IO 처리에 대한 내 지식의 부족 때문인지, 아니면 네트워크 특히 HTTP에 대한 내 지신의 부족 때문인지 잘 모르겠지만 정말 어려웠다. 
* 우선 작성한 코드는 다음과 같았으며 HttpResponseUtils에서 response302Header이라는 메소드를 만든 후 SignupController에서 해당 메소드를 실행해 주는 방식으로 진행했다.
```
// SignupController 로직
private void signupPost(HttpRequest request, DataOutputStream dos) {
    Map<String, String> modelAttributes = request.getModelAttributes();
    User user = new User(modelAttributes.get("userId"), modelAttributes.get("password"), modelAttributes.get("name"), modelAttributes.get("email"));
    DataBase.addUser(user);

//        String location = request.getHeaders().get("Host") + "/index.html";
    HttpResponseUtils.response302Header(dos, "/index.html", log);
}

// HttpResponseUtils 로직
public static void response302Header(DataOutputStream dos, String location, Logger log) {
    try {
        dos.writeBytes("HTTP/1.1 302 Found \r\n");
        dos.writeBytes("Location: " + location + "\r\n");
        dos.writeBytes("\r\n");
    } catch (IOException e) {
        log.error(e.getMessage());
    }
}
```

* SignupController에서 location을 만들어서 리다이렉트될 url을 적어주는 부분이 좀 문제였는데, 처음에는 localhost:8080/index.html으로 리다이렉트 위치를 설정해줬다. 즉 Location: localhost:8080/index.html 이런 식으로 응답이 갔던 것. 문제는 이렇게는 리다이렉트가 되지 않았을 뿐더러 서버 내부에서는 소켓 에러까지 발생했다. 이유를 도저히 모르겠다.
* 그래서 location을 /index.html로 바꿔주었더니, 리다이렉트도 정상적으로 실행되고 모든 코드가 정상적으로 실행됐다. 네트워크.... IO.... 진짜 모르겠다.

### 요구사항 5 - 로그인 처리 (cookie 적용)

* 이전 요구사항에서 회원 가입 기능을 구현했고, 메모리 상에 회원 데이터가 저장되도록 구현되어 있었기 때문에 LoginController라는 새로운 클래스를 만들어서 get 메소드(로그인 페이지 화면)와 post 메소드(로그인 로직 처리)에 대응할 수 있는 로직을 만들어 주었다. 로그인 post 처리 시 userId와 password를 비교해 일치하면 cookie에 logined=true를 적용해 인덱스 페이지로 리다이렉트 시켰고, 로그인에서 실패하면 cookie에 logined=false를 준 후 로그인 실패 페이지로 리다이렉트 시켜줬다.
* HttpResponseUtils에 있는 response302Header 메소드가 쿠키를 적용할 수 있게 해당 메소드를 오버라이딩 해주었다. 파라미터 값으로 cookie를 부여할 수 있게 작업하기는 했지만, 리펙토링의 여지가 다분하다. 두 가지 경우 곤란해질 수 있는데 첫번째는 지금 부여하는 쿠키는 String 형태로 부여되기 때문에 "aa=xx; bb=yy" 이런 식으로 String을 짜서 파라미터를 전달하면 두개 이상의 쿠키를 적용할 수 있지만 일단 저런 작업 자체가 불편하다. 두 번째는 302 리다이렉트를 담당하는 Http response 메소드가 오버라이딩 되며 너무 많아질 수 있다는 부분에 대한 우려스러움이 있다.
* 우선 코드는 아래와 같이 작업해주었다. 차례로 LoginController에서의 로그인 처리 로직, HttpResponseUtils에 새로 추가된 response302Header 메소드이다.
```
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
```
```
public static void response302Header(DataOutputStream dos, String location, Logger log) {
    try {
        dos.writeBytes("HTTP/1.1 302 Found \r\n");
        dos.writeBytes("Location: " + location + "\r\n");
        dos.writeBytes("\r\n");
    } catch (IOException e) {
        log.error(e.getMessage());
    }
}

public static void response302Header(DataOutputStream dos, String location, String cookie, Logger log) {
    try {
        dos.writeBytes("HTTP/1.1 302 Found \r\n");
        dos.writeBytes("Location: " + location + "\r\n");
        dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
        dos.writeBytes("\r\n");
    } catch (IOException e) {
        log.error(e.getMessage());
    }
}
```

### 요구사항 6 - 로그인 여부에 따라 user list 페이지 노출 분기

* 다음으로 진행해본 사항은 로그인 여부에 따라, 로그인을 한 회원은 user list 요청에 응해 모든 회원의 리스트를 보여주고 로그인을 하지 않은 회원은 로그인 페이지로 리다이렉트 하는 작업이었다. 해당 작업은 회사에서 일을 하며 수시로 했던 작업이었기 때문에, 그리고 스프링에서 작업하는 경우와 거의 유사한 패턴을 가졌기 때문에 가볍게 처리할 수 있었다.
* 다만 회원의 정보를 html로 만들어서 출력해야 하는 이슈가 있었는데, html 파일을 만들어서 출력을 해줄 수도 있겠지만 JSP와 같은 템플릿을 어떻게 개발할 수 있는지 배우지 않았으므로 StringBuilder를 이용해 직접 html 태그를 작성해 서버상에서 뿌려주는 방식으로 작업했다. (JSP도 SSR이기 때문에 결국 서버에서 뿌려주기는 하겠지만..?)
* 코드는 아래와 같다.
```
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
```

### 요구사항 7 - css 파일 지원하기

* 해당 작업은 처음해 봤다. 스프링을 사용하면서, 그리고 웹서버를 구축하지 않고 단순히 html 공부를 할 때, 스프링이나 브라우저가 해당 작업을 해주었기 때문에 css 파일이 Content-Type을 text/css로 주어야 한다는 사실을 처음 안 것이었다. js도 마찬가지일 것 같다는 생각을 했는데, 내가 모르는 작업들이 스프링이나 브라우저 내부에서 어떻게 추상화되어서 사용자 모르게 처리가 되고 돌아가는지 새삼 신기했던 것 같다. 모르는 동안 프레임워크나 프로그램의 도움을 받아 작업을 편하게 한 것들이 얼마나 많았던 것일까 생각했던 것 같다.
* 해당 작업은 브라우저에서 css 요청을 오는 부분도 정상적이었으므로 HttpResponseUtils에 css 파일을 지원하는 코드를 작성한 후, CssController를 만들어 css 파일을 지원할 수 있도록 코드를 작성했다.
```
// HttpResponseUtils
public static void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent, Logger log) {
    try {
        dos.writeBytes("HTTP/1.1 200 OK \r\n");
        dos.writeBytes("Content-Type: text/css\r\n");
        dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
        dos.writeBytes("\r\n");
    } catch (IOException e) {
        log.error(e.getMessage());
    }
}

// CssController
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
```

### heroku 서버에 배포 후
* 