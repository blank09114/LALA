/**
 * 마이페이지 UI 상호작용 모듈
 * 
 * 기능:
 * - 사이드바 메뉴 활성화 상태 관리
 * - 서브메뉴 및 필터 버튼 상호작용
 * - 편집 모드 토글 및 모달 관리
 * - 탭 전환 기능
 */

// ==================== 상수 정의 ====================
const CSS_CLASSES = {
    ACTIVE: 'active',
    HIDDEN: 'none'
};

const DISPLAY_MODES = {
    BLOCK: 'block',
    NONE: 'none',
    FLEX: 'flex'
};

const SELECTORS = {
    SIDEBAR_LINKS: '.sidebar-menu a',
    MENU_ITEMS: '.menu li',
    FILTER_BUTTONS: '.filter-buttons button',
    TAB_CONTENT: '.tab-content',
    INFO_VIEW: '#info-view',
    INFO_EDIT: '#info-edit',
    MODAL: '#modal'
};

// ==================== 메인 핸들러 객체 ====================
const MypageHandler = {

    /**
     * 사이드바 메뉴 활성화 상태 초기화
     * 현재 페이지 URL과 메뉴 링크를 비교하여 active 클래스 추가
     */
    initSidebarMenu() {
        const links = document.querySelectorAll(SELECTORS.SIDEBAR_LINKS);
        const currentPath = this.getCurrentPath();

        if (!links.length) {
            console.warn('사이드바 메뉴 링크를 찾을 수 없습니다.');
            return;
        }

        links.forEach(link => {
            const linkPath = this.extractPathFromHref(link.getAttribute('href'));
            
            if (linkPath === currentPath) {
                this.setActiveState(link, true);
            } else {
                this.setActiveState(link, false);
            }
        });
    },

    /**
     * 서브메뉴 클릭 이벤트 초기화
     * 단일 선택 방식으로 활성화 상태 관리
     */
    initSubMenu() {
        const menuItems = document.querySelectorAll(SELECTORS.MENU_ITEMS);

        if (!menuItems.length) {
            console.warn('서브메뉴 항목을 찾을 수 없습니다.');
            return;
        }

        menuItems.forEach(item => {
            item.addEventListener('click', (event) => {
                this.handleSubMenuClick(event.currentTarget, menuItems);
            });
        });
    },

    /**
     * 필터 버튼 클릭 이벤트 초기화
     * 토글 방식으로 활성화 상태 관리 (같은 버튼 클릭 시 비활성화)
     */
    initFilterButtons() {
        const filterButtons = document.querySelectorAll(SELECTORS.FILTER_BUTTONS);

        if (!filterButtons.length) {
            console.warn('필터 버튼을 찾을 수 없습니다.');
            return;
        }

        filterButtons.forEach(button => {
            button.addEventListener('click', (event) => {
                this.handleFilterButtonClick(event.currentTarget, filterButtons);
            });
        });
    },

    // ==================== 이벤트 핸들러 메서드 ====================

    /**
     * 서브메뉴 클릭 처리
     * @param {HTMLElement} clickedItem - 클릭된 메뉴 항목
     * @param {NodeList} allItems - 모든 메뉴 항목
     */
    handleSubMenuClick(clickedItem, allItems) {
        try {
            // 모든 항목에서 active 클래스 제거
            allItems.forEach(item => this.setActiveState(item, false));
            
            // 클릭된 항목에 active 클래스 추가
            this.setActiveState(clickedItem, true);
        } catch (error) {
            console.error('서브메뉴 클릭 처리 중 오류:', error);
        }
    },

    /**
     * 필터 버튼 클릭 처리
     * @param {HTMLElement} clickedButton - 클릭된 버튼
     * @param {NodeList} allButtons - 모든 필터 버튼
     */
    handleFilterButtonClick(clickedButton, allButtons) {
        try {
            const isCurrentlyActive = clickedButton.classList.contains(CSS_CLASSES.ACTIVE);

            if (isCurrentlyActive) {
                // 현재 활성화된 버튼을 다시 클릭한 경우 비활성화
                this.setActiveState(clickedButton, false);
            } else {
                // 다른 버튼을 클릭한 경우 모든 버튼 비활성화 후 해당 버튼만 활성화
                allButtons.forEach(button => this.setActiveState(button, false));
                this.setActiveState(clickedButton, true);
            }
        } catch (error) {
            console.error('필터 버튼 클릭 처리 중 오류:', error);
        }
    },

    // ==================== 유틸리티 메서드 ====================

    /**
     * 현재 페이지 경로 반환
     * @returns {string} 현재 페이지의 파일명
     */
    getCurrentPath() {
        return window.location.pathname.split('/').pop() || '';
    },

    /**
     * href 속성에서 경로 추출
     * @param {string} href - 링크의 href 속성값
     * @returns {string} 추출된 경로
     */
    extractPathFromHref(href) {
        if (!href) return '';
        return href.split('/').pop() || '';
    },

    /**
     * 요소의 활성화 상태 설정
     * @param {HTMLElement} element - 대상 요소
     * @param {boolean} isActive - 활성화 여부
     */
    setActiveState(element, isActive) {
        if (!element) return;

        if (isActive) {
            element.classList.add(CSS_CLASSES.ACTIVE);
        } else {
            element.classList.remove(CSS_CLASSES.ACTIVE);
        }
    }
};

