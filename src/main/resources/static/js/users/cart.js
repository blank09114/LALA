/**
 * 🛒 장바구니 페이지 관리 모듈
 * 페이지네이션, 체크박스 관리, 결제 기능을 포함합니다.
 *
 * @author LALA Team
 * @version 1.0.0
 */

class CartPageManager {
    constructor() {
        this.currentPage = 1;
        this.itemsPerPage = 5;
        this.totalItems = 0;
        this.totalPages = 0;
        this.allCartItems = [];

        this.elements = {
            container: document.getElementById('cart-items-container'),
            paginationContainer: document.getElementById('pagination-container'),
            checkAllBox: document.getElementById('checkAll'),
            totalAmount: document.getElementById('total-amount'),
            paymentDetails: document.getElementById('payment-details'),
            checkoutBtn: document.getElementById('checkout-btn')
        };

        this.init();
    }

    /**
     * 초기화 메서드
     * 이벤트 리스너 등록 및 초기 데이터 로드
     */
    init() {
        this.bindEvents();
        this.displayCartItems();
        console.log('🛒 장바구니 페이지 초기화 완료');
    }

    /**
     * 이벤트 리스너 바인딩
     */
    bindEvents() {
        // 전체 선택 체크박스
        this.elements.checkAllBox?.addEventListener('change', () => this.handleSelectAll());

        // 결제 버튼
        this.elements.checkoutBtn?.addEventListener('click', () => this.proceedToCheckout());

        // 장바구니 업데이트 이벤트
        document.addEventListener('lalaCartUpdate', (e) => this.handleCartUpdate(e));
    }

    /**
     * 전체 선택/해제 처리
     */
    handleSelectAll() {
        const isChecked = this.elements.checkAllBox.checked;
        const lectureChecks = document.querySelectorAll('.lecture-check');

        lectureChecks.forEach(check => {
            check.checked = isChecked;
        });

        this.updatePaymentTotal();
    }

    /**
     * 개별 체크박스 이벤트 설정
     */
    setupLectureCheckboxes() {
        const lectureChecks = document.querySelectorAll('.lecture-check');

        lectureChecks.forEach(check => {
            check.addEventListener('change', () => {
                this.updateSelectAllStatus();
                this.updatePaymentTotal();
            });
        });
    }

    /**
     * 전체 선택 상태 업데이트
     */
    updateSelectAllStatus() {
        const lectureChecks = document.querySelectorAll('.lecture-check');
        const checkedCount = document.querySelectorAll('.lecture-check:checked').length;

        this.elements.checkAllBox.checked =
            checkedCount === lectureChecks.length && lectureChecks.length > 0;
    }

    /**
     * 결제 금액 계산 및 업데이트
     */
    updatePaymentTotal() {
        const checkedLectures = document.querySelectorAll('.lecture-check:checked');
        let totalAmount = 0;
        let totalDiscount = 0;
        let subtotal = 0;

        checkedLectures.forEach(check => {
            const lectureCard = check.closest('.lecture-card');
            const lectureId = lectureCard.dataset.lectureId;
            const lecture = this.allCartItems.find(item => item.lectureId === lectureId);

            if (lecture) {
                const price = lecture.price;
                const discountAmount = Math.floor(price * (lecture.discountRate || 0) / 100);

                subtotal += price;
                totalDiscount += discountAmount;
                totalAmount += (price - discountAmount);
            }
        });

        this.updatePaymentUI(totalAmount, totalDiscount, subtotal, checkedLectures.length);
    }

    /**
     * 결제 UI 업데이트
     * @param {number} totalAmount - 총 결제 금액
     * @param {number} totalDiscount - 총 할인 금액
     * @param {number} subtotal - 상품 금액
     * @param {number} selectedCount - 선택된 강의 수
     */
    updatePaymentUI(totalAmount, totalDiscount, subtotal, selectedCount) {
        this.elements.totalAmount.textContent = `₩${totalAmount.toLocaleString()}`;

        if (selectedCount > 0) {
            this.elements.paymentDetails.innerHTML = `
                <div style="font-size: 14px; color: #666; margin: 10px 0;">
                    <div>선택된 강의: ${selectedCount}개</div>
                    ${totalDiscount > 0 ? `
                        <div>상품 금액: ₩${subtotal.toLocaleString()}</div>
                        <div style="color: #e74c3c;">할인 금액: -₩${totalDiscount.toLocaleString()}</div>
                    ` : ''}
                </div>
            `;
        } else {
            this.elements.paymentDetails.innerHTML =
                '<div style="font-size: 14px; color: #999;">선택된 강의가 없습니다</div>';
        }
    }

