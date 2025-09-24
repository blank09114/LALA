const LECTURE_PER_PAGE = 40;
let currentPage = 0;
let currentCategory = null;

// URL에서 검색 키워드 가져오기
const urlParams = new URLSearchParams(window.location.search);
const searchKeyword = urlParams.get('keyword');

const filterState = {
    categoryId: null,
    priceType: null,
    onlyDiscount: false,
    difficulty: null,
    keyword: searchKeyword
};

// 가격 필터 (무료/유료/할인)
document.querySelectorAll(".filterItem").forEach(item => {
    item.addEventListener("click", () => {
        const type = item.textContent.trim();
        const isSelected = item.classList.contains("selected");

        // 모든 필터에서 selected 제거
        document.querySelectorAll(".filterItem").forEach(i => i.classList.remove("selected"));

        // 선택 해제 상태였으면 다시 선택 상태로 설정
        if (!isSelected) {
            item.classList.add("selected");

            if (type === "무료") filterState.priceType = "free";
            else if (type === "유료") filterState.priceType = "paid";
            else if (type === "할인") filterState.priceType = "discount";
        } else {
            filterState.priceType = null; // 선택 해제
        }

        currentPage = 0;
        fetchFilteredLectures();
    });
});

// 난이도 필터
document.querySelector(".level").addEventListener("change", (e) => {
    const value = e.target.value;

    if (value === "default") {
        filterState.difficulty = null;
    } else if (value === "level1") filterState.difficulty = "0";
    else if (value === "level2") filterState.difficulty = "1";
    else if (value === "level3") filterState.difficulty = "2";
    else if (value === "level4") filterState.difficulty = "3";

    currentPage = 0;
    fetchFilteredLectures();
});

// 카테고리 필터
document.querySelectorAll(".nav-menu li").forEach(item => {
    item.addEventListener("click", () => {
        document.querySelectorAll(".nav-menu li").forEach(li => li.classList.remove("selected"));
        item.classList.add("selected");

        const categoryId = item.dataset.category;
        currentCategory = categoryId || null;
        filterState.categoryId = currentCategory;
        currentPage = 0;

        fetchFilteredLectures();
    });
});

// 강의 렌더링
function renderLectures(lectures, targetSelector, offset) {
    const container = document.querySelector(targetSelector);
    container.innerHTML = "";

    const fragment = document.createDocumentFragment();
    for (let i = offset; i < offset + 20 && i < lectures.length; i += 4) {
        const row = document.createElement("div");
        row.className = "lecListRow";

        for (let j = i; j < i + 4 && j < lectures.length; j++) {
            const lec = lectures[j];

            const item = document.createElement("div");
            item.className = "lecItem";
            item.setAttribute("data-id", lec.lectureId);
            item.onclick = () => goToDetail(item);

            const discountRate = lec.discountRate || 0;
            const originalPrice = lec.price;
            const finalPrice = discountRate > 0
                ? Math.round(originalPrice * (100 - discountRate) / 100)
                : originalPrice;

            let priceHTML = "";
            if (discountRate > 0) {
                priceHTML = `
                    <span class="regPrice"><del>₩${originalPrice.toLocaleString()}</del></span><br>
                    <span class="salePrice" style="color: red;">
                        ${discountRate}% 할인 → ₩${finalPrice.toLocaleString()}
                    </span><br>
                `;
            } else {
                priceHTML = `<span class="salePrice">₩${originalPrice.toLocaleString()}</span><br>`;
            }

            item.innerHTML = `
                <div class="lecImg" style="background-image: url('${lec.thumbnail || ''}')"></div>
                <div class="lecInfo">
                    <h2 class="lecTitle">${lec.title}</h2>
                    <p class="lecContent">
                        <span class="provider">${lec.nickname}</span><br>
                        ${priceHTML}
                        ★${lec.avgReviewRate} (${lec.reviewNum}) 👤${lec.studentNum}+
                    </p>
                    <div class="lecGrade">${mapDifficulty(lec.difficulty)}</div>
                </div>
            `;
            row.appendChild(item);
        }

        fragment.appendChild(row);
    }

    container.appendChild(fragment);
}

