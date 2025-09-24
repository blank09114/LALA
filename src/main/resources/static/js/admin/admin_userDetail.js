 // URL에서 userId 추출
  const userId = location.pathname.split('/').pop();

  const typeConfig = {
    posts:   { label: '게시글',   color: '#007bff' },
    comments:{ label: '댓글',     color: '#28a745' },
    replies: { label: '대댓글',   color: '#17a2b8' },
    reviews: { label: '수강평',   color: '#ffc107' },
    lectureRequests:{ label: '강의 요청', color: '#ff5722' }
  };


  // 전역 변수로 마지막 로드된 데이터 저장
  window.lastLoadedData = null;

  // 유저 정보 로딩 함수
  async function loadUserData(filter = "mine") {
      try {
          const title = document.querySelector('input[name="contentTitle"]').value;
          const startDate = document.querySelector('input[name="startDate"]').value;
          const endDate = document.querySelector('input[name="endDate"]').value;

          const labelSpan   = document.getElementById('reviews-label');
          const anchor      = document.getElementById('reviews-anchor');

          // 필터 기능 (제목, 날짜)
          const params = new URLSearchParams();
          params.append("filter", filter);
          if (title) params.append("title", title);
          if (startDate) params.append("startDate", startDate);
          if (endDate) params.append("endDate", endDate);

          const response = await fetch(`/api/admin/user/${userId}?${params.toString()}`);
          const data = await response.json();
          const user = data.user;

          // 전역 변수에 데이터 저장
          window.lastLoadedData = data;

          // 유저 정보 출력
          document.getElementById('email').textContent = user.email || '정보 없음';
          document.getElementById('nickname').textContent = user.userNickname || '정보 없음';
          document.getElementById('regDate').textContent = user.membershipRegistrationDate || '정보 없음';
          document.getElementById("userTypeSelect").value = user.accountTypeId || 0;
          document.getElementById("activityTypeSelect").value = user.activityTypeId || 1;


          if (user.accountTypeId === 1) {            // 지식제공자 계정
              labelSpan.textContent     = '강의 요청';
              anchor.dataset.type       = 'lectureRequests';   // 모달·미리보기용 key
              renderCommunityData(data.lectureRequests, 'reviews', 'lectureRequests');


            } else {                                   // 일반·지식제공자·관리자
              labelSpan.textContent     = '수강평';
              anchor.dataset.type       = 'reviews';
              renderCommunityData(data.reviews, 'reviews', 'reviews');
            }

          // 각 카테고리별 데이터 렌더링
          renderCommunityData(data.posts, 'posts');
          renderCommunityData(data.comments, 'comments');
          renderCommunityData(data.replies, 'replies');

      } catch (error) {
          console.error("유저 데이터 불러오기 실패:", error);
          // 에러 발생 시 기본값 설정
          document.getElementById('userId').textContent = '로드 실패';
          document.getElementById('email').textContent = '로드 실패';
          document.getElementById('nickname').textContent = '로드 실패';
          document.getElementById('regDate').textContent = '로드 실패';
      }
  }

function renderCommunityData(items, containerId, dataType = containerId) {
  const container = document.getElementById(containerId);
  container.innerHTML = "";
  if (!items || items.length === 0) {
    container.innerHTML = '<div class="no-data">데이터가 없습니다.</div>';
    return;
  }
  items.slice(0, 3).forEach(item => {
    const block = createItemCard(item, dataType);
    container.appendChild(block);
  });
}

function createItemCard(item, type) {
  const config = typeConfig[type] || { label: '기타', color: '#6c757d' };

  const title = item.contentTitle || item.title || item.postTitle || item.commentContent || '제목 없음';
  const content = item.contentText || item.description || item.reviewContent || '내용 없음';
  const date = item.writeDate || item.regDate || item.date || '날짜 정보 없음';
  const author = item.writerNickname || item.writer || item.userName || '작성자 정보 없음';

  const card = document.createElement('div');
  card.className = 'item-card';

  card.innerHTML = `
    <div class="item-header">
        <span class="item-type" style="background-color: ${config.color}">${config.label}</span>
        <span class="item-date">${formatDate(date)}</span>
    </div>
    <div class="item-content">
        <div class="item-row">
            <span class="item-label">제목:</span>
            <span class="item-value">${truncateText(title, 30)}</span>
        </div>
        <div class="item-row">
            <span class="item-label">내용:</span>
            <span class="item-value">${truncateText(content, 50)}</span>
        </div>
        <div class="item-row">
            <span class="item-label">작성자:</span>
            <span class="item-value">${author}</span>
        </div>
    </div>
  `;

  return card;
}

  // 텍스트 길이 제한 함수
  function truncateText(text, maxLength) {
      if (!text) return '정보 없음';
      if (text.length <= maxLength) return text;
      return text.substring(0, maxLength) + '...';
  }

  // 날짜 포맷팅 함수
  function formatDate(dateString) {
      if (!dateString) return '날짜 정보 없음';

      try {
          const date = new Date(dateString);
          if (isNaN(date.getTime())) return dateString; // 날짜 파싱 실패 시 원본 반환

          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          return `${year}.${month}.${day}`;
      } catch (e) {
          return dateString;
      }
  }

