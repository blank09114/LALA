/**
 * 📚 목차 등록 페이지 JavaScript
 * 초급 개발자도 이해하기 쉽게 작성된 코드입니다.
 * 
 * 주요 기능:
 * 1. URL에서 lectureId 추출
 * 2. 목차 제목 및 학습 목표 입력
 * 3. 콘텐츠 동적 관리 (영상, 퀴즈, 자료)
 * 4. API 호출을 통한 목차 등록
 */

// 🌟 전역 변수들
let currentLectureId = null;  // 현재 강의 ID
let contentList = [];         // 추가된 콘텐츠들을 저장하는 배열

/**
 * 🔗 URL에서 lectureId를 추출하는 함수
 */
function getLectureIdFromUrl() {
    const pathParts = window.location.pathname.split('/');
    const lectureId = pathParts[pathParts.length - 1]; // URL의 마지막 부분
    console.log('🔗 URL에서 추출한 lectureId:', lectureId);
    return lectureId;
}

/**
 * 🚀 페이지 초기화 함수
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('📚 목차 등록 페이지 초기화 시작');
    
    // 1. URL에서 lectureId 추출
    currentLectureId = getLectureIdFromUrl();
    
    if (!currentLectureId) {
        alert('강의 ID를 찾을 수 없습니다. 강의 등록 페이지로 돌아갑니다.');
        window.location.href = '/lms/register';
        return;
    }
    
    console.log('📚 현재 작업 중인 강의 ID:', currentLectureId);
    
    // 2. 이벤트 리스너 등록
    initializeEventListeners();
});

/**
 * 🎯 이벤트 리스너 초기화
 */
function initializeEventListeners() {
    // 콘텐츠 추가 버튼
    const addButton = document.querySelector('.classAdd');
    if (addButton) {
        addButton.addEventListener('click', openModal);
    }
    
    // 모달 관련 이벤트 리스너
    initializeModalEvents();
}

/**
 * 🎨 모달 관련 이벤트 초기화
 */
function initializeModalEvents() {
    // 콘텐츠 유형 선택 버튼들
    const selectButtons = document.querySelectorAll('.selectBtn');
    const sections = {
        '강좌': document.getElementById('video'),
        '퀴즈': document.getElementById('quiz'),
        '자료': document.getElementById('file')
    };

    selectButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            // 버튼 상태 초기화
            selectButtons.forEach(b => b.classList.remove('now'));
            btn.classList.add('now');

            // 모든 섹션 숨기기
            Object.values(sections).forEach(sec => sec.style.display = 'none');

            // 해당 섹션 표시
            const label = btn.textContent.trim();
            if (sections[label]) {
                sections[label].style.display = 'flex';
            }
        });
    });
    
    // 모달 외부 클릭 시 닫기
    document.addEventListener('click', function(event) {
        const overlay = document.querySelector('.overlay');
        const modal = document.querySelector('.modal');
        if (event.target === overlay) {
            closeModal();
        }
    });
}

// ==================== 📝 번호 및 순서 관리 ====================

/**
 * 📝 콘텐츠 번호 업데이트 함수
 */
function updateNumbers() {
    const items = document.querySelectorAll('.class');
    let count = 1;

    items.forEach((item) => {
        const icon = item.querySelector('.icon');
        const title = item.querySelector('.classTitle');
        if (!icon || !title) return;

        const src = icon.getAttribute('src');
        if (src && src.includes('file.png')) return; // 자료는 넘버링 안 함

        const text = title.textContent.trim();
        const afterDot = text.replace(/^\d+\.\s*/, '');
        title.textContent = `${count++}. ${afterDot}`;
    });
}

/**
 * ⬆️ 위로 이동
 */
function moveUp(button) {
    const classDiv = button.closest('.class');
    const prev = classDiv?.previousElementSibling;
    if (prev && prev.classList.contains('class')) {
        prev.before(classDiv);
        updateNumbers();
        updateContentList();
    }
}

/**
 * ⬇️ 아래로 이동
 */