function mapDifficulty(level) {
    switch (level) {
        case 0: return "입문";
        case 1: return "초급";
        case 2: return "중급";
        case 3: return "고급";
        default: return "기타";
    }
}

// 필터 상태 기반 강의 fetch
function fetchFilteredLectures() {
    const params = new URLSearchParams();

    if (filterState.categoryId) params.append("categoryId", filterState.categoryId);
    if (filterState.priceType) params.append("priceType", filterState.priceType);
    if (filterState.onlyDiscount) params.append("onlyDiscount", "true");
    if (filterState.difficulty) params.append("difficulty", filterState.difficulty);
    if (filterState.keyword) params.append("keyword", filterState.keyword);

    // 검색 키워드가 있으면 검색 API 사용, 없으면 일반 필터 API 사용
    const apiUrl = filterState.keyword ? 
        `/api/course/lectures/search?${params.toString()}` : 
        `/api/course/lectures/filtered?${params.toString()}`;

    fetch(apiUrl)
        .then(res => res.json())
        .then(data => {
            const lectures = data.lectures || [];
            renderLectures(lectures, "#lectureTop", 0);
            renderLectures(lectures, "#lectureBottom", 20);
            renderPagination(Math.ceil(lectures.length / LECTURE_PER_PAGE));
        });
}

// 지식 제공자 스크롤
function scrLeft() {
    const list = document.querySelector(".provList-items");
    list.scrollBy({ left: -300, behavior: 'smooth' });
    updateBtn();
}
function scrRight() {
    const list = document.querySelector(".provList-items");
    list.scrollBy({ left: 300, behavior: 'smooth' });
    updateBtn();
}
function updateBtn() {
    const list = document.querySelector(".provList-items");
    const leftBtn = document.getElementById("leftBtn");
    const rightBtn = document.getElementById("rightBtn");

    leftBtn.style.display = list.scrollLeft <= 0 ? "none" : "block";
    rightBtn.style.display = list.scrollLeft + list.clientWidth >= list.scrollWidth - 1 ? "none" : "block";
}

// 지식제공자 렌더링
function renderProviders(providerList) {
    const container = document.getElementById("provListContainer");
    if (!container) return;
    container.innerHTML = "";

    providerList.forEach(provider => {
        const item = document.createElement("div");
        item.className = "provList-item";

        const profileUrl = provider.profileName?.trim() || "/img/profileImg.png";
        const nickname = provider.nickname || "이름없음";
        const providerId = provider.providerId;

        item.innerHTML = `
            <a href="/course/IPdetail/${providerId}">
                <div class="profileImg" style="background-image: url('${profileUrl}')"></div>
            </a>
            <span>${nickname}</span>
        `;

        container.appendChild(item);
    });

    updateBtn();
}

function goToDetail(element) {
    const id = element.getAttribute("data-id");
    if (!id) return;
    window.location.href = "/course/detail/" + id;
}

// 검색 초기화 함수
function clearSearch() {
    filterState.keyword = null;
    currentPage = 0;
    // URL에서 검색 파라미터 제거
    const url = new URL(window.location);
    url.searchParams.delete('keyword');
    window.history.replaceState({}, '', url);
    
    // 검색 결과 헤더 숨기기
    const searchHeader = document.querySelector('.search-result-header');
    if (searchHeader) {
        searchHeader.style.display = 'none';
    }
    
    fetchFilteredLectures();
}

// 초기 실행
document.addEventListener("DOMContentLoaded", () => {
    fetch("/api/course/main/data")
        .then(res => res.json())
        .then(data => {
            if (data.informationProviders)
                renderProviders(data.informationProviders);
        });

    fetchFilteredLectures();
});