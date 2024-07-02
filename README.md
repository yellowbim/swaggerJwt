# swaggerJwt 프로젝트
Springboot3 에서 Swagger + JWT 기능을 가진 API 서비스 구축을 진행하는 프로젝트
<br/><br/><br/>


# ⚙️개발환경
- JDK	openJDK 17
- Spring Boot	3.3.0
- Gradle	8.8
- DB	H2
- Swagger(Springdoc)	2.0.2
- IDE	Intellij
<br/><br/><br/>

# 관련 정리 사이트
[https://tistory.slowtuttle.co.kr](https://slowtuttle.tistory.com/entry/Springboot3-Swagger-Jwt-1)
<br/><br/><br/>


# JWT 기본 인증 FLOW
|     구분      |   단계   | 클라이언트 | 서버 | 설명 |
|:-----------:|:------:|------------|------|------|
|   **인증**    |   1    | 로그인 요청 |  |  |
|             |   2    |  | 데이터베이스에서 ID와 비밀번호 대조 후 일치여부 확인 |  |
|             |   3    |  | 일치 시 암호화된 토큰 생성 |  |
|             |   4    |  | 응답으로 Access 토큰 / Refresh 토큰을 반환 |  |
|             |   5    | 토큰을 저장 |  |  |
|   **인가**    |   1    | API 요청 시 헤더에 Access Token을 포함시켜 요청 |  |  |
|             |   2    |  | 토큰 유효성 검증 |  |
|             |   3    |  | 유효성 확인 중 Access Token 만료가 되지 않음 |  |
|             |   4    | 응답을 받음 | 검증 완료되었다면 API 로직 처리 후 응답 |  |
|   **재발급**   |   1    | API 요청 시 헤더에 Access Token을 포함시켜 요청 |  |  |
|             |   2    |  | 토큰 유효성 검증 |  |
|             |   3    |  | 유효성 확인 중 Access Token 만료됨 |  |
|             |   4    |  | 응답으로 Access Token 만료가 되었음을 보냄 |  |
|             |   5    | Refresh Token을 헤더에 포함시켜 Access Token 재발급 요청 |  |  |
|             |   6    |  | Refresh Token의 유효성 검증 |  |
|             |   7    |  | 새로운 Access Token을 응답으로 반환하여 발급 |  |
|             |   8    | 새로운 Access Token 저장 |  |  |
- 모든 Token 만료 코드는 __401__ 로 응답 
- Access Token 만료시 Refresh Token 을 가지고 재발급 요청
- 재발급 요청 시 Refresh Token 또한 만료 시 __재로그인__ 필요
<br/><br/><br/>



# 작업 중 정리 내용
## id, userId 구분
- Member.java
  - id : seq
  - userId : 사이트 id
- Access Token / Refresh Token
  - id : seq
  - userId : 사이트 id
- Refresh Token 인증
  - userId : id
<br/><br/><br/>

# Access Token, Refresh Token 갱신 주기
>Access Token
>- 로그인 시
>- 토큰 기간 만료 시 (하루로 셋팅하는게 이상적일듯)
>
>Refresh Token
>- Access Token 재발급 시