/**
 * LMS 메인 페이지 JavaScript
 * 핵심 기능:
 * 1. 최근 강의 정보 표시 (last_update_date 기준)
 * 2. 수강률 0%일 때 취소 버튼, 1% 이상일 때 진도율 바
 * 3. 목차별 자료 클릭 기능
 * 4. 진도율 정확성 개선 및 썸네일 이미지 처리 개선
 */

// 전역 변수
let currentLectureData = null;
let currentUserType = null;
let progressData = null;
let isAllExpanded = false;
let allChapters = null;

/**
 * 페이지 로드 시 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('[LMS] 🚀 페이지 초기화 시작');
    initializePage();
});

/**
 * 🎯 페이지 초기화 함수 (수정됨)
 */
async function initializePage() {
    try {
        showLoading();

        // 1. 최근 강의 정보 로드
        console.log('[LMS] 📖 최근 강의 정보 로드 중...');
        await loadRecentLecture();

        // 2. 진도율 정보 로드 (사용자가 있고 강의 ID가 있는 경우)
        if (currentLectureData && currentLectureData.lectureId) {
            console.log('[LMS] 📊 진도율 정보 로드 중...');
            await loadProgressInfo(currentLectureData.lectureId);
        }

        // 3. 강의 목차 및 자료 로드
        console.log('[LMS] 📚 강의 목차 및 자료 로드 중...');
        await loadLectureChapters();

        // 4. UI 업데이트
        console.log('[LMS] 🎨 UI 업데이트 중...');
        updateUserInterface();

        hideLoading();
        console.log('[LMS] ✅ 페이지 초기화 완료!');

    } catch (error) {
        console.error('[LMS] ❌ 페이지 초기화 실패:', error);
        hideLoading();
        showToast('페이지 로드에 실패했습니다. 새로고침해주세요.', 'error');
    }
}

/**
 * 🎯 최근 강의 정보 로드 (수정됨)
 */
async function loadRecentLecture() {
    try {
        const response = await fetch('/api/lms/myRecentLecture');

        if (!response.ok) {
            throw new Error(`서버 오류: ${response.status}`);
        }

        const data = await response.json();
        console.log('[LMS] 📡 서버 응답 데이터:', data);

        // 강의 데이터 추출
        if (data.lecture) {
            currentLectureData = data.lecture;
        } else {
            currentLectureData = data;
        }

        // 사용자 타입 추출
        if (data.userInfo) {
            currentUserType = data.userInfo.accountType || 0;
        } else {
            currentUserType = 0;
        }

        console.log('[LMS] 💾 강의 데이터 저장 완료:', {
            lectureId: currentLectureData?.lectureId,
            title: currentLectureData?.title,
            userType: currentUserType
        });

        // 🎯 강의 헤더 정보 즉시 업데이트
        updateLectureHeader(currentLectureData);

    } catch (error) {
        console.error('[LMS] ❌ 강의 정보 로드 실패:', error);
        throw error;
    }
}

/**
 * 🎯 강의 헤더 정보 업데이트 (핵심 수정)
 */
function updateLectureHeader(lectureData) {
    console.log('[LMS] 🎨 강의 헤더 업데이트 시작:', lectureData);

    if (!lectureData) {
        console.warn('[LMS] ⚠️ lectureData가 없습니다.');
        return;
    }

    // 🎯 강의 메타 정보 - 실제 데이터로 업데이트
    const ratingElement = document.getElementById('lectureRating');
    const studentElement = document.getElementById('studentCount');
    const instructorElement = document.getElementById('instructorName');

    if (ratingElement) {
        const rating = lectureData.avgReviewRate || 0.0;
        const reviewCount = lectureData.reviewNum || 0;
        ratingElement.textContent = `★${rating.toFixed(1)}(${reviewCount})`;
        console.log('[LMS] ⭐ 평점 업데이트:', `★${rating.toFixed(1)}(${reviewCount})`);
    }

    if (studentElement) {
        const students = lectureData.studentNum || 0;
        const displayStudents = students > 1000 ? `${Math.floor(students/1000)}k+` : `${students}+`;
        studentElement.textContent = `👤${displayStudents}`;
        console.log('[LMS] 👥 수강생 수 업데이트:', `👤${displayStudents}`);
    }

    if (instructorElement) {
        const instructor = lectureData.nickname || '지식제공자';
        instructorElement.textContent = `👤${instructor}`;
        console.log('[LMS] 👨‍🏫 강사명 업데이트:', `👤${instructor}`);
    }

    // 강의 제목과 설명도 업데이트
    const titleElement = document.getElementById('lectureTitle');
    if (titleElement) {
        const title = lectureData.title || lectureData.name || '강의 제목';
        titleElement.textContent = title;
        console.log('[LMS] 📖 제목 업데이트:', title);
    }

    const descElement = document.getElementById('lectureDescription');
    if (descElement) {
        const description = lectureData.introduction || lectureData.outline || '강의 소개';
        descElement.textContent = description;
        console.log('[LMS] 📝 설명 업데이트 완료');
    }

    // 카테고리 태그
    const categoryTags = document.getElementById('categoryTags');
    if (categoryTags) {
        const bigCategory = lectureData.big || '미정';
        const smallCategories = lectureData.small || '미정';
        categoryTags.innerHTML = `
            <span class="category-tag">${bigCategory}</span>
            <span class="category-tag">${smallCategories}</span>
        `;
        console.log('[LMS] 🏷️ 카테고리 업데이트:', bigCategory, smallCategories);
    }
}

/**
 * 진도율 정보 로드
 */
async function loadProgressInfo(lectureId) {
    try {
        const response = await fetch(`/api/lms/getProgress/${lectureId}`);
        if (!response.ok) {
            console.warn('[LMS] ⚠️ 진도율 정보 로드 실패:', response.status);
            return;
        }

        const progressData = await response.json();  // 지역변수로 선언해주는 게 좋음
        console.log('[LMS] 📊 진도율 정보:', progressData);

        // 진도율을 currentLectureData에 저장
        if (progressData && currentLectureData) {
            currentLectureData.progressRate = progressData.progressRate || 0;
            console.log('[LMS] 💾 진도율 저장:', currentLectureData.progressRate + '%');
        }

    } catch (error) {
        console.error('[LMS] ❌ 진도율 정보 로드 실패:', error);
    }
}

/**
 * 🎯 강의 목차 및 자료 로드 (핵심 수정)
 */
async function loadLectureChapters() {
    try {
        // 1. 목차 정보 로드
        console.log('[LMS] 📚 목차 정보 요청 중...');
        const chaptersResponse = await fetch('/api/lms/myRecentLecture/chapters');
        console.log(chaptersResponse);

        if (!chaptersResponse.ok) {
            throw new Error(`목차 로드 실패: ${chaptersResponse.status}`);
        }

        allChapters = await chaptersResponse.json();
        console.log(`[LMS] 📚 목차 로드 완료: 총 ${allChapters.length}개 목차`, allChapters);

        // 2. 자료 정보 로드
        console.log('[LMS] 📖 자료 정보 요청 중...');
        const materialsResponse = await fetch('/api/lms/myRecentLecture/chapters/materials');

        if (!materialsResponse.ok) {
            throw new Error(`자료 로드 실패: ${materialsResponse.status}`);
        }

        const materials = await materialsResponse.json();
        console.log('[LMS] 📖 자료 로드 완료:', materials);

        // 전역 변수에 저장
        window.chapterMaterials = materials;

        // 3. 목차 렌더링
        if (allChapters && Array.isArray(allChapters) && allChapters.length > 0) {
            console.log(`[LMS] 🎨 ${allChapters.length}개 목차 렌더링 시작`);
            renderChapters(allChapters);
        } else {
            console.log('[LMS] 📭 목차가 없어서 noChapters 표시');
            showNoChapters();
        }

    } catch (error) {
        console.error('[LMS] ❌ 강의 목차 로드 실패:', error);
        showNoChapters();
    }
}

/**
 * 🎯 강의 목차 렌더링 (수정됨)
 */
function renderChapters(chapters) {
    console.log(`[LMS] 🎨 목차 렌더링 시작 - 총 ${chapters.length}개 목차`);

    const chaptersList = document.getElementById('chaptersList');
    if (!chaptersList) {
        console.error('[LMS] ❌ chaptersList 요소를 찾을 수 없습니다.');
        return;
    }

    // 총 자료 개수 계산
    let totalMaterialCount = 0;
    if (window.chapterMaterials) {
        totalMaterialCount += (window.chapterMaterials.video || []).length;
        totalMaterialCount += (window.chapterMaterials.question || []).length;
        totalMaterialCount += (window.chapterMaterials.addi || []).length;
    }

    // 총 정보 업데이트
    const totalInfoElement = document.getElementById('totalInfo');
    if (totalInfoElement) {
        totalInfoElement.textContent = `전체 ${totalMaterialCount}개`;
    }

    // 🎯 모든 목차 HTML 생성
    const chaptersHTML = chapters.map((chapter, index) => {
        console.log(`[LMS] 🎨 목차 ${index + 1} 렌더링: ${chapter.name} (ID: ${chapter.chapterId})`);

        return `
            <div class="chapter-item" data-chapter-id="${chapter.chapterId}">
                <div class="chapter-header" onclick="toggleChapter('${chapter.chapterId}')">
                    <div class="chapter-title-section">
                        <h3 class="chapter-title">${index + 1}. ${chapter.name || '목차 제목'}</h3>
                        <p class="chapter-description">${chapter.objective || '목차 설명'}</p>
                    </div>
                    <button class="chapter-toggle" id="toggle-${chapter.chapterId}">▼</button>
                </div>
                <div class="materials-list" id="materials-${chapter.chapterId}">
                    ${renderChapterMaterials(chapter.chapterId, index)}
                </div>
            </div>
        `;
    }).join('');

    chaptersList.innerHTML = chaptersHTML;
    console.log(`[LMS] ✅ ${chapters.length}개 목차 렌더링 완료!`);
}

/**
 * 🎯 목차별 자료 렌더링 (완전 수정)
 */
function renderChapterMaterials(chapterId, chapterIndex) {
    console.log(`[LMS] 📖 목차 ${chapterIndex + 1} (${chapterId}) 자료 렌더링 시작`);

    if (!window.chapterMaterials) {
        console.warn('[LMS] ⚠️ chapterMaterials가 없습니다.');
        return '<p style="padding: 20px; text-align: center; color: #6c757d;">자료를 불러오는 중...</p>';
    }

    const materials = [];
    const totalChapters = allChapters ? allChapters.length : 1;

    // 🎯 각 자료 타입별로 목차에 균등 분배
    ['video', 'question', 'addi'].forEach(materialType => {
        const materialsOfType = window.chapterMaterials[materialType] || [];

        if (materialsOfType.length > 0) {
            // 자료를 목차별로 균등 분배
            const materialsPerChapter = Math.ceil(materialsOfType.length / totalChapters);
            const startIndex = chapterIndex * materialsPerChapter;
            const endIndex = Math.min(startIndex + materialsPerChapter, materialsOfType.length);

            const chapterMaterials = materialsOfType.slice(startIndex, endIndex);

            console.log(`[LMS] 📖 ${materialType} 자료 분배: ${startIndex}-${endIndex-1} (총 ${chapterMaterials.length}개)`);

            // 자료 정보 구성
            chapterMaterials.forEach(material => {
                let materialInfo = {
                    id: material.matId,
                    type: materialType,
                    title: material.name || material.contentName || material.question || `${materialType} 자료`,
                    status: 'incomplete'
                };

                // 타입별 아이콘 설정
                switch (materialType) {
                    case 'video':
                        materialInfo.icon = '🎥';
                        materialInfo.statusText = '미시청';
                        break;
                    case 'question':
                        materialInfo.icon = '❓';
                        materialInfo.statusText = '미제출';
                        break;
                    case 'addi':
                        materialInfo.icon = '📎';
                        materialInfo.statusText = '다운로드';
                        break;
                }

                materials.push(materialInfo);
            });
        }
    });

    console.log(`[LMS] 📖 목차 ${chapterIndex + 1}에 할당된 자료: ${materials.length}개`, materials);

    if (materials.length === 0) {
        return '<p style="padding: 20px; text-align: center; color: #6c757d;">등록된 자료가 없습니다.</p>';
    }

    // 🎯 자료 HTML 생성
    return materials.map(material => `
        <div class="material-item" onclick="openMaterial('${material.type}', '${material.id}')">
            <div class="material-info">
                <span class="material-icon">${material.icon}</span>
                <span class="material-title">${material.title}</span>
            </div>
            <div class="material-status">
                <span class="status-badge">${material.statusText}</span>
            </div>
        </div>
    `).join('');
}

/**
 * 사용자 인터페이스 업데이트
 */
function updateUserInterface() {
    console.log('[LMS] 🎨 UI 업데이트:', {
        userType: currentUserType,
        progressRate: currentLectureData?.progressRate
    });

    // 모든 액션 그룹 숨기기
    hideAllActionGroups();

    if (currentUserType === 0) {
        // 일반 사용자
        updateStudentInterface();
    } else if (currentUserType === 1) {
        // 지식 제공자
        updateProviderInterface();
    }
}

/**
 * 일반 사용자 인터페이스 업데이트
 */
function updateStudentInterface() {
    const progressPercentage = currentLectureData?.progressRate || 0;
    console.log(`[LMS] 👨‍🎓 일반 사용자 UI - 진도율: ${progressPercentage}%`);

    if (progressPercentage === 0) {
        // 취소 버튼 표시
        const cancelGroup = document.getElementById('cancelGroup');
        if (cancelGroup) {
            cancelGroup.style.display = 'block';
            console.log('[LMS] 🔴 취소 버튼 표시');
        }
    } else {
        // 진도율 바 표시
        const progressGroup = document.getElementById('progressGroup');
        if (progressGroup) {
            progressGroup.style.display = 'block';

            const progressFill = document.getElementById('progressFill');
            const progressText = document.getElementById('progressText');

            if (progressFill) {
                progressFill.style.width = `${progressPercentage}%`;
            }
            if (progressText) {
                progressText.textContent = `진도율: ${progressPercentage}%`;
            }
            console.log(`[LMS] 📊 진도율 바 표시: ${progressPercentage}%`);
        }
    }
}

/**
 * 지식 제공자 인터페이스 업데이트
 */
function updateProviderInterface() {
    console.log('[LMS] 👨‍🏫 지식 제공자 UI 업데이트');

    const editGroup = document.getElementById('providerEditGroup');
    const registerGroup = document.getElementById('providerRegisterGroup');

    if (currentLectureData && currentLectureData.lectureStatus === 1) {
        // 승인된 강의 - 수정 가능
        if (editGroup) editGroup.style.display = 'block';
    } else {
        // 새 강의 등록 또는 심사 중
        if (registerGroup) registerGroup.style.display = 'block';
    }
}

/**
 * 유틸리티 함수들
 */
function hideAllActionGroups() {
    const groups = ['cancelGroup', 'progressGroup', 'providerRegisterGroup', 'providerEditGroup'];
    groups.forEach(groupId => {
        const element = document.getElementById(groupId);
        if (element) {
            element.style.display = 'none';
        }
    });
}

function showLoading() {
    const spinner = document.getElementById('loadingSpinner');
    const container = document.getElementById('mainContainer');
    if (spinner) spinner.style.display = 'block';
    if (container) container.style.display = 'none';
}

function hideLoading() {
    const spinner = document.getElementById('loadingSpinner');
    const container = document.getElementById('mainContainer');
    if (spinner) spinner.style.display = 'none';
    if (container) container.style.display = 'block';
}

function showToast(message, type = 'info') {
    console.log(`[Toast] ${type}: ${message}`);
    // 실제 토스트 구현 필요시 여기에 추가
}

function showNoChapters() {
    const chaptersList = document.getElementById('chaptersList');
    const noChapters = document.getElementById('noChapters');
    if (chaptersList) chaptersList.style.display = 'none';
    if (noChapters) noChapters.style.display = 'block';
}

function toggleChapter(chapterId) {
    const materialsList = document.getElementById(`materials-${chapterId}`);
    const toggleBtn = document.getElementById(`toggle-${chapterId}`);

    if (materialsList && toggleBtn) {
        if (materialsList.classList.contains('show')) {
            materialsList.classList.remove('show');
            toggleBtn.textContent = '▼';
        } else {
            materialsList.classList.add('show');
            toggleBtn.textContent = '▲';
        }
    }
}

function openMaterial(type, materialId) {
    console.log(`[LMS] 🎯 자료 열기: ${type}, ${materialId}`);
    // 자료 열기 로직 구현 필요
}

// 기타 필요한 함수들
function toggleAllChapters() {
    console.log('[LMS] 🎯 모든 목차 토글');
    // 구현 필요
}

function cancelLecture() {
    console.log('[LMS] 🔴 수강 취소');
    // 구현 필요
}

function registerLecture() {
    console.log('[LMS] ➕ 강의 등록');
    // 구현 필요
}

function editLecture() {
    console.log('[LMS] ✏️ 강의 수정');
    // 구현 필요
}

function addChapter() {
    console.log('[LMS] ➕ 목차 추가');
    // 구현 필요
}