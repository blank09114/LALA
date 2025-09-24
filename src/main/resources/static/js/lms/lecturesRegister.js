/**
 * 🎓 강의 등록 페이지 JavaScript
 * 초급 개발자도 이해하기 쉽게 작성된 코드입니다.
 * 
 * 주요 기능:
 * 1. 파일 업로드 처리
 * 2. 카테고리 선택 관리
 * 3. 폼 유효성 검사
 * 4. API 호출 및 강의 등록
 */

 // 🌟 전역 변수 - 선택된 카테고리들을 저장하는 배열
 let selectedCategories = [];

 // 🌟 스마트에디터 객체 (HTML에서 이미 선언됨)
 let Editor = [];

 // 🔧 실제 데이터베이스에 맞춘 카테고리 매핑
 // 이 매핑은 실제 interested 테이블의 데이터와 일치해야 합니다!
 const CATEGORY_ID_MAPPING = {
         // 외국어 (category_id: 1)
         'eng': 'I010',        // 영어
         'jap': 'I011',        // 일본어
         'chi': 'I012',        // 중국어
         'fre': 'I013',        // 프랑스어
         'foreignEct': 'I014', // 기타 언어

         // 예술 (category_id: 2)
         'visual': 'I015',     // 미술
         'sound': 'I016',      // 음악
         'pic': 'I017',        // 사진
         'video': 'I018',      // 영상
         'artEct': 'I020',     // 기타 예술

         // 개발/IT (category_id: 3)
         'web': 'I021',        // 웹개발
         'app': 'I022',        // 앱개발
         'data': 'I023',       // 데이터
         'ai': 'I024',         // 인공지능
         'itEct': 'I025',      // 기타 개발/IT

         // 운동 (category_id: 4)
         'pil': 'I026',        // 요가/필라테스
         'reh': 'I027',        // 재활
         'pit': 'I028',        // 피트니스
         'spo': 'I029',        // 스포츠
         'exerciseEct': 'I030', // 기타 운동

         // 취미 (category_id: 5)
         'cook': 'I031',       // 요리
         'beauty': 'I032',     // 뷰티
         'crafts': 'I033',     // 공예
         'disert': 'I034',     // 디저트
         'favoritEct': 'I035', // 기타 취미

         // 비즈니스 (category_id: 6)
         'startup': 'I036',    // 창업
         'management': 'I037', // 경영
         'finance': 'I038',    // 재무회계
         'marketing': 'I039'   // 마케팅
 };

 function mapCategoryIdToDbId(htmlId) {
     const dbId = CATEGORY_ID_MAPPING[htmlId];
     if (!dbId) {
         console.warn('⚠️ 매핑되지 않은 카테고리 ID:', htmlId);
         console.warn('사용 가능한 매핑:', Object.keys(CATEGORY_ID_MAPPING));
         return null; // 매핑되지 않으면 null 반환
     }
     console.log('🔄 카테고리 ID 변환:', htmlId, '->', dbId);
     return dbId;
 }

// 🌟 전역 변수 - 선택된 카테고리들을 저장하는 배열
//let selectedCategories = [];

// 🌟 스마트에디터 객체 (HTML에서 이미 선언됨)
//let Editor = [];

/**
 * 📁 파일 업로드 처리 함수
 * 사용자가 파일을 선택하면 파일명을 화면에 표시합니다.
 */
window.updateFile = function(input) {
    console.log('📁 파일 업로드 함수 호출됨'); // 디버깅용 로그

    const fileName = document.getElementById('fileName');

    // 파일이 선택되었는지 확인
    if (input.files && input.files.length > 0) {
        const file = input.files[0];

        // 이미지 파일인지 검증
        if (!file.type.startsWith('image/')) {
            alert('이미지 파일만 업로드 가능합니다.');
            input.value = ''; // 파일 선택 초기화
            fileName.textContent = '16:9 비율 권장';
            return;
        }

        // 파일 크기 검증 (5MB 제한)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (file.size > maxSize) {
            alert('파일 크기는 5MB 이하만 업로드 가능합니다.');
            input.value = ''; // 파일 선택 초기화
            fileName.textContent = '16:9 비율 권장';
            return;
        }

        fileName.textContent = file.name;
        console.log('✅ 파일 선택 완료:', file.name);

        // 📷 이미지 미리보기 생성
        showImagePreview(file);
    } else {
        fileName.textContent = '16:9 비율 권장';
    }
};