function moveDown(button) {
    const classDiv = button.closest('.class');
    const next = classDiv?.nextElementSibling;
    if (next && next.classList.contains('class')) {
        next.after(classDiv);
        updateNumbers();
        updateContentList();
    }
}

/**
 * 🗑️ 콘텐츠 삭제
 */
function removeClass(button) {
    const classElement = button.closest('.class');
    if (classElement) {
        const contentId = classElement.dataset.contentId;
        
        // DOM에서 제거
        classElement.remove();
        
        // 배열에서도 제거
        contentList = contentList.filter(item => item.id !== contentId);
        
        updateNumbers();
        console.log('🗑️ 콘텐츠 삭제됨. 남은 콘텐츠:', contentList.length);
    }
}

/**
 * 📊 콘텐츠 배열 업데이트
 */
function updateContentList() {
    const items = document.querySelectorAll('.class');
    const newList = [];
    
    items.forEach((item, index) => {
        const contentId = item.dataset.contentId;
        const existingContent = contentList.find(c => c.id === contentId);
        if (existingContent) {
            existingContent.order = index;
            newList.push(existingContent);
        }
    });
    
    contentList = newList;
}

// ==================== 🎭 모달 관리 ====================

/**
 * 📱 모달 열기
 */
function openModal() {
    document.querySelector('.overlay').style.display = 'flex';
}

/**
 * ❌ 모달 닫기
 */
function closeModal() {
    const overlay = document.querySelector('.overlay');
    overlay.style.display = 'none';
    
    // 모달 폼 초기화
    resetModalForms();
}

/**
 * 🔄 모달 폼 초기화
 */
function resetModalForms() {
    // 모든 입력 필드 초기화
    const modal = document.querySelector('.modal');
    const inputs = modal.querySelectorAll('input[type="text"], input[type="file"], textarea');
    inputs.forEach(input => input.value = '');
    
    // 파일명 표시 초기화
    const fileNames = modal.querySelectorAll('.fileName');
    fileNames.forEach(fileName => {
        if (fileName.textContent !== '파일 형식 제한 없음') {
            fileName.textContent = 'mp4 파일';
        }
    });
    
    // 첫 번째 탭으로 초기화
    const selectButtons = document.querySelectorAll('.selectBtn');
    selectButtons.forEach(btn => btn.classList.remove('now'));
    selectButtons[0]?.classList.add('now');
    
    const sections = document.querySelectorAll('.modal-section');
    sections.forEach(section => section.style.display = 'none');
    document.getElementById('video').style.display = 'flex';
}

// ==================== 📁 파일 업로드 관리 ====================

/**
 * 📁 파일 업로드 처리 함수
 */
window.updateFile = function(input) {
    const fileNameBox = input.closest('.file')?.querySelector('.fileName');
    if (!fileNameBox) return;

    if (input.files.length > 0) {
        const file = input.files[0];
        fileNameBox.textContent = file.name;
        console.log('📁 파일 선택됨:', file.name);
    } else {
        const defaultText = input.accept === '.mp4' ? 'mp4 파일' : '파일 형식 제한 없음';
        fileNameBox.textContent = defaultText;
    }
};

// ==================== 🧩 퀴즈 관리 ====================

/**
 * 🔄 퀴즈 타입 토글 (객관식/단답형)
 */
function toggleQuizType(selectElement) {
    const quizBox = selectElement.closest('.quizContent');
    const choiceSection = quizBox.querySelector('.choiceSection');
    const answerSection = quizBox.querySelector('.answerSection');

    if (selectElement.value === 'choice') {
        choiceSection.style.display = 'block';
        answerSection.style.display = 'none';
    } else if (selectElement.value === 'answer') {
        choiceSection.style.display = 'none';
        answerSection.style.display = 'block';
    }
}

/**
 * ➕ 퀴즈 문항 추가
 */
