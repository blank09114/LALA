const lectures = [
  "자바의 정석",
  "자바스크립트 입문",
  "파이썬 완전정복",
  "스프링부트 강좌",
  "자바 자료구조",
  "리액트로 만드는 쇼핑몰",
  "데이터베이스 기초",
  "코틀린 앱 개발",
];


document.getElementById("searchInput").addEventListener("input", function(e) {
  const query = e.target.value.trim().toLowerCase();
  const suggestionBox = document.getElementById("searchSuggestions");

  if (!query) {
    suggestionBox.innerHTML = "";
    return;
  }

  const matches = lectures.filter(title => title.toLowerCase().includes(query)).slice(0, 5);

  if (matches.length > 0) {
    suggestionBox.innerHTML = `
      <div class="search-header">‘${query}’ 검색 결과</div>
      <ul>
        ${matches.map(match => `<li>${match}</li>`).join("")}
      </ul>
    `;
  } else {
    suggestionBox.innerHTML = `<div class="search-header">‘${query}’와(과) 관련된 강의를 찾을 수 없습니다.</div>`;
  }
});


function performSearch() {
  const query = document.getElementById("searchInput").value.trim();
  if (query) {
    window.location.href = `/course/mainLectureList?keyword=${encodeURIComponent(query)}`;
  }
}

document.getElementById("searchInput").addEventListener("keydown", function(e) {
  if (e.key === "Enter") {
    performSearch();
  }
});


//프로필 이미지 불러오기
function loadProfileImage() {
  // 프로필 이미지 요소 찾기 (여러 가능한 선택자 시도)
  let profileImg = document.getElementById('profileImg');
  
  if (!profileImg) {
    // profileImg ID가 없으면 다른 선택자 시도
    profileImg = document.querySelector('.profile_btn img');
  }
  
  if (!profileImg) {
    // profile_btn 클래스도 없으면 header_actions 내의 img 찾기
    const headerActions = document.querySelector('.header_actions');
    if (headerActions) {
      profileImg = headerActions.querySelector('img');
    }
  }
  
  // 프로필 이미지 요소가 없으면 함수 종료
  if (!profileImg) {
    console.log('프로필 이미지 요소를 찾을 수 없습니다. 페이지에 프로필 이미지가 없을 수 있습니다.');
    return;
  }

  console.log('프로필 이미지 요소 찾음:', profileImg);

  // 이미 로딩 중이면 중복 실행 방지
  if (profileImg.dataset.loading === 'true') {
    console.log('프로필 이미지 로딩 중...');
    return;
  }

  // 로딩 상태 표시
  profileImg.dataset.loading = 'true';
  profileImg.style.opacity = '0.5';

  fetch(`/users/mypage/profile`, {
    method: 'GET',
    headers: {
      'Cache-Control': 'no-cache',
      'Pragma': 'no-cache'
    }
  })
  .then(res => {
    if (!res.ok) {
      throw new Error(`프로필 이미지 조회 실패: ${res.status}`);
    }
    return res.json();
  })
  .then(data => {
    console.log('프로필 이미지 데이터:', data);
    
    const profilename = data?.profilename;
    
    if (profilename && profilename.trim() !== '') {
      // 캐시 방지를 위한 타임스탬프 추가
      const timestamp = new Date().getTime();
      
      if (profilename.startsWith('http')) {
        // 절대 경로인 경우 (S3 URL 등)
        const separator = profilename.includes('?') ? '&' : '?';
        const imageUrl = `${profilename}${separator}t=${timestamp}`;
        
        // 이미지 로드 이벤트 추가
        profileImg.onload = function() {
          console.log('프로필 이미지 로드 성공:', imageUrl);
          profileImg.style.opacity = '1';
          profileImg.dataset.loading = 'false';
        };
        
        profileImg.onerror = function() {
          console.error('프로필 이미지 로드 실패:', imageUrl);
          profileImg.src = '/img/profileImg.png';
          profileImg.style.opacity = '1';
          profileImg.dataset.loading = 'false';
        };
        
        profileImg.src = imageUrl;
      } else {
        // 상대 경로인 경우
        const imageUrl = `/upload/profile/${profilename}?t=${timestamp}`;
        
        // 이미지 로드 이벤트 추가
        profileImg.onload = function() {
          console.log('프로필 이미지 로드 성공:', imageUrl);
          profileImg.style.opacity = '1';
          profileImg.dataset.loading = 'false';
        };
        
        profileImg.onerror = function() {
          console.error('프로필 이미지 로드 실패:', imageUrl);
          profileImg.src = '/img/profileImg.png';
          profileImg.style.opacity = '1';
          profileImg.dataset.loading = 'false';
        };
        
        profileImg.src = imageUrl;
      }
    } else {
      // 프로필 이미지가 없거나 빈 문자열인 경우 기본 이미지 설정
      console.log('프로필 이미지가 없어 기본 이미지 사용');
      profileImg.src = '/img/profileImg.png';
      profileImg.style.opacity = '1';
      profileImg.dataset.loading = 'false';
    }
  })
  .catch(err => {
    console.error('프로필 이미지 불러오기 실패:', err);
    
    // 에러 시 기본 이미지 설정
    if (profileImg) {
      profileImg.src = '/img/profileImg.png';
      profileImg.style.opacity = '1';
      profileImg.dataset.loading = 'false';
    }
  });
}

// 페이지 로드 시 프로필 이미지 불러오기 (조건부 실행)
function initProfileImage() {
  // 헤더가 있는 페이지에서만 실행
  if (document.querySelector('.header_actions') || document.getElementById('profileImg')) {
    loadProfileImage();
  }
}

// 페이지 로드 시 프로필 이미지 불러오기
document.addEventListener('DOMContentLoaded', function() {
  initProfileImage();
});

// 페이지가 완전히 로드된 후에도 한 번 더 시도 (백업)
window.addEventListener('load', function() {
  // DOMContentLoaded에서 이미 실행되었지만, 혹시 모르니 다시 시도
  setTimeout(initProfileImage, 100);
});

// 페이지 이동 시에도 프로필 이미지 업데이트 (SPA 대응)
if (typeof window !== 'undefined') {
  // 페이지 이동 감지 (History API 사용)
  const originalPushState = history.pushState;
  const originalReplaceState = history.replaceState;
  
  history.pushState = function() {
    originalPushState.apply(this, arguments);
    setTimeout(initProfileImage, 100);
  };
  
  history.replaceState = function() {
    originalReplaceState.apply(this, arguments);
    setTimeout(initProfileImage, 100);
  };
  
  window.addEventListener('popstate', function() {
    setTimeout(initProfileImage, 100);
  });
}