/**
 * 🏷️ 카테고리 선택 관리 시스템
 * DOM이 로드된 후 실행됩니다.
 */
document.addEventListener("DOMContentLoaded", function() {
    console.log('🚀 카테고리 시스템 초기화 시작');

    // HTML 요소들 가져오기
    const majorBtns = document.querySelectorAll(".majorCate");           // 대분류 버튼들
    const subLists = document.querySelectorAll(".subCate");             // 소분류 리스트들
    const selectedList = document.querySelector(".selectedCateList");   // 선택된 카테고리 표시 영역

    console.log('🔍 대분류 버튼 개수:', majorBtns.length);
    console.log('🔍 소분류 리스트 개수:', subLists.length);

    /**
     * 🔘 대분류 버튼 클릭 이벤트 처리
     */
    majorBtns.forEach(button => {
        button.addEventListener("click", (e) => {
            e.preventDefault(); // 기본 동작 방지

            console.log('🔘 대분류 선택:', button.textContent, 'ID:', button.id);

            const categoryId = button.id + "List"; // 예: "foreign" -> "foreignList"
            console.log('🔍 찾을 소분류 ID:', categoryId);

            // 1. 기존 활성화된 버튼의 스타일 제거
            majorBtns.forEach(btn => btn.classList.remove("activeCate"));

            // 2. 현재 클릭된 버튼 활성화
            button.classList.add("activeCate");

            // 3. 모든 소분류 리스트 숨기기
            subLists.forEach(list => {
                list.style.display = "none";
                console.log('숨김:', list.id);
            });

            // 4. 해당하는 소분류 리스트만 표시
            const targetList = document.getElementById(categoryId);
            if (targetList) {
                targetList.style.display = "grid";
                console.log('✅ 소분류 표시 성공:', categoryId);
            } else {
                console.error('❌ 소분류를 찾을 수 없음:', categoryId);
                // 모든 소분류 요소 확인
                subLists.forEach(list => {
                    console.log('사용 가능한 소분류 ID:', list.id);
                });
            }
        });
    });

    /**
     * 🏷️ 소분류 클릭 이벤트 처리
     */
    subLists.forEach(list => {
        list.addEventListener("click", handleSubCategoryClick);
    });

    /**
     * 🏷️ 소분류 클릭 처리 함수
     */
    function handleSubCategoryClick(event) {
        const li = event.target.closest("li");
        if (!li) return; // li 요소가 아니면 무시

        const categoryId = li.id;
        const categoryName = li.childNodes[0].textContent.trim(); // 카테고리 이름
        const stateDiv = li.querySelector(".state");

        console.log('🏷️ 소분류 클릭:', categoryName, '(ID:', categoryId + ')');

        // 이미 선택된 카테고리인지 확인
        const existingIndex = selectedCategories.findIndex(cat => cat.id === categoryId);

        if (existingIndex !== -1) {
            // 이미 선택되어 있으면 제거
            removeCategory(categoryId, li, stateDiv);
        } else {
            // 새로 선택하는 경우 추가
            addCategory(categoryId, categoryName, li, stateDiv);
        }
    }

    /**
     * ➕ 카테고리 추가 함수
     */
    function addCategory(categoryId, categoryName, li, stateDiv) {
        // 최대 선택 수 제한 (1개만 선택 가능하도록 수정)
        if (selectedCategories.length >= 1) {
            alert("강의는 하나의 카테고리만 선택할 수 있습니다.");
            return;
        }

        // 1. 배열에 추가
        selectedCategories.push({
            id: categoryId,
            name: categoryName
        });

        // 2. UI 업데이트
        li.classList.add("selected");
        stateDiv.textContent = "-";

        // 3. 선택된 카테고리 표시 영역에 추가
        const selectedDiv = document.createElement("div");
        selectedDiv.className = "selectedCate";
        selectedDiv.id = `selected-${categoryId}`;
        selectedDiv.innerHTML = `${categoryName}<div class="state">-</div>`;

        // 4. 클릭 시 제거 이벤트 추가
        selectedDiv.addEventListener("click", () => {
            removeCategory(categoryId, li, stateDiv);
        });

        selectedList.appendChild(selectedDiv);
        
        // 🎯 5. interestedId 필드에 매핑된 값 설정
        const dbInterestedId = mapCategoryIdToDbId(categoryId);
        if (dbInterestedId) {
            document.getElementById('interestedId').value = dbInterestedId;
            console.log('🎯 interestedId 설정됨:', dbInterestedId);
        }

        console.log('✅ 카테고리 추가됨:', categoryName);
        console.log('📋 현재 선택된 카테고리:', selectedCategories);
    }

    /**
     * ➖ 카테고리 제거 함수
     */
    function removeCategory(categoryId, li, stateDiv) {
        // 1. 배열에서 제거
        selectedCategories = selectedCategories.filter(cat => cat.id !== categoryId);

        // 2. UI 업데이트
        li.classList.remove("selected");
        stateDiv.textContent = "+";

        // 3. 선택된 카테고리 표시 영역에서 제거
        const selectedDiv = document.getElementById(`selected-${categoryId}`);
        if (selectedDiv) {
            selectedDiv.remove();
        }
        
        // 🎯 4. interestedId 필드 초기화
        document.getElementById('interestedId').value = '';
        console.log('🎯 interestedId 초기화됨');

        console.log('❌ 카테고리 제거됨:', categoryId);
        console.log('📋 현재 선택된 카테고리:', selectedCategories);
    }
});