function addQuiz() {
    const quizList = document.querySelector('.modalSectionContent[style*="flex"]');
    const firstQuiz = quizList.querySelector('.quizContent');
    const newQuiz = firstQuiz.cloneNode(true);

    // 내부 값 초기화
    const inputs = newQuiz.querySelectorAll('input, select');
    inputs.forEach(input => {
        if (input.tagName === 'SELECT') {
            input.value = 'choice';
        } else {
            input.value = '';
        }
    });

    // 퀴즈 타입 초기화
    const choiceSection = newQuiz.querySelector('.choiceSection');
    const answerSection = newQuiz.querySelector('.answerSection');
    choiceSection.style.display = 'block';
    answerSection.style.display = 'none';

    // 고유 라디오 그룹 이름 설정
    const radioInputs = choiceSection.querySelectorAll('input[type="radio"]');
    const radioName = `choice-${Date.now()}`;
    radioInputs.forEach(r => r.name = radioName);

    // 이벤트 리스너 다시 바인딩
    const select = newQuiz.querySelector('select');
    select.onchange = () => toggleQuizType(select);

    // 새 퀴즈 삽입
    const addButtonWrapper = quizList.querySelector('.quizAddBtnWarp');
    quizList.insertBefore(newQuiz, addButtonWrapper);
}

/**
 * ⬆️ 퀴즈 문항 위로 이동
 */
function moveQuizUp(button) {
    const quiz = button.closest('.quizContent');
    const prev = quiz.previousElementSibling;
    if (prev && prev.classList.contains('quizContent')) {
        prev.before(quiz);
    }
}

/**
 * ⬇️ 퀴즈 문항 아래로 이동
 */
function moveQuizDown(button) {
    const quiz = button.closest('.quizContent');
    const next = quiz.nextElementSibling;
    if (next && next.classList.contains('quizContent')) {
        next.after(quiz);
    }
}

/**
 * 🗑️ 퀴즈 문항 삭제
 */
function removeQuiz(button) {
    const quizList = document.querySelectorAll('.quizContent');
    if (quizList.length <= 1) {
        alert("최소 하나의 문항은 남겨야 합니다.");
        return;
    }

    const quiz = button.closest('.quizContent');
    quiz.remove();
}

/**
 * ➕ 선지 추가
 */
function addChoice(button) {
    const choiceList = button.closest('.choiceList');
    const labels = choiceList.querySelectorAll('label');
    if (labels.length >= 5) {
        alert('선지는 최대 5개까지만 추가할 수 있습니다.');
        return;
    }

    const label = document.createElement('label');
    const radio = document.createElement('input');
    radio.type = 'radio';
    radio.name = `choice-${Date.now()}`;

    const text = document.createElement('input');
    text.type = 'text';
    text.className = 'radio';
    text.placeholder = '선지 내용을 입력하세요';

    label.appendChild(radio);
    label.appendChild(text);

    // 버튼 위에 삽입
    choiceList.insertBefore(label, button.parentElement);
}

// ==================== ✅ 콘텐츠 등록 및 검증 ====================

/**
 * ✅ 입력 검증 후 콘텐츠 등록
 */
function addContent() {
    const currentTab = document.querySelector('.selectBtn.now').textContent.trim();
    
    try {
        let contentData = null;
        
        if (currentTab === '강좌') {
            contentData = validateAndCollectVideoData();
        } else if (currentTab === '퀴즈') {
            contentData = validateAndCollectQuizData();
        } else if (currentTab === '자료') {
            contentData = validateAndCollectFileData();
        }
        
        if (contentData) {
            addContentToList(contentData);
            closeModal();
        }
        
    } catch (error) {
        alert(error.message);
    }
}

/**
 * 🎬 비디오 콘텐츠 데이터 검증 및 수집
 */
function validateAndCollectVideoData() {
    const section = document.getElementById('video');
    const titleInput = section.querySelector('input[placeholder="강좌 제목 입력"]');
    const fileInput = section.querySelector('input[type="file"]');
    const commentInput = section.querySelector('input[placeholder="학습자에게 한 마디"]');

    if (!titleInput.value.trim()) {
        throw new Error("강좌 제목을 입력해주세요.");
    }
    if (!fileInput.files.length) {
        throw new Error("강좌 동영상을 업로드해주세요.");
    }

    return {
        type: 'video',
        title: titleInput.value.trim(),
        file: fileInput.files[0],
        comment: commentInput.value.trim(),
        id: generateContentId()
    };
}

