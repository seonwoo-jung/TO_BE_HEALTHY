<p align="center">
    <img src="https://github.com/to-be-healthy/FrontEnd/assets/102174146/f0629a08-f862-4b67-bf93-d52df57acb79" alt="건강해짐 로고 이미지" >
    <br />
    <h1 align="center">건강해짐</h1>
    <p align="center">피트니스 센터, 트레이너와 회원을 위한 일정 관리 앱</p>
    <br />
    <p align="center">
      <a href="https://main.to-be-healthy.site/">웹 사이트</a>
<!--       .
      <a href="#">App</a> -->
    </p align="center">
</p>

<div align="center">
    
|테스트계정|아이디|비밀번호|
|------|---|---|
|트레이너|trainer|12345678a|
|회원|student|12345678a|

</div>

<br />

## 서비스 개요

![건강해짐 배너 sns](https://github.com/to-be-healthy/FrontEnd/assets/102174146/d1682aea-4a3e-4c3e-84fc-9c55b3626547)


## 핵심 기능

<p align="center">
    <img src="https://github.com/to-be-healthy/FrontEnd/assets/102174146/96784978-d903-47bf-832d-8433da311ae8" width="250">
    <img src="https://github.com/to-be-healthy/FrontEnd/assets/102174146/05e70f40-4c75-4349-bfaa-fedc69cbc923" width="250">
    <img src="https://github.com/ChaeRin-Im/to-be-healthy/assets/72774476/13d25e34-2c91-437f-9020-1e33a63ad4a4" width="250">
</p>

<br />

## 설계의 주안점

1. Spring Security, Oauth JWT, Redis를 사용한 로그인 시스템
2. S3, CloudFront, Lambda 활용 이미지 리사이징
3. Spring Event 기반의 알람 기능
4. WebClient를 이용한 Non-Blocking & 비동기 HTTP 통신
5. Pessimistic Lock을 활용한 동시성 해결
6. AOP로 비지니스 로직 분리, 중복 코드 제거
7. 스케줄링을 이용한 랭킹 산정
8. Rest API 구현

<br />

## 백엔드

- ### 기술 스택
1. BackEnd - Java(JDK 17), Spring Boot, JPA, Redis
2. OS - Windows, macOS
3. Library&API - KakaoLogin, NaverLogin, GoogleLogin, JavaMail, Firebase
4. Document - Jira, ERD Cloud, Discord, Postman, Figma
5. CI/CD - git, Jenkins
6. DataBase - MySQL(v8.0)
7. Server - AWS(EC2, Route 53, S3, CloudFront, Lambda), Docker
8. Monitor - Actuator, Prometheus, Promtail, Loki, Grafana

<br />

- ### API 명세
    - https://documenter.getpostman.com/view/16713698/2sA3e1CW6f

<br />

- ### 인프라

    - Docker Compose로 컨테이너 관리
    - Jenkins를 활용한 배포 자동화
    <p align="center"><img src="https://github.com/ChaeRin-Im/to-be-healthy/assets/72774476/4bdcc0f6-6ca3-4408-8f79-68fe2380ba11" width="700"></p>


<br />

- ### 모니터링 시스템

    - Docker network로 모니터링 시스템 내부망 사용
    <p align="center"><img src="https://github.com/ChaeRin-Im/to-be-healthy/assets/72774476/4e45db47-6a76-41ea-a6b7-e0fa0add1480" width="700"></p>

    - Grafana로 CPU 사용량, 실시간 로그, 알림 확인
    <p align="center"><img src="https://github.com/ChaeRin-Im/to-be-healthy/assets/72774476/caac93a3-0fcd-4104-b76e-ab1ddc19a37e"></p>