/**
 * ✅ 폼 유효성 검사 및 제출 함수
 * 사용자가 "등록하기" 버튼을 클릭했을 때 실행됩니다.
 */
function validateForm() {
    console.log('🔍 폼 유효성 검사 시작');

    // 1. 강의 제목 검증
    const titleInput = document.querySelectorAll('input[type="text"]')[0];
    if (!titleInput.value.trim()) {
        alert("강의 제목을 입력해주세요.");
        titleInput.focus();
        return false;
    }

    // 제목 길이 검증 (최소 5자)
    if (titleInput.value.trim().length < 5) {
        alert("강의 제목은 최소 5자 이상 입력해주세요.");
        titleInput.focus();
        return false;
    }

    // 2. 강의 개요 검증
    const outlineInput = document.querySelectorAll('input[type="text"]')[1];
    if (!outlineInput.value.trim()) {
        alert("강의 개요를 입력해주세요.");
        outlineInput.focus();
        return false;
    }

    // 3. 썸네일 이미지 검증
    const fileInput = document.getElementById("file");
    if (!fileInput.files || fileInput.files.length === 0) {
        alert("썸네일 이미지를 업로드해주세요.");
        return false;
    }

    // 4. 카테고리 선택 검증
    if (selectedCategories.length > 3) {
        alert("카테고리는 최대 3개입니다.");
        return false;
    }

    // 5. 난이도 선택 검증
    const difficultySelect = document.querySelector('select');
    if (!difficultySelect.value) {
        alert("강의 난이도를 선택해주세요.");
        difficultySelect.focus();
        return false;
    }

    // 6. 가격 검증 (선택사항이지만 입력했다면 숫자인지 확인)
    const priceInput = document.querySelectorAll('input[type="text"]')[2]; // 정가 입력 필드
    if (priceInput && priceInput.value.trim()) {
        const price = parseInt(priceInput.value.replace(/[^0-9]/g, ''));
        if (isNaN(price) || price < 0) {
            alert("올바른 가격을 입력해주세요.");
            priceInput.focus();
            return false;
        }
    }

    // 7. 스마트에디터 내용 검증
    if (typeof oEditor !== 'undefined' && oEditor.length > 0) {
        // 스마트에디터 내용을 textarea에 동기화
        oEditor.getById["smartEditor"].exec("UPDATE_CONTENTS_FIELD", []);

        // 비동기 처리로 동기화 후 검증
        setTimeout(function() {
            const smartEditor = document.getElementById("smartEditor");
            const content = smartEditor.value
                .replace(/<[^>]+>/g, "")      // HTML 태그 제거
                .replace(/&nbsp;/g, "")       // 공백 문자 제거
                .trim();

            if (!content) {
                alert("소개글을 입력해주세요.");
                return false;
            }

            // 모든 검증 통과 - 실제 등록 처리
            submitLectureForm();
        }, 100);

        return false; // 기본 폼 제출 방지
    } else {
        // 스마트에디터가 없는 경우 바로 제출
        submitLectureForm();
        return false;
    }
}

/**
 * 📤 실제 강의 등록 API 호출 함수
 * 모든 유효성 검사를 통과한 후 실행됩니다.
 */