/**
 * 🧩 퀴즈 콘텐츠 데이터 검증 및 수집
 */
function validateAndCollectQuizData() {
    const section = document.getElementById('quiz');
    const titleInput = section.querySelector('input[placeholder="퀴즈 제목 입력"]');
    const commentInput = section.querySelector('input[placeholder="학습자에게 한 마디"]');
    
    if (!titleInput.value.trim()) {
        throw new Error("퀴즈 제목을 입력해주세요.");
    }

    const quizzes = section.querySelectorAll('.quizContent');
    if (quizzes.length === 0) {
        throw new Error("최소 하나의 퀴즈 문항을 추가해주세요.");
    }

    const quizData = [];
    for (let i = 0; i < quizzes.length; i++) {
        const quiz = quizzes[i];
        const questionInput = quiz.querySelector('.quizSection input[type="text"]');
        const type = quiz.querySelector('select').value;

        if (!questionInput.value.trim()) {
            throw new Error(`문항 ${i + 1}의 퀴즈 내용을 입력해주세요.`);
        }

        const quizItem = {
            question: questionInput.value.trim(),
            type: type
        };

        if (type === 'choice') {
            const choices = quiz.querySelectorAll('.choiceSection input[type="text"]');
            const checkedRadio = quiz.querySelector('.choiceSection input[type="radio"]:checked');

            if (choices.length < 2) {
                throw new Error(`문항 ${i + 1}은 최소 2개의 선지를 입력해야 합니다.`);
            }

            let filledChoices = 0;
            const choiceTexts = [];
            choices.forEach(choice => {
                if (choice.value.trim()) {
                    filledChoices++;
                    choiceTexts.push(choice.value.trim());
                }
            });

            if (filledChoices < 2) {
                throw new Error(`문항 ${i + 1}의 선지 내용이 부족합니다.`);
            }

            if (!checkedRadio) {
                throw new Error(`문항 ${i + 1}의 정답을 선택해주세요.`);
            }

            quizItem.choices = choiceTexts;
            quizItem.answer = Array.from(choices).indexOf(checkedRadio.parentElement.querySelector('input[type="text"]'));

        } else if (type === 'answer') {
            const answerInput = quiz.querySelector('.answerSection input[type="text"]');
            if (!answerInput.value.trim()) {
                throw new Error(`문항 ${i + 1}의 정답을 입력해주세요.`);
            }
            quizItem.answer = answerInput.value.trim();
        }

        quizData.push(quizItem);
    }

    return {
        type: 'quiz',
        title: titleInput.value.trim(),
        comment: commentInput.value.trim(),
        quizzes: quizData,
        id: generateContentId()
    };
}

/**
 * 📄 파일 콘텐츠 데이터 검증 및 수집
 */
function validateAndCollectFileData() {
    const section = document.getElementById('file');
    const titleInput = section.querySelector('input[placeholder="자료 제목 입력"]');
    const fileInput = section.querySelector('input[type="file"]');

    if (!titleInput.value.trim()) {
        throw new Error("자료 제목을 입력해주세요.");
    }
    if (!fileInput.files.length) {
        throw new Error("자료 파일을 업로드해주세요.");
    }

    return {
        type: 'file',
        title: titleInput.value.trim(),
        file: fileInput.files[0],
        id: generateContentId()
    };
}

/**
 * 🆔 고유 콘텐츠 ID 생성
 */
