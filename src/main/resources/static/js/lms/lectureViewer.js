// 모달 오픈
function showModal()
{
    const modal = document.querySelector('.modal');
    if (modal) modal.style.display = 'flex';
}

// 모달 닫기
function closeModal()
{
    const modal = document.querySelector('.modal');
    if (modal) modal.style.display = 'none';
}

// 수강 취소
function confirm()
{
    alert("수강이 취소되었습니다.");
    closeModal()
}

// DOM 로드 후 이벤트 바인딩
document.addEventListener("DOMContentLoaded", function()
{ document.querySelector('.cancelBtn').onclick = showModal; });

// 강의 등록
function submit()
{
    alert("강의 등록 요청이 서버에 전달됐습니다.");

    const submitBtn = document.querySelector('.provBtn:not(.gray)');
    if (submitBtn) submitBtn.style.display = 'none';

    const grayBtn = document.querySelector('.provBtn.gray');
    if (grayBtn) grayBtn.style.display = 'block';
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

// toggleAllBtn 텍스트 업데이트
function updateToggleBtnLabel()
{
    const classLists = document.querySelectorAll('.classList');
    const toggleAllBtn = document.querySelector('.toggleBtn');

    const anyOpen = Array.from(classLists).some(el => el.style.display === 'flex');
    toggleAllBtn.textContent = anyOpen ? '▲ 모두 접기' : '▼ 모두 펼치기';
}