async function submitLectureForm() {
    console.log('📤 강의 등록 API 호출 시작');

    try {
        // 1. 로딩 표시
        showLoading(true);

        // 2. 카테고리 검증
        if (!selectedCategories || selectedCategories.length === 0) {
            throw new Error('카테고리가 선택되지 않았습니다.');
        }

        // 3. 카테고리 매핑 검증
        const htmlCategoryId = selectedCategories[0].id;
        const dbInterestedId = mapCategoryIdToDbId(htmlCategoryId);

        if (!dbInterestedId) {
            throw new Error(`선택된 카테고리 '${htmlCategoryId}'가 유효하지 않습니다.`);
        }

        console.log('🗺️ 카테고리 ID 매핑:');
        console.log('  - HTML ID:', htmlCategoryId);
        console.log('  - DB interested_id:', dbInterestedId);

        // 4. 폼 데이터 수집
        const formData = collectFormData();

        // 🎯 핵심 수정: interestedId를 formData에 포함
        formData.interestedId = dbInterestedId;

        console.log('📋 수집된 폼 데이터:', formData);

        // 5. 썸네일 업로드
        console.log('📁 썸네일 업로드 시작...');
        const thumbnailUrl = await uploadThumbnail();
        formData.thumbnail = thumbnailUrl;

        // 6. 🎯 수정된 API URL (파라미터 제거)
        const apiUrl = `/api/lms/regLecture`;
        console.log('📡 API URL:', apiUrl);

        // 7. API 호출 (interestedId가 JSON body에 포함됨)
        const response = await fetch(apiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        console.log('📡 API 응답 상태:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('❌ 서버 오류 응답:', errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        console.log('✅ 응답 데이터:', result);

        if (result.success === true) {
            alert('강의가 성공적으로 등록되었습니다!');
            if (result.lectureId) {
                window.location.href = `/lms/chapter/add/${result.lectureId}`;
            } else {
                window.location.href = '/lms/main';
            }
        } else {
            throw new Error(result.message || '알 수 없는 오류가 발생했습니다.');
        }

    } catch (error) {
        console.error('❌ 강의 등록 실패:', error);
        alert('강의 등록 중 오류가 발생했습니다: ' + error.message);
    } finally {
        showLoading(false);
    }
}

/**
 * 📋 폼 데이터 수집 함수
 * HTML 폼의 모든 입력값을 객체로 만들어 반환합니다.
 */
function collectFormData() {
    console.log('📋 폼 데이터 수집 중...');

    // 입력 필드들 가져오기
    const inputs = document.querySelectorAll('input[type="text"]');
    const difficultySelect = document.querySelector('select');
    const introTextarea = document.getElementById('smartEditor');

    // 할인 관련 입력 필드
    const discountInputs = document.querySelectorAll('.multi-content input[type="number"]');

    const formData = {
        name: inputs[0].value.trim(),                    // 강의 제목
        outline: inputs[1].value.trim(),                 // 강의 개요
        price: parsePrice(inputs[2].value),              // 정가
        discountRate: parseFloat(discountInputs[0]?.value) || 0,  // 할인율
        difficulty: difficultySelect.value,              // 난이도
        introduction: introTextarea.value,               // 소개글
        thumbnail: null  // 나중에 업로드 후 설정
    };

    console.log('📋 수집된 기본 폼 데이터:', formData);
    return formData;
}

/**
 * 💰 가격 파싱 함수
 * 문자열로 입력된 가격을 숫자로 변환합니다.
 */
function parsePrice(priceString) {
    if (!priceString || !priceString.trim()) {
        return 0; // 무료 강의
    }

    // 숫자가 아닌 문자 제거 후 정수로 변환
    const price = parseInt(priceString.replace(/[^0-9]/g, ''));
    return isNaN(price) ? 0 : price;
}

/**
 * 📁 썸네일 업로드 함수
 * 선택된 이미지 파일을 서버에 업로드하고 URL을 반환합니다.
 */
async function uploadThumbnail() {
    console.log('📁 썸네일 업로드 시작');

    const fileInput = document.getElementById("file");
    const file = fileInput.files[0];

    if (!file) {
        throw new Error('업로드할 파일이 없습니다.');
    }

    // FormData 객체 생성 (파일 업로드용)
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/upload/image', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('파일 업로드에 실패했습니다.');
        }

        const result = await response.json();
        console.log('✅ 썸네일 업로드 성공:', result.url);
        return result.url;

    } catch (error) {
        console.error('❌ 썸네일 업로드 실패:', error);
        throw error;
    }
}

