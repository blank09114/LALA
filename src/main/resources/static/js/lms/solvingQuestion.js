// 이 코드 전체 사용 안 함
// 제출
function submitQuiz()
{
    const viewer = document.querySelector('.viewer');
    const quizItems = viewer.querySelectorAll('.quizItem');
    let total = quizItems.length;
    let score = 0;

    quizItems.forEach((item, index) =>
    {
        const radios = item.querySelectorAll('input[type="radio"]');
        const textInput = item.querySelector('input[type="text"]');

        // 객관식 처리
        if (radios.length > 0)
        {
            radios.forEach(r =>
            {
                r.disabled = true;
                if (r.checked && r === radios[0]) score++; // 첫 번째가 정답
            });
        }

        // 단답형 처리
        if (textInput)
        {
            textInput.readOnly = true;
            if (textInput.value.trim() !== '') score++; // 비어있지 않으면 정답으로 처리
        }

        // 정답 표시
        if (!item.querySelector('.quizResult'))
        {
            const result = document.createElement('div');
            result.className = 'quizResult';
            result.innerHTML = `<p class="answerText">정답: 예시 정답 ${index + 1}</p>`;
            item.appendChild(result);
        }
    });

    // 점수 출력
    const scoreText = viewer.querySelector('.score');
    scoreText.textContent = `점수: ${score}/${total}`;
    scoreText.style.display = 'block';

    // 버튼 전환
    const submitBtn = viewer.querySelector('.submit');
    submitBtn.textContent = '다시 풀기';
    submitBtn.onclick = resetQuiz;

    // QnA 활성화
    document.querySelector('.qnaWrap')?.classList.remove('disabled');
}

function resetQuiz()
{
    const viewer = document.querySelector('.viewer');
    const quizItems = viewer.querySelectorAll('.quizItem');

    quizItems.forEach(item =>
    {
        const radios = item.querySelectorAll('input[type="radio"]');
        const textInput = item.querySelector('input[type="text"]');

        radios.forEach(r =>
        {
            r.checked = false;
            r.disabled = false;
        });

        if (textInput)
        {
            textInput.value = '';
            textInput.readOnly = false;
        }

        const result = item.querySelector('.quizResult');
        if (result) result.remove();
    });

    // 점수 숨김
    const scoreText = viewer.querySelector('.score');
    scoreText.style.display = 'none';

    // 버튼 초기화
    const submitBtn = viewer.querySelector('.submit');
    submitBtn.textContent = '제출';
    submitBtn.onclick = submitQuiz;

    // QnA 비활성화
    document.querySelector('.qnaWrap')?.classList.add('disabled');
}

// 초기 상태 설정
window.addEventListener('DOMContentLoaded', () =>
{
    document.querySelector('.qnaWrap')?.classList.add('disabled');
    document.querySelector('.score').style.display = 'none';
});