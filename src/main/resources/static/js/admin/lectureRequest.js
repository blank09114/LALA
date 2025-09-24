// 지식제공자 요청 처리 페이지 JavaScript

// URL에서 userId 추출
const userId = location.pathname.split('/').pop();

// 현재 사용자 정보와 요청 정보
let currentUser = null;
let currentProviderRequest = null;

/**
 * 페이지 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('페이지 로드 완료 - userId:', userId);

    if (!userId) {
        showError('사용자 ID를 찾을 수 없습니다.');
        return;
    }

    // 초기 데이터 로드
    loadAllData();

    // 버튼 이벤트 리스너 등록
    setupEventListeners();
});

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    const approveBtn = document.getElementById('approveBtn');
    const rejectBtn = document.getElementById('rejectBtn');
    const backBtn = document.getElementById('backBtn');

    if (approveBtn) {
        approveBtn.addEventListener('click', handleApproveRequest);
    }

    if (rejectBtn) {
        rejectBtn.addEventListener('click', handleRejectRequest);
    }

    if (backBtn) {
        backBtn.addEventListener('click', goBackToUserList);
    }
}

/**
 * 모든 데이터 로드 (사용자 정보 + 지식제공자 요청 정보)
 */
async function loadAllData() {
    try {
        showLoading();

        console.log('데이터 로딩 시작 - userId:', userId);

        // 사용자 정보와 지식제공자 요청 정보를 병렬로 가져오기
        const [userResponse, providerResponse] = await Promise.all([
            fetch(`/api/admin/user/${userId}`),
            fetch(`/api/admin/user/${userId}/provider-request`)
        ]);

        console.log('API 응답 상태 - User:', userResponse.status, 'Provider:', providerResponse.status);

        // 사용자 정보 처리
        if (!userResponse.ok) {
            throw new Error(`사용자 정보 조회 실패: ${userResponse.status} ${userResponse.statusText}`);
        }

        const userData = await userResponse.json();
        currentUser = userData.user;
        console.log('사용자 정보 로드 완료:', currentUser);

        // 지식제공자 요청 정보 처리
        let providerData = null;
        if (providerResponse.ok) {
            const providerResponseData = await providerResponse.json();
            console.log('지식제공자 요청 응답:', providerResponseData);

            if (providerResponseData.hasRequest) {
                providerData = providerResponseData.providerRequest;
                currentProviderRequest = providerData;
                console.log('지식제공자 요청 정보 로드 완료:', providerData);
            } else {
                console.log('지식제공자 요청 정보 없음');
            }
        } else {
            console.warn('지식제공자 요청 정보 조회 실패:', providerResponse.status);
        }

        // UI 업데이트
        displayUserInfo(currentUser);
        displayProviderRequestInfo(providerData);
        setupActionButtons(currentUser, providerData);

        hideLoading();

    } catch (error) {
        console.error('데이터 로딩 실패:', error);
        showError(`데이터를 불러오는데 실패했습니다: ${error.message}`);
        hideLoading();
    }
}

/**
 * 사용자 기본 정보 표시
 */
function displayUserInfo(user) {
    if (!user) {
        console.error('사용자 정보가 없습니다');
        return;
    }

    console.log('사용자 정보 표시:', user);

    // 기본 정보 업데이트
    updateElementText('userEmail', user.email);
    updateElementText('userNickname', user.userNickname);
    updateElementText('userRegDate', formatDate(user.membershipRegistrationDate));

    // 회원유형과 활동유형은 뱃지 포함해서 표시
    updateElementHTML('userAccountType', getUserTypeDisplay(user.accountTypeId, user.accountTypeName));
    updateElementHTML('userActivityType', getActivityTypeDisplay(user.activityTypeId, user.activityTypeName));
}

/**
 * 지식제공자 요청 정보 표시
 */
function displayProviderRequestInfo(providerData) {
    const contentArea = document.querySelector('.content-area');

    if (!providerData) {
        // 요청 정보가 없는 경우
        contentArea.innerHTML = `
            <div class="no-request-info">
                <h2 class="section-title">❌ 지식제공자 요청 정보 없음</h2>
                <div style="text-align: center; padding: 60px; color: #666;">
                    <p style="font-size: 18px; margin-bottom: 20px;">이 사용자의 지식제공자 요청 정보를 찾을 수 없습니다.</p>
                    <p style="font-size: 14px;">다음과 같은 경우일 수 있습니다:</p>
                    <ul style="text-align: left; display: inline-block; margin-top: 15px;">
                        <li>아직 지식제공자 요청을 하지 않음</li>
                        <li>이미 요청이 처리되어 삭제됨</li>
                        <li>데이터베이스 연결 문제</li>
                    </ul>
                </div>
            </div>
        `;
        return;
    }

    console.log('지식제공자 요청 정보 표시:', providerData);

    // 외부 링크 처리
    const externalLinks = providerData.externalLink ?
        providerData.externalLink.split('\n').filter(link => link.trim()) : [];

    // 승인 상태 확인
    const isApproved = providerData.approvalDate != null;
    const statusBadge = isApproved ?
        '<span class="status-badge status-request-approved">✅ 승인됨</span>' :
        '<span class="status-badge status-request-pending">⏳ 승인 대기중</span>';

    contentArea.innerHTML = `
        <div class="provider-request-info">
            <h2 class="section-title">📋 지식제공자 요청 정보 ${statusBadge}</h2>

            <!-- 요청자 기본 정보 -->
            <div class="request-section" style="margin-bottom: 30px;">
                <h3 class="sub-section-title">👤 요청자 정보</h3>
                <div class="info-grid" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 20px;">
                    <div class="info-field">
                        <label>법적 이름 (실명)</label>
                        <input type="text" class="display-field" value="${providerData.legalName || '정보 없음'}" readonly>
                    </div>
                    <div class="info-field">
                        <label>신청 시 닉네임</label>
                        <input type="text" class="display-field" value="${providerData.nickname || '정보 없음'}" readonly>
                    </div>
                </div>
                <div class="info-grid" style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                    <div class="info-field">
                        <label>요청일시</label>
                        <input type="text" class="display-field" value="${formatDateTime(providerData.requestDate)}" readonly>
                    </div>
                    <div class="info-field">
                        <label>승인일시</label>
                        <input type="text" class="display-field" value="${providerData.approvalDate ? formatDateTime(providerData.approvalDate) : '미승인'}" readonly>
                    </div>
                </div>
            </div>

            <!-- 외부 링크 -->
            <div class="request-section" style="margin-bottom: 30px;">
                <h3 class="sub-section-title">🔗 외부 링크</h3>
                <div class="external-links">
                    ${externalLinks.length > 0 ?
                        externalLinks.map((link, index) => `
                            <div class="info-field" style="margin-bottom: 10px;">
                                <label>링크 ${index + 1}</label>
                                <input type="url" class="display-field" value="${link}" readonly onclick="window.open('${link}', '_blank')">
                            </div>
                        `).join('') :
                        '<p style="color: #666; font-style: italic;">외부 링크가 없습니다.</p>'
                    }
                </div>
            </div>

            <!-- 소개 내용 -->
            <div class="request-section">
                <h3 class="sub-section-title">📝 지식제공자 소개</h3>
                <textarea class="display-textarea" readonly style="min-height: 200px;">${providerData.infoContent || '소개 내용이 없습니다.'}</textarea>
            </div>
        </div>
    `;
}

/**
 * 액션 버튼 설정
 */
function setupActionButtons(user, providerData) {
    const approveBtn = document.getElementById('approveBtn');
    const rejectBtn = document.getElementById('rejectBtn');

    if (!user || !approveBtn || !rejectBtn) {
        return;
    }

    // 지식제공자 요청 상태(accountTypeId = 2)이고 요청 데이터가 있는 경우에만 버튼 표시
    if (user.accountTypeId === 2 && providerData && !providerData.approvalDate) {
        approveBtn.style.display = 'block';
        rejectBtn.style.display = 'block';
        console.log('승인/거부 버튼 활성화');
    } else {
        approveBtn.style.display = 'none';
        rejectBtn.style.display = 'none';

        if (user.accountTypeId === 1) {
            showAlert('이미 지식제공자로 승인된 사용자입니다.', 'success');
        } else if (user.accountTypeId === 0) {
            showAlert('일반 회원 상태입니다.', 'info');
        } else if (providerData && providerData.approvalDate) {
            showAlert('이미 처리된 요청입니다.', 'info');
        }

        console.log('승인/거부 버튼 비활성화 - userType:', user.accountTypeId, 'hasRequest:', !!providerData);
    }
}

/**
 * 지식제공자 요청 승인 처리
 */
async function handleApproveRequest() {
    if (!currentUser || !currentProviderRequest) {
        showAlert('요청 정보를 찾을 수 없습니다.', 'danger');
        return;
    }

    const confirmMessage = `${currentUser.userNickname} 님을 지식제공자로 승인하시겠습니까?\n\n` +
                          `법적 이름: ${currentProviderRequest.legalName}\n` +
                          `요청일: ${formatDateTime(currentProviderRequest.requestDate)}\n\n` +
                          `승인 후에는 회원유형이 "지식제공자"로 변경되고, 요청 데이터에 승인일이 기록됩니다.`;

    if (!confirm(confirmMessage)) {
        return;
    }

    try {
        const response = await fetch(`/api/admin/user/${userId}/userTypeNo`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userTypeNo: 1 }) // 지식제공자로 변경
        });

        if (!response.ok) {
            throw new Error(`승인 처리 실패: ${response.status}`);
        }

        const result = await response.text();
        showAlert('지식제공자 요청이 승인되었습니다! 🎉', 'success');

        // 페이지 새로고침하여 변경사항 반영
        setTimeout(() => {
            location.reload();
        }, 2000);

    } catch (error) {
        console.error('승인 처리 실패:', error);
        showAlert(`승인 처리 실패: ${error.message}`, 'danger');
    }
}

/**
 * 지식제공자 요청 거부 처리
 */
async function handleRejectRequest() {
    if (!currentUser || !currentProviderRequest) {
        showAlert('요청 정보를 찾을 수 없습니다.', 'danger');
        return;
    }

    const confirmMessage = `${currentUser.userNickname} 님의 지식제공자 요청을 거부하시겠습니까?\n\n` +
                          `법적 이름: ${currentProviderRequest.legalName}\n` +
                          `요청일: ${formatDateTime(currentProviderRequest.requestDate)}\n\n` +
                          `거부 후에는 회원유형이 "일반회원"으로 변경되고, 요청 데이터가 삭제됩니다.`;

    if (!confirm(confirmMessage)) {
        return;
    }

    try {
        const response = await fetch(`/api/admin/user/${userId}/userTypeNo`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userTypeNo: 0 }) // 일반회원으로 변경
        });

        if (!response.ok) {
            throw new Error(`거부 처리 실패: ${response.status}`);
        }

        const result = await response.text();
        showAlert('지식제공자 요청이 거부되었습니다.', 'success');

        // 페이지 새로고침하여 변경사항 반영
        setTimeout(() => {
            location.reload();
        }, 2000);

    } catch (error) {
        console.error('거부 처리 실패:', error);
        showAlert(`거부 처리 실패: ${error.message}`, 'danger');
    }
}

/**
 * 목록으로 돌아가기
 */
function goBackToUserList() {
    window.location.href = '/admin/userlist';
}

/**
 * 유틸리티 함수들
 */

function updateElementText(id, text) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = text || '정보 없음';
    }
}

function updateElementHTML(id, html) {
    const element = document.getElementById(id);
    if (element) {
        element.innerHTML = html || '정보 없음';
    }
}

function getUserTypeDisplay(accountTypeId, accountTypeName) {
    const badge = getUserTypeBadge(accountTypeId);
    return `${accountTypeName || '알 수 없음'} ${badge}`;
}

function getActivityTypeDisplay(activityTypeId, activityTypeName) {
    const badge = getActivityBadge(activityTypeId);
    return `${activityTypeName || '알 수 없음'} ${badge}`;
}

function getUserTypeBadge(accountTypeId) {
    switch(accountTypeId) {
        case 0: return '<span class="status-badge status-user">일반회원</span>';
        case 1: return '<span class="status-badge status-provider">지식제공자</span>';
        case 2: return '<span class="status-badge status-provider-request">지식제공자 요청</span>';
        case 3: return '<span class="status-badge status-admin">관리자</span>';
        default: return '<span class="status-badge status-user">알 수 없음</span>';
    }
}

function getActivityBadge(activityTypeId) {
    switch(activityTypeId) {
        case 1: return '<span class="status-badge status-active">활동중</span>';
        case 2: return '<span class="status-badge status-inactive">활동정지</span>';
        default: return '<span class="status-badge status-user">알 수 없음</span>';
    }
}

function formatDate(dateString) {
    if (!dateString) return '정보 없음';

    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return dateString;

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}.${month}.${day}`;
    } catch (e) {
        return dateString;
    }
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '정보 없음';

    try {
        const date = new Date(dateTimeString);
        if (isNaN(date.getTime())) return dateTimeString;

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');

        return `${year}.${month}.${day} ${hours}:${minutes}`;
    } catch (e) {
        return dateTimeString;
    }
}

function showLoading() {
    const contentArea = document.querySelector('.content-area');
    if (contentArea) {
        contentArea.innerHTML = `
            <div class="loading-container">
                <div class="loading-spinner"></div>
                <p>지식제공자 요청 정보를 불러오는 중...</p>
            </div>
        `;
    }
}

function hideLoading() {
    // displayProviderRequestInfo에서 컨텐츠가 교체되므로 별도 처리 불필요
}

function showError(message) {
    const contentArea = document.querySelector('.content-area');
    if (contentArea) {
        contentArea.innerHTML = `
            <div class="error-state">
                <h3>⚠️ 오류 발생</h3>
                <p>${message}</p>
                <button class="btn-action btn-back" onclick="goBackToUserList()" style="margin-top: 20px;">
                    목록으로 돌아가기
                </button>
            </div>
        `;
    }
}

function showAlert(message, type = 'success') {
    // 기존 알림 제거
    const existingAlert = document.querySelector('.alert');
    if (existingAlert) {
        existingAlert.remove();
    }

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;

    const container = document.querySelector('.instructor-profile-container');
    if (container) {
        container.insertBefore(alertDiv, container.firstChild);

        // 3초 후 자동 제거
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 3000);
    }
}