/**
 * 📷 이미지 미리보기 표시 함수
 * 사용자가 선택한 이미지를 화면에 미리보기로 보여줍니다.
 */
function showImagePreview(file) {
    const previewContainer = document.getElementById('thumbnailPreview');
    const previewImage = document.getElementById('previewImage');

    if (file && file.type.startsWith('image/')) {
        const reader = new FileReader();

        reader.onload = function(e) {
            previewImage.src = e.target.result;
            previewContainer.style.display = 'block';
            console.log('📷 이미지 미리보기 생성 완료');
        };

        reader.readAsDataURL(file);
    } else {
        // 이미지가 아닌 경우 미리보기 숨기기
        previewContainer.style.display = 'none';
    }
}

/**
 * ⏳ 로딩 표시 함수
 * 사용자에게 처리 중임을 알려줍니다.
 */
function showLoading(show) {
    const submitBtn = document.querySelector('.submitBtn');
    const loadingModal = document.getElementById('loadingModal');

    if (show) {
        submitBtn.disabled = true;
        submitBtn.textContent = '등록 중...';
        submitBtn.style.opacity = '0.6';

        // 로딩 모달 표시
        if (loadingModal) {
            loadingModal.style.display = 'flex';
        }
    } else {
        submitBtn.disabled = false;
        submitBtn.textContent = '등록하기';
        submitBtn.style.opacity = '1';

        // 로딩 모달 숨기기
        if (loadingModal) {
            loadingModal.style.display = 'none';
        }
    }
}

// 🔍 디버깅용 함수 (개발자 도구에서 사용 가능)
window.debugCategories = function() {
    console.log('🔍 카테고리 디버깅 시작');

    // 대분류 버튼 확인
    const majorBtns = document.querySelectorAll(".majorCate");
    console.log('대분류 버튼 개수:', majorBtns.length);
    majorBtns.forEach((btn, index) => {
        console.log(`대분류 ${index + 1}: ID='${btn.id}', 텍스트='${btn.textContent.trim()}'`);
    });

    // 소분류 리스트 확인
    const subLists = document.querySelectorAll(".subCate");
    console.log('소분류 리스트 개수:', subLists.length);
    subLists.forEach((list, index) => {
        console.log(`소분류 ${index + 1}: ID='${list.id}', display='${list.style.display}'`);
    });

    // 예상 매칭 확인
    majorBtns.forEach(btn => {
        const expectedSubId = btn.id + 'List';
        const subList = document.getElementById(expectedSubId);
        console.log(`매칭 확인: '${btn.id}' -> '${expectedSubId}' = ${subList ? '✅ 존재함' : '❌ 없음'}`);
    });
};

// 🎉 초기화 완료 로그
console.log('🎓 강의 등록 JavaScript 로드 완료!');
console.log('🔧 디버깅을 위해 브라우저 콘솔에서 debugCategories() 호출 가능');

window.testCategoryMapping = function() {
    console.log('🧪 === 카테고리 매핑 테스트 ===');

    if (selectedCategories.length === 0) {
        console.log('❌ 선택된 카테고리가 없습니다. 먼저 카테고리를 선택하세요.');
        return;
    }

    selectedCategories.forEach((category, index) => {
        const htmlId = category.id;
        const dbId = mapCategoryIdToDbId(htmlId);

        console.log(`카테고리 ${index + 1}:`);
        console.log(`  - 이름: ${category.name}`);
        console.log(`  - HTML ID: ${htmlId}`);
        console.log(`  - DB ID: ${dbId}`);
    });
};

// 전역 함수로 등록
window.updateFile = updateFile;
window.validateForm = validateForm;
window.submitLectureForm = submitLectureForm;

console.log('🎓 강의 등록 시스템 완전 수정 완료!');
console.log('🔧 디버깅 도구들:');
console.log('  - debugCategories(): 카테고리 시스템 디버깅');
console.log('  - testCategoryMapping(): 카테고리 매핑 테스트');

// 🎯 lectureCreator 방식으로 업데이트된 추가 함수들

// 목차 및 자료 관리를 위한 전역 변수들
let chaptersData = [];      // 목차 데이터 배열
let materialsData = {};     // 자료 데이터 객체 (목차별)

/**
 * 🎯 목차 추가 함수 (lectureCreator 방식)
 */
