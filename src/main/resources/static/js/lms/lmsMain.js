/**
 * LMS 메인 페이지 JavaScript
 * 핵심 기능:
 * 1. 최근 강의 정보 표시 (last_update_date 기준)
 * 2. 수강률 0%일 때 취소 버튼, 1% 이상일 때 진도율 바
 * 3. 목차별 자료 클릭 기능 (수정됨 - 목차별로 실제 자료 조회)
 * 4. 진도율 정확성 개선 및 썸네일 이미지 처리 개선
 */

// 전역 변수
let currentLectureData = null;
let currentUserType = null;
let progressData = null;
let isAllExpanded = false;
let allChapters = null;
let totalMaterialsCount = 0; // 🎯 전체 자료 개수 추가

// 🔐 관리자용 전역 변수
let pendingLectureId = null; // 현재 처리 중인 강의 ID

/**
 * 🎯 URL에서 lectureId 추출 함수 (새로 추가)
 * 전역 함수로 다른 파일에서도 사용 가능
 */
window.extractLectureIdFromUrl = function() {
    const path = window.location.pathname;
    console.log('[LMS] 🔍 현재 URL 경로:', path);
    
    // /lms/main/123 형태에서 123 추출
    const match = path.match(/^\/lms\/main\/(\d+)$/);
    
    if (match && match[1]) {
        const lectureId = match[1]; // 문자열로 유지 (데이터베이스 ID 형식에 맞춤)
        console.log('[LMS] ✅ URL에서 lectureId 추출:', lectureId);
        return lectureId;
    }
    
    // /lms/main 경로인 경우 null 반환 (최근 강의 모드)
    console.log('[LMS] ℹ️ 최근 강의 모드 (lectureId 없음)');
    return null;
};

// 호환성을 위해 지역 함수로도 사용 가능
function extractLectureIdFromUrl() {
    return window.extractLectureIdFromUrl();
}

/**
 * 🎯 lectureId 초기화 함수 (새로 추가)
 */
function initializeLectureId() {
    if (!window.isAdmin) {
        // 🎯 일반 사용자의 경우 URL과 서버 값 우선순위 결정
        const urlLectureId = extractLectureIdFromUrl();
        window.currentLectureId = urlLectureId || window.serverLectureId;
        
        console.log('[LMS] 🎯 최종 lectureId 결정:', {
            urlLectureId: urlLectureId,
            serverLectureId: window.serverLectureId,
            finalLectureId: window.currentLectureId
        });
    }
}

/**
 * 페이지 로드 시 초기화 (수정됨)
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('[LMS] 🚀 페이지 초기화 시작');
    
    // 🎯 1단계: lectureId 초기화
    initializeLectureId();
    
    // 🎯 2단계: URL에서 lectureId 추출 (일반 사용자를 위해 유지)
    const currentLectureId = extractLectureIdFromUrl();
    console.log('[LMS] 📍 현재 강의 ID:', currentLectureId);
    
    // 전역 변수에 저장 (호환성을 위해 유지)
    window.currentLectureId = window.currentLectureId || currentLectureId;
    
    // 관리자/일반 사용자 구분하여 초기화
    if (window.isAdmin) {
        console.log('[LMS] 🔐 관리자 모드로 초기화');
        initializeAdminPage();
    } else {
        console.log('[LMS] 👥 일반 사용자 모드로 초기화');
        initializeUserPage();
    }
});

/**
 * 🔐 관리자 페이지 초기화
 */
async function initializeAdminPage() {
    try {
        showLoading();
        
        console.log('[LMS] 🔐 관리자 UI 설정 중...');
        
        // 관리자 컨테이너 표시
        const adminContainer = document.getElementById('adminContainer');
        if (adminContainer) {
            adminContainer.style.display = 'block';
        }
        
        // 승인 대기 강의가 있는 경우 현재 강의 ID 설정
        if (window.showApprovalButtons && window.adminLecture) {
            pendingLectureId = window.adminLecture.lectureId;
            console.log('[LMS] 🔐 현재 처리 대상 강의:', pendingLectureId);
        }
        
        hideLoading();
        console.log('[LMS] ✅ 관리자 페이지 초기화 완료!');
        
    } catch (error) {
        console.error('[LMS] ❌ 관리자 페이지 초기화 실패:', error);
        hideLoading();
        showToast('페이지 로드에 실패했습니다. 새로고침해주세요.', 'error');
    }
}

/**
 * 👥 일반 사용자 페이지 초기화 (기존 로직)
 */
async function initializeUserPage() {
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

        // 3. 강의 목차 로드 (자료는 목차 클릭 시 개별 로드)
        console.log('[LMS] 📚 강의 목차 로드 중...');
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
        let apiUrl;
        
        // 🎯 URL에 lectureId가 있으면 특정 강의 조회, 없으면 최근 강의 조회
        if (window.currentLectureId) {
            apiUrl = `/api/lms/lecture/${window.currentLectureId}`;
            console.log('[LMS] 📍 특정 강의 정보 로드:', window.currentLectureId);
        } else {
            apiUrl = '/api/lms/myRecentLecture';
            console.log('[LMS] 📍 최근 강의 정보 로드');
        }
        
        const response = await fetch(apiUrl);

        if (!response.ok) {
            if (response.status === 403) {
                throw new Error('해당 강의에 접근할 권한이 없습니다.');
            } else if (response.status === 404) {
                throw new Error('강의를 찾을 수 없습니다.');
            } else {
                throw new Error(`서버 오류: ${response.status}`);
            }
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
        
        // 권한 오류인 경우 사용자에게 알림 후 리다이렉트
        if (error.message.includes('권한') || error.message.includes('찾을 수 없습니다')) {
            alert(error.message);
            window.location.href = '/courses'; // 강의 목록 페이지로 리다이렉트
            return;
        }
        
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

    // 🎯 썸네일 업데이트 추가
    const thumbnailElement = document.getElementById('lectureThumbnail');
    if (thumbnailElement) {
        // 서버에서 받은 썸네일 경로 사용, 없으면 기본 이미지
        const thumbnailPath = lectureData.thumbnail || '/img/thumbnail.png';
        
        thumbnailElement.src = thumbnailPath;
        
        // 이미지 로드 실패 시 기본 이미지로 대체하는 이벤트 리스너
        thumbnailElement.onerror = function() {
            console.warn('[LMS] ⚠️ 썸네일 로드 실패, 기본 이미지로 대체:', thumbnailPath);
            this.src = '/img/thumbnail.png';
            this.onerror = null; // 무한 루프 방지
        };
        
        console.log('[LMS] 🖼️ 썸네일 업데이트:', thumbnailPath);
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
        studentElement.textContent = `🙍‍♂️${displayStudents}`;
        console.log('[LMS] 👥 수강생 수 업데이트:', `🙍‍♂${displayStudents}`);
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
    descElement.innerHTML = description;  // HTML 태그 포함한 설명이 올 경우
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
 * 🎯 강의 목차 로드 (수정됨 - 자료는 별도 로드)
 */
async function loadLectureChapters() {
    try {
        // 목차 정보만 로드 (자료는 클릭 시 개별 로드)
        const targetLectureId = window.currentLectureId;
        let apiUrl;
        
        // 🎯 URL에 lectureId가 있으면 해당 강의의 목차, 없으면 최근 강의 목차
        if (targetLectureId) {
            apiUrl = `/api/lms/lecture/${targetLectureId}/chapters`;
            console.log('[LMS] 📚 특정 강의 목차 요청:', targetLectureId);
        } else {
            apiUrl = '/api/lms/myRecentLecture/chapters';
            console.log('[LMS] 📚 최근 강의 목차 요청');
        }
        
        const chaptersResponse = await fetch(apiUrl);

        if (!chaptersResponse.ok) {
            throw new Error(`목차 로드 실패: ${chaptersResponse.status}`);
        }

        allChapters = await chaptersResponse.json();
        console.log(`[LMS] 📚 목차 로드 완료: 총 ${allChapters.length}개 목차`, allChapters);

        // 목차 렌더링 (자료는 빈 상태로 시작)
        if (allChapters && Array.isArray(allChapters) && allChapters.length > 0) {
            console.log(`[LMS] 🎨 ${allChapters.length}개 목차 렌더링 시작`);
            renderChapters(allChapters);
            // 🎯 초기 목차 개수 표시
            updateTotalMaterialsCount();
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

    // 🎯 모든 목차 HTML 생성 (자료는 빈 상태로 시작)
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
                    <p style="padding: 20px; text-align: center; color: #6c757d;">목차를 클릭하면 자료가 로드됩니다</p>
                </div>
            </div>
        `;
    }).join('');

    chaptersList.innerHTML = chaptersHTML;
    console.log(`[LMS] ✅ ${chapters.length}개 목차 렌더링 완료!`);
}

/**
 * 🎯 목차별 자료 렌더링
 */
async function loadAndRenderChapterMaterials(chapterId) {
    console.log(`[LMS] 📖 목차 ${chapterId} 자료 로드 및 렌더링 시작`);

    const materialsContainer = document.getElementById(`materials-${chapterId}`);
    if (!materialsContainer) {
        console.error(`[LMS] ❌ materials-${chapterId} 요소를 찾을 수 없습니다.`);
        return;
    }

    try {
        // 로딩 표시
        materialsContainer.innerHTML = '<p style="padding: 20px; text-align: center; color: #6c757d;">📋 자료를 불러오는 중...</p>';

        // 🎯 API URL 결정
        const targetLectureId = window.currentLectureId;
        let apiUrl;
        
        if (targetLectureId) {
            apiUrl = `/api/lms/lecture/${targetLectureId}/chapters/${chapterId}/materials`;
        } else {
            apiUrl = `/api/lms/myRecentLecture/chapters/${chapterId}/materials`;
        }
        
        const response = await fetch(apiUrl);
        
        if (!response.ok) {
            throw new Error(`자료 로드 실패: ${response.status}`);
        }

        const materialsData = await response.json();
        console.log(`[LMS] 📖 목차 ${chapterId} 자료 로드 완료:`, materialsData);

        // 자료 통합 리스트 생성
        const allMaterials = [];

        // 영상 자료 추가
        if (materialsData.video && materialsData.video.length > 0) {
            materialsData.video.forEach(video => {
                allMaterials.push({
                    id: video.matId,
                    type: 'video',
                    title: video.name || video.contentName || '영상 자료',
                    icon: '🎥',
                    statusText: '미시청'
                });
            });
        }

        // 문제 자료 추가
        if (materialsData.question && materialsData.question.length > 0) {
            materialsData.question.forEach(question => {
                allMaterials.push({
                    id: question.matId,
                    type: 'question', 
                    title: question.question || question.contentName || '문제',
                    icon: '❓',
                    statusText: '미제출'
                });
            });
        }

        // 추가 자료 추가
        if (materialsData.addi && materialsData.addi.length > 0) {
            materialsData.addi.forEach(addi => {
                allMaterials.push({
                    id: addi.matId,
                    type: 'addi',
                    title: addi.name || addi.contentName || '추가 자료',
                    icon: '📎',
                    statusText: '다운로드'
                });
            });
        }

        console.log(`[LMS] 📖 목차 ${chapterId}에 실제 자료: ${allMaterials.length}개`, allMaterials);
        
        // 🎯 전체 자료 개수 업데이트
        updateTotalMaterialsCount();


        // 자료가 없는 경우
        if (allMaterials.length === 0) {
            materialsContainer.innerHTML = '<p style="padding: 20px; text-align: center; color: #6c757d;">📭 등록된 자료가 없습니다.</p>';
            return;
        }

        // 🎯 자료 HTML 생성
        const materialsHTML = allMaterials.map(material => `
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

        materialsContainer.innerHTML = materialsHTML;
        console.log(`[LMS] ✅ 목차 ${chapterId} 자료 렌더링 완료`);
        
        // 🎯 전체 자료 개수 업데이트
        updateTotalMaterialsCount();

    } catch (error) {
        console.error(`[LMS] ❌ 목차 ${chapterId} 자료 로드 실패:`, error);
        materialsContainer.innerHTML = '<p style="padding: 20px; text-align: center; color: #e74c3c;">⚠️ 자료 로드에 실패했습니다.</p>';
    }
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
    const adminContainer = document.getElementById('adminContainer');
    
    if (spinner) spinner.style.display = 'none';
    
    // 🎯 관리자/일반 사용자에 따라 올바른 컨테이너 표시
    if (window.isAdmin && adminContainer) {
        adminContainer.style.display = 'block';
    } else if (container) {
        container.style.display = 'block';
    }
}

function showToast(message, type = 'info') {
    console.log(`[Toast] ${type}: ${message}`);
    
    // 토스트 요소가 있다면 실제로 표시
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    if (toast && toastMessage) {
        // 메시지 설정
        toastMessage.textContent = message;
        
        // 타입에 따른 스타일 적용
        toast.className = `toast toast-${type}`;
        
        // 토스트 표시
        toast.style.display = 'block';
        
        // 3초 후 자동으로 숨김
        setTimeout(() => {
            toast.style.display = 'none';
        }, 3000);
    }
}

function showNoChapters() {
    const chaptersList = document.getElementById('chaptersList');
    const noChapters = document.getElementById('noChapters');
    if (chaptersList) chaptersList.style.display = 'none';
    if (noChapters) noChapters.style.display = 'block';
}

/**
 * 🎯 목차 토글 함수
 */
async function toggleChapter(chapterId) {
    console.log(`[LMS] 🎯 목차 ${chapterId} 토글 시작`);
    
    const materialsList = document.getElementById(`materials-${chapterId}`);
    const toggleBtn = document.getElementById(`toggle-${chapterId}`);

    if (!materialsList || !toggleBtn) {
        console.error(`[LMS] ❌ 목차 ${chapterId} 요소를 찾을 수 없습니다.`);
        return;
    }

    if (materialsList.classList.contains('show')) {
        // 닫기
        materialsList.classList.remove('show');
        toggleBtn.textContent = '▼';
        console.log(`[LMS] 📤 목차 ${chapterId} 닫음`);
    } else {
        // 열기 - 자료가 아직 로드되지 않았다면 로드
        const currentContent = materialsList.innerHTML.trim();
        const isNotLoaded = currentContent.includes('목차를 클릭하면 자료가 로드됩니다') || 
                           currentContent.includes('자료를 불러오는 중');

        if (isNotLoaded) {
            console.log(`[LMS] 📥 목차 ${chapterId} 자료 로드 시작`);
            await loadAndRenderChapterMaterials(chapterId);
        }

        materialsList.classList.add('show');
        toggleBtn.textContent = '▲';
        console.log(`[LMS] 📤 목차 ${chapterId} 열림`);
    }
}

/**
 * 🎯 전체 자료 개수 업데이트 함수
 * 
 * 로직 설명:
 * 1. 현재 로드된 모든 목차의 자료 개수를 세기
 * 2. totalInfo 요소에 총 개수 표시
 */
function updateTotalMaterialsCount() {
    const totalInfoElement = document.getElementById('totalInfo');
    if (!totalInfoElement) {
        console.warn('[LMS] ⚠️ totalInfo 요소를 찾을 수 없습니다.');
        return;
    }

    // 📊 총 목차 개수 표시
    const totalChapters = allChapters ? allChapters.length : 0;
    
    // 🎯 텍스트 업데이트
    totalInfoElement.textContent = `전체 ${totalChapters}개 목차`;
    
    console.log(`[LMS] 📊 전체 자료 개수 업데이트: 목차 ${totalChapters}개`);
}

function openMaterial(type, materialId) {
    console.log(`[LMS] 🎯 자료 열기: ${type}, ${materialId}`);

    let url = '';
    switch(type) {
        case 'video':
            url = `/lms/material/video/${materialId}`;
            break;
        case 'question':
            url = `/lms/material/question/${materialId}`;
            break;
        case 'addi': // 추가 자료 (파일)
        case 'file': // 혹시 다른 이름일 경우
            url = `/lms/material/file/${materialId}`;
            break;
        default:
            console.warn('[LMS] ⚠️ 알 수 없는 자료 타입:', type);
            showToast('지원하지 않는 자료 형식입니다.', 'error');
            return;
    }

    // 해당 URL로 이동
    window.location.href = url;
}


/**
 * 🎯 모든 목차 토글 함수
 * 
 * 로직 설명:
 * 1. 현재 상태 확인 - isAllExpanded 변수로 모든 목차가 펼쳐져 있는지 체크
 * 2. 모든 목차 찾기 - 페이지에 있는 모든 목차들을 찾음
 * 3. 상태에 따라 처리:
 *    - 모두 펼치기: 각 목차를 열고 자료도 로드
 *    - 모두 접기: 각 목차를 닫음
 * 4. 버튼 텍스트 업데이트
 */
async function toggleAllChapters() {
    console.log('[LMS] 🎯 모든 목차 토글 시작 - 현재 상태:', isAllExpanded);
    
    // 🔍 1단계: 모든 목차 요소들 찾기
    const chapterItems = document.querySelectorAll('.chapter-item');
    const toggleAllBtn = document.getElementById('toggleAllBtn');
    
    // 목차가 없으면 처리하지 않음
    if (!chapterItems || chapterItems.length === 0) {
        console.warn('[LMS] ⚠️ 목차가 없어서 토글할 수 없습니다.');
        return;
    }
    
    console.log(`[LMS] 📚 총 ${chapterItems.length}개 목차 발견`);
    
    try {
        // 🎯 2단계: 상태에 따라 모두 펼치기 OR 모두 접기
        if (isAllExpanded) {
            // 🔺 모든 목차 접기
            console.log('[LMS] 📤 모든 목차 접기 시작');
            
            chapterItems.forEach((chapterItem, index) => {
                const chapterId = chapterItem.getAttribute('data-chapter-id');
                const materialsList = document.getElementById(`materials-${chapterId}`);
                const toggleBtn = document.getElementById(`toggle-${chapterId}`);
                
                if (materialsList && toggleBtn) {
                    // 자료 영역 숨기기
                    materialsList.classList.remove('show');
                    // 토글 버튼을 아래쪽 화살표로 변경
                    toggleBtn.textContent = '▼';
                    
                    console.log(`[LMS] 📤 목차 ${index + 1} (ID: ${chapterId}) 접음`);
                }
            });
            
            // 상태 변경
            isAllExpanded = false;
            
            // 버튼 텍스트 변경
            if (toggleAllBtn) {
                toggleAllBtn.textContent = '▼ 모두 펼치기';
            }
            
            console.log('[LMS] ✅ 모든 목차 접기 완료');
            
        } else {
            // 🔻 모든 목차 펼치기
            console.log('[LMS] 📥 모든 목차 펼치기 시작');
            
            // 버튼 상태를 로딩으로 변경
            if (toggleAllBtn) {
                toggleAllBtn.textContent = '⏳ 로딩 중...';
                toggleAllBtn.disabled = true;
            }
            
            // 🚀 각 목차를 순차적으로 처리 (병렬 처리로 성능 개선)
            const expandPromises = [];
            
            chapterItems.forEach((chapterItem, index) => {
                const chapterId = chapterItem.getAttribute('data-chapter-id');
                
                console.log(`[LMS] 📥 목차 ${index + 1} (ID: ${chapterId}) 펼치기 시작`);
                
                // 각 목차를 비동기로 처리
                const expandPromise = expandSingleChapter(chapterId, index + 1);
                expandPromises.push(expandPromise);
            });
            
            // 모든 목차가 완료될 때까지 기다림
            await Promise.all(expandPromises);
            
            // 상태 변경
            isAllExpanded = true;
            
            // 버튼 상태 복원 및 텍스트 변경
            if (toggleAllBtn) {
                toggleAllBtn.textContent = '▲ 모두 접기';
                toggleAllBtn.disabled = false;
            }
            
            console.log('[LMS] ✅ 모든 목차 펼치기 완료');
        }
        
    } catch (error) {
        console.error('[LMS] ❌ 모든 목차 토글 중 오류 발생:', error);
        
        // 오류 발생 시 버튼 상태 복원
        if (toggleAllBtn) {
            toggleAllBtn.textContent = isAllExpanded ? '▲ 모두 접기' : '▼ 모두 펼치기';
            toggleAllBtn.disabled = false;
        }
        
        // 사용자에게 오류 알림
        showToast('목차 로드 중 오류가 발생했습니다.', 'error');
    }
}

/**
 * 🎯 개별 목차 펼치기 함수 (내부 유틸리티 함수)
 * 
 * 로직 설명:
 * 1. 목차의 자료 영역과 토글 버튼 찾기
 * 2. 자료가 로드되지 않았다면 API 호출해서 로드
 * 3. 자료 영역 표시하고 버튼 상태 변경
 */
async function expandSingleChapter(chapterId, chapterNumber) {
    const materialsList = document.getElementById(`materials-${chapterId}`);
    const toggleBtn = document.getElementById(`toggle-${chapterId}`);
    
    if (!materialsList || !toggleBtn) {
        console.error(`[LMS] ❌ 목차 ${chapterNumber} (ID: ${chapterId}) 요소를 찾을 수 없습니다.`);
        return;
    }
    
    try {
        // 🔍 자료가 이미 로드되었는지 확인
        const currentContent = materialsList.innerHTML.trim();
        const isNotLoaded = currentContent.includes('목차를 클릭하면 자료가 로드됩니다') || 
                           currentContent.includes('자료를 불러오는 중');
        
        if (isNotLoaded) {
            console.log(`[LMS] 📖 목차 ${chapterNumber} 자료 로드 시작`);
            
            // 자료 로드 (기존 함수 재사용)
            await loadAndRenderChapterMaterials(chapterId);
        }
        
        // 📤 자료 영역 표시
        materialsList.classList.add('show');
        
        // 🔄 토글 버튼을 위쪽 화살표로 변경
        toggleBtn.textContent = '▲';
        
        console.log(`[LMS] ✅ 목차 ${chapterNumber} (ID: ${chapterId}) 펼치기 완료`);
        
    } catch (error) {
        console.error(`[LMS] ❌ 목차 ${chapterNumber} (ID: ${chapterId}) 펼치기 실패:`, error);
        
        // 오류 발생 시에도 UI는 열어서 일관성 유지
        materialsList.classList.add('show');
        toggleBtn.textContent = '▲';
        
        // 오류 메시지 표시
        materialsList.innerHTML = '<p style="padding: 20px; text-align: center; color: #e74c3c;">⚠️ 자료 로드에 실패했습니다.</p>';
    }
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

// 🔐 =========================== 관리자 전용 함수들 ===========================

/**
 * 🔐 강의 승인 함수
 */
async function approveLecture(lectureId) {
    if (!confirm('이 강의를 승인하시겠습니까?')) {
        return;
    }
    
    try {
        console.log('[LMS] 🔐 강의 승인 요청:', lectureId);
        
        // 버튼 비활성화
        const approveBtn = document.querySelector('.btn-approve');
        const rejectBtn = document.querySelector('.btn-reject');
        if (approveBtn) {
            approveBtn.disabled = true;
            approveBtn.textContent = '⏳ 처리 중...';
        }
        if (rejectBtn) {
            rejectBtn.disabled = true;
        }
        
        const response = await fetch(`/api/lms/admin/approve/${lectureId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        
        const result = await response.json();
        
        if (result.success) {
            console.log('[LMS] ✅ 강의 승인 성공:', result);
            showToast('강의가 승인되었습니다! 페이지를 새로고침합니다.', 'success');
            
            // 3초 후 페이지 새로고침으로 다음 대기 강의 표시
            setTimeout(() => {
                location.reload();
            }, 2000);
            
        } else {
            console.error('[LMS] ❌ 강의 승인 실패:', result.message);
            showToast('승인 실패: ' + result.message, 'error');
            
            // 버튼 상태 복원
            if (approveBtn) {
                approveBtn.disabled = false;
                approveBtn.textContent = '✅ 승인';
            }
            if (rejectBtn) {
                rejectBtn.disabled = false;
            }
        }
        
    } catch (error) {
        console.error('[LMS] ❌ 강의 승인 오류:', error);
        showToast('오류가 발생했습니다: ' + error.message, 'error');
        
        // 버튼 상태 복원
        const approveBtn = document.querySelector('.btn-approve');
        const rejectBtn = document.querySelector('.btn-reject');
        if (approveBtn) {
            approveBtn.disabled = false;
            approveBtn.textContent = '✅ 승인';
        }
        if (rejectBtn) {
            rejectBtn.disabled = false;
        }
    }
}

/**
 * 🔐 강의 거부 함수 (거부 사유 입력 모달 표시)
 */
function rejectLecture(lectureId) {
    console.log('[LMS] 🔐 강의 거부 모달 표시:', lectureId);
    
    // 현재 처리 중인 강의 ID 저장
    pendingLectureId = lectureId;
    
    // 거부 사유 입력 모달 표시
    const rejectModal = document.getElementById('rejectModal');
    const rejectReasonTextarea = document.getElementById('rejectReason');
    
    if (rejectModal && rejectReasonTextarea) {
        rejectReasonTextarea.value = ''; // 기존 내용 초기화
        rejectModal.style.display = 'flex';
        rejectReasonTextarea.focus(); // 텍스트 영역에 포커스
    } else {
        console.error('[LMS] ❌ 거부 모달 요소를 찾을 수 없습니다.');
    }
}

/**
 * 🔐 강의 거부 확인 (실제 API 호출)
 */
async function confirmReject() {
    const rejectReasonTextarea = document.getElementById('rejectReason');
    const failReason = rejectReasonTextarea ? rejectReasonTextarea.value.trim() : '';
    
    // 거부 사유 입력 검증
    if (!failReason) {
        alert('거부 사유를 입력해주세요.');
        if (rejectReasonTextarea) {
            rejectReasonTextarea.focus();
        }
        return;
    }
    
    if (failReason.length < 10) {
        alert('거부 사유를 10자 이상 상세히 입력해주세요.');
        if (rejectReasonTextarea) {
            rejectReasonTextarea.focus();
        }
        return;
    }
    
    if (!confirm(`이 강의를 거부하시겠습니까?\n\n거부 사유: ${failReason}`)) {
        return;
    }
    
    try {
        console.log('[LMS] 🔐 강의 거부 요청:', pendingLectureId, '사유:', failReason);
        
        // 모달 내 버튼 비활성화
        const confirmBtn = document.querySelector('.btn-confirm-reject');
        if (confirmBtn) {
            confirmBtn.disabled = true;
            confirmBtn.textContent = '⏳ 처리 중...';
        }
        
        const response = await fetch(`/api/lms/admin/reject/${pendingLectureId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ failReason: failReason })
        });
        
        const result = await response.json();
        
        if (result.success) {
            console.log('[LMS] ✅ 강의 거부 성공:', result);
            showToast('강의가 거부되었습니다. 페이지를 새로고침합니다.', 'success');
            
            // 모달 닫기
            closeRejectModal();
            
            // 3초 후 페이지 새로고침으로 다음 대기 강의 표시
            setTimeout(() => {
                location.reload();
            }, 2000);
            
        } else {
            console.error('[LMS] ❌ 강의 거부 실패:', result.message);
            showToast('거부 실패: ' + result.message, 'error');
            
            // 버튼 상태 복원
            if (confirmBtn) {
                confirmBtn.disabled = false;
                confirmBtn.textContent = '거부하기';
            }
        }
        
    } catch (error) {
        console.error('[LMS] ❌ 강의 거부 오류:', error);
        showToast('오류가 발생했습니다: ' + error.message, 'error');
        
        // 버튼 상태 복원
        const confirmBtn = document.querySelector('.btn-confirm-reject');
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.textContent = '거부하기';
        }
    }
}

/**
 * 🔐 거부 모달 닫기
 */
function closeRejectModal() {
    const rejectModal = document.getElementById('rejectModal');
    if (rejectModal) {
        rejectModal.style.display = 'none';
    }
    
    // 거부 사유 초기화
    const rejectReasonTextarea = document.getElementById('rejectReason');
    if (rejectReasonTextarea) {
        rejectReasonTextarea.value = '';
    }
    
    console.log('[LMS] 🔐 거부 모달 닫힘');
}