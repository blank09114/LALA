let currentPage = 1;      // 현재 페이지 상태
const pageSize = 4;      // 한 페이지에 보여줄 글 수

let currentPostTypes = [2];
let currentOrderBy = "latest";
let currentSearchType = "";
let currentKeyword = "";
let currentCategory = "free"; // 현재 카테고리 상태 추가

// 사용자 권한 확인 함수
let isAdmin = false; // 전역 변수로 관리자 여부 저장

async function checkUserPermission() {
    try {
        const response = await fetch('/api/community/user/permission');
        const data = await response.json();

        if (data.success) {
            isAdmin = data.isAdmin || false;
            console.log("사용자 권한 확인:", isAdmin ? "관리자" : "일반 사용자");
        } else {
            isAdmin = false;
            console.log("권한 확인 실패:", data.message);
        }
    } catch (error) {
        console.error("권한 확인 오류:", error);
        isAdmin = false;
    }
}

// 글쓰기 버튼 표시/숨김 처리
function updateWriteButtonVisibility() {
    const writeBtn = document.querySelector(".write-btn");

    console.log("=== 글쓰기 버튼 디버깅 ===");
    console.log("writeBtn 요소:", writeBtn);
    console.log("currentPostTypes:", currentPostTypes);
    console.log("isAdmin:", isAdmin);

    if (writeBtn) {
        if (currentPostTypes.length === 1 && currentPostTypes[0] === 1) {
            // 공지사항 게시판 - 관리자만 글쓰기 가능
            console.log("공지사항 게시판 - 관리자 확인:", isAdmin);

            if (isAdmin) {
                // 관리자인 경우 글쓰기 버튼 표시
                writeBtn.style.display = "block";

                // 버튼 내부 요소 스타일 조정
                const btnElement = writeBtn.querySelector('button');
                if (btnElement) {
                    // 공지사항 작성 URL로 변경
                    btnElement.setAttribute('onclick', "location.href='/auth/community/boardWriter?category=notice'");
                }
            } else {
                writeBtn.style.display = "none";
            }
        } else {
            // 다른 게시판 - 모든 사용자 글쓰기 가능
            console.log("일반 게시판 - 모든 사용자 가능");
            writeBtn.style.display = "block";

            // 일반 게시판 URL로 복원
            const btnElement = writeBtn.querySelector('button');
            if (btnElement) {
                btnElement.setAttribute('onclick', "location.href='/auth/community/boardWriter'");
            }
        }
    } else {
        console.log("❌ 글쓰기 버튼을 찾을 수 없습니다!");
    }
}

// 카테고리별 postType 매핑
const categoryPostTypeMap = {
    'free': [2],           // 자유 게시판
    'study': [3, 4],       // 스터디 모집
    'qna': [5, 6],         // 질문 & 답변
    'notice': [1]          // 공지 게시판
};

//사이드바 메뉴
function setActiveSidebar(clickedElement) {
    // 기존 active 제거
    document.querySelectorAll('.sidebar-menu a').forEach(link => {
        link.classList.remove('active');
    });
    // 클릭한 항목에 active 부여
    clickedElement.classList.add('active');
}

// 카테고리에 따른 사이드바 활성화
function setActiveSidebarFromCategory(category) {
    // 모든 메뉴에서 active 제거
    document.querySelectorAll('.sidebar-menu a').forEach(link => {
        link.classList.remove('active');
    });

    // 카테고리에 따라 해당 메뉴 활성화
    const categoryMenuMap = {
        'free': 0,    // 자유 게시판
        'study': 1,   // 스터디 모집
        'qna': 2,     // 질문 & 답변
        'notice': 3   // 공지 게시판
    };

    const menuIndex = categoryMenuMap[category] || 0;
    const menuItems = document.querySelectorAll('.sidebar-menu a');
    if (menuItems[menuIndex]) {
        menuItems[menuIndex].classList.add('active');
        // 현재 postTypes와 category도 업데이트
        currentPostTypes = categoryPostTypeMap[category] || [2];
        currentCategory = category;
    }
}

// 검색 정보 표시 함수
function updateSearchInfo() {
    const searchInfo = document.getElementById('searchInfo');

    if (currentKeyword && currentSearchType) {
        const searchTypeNames = {
            'title': '제목',
            'content': '내용',
            'writer': '작성자'
        };

        const searchTypeName = searchTypeNames[currentSearchType] || '선택';
        searchInfo.innerHTML = `<span class="search-info-text">"${currentKeyword}"</span>에 대한 ${searchTypeName} 검색 결과`;
        searchInfo.classList.add('active');
    } else {
        searchInfo.classList.remove('active');
    }
}