function addChapter() {
    const index = chaptersData.length;
    const div = document.createElement('div');
    div.className = 'chapter-item';
    div.innerHTML = `
        <h3>목차 ${index + 1}</h3>
        <label>목차 제목*</label>
        <input type="text" id="chapterTitle${index}" placeholder="목차 제목을 입력하세요">

        <label>목차 목표</label>
        <textarea id="chapterObjective${index}" placeholder="이 목차에서 학습할 목표를 설명하세요" rows="3"></textarea>

        <label>순서</label>
        <input type="number" id="chapterOrder${index}" value="${index + 1}" min="1">

        <button type="button" onclick="addMaterial(${index})" class="btn-primary">자료 추가</button>
        <button type="button" onclick="removeChapter(${index})" class="btn-secondary">목차 삭제</button>

        <div id="materialsList${index}"></div>
    `;

    document.getElementById('chaptersList').appendChild(div);
    chaptersData.push({index});
    materialsData[index] = [];

    console.log('📚 목차 추가됨:', index + 1);
}

/**
 * 🗑️ 목차 삭제 함수
 */
function removeChapter(chapterIndex) {
    if (confirm('이 목차와 모든 자료를 삭제하시겠습니까?')) {
        // DOM에서 제거
        const chapterElement = document.querySelector(`#chapterTitle${chapterIndex}`).closest('.chapter-item');
        chapterElement.remove();

        // 데이터에서 제거
        delete materialsData[chapterIndex];
        chaptersData = chaptersData.filter(chapter => chapter.index !== chapterIndex);

        console.log('🗑️ 목차 삭제됨:', chapterIndex + 1);
    }
}

/**
 * 📚 자료 추가 함수 (lectureCreator 방식)
 */
function addMaterial(chapterIndex) {
    const materialIndex = materialsData[chapterIndex].length;
    const div = document.createElement('div');
    div.className = 'material-item';
    div.innerHTML = `
        <h4>자료 ${materialIndex + 1}</h4>

        <label>자료 유형*</label>
        <select id="materialType${chapterIndex}_${materialIndex}" onchange="showMaterialFields(${chapterIndex}, ${materialIndex})">
            <option value="">선택하세요</option>
            <option value="2">영상</option>
            <option value="3">추가자료</option>
            <option value="1">문제</option>
        </select>

        <label>자료 제목*</label>
        <input type="text" id="materialTitle${chapterIndex}_${materialIndex}" placeholder="자료 제목">

        <label>자료 설명</label>
        <textarea id="materialDesc${chapterIndex}_${materialIndex}" placeholder="자료에 대한 설명" rows="2"></textarea>

        <div id="materialSpecific${chapterIndex}_${materialIndex}" class="material-specific"></div>

        <button type="button" onclick="removeMaterial(${chapterIndex}, ${materialIndex})" class="btn-secondary">자료 삭제</button>
    `;

    document.getElementById(`materialsList${chapterIndex}`).appendChild(div);
    materialsData[chapterIndex].push({index: materialIndex});

    console.log('📄 자료 추가됨:', chapterIndex, materialIndex);
}

/**
 * 🗑️ 자료 삭제 함수
 */
function removeMaterial(chapterIndex, materialIndex) {
    if (confirm('이 자료를 삭제하시겠습니까?')) {
        const materialElement = document.querySelector(`#materialType${chapterIndex}_${materialIndex}`).closest('.material-item');
        materialElement.remove();

        materialsData[chapterIndex] = materialsData[chapterIndex].filter(material => material.index !== materialIndex);
        console.log('🗑️ 자료 삭제됨:', chapterIndex, materialIndex);
    }
}

/**
 * 🎨 자료 유형별 필드 표시 (lectureCreator 방식)
 */
