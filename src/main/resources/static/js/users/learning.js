/**
 * 학습 관리 페이지 JavaScript 모듈
 * - 필터 버튼을 통한 강의 목록 조회 및 표시
 * - 수강평 작성 모달 기능
 * - 강의 카드 동적 렌더링 및 상호작용
 * - 페이지네이션 기능
 */

// 상수 정의
const FILTER_TYPES = {
    "학습 중": 0,
    "학습 완료": 1,
    "수강평 작성 가능": 2
};

const PROGRESS_STATUS = {
    LEARNING: 'learning',
    COMPLETED: 'completed',
    REVIEWABLE: 'reviewable'
};

const API_ENDPOINTS = {
    USER_LECTURE: '/users/mypage/user-lecture',
    REVIEW: '/users/mypage/review'
};

const MESSAGES = {
    NO_LECTURES: '해당하는 강의가 없습니다.',
    API_ERROR: '강의 데이터를 불러오는데 실패했습니다.',
    RATING_REQUIRED: '별점을 선택해주세요.',
    REVIEW_MIN_LENGTH: '후기를 5자 이상 입력해주세요.',
    REVIEW_SUCCESS: '리뷰가 성공적으로 등록되었습니다!',
    REVIEW_ERROR: '리뷰 등록에 실패했습니다. 다시 시도해주세요.'
};

// 페이지네이션 관련 상수
const PAGINATION_CONFIG = {
    DEFAULT_PAGE_SIZE: 6,
    MAX_PAGE_SIZE: 50,
    VISIBLE_PAGE_COUNT: 5
};

// 전역 변수
let currentLectureId = null;
let selectedRating = 0;
let currentPage = 1;
let currentPageSize = PAGINATION_CONFIG.DEFAULT_PAGE_SIZE;
let totalLectures = 0;
let totalPages = 0;
let currentFilterType = 0;
let allLectures = []; // 전체 강의 데이터 캐시

/**
 * DOM 로드 완료 시 초기화 함수 실행
 */
document.addEventListener("DOMContentLoaded", () => {
    initializePage();
});

/**
 * 페이지 초기화 함수
 * - 필터 버튼 이벤트 등록
 * - 초기 데이터 로드
 * - 모달 관련 이벤트 등록
 */
function initializePage() {
    const buttons = document.querySelectorAll(".filter-buttons button");
    const lectureListContainer = document.querySelector(".lecture-list");

    // 필터 버튼 이벤트 등록
    setupFilterButtons(buttons);

    // 초기 "학습 중" 버튼 활성화 및 데이터 로드
    initializeDefaultFilter(buttons);

    // 수강평 모달 이벤트 등록
    setupReviewModal();

    // 페이지네이션 컨테이너 추가
    addPaginationContainer();
}

/**
 * 페이지네이션 컨테이너를 DOM에 추가
 */
function addPaginationContainer() {
    const lectureListContainer = document.querySelector(".lecture-list");
    if (lectureListContainer && !document.querySelector(".pagination")) {
        const paginationContainer = document.createElement('div');
        paginationContainer.className = 'pagination';
        lectureListContainer.parentNode.insertBefore(paginationContainer, lectureListContainer.nextSibling);
    }
}

/**
 * 필터 버튼 이벤트 설정
 * @param {NodeList} buttons - 필터 버튼 노드 리스트
 */
function setupFilterButtons(buttons) {
    buttons.forEach((button) => {
        button.addEventListener("click", () => {
            handleFilterButtonClick(buttons, button);
        });
    });
}

/**
 * 필터 버튼 클릭 처리
 * @param {NodeList} buttons - 모든 필터 버튼
 * @param {Element} clickedButton - 클릭된 버튼
 */
