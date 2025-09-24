// 카테고리-분야 데이터
    const INTERESTED_DATA = {
        1: [{ id: "I010", name: "영어" }, { id: "I011", name: "일본어" }, { id: "I012", name: "중국어" }, { id: "I013", name: "프랑스어" }, { id: "I014", name: "기타" }],
        2: [{ id: "I015", name: "미술" }, { id: "I016", name: "음악" }, { id: "I017", name: "사진" }, { id: "I018", name: "영상" }, { id: "I020", name: "기타" }],
        3: [{ id: "I021", name: "웹개발" }, { id: "I022", name: "앱개발" }, { id: "I023", name: "데이터" }, { id: "I024", name: "인공지능" }, { id: "I025", name: "기타" }],
        4: [{ id: "I026", name: "요가/필라테스" }, { id: "I027", name: "재활" }, { id: "I028", name: "피트니스" }, { id: "I029", name: "스포츠" }, { id: "I030", name: "기타" }],
        5: [{ id: "I031", name: "요리" }, { id: "I032", name: "뷰티" }, { id: "I033", name: "공예" }, { id: "I034", name: "디저트" }, { id: "I035", name: "기타" }],
        6: [{ id: "I036", name: "창업" }, { id: "I037", name: "경영" }, { id: "I038", name: "재무회계" }, { id: "I039", name: "마케팅" }]
    }

    // 🎬 영상 파일 업로드 함수
    async function uploadVideoFile(file, chapterIndex, materialIndex) {
        const formData = new FormData();
        formData.append('file', file);

        try {
            console.log('🎬 영상 업로드 중:', file.name);
            showUploadProgress(`영상 업로드 중... (${file.name})`);

            const response = await fetch('/api/lms/upload/video', {
                method: 'POST',
                body: formData
            });

            const result = await response.json();

            if (result.success) {
                console.log('✅ 영상 업로드 성공:', result.url);
                
                // 업로드된 URL을 해당 입력 필드에 자동 입력
                const videoAddressInput = document.getElementById(`videoAddress${chapterIndex}_${materialIndex}`);
                if (videoAddressInput) {
                    videoAddressInput.value = result.url;
                }

                showUploadSuccess('✅ 영상 업로드 완료! (S3 버킷에 저장됨)');
                return result.url;
                
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('❌ 영상 업로드 실패:', error);
            alert('영상 업로드 실패: ' + error.message);
            throw error;
        } finally {
            hideUploadProgress();
        }
    }

    // 📄 자료 파일 업로드 함수
    async function uploadMaterialFile(file, chapterIndex, materialIndex) {
        const formData = new FormData();
        formData.append('file', file);

        try {
            console.log('📄 자료 업로드 중:', file.name);
            showUploadProgress(`자료 업로드 중... (${file.name})`);

            const response = await fetch('/api/lms/upload/material', {
                method: 'POST',
                body: formData
            });

            const result = await response.json();

            if (result.success) {
                console.log('✅ 자료 업로드 성공:', result.url);
                
                // 업로드된 URL을 해당 입력 필드에 자동 입력
                const fileAddressInput = document.getElementById(`fileAddress${chapterIndex}_${materialIndex}`);
                if (fileAddressInput) {
                    fileAddressInput.value = result.url;
                }
                
                // 파일명도 자동 입력
                const fileNameInput = document.getElementById(`fileName${chapterIndex}_${materialIndex}`);
                if (fileNameInput && !fileNameInput.value) {
                    fileNameInput.value = result.originalName;
                }

                showUploadSuccess('✅ 자료 업로드 완료! (S3 버킷에 저장됨)');
                return result.url;
                
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('❌ 자료 업로드 실패:', error);
            alert('자료 업로드 실패: ' + error.message);
            throw error;
        } finally {
            hideUploadProgress();
        }
    }

    // 📱 UI 상태 관리 함수들
    function showUploadProgress(message) {
        // 기존 메시지가 있으면 제거
        hideUploadProgress();
        
        const progressDiv = document.createElement('div');
        progressDiv.id = 'uploadProgress';
        progressDiv.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: rgba(0, 0, 0, 0.8);
            color: white;
            padding: 20px 30px;
            border-radius: 8px;
            z-index: 9999;
            font-size: 16px;
            display: flex;
            align-items: center;
            gap: 15px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.3);
        `;
        
        // 로딩 스피너 추가
        const spinner = document.createElement('div');
        spinner.style.cssText = `
            width: 24px;
            height: 24px;
            border: 3px solid rgba(255, 255, 255, 0.3);
            border-top: 3px solid white;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        `;
        
        progressDiv.appendChild(spinner);
        progressDiv.appendChild(document.createTextNode(message));
        
        // CSS 애니메이션 추가
        if (!document.getElementById('spinnerStyle')) {
            const style = document.createElement('style');
            style.id = 'spinnerStyle';
            style.textContent = `
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            `;
            document.head.appendChild(style);
        }
        
        document.body.appendChild(progressDiv);
    }

    function hideUploadProgress() {
        const progressDiv = document.getElementById('uploadProgress');
        if (progressDiv) {
            progressDiv.remove();
        }
    }

    function showUploadSuccess(message) {
        const successDiv = document.createElement('div');
        successDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: linear-gradient(135deg, #4CAF50, #45a049);
            color: white;
            padding: 15px 20px;
            border-radius: 8px;
            z-index: 9999;
            font-size: 14px;
            box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
            animation: slideInRight 0.3s ease-out;
        `;
        
        successDiv.textContent = message;
        
        // CSS 애니메이션 추가
        if (!document.getElementById('successAnimationStyle')) {
            const style = document.createElement('style');
            style.id = 'successAnimationStyle';
            style.textContent = `
                @keyframes slideInRight {
                    from {
                        transform: translateX(100%);
                        opacity: 0;
                    }
                    to {
                        transform: translateX(0);
                        opacity: 1;
                    }
                }
                @keyframes slideOutRight {
                    from {
                        transform: translateX(0);
                        opacity: 1;
                    }
                    to {
                        transform: translateX(100%);
                        opacity: 0;
                    }
                }
            `;
            document.head.appendChild(style);
        }
        
        document.body.appendChild(successDiv);
        
        // 3초 후 애니메이션과 함께 제거
        setTimeout(() => {
            if (successDiv.parentNode) {
                successDiv.style.animation = 'slideOutRight 0.3s ease-in';
                setTimeout(() => {
                    if (successDiv.parentNode) {
                        successDiv.remove();
                    }
                }, 300);
            }
        }, 3000);
    };

    // 📝 Quill 에디터 초기화
    let quillEditor;

    // 전역 변수
    let chaptersData = [];
    let materialsData = {};
    let uploadedThumbnailUrl = 'whatyouwant.jpg';
    let selectedMainCategoryId = null; // 선택된 주 카테고리 ID
    let selectedInterestedIds = new Set(); // 선택된 분야 ID들을 저장할 Set

    // 페이지 로드 후 에디터 초기화 및 이벤트 리스너 설정
    document.addEventListener('DOMContentLoaded', function() {
        // Quill 에디터 설정
        quillEditor = new Quill('#lectureIntroductionEditor', {
            theme: 'snow',  // 테마 (snow 또는 bubble)
            placeholder: '강의에 대한 상세한 설명을 작성해주세요...',
            modules: {
                toolbar: [
                    [{ 'header': [1, 2, 3, false] }],
                    ['bold', 'italic', 'underline', 'strike'],
                    [{ 'color': [] }, { 'background': [] }],
                    [{ 'align': [] }],
                    [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                    [{ 'indent': '-1'}, { 'indent': '+1' }],
                    ['link', 'image'],
                    ['clean']
                ]
            }
        });

        console.log('✅ Quill 에디터 초기화 완료');

        // 메인 카테고리 라디오 버튼에 이벤트 리스너 추가
        document.querySelectorAll('input[name="categoryId"]').forEach(radio => {
            radio.addEventListener('change', function() {
                selectedMainCategoryId = this.value; // 주 카테고리 업데이트
                selectedInterestedIds.clear(); // 주 카테고리 변경 시 분야 초기화
                updateInterestedOptions(selectedMainCategoryId);
                updateSelectedCategoriesDisplay(); // 선택된 항목 업데이트
            });
        });

        // 초기 난이도 선택 (입문 기본 선택)
        document.getElementById('difficulty0').checked = true;

                                                    //썸네일 aws 버킷에 올리는거 해야함 ㅅㅂ
        // 썸네일 업로드 이벤트 리스너
        document.getElementById('thumbnailInput').addEventListener('change', async (event) => {
            const file = event.target.files[0];
            if (!file) return;

            // 미리보기
            const preview = document.getElementById('thumbnailPreview');
            const reader = new FileReader();
            reader.onload = e => {
                preview.src = e.target.result;
                preview.style.display = 'block';
            };
            reader.readAsDataURL(file);

            // 🔥 서버에 실제 업로드 (S3 버킷에 업로드)
            const formData = new FormData();
            formData.append('file', file);

            try {
                console.log('🖼️ 썸네일 S3 업로드 중...');
                showUploadProgress('썸네일 업로드 중...');

                const response = await fetch('/api/lms/upload/thumbnail', {
                    method: 'POST',
                    body: formData
                });

                const result = await response.json();

                if (result.success) {
                    // 업로드 성공 - 서버에서 반환한 S3 URL 저장
                    uploadedThumbnailUrl = result.url;
                    console.log('✅ 썸네일 S3 업로드 성공:', uploadedThumbnailUrl);

                    // 사용자에게 성공 메시지 표시
                    showUploadSuccess('✅ 썸네일 업로드 완료! (S3 버킷에 저장됨)');

                } else {
                    // 업로드 실패
                    alert('썸네일 업로드 실패: ' + result.message);
                    console.error('❌ 업로드 실패:', result);
                    uploadedThumbnailUrl = ''; // 실패시 초기화
                }
            } catch (err) {
                // 네트워크 오류 등
                console.error('❌ 썸네일 업로드 오류:', err);
                alert('썸네일 업로드 중 오류가 발생했습니다. 다시 시도해주세요.');
                uploadedThumbnailUrl = ''; // 오류시 초기화
            } finally {
                hideUploadProgress();
            }
        });
    });

    // 카테고리 선택 시 분야 옵션 업데이트 및 클릭 이벤트 설정
    function updateInterestedOptions(categoryId) {
        const interestedGrid = document.getElementById('interestedIdGrid');
        interestedGrid.innerHTML = ''; // 기존 옵션 초기화

        if (categoryId && INTERESTED_DATA[categoryId]) {
            INTERESTED_DATA[categoryId].forEach(item => {
                const itemDiv = document.createElement('div');
                itemDiv.classList.add('category-item');
                itemDiv.innerHTML = `
                    <input type="checkbox" id="interested_${item.id}" name="interestedId" value="${item.id}" class="category-radio">
                    <label for="interested_${item.id}" class="category-label">
                        <span class="category-text">${item.name}</span>
                    </label>
                `;
                interestedGrid.appendChild(itemDiv);

                // 체크박스 변경 시 selectedInterestedIds 업데이트
                const checkbox = itemDiv.querySelector(`#interested_${item.id}`);
                checkbox.addEventListener('change', function() {
                    if (this.checked) {
                        if (selectedInterestedIds.size >= 2) {
                            alert('분야는 최대 2개까지만 선택할 수 있습니다.');
                            this.checked = false; // Prevent selection
                            return;
                        }
                        selectedInterestedIds.add(this.value);
                    } else {
                        selectedInterestedIds.delete(this.value);
                    }
                    updateSelectedCategoriesDisplay();
                });
            });
        }
    }

    // 선택된 카테고리 및 분야를 '선택된 항목' 섹션에 표시하는 함수
    function updateSelectedCategoriesDisplay() {
        const container = document.getElementById('selectedCategoriesContainer');
        container.innerHTML = ''; // 초기화

        // 선택된 주 카테고리 표시
        if (selectedMainCategoryId) {
            const mainCategoryLabel = document.querySelector(`label[data-category-id="${selectedMainCategoryId}"] .category-text`).textContent;
            const span = document.createElement('span');
            span.classList.add('selected-tag');
            span.textContent = mainCategoryLabel;
            const removeBtn = document.createElement('button');
            removeBtn.classList.add('remove-tag-btn');
            removeBtn.textContent = 'x';
            removeBtn.onclick = () => {
                selectedMainCategoryId = null;
                document.querySelector(`input[name="categoryId"]:checked`).checked = false;
                selectedInterestedIds.clear(); // 주 카테고리 해제 시 분야도 초기화
                updateInterestedOptions(null); // 분야 옵션도 지움
                updateSelectedCategoriesDisplay();
            };
            span.appendChild(removeBtn);
            container.appendChild(span);
        }

        // 선택된 분야 표시
        selectedInterestedIds.forEach(interestedId => {
            const interestedLabel = document.querySelector(`label[for="interested_${interestedId}"] .category-text`).textContent;
            const span = document.createElement('span');
            span.classList.add('selected-tag');
            span.textContent = interestedLabel;
            const removeBtn = document.createElement('button');
            removeBtn.classList.add('remove-tag-btn');
            removeBtn.textContent = 'x';
            removeBtn.onclick = () => {
                selectedInterestedIds.delete(interestedId);
                const checkbox = document.getElementById(`interested_${interestedId}`);
                if (checkbox) checkbox.checked = false;
                updateSelectedCategoriesDisplay();
            };
            span.appendChild(removeBtn);
            container.appendChild(span);
        });

        if (!selectedMainCategoryId && selectedInterestedIds.size === 0) {
             container.textContent = '선택된 항목이 없습니다.'; // 아무것도 선택되지 않았을 때 메시지
             container.classList.add('no-selection-message');
        } else {
             container.classList.remove('no-selection-message');
        }
    }


    // 목차 추가 함수
    function addChapter() {
        const index = chaptersData.length;
        const chapterHtml = `
            <div class="chapter-item" id="chapterItem${index}">
                <div class="chapter-header">
                    <h3 class="chapter-title">목차 ${index + 1}</h3>
                    <button type="button" class="btn btn-danger btn-info" onclick="removeChapter(${index})">삭제</button>
                </div>
                <div class="form-group">
                    <label for="chapterTitle${index}" class="form-label">목차 제목<span class="required">*</span></label>
                    <input type="text" id="chapterTitle${index}" class="form-input" placeholder="예: 챕터 1. 강의 소개">
                </div>
                <div class="form-group">
                    <label for="chapterObjective${index}" class="form-label">목차 목표</label>
                    <textarea id="chapterObjective${index}" class="form-input form-textarea" placeholder="이 목차에서 학습할 목표를 작성하세요"></textarea>
                </div>
                <div class="form-group">
                    <label for="chapterOrder${index}" class="form-label">순서</label>
                    <input type="number" id="chapterOrder${index}" class="form-input" value="${index + 1}" min="1">
                </div>
                <button type="button" onclick="addMaterial(${index})" class="btn btn-secondary btn-info">자료 추가</button>
                <div id="materialsList${index}"></div>
            </div>
        `;
        document.getElementById('chaptersList').insertAdjacentHTML('beforeend', chapterHtml);
        chaptersData.push({index});
        materialsData[index] = [];
    }

    // 목차 제거 함수
    function removeChapter(indexToRemove) {
        const chapterElement = document.getElementById(`chapterItem${indexToRemove}`);
        if (chapterElement) {
            chapterElement.remove();
            chaptersData = chaptersData.filter(chapter => chapter.index !== indexToRemove);
            delete materialsData[indexToRemove];
            // Adjust order numbers and titles for remaining chapters if needed (optional, for visual consistency)
            document.querySelectorAll('.chapter-item').forEach((item, newIndex) => {
                item.id = `chapterItem${newIndex}`;
                item.querySelector('.chapter-title').textContent = `목차 ${newIndex + 1}`;
                const chapterOrderInput = item.querySelector(`#chapterOrder${item.dataset.originalIndex}`); // Assuming you store original index
                if (chapterOrderInput) {
                    chapterOrderInput.id = `chapterOrder${newIndex}`;
                    chapterOrderInput.value = newIndex + 1;
                }
                item.querySelectorAll('[id^="materialsList"]').forEach(matList => {
                    matList.id = `materialsList${newIndex}`;
                });
            });
            // Re-index chaptersData and materialsData based on new visual order if strict sequential indexing is desired
        }
    }


    // 자료 추가 함수
    function addMaterial(chapterIndex) {
        const materialIndex = materialsData[chapterIndex].length;
        const materialHtml = `
            <div class="material-item" id="materialItem${chapterIndex}_${materialIndex}">
                <div class="material-header">
                    <h4 class="material-title">자료 ${materialIndex + 1}</h4>
                    <button type="button" class="btn btn-danger btn-info" onclick="removeMaterial(${chapterIndex}, ${materialIndex})">삭제</button>
                </div>
                <div class="form-group">
                    <label for="materialType${chapterIndex}_${materialIndex}" class="form-label">자료 유형<span class="required">*</span></label>
                    <select id="materialType${chapterIndex}_${materialIndex}" class="form-input" onchange="showMaterialFields(${chapterIndex}, ${materialIndex})">
                        <option value="">선택</option>
                        <option value="1">영상</option>
                        <option value="2">추가자료</option>
                        <option value="3">문제</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="materialTitle${chapterIndex}_${materialIndex}" class="form-label">자료 제목<span class="required">*</span></label>
                    <input type="text" id="materialTitle${chapterIndex}_${materialIndex}" class="form-input">
                </div>
                <div class="form-group">
                    <label for="materialDesc${chapterIndex}_${materialIndex}" class="form-label">자료 설명</label>
                    <textarea id="materialDesc${chapterIndex}_${materialIndex}" class="form-input form-textarea"></textarea>
                </div>
                <div id="materialSpecific${chapterIndex}_${materialIndex}"></div>
            </div>
        `;
        document.getElementById(`materialsList${chapterIndex}`).insertAdjacentHTML('beforeend', materialHtml);
        materialsData[chapterIndex].push({index: materialIndex});
    }

    // 자료 제거 함수
    function removeMaterial(chapterIndex, materialIndexToRemove) {
        const materialElement = document.getElementById(`materialItem${chapterIndex}_${materialIndexToRemove}`);
        if (materialElement) {
            materialElement.remove();
            materialsData[chapterIndex] = materialsData[chapterIndex].filter(material => material.index !== materialIndexToRemove);
            // Re-index remaining materials visually if desired
            document.querySelectorAll(`#materialsList${chapterIndex} .material-item`).forEach((item, newIndex) => {
                item.id = `materialItem${chapterIndex}_${newIndex}`;
                item.querySelector('.material-title').textContent = `자료 ${newIndex + 1}`;
                // Update IDs of inputs/selects inside here as well if strict re-indexing is needed
                // This part can be complex depending on how precise you want the re-indexing.
                // For simplicity, we might just re-render the whole material list or rely on the data structure for submission.
            });
        }
    }


    // 자료 유형별 필드 표시
    function showMaterialFields(chapterIndex, materialIndex) {
        const type = document.getElementById(`materialType${chapterIndex}_${materialIndex}`).value;
        const container = document.getElementById(`materialSpecific${chapterIndex}_${materialIndex}`);

        let html = '';
        if (type === '1') {
            html = `
                <div class="form-group">
                    <label for="videoName${chapterIndex}_${materialIndex}" class="form-label">영상 제목</label>
                    <input type="text" id="videoName${chapterIndex}_${materialIndex}" class="form-input">
                </div>
                <div class="form-group">
                    <label for="videoFile${chapterIndex}_${materialIndex}" class="form-label">🎬 영상 파일 업로드</label>
                    <input type="file" id="videoFile${chapterIndex}_${materialIndex}" class="form-input" accept="video/*">
                    <small style="color: #666;">또는 아래에 직접 URL을 입력하세요</small>
                </div>
                <div class="form-group">
                    <label for="videoAddress${chapterIndex}_${materialIndex}" class="form-label">영상 URL</label>
                    <input type="url" id="videoAddress${chapterIndex}_${materialIndex}" class="form-input" placeholder="업로드하거나 직접 URL 입력">
                </div>
                <div class="form-group">
                    <label for="videoComment${chapterIndex}_${materialIndex}" class="form-label">영상 설명</label>
                    <textarea id="videoComment${chapterIndex}_${materialIndex}" class="form-input form-textarea"></textarea>
                </div>
            `;
        } else if (type === '2') {
            html = `
                <div class="form-group">
                    <label for="fileName${chapterIndex}_${materialIndex}" class="form-label">파일명</label>
                    <input type="text" id="fileName${chapterIndex}_${materialIndex}" class="form-input">
                </div>
                <div class="form-group">
                    <label for="materialFile${chapterIndex}_${materialIndex}" class="form-label">📄 자료 파일 업로드</label>
                    <input type="file" id="materialFile${chapterIndex}_${materialIndex}" class="form-input">
                    <small style="color: #666;">또는 아래에 직접 경로를 입력하세요</small>
                </div>
                <div class="form-group">
                    <label for="fileAddress${chapterIndex}_${materialIndex}" class="form-label">파일 경로</label>
                    <input type="text" id="fileAddress${chapterIndex}_${materialIndex}" class="form-input" placeholder="업로드하거나 직접 경로 입력">
                </div>
                <div class="form-group">
                    <label for="fileFormat${chapterIndex}_${materialIndex}" class="form-label">파일 형식</label>
                    <select id="fileFormat${chapterIndex}_${materialIndex}" class="form-input">
                        <option value="pdf">PDF</option>
                        <option value="ppt">PPT</option>
                        <option value="doc">DOC</option>
                        <option value="txt">TXT</option>
                        <option value="zip">ZIP</option>
                    </select>
                </div>
            `;
        } else if (type === '3') { // 문제
            html = `
                <div class="form-group">
                    <label for="question${chapterIndex}_${materialIndex}" class="form-label">문제 내용</label>
                    <textarea id="question${chapterIndex}_${materialIndex}" class="form-input form-textarea" placeholder="문제를 입력하세요"></textarea>
                </div>
                <div class="form-group">
                    <label for="choiceAnswer${chapterIndex}_${materialIndex}" class="form-label">선택지 (형식: 1. A 2. B 3. C 4. D)</label>
                    <textarea id="choiceAnswer${chapterIndex}_${materialIndex}" class="form-input form-textarea" placeholder="1. 첫 번째 선택지&#10;2. 두 번째 선택지&#10;3. 세 번째 선택지&#10;4. 네 번째 선택지"></textarea>
                </div>
                <div class="form-group">
                    <label for="questionType${chapterIndex}_${materialIndex}" class="form-label">문제 유형</label>
                    <select id="questionType${chapterIndex}_${materialIndex}" class="form-input">
                        <option value="multiple">객관식</option>
                        <option value="essay">주관식</option>
                        <option value="truefalse">O/X</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="answer${chapterIndex}_${materialIndex}" class="form-label">정답</label>
                    <input type="text" id="answer${chapterIndex}_${materialIndex}" class="form-input" placeholder="정답 (객관식: 1,2,3,4 / 주관식: 텍스트)">
                </div>
                <div class="form-group">
                    <label for="questionComment${chapterIndex}_${materialIndex}" class="form-label">해설</label>
                    <textarea id="questionComment${chapterIndex}_${materialIndex}" class="form-input form-textarea" placeholder="문제 해설"></textarea>
                </div>
            `;
        }
        
        container.innerHTML = html;

        // 🔥 파일 업로드 이벤트 리스너 추가 (DOM에 HTML이 삽입된 후 실행)
        setTimeout(() => {
            if (type === '1') { // 영상
                const videoFileInput = document.getElementById(`videoFile${chapterIndex}_${materialIndex}`);
                if (videoFileInput) {
                    videoFileInput.addEventListener('change', async (event) => {
                        const file = event.target.files[0];
                        if (file) {
                            try {
                                console.log('📹 영상 파일 선택됨:', file.name);
                                await uploadVideoFile(file, chapterIndex, materialIndex);
                            } catch (error) {
                                console.error('영상 파일 업로드 실패:', error);
                            }
                        }
                    });
                    console.log('✅ 영상 파일 업로드 이벤트 리스너 등록됨');
                } else {
                    console.warn('❌ 영상 파일 input을 찾을 수 없습니다:', `videoFile${chapterIndex}_${materialIndex}`);
                }
            } else if (type === '2') { // 추가자료
                const materialFileInput = document.getElementById(`materialFile${chapterIndex}_${materialIndex}`);
                if (materialFileInput) {
                    materialFileInput.addEventListener('change', async (event) => {
                        const file = event.target.files[0];
                        if (file) {
                            try {
                                console.log('📄 자료 파일 선택됨:', file.name);
                                await uploadMaterialFile(file, chapterIndex, materialIndex);
                            } catch (error) {
                                console.error('자료 파일 업로드 실패:', error);
                            }
                        }
                    });
                    console.log('✅ 자료 파일 업로드 이벤트 리스너 등록됨');
                } else {
                    console.warn('❌ 자료 파일 input을 찾을 수 없습니다:', `materialFile${chapterIndex}_${materialIndex}`);
                }
            }
        }, 100); // 짧은 딜레이로 DOM 업데이트 보장
    }


    // 📤 강의 등록 함수
    async function submitLecture() {
        const resultDiv = document.getElementById('result');
        resultDiv.innerHTML = '<div class="loading">강의 등록 중...</div>';
        resultDiv.style.display = 'block'; // Show loading message

        try {
            // 🎯 Quill 에디터에서 HTML 내용 가져오기
            const editorHtml = quillEditor.root.innerHTML;

            // 필수 입력 필드 유효성 검사 (개선된 UI에 맞게)
            const lectureName = document.getElementById('lectureName').value;
            const lectureOutline = document.getElementById('lectureOutline').value;
            const thumbnail = uploadedThumbnailUrl;
            const selectedDifficulty = document.querySelector('input[name="difficulty"]:checked')?.value;

            if (!lectureName || !lectureOutline || !editorHtml || !thumbnail || !selectedMainCategoryId || selectedInterestedIds.size === 0 || !selectedDifficulty) {
                resultDiv.innerHTML = `<div class="result error">필수 입력 항목을 모두 채워주세요 (강의 제목, 개요, 상세, 썸네일, 카테고리, 분야, 난이도).</div>`;
                return;
            }

            // 강의 등록 데이터 구성
            const data = {
                lectureInfo: {
                    name: lectureName,
                    outline: lectureOutline,
                    introduction: editorHtml,
                    thumbnail: thumbnail,
                    price: parseInt(document.getElementById('lecturePrice').value) || 0,
                    discountRate: parseInt(document.getElementById('discountRate').value) || 0,
                    difficulty: parseInt(selectedDifficulty)
                },
                // 관심 분야는 Set에서 배열로 변환하여 전송
                interestedIds: Array.from(selectedInterestedIds),
                chapters: []
            };

            // 목차 데이터 수집
            chaptersData.forEach((chapter, chapterIndex) => {
                const chapterTitleInput = document.getElementById(`chapterTitle${chapterIndex}`);
                if (!chapterTitleInput || !chapterTitleInput.value) {
                    console.warn(`Chapter ${chapterIndex + 1} has no title and will be skipped.`);
                    return; // Skip chapter if title is empty
                }

                const chapterData = {
                    chapterInfo: {
                        name: chapterTitleInput.value,
                        objective: document.getElementById(`chapterObjective${chapterIndex}`)?.value || '',
                        orderNum: parseInt(document.getElementById(`chapterOrder${chapterIndex}`)?.value) || (chapterIndex + 1)
                    },
                    materials: []
                };

                // 자료 데이터 수집
                materialsData[chapterIndex].forEach((material, materialIndex) => {
                    const typeSelect = document.getElementById(`materialType${chapterIndex}_${materialIndex}`);
                    const materialTitleInput = document.getElementById(`materialTitle${chapterIndex}_${materialIndex}`);

                    if (!typeSelect || !typeSelect.value || !materialTitleInput || !materialTitleInput.value) {
                        console.warn(`Material ${materialIndex + 1} in Chapter ${chapterIndex + 1} has missing type or title and will be skipped.`);
                        return; // Skip material if type or title is empty
                    }

                    const materialData = {
                        materialType: parseInt(typeSelect.value),
                        contentInfo: {
                            name: materialTitleInput.value,
                            discription: document.getElementById(`materialDesc${chapterIndex}_${materialIndex}`)?.value || ''
                        }
                    };

                    if (typeSelect.value === '1') { // 영상
                        materialData.videoMaterial = {
                            name: document.getElementById(`videoName${chapterIndex}_${materialIndex}`)?.value || '',
                            address: document.getElementById(`videoAddress${chapterIndex}_${materialIndex}`)?.value || '',
                            comment: document.getElementById(`videoComment${chapterIndex}_${materialIndex}`)?.value || ''
                        };
                    } else if (typeSelect.value === '2') { // 추가자료
                        materialData.addiMaterial = {
                            name: document.getElementById(`fileName${chapterIndex}_${materialIndex}`)?.value || '',
                            address: document.getElementById(`fileAddress${chapterIndex}_${materialIndex}`)?.value || '',
                            format: document.getElementById(`fileFormat${chapterIndex}_${materialIndex}`)?.value || 'pdf'
                        };
                    } else if (typeSelect.value === '3') { // 문제
                        materialData.questionMaterial = {}; // Empty object as per your existing structure
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

            console.log('📤 전송 데이터:', data);

            // API 호출
            const response = await fetch('/api/lms/createCompleteLecture', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (result.success) {
                resultDiv.innerHTML = `
                    <div class="result success">
                        <h3>✅ 성공!</h3>
                        <p><strong>강의 ID:</strong> ${result.lectureId}</p>
                        <p><strong>목차 수:</strong> ${result.chapterCount || 0}</p>
                        <p><strong>메시지:</strong> ${result.message}</p>
                        <details>
                            <summary>상세 결과 보기</summary>
                            <pre>${JSON.stringify(result, null, 2)}</pre>
                        </details>
                    </div>
                `;
            } else {
                resultDiv.innerHTML = `
                    <div class="result error">
                        <h3>❌ 실패</h3>
                        <p><strong>메시지:</strong> ${result.message}</p>
                        <details>
                            <summary>오류 상세 정보</summary>
                            <pre>${JSON.stringify(result, null, 2)}</pre>
                        </details>
                    </div>
                `;
            }
        } catch (error) {
            resultDiv.innerHTML = `
                <div class="result error">
                    <h3>🚨 오류</h3>
                    <p><strong>메시지:</strong> ${error.message}</p>
                    <p>콘솔을 확인하세요.</p>
                </div>
            `;
            console.error('강의 등록 오류:', error);
        }
    }