function showMaterialFields(chapterIndex, materialIndex) {
    const type = document.getElementById(`materialType${chapterIndex}_${materialIndex}`).value;
    const container = document.getElementById(`materialSpecific${chapterIndex}_${materialIndex}`);

    let html = '';

    if (type === '2') {
        // 🎥 영상 자료
        html = `
            <h5>영상 정보</h5>
            <label>영상 제목</label>
            <input type="text" id="videoName${chapterIndex}_${materialIndex}" placeholder="영상 제목">

            <label>영상 URL</label>
            <input type="url" id="videoAddress${chapterIndex}_${materialIndex}" placeholder="https://...">

            <label>영상 설명</label>
            <textarea id="videoComment${chapterIndex}_${materialIndex}" placeholder="영상에 대한 설명" rows="2"></textarea>
        `;
    } else if (type === '3') {
        // 📎 추가 자료
        html = `
            <h5>파일 정보</h5>
            <label>파일명</label>
            <input type="text" id="fileName${chapterIndex}_${materialIndex}" placeholder="파일명">

            <label>파일 경로/URL</label>
            <input type="text" id="fileAddress${chapterIndex}_${materialIndex}" placeholder="파일 경로 또는 URL">

            <label>파일 형식</label>
            <select id="fileFormat${chapterIndex}_${materialIndex}">
                <option value="pdf">PDF</option>
                <option value="ppt">PPT</option>
                <option value="doc">DOC</option>
                <option value="txt">TXT</option>
                <option value="zip">ZIP</option>
                <option value="jpg">JPG</option>
                <option value="png">PNG</option>
            </select>
        `;
    } else if (type === '1') {
        // ❓ 문제
        html = `
            <h5>문제 정보</h5>
            <label>문제 내용*</label>
            <textarea id="question${chapterIndex}_${materialIndex}" placeholder="문제를 입력하세요" rows="3"></textarea>

            <label>선택지 (형식: 1. A / 2. B / 3. C / 4. D)</label>
            <textarea id="choiceAnswer${chapterIndex}_${materialIndex}"
                placeholder="1. 첫 번째 선택지&#10;2. 두 번째 선택지&#10;3. 세 번째 선택지&#10;4. 네 번째 선택지" rows="4"></textarea>

            <label>문제 유형</label>
            <select id="questionType${chapterIndex}_${materialIndex}">
                <option value="mul">객관식</option>
                <option value="sub">주관식</option>
            </select>

            <label>정답*</label>
            <input type="text" id="answer${chapterIndex}_${materialIndex}"
                placeholder="정답 (객관식: 1,2,3,4 / 주관식: 텍스트)">

            <label>해설</label>
            <textarea id="questionComment${chapterIndex}_${materialIndex}" placeholder="문제 해설" rows="2"></textarea>
        `;
    }

    container.innerHTML = html;
}

