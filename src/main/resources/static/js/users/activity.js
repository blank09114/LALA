/**
 * 마이페이지 활동 관리 시스템
 *
 * 이 시스템은 사용자의 다양한 활동을 관리하는 통합 인터페이스를 제공합니다:
 * - 커뮤니티 활동: 자유글, 스터디, 질문&답변 게시글 관리
 * - 수강평 관리: 작성한 강의 리뷰 조회 및 수정
 * - 페이지네이션: 효율적인 데이터 탐색을 위한 페이지 분할
 * - 필터링: 다양한 조건으로 활동 내역 필터링
 *
 * 주요 기능:
 * 1. 탭 기반 네비게이션 (자유/스터디/질문&답변/수강평)
 * 2. 커뮤니티 활동 필터링 (작성한 글/답변한 글/삭제된 글/신고한 글)
 * 3. 서버/클라이언트 사이드 페이지네이션 지원
 * 4. 리뷰 수정 기능 (별점 및 내용 수정)
 * 5. 실시간 데이터 로딩 및 에러 처리
 */

// ==================== 전역 상태 관리 ====================

/**
 * 애플리케이션 전역 상태 객체
 *
 * 모든 UI 상태와 데이터를 중앙에서 관리하여 일관성 있는 상태 관리를 제공합니다.
 * 각 섹션별로 독립적인 상태를 유지하면서도 전체적인 앱 상태를 통합 관리합니다.
 */
const AppState = {
    // 커뮤니티 관련 상태
    community: {
        postType: 'free',          // 현재 선택된 게시글 타입 (free, study, qa)
        content: 'written',        // 컨텐츠 필터 (written, answered, deleted, reported)
        currentPage: 1,            // 현재 페이지 번호
        pageSize: 5,               // 페이지당 항목 수
        totalPages: 0,             // 전체 페이지 수
        totalElements: 0           // 전체 항목 수
    },

    // 현재 활성 탭 (free, study, qa, review)
    currentTab: 'free',

    // 리뷰 관련 상태
    review: {
        selectedReviewId: null,    // 수정 중인 리뷰 ID
        selectedStars: 0,          // 선택된 별점 (1-5)
        currentPage: 1,            // 현재 페이지 번호
        pageSize: 3,               // 페이지당 항목 수
        totalPages: 0,             // 전체 페이지 수
        totalElements: 0           // 전체 항목 수
    }
};

/**
 * 디바운스 타이머 - API 호출 최적화를 위해 사용
 * 연속적인 사용자 입력에 대해 마지막 입력 후 일정 시간이 지난 후에만 API를 호출하여
 * 서버 부하를 줄이고 성능을 향상시킵니다.
 */
let debounceTimer;

// ==================== 초기화 ====================

/**
 * DOM 로딩 완료 시 페이지 초기화
 */
document.addEventListener('DOMContentLoaded', function () {
    initializePage();
});

/**
 * 페이지 전체 초기화 함수
 *
 * 애플리케이션의 모든 초기 설정을 담당합니다:
 * - 이벤트 리스너 설정
 * - 기본 필터 상태 초기화
 * - 리뷰 모달 초기화
 * - 기본 탭 설정 및 데이터 로드
 */
function initializePage() {
    setupEventListeners();
    initializeFilters();
    initializeReviewModal();
    switchTab('free'); // 기본적으로 자유 탭으로 설정
}

/**
 * 이벤트 리스너 설정
 *
 * 사용자 인터랙션을 처리하는 모든 이벤트 리스너를 설정합니다:
 * - 탭 클릭 이벤트 (메인 네비게이션)
 * - 필터 클릭 이벤트 (커뮤니티 활동 필터링)
 * - 동적 이벤트 위임을 통한 효율적인 이벤트 처리
 */
