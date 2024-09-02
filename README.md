<p align="center">
    <img src="https://github.com/to-be-healthy/FrontEnd/assets/102174146/f0629a08-f862-4b67-bf93-d52df57acb79" alt="건강해짐 로고 이미지" >
    <br />
    <h1 align="center">건강해짐</h1>
    <p align="center">피트니스 센터, 트레이너와 회원을 위한 일정 관리 앱</p>
    <br />
    <p align="center">
      <a href="https://main.to-be-healthy.site/">웹 사이트</a> <br/>
      <a href="https://apps.apple.com/kr/app/%EA%B1%B4%EA%B0%95%ED%95%B4%EC%A7%90/id6547838272/">애플 앱스토어</a>
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
<p>건강해짐은 피트니스 센터와 트레이너, 회원들을 위한 일정 관리 및 커뮤니케이션을 간편하게 도와주는 앱입니다.</p>
<p>주요 기능으로는 트레이너와 회원 간의 일정 예약, 실시간 알림, 회원 관리 등이 있습니다.</p>

<br />

## 핵심 기능

<p align="center">
    <img src="https://github.com/to-be-healthy/FrontEnd/assets/102174146/96784978-d903-47bf-832d-8433da311ae8" width="250">
    <img src="https://github.com/to-be-healthy/FrontEnd/assets/102174146/05e70f40-4c75-4349-bfaa-fedc69cbc923" width="250">
    <img src="https://github.com/ChaeRin-Im/to-be-healthy/assets/72774476/13d25e34-2c91-437f-9020-1e33a63ad4a4" width="250">
</p>

- 일정 관리: 트레이너와 회원 간의 실시간 예약 및 일정 관리 기능 제공
- 실시간 알림: 예약, 변경, 취소 등 모든 일정 변화에 대해 즉각적인 알림 제공
- 회원 관리: 회원의 수업 기록 관리 및 맞춤형 피드백 제공
<br />

## 담당 기능

### 1. 아키텍처 및 API 설계
- **아키텍처 설계**: DDD(도메인 주도 설계) 기반으로 설계하여 확장성과 유지보수성을 고려한 구조화. RESTful API 규칙을 준수하여 API 설계
- **주요 도메인 구현**: 회원, 예약, 수업 일지, 헬스장, 알림/푸시 알림, 파일, 홈 API
- **동시성 제어**: 예약 기능에서 Pessimistic Lock을 활용하여 동시성 문제 해결
- **응답 및 예외 처리**: 공통 응답 객체와 GlobalExceptionHandler를 통해 일관된 응답 처리 및 예외 관리
- **테스트**: JUnit과 Kotest를 이용하여 테스트 코드 작성
- **비동기 통신**: WebClient를 사용하여 Non-Blocking 비동기 HTTP 통신을 구현, 성능 최적화
- **푸시 알림**: 스케줄링을 활용하여 푸시 알림 전송 구현

### 2. 인증 및 인가
- **인증/인가 구현**: Spring Security와 JWT를 이용한 인증/인가 체계 구축
- **토큰 관리**: Redis를 활용한 JWT Refresh Token 저장으로 서버 스케일 아웃 시 유리한 구조 구성
- **소셜 로그인**: OAuth2(네이버, 카카오, 애플)를 통한 소셜 로그인 기능 구현

### 3. 파일 업로드 및 이미지 처리
- **Presigned URL 활용을 통한 파일 업로드 최적화**: Presigned URL 사용함으로 클라이언트가 S3에 직접 파일을 업로드함으로써 백엔드 서버의 자원을 절약하고, 업로드 처리의 병목을 해소하도록 구현
- **실시간 이미지 리사이징**: AWS S3와 Lambda@Edge를 사용해 실시간 이미지 리사이징 기능을 구현, 리사이징된 이미지는 CDN 서버에 캐싱하여 스토리지 비용 절감

### 4. 알림 기능
- **푸시 알림**: Firebase FCM을 이용하여 웹 푸시 알림 기능 구현

<br />

## 백엔드 기술 스택
- 프레임워크 및 언어: Java (JDK 17), Kotlin, Spring Boot, JPA, QueryDSL
- 데이터베이스: MySQL 8.0
- 캐시: Redis
- 인증 및 로그인: Spring Security, OAuth, JWT
- 비동기 처리: Spring Event, WebClient
- CI/CD: Git, Jenkins
- 서버 및 인프라: AWS (EC2, Route 53, S3, CloudFront, Lambda), Docker
- 모니터링: Actuator, Prometheus, Grafana, Loki

<br />

## 인프라 및 배포
- 컨테이너 관리: Docker Compose를 사용하여 애플리케이션과 관련 서비스를 컨테이너화 및 오케스트레이션
- 배포 자동화: Jenkins를 활용해 지속적인 통합 및 배포(CI/CD)를 구현하여 개발 속도와 안정성 향상
<p align="center"><img src="https://github.com/ChaeRin-Im/to-be-healthy/assets/72774476/4bdcc0f6-6ca3-4408-8f79-68fe2380ba11"></p>

<br />

## 모니터링 시스템
- 네트워크 구성: Docker 네트워크로 모니터링 시스템 내부망을 구성하여 보안을 강화
- 실시간 모니터링: Grafana를 통해 CPU 사용량, 실시간 로그, 알림 등을 모니터링하여 시스템의 성능 및 안정성을 관리
<p align="center"><img src="https://github.com/ChaeRin-Im/to-be-healthy/assets/72774476/4e45db47-6a76-41ea-a6b7-e0fa0add1480"></p>
<p align="center"><img src="https://github.com/ChaeRin-Im/to-be-healthy/assets/72774476/caac93a3-0fcd-4104-b76e-ab1ddc19a37e"></p>

## API 명세
- https://documenter.getpostman.com/view/16713698/2sA3e1CW6f