// 검색 정보 초기화 함수
function clearSearchInfo() {
    currentKeyword = "";
    currentSearchType = "";
    updateSearchInfo();

    // 검색 입력창도 초기화
    const searchInput = document.getElementById("searchInputs");
    if (searchInput) {
        searchInput.value = "";
    }

    // 드롭다운도 '선택'으로 초기화
    const label = document.querySelector(".dropdown-toggle .label");
    if (label) {
        label.innerText = "선택";
    }

    document.querySelectorAll(".dropdown-list li").forEach(li => li.classList.remove("selected"));
    const firstItem = document.querySelector(".dropdown-list li:first-child");
    if (firstItem) {
        firstItem.classList.add("selected");
    }
}

function loadPosts(postTypes, options = {}) {
    currentPostTypes = postTypes;
    const queryParams = [];
    postTypes.forEach(t => queryParams.push(`postType=${t}`));

    // 공지 게시판 필터 처리
    const orderList = document.getElementById("orderList");

    if (postTypes.length === 1 && postTypes[0] === 1) {
        // 공지사항 게시판: 정렬 필터만 숨기기
        if (orderList) {
            // 정렬 필터 항목들만 숨기기
            const filterItems = orderList.querySelectorAll('li:not(.write-btn)');
            filterItems.forEach(item => {
                item.style.display = 'none';
            });
        }
    } else {
        // 다른 게시판: 모든 필터 표시
        if (orderList) {
            orderList.style.display = "flex";
            const filterItems = orderList.querySelectorAll('li');
            filterItems.forEach(item => {
                item.style.display = 'block';
            });
        }
    }

    // 글쓰기 버튼 표시/숨김 처리
    updateWriteButtonVisibility();

    // 페이지 & 사이즈 기본값 지정
    const page = options.page || currentPage;
    const size = options.size || pageSize;
    queryParams.push(`page=${page}`);
    queryParams.push(`size=${size}`);

    const order = options.orderBy ?? currentOrderBy;
    queryParams.push(`orderBy=${order}`);

    // 검색 파라미터 처리
    if (options.keyword && options.searchType) {
        queryParams.push(`${options.searchType}=${encodeURIComponent(options.keyword)}`);

        // 전역 변수도 업데이트
        currentKeyword = options.keyword;
        currentSearchType = options.searchType;
    }

    currentPage = page;

    console.log("=== API 호출 정보 ===");
    console.log("최종 쿼리 파라미터:", queryParams.join("&"));

    fetch(`/api/community/board?${queryParams.join("&")}`)
        .then(res => {
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then(data => {
            console.log("서버 응답 결과:", data);
            const list = document.getElementById("postList");
            list.innerHTML = "";

            if (!data.communitys || data.communitys.length === 0) {
                list.innerHTML = "<li>게시물이 없습니다.</li>";
                const pagination = document.querySelector(".pagination");
                if (pagination) {
                    pagination.innerHTML = "";
                }

                // 검색 결과가 없을 때 메시지 업데이트
                if (currentKeyword && currentSearchType) {
                    updateSearchInfo();
                }
                return;
            }

            // 게시글 출력
            data.communitys.forEach(post => {
                const li = document.createElement("li");
                li.className = "post-list";

                li.addEventListener("click", () => {
                    // 현재 카테고리 정보를 URL에 포함시켜 게시글 상세로 이동
                    location.href = `/auth/community/boardDetails/${post.postId}?category=${currentCategory}`;
                });

                // 텍스트를 한 줄로 자르는 함수
                function truncateText(text, maxLength = 50) {
                    const tempDiv = document.createElement('div');
                    tempDiv.innerHTML = text;
                    const textOnly = tempDiv.textContent || tempDiv.innerText || '';

                    if (textOnly.length > maxLength) {
                        return textOnly.substring(0, maxLength) + '...';
                    }
                    return textOnly;
                }

                if (post.postType === "공지" || post.postType === 1) {
                    li.className = "notice-item";
                    li.innerHTML = `
                        <div class="notice-content">
                            <div class="notice-title">${post.postTitle}</div>
                            <p class="notice-desc">${truncateText(post.postContent)}</p>
                        </div>
                        <div class="notice-date">${post.writeDate.substring(0, 10)}</div>
                    `;
                } else {
                    li.className = "post-item";
                    // 자유 게시판은 postType 숨김
                    const isFreeBoard = post.postType === "자유" || post.postType === 2;
                    const isFinished = post.postType === "해결" || post.postType === "모집완료";

                    li.innerHTML = `
                        ${!isFreeBoard ? `<span class="post-type ${isFinished ? 'finished' : ''}">${post.postType}</span>` : ``}
                        <div class="info">
                            <h3 class="post-title">${post.postTitle}</h3>
                            <p class="desc">${truncateText(post.postContent)}</p>
                            <div class="meta">
                                <span class="meta-left">${post.writerNickname} - ${post.writeDate.substring(0, 10)}</span>
                                <span class="meta-right">
                                    <i class="fa-regular fa-eye"></i> ${post.postView}
                                    <i class="fa-regular fa-comment"></i> ${post.postCommNum}
                                    <i class="fa-regular fa-heart"></i> ${post.postLikeNum}
                                </span>
                            </div>
                        </div>
                    `;
                }

                list.appendChild(li);
            });

            // 페이지네이션 UI 생성
            renderPagination(data.total, page, size);

            // 검색 정보 업데이트
            updateSearchInfo();

            console.log(`API 호출: /api/community/board?${queryParams.join("&")}`);
        })
        .catch(err => {
            console.error("에러:", err);
            const errorMessage = err.message.includes('HTTP error')
                ? "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                : "게시글을 불러오는 중 문제가 발생했습니다.";
            alert(errorMessage);

            // 에러 발생 시 빈 목록 표시
            const list = document.getElementById("postList");
            list.innerHTML = "<li>게시글을 불러올 수 없습니다.</li>";
        });
}

// 카테고리별 게시물 로드 (사이드바 클릭 시)
function loadPostsByCategory(category, clickedElement) {
    setActiveSidebar(clickedElement);

    const postTypes = categoryPostTypeMap[category] || [2];
    currentPostTypes = postTypes;
    currentCategory = category; // 현재 카테고리 업데이트

    // URL 업데이트 (브라우저 히스토리에 추가)
    const url = new URL(window.location);
    url.searchParams.set('category', category);
    window.history.pushState({}, '', url);

    // 검색 초기화 후 게시물 로드
    clearSearchInfo();
    loadPosts(postTypes, { orderBy: currentOrderBy });
}

// 검색 실행 함수
function onSearch() {
    const searchInput = document.getElementById("searchInputs");
    const keyword = searchInput ? searchInput.value.trim() : "";
    const selected = document.querySelector(".dropdown-list .selected");
    const searchType = selected ? selected.dataset.value : "";

    console.log("=== 검색 디버깅 ===");
    console.log("검색 입력창:", searchInput);
    console.log("입력된 키워드:", keyword);
    console.log("선택된 검색 타입:", searchType);
    console.log("selected 요소:", selected);

    if (!keyword) {
        alert("검색어를 입력해주세요.");
        return;
    }

    if (!searchType || searchType === "") {
        alert("검색 유형을 선택해주세요.");
        return;
    }

    currentKeyword = keyword;
    currentSearchType = searchType;
    currentPage = 1; // 검색시 첫 페이지로

    loadPosts(currentPostTypes, {
        page: 1,
        keyword: keyword,
        searchType: searchType,
        orderBy: currentOrderBy
    });
}

// 검색 초기화 함수 (사이드바 클릭시 사용)
function loadPostsAndClearSearch(postTypes) {
    clearSearchInfo();
    loadPosts(postTypes, { orderBy: currentOrderBy });
}

// DOM 로드 완료 후 이벤트 리스너 등록
document.addEventListener("DOMContentLoaded", async () => {
    // 먼저 사용자 권한 확인
    await checkUserPermission();

    // 엔터키로 검색
    const searchInput = document.getElementById("searchInputs");
    if (searchInput) {
        searchInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                onSearch();
            }
        });
    }

    // 드롭다운 동작
    const toggleBtn = document.querySelector(".dropdown-toggle");
    const dropdownList = document.querySelector(".dropdown-list");

    if (toggleBtn && dropdownList) {
        toggleBtn.addEventListener("click", (e) => {
            e.preventDefault();
            dropdownList.style.display = dropdownList.style.display === "block" ? "none" : "block";
        });

        dropdownList.addEventListener("click", (e) => {
            if (e.target.tagName === "LI") {
                const label = toggleBtn.querySelector(".label");
                if (label) {
                    label.innerText = e.target.innerText;
                }

                // 기존 selected 제거
                dropdownList.querySelectorAll("li").forEach(li => li.classList.remove("selected"));
                // 클릭한 항목에 selected 추가
                e.target.classList.add("selected");

                dropdownList.style.display = "none";
            }
        });

        // 외부 클릭시 드롭다운 닫기
        document.addEventListener("click", (e) => {
            const customDropdown = document.querySelector(".custom-dropdown");
            if (customDropdown && !customDropdown.contains(e.target)) {
                dropdownList.style.display = "none";
            }
        });
    }

    // 정렬 클릭 이벤트 등록
    document.querySelectorAll("#orderList li").forEach(li => {
        if (!li.classList.contains("write-btn")) {
            li.addEventListener("click", () => {
                // 기존 active 제거
                document.querySelectorAll("#orderList li").forEach(el => el.classList.remove("active"));
                // 클릭한 항목에 active 추가
                li.classList.add("active");

                currentOrderBy = li.dataset.order;
                currentPage = 1; // 정렬 변경시 첫 페이지로

                loadPosts(currentPostTypes, {
                    page: 1,
                    keyword: currentKeyword,
                    searchType: currentSearchType,
                    orderBy: currentOrderBy
                });
            });
        }
    });

    const urlParams = new URLSearchParams(window.location.search);
    const category = urlParams.get('category') || 'free'; // 기본값 'free' 설정

    // 카테고리 파라미터에 따른 게시물 로드
    const postTypes = categoryPostTypeMap[category] || [2];
    currentPostTypes = postTypes;
    currentCategory = category; // 현재 카테고리 설정

    // 해당 카테고리의 게시물 로드
    loadPosts(postTypes, { orderBy: currentOrderBy });

    // 사이드바 메뉴 활성화
    setActiveSidebarFromCategory(category);

    console.log("초기 로드 완료 - 현재 카테고리:", currentCategory);
});