function setupEventListeners() {
    // 메인 탭 클릭 이벤트 (자유, 스터디, 질문&답변, 수강평)
    document.querySelectorAll('.main-tab').forEach(tab => {
        tab.addEventListener('click', function () {
            const tabValue = this.getAttribute('data-tab');
            switchTab(tabValue);
        });
    });

    // 커뮤니티 필터 클릭 이벤트 (작성한 글, 답변한 글, 삭제된 글, 신고한 글)
    // 이벤트 위임을 사용하여 동적으로 생성되는 요소들도 처리
    document.addEventListener('click', function (e) {
        if (e.target.classList.contains('filter-option') && AppState.currentTab !== 'review') {
            const filterType = e.target.getAttribute('data-filter-type');
            const value = e.target.getAttribute('data-value');
            setActiveFilter(filterType, value);
        }
    });
}

// ==================== 탭 관리 ====================

/**
 * 탭 전환 함수
 * 선택된 탭에 따라 UI를 변경하고 해당 데이터를 로드
 *
 * @param {string} tabValue - 전환할 탭 값 (free, study, qa, review)
 */
function switchTab(tabValue) {
    // 탭 UI 업데이트
    updateTabUI(tabValue);

    AppState.currentTab = tabValue;

    if (tabValue === 'review') {
        // 리뷰 탭 활성화
        showReviewSection();
        if (AppState.currentTab !== 'review') {
            AppState.review.currentPage = 1;
        }
        loadReviews();
    } else {
        // 커뮤니티 탭 활성화 (자유, 스터디, 질문&답변)
        showCommunitySection();
        AppState.community.postType = tabValue;
        AppState.community.currentPage = 1; // 탭 변경 시 첫 페이지로 초기화
        initializeFilters();
        loadCommunityData();
    }
}

/**
 * 탭 UI 업데이트
 * @param {string} activeTab - 활성화할 탭
 */
function updateTabUI(activeTab) {
    document.querySelectorAll('.main-tab').forEach(tab => {
        tab.classList.toggle('active', tab.getAttribute('data-tab') === activeTab);
    });
}

/**
 * 커뮤니티 섹션 표시
 */
function showCommunitySection() {
    document.getElementById('communityFilters').style.display = 'flex';
    document.getElementById('postList').style.display = 'block';
    document.getElementById('reviewSection').style.display = 'none';
    document.getElementById('pagination').style.display = 'block';
}

/**
 * 리뷰 섹션 표시
 */
function showReviewSection() {
    document.getElementById('communityFilters').style.display = 'none';
    document.getElementById('postList').style.display = 'none';
    document.getElementById('reviewSection').style.display = 'block';
    document.getElementById('pagination').style.display = 'none';
    document.getElementById('reviewPagination').style.display = 'block';
}

// ==================== 필터 관리 ====================

/**
 * 커뮤니티 필터 초기화
 * 기본 필터 상태로 설정
 */
function initializeFilters() {
    setActiveFilter('content', AppState.community.content);
}

/**
 * 필터 설정 및 데이터 새로고침
 *
 * @param {string} filterType - 필터 타입 (content)
 * @param {string} value - 필터 값 (written, answered, deleted, reported)
 */
function setActiveFilter(filterType, value) {
    AppState.community[filterType] = value;
    AppState.community.currentPage = 1; // 필터 변경 시 첫 페이지로 초기화
    updateFilterUI(filterType, value);

    // 디바운스를 적용하여 API 호출 최적화
    debounce(() => loadCommunityData(), 300);
}

/**
 * 필터 UI 업데이트
 *
 * @param {string} filterType - 필터 타입
 * @param {string} value - 선택된 필터 값
 */
function updateFilterUI(filterType, value) {
    const options = document.querySelectorAll(`[data-filter-type="${filterType}"]`);
    options.forEach(option => {
        option.classList.toggle('active', option.getAttribute('data-value') === value);
    });
}

/**
 * 디바운스 함수 - 연속적인 호출을 방지하여 성능 최적화
 *
 * @param {Function} func - 실행할 함수
 * @param {number} delay - 지연 시간 (밀리초)
 */
function debounce(func, delay) {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(func, delay);
}

// ==================== 페이지네이션 ====================

/**
 * 커뮤니티 페이지 이동 함수
 *
 * @param {number} page - 이동할 페이지 번호
 */
function goToPage(page) {
    if (page < 1 || page > AppState.community.totalPages) return;

    AppState.community.currentPage = page;
    loadCommunityData();
}

