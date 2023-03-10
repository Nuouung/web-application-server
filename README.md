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
for (int i = 0; i < client.length; i++) {
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
* 

### 요구사항 3 - post 방식으로 회원가입
* 

### 요구사항 4 - redirect 방식으로 이동
* 

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 