//  // 회원유형 변경
//  async function updateUserType() {
//      const userType = document.getElementById("userTypeSelect").value;
//      try {
//          const res = await fetch(`/api/admin/user/${userId}/userTypeNo`, {
//              method: 'POST',
//              headers: { 'Content-Type': 'application/json' },
//              body: JSON.stringify({ userTypeNo: parseInt(userType) })
//          });
//          const result = await res.text();
//          alert(result);
//          // 성공 시 화면 업데이트는 다음 데이터 로드 시 자동으로 반영됨
//      } catch (error) {
//          alert("회원유형 변경 실패: " + error.message);
//      }
//  }

 // 기존 코드에서 이 부분이 구문 오류입니다:
 //  / 회원유형 변경  <- 주석이 잘못됨
 //  async function updateUserType() {

 // 올바른 수정:
 // 회원유형 변경
 async function updateUserType() {
     const userType = document.getElementById("userTypeSelect").value;

     // 사용자에게 권한도 변경됨을 알림
     const userTypeNames = {
         "0": "일반회원 (ROLE_USER)",
         "1": "지식제공자 (ROLE_PROVIDER)",
         "2": "지식제공자 요청 (ROLE_USER)",
         "3": "관리자 (ROLE_ADMIN)"
     };

     const confirmMessage = `회원유형을 ${userTypeNames[userType]}로 변경하시겠습니까?\n권한도 자동으로 변경됩니다.`;

     if (!confirm(confirmMessage)) {
         return;
     }

     try {
         const res = await fetch(`/api/admin/user/${userId}/userTypeNo`, {
             method: 'POST',
             headers: { 'Content-Type': 'application/json' },
             body: JSON.stringify({ userTypeNo: parseInt(userType) })
         });
         const result = await res.text();
         alert(result);

         // 성공 시 화면 다시 로드하여 변경사항 반영
         if (res.ok) {
             loadUserData();
         }
     } catch (error) {
         alert("회원유형 변경 실패: " + error.message);
     }
 }

  // 활동유형 변경
  async function updateActivityType() {
      const activityType = document.getElementById("activityTypeSelect").value;
      try {
          const res = await fetch(`/api/admin/user/${userId}/activity`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ activity: parseInt(activityType) })
          });
          const result = await res.text();
          alert(result);
          // 성공 시 화면 업데이트는 다음 데이터 로드 시 자동으로 반영됨
      } catch (error) {
          alert("활동상태 변경 실패: " + error.message);
      }
  }

  // 모달 관련 함수들
  const modal = document.getElementById("modal");
  const modalClose = modal.querySelector(".close-btn");
  const modalContent = document.getElementById("modal-content-dynamic");
  const modalTitle = document.getElementById("modal-title");

  function closeModal() {
      modal.style.display = "none";
  }

  function openModalWithData(items, title) {
      modalContent.innerHTML = "";
      modal.style.display = "flex";
      modalTitle.textContent = `${title} 전체보기`;

      if (!items || items.length === 0) {
          modalContent.innerHTML = '<div class="no-data">데이터가 없습니다.</div>';
          return;
      }

      // 모달에서는 모든 데이터를 카드 형식으로 표시
      const typeMap = {
          '게시글': 'posts',
          '댓글': 'comments',
          '대댓글': 'replies',
          '수강평': 'reviews',
          '강의요청': 'lectureRequests'
      };

      const dataType = typeMap[title] || 'lectureRequests';

      items.forEach(item => {
          const block = createItemCard(item, dataType);
          modalContent.appendChild(block);
      });
  }

  // 이벤트 리스너 설정
  document.addEventListener("DOMContentLoaded", function() {
      // 초기 데이터 로드
      loadUserData();

      // 필터 select 변경 시 재조회
      document.getElementById("filterSelect").addEventListener("change", () => {
          const selectedFilter = document.getElementById("filterSelect").value;
          loadUserData(selectedFilter);
      });

      // 검색 form 제출 시 필터링 요청
      document.getElementById("searchForm").addEventListener("submit", function(e) {
          e.preventDefault();
          const selectedFilter = document.getElementById("filterSelect").value;
          loadUserData(selectedFilter);
      });

      // 모달 열기 이벤트
      document.querySelectorAll(".open-modal").forEach((btn) => {
          btn.addEventListener("click", function(e) {
              e.preventDefault();
              const dataType = btn.getAttribute('data-type');
              const title = btn.closest('.community-card').querySelector('.title-wrapper span').textContent;

              if (window.lastLoadedData && window.lastLoadedData[dataType]) {
                  openModalWithData(window.lastLoadedData[dataType], title);
              } else {
                  openModalWithData([], title);
              }
          });
      });

      // 모달 닫기 이벤트
      modalClose.addEventListener("click", closeModal);
      window.addEventListener("click", (e) => {
          if (e.target === modal) closeModal();
      });
  });