/**
 * 리뷰 페이지 이동 함수
 *
 * @param {number} page - 이동할 페이지 번호
 */
function goToReviewPage(page) {
    console.log(`리뷰 페이지 이동: ${AppState.review.currentPage} → ${page}`);
    
    if (page < 1 || page > AppState.review.totalPages) {
        console.warn(`유효하지 않은 페이지: ${page} (전체: ${AppState.review.totalPages})`);
        return;
    }

    AppState.review.currentPage = page;
    console.log(`현재 페이지 상태: ${AppState.review.currentPage}`);
    loadReviews();
}

/**
 * 커뮤니티 페이지네이션 UI 렌더링
 *
 * @param {Object} pageData - 페이지 정보 객체
 */
function renderPagination(pageData) {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    if (pageData.totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }

    pagination.style.display = 'flex';

    // 이전 버튼
    const prevBtn = createPaginationButton('&lt;',
        () => goToPage(pageData.currentPage - 1),
        !pageData.hasPrevious
    );
    pagination.appendChild(prevBtn);

    // 페이지 번호 계산 (현재 페이지 ±2 범위)
    const startPage = Math.max(1, pageData.currentPage - 2);
    const endPage = Math.min(pageData.totalPages, pageData.currentPage + 2);

    // 첫 페이지와 점점점 표시
    if (startPage > 1) {
        pagination.appendChild(createPaginationButton('1', () => goToPage(1)));

        if (startPage > 2) {
            pagination.appendChild(createPaginationDots());
        }
    }

    // 페이지 번호 버튼들
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = createPaginationButton(
            i.toString(),
            () => goToPage(i),
            false,
            i === pageData.currentPage
        );
        pagination.appendChild(pageBtn);
    }

    // 마지막 페이지와 점점점 표시
    if (endPage < pageData.totalPages) {
        if (endPage < pageData.totalPages - 1) {
            pagination.appendChild(createPaginationDots());
        }

        pagination.appendChild(createPaginationButton(
            pageData.totalPages.toString(),
            () => goToPage(pageData.totalPages)
        ));
    }

    // 다음 버튼
    const nextBtn = createPaginationButton('&gt;',
        () => goToPage(pageData.currentPage + 1),
        !pageData.hasNext
    );
    pagination.appendChild(nextBtn);
}

/**
 * 리뷰 페이지네이션 UI 렌더링 (수정 버전)
 *
 * @param {Object} pageData - 페이지 정보 객체
 */
function renderReviewPagination(pageData) {
    const pagination = document.getElementById('reviewPagination');
    pagination.innerHTML = '';

    if (pageData.totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }

    pagination.style.display = 'flex';
    
    // ✅ 현재 페이지 정보 확인
    const currentPage = AppState.review.currentPage;
    const totalPages = pageData.totalPages;
    
    console.log(`페이지네이션 렌더링 - 현재: ${currentPage}, 전체: ${totalPages}`);

    // 이전 버튼
    const prevBtn = createPaginationButton('&lt;',
        () => goToReviewPage(currentPage - 1),
        currentPage <= 1  // ✅ AppState 값 사용
    );
    pagination.appendChild(prevBtn);

    // 페이지 번호 계산 (현재 페이지 ±2 범위)
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    // 첫 페이지와 점점점 표시
    if (startPage > 1) {
        pagination.appendChild(createPaginationButton('1', () => goToReviewPage(1)));

        if (startPage > 2) {
            pagination.appendChild(createPaginationDots());
        }
    }

    // 페이지 번호 버튼들
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = createPaginationButton(
            i.toString(),
            () => goToReviewPage(i),
            false,
            i === currentPage  // ✅ AppState 값 사용
        );
        pagination.appendChild(pageBtn);
    }

    // 마지막 페이지와 점점점 표시
    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            pagination.appendChild(createPaginationDots());
        }

        pagination.appendChild(createPaginationButton(
            totalPages.toString(),
            () => goToReviewPage(totalPages)
        ));
    }

    // 다음 버튼
    const nextBtn = createPaginationButton('&gt;',
        () => goToReviewPage(currentPage + 1),
        currentPage >= totalPages  // ✅ AppState 값 사용
    );
    pagination.appendChild(nextBtn);
}