    /**
     * 페이지네이션 버튼 생성
     * @param {string} text - 버튼 텍스트
     * @param {Function} clickHandler - 클릭 핸들러
     * @param {boolean} disabled - 비활성화 여부
     * @param {boolean} active - 활성 상태 여부
     * @returns {HTMLButtonElement} 생성된 버튼
     */
    createPaginationButton(text, clickHandler, disabled = false, active = false) {
        const button = document.createElement('button');
        button.innerHTML = text;
        button.disabled = disabled;

        if (active) button.className = 'active';
        if (clickHandler) button.onclick = clickHandler;

        return button;
    }

    /**
     * 페이지네이션 UI 렌더링
     */
    renderPagination() {
        if (this.totalPages <= 1) {
            this.elements.paginationContainer.style.display = 'none';
            return;
        }

        this.elements.paginationContainer.style.display = 'flex';
        this.elements.paginationContainer.innerHTML = '';

        const maxVisiblePages = 5;
        let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(this.totalPages, startPage + maxVisiblePages - 1);

        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }

        // 이전 버튼
        const prevBtn = this.createPaginationButton(
            '&lt;',
            this.currentPage > 1 ? () => this.goToPage(this.currentPage - 1) : null,
            this.currentPage === 1
        );
        this.elements.paginationContainer.appendChild(prevBtn);

        // 첫 페이지 버튼
        if (startPage > 1) {
            const firstBtn = this.createPaginationButton(
                '1',
                () => this.goToPage(1),
                false,
                this.currentPage === 1
            );
            this.elements.paginationContainer.appendChild(firstBtn);

            if (startPage > 2) {
                const ellipsis = document.createElement('span');
                ellipsis.textContent = '...';
                ellipsis.style.margin = '0 10px';
                this.elements.paginationContainer.appendChild(ellipsis);
            }
        }

        // 페이지 번호 버튼들
        for (let i = startPage; i <= endPage; i++) {
            const pageBtn = this.createPaginationButton(
                i.toString(),
                () => this.goToPage(i),
                false,
                i === this.currentPage
            );
            this.elements.paginationContainer.appendChild(pageBtn);
        }

        // 마지막 페이지 버튼
        if (endPage < this.totalPages) {
            if (endPage < this.totalPages - 1) {
                const ellipsis = document.createElement('span');
                ellipsis.textContent = '...';
                ellipsis.style.margin = '0 10px';
                this.elements.paginationContainer.appendChild(ellipsis);
            }

            const lastBtn = this.createPaginationButton(
                this.totalPages.toString(),
                () => this.goToPage(this.totalPages),
                false,
                this.currentPage === this.totalPages
            );
            this.elements.paginationContainer.appendChild(lastBtn);
        }