function handleFilterButtonClick(buttons, clickedButton) {
    // 모든 버튼의 active 상태 제거
    buttons.forEach(btn => btn.classList.remove("active"));

    // 클릭된 버튼을 active로 설정
    clickedButton.classList.add("active");

    const filterText = clickedButton.innerText.trim();
    const filterType = FILTER_TYPES[filterText];

    if (filterType !== undefined) {
        currentFilterType = filterType;
        currentPage = 1; // 필터 변경 시 첫 페이지로 리셋
        loadUserLectures(filterType);
    }
}

/**
 * 초기 필터 설정 (기본값: "학습 중")
 * @param {NodeList} buttons - 필터 버튼 노드 리스트
 */
function initializeDefaultFilter(buttons) {
    const learningButton = Array.from(buttons).find(btn =>
        btn.innerText.trim() === "학습 중"
    );

    if (learningButton) {
        learningButton.classList.add("active");
        currentFilterType = FILTER_TYPES["학습 중"];
        loadUserLectures(FILTER_TYPES["학습 중"]);
    } else {
        // "학습 중" 버튼이 없을 경우 기본값으로 0 사용
        currentFilterType = 0;
        loadUserLectures(0);
    }
}

/**
 * 사용자 강의 목록 API 호출
 * @param {number} filterType - 필터 타입 (0: 학습중, 1: 학습완료, 2: 수강평 작성 가능)
 */