// 🔄 기존 submitLectureForm을 lectureCreator 방식으로 업데이트
async function submitLectureFormNew() {
    console.log('📤 강의 등록 API 호출 시작 (lectureCreator 방식)');

    const resultDiv = document.getElementById('result');
    if (resultDiv) {
        resultDiv.style.display = 'block';
        resultDiv.innerHTML = '등록 중...';
    }

    try {
        showLoading(true);

        // 1. 기본 강의 정보 수집
        const data = {
            lectureInfo: {
                name: document.getElementById('lectureTitle').value.trim(),
                outline: document.getElementById('lectureOutline').value.trim(),
                introduction: document.getElementById('smartEditor').value || '',
                thumbnail: 'default.jpg', // 임시값
                price: parseInt(document.getElementById('lecturePrice').value) || 0,
                discountRate: parseInt(document.getElementById('discountRate').value) || 0,
                difficulty: parseInt(document.getElementById('lectureDifficulty').value)
            },
            interestedId: document.getElementById('interestedId').value, // 🎯 선택된 카테고리 ID 사용
            chapters: []
        };

        // 🎯 카테고리 선택 확인
        if (!data.interestedId) {
            throw new Error('카테고리를 선택해주세요.');
        }

        // 2. 썸네일 업로드 (파일이 있는 경우)
        const fileInput = document.getElementById("file");
        if (fileInput.files && fileInput.files.length > 0) {
            console.log('📁 썸네일 업로드 시작...');
            const thumbnailUrl = await uploadThumbnail();
            data.lectureInfo.thumbnail = thumbnailUrl;
        }

        // 3. 목차 및 자료 데이터 수집 (lectureCreator 방식)
        chaptersData.forEach((chapter, chapterIndex) => {
            const titleElement = document.getElementById(`chapterTitle${chapterIndex}`);
            if (!titleElement || !titleElement.value.trim()) return;

            const chapterData = {
                chapterInfo: {
                    name: titleElement.value.trim(),
                    objective: document.getElementById(`chapterObjective${chapterIndex}`)?.value || '',
                    orderNum: parseInt(document.getElementById(`chapterOrder${chapterIndex}`)?.value) || (chapterIndex + 1)
                },
                materials: []
            };

            // 자료 데이터 수집
            materialsData[chapterIndex].forEach((material, materialIndex) => {
                const typeElement = document.getElementById(`materialType${chapterIndex}_${materialIndex}`);
                const titleElement = document.getElementById(`materialTitle${chapterIndex}_${materialIndex}`);

                if (!typeElement || !titleElement || !typeElement.value || !titleElement.value.trim()) return;

                const type = parseInt(typeElement.value);
                const materialData = {
                    materialType: type,
                    contentInfo: {
                        name: titleElement.value.trim(),
                        discription: document.getElementById(`materialDesc${chapterIndex}_${materialIndex}`)?.value || ''
                    }
                };

                // 자료 유형별 세부 정보 추가
                if (type === 2) {
                    // 영상 자료
                    materialData.videoMaterial = {
                        name: document.getElementById(`videoName${chapterIndex}_${materialIndex}`)?.value || '',
                        address: document.getElementById(`videoAddress${chapterIndex}_${materialIndex}`)?.value || '',
                        comment: document.getElementById(`videoComment${chapterIndex}_${materialIndex}`)?.value || ''
                    };
                } else if (type === 3) {
                    // 추가 자료
                    materialData.addiMaterial = {
                        name: document.getElementById(`fileName${chapterIndex}_${materialIndex}`)?.value || '',
                        address: document.getElementById(`fileAddress${chapterIndex}_${materialIndex}`)?.value || '',
                        format: document.getElementById(`fileFormat${chapterIndex}_${materialIndex}`)?.value || 'pdf'
                    };
                } else if (type === 1) {
                    // 문제
                    materialData.questionMaterial = {};
                    materialData.questionDetail = {
                        question: document.getElementById(`question${chapterIndex}_${materialIndex}`)?.value || '',
                        choiceAnswer: document.getElementById(`choiceAnswer${chapterIndex}_${materialIndex}`)?.value || '',
                        type: document.getElementById(`questionType${chapterIndex}_${materialIndex}`)?.value || 'multiple',
                        answer: document.getElementById(`answer${chapterIndex}_${materialIndex}`)?.value || '',
                        comment: document.getElementById(`questionComment${chapterIndex}_${materialIndex}`)?.value || ''
                    };
                }

                chapterData.materials.push(materialData);
            });

            data.chapters.push(chapterData);
        });

        console.log('📋 전송 데이터:', data);

        // 4. API 호출 (lectureCreator와 동일한 엔드포인트 사용)
        const response = await fetch('/api/lms/createCompleteLecture', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(data)
        });

        console.log('📡 API 응답 상태:', response.status);

        const result = await response.json();
        console.log('✅ 응답 데이터:', result);

        // 5. 결과 처리
        if (result.success) {
            if (resultDiv) {
                resultDiv.innerHTML = `
                    <h3>✅ 성공!</h3>
                    <p><strong>강의 ID:</strong> ${result.lectureId}</p>
                    <p><strong>목차 수:</strong> ${result.chapterCount || 0}</p>
                    <p><strong>메시지:</strong> ${result.message}</p>
                `;
            }

            // 3초 후 메인 페이지로 이동
            setTimeout(() => {
                alert('강의가 성공적으로 등록되었습니다!');
                window.location.href = '/lms/main';
            }, 3000);

        } else {
            if (resultDiv) {
                resultDiv.innerHTML = `
                    <h3>❌ 실패</h3>
                    <p><strong>메시지:</strong> ${result.message}</p>
                `;
            }
            alert('강의 등록에 실패했습니다: ' + result.message);
        }

    } catch (error) {
        console.error('❌ 강의 등록 실패:', error);

        if (resultDiv) {
            resultDiv.innerHTML = `
                <h3>🚨 오류</h3>
                <p><strong>메시지:</strong> ${error.message}</p>
            `;
        }

        alert('강의 등록 중 오류가 발생했습니다: ' + error.message);
    } finally {
        showLoading(false);
    }
}

// 새로운 전역 함수들 등록
window.addChapter = addChapter;
window.removeChapter = removeChapter;
window.addMaterial = addMaterial;
window.removeMaterial = removeMaterial;
window.showMaterialFields = showMaterialFields;
window.submitLectureFormNew = submitLectureFormNew;

console.log('🎓 강의 등록 시스템 lectureCreator 방식으로 완전 업데이트 완료!');
console.log('🆕 새로운 기능:');
console.log('  - addChapter(): 목차 추가');
console.log('  - addMaterial(chapterIndex): 자료 추가');
console.log('  - submitLectureFormNew(): 새로운 강의 등록 방식');