function generateContentId() {
    return 'content_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

/**
 * 📋 콘텐츠를 목록에 추가
 */
function addContentToList(contentData) {
    // 배열에 추가
    contentList.push(contentData);
    
    // DOM에 추가
    const classList = document.querySelector('.classList');
    
    // 기존 안내 문구 제거
    if (classList.textContent.includes('아직 콘텐츠가 없습니다.')) {
        classList.textContent = '';
    }

    const classDiv = document.createElement('div');
    classDiv.className = 'class';
    classDiv.dataset.contentId = contentData.id;

    classDiv.innerHTML = `
        <div class="wrap">
            <p class="classTitle">${contentData.title}</p>
        </div>
        <div class="wrap">
            <button type="button" class="btn editBtn">수정</button>
            <button type="button" class="btn" onclick="moveUp(this)">↑</button>
            <button type="button" class="btn" onclick="moveDown(this)">↓</button>
            <button type="button" class="btn" onclick="removeClass(this)">×</button>
        </div>
    `;

    // 아이콘 추가
    const iconMap = {
        'video': 'iconVideo',
        'quiz': 'iconQuiz', 
        'file': 'iconFile'
    };
    
    const iconClone = document.getElementById(iconMap[contentData.type]).cloneNode(true);
    iconClone.classList.add('icon');
    classDiv.querySelector('.wrap').prepend(iconClone);

    classList.appendChild(classDiv);
    updateNumbers();
    
    console.log('📋 콘텐츠 추가됨:', contentData.title, '전체:', contentList.length);
}

// ==================== 🚀 목차 등록 API ====================

/**
 * ✅ 목차 등록 폼 검증 및 제출
 */
function validatePartForm() {
    const titleInput = document.querySelector('input[placeholder="목차 제목 입력"]');
    const goalInput = document.querySelector('input[placeholder="목차의 학습 목표 입력"]');
    const contents = document.querySelectorAll('.classList .class');

    if (!titleInput.value.trim()) {
        alert("목차 제목을 입력해주세요.");
        titleInput.focus();
        return false;
    }

    if (!goalInput.value.trim()) {
        alert("학습 목표를 입력해주세요.");
        goalInput.focus();
        return false;
    }

    if (contents.length === 0) {
        alert("콘텐츠를 최소 1개 이상 등록해주세요.");
        return false;
    }

    // 실제 API 호출
    submitChapter();
    return false; // 기본 폼 제출 방지
}

/**
 * 📤 목차 등록 API 호출
 */
async function submitChapter() {
    try {
        showLoading(true);
        
        const chapterData = {
            lectureId: currentLectureId,
            name: document.querySelector('input[placeholder="목차 제목 입력"]').value.trim(),
            goal: document.querySelector('input[placeholder="목차의 학습 목표 입력"]').value.trim()
        };
        
        console.log('📤 목차 등록 API 호출:', chapterData);
        
        const response = await fetch('/api/lms/regChapter', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(chapterData)
        });

        const result = await response.json();
        
        if (result.success === "true") {
            console.log('✅ 목차 등록 성공:', result);
            alert('목차가 성공적으로 등록되었습니다!');
            
            // 메인 페이지로 이동
            window.location.href = '/lms/main';
        } else {
            throw new Error(result.message || '목차 등록에 실패했습니다.');
        }
        
    } catch (error) {
        console.error('❌ 목차 등록 실패:', error);
        alert('목차 등록 중 오류가 발생했습니다: ' + error.message);
    } finally {
        showLoading(false);
    }
}

/**
 * ⏳ 로딩 표시 함수
 */
function showLoading(show) {
    const submitBtn = document.querySelector('.submit');
    
    if (show) {
        submitBtn.disabled = true;
        submitBtn.textContent = '등록 중...';
        submitBtn.style.opacity = '0.6';
    } else {
        submitBtn.disabled = false;
        submitBtn.textContent = '등록';
        submitBtn.style.opacity = '1';
    }
}

// ==================== 🎯 초기화 ====================

// 페이지 로드 시 라디오 그룹 설정
document.addEventListener('DOMContentLoaded', function() {
    const firstQuiz = document.querySelector('.quizContent');
    if (firstQuiz) {
        const radioInputs = firstQuiz.querySelectorAll('.choiceSection input[type="radio"]');
        const radioGroupName = `choice-${Date.now()}`;
        radioInputs.forEach(r => r.name = radioGroupName);
    }
});

// 🎉 목차 등록 JavaScript 로드 완료!
console.log('📚 목차 등록 JavaScript 로드 완료!');
