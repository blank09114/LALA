// 목차 펼치기/접기
function toggleClassList(button)
{
    const classList = button.nextElementSibling;
    
    if (classList && classList.classList.contains("classList"))
    {
        if (classList.style.display === "none") { classList.style.display = ""; }
        else { classList.style.display = "none"; }
    }
}

// 오늘 날짜 구하기
function getToday()
{
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    return `${yyyy}.${mm}.${dd}.`;
}

// 질문 수 카운트
function updateQnaCount()
{
    const questions = document.querySelectorAll('.qnaList > .qnaItem');
    document.getElementById('qnaCount').textContent = String(questions.length).padStart(2, '0');
}

// 질문 삭제
function deleteQuestion(btn)
{
    const qnaItem = btn.closest('.qnaItem');
    const isQuestion = !qnaItem.classList.contains('answer');

    qnaItem.remove();

    if (isQuestion) updateQnaCount();
}


// 질문 수 카운트 초기화
document.addEventListener('DOMContentLoaded', updateQnaCount);