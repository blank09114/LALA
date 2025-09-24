// 페이지 핸들러
function toScroll(sectionId)
{
    const section = document.getElementById(sectionId);
    if (section) { section.scrollIntoView({ behavior: 'smooth', block: 'start' }); }
}

// 개별 목차 토글
function toggle(btn)
{
    const part = btn.closest('.part');
    const classList = part.querySelector('.classList');

    const isOpen = classList.style.display === 'flex';

    classList.style.display = isOpen ? 'none' : 'flex';
    btn.textContent = isOpen ? '▼' : '▲';

    updateToggleBtnLabel(); // 상태 반영
}

// 모든 목차 토글
function toggleAll()
{
    const classLists = document.querySelectorAll('.classList');
    const toggleBtns = document.querySelectorAll('.partToggleBtn');
    const toggleAllBtn = document.querySelector('.toggleBtn');

    const anyOpen = Array.from(classLists).some(el => el.style.display === 'flex');

    const shouldOpen = !anyOpen; // 하나라도 열려 있으면 닫기, 전부 닫혀 있으면 열기

    classLists.forEach(el => el.style.display = shouldOpen ? 'flex' : 'none');
    toggleBtns.forEach(btn => btn.textContent = shouldOpen ? '▲' : '▼');
    toggleAllBtn.textContent = shouldOpen ? '▲ 모두 접기' : '▼ 모두 펼치기';
}

// 리뷰 수정
function editReview(button)
{
    const reviewElem = button.closest(".myReview");
    const scoreText = reviewElem.querySelector(".myReviewContent .score").textContent;
    const contentText = reviewElem.querySelector(".myReviewContent .reviewContent").textContent;

    // 폼으로 복귀
    reviewElem.querySelector(".reviewSubmit").style.display = "block";
    reviewElem.querySelector(".myReviewContent").style.display = "none";

    // 기존 리뷰 값으로 select, textarea 세팅
    reviewElem.querySelector("#ratingSelect").value = parseInt(scoreText);
    reviewElem.querySelector("textarea").value = contentText;
}

// 리뷰 삭제
let reviewToDelete = null;

// 모달 표시
function openModal(btn)
{
    reviewToDelete = btn.closest(".review");
    document.getElementById("modal").classList.remove("hidden");
    document.getElementById("modalOverlay").style.display = "flex";
}

// 모달 닫기
function closeModal()
{
    document.getElementById("modal").classList.add("hidden");
    document.getElementById("modalOverlay").style.display = "none";
    reviewToDelete = null;
}

// 리뷰 더보기
let reviewIndex = 1;

function showMore()
{
    const reviewLists = document.querySelectorAll(".reviewList");
    const moreBtn = document.querySelector(".moreBtn");

    if (reviewIndex < reviewLists.length)
    {
        reviewLists[reviewIndex].style.display = "flex";
        reviewIndex++;

        if (reviewIndex === reviewLists.length) { moreBtn.style.display = "none"; }
    }
}

// toggleAllBtn 텍스트 업데이트
function updateToggleBtnLabel()
{
    const classLists = document.querySelectorAll('.classList');
    const toggleAllBtn = document.querySelector('.toggleBtn');

    const anyOpen = Array.from(classLists).some(el => el.style.display === 'flex');
    toggleAllBtn.textContent = anyOpen ? '▲ 모두 접기' : '▼ 모두 펼치기';
}

// 모달 오픈
function showToast(message)
{
    const toast = document.getElementById('toast');
    toast.querySelector('span').textContent = message;

    toast.classList.remove('hidden');
    toast.classList.add('show');

    setTimeout(() =>
    {
        toast.classList.remove('show');
        toast.classList.add('hidden');
    }, 2000);
}

// 이벤트 리스너
document.addEventListener("DOMContentLoaded", function()
{
    // 페이지 핸들러 now 클래스 갱신
    const handlerItems = document.querySelectorAll(".pageHandler li");
    const sections =
    [
        { id: "introSection", element: document.getElementById("introSection") },
        { id: "contentSection", element: document.getElementById("contentSection") },
        { id: "reviewSection", element: document.getElementById("reviewSection") }
    ];

    function updateNowClass()
    {
        const scrollY = window.scrollY;
        const offset = 100;

        let current = null;
        for (const section of sections)
        {
            const top = section.element.offsetTop;
            if (scrollY + offset >= top) { current = section.id; }
        }

        if (current)
        {
            handlerItems.forEach(item =>
            {
                item.classList.remove("now");
                if (item.getAttribute("onclick")?.includes(current)) { item.classList.add("now"); }
            });
        }
    }

    window.addEventListener("scroll", updateNowClass);
    updateNowClass(); // 초기화
});