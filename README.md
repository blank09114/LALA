# 🧠 LALA (Learning And Learning Again)

강의 수강 → 수강평 → 커뮤니티 → 학습 순환을 유도하는 **E-러닝 웹 애플리케이션**입니다.  

---

## 📌 프로젝트 개요
- **개발 형태**: Spring Boot 기반 웹 애플리케이션 (MVC 구조)  
- **개발 인원**: 6인 (박예린, 최지태, 박수경, 박시우, 이동우, 최하은)  
- **핵심 목표**  
  - 강의 수강 → 후기 → 커뮤니티 순환 구조 설계  
  - 사용자/관리자 권한 분리  
  - 학습 + 커뮤니티 기능 통합 플랫폼 구현  

---

## 🛠️ 기술 스택
| 분야 | 사용 기술 |
|------|-----------|
| Front-end | HTML, CSS, JavaScript, Thymeleaf |
| Back-end | Java |
| Database | MySQL |
| Frameworks/Libraries | Spring Boot, MyBatis, Lombok, Spring Security |
| Tools | IntelliJ, VS Code, Git |
| 배포 | AWS EC2, AWS S3, Docker |

---

## 🔍 주요 기능
- **수강 시스템**: 강의 목록 조회, 상세 정보 확인, 수강 신청  
- **수강 후기 게시판**: 강의별 후기 CRUD
- **커뮤니티 게시판**: 자유게시판, 게시글/댓글 CRUD  
- **관리자 페이지**: 회원/강의/게시글 관리 기능 제공  
- **권한 분리**: 사용자 / 관리자 권한 차등 제공

---

## 💼 담당 파트
- **메인 페이지 / 강의 리스트 / 강의 상세 / LMS 프론트엔드 구현**
  - HTML/CSS/JS 화면 퍼블리싱  
  - Thymeleaf 템플릿 엔진 적용  
  - 강의 리스트 및 상세 페이지 레이아웃 구성  
  - 수강 화면(LMS) 설계 및 학습 진행 화면 구현
  - 유효성 검사 및 사용자 피드백 UI 처리

---

## ⚙️ 기술적 구현
- **Spring MVC + Rest 아키텍처 적용**
  - **DB ↔ MyBatis Mapper(XML) ↔ Mapper Interface ↔ Service ↔ Controller ↔ REST API/Model ↔ View**
  - REST API를 통해 클라이언트-서버 간 데이터 교환 처리
  - 일부 기능은 Controller에서 Model 객체에 데이터 바인딩 후 View로 전달 처리
- **MyBatis ORM 활용**: Mapper XML 기반 SQL 관리, DTO 매핑 처리
- **Thymeleaf 렌더링**: 서버사이드 동적 렌더링으로 UI 구성  
- **권한 관리**: 사용자/관리자 Role 기반 접근 제어
- **AWS 배포**: 로컬 개발 후 클라우드 환경에서 실행 테스트  

---

## 🎯 협업 경험
- 기능별(강의/후기/커뮤니티/문의/관리자) 역할 분담  
- ERD, 기능 명세, 개발 명세 공동 설계 후 기능 구현  
- Git 브랜치 전략을 활용해 코드 공유 및 최종 통합
- 발표 자료 공동 작성 및 발표 공동 진행

---

## 📂 디렉터리 구조 (요약)
<pre>
src/main/
├── java/com/example/lala
│ ├── API/ # REST API 컨트롤러
│ ├── Config/ # 설정 클래스 (보안, S3 등)
│ ├── Controller/ # 웹 MVC 컨트롤러
│ ├── DTO/ # 데이터 전송 객체
│ ├── Entity/ # 엔티티 클래스
│ ├── JPARepository/ # JPA 레포지토리
│ ├── Mapper/ # MyBatis 매퍼 인터페이스
│ ├── Service/ # 서비스 계층
│ ├── exception/ # 예외 처리 클래스
│ └── LalaApplication.java # 메인 실행 클래스
│
└── resources
├── mapper/ # MyBatis XML 매퍼
├── static/ # 정적 자원 (CSS, JS, 이미지)
└── templates/ # Thymeleaf 템플릿
</pre>