        // 다음 버튼
        const nextBtn = this.createPaginationButton(
            '&gt;',
            this.currentPage < this.totalPages ? () => this.goToPage(this.currentPage + 1) : null,
            this.currentPage === this.totalPages
        );
        this.elements.paginationContainer.appendChild(nextBtn);
    }

    /**
     * 페이지 이동 처리
     * @param {number} pageNumber - 이동할 페이지 번호
     */
    goToPage(pageNumber) {
        if (pageNumber < 1 || pageNumber > this.totalPages) return;

        this.currentPage = pageNumber;
        this.displayCartItems();
        this.renderPagination();

        // 부드러운 스크롤
        document.querySelector('.basket-left')?.scrollIntoView({ behavior: 'smooth' });
    }

    /**
     * 현재 페이지에 해당하는 아이템들 반환
     * @returns {Array} 현재 페이지 아이템 배열
     */
    getCurrentPageItems() {
        const startIndex = (this.currentPage - 1) * this.itemsPerPage;
        const endIndex = startIndex + this.itemsPerPage;
        return this.allCartItems.slice(startIndex, endIndex);
    }

    /**
     * 장바구니 아이템들을 화면에 표시
     */
    displayCartItems() {
        this.allCartItems = LalaCartManager.getCart();
        this.totalItems = this.allCartItems.length;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);

        if (this.totalItems === 0) {
            this.displayEmptyCart();
            return;
        }

        // 현재 페이지가 범위를 벗어나면 첫 페이지로 이동
        if (this.currentPage > this.totalPages) {
            this.currentPage = 1;
        }

        const currentPageItems = this.getCurrentPageItems();
        this.renderCartItems(currentPageItems);
        this.addPageInfo();
        this.setupLectureCheckboxes();
        this.updatePaymentTotal();
        this.renderPagination();
    }

    /**
     * 빈 장바구니 화면 표시
     */
    displayEmptyCart() {
        this.elements.container.innerHTML = `
            <div class="empty-cart-message" style="text-align: center; padding: 60px 20px; color: #666;">
                <h3>🛒 장바구니가 비어있습니다</h3>
                <p>관심있는 강의를 장바구니에 담아보세요!</p>
                <a href="/course" style="display: inline-block; margin-top: 20px; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px;">강의 둘러보기</a>
            </div>
        `;
        this.elements.paginationContainer.style.display = 'none';
        this.updatePaymentTotal();
    }

    /**
     * 장바구니 아이템 HTML 렌더링
     * @param {Array} items - 렌더링할 아이템 배열
     */
    renderCartItems(items) {
        const cartHTML = items.map(item => this.createCartItemHTML(item)).join('');
        this.elements.container.innerHTML = cartHTML;
    }

    /**
     * 개별 장바구니 아이템 HTML 생성
     * @param {Object} item - 장바구니 아이템 객체
     * @returns {string} 생성된 HTML 문자열
     */
    createCartItemHTML(item) {
        const addedDate = new Date(item.addedDate).toLocaleDateString('ko-KR');
        const discountedPrice = item.price - Math.floor(item.price * (item.discountRate || 0) / 100);

        return `
            <div class="lecture-card" data-lecture-id="${item.lectureId}">
                <label class="custom-checkbox">
                    <input type="checkbox" class="lecture-check" ${item.checked ? 'checked' : ''} />
                    <span class="checkmark"></span>
                </label>

                <div class="remove-btn" onclick="cartPageManager.removeLectureItem('${item.lectureId}')">✕</div>

                <div class="left-section">
                    ${item.thumbnail ?
                        `<img src="${item.thumbnail}" alt="강의 이미지" style="width: 100%; height: 100%; object-fit: cover; border-radius: 8px;" />` :
                        `<div style="width: 100%; height: 100%; background: linear-gradient(45deg, #667eea, #764ba2); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold;">${item.title.charAt(0)}</div>`
                    }
                </div>

                <div class="center-section">
                    <h3>${item.title}</h3>
                    <p>${item.instructor}</p>
                    <span class="level">초급</span>
                    <div style="font-size: 12px; color: #999; margin-top: 5px;">담은 날짜: ${addedDate}</div>
                </div>

                <div class="divider"></div>

                <div class="right-section">
                    ${item.discountRate > 0 ? `
                        <div style="text-align: right;">
                            <div style="text-decoration: line-through; color: #999; font-size: 14px;">₩${item.price.toLocaleString()}</div>
                            <strong class="price">₩${discountedPrice.toLocaleString()}</strong>
                            <div style="color: #e74c3c; font-size: 12px;">${item.discountRate}% 할인</div>
                        </div>
                    ` : `
                        <strong class="price">₩${item.price.toLocaleString()}</strong>
                    `}
                </div>
            </div>
        `;
    }

    /**
     * 페이지 정보 추가
     */
    addPageInfo() {
        const pageInfo = document.createElement('div');
        pageInfo.style.cssText = 'text-align: center; margin: 20px 0; color: #666; font-size: 14px;';
//        pageInfo.innerHTML = `총 ${this.totalItems}개의 강의 중 ${((this.currentPage - 1) * this.itemsPerPage) + 1}-${Math.min(this.currentPage * this.itemsPerPage, this.totalItems)}번째 표시`;
        this.elements.container.appendChild(pageInfo);
    }

    /**
     * 장바구니에서 아이템 제거
     * @param {string} lectureId - 제거할 강의 ID
     */
    removeLectureItem(lectureId) {
        if (!confirm('정말 이 강의를 장바구니에서 제거하시겠습니까?')) {
            return;
        }

        LalaCartManager.removeFromCart(lectureId);

        // 현재 페이지 조정
        const newTotalItems = LalaCartManager.getCart().length;
        const newTotalPages = Math.ceil(newTotalItems / this.itemsPerPage);

        if (this.currentPage > newTotalPages && newTotalPages > 0) {
            this.currentPage = newTotalPages;
        }

        this.displayCartItems();
    }

    /**
     * 결제 진행 처리
     */
    proceedToCheckout() {
        const checkedLectures = document.querySelectorAll('.lecture-check:checked');

        if (checkedLectures.length === 0) {
            alert('결제할 강의를 선택해주세요!');
            return;
        }

        const selectedLectures = this.getSelectedLectures(checkedLectures);
        const lectureIds = selectedLectures.map(lecture => lecture.lectureId);

        // 구매 확인
        if (!confirm(`${selectedLectures.length}개 강의를 구매하시겠습니까?\n\n총 결제 금액: ₩${this.calculateTotalAmount(selectedLectures).toLocaleString()}`)) {
            return;
        }

        // 구매 API 호출
        this.processPurchase(lectureIds, selectedLectures);
    }

    /**
     * 구매 처리 API 호출
     * @param {Array} lectureIds - 구매할 강의 ID 배열
     * @param {Array} selectedLectures - 선택된 강의 정보 배열
     */
    async processPurchase(lectureIds, selectedLectures) {
        try {
            // 로딩 상태 표시
            this.showLoadingState();

            const response = await fetch('/users/mypage/purchase', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({
                    lectureIds: lectureIds
                })
            });

            const result = await response.json();

            if (response.ok && result.success) {
                // 구매 성공 처리
                this.handlePurchaseSuccess(result, selectedLectures);
            } else {
                // 구매 실패 처리
                this.handlePurchaseFailure(result);
            }

        } catch (error) {
            console.error('구매 처리 중 오류 발생:', error);
            alert('구매 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
        } finally {
            this.hideLoadingState();
        }
    }

    /**
     * 구매 성공 처리
     * @param {Object} result - 구매 결과
     * @param {Array} selectedLectures - 선택된 강의 정보
     */
    handlePurchaseSuccess(result, selectedLectures) {
        // 장바구니에서 구매한 강의들 제거
        selectedLectures.forEach(lecture => {
            LalaCartManager.removeFromCart(lecture.lectureId);
        });

        // 성공 메시지 표시
        const successMessage = `구매가 완료되었습니다!\n\n` +
            `성공: ${result.successCount}개\n` +
            `실패: ${result.failCount}개\n\n` +
            `구매한 강의는 '내 강의'에서 확인할 수 있습니다.`;

        alert(successMessage);

        // 장바구니 페이지 새로고침
        this.displayCartItems();
    }

    /**
     * 구매 실패 처리
     * @param {Object} result - 구매 결과
     */
    handlePurchaseFailure(result) {
        let errorMessage = '구매 처리에 실패했습니다.\n\n';
        
        if (result.purchaseResults) {
            const failedLectures = result.purchaseResults.filter(item => item.status === 'fail');
            if (failedLectures.length > 0) {
                errorMessage += '실패한 강의:\n';
                failedLectures.forEach(item => {
                    errorMessage += `- ${item.lectureId}: ${item.message}\n`;
                });
            }
        } else {
            errorMessage += result.message || '알 수 없는 오류가 발생했습니다.';
        }

        alert(errorMessage);
    }

    /**
     * 총 결제 금액 계산
     * @param {Array} selectedLectures - 선택된 강의 배열
     * @returns {number} 총 결제 금액
     */
    calculateTotalAmount(selectedLectures) {
        return selectedLectures.reduce((total, lecture) => {
            const price = lecture.price;
            const discountAmount = Math.floor(price * (lecture.discountRate || 0) / 100);
            return total + (price - discountAmount);
        }, 0);
    }

    /**
     * 로딩 상태 표시
     */
    showLoadingState() {
        if (this.elements.checkoutBtn) {
            this.elements.checkoutBtn.disabled = true;
            this.elements.checkoutBtn.textContent = '처리 중...';
        }
    }

    /**
     * 로딩 상태 해제
     */
    hideLoadingState() {
        if (this.elements.checkoutBtn) {
            this.elements.checkoutBtn.disabled = false;
            this.elements.checkoutBtn.textContent = '결제 요청';
        }
    }

    /**
     * 선택된 강의 정보 수집
     * @param {NodeList} checkedLectures - 선택된 체크박스 목록
     * @returns {Array} 선택된 강의 배열
     */
    getSelectedLectures(checkedLectures) {
        const selectedLectures = [];

        checkedLectures.forEach(check => {
            const lectureId = check.closest('.lecture-card').dataset.lectureId;
            const lecture = this.allCartItems.find(item => item.lectureId === lectureId);
            if (lecture) {
                selectedLectures.push(lecture);
            }
        });

        return selectedLectures;
    }



    /**
     * 장바구니 업데이트 이벤트 처리
     * @param {Event} e - 커스텀 이벤트
     */
    handleCartUpdate(e) {
        console.log('장바구니 업데이트 감지:', e.detail);

        if (e.detail.type === 'itemRemoved' || e.detail.type === 'cartCleared') {
            this.displayCartItems();
        }
    }

    /**
     * 페이지당 아이템 수 변경
     * @param {number} newItemsPerPage - 새로운 페이지당 아이템 수
     */
    changeItemsPerPage(newItemsPerPage) {
        this.itemsPerPage = newItemsPerPage;
        this.currentPage = 1;
        this.displayCartItems();
    }
}

// 전역 변수로 인스턴스 생성 (HTML에서 접근 가능하도록)
let cartPageManager;

// DOM 로드 완료 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    cartPageManager = new CartPageManager();
});