async function loadUserLectures(filterType) {
    const lectureListContainer = document.querySelector(".lecture-list");

    try {
        const response = await fetch(API_ENDPOINTS.USER_LECTURE, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({filterType: filterType})
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        allLectures = data || [];
        totalLectures = allLectures.length;
        totalPages = Math.ceil(totalLectures / currentPageSize);

        displayCurrentPageLectures();
        renderPagination();

    } catch (error) {
        handleAPIError(error, lectureListContainer);
    }
}

/**
 * 현재 페이지의 강의 목록을 표시
 */
function displayCurrentPageLectures() {
    const startIndex = (currentPage - 1) * currentPageSize;
    const endIndex = Math.min(startIndex + currentPageSize, totalLectures);
    const currentPageLectures = allLectures.slice(startIndex, endIndex);

    displayLectures(currentPageLectures);
}

/**
 * API 에러 처리
 * @param {Error} error - 발생한 에러
 * @param {Element} container - 에러 메시지를 표시할 컨테이너
 */
function handleAPIError(error, container) {
    container.innerHTML = `<p>${MESSAGES.API_ERROR}</p>`;

    // 페이지네이션 숨김
    const paginationContainer = document.querySelector(".pagination");
    if (paginationContainer) {
        paginationContainer.style.display = 'none';
    }
}

/**
 * 강의 목록 화면 렌더링
 * @param {Array} lectures - 강의 데이터 배열
 */
function displayLectures(lectures) {
    const lectureListContainer = document.querySelector(".lecture-list");
    const paginationContainer = document.querySelector(".pagination");

    if (!lectures || lectures.length === 0) {
        lectureListContainer.innerHTML = `<p>${MESSAGES.NO_LECTURES}</p>`;
        if (paginationContainer) {
            paginationContainer.style.display = 'none';
        }
        return;
    }

    const html = lectures.map(lecture => generateLectureCardHTML(lecture)).join('');
    lectureListContainer.innerHTML = html;

    // 페이지네이션 표시
    if (paginationContainer) {
        paginationContainer.style.display = 'flex';
    }

    // 팝업 메뉴 이벤트 재등록
    bindPopupMenuEvents();
}

/**
 * 페이지네이션 렌더링
 */
function renderPagination() {
    const paginationContainer = document.querySelector(".pagination");
    if (!paginationContainer || totalPages <= 1) {
        if (paginationContainer) {
            paginationContainer.style.display = 'none';
        }
        return;
    }

    paginationContainer.style.display = 'flex';
    paginationContainer.innerHTML = '';

    // 이전 페이지 버튼
    const prevButton = createPaginationButton(
        '‹',
        () => goToPage(currentPage - 1),
        currentPage <= 1
    );
    paginationContainer.appendChild(prevButton);

    // 페이지 번호 버튼들
    const {startPage, endPage} = calculatePageRange();

    for (let i = startPage; i <= endPage; i++) {
        const pageButton = createPaginationButton(
            i.toString(),
            () => goToPage(i),
            false,
            i === currentPage
        );
        paginationContainer.appendChild(pageButton);
    }

    // 다음 페이지 버튼
    const nextButton = createPaginationButton(
        '›',
        () => goToPage(currentPage + 1),
        currentPage >= totalPages
    );
    paginationContainer.appendChild(nextButton);
}

/**
 * 페이지 범위 계산 (표시할 페이지 번호들)
 * @returns {Object} startPage와 endPage를 포함한 객체
 */
function calculatePageRange() {
    const halfVisible = Math.floor(PAGINATION_CONFIG.VISIBLE_PAGE_COUNT / 2);
    let startPage = Math.max(1, currentPage - halfVisible);
    let endPage = Math.min(totalPages, startPage + PAGINATION_CONFIG.VISIBLE_PAGE_COUNT - 1);

    // 끝 페이지가 조정된 경우 시작 페이지도 조정
    if (endPage - startPage + 1 < PAGINATION_CONFIG.VISIBLE_PAGE_COUNT) {
        startPage = Math.max(1, endPage - PAGINATION_CONFIG.VISIBLE_PAGE_COUNT + 1);
    }

    return {startPage, endPage};
}

/**
 * 페이지네이션 버튼 생성
 * @param {string} text - 버튼 텍스트
 * @param {Function} clickHandler - 클릭 핸들러
 * @param {boolean} disabled - 비활성화 여부
 * @param {boolean} active - 활성 상태 여부
 * @returns {HTMLButtonElement} 생성된 버튼
 */
function createPaginationButton(text, clickHandler, disabled = false, active = false) {
    const button = document.createElement('button');
    button.innerHTML = text;
    button.disabled = disabled;

    if (active) button.className = 'active';
    if (clickHandler && !disabled) button.onclick = clickHandler;

    return button;
}

/**
 * 특정 페이지로 이동
 * @param {number} page - 이동할 페이지 번호
 */
function goToPage(page) {
    if (page < 1 || page > totalPages || page === currentPage) {
        return;
    }

    currentPage = page;
    displayCurrentPageLectures();
    renderPagination();
}

/**
 * 페이지 크기 변경
 * @param {number} newPageSize - 새로운 페이지 크기
 */
function changePageSize(newPageSize) {
    if (newPageSize < 1 || newPageSize > PAGINATION_CONFIG.MAX_PAGE_SIZE) {
        return;
    }

    currentPageSize = newPageSize;
    currentPage = 1; // 페이지 크기 변경 시 첫 페이지로 리셋
    totalPages = Math.ceil(totalLectures / currentPageSize);

    displayCurrentPageLectures();
    renderPagination();
}

/**
 * 강의 카드 HTML 생성
 * @param {Object} lecture - 강의 객체
 * @returns {string} 강의 카드 HTML 문자열
 */
/**
 * HTML 태그를 제거하고 순수 텍스트만 반환하는 함수
 * @param {string} htmlString - HTML 문자열
 * @returns {string} 순수 텍스트
 */
function stripHtmlTags(htmlString) {
    if (!htmlString) return '';

    // 임시 DOM 요소 생성
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = htmlString;

    // 텍스트만 추출
    return tempDiv.textContent || tempDiv.innerText || '';
}

// generateLectureCardHTML 함수에서 학습 중 카드 부분 수정
function generateLectureCardHTML(lecture) {
    const progressPercent = Math.round(lecture.progressRate * 100);
    const cardStatus = determineCardStatus(progressPercent);
    const progressBar = generateProgressBarHTML(progressPercent, cardStatus);

    // HTML 태그 제거된 순수 텍스트로 변환
    const cleanIntroduction = stripHtmlTags(lecture.introduction);

    // 학습 중일 경우: 간단한 카드만 렌더링
    if (cardStatus === PROGRESS_STATUS.LEARNING) {
        return `
            <div class="lecture-card ${cardStatus}"
                 data-lecture-id="${lecture.lectureId}"
                 onclick="goToDetail('${lecture.lectureId}')"
                 style="cursor: pointer;">
                <img class="thumbnail" src="${lecture.thumbnail || '/images/default_thumbnail.png'}" alt="${lecture.title}" />
                <div class="lecture-info">
                    <h3 class="lecture-title">${lecture.title}</h3>
                    <p class="lecture-teacher">${lecture.nickname}</p>
                    ${cleanIntroduction ? `<p class="lecture-desc">${cleanIntroduction}</p>` : ''}
                    ${progressBar}
                </div>
            </div>
        `;
    }

    // 완료 또는 수강평 가능일 경우: 액션 버튼 포함한 전체 카드 렌더링
    const actionButtons = generateActionButtonsHTML(lecture.lectureId, cardStatus, lecture.hasReview);

    return `
        <div class="lecture-card ${cardStatus}"
             data-lecture-id="${lecture.lectureId}"
             onclick="goToDetail('${lecture.lectureId}')"
             style="cursor: pointer;">
            <img class="thumbnail" src="${lecture.thumbnail || '/images/default_thumbnail.png'}" alt="${lecture.title}" />
            <div class="lecture-info">
                <h3 class="lecture-title">${lecture.title}</h3>
                <p class="lecture-teacher">${lecture.nickname}</p>
                ${cleanIntroduction ? `<p class="lecture-desc">${cleanIntroduction}</p>` : ''}
                ${progressBar}
                ${actionButtons}
            </div>
        </div>
    `;
}


/**
 * 진행률에 따른 카드 상태 결정
 * @param {number} progressPercent - 진행률 (0-100)
 * @returns {string} 카드 상태
 */
function determineCardStatus(progressPercent) {
    if (progressPercent < 100) {
        return PROGRESS_STATUS.LEARNING;
    } else {
        return PROGRESS_STATUS.COMPLETED;
    }
}


/**
 * 진행률 바 HTML 생성
 * @param {number} progressPercent - 진행률
 * @param {string} cardStatus - 카드 상태
 * @returns {string} 진행률 바 HTML
 */
function generateProgressBarHTML(progressPercent, cardStatus) {
    console.log(progressPercent); // 이거 곱하기 100임
    //if (cardStatus === PROGRESS_STATUS.LEARNING) {
    if (progressPercent != null) {
        progressPercent /= 100;
        return `
            <div class="progress-bar-container">
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${progressPercent}%;"></div>
                </div>
                <p class="progress-text">진행률: ${progressPercent}%</p>
            </div>
        `;
    }
    return '';
}

/**
 * 액션 버튼 HTML 생성
 * @param {string} lectureId - 강의 ID
 * @param {string} cardStatus - 카드 상태
 * @param {boolean} hasReview - 수강평 작성 여부
 * @returns {string} 액션 버튼 HTML
 */
function generateActionButtonsHTML(lectureId, cardStatus, hasReview) {
    // 학습 중인 강의는 아무 액션 버튼도 출력하지 않음
    if (cardStatus === PROGRESS_STATUS.LEARNING) {
        return '';
    }

    if (cardStatus === PROGRESS_STATUS.COMPLETED || cardStatus === PROGRESS_STATUS.REVIEWABLE) {
        const reviewButtonText = hasReview ? '수강평 수정하기' : '강의평 작성하기';
        const reviewButtonClass = hasReview ? 'hidden-review-edit' : 'hidden-review';

        return `
            <button class="hidden-menu" onclick="event.stopPropagation()">...</button>
            <div class="popup-menu">
                <button class="${reviewButtonClass}" onclick="event.stopPropagation(); openReviewModal('${lectureId}')">${reviewButtonText}</button>
                <button class="hidden-review" onclick="event.stopPropagation(); goToDetail('${lectureId}')">강의 상세 보기</button>
            </div>
        `;
    }

    return '';
}



/**
 * 팝업 메뉴 이벤트 바인딩
 */
function bindPopupMenuEvents() {
    const menuButtons = document.querySelectorAll('.hidden-menu');

    menuButtons.forEach(button => {
        button.addEventListener('click', handlePopupMenuClick);
    });

    // 외부 클릭시 팝업 닫기
    document.addEventListener('click', closeAllPopupMenus);
}

/**
 * 팝업 메뉴 클릭 처리
 * @param {Event} e - 클릭 이벤트
 */
function handlePopupMenuClick(e) {
    e.stopPropagation();
    const popup = e.target.nextElementSibling;

    // 다른 팝업들 닫기
    document.querySelectorAll('.popup-menu').forEach(menu => {
        if (menu !== popup) {
            menu.style.display = 'none';
        }
    });

    // 현재 팝업 토글
    popup.style.display = popup.style.display === 'block' ? 'none' : 'block';
}

/**
 * 모든 팝업 메뉴 닫기
 */
function closeAllPopupMenus() {
    document.querySelectorAll('.popup-menu').forEach(menu => {
        menu.style.display = 'none';
    });
}


/**
 * 수강평 모달 관련 이벤트 설정
 */
function setupReviewModal() {
    const stars = document.querySelectorAll('.star');
    const saveButton = document.querySelector('.save');

    // 별점 클릭 이벤트
    stars.forEach(star => {
        star.addEventListener('click', () => handleStarClick(star));
    });

    // 저장 버튼 이벤트
    if (saveButton) {
        saveButton.addEventListener('click', handleReviewSave);
    }
}

/**
 * 별점 클릭 처리
 * @param {Element} star - 클릭된 별 요소
 */
function handleStarClick(star) {
    selectedRating = parseInt(star.dataset.value);
    updateStars();
}

/**
 * 별점 UI 업데이트
 */
function updateStars() {
    const stars = document.querySelectorAll('.star');
    stars.forEach(star => {
        const starValue = parseInt(star.dataset.value);
        star.classList.toggle('active', starValue <= selectedRating);
    });
}

/**
 * 수강평 모달 오픈
 * @param {string} lectureId - 강의 ID
 */
async function openReviewModal(lectureId) {
    currentLectureId = lectureId;
    const modal = document.getElementById("review-modal");

    if (modal) {
        modal.style.display = "flex";

        // 기존 수강평이 있는지 확인
        try {
            const response = await fetch('/users/mypage/check-review', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({lectureId: lectureId})
            });

            if (response.ok) {
                const reviewData = await response.json();
                if (reviewData.reviewId) {
                    // 기존 수강평이 있는 경우
                    selectedRating = reviewData.rate;
                    document.getElementById('review-text').value = reviewData.content || '';
                    updateStars();
                } else {
                    // 새로운 수강평인 경우
                    selectedRating = 0;
                    document.getElementById('review-text').value = '';
                    updateStars();
                }
            }
        } catch (error) {
            console.error('수강평 확인 실패:', error);
            // 에러가 발생해도 모달은 열기
            selectedRating = 0;
            document.getElementById('review-text').value = '';
            updateStars();
        }
    }
}