/**
 * 페이지네이션 버튼 생성
 *
 * @param {string} text - 버튼 텍스트
 * @param {Function} clickHandler - 클릭 핸들러
 * @param {boolean} disabled - 비활성화 여부
 * @param {boolean} active - 활성 상태 여부
 * @return {HTMLButtonElement} 생성된 버튼 엘리먼트
 */
function createPaginationButton(text, clickHandler, disabled = false, active = false) {
    const button = document.createElement('button');
    button.innerHTML = text;
    button.disabled = disabled;
    if (active) button.className = 'active';
    if (clickHandler) button.onclick = clickHandler;
    return button;
}

/**
 * 페이지네이션 점점점 요소 생성
 *
 * @return {HTMLSpanElement} 점점점 요소
 */
function createPaginationDots() {
    const dots = document.createElement('span');
    dots.textContent = '...';
    dots.className = 'pagination-dots';
    return dots;
}

// ==================== 데이터 로딩 ====================

/**
 * 커뮤니티 데이터 로드 (메인 함수)
 * 현재 상태에 따라 일반 커뮤니티 데이터 또는 신고한 글 데이터를 로드
 */
async function loadCommunityData() {
    const postList = document.getElementById("postList");
    const loading = document.getElementById("loading");

    try {
        showLoading(true);
        postList.innerHTML = "";

        if (AppState.community.content === 'reported') {
            await loadReportedData();
        } else {
            await loadRegularCommunityData();
        }
    } catch (error) {
        handleDataLoadError(error, postList);
    } finally {
        showLoading(false);
    }
}

/**
 * 일반 커뮤니티 데이터 로드
 */
async function loadRegularCommunityData() {
    const requestData = createCommunityRequestData();

    const response = await fetchWithErrorHandling('/users/mypage/community', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(requestData)
    });

    const pageData = await response.json();
    console.log(pageData)
    handleSuccessfulDataLoad(pageData);
}

/**
 * 신고한 글 데이터 로드
 */
async function loadReportedData() {
    const requestData = {
        postType: getPostTypeArray(),
        page: AppState.community.currentPage,
        size: AppState.community.pageSize
    };

    const response = await fetchWithErrorHandling('/users/mypage/reported', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(requestData)
    });

    const pageData = await response.json();
    handleSuccessfulDataLoad(pageData);
}

/**
 * 커뮤니티 요청 데이터 생성
 *
 * @return {Object} API 요청에 사용할 데이터 객체
 */
function createCommunityRequestData() {
    return {
        postType: getPostTypeArray(),
        contentCheck: getContentCheck(),
        deleted: getDeletedCheck(),
        pageRequest: {
            page: AppState.community.currentPage,
            size: AppState.community.pageSize
        }
    };
}

/**
 * 성공적인 데이터 로드 처리
 *
 * @param {Object} pageData - 서버에서 받은 페이지 데이터
 */
function handleSuccessfulDataLoad(pageData) {
    // 페이지네이션 상태 업데이트
    AppState.community.totalPages = pageData.totalPages;
    AppState.community.totalElements = pageData.totalElements;

    // 데이터 렌더링
    renderPosts(pageData.content);
    renderPagination(pageData);
}

/**
 * 데이터 로드 에러 처리
 *
 * @param {Error} error - 발생한 에러
 * @param {HTMLElement} postList - 게시글 리스트 컨테이너
 */
function handleDataLoadError(error, postList) {
    console.error('데이터 로딩 에러:', error);
    postList.innerHTML = `<p>오류 발생: ${error.message}</p>`;
    document.getElementById('pagination').style.display = 'none';
}

/**
 * 로딩 상태 표시/숨김
 *
 * @param {boolean} show - 로딩 표시 여부
 */
function showLoading(show) {
    document.getElementById("loading").style.display = show ? "block" : "none";
}

/**
 * 에러 핸들링이 포함된 fetch 함수
 *
 * @param {string} url - 요청 URL
 * @param {Object} options - fetch 옵션
 * @return {Promise<Response>} fetch 응답
 */
async function fetchWithErrorHandling(url, options) {
    const response = await fetch(url, options);
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response;
}

// ==================== 데이터 렌더링 ====================

/**
 * 게시글 목록 렌더링
 *
 * @param {Array} data - 게시글 데이터 배열
 */
function renderPosts(data) {
    const postList = document.getElementById("postList");

    if (!data || data.length === 0) {
        postList.innerHTML = "<span>게시글이 없습니다.</span>"
        "<p>이후 활동 내역이 있을 때 확인 가능해요!</p>"
        "<button>커뮤니티 둘러보기</button>";
        return;
    }

    postList.innerHTML = "";
    data.forEach(post => {
        const postElement = createPostElement(post);
        postList.appendChild(postElement);
    });
}

/**
 * 개별 게시글 엘리먼트 생성
 *
 * @param {Object} post - 게시글 데이터
 * @return {HTMLDivElement} 생성된 게시글 엘리먼트
 */
function createPostElement(post) {
    const postElement = document.createElement("div");
    postElement.className = "post";

    // ✅ 게시글 클릭 시 상세 페이지로 이동
    postElement.addEventListener("click", () => {
        if (post.postId) {
            location.href = `/auth/community/boardDetails/${post.postId}`;
        }
    });

    const statusBadge = post.status === '미해결'
        ? '<span class="badge">미해결</span>'
        : '';

    // HTML 태그를 제거하고 순수 텍스트만 추출하는 함수
    function stripHtml(html) {
        if (!html) return '';
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;
        let text = tempDiv.textContent || tempDiv.innerText || '';
        
        // @C058:: 같은 형식의 텍스트 제거 (댓글 ID나 사용자 식별자)
        text = text.replace(/@[A-Z]\d+::/g, '');
        
        return text;
    }

    postElement.innerHTML = `
        <div class="title">${post.title || '제목 없음'} ${statusBadge}</div>
        <div class="content">${stripHtml(post.content) || '내용 없음'}</div>
        <div class="info-row">
            <div class="time">${post.nickname || '작성자'} - ${post.date || '방금 전'}</div>
            <div class="icons">
            <i class="fa-regular fa-heart"></i> ${post.like || 0}
            <i class="fa-regular fa-eye"></i> ${post.view || 0}
            <i class="fa-regular fa-comment"></i> ${post.comment || 0}</div>
        </div>
    `;

    return postElement;
}

// ==================== 데이터 변환 함수 ====================

/**
 * 현재 게시글 타입에 해당하는 배열 반환
 *
 * @return {Array<number>} 게시글 타입 ID 배열
 */
function getPostTypeArray() {
    const typeMap = {
        'free': [2],        // 자유 게시판
        'study': [3, 4],    // 스터디 게시판
        'qa': [5, 6]        // 질문&답변 게시판
    };
    return typeMap[AppState.community.postType] || [];
}

/**
 * 컨텐츠 체크 값 반환 (작성한 글 여부)
 *
 * @return {boolean} 작성한 글 여부
 */
function getContentCheck() {
    return AppState.community.content === 'written';
}

/**
 * 삭제된 글 체크 값 반환
 *
 * @return {boolean} 삭제된 글 여부
 */
function getDeletedCheck() {
    return AppState.community.content === 'deleted';
}

// ==================== 리뷰 관리 ====================

/**
 * 리뷰 데이터 로드 함수 (수정 버전)
 */
async function loadReviews() {
    const reviewList = document.getElementById("reviewList");
    const noReviews = document.getElementById("noReviews");
    const reviewPagination = document.getElementById("reviewPagination");
    
    try {
        console.log(`리뷰 데이터 로드 시작 - 페이지: ${AppState.review.currentPage}`);
        
        // 로딩 표시
        reviewList.innerHTML = '<p>리뷰를 불러오는 중...</p>';
        noReviews.style.display = 'none';
        
        const requestData = {
            page: AppState.review.currentPage,
            size: AppState.review.pageSize
        };
        
        const response = await fetch('/users/mypage/reviews', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const pageData = await response.json();
        console.log('리뷰 데이터 응답:', pageData);
        
        // ✅ 페이지 정보 업데이트 (중요: 서버 응답으로 덮어쓰지 않도록)
        AppState.review.totalPages = pageData.totalPages || 0;
        AppState.review.totalElements = pageData.totalElements || 0;
        
        // 리뷰 렌더링
        if (pageData.content && pageData.content.length > 0) {
            renderReviews(pageData.content);
            noReviews.style.display = 'none';
            renderReviewPagination(pageData);
        } else {
            reviewList.innerHTML = '';
            noReviews.style.display = 'block';
            reviewPagination.innerHTML = '';
        }
        
        console.log(`리뷰 로드 완료 - 현재 페이지: ${AppState.review.currentPage}`);
        
    } catch (error) {
        console.error('리뷰 데이터 로딩 에러:', error);
        reviewList.innerHTML = '<p>리뷰를 불러오는 중 오류가 발생했습니다.</p>';
        noReviews.style.display = 'none';
        reviewPagination.innerHTML = '';
    }
}

/**
 * 리뷰 목록 렌더링
 *
 * @param {Array} reviews - 리뷰 데이터 배열
 */
function renderReviews(reviews) {
    const reviewList = document.getElementById('reviewList');

    const reviewsHtml = reviews.map(review => createReviewCardHTML(review)).join('');
    reviewList.innerHTML = reviewsHtml;
}

/**
 * 리뷰 카드 HTML 생성
 *
 * @param {Object} review - 리뷰 데이터
 * @return {string} 생성된 HTML 문자열
 */
function createReviewCardHTML(review) {
    const stars = generateStars(review.rate);
    const thumbnail = review.thumbnail || '/images/default-thumbnail.png';
    const formattedDate = formatDate(review.writeDate);

    return `
        <div class="review-card" data-review-id="${review.reviewId}">
            <img src="${thumbnail}" alt="강의 썸네일" />
            <div class="review-info" data-id="${review.lectureId}" onclick="goToDetail(this)">
                <p class="rating">${stars}</p>
                <p class="title">${escapeHtml(review.lectureTitle)}</p>
                <span class="review_date">${formattedDate}</span><br>
                <p class="desc">${escapeHtml(review.content)}</p>
            </div>
            <div class="btn" onclick="openModal(this)">수정</div>
        </div>
    `;
}

function goToDetail(element) {
    const id = element.getAttribute("data-id");
    if (!id) return;
    window.location.href = "/course/detail/" + id;
}

/**
 * 별점 HTML 생성
 *
 * @param {number} rate - 평점 (1-10 스케일)
 * @return {string} 별점 HTML
 */
function generateStars(rate) {
    const starCount = Math.round(rate / 2); // 10점 만점을 5점 만점으로 변환
    let stars = '';

    for (let i = 1; i <= 5; i++) {
        stars += i <= starCount ? '<span>★</span>' : '<span>☆</span>';
    }

    return stars;
}

/**
 * 날짜 포맷팅
 *
 * @param {string} dateString - 날짜 문자열
 * @return {string} 포맷된 날짜 문자열
 */
function formatDate(dateString) {
    if (!dateString) return '';

    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

/**
 * HTML 특수문자 이스케이프
 * XSS 공격 방지를 위한 보안 함수
 *
 * @param {string} unsafe - 이스케이프할 문자열
 * @return {string} 이스케이프된 문자열
 */
function escapeHtml(unsafe) {
    if (!unsafe) return '';

    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// ==================== 리뷰 모달 관리 ====================

/**
 * 리뷰 모달 초기화
 * 별점 클릭 이벤트 설정
 */
function initializeReviewModal() {
    document.querySelectorAll(".star").forEach(star => {
        star.addEventListener("click", function () {
            AppState.review.selectedStars = parseInt(this.getAttribute("data-value"));
            highlightStars(AppState.review.selectedStars);
        });
    });
}

/**
 * 리뷰 수정 모달 열기
 *
 * @param {HTMLElement} el - 클릭된 수정 버튼 엘리먼트
 */
function openModal(el) {
    const card = el.closest(".review-card");
    const reviewId = card.getAttribute("data-review-id");
    
    console.log('모달 열기 - 카드:', card);
    console.log('리뷰 ID:', reviewId);
    
    AppState.review.selectedReviewId = reviewId;

    // 현재 리뷰 데이터 추출
    const content = card.querySelector(".desc").innerText.trim();
    const currentStars = extractCurrentStarRating(card);
    
    console.log('추출된 데이터 - 내용:', content, '별점:', currentStars);

    // 모달에 현재 데이터 설정
    document.getElementById("review-text").value = content;
    AppState.review.selectedStars = currentStars;
    highlightStars(AppState.review.selectedStars);

    // 모달 표시
    document.getElementById("review-modal").style.display = "flex";
}

/**
 * 현재 별점 추출
 *
 * @param {HTMLElement} card - 리뷰 카드 엘리먼트
 * @return {number} 현재 별점
 */
function extractCurrentStarRating(card) {
    const ratingElement = card.querySelector(".rating");
    return Array.from(ratingElement.querySelectorAll('span'))
        .filter(span => span.textContent === '★').length;
}

/**
 * 별점 하이라이트 표시
 *
 * @param {number} count - 하이라이트할 별의 개수
 */
function highlightStars(count) {
    document.querySelectorAll(".star").forEach(star => {
        const starValue = parseInt(star.getAttribute("data-value"));
        star.style.color = starValue <= count ? "#f1c40f" : "#ccc";
    });
}

/**
 * 리뷰 모달 닫기
 */
function closeModal() {
    document.getElementById("review-modal").style.display = "none";
    resetModalState();
}

/**
 * 모달 상태 초기화
 */
function resetModalState() {
    AppState.review.selectedReviewId = null;
    AppState.review.selectedStars = 0;
    document.getElementById("review-text").value = '';
    highlightStars(0);
}

/**
 * 리뷰 저장
 * 수정된 리뷰 데이터를 서버에 전송
 */
async function saveReview() {
    const content = document.getElementById("review-text").value.trim();

    // 입력 유효성 검사
    if (!validateReviewInput(content)) {
        return;
    }

    // ✅ 별점을 10점 스케일로 변환 (5점 → 10점)
    const rateInTenScale = AppState.review.selectedStars * 2;

    const reviewData = {
        reviewId: AppState.review.selectedReviewId,
        rate: rateInTenScale,  // 10점 스케일로 변환
        content: content
    };

    console.log('리뷰 수정 요청 데이터:', reviewData);

    try {
        const response = await fetchWithErrorHandling("/users/mypage/review", {
            method: "PUT",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(reviewData)
        });

        const data = await response.json();
        console.log('서버 응답:', data);

        if (response.ok && data.status === "success") {
            alert("리뷰가 수정되었습니다.");
            closeModal();
            // 현재 페이지 유지하면서 리뷰 목록 새로고침
            loadReviews();
        } else {
            alert("수정 실패: " + (data.message || "알 수 없는 오류"));
        }
    } catch (error) {
        console.error('리뷰 수정 실패:', error);
        alert("오류 발생: " + error.message);
    }
}

/**
 * 리뷰 입력 유효성 검사
 *
 * @param {string} content - 리뷰 내용
 * @return {boolean} 유효성 검사 결과
 */
function validateReviewInput(content) {
    if (AppState.review.selectedReviewId == null) {
        alert("리뷰를 선택해주세요.");
        return false;
    }

    if (AppState.review.selectedStars == 0) {
        alert("별점을 선택해주세요.");
        return false;
    }

    if (content === "") {
        alert("리뷰 내용을 입력해주세요.");
        return false;
    }

    return true;
}

// ==================== 레거시 함수 (하위 호환성) ====================

/**
 * @deprecated loadCommunityData() 사용 권장
 */
function getData() {
    loadCommunityData();
}

/**
 * @deprecated loadReportedData() 사용 권장
 */
function getReportedData() {
    return loadReportedData();
}