// 페이지네이션 렌더링
function renderPagination(total, currentPage, pageSize) {
    const paginationDiv = document.querySelector(".pagination");
    if (!paginationDiv) return;

    paginationDiv.innerHTML = "";

    const totalPages = Math.ceil(total / pageSize);
    if (totalPages <= 1) return;

    // 이전 버튼
    const prev = document.createElement("button");
    prev.textContent = "<";
    prev.className = "pagination-btn";
    prev.disabled = currentPage === 1;
    prev.addEventListener("click", () => {
        if (currentPage > 1) {
            loadPosts(currentPostTypes, {
                page: currentPage - 1,
                size: pageSize,
                keyword: currentKeyword,
                searchType: currentSearchType,
                orderBy: currentOrderBy
            });
        }
    });
    paginationDiv.appendChild(prev);

    // 페이지 번호 버튼들
    const maxPageButtons = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxPageButtons / 2));
    let endPage = Math.min(totalPages, startPage + maxPageButtons - 1);

    if (endPage - startPage < maxPageButtons - 1) {
        startPage = Math.max(1, endPage - maxPageButtons + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
        const btn = document.createElement("button");
        btn.textContent = i;
        btn.className = "pagination-btn";

        if (i === currentPage) {
            btn.disabled = true;
            btn.classList.add("active");
        }

        btn.addEventListener("click", () => {
            loadPosts(currentPostTypes, {
                page: i,
                size: pageSize,
                keyword: currentKeyword,
                searchType: currentSearchType,
                orderBy: currentOrderBy
            });
        });
        paginationDiv.appendChild(btn);
    }

    // 다음 버튼
    const next = document.createElement("button");
    next.textContent = ">";
    next.className = "pagination-btn";
    next.disabled = currentPage === totalPages;
    next.addEventListener("click", () => {
        if (currentPage < totalPages) {
            loadPosts(currentPostTypes, {
                page: currentPage + 1,
                size: pageSize,
                keyword: currentKeyword,
                searchType: currentSearchType,
                orderBy: currentOrderBy
            });
        }
    });
    paginationDiv.appendChild(next);
}