/**
 * 수강평 모달 닫기
 */
function closeModal() {
    const modal = document.getElementById("review-modal");
    const reviewTextArea = document.getElementById('review-text');

    if (modal) {
        modal.style.display = "none";
    }

    // 상태 초기화
    currentLectureId = null;
    selectedRating = 0;
    updateStars();

    if (reviewTextArea) {
        reviewTextArea.value = '';
    }
}

/**
 * 수강평 저장 처리
 */
function handleReviewSave() {
    const reviewText = document.getElementById('review-text').value.trim();

    // 유효성 검사
    if (!validateReviewInput(selectedRating, reviewText)) {
        return;
    }

    const reviewData = {
        lectureId: currentLectureId,
        rating: parseInt(selectedRating),
        content: reviewText
    };

    saveReview(reviewData);
}

/**
 * 수강평 입력 유효성 검사
 * @param {number} rating - 선택된 별점
 * @param {string} reviewText - 수강평 텍스트
 * @returns {boolean} 유효성 검사 결과
 */
function validateReviewInput(rating, reviewText) {
    if (rating === 0) {
        alert(MESSAGES.RATING_REQUIRED);
        return false;
    }

    if (reviewText.length < 5) {
        alert(MESSAGES.REVIEW_MIN_LENGTH);
        return false;
    }

    return true;
}

/**
 * 수강평 저장 API 호출
 * @param {Object} reviewData - 수강평 데이터
 */
async function saveReview(reviewData) {
    try {
        // 기존 수강평이 있는지 확인
        const checkResponse = await fetch('/users/mypage/check-review', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({lectureId: reviewData.lectureId})
        });

        let existingReview = null;
        if (checkResponse.ok) {
            existingReview = await checkResponse.json();
        }

        // 기존 수강평이 있으면 수정, 없으면 새로 작성
        const method = existingReview && existingReview.reviewId ? 'PUT' : 'POST';
        const endpoint = existingReview && existingReview.reviewId ? '/users/mypage/review' : API_ENDPOINTS.REVIEW;

        // API에 맞는 데이터 형식으로 변환
        let apiData;
        if (existingReview && existingReview.reviewId) {
            // 수정 API용 데이터 형식
            apiData = {
                reviewId: existingReview.reviewId,
                rate: reviewData.rating,  // rating을 rate로 변환
                content: reviewData.content
            };
        } else {
            // 새로 작성 API용 데이터 형식
            apiData = {
                lectureId: reviewData.lectureId,
                rating: reviewData.rating,
                content: reviewData.content
            };
        }

        const response = await fetch(endpoint, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(apiData)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        if (data.status === "success") {
            handleReviewSaveSuccess();
        } else {
            handleReviewSaveError(new Error(data.message || MESSAGES.REVIEW_ERROR));
        }

    } catch (error) {
        handleReviewSaveError(error);
    }
}

/**
 * 수강평 저장 성공 처리
 */
function handleReviewSaveSuccess() {
    alert(MESSAGES.REVIEW_SUCCESS);
    closeModal();
    // 현재 필터 상태를 유지하면서 데이터 새로고침
    loadUserLectures(currentFilterType);
}

/**
 * 수강평 저장 에러 처리
 * @param {Error} error - 발생한 에러
 */
function handleReviewSaveError(error) {
    alert(MESSAGES.REVIEW_ERROR);
}

/**
 * 현재 활성화된 필터로 강의 목록 새로고침
 */
function refreshCurrentFilter() {
    const activeButton = document.querySelector('.filter-buttons button.active');
    if (activeButton) {
        // 현재 페이지와 필터를 유지하면서 새로고침
        loadUserLectures(currentFilterType);
    }
}

// 수정된 goToDetail 함수
function goToDetail(elementOrId) {
    let lectureId;

    // 전달받은 인자가 문자열(lectureId)인지 DOM 요소인지 확인
    if (typeof elementOrId === 'string') {
        // 문자열로 lectureId가 직접 전달된 경우
        lectureId = elementOrId;
    } else if (elementOrId && typeof elementOrId === 'object') {
        // DOM 요소가 전달된 경우
        lectureId = elementOrId.getAttribute('data-lecture-id') || elementOrId.getAttribute('data-id');
    }

    console.log('강의 ID:', lectureId);

    if (lectureId) {
        window.location.href = `/lms/main/${lectureId}`;
    } else {
        console.error("Lecture ID not found:", elementOrId);
    }
}

// 전역 함수로 노출 (HTML에서 직접 호출하는 경우를 위해)
window.openReviewModal = openReviewModal;
window.closeModal = closeModal;
window.goToPage = goToPage;
window.changePageSize = changePageSize;