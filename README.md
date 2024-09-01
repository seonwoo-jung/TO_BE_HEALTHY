건강해짐 로고 이미지
건강해짐
피트니스 센터, 트레이너와 회원을 위한 일정 관리 앱


웹 사이트

테스트계정	아이디	비밀번호
트레이너	trainer	12345678a
회원	student	12345678a

서비스 개요
건강해짐 배너 sns

핵심 기능
  


설계의 주안점
Spring Security, Oauth JWT, Redis를 사용한 로그인 시스템
S3, CloudFront, Lambda 활용 이미지 리사이징
Spring Event 기반의 알람 기능
WebClient를 이용한 Non-Blocking & 비동기 HTTP 통신
Pessimistic Lock을 활용한 동시성 해결
AOP로 비지니스 로직 분리, 중복 코드 제거
스케줄링을 이용한 랭킹 산정
Rest API 구현

백엔드
기술 스택
BackEnd - Java(JDK 17), Spring Boot, JPA, Redis
OS - Windows, macOS
Library&API - KakaoLogin, NaverLogin, GoogleLogin, JavaMail, Firebase
Document - Jira, ERD Cloud, Discord, Postman, Figma
CI/CD - git, Jenkins
DataBase - MySQL(v8.0)
Server - AWS(EC2, Route 53, S3, CloudFront, Lambda), Docker
Monitor - Actuator, Prometheus, Promtail, Loki, Grafana

API 명세
https://documenter.getpostman.com/view/16713698/2sA3e1CW6f

인프라
Docker Compose로 컨테이너 관리
Jenkins를 활용한 배포 자동화



모니터링 시스템
Docker network로 모니터링 시스템 내부망 사용


Grafana로 CPU 사용량, 실시간 로그, 알림 확인