// 유틸리티 함수들
const toggleEdit = (isEditing) => {
    const viewElement = document.getElementById(SELECTORS.INFO_VIEW.substring(1));
    const editElement = document.getElementById(SELECTORS.INFO_EDIT.substring(1));

    if (!viewElement || !editElement) {
        console.error('편집 모드 전환에 필요한 요소를 찾을 수 없습니다.');
        return;
    }

    try {
        if (isEditing) {
            viewElement.style.display = DISPLAY_MODES.NONE;
            editElement.style.display = DISPLAY_MODES.BLOCK;
        } else {
            viewElement.style.display = DISPLAY_MODES.BLOCK;
            editElement.style.display = DISPLAY_MODES.NONE;
        }
    } catch (error) {
        console.error('편집 모드 전환 중 오류:', error);
    }
};

const openModal = () => {
    const modal = document.getElementById(SELECTORS.MODAL.substring(1));
    
    if (!modal) {
        console.error('모달 요소를 찾을 수 없습니다.');
        return;
    }

    try {
        modal.style.display = DISPLAY_MODES.FLEX;
        
        // 접근성을 위한 포커스 관리
        const firstFocusableElement = modal.querySelector('button, input, textarea, select');
        if (firstFocusableElement) {
            firstFocusableElement.focus();
        }
    } catch (error) {
        console.error('모달 열기 중 오류:', error);
    }
};

const closeModal = () => {
    const modal = document.getElementById(SELECTORS.MODAL.substring(1));
    
    if (!modal) {
        console.error('모달 요소를 찾을 수 없습니다.');
        return;
    }

    try {
        modal.style.display = DISPLAY_MODES.NONE;
    } catch (error) {
        console.error('모달 닫기 중 오류:', error);
    }
};

const showTab = (tabId) => {
    if (!tabId) {
        console.error('탭 ID가 제공되지 않았습니다.');
        return;
    }

    try {
        // 모든 탭 콘텐츠 숨기기
        const allTabContents = document.querySelectorAll(SELECTORS.TAB_CONTENT);
        allTabContents.forEach(element => {
            element.style.display = DISPLAY_MODES.NONE;
        });

        // 선택된 탭만 표시
        const targetTab = document.getElementById(tabId);
        if (targetTab) {
            targetTab.style.display = DISPLAY_MODES.BLOCK;
        } else {
            console.error(`ID가 '${tabId}'인 탭을 찾을 수 없습니다.`);
        }
    } catch (error) {
        console.error('탭 전환 중 오류:', error);
    }
};

// ==================== 초기화 ====================

/**
 * DOM 로드 완료 시 초기화 실행
 */
document.addEventListener('DOMContentLoaded', () => {
    try {
        MypageHandler.initSidebarMenu();
        MypageHandler.initSubMenu();
        MypageHandler.initFilterButtons();
        
        console.log('마이페이지 UI 핸들러 초기화 완료');
    } catch (error) {
        
    }
});

// ==================== 글로벌 노출 ====================
window.MypageHandler = MypageHandler;
window.toggleEdit = toggleEdit;
window.openModal = openModal;
window.closeModal = closeModal;
window.showTab = showTab;