/**
 * 🛒 LALA 프로젝트 장바구니 관리 시스템
 * localStorage를 사용하여 브라우저 캐시에 장바구니 데이터 저장
 */
const LalaCartManager = {
    // 설정값들
    CART_KEY: 'lala_lecture_cart',          // localStorage 키
    MAX_CART_ITEMS: 20,                     // 최대 장바구니 담기 개수
    
    /**
     * 🎯 장바구니에 강의 추가
     * @param {Object} lectureData - 강의 정보 객체
     * @returns {boolean} - 성공 여부
     */
    addToCart: function(lectureData) {
        try {
            // 필수 데이터 검증
            if (!this.validateLectureData(lectureData)) {
                this.showNotification('강의 정보가 올바르지 않습니다.', 'error');
                return false;
            }

            let cart = this.getCart();
            
            // 최대 개수 체크
            if (cart.length >= this.MAX_CART_ITEMS) {
                this.showNotification(`장바구니에는 최대 ${this.MAX_CART_ITEMS}개까지만 담을 수 있습니다.`, 'warning');
                return false;
            }
            
            // 중복 체크
            const existingItem = cart.find(item => item.lectureId === lectureData.lectureId);
            if (existingItem) {
                this.showNotification('이미 장바구니에 있는 강의입니다!', 'warning');
                return false;
            }

            // 장바구니에 추가 (타임스탬프와 함께)
            const cartItem = {
                ...lectureData,
                addedDate: new Date().toISOString(),
                cartItemId: this.generateCartItemId()
            };
            
            cart.push(cartItem);
            
            // localStorage에 저장
            localStorage.setItem(this.CART_KEY, JSON.stringify(cart));
            
            // UI 업데이트
            this.updateCartBadge();
            this.showNotification(`"${lectureData.title}"이(가) 장바구니에 추가되었습니다!`, 'success');
            
            // 커스텀 이벤트 발생 (다른 컴포넌트에서 감지 가능)
            this.triggerCartEvent('itemAdded', cartItem);
            
            return true;
            
        } catch (error) {
            console.error('장바구니 추가 실패:', error);
            this.showNotification('장바구니 추가 중 오류가 발생했습니다.', 'error');
            return false;
        }
    },

    /**
     * 🗑️ 장바구니에서 강의 제거
     * @param {string} lectureId - 제거할 강의 ID
     * @returns {boolean} - 성공 여부
     */
    removeFromCart: function(lectureId) {
        try {
            let cart = this.getCart();
            const originalLength = cart.length;
            const removedItem = cart.find(item => item.lectureId === lectureId);
            
            cart = cart.filter(item => item.lectureId !== lectureId);
            
            if (cart.length < originalLength) {
                localStorage.setItem(this.CART_KEY, JSON.stringify(cart));
                this.updateCartBadge();
                this.showNotification('강의가 장바구니에서 제거되었습니다.', 'success');
                
                // 커스텀 이벤트 발생
                this.triggerCartEvent('itemRemoved', removedItem);
                
                return true;
            }
            
            return false;
            
        } catch (error) {
            console.error('장바구니 제거 실패:', error);
            this.showNotification('장바구니 제거 중 오류가 발생했습니다.', 'error');
            return false;
        }
    },

    /**
     * 📦 장바구니 전체 조회
     * @returns {Array} - 장바구니 아이템 배열
     */
    getCart: function() {
        try {
            const cartData = localStorage.getItem(this.CART_KEY);
            return cartData ? JSON.parse(cartData) : [];
        } catch (error) {
            console.error('장바구니 조회 실패:', error);
            return [];
        }
    },

    /**
     * 🔍 특정 강의가 장바구니에 있는지 확인
     * @param {string} lectureId - 확인할 강의 ID
     * @returns {boolean} - 포함 여부
     */
    isInCart: function(lectureId) {
        const cart = this.getCart();
        return cart.some(item => item.lectureId === lectureId);
    },

    /**
     * 🧹 장바구니 전체 비우기
     */
    clearCart: function() {
        try {
            localStorage.removeItem(this.CART_KEY);
            this.updateCartBadge();
            this.showNotification('장바구니가 비워졌습니다.', 'success');
            
            // 커스텀 이벤트 발생
            this.triggerCartEvent('cartCleared', null);
            
        } catch (error) {
            console.error('장바구니 비우기 실패:', error);
        }
    },

    /**
     * 🔢 장바구니 개수 조회
     * @returns {number} - 장바구니 아이템 개수
     */
    getCartCount: function() {
        return this.getCart().length;
    },

    /**
     * 💰 장바구니 총 금액 계산
     * @returns {Object} - 가격 정보 객체
     */
    calculateTotal: function() {
        const cart = this.getCart();
        
        let subtotal = 0;
        let totalDiscount = 0;
        
        cart.forEach(item => {
            const originalPrice = item.price;
            const discountAmount = Math.floor(originalPrice * (item.discountRate || 0) / 100);
            
            subtotal += originalPrice;
            totalDiscount += discountAmount;
        });
        
        return {
            itemCount: cart.length,
            subtotal: subtotal,
            discount: totalDiscount,
            total: subtotal - totalDiscount
        };
    },

    /**
     * 🎨 장바구니 배지 업데이트 (헤더 등의 장바구니 아이콘 옆 숫자)
     */
    updateCartBadge: function() {
        const count = this.getCartCount();
        const badges = document.querySelectorAll('.cart-badge, .cart-count, #cartCount');
        
        badges.forEach(badge => {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline' : 'none';
        });
    },

    /**
     * 📢 알림 메시지 표시
     * @param {string} message - 표시할 메시지
     * @param {string} type - 알림 타입 (success, warning, error)
     */
    showNotification: function(message, type = 'success') {
        // 기존 토스트 메시지가 있다면 제거
        const existingToast = document.getElementById('cart-toast');
        if (existingToast) {
            existingToast.remove();
        }

        // 토스트 메시지 생성
        const toast = document.createElement('div');
        toast.id = 'cart-toast';
        toast.className = `cart-toast cart-toast-${type}`;
        toast.innerHTML = `
            <div class="cart-toast-content">
                <span class="cart-toast-icon">${this.getToastIcon(type)}</span>
                <span class="cart-toast-message">${message}</span>
                <button class="cart-toast-close" onclick="this.parentElement.parentElement.remove()">×</button>
            </div>
        `;

        // 스타일 추가 (한 번만)
        if (!document.getElementById('cart-toast-styles')) {
            const styles = document.createElement('style');
            styles.id = 'cart-toast-styles';
            styles.textContent = `
                .cart-toast {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    z-index: 10000;
                    min-width: 300px;
                    padding: 15px;
                    border-radius: 8px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.2);
                    animation: slideInRight 0.3s ease-out;
                }
                .cart-toast-success { background: #d4edda; border-left: 4px solid #28a745; color: #155724; }
                .cart-toast-warning { background: #fff3cd; border-left: 4px solid #ffc107; color: #856404; }
                .cart-toast-error { background: #f8d7da; border-left: 4px solid #dc3545; color: #721c24; }
                .cart-toast-content { display: flex; align-items: center; gap: 10px; }
                .cart-toast-icon { font-size: 18px; }
                .cart-toast-message { flex: 1; font-weight: 500; }
                .cart-toast-close { 
                    background: none; border: none; font-size: 20px; cursor: pointer; 
                    color: inherit; padding: 0; width: 24px; height: 24px; display: flex; 
                    align-items: center; justify-content: center; border-radius: 50%;
                }
                .cart-toast-close:hover { background: rgba(0,0,0,0.1); }
                @keyframes slideInRight {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
            `;
            document.head.appendChild(styles);
        }

        // 페이지에 추가
        document.body.appendChild(toast);

        // 5초 후 자동 제거
        setTimeout(() => {
            if (toast.parentNode) {
                toast.style.animation = 'slideInRight 0.3s ease-out reverse';
                setTimeout(() => toast.remove(), 300);
            }
        }, 5000);
    },

    /**
     * 🎭 토스트 아이콘 반환
     * @param {string} type - 알림 타입
     * @returns {string} - 아이콘 문자
     */
    getToastIcon: function(type) {
        switch(type) {
            case 'success': return '✅';
            case 'warning': return '⚠️';
            case 'error': return '❌';
            default: return 'ℹ️';
        }
    },

    /**
     * ✅ 강의 데이터 유효성 검사
     * @param {Object} lectureData - 검사할 강의 데이터
     * @returns {boolean} - 유효성 여부
     */
    validateLectureData: function(lectureData) {
        const required = ['lectureId', 'title', 'instructor', 'price'];
        return required.every(field => lectureData.hasOwnProperty(field) && lectureData[field] !== null);
    },

    /**
     * 🆔 고유한 장바구니 아이템 ID 생성
     * @returns {string} - 고유 ID
     */
    generateCartItemId: function() {
        return 'cart_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    },

    /**
     * 📡 커스텀 이벤트 발생 (다른 컴포넌트에서 장바구니 변화 감지 가능)
     * @param {string} eventType - 이벤트 타입
     * @param {Object} data - 이벤트 데이터
     */
    triggerCartEvent: function(eventType, data) {
        const event = new CustomEvent('lalaCartUpdate', {
            detail: {
                type: eventType,
                data: data,
                cartCount: this.getCartCount(),
                total: this.calculateTotal()
            }
        });
        document.dispatchEvent(event);
    },

    /**
     * 🚀 초기화 함수 (페이지 로드 시 호출)
     */
    init: function() {
        this.updateCartBadge();
        console.log('🛒 LALA 장바구니 시스템 초기화 완료');
        console.log('📦 현재 장바구니:', this.getCart());
        
        // 장바구니 이벤트 리스너 등록 예시
        document.addEventListener('lalaCartUpdate', (e) => {
            console.log('장바구니 업데이트:', e.detail);
        });
    }
};

// =============================================================================
// 🎯 사용하기 쉬운 래퍼 함수들 (전역 함수로 사용)
// =============================================================================

/**
 * 강의 상세페이지에서 사용할 장바구니 추가 함수
 * @param {string} lectureId - 강의 ID
 * @param {string} title - 강의 제목
 * @param {string} instructor - 강사명
 * @param {number} price - 가격
 * @param {number} discountRate - 할인율 (선택)
 * @param {string} thumbnail - 썸네일 (선택)
 */
function addLectureToCart(lectureId, title, instructor, price, discountRate = 0, thumbnail = '') {
    const lectureData = {
        lectureId: lectureId,
        title: title,
        instructor: instructor,
        price: price,
        discountRate: discountRate,
        thumbnail: thumbnail
    };
    
    return LalaCartManager.addToCart(lectureData);
}

/**
 * 강의가 이미 장바구니에 있는지 확인하는 함수
 * @param {string} lectureId - 확인할 강의 ID
 * @returns {boolean} - 장바구니 포함 여부
 */
function isLectureInCart(lectureId) {
    return LalaCartManager.isInCart(lectureId);
}

/**
 * 장바구니에서 강의 제거 함수
 * @param {string} lectureId - 제거할 강의 ID
 */
function removeLectureFromCart(lectureId) {
    return LalaCartManager.removeFromCart(lectureId);
}

/**
 * 장바구니 개수 조회 함수
 * @returns {number} - 장바구니 아이템 개수
 */
function getCartCount() {
    return LalaCartManager.getCartCount();
}

// =============================================================================
// 🚀 자동 초기화 (페이지 로드 시)
// =============================================================================
document.addEventListener('DOMContentLoaded', function() {
    LalaCartManager.init();
});

// 전역 객체로 노출 (디버깅 및 콘솔에서 접근 가능)
window.LalaCart = LalaCartManager;