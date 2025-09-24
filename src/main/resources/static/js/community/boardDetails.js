// URL에서 postId 추출
const pathSegments = window.location.pathname.split('/');
const postId = pathSegments[pathSegments.length - 1];
console.log("postId:", postId);

// URL에서 category 파라미터 추출
const urlParams = new URLSearchParams(window.location.search);
const currentCategory = urlParams.get('category');
console.log("currentCategory:", currentCategory);

// 카테고리별 postType 매핑 (board.js와 동일)
const categoryPostTypeMap = {
    'free': [2],           // 자유 게시판
    'study': [3, 4],       // 스터디 모집
    'qna': [5, 6],         // 질문 & 답변
    'notice': [1]          // 공지 게시판
};

// 게시글 타입으로 카테고리 추정하는 함수
function getCategoryFromPostType(postType) {
    for (const [category, types] of Object.entries(categoryPostTypeMap)) {
        if (types.includes(postType)) {
            return category;
        }
    }
    return 'free'; // 기본값
}

// 돌아가기 기능
function goBackToBoard() {
    let backUrl = '/auth/community/board';

    // URL 파라미터로 category가 있다면 해당 카테고리로 돌아가기
    if (currentCategory) {
        backUrl += `?category=${currentCategory}`;
    } else {
        // 게시글 타입을 기반으로 카테고리 추정
        const categoryElement = document.querySelector('.post-category');
        if (categoryElement) {
            const categoryText = categoryElement.textContent.trim();
            let estimatedCategory = 'free'; // 기본값

            if (categoryText.includes('스터디') || categoryText.includes('모집')) {
                estimatedCategory = 'study';
            } else if (categoryText.includes('질문') || categoryText.includes('Q&A') || categoryText.includes('QnA')) {
                estimatedCategory = 'qna';
            } else if (categoryText.includes('공지')) {
                estimatedCategory = 'notice';
            }

            backUrl += `?category=${estimatedCategory}`;
        }
    }

    window.location.href = backUrl;
}

// 🔥 게시물 삭제 권한 확인
function checkCanDeletePost(postId) {
    fetch(`/api/community/boardDetails/${postId}/can-delete`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.canDelete) {
                showDeleteButton(postId, data);
            }
        })
        .catch(error => console.error('삭제 권한 확인 오류:', error));
}

// 🔥 삭제 버튼 표시
function showDeleteButton(postId, permissionData) {
    const actionsDiv = document.querySelector('.post-actions');
    if (!actionsDiv) return;

    // 이미 삭제 버튼이 있는지 확인
    if (document.getElementById(`delete-button-${postId}`)) return;

    const deleteButton = document.createElement("button");
    deleteButton.id = `delete-button-${postId}`;
    deleteButton.className = "btn danger";
    deleteButton.textContent = "삭제";

    // 관리자인지 여부에 따라 다른 삭제 함수 호출
    if (permissionData.isAdmin && !permissionData.isOwner) {
        deleteButton.onclick = function() { deletePostWithModal(postId); }; // 관리자용 상세 모달
    } else {
        deleteButton.onclick = function() { deletePost(postId); }; // 일반 사용자용 간단 입력
    }

    // 신고 버튼 앞에 삽입
    const reportButton = document.getElementById(`report-button-${postId}`);
    if (reportButton) {
        actionsDiv.insertBefore(deleteButton, reportButton);
    } else {
        actionsDiv.appendChild(deleteButton);
    }
}

// 🔥 일반 게시물 삭제 (100자 제한)
function deletePost(postId) {
    const deleteReason = prompt("삭제 사유를 입력해주세요 (최대 100자):\n(예: 내용 수정 필요, 중복 게시물 등)");

    if (!deleteReason || deleteReason.trim() === "") {
        alert("삭제 사유를 입력해야 합니다.");
        return;
    }

    if (deleteReason.trim().length < 5) {
        alert("삭제 사유를 5자 이상 입력해주세요.");
        return;
    }

    if (deleteReason.trim().length > 100) {
        alert("삭제 사유는 100자 이내로 입력해주세요.");
        return;
    }

    const confirmMessage = `정말로 이 게시물을 삭제하시겠습니까?\n\n` +
                          `삭제 사유: ${deleteReason.trim()}\n\n` +
                          `• 삭제된 게시물은 "[삭제된 게시물]"로 표시됩니다.\n` +
                          `• 게시물 내용은 삭제 사유로 대체됩니다.\n` +
                          `• 삭제 후에는 복구할 수 없습니다.`;

    if (!confirm(confirmMessage)) {
        return;
    }

    executeDeletePost(postId, deleteReason.trim());
}

// 🔥 관리자용 게시물 삭제 (상세 모달)
function deletePostWithModal(postId) {
    showDeleteReasonModal(postId);
}

// 🔥 삭제 사유 입력 모달 수정 (100자 제한)
function showDeleteReasonModal(postId) {
    // 기존 모달이 있으면 제거
    const existingModal = document.getElementById('deleteReasonModal');
    if (existingModal) {
        existingModal.remove();
    }

    const modalHtml = `
        <div id="deleteReasonModal" class="modal" data-post-id="${postId}">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title">게시물 삭제</h3>
                    <span class="close" onclick="closeDeleteReasonModal()">&times;</span>
                </div>
                <div class="modal-body">
                    <label for="deleteReasonText">삭제 사유 (필수, 최대 100자):</label>
                    <textarea id="deleteReasonText" rows="3" maxlength="100" placeholder="삭제 사유를 입력해주세요...&#10;예: 부적절한 내용, 스팸, 중복 게시물, 규정 위반 등"></textarea>
                    <div class="char-count">
                        <span id="charCount">0</span>/100자
                    </div>
                    <div class="delete-warning">
                        <p><strong>⚠️ 삭제 시 주의사항:</strong></p>
                        <ul>
                            <li>게시물 제목이 "[삭제된 게시물]"로 변경됩니다</li>
                            <li>게시물 내용이 입력한 삭제 사유로 대체됩니다</li>
                            <li>삭제 후에는 복구할 수 없습니다</li>
                            <li>삭제된 게시물은 일반 사용자에게 보이지 않습니다</li>
                        </ul>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" onclick="executeDeleteWithReason()" class="btn-delete">삭제하기</button>
                    <button type="button" onclick="closeDeleteReasonModal()" class="btn-cancel">취소</button>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);
    document.getElementById('deleteReasonModal').style.display = 'block';

    // 텍스트 영역에 포커스 및 글자 수 카운터 이벤트
    const textarea = document.getElementById('deleteReasonText');
    const charCount = document.getElementById('charCount');

    textarea.focus();

    textarea.addEventListener('input', function() {
        const currentLength = this.value.length;
        charCount.textContent = currentLength;

        if (currentLength > 100) {
            charCount.style.color = 'red';
        } else {
            charCount.style.color = '#666';
        }
    });
}

// 🔥 모달에서 삭제 실행 (100자 제한)
function executeDeleteWithReason() {
    const modal = document.getElementById('deleteReasonModal');
    const postId = modal.dataset.postId;
    const deleteReason = document.getElementById('deleteReasonText').value.trim();

    if (!deleteReason || deleteReason.length < 5) {
        alert('삭제 사유를 5자 이상 입력해주세요.');
        return;
    }

    if (deleteReason.length > 100) {
        alert('삭제 사유는 100자 이내로 입력해주세요.');
        return;
    }

    const deleteButton = modal.querySelector('.btn-delete');
    deleteButton.disabled = true;
    deleteButton.textContent = '삭제 중...';

    executeDeletePost(postId, deleteReason)
        .then(function() {
            closeDeleteReasonModal();
        })
        .catch(function() {
            deleteButton.disabled = false;
            deleteButton.textContent = '삭제하기';
        });
}

// 🔥 실제 삭제 API 호출
function executeDeletePost(postId, deleteReason) {
    // 삭제 버튼 비활성화
    const deleteButton = document.getElementById(`delete-button-${postId}`);
    if (deleteButton) {
        deleteButton.disabled = true;
        deleteButton.textContent = "삭제 중...";
    }

    return fetch(`/api/community/boardDetails/${postId}`, {
        method: "DELETE",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            deleteReason: deleteReason
        })
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error(`삭제 실패: ${response.status}`);
        }
        return response.text();
    })
    .then(function(data) {
        alert("게시물이 삭제되었습니다.");
        goBackToBoard();
    })
    .catch(function(error) {
        console.error('게시물 삭제 오류:', error);
        alert("게시물 삭제 중 오류가 발생했습니다: " + error.message);

        // 삭제 버튼 복구
        if (deleteButton) {
            deleteButton.disabled = false;
            deleteButton.textContent = "삭제";
        }
        throw error; // 모달용 에러 처리를 위해 재던짐
    });
}

// 🔥 삭제 모달 닫기
function closeDeleteReasonModal() {
    const modal = document.getElementById('deleteReasonModal');
    if (modal) {
        modal.remove();
    }
}

// 좋아요 상태 확인
function checkLikeStatus(postId) {
    fetch(`/api/community/boardDetails/${postId}/like-status`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateLikeButton(postId, data.isLiked, data.likeCount);
            }
        })
        .catch(error => console.error('좋아요 상태 확인 오류:', error));
}

// 좋아요 버튼 UI 업데이트
function updateLikeButton(postId, isLiked, likeCount) {
    const likeCountElement = document.getElementById(`like-count-${postId}`);
    const likeButton = document.getElementById(`like-button-${postId}`);

    if (likeCountElement) {
        likeCountElement.textContent = likeCount;
    }

    if (likeButton) {
        likeButton.className = isLiked ? 'btn liked' : 'btn';
        likeButton.innerHTML = isLiked ? '❤️ ' + likeCount : '♡ ' + likeCount;
    }
}

// 좋아요 토글
function toggleLike(postId) {
    fetch(`/api/community/boardDetails/${postId}/like`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            updateLikeButton(postId, data.isLiked, data.likeCount);
        } else {
            alert(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('오류가 발생했습니다.');
    });
}

// 신고 모달 관련 함수들
function showReportModal(id, type) {
    const modal = document.getElementById('reportModal');
    const titleText = type === 'post' ? '게시물 신고' : '댓글 신고';

    modal.querySelector('.modal-title').textContent = titleText;
    modal.querySelector('#reportContent').value = '';
    modal.style.display = 'block';

    modal.dataset.targetId = id;
    modal.dataset.targetType = type;
}

function closeReportModal() {
    document.getElementById('reportModal').style.display = 'none';
}

function submitReport() {
    const modal = document.getElementById('reportModal');
    const targetId = modal.dataset.targetId;
    const targetType = modal.dataset.targetType;
    const content = document.getElementById('reportContent').value.trim();

    if (!content) {
        alert('신고 사유를 입력해주세요.');
        return;
    }

    const url = targetType === 'post'
        ? `/api/community/boardDetails/${targetId}/report`
        : `/api/community/comment/${targetId}/report`;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ content: content })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            closeReportModal();
            updateReportButton(targetId, targetType);
        } else {
            alert(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('오류가 발생했습니다.');
    });
}

function updateReportButton(id, type) {
    const button = document.getElementById(`report-button-${id}`);
    if (button) {
        button.textContent = '신고됨';
        button.disabled = true;
        button.style.opacity = '0.5';
        button.onclick = null;
    }
}

function checkReportStatus(id, type) {
    const url = type === 'post'
        ? `/api/community/boardDetails/${id}/report-status`
        : `/api/community/comment/${id}/report-status`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.isReported) {
                updateReportButton(id, type);
            }
        })
        .catch(error => console.error('신고 상태 확인 오류:', error));
}

// 🔥 수정된 댓글 목록 불러오기
function loadComments() {
    fetch(`/api/community/boardDetails/${postId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("존재하지 않는 게시글입니다.");
            }
            return response.json();
        })
        .then(data => {
            // 게시글 정보 표시
            const postHtml = `
                <div class="post-header">
                    <div class="post-category">${data.postTypeName}</div>
                    <div class="post-title">${data.postTitle}</div>
                    <div class="post-meta">
                        <span>작성자: ${data.writerNickname}</span>
                        <span>작성일: ${data.writeDate}</span>
                    </div>
                    <div class="post-stats">
                        <span>조회수 ${data.postView}</span>
                        <span>좋아요 <span id="like-count-${postId}">${data.postLikeNum || 0}</span></span>
                        <div class="post-actions">
                            <button id="like-button-${postId}" onclick="toggleLike('${postId}')" class="btn">
                                ♡ ${data.postLikeNum || 0}
                            </button>
                            <button id="report-button-${postId}" onclick="showReportModal('${postId}', 'post')" class="btn danger">
                                신고
                            </button>
                        </div>
                    </div>
                </div>
                  <div class="post-content smart-editor-content">${data.postContent}</div>

            `;
            document.getElementById("postDetail").innerHTML = postHtml;

            // 좋아요 상태 확인
            checkLikeStatus(postId);
            // 신고 상태 확인
            checkReportStatus(postId, 'post');
            // 상태 변경 버튼 확인
            checkCanToggleStatus(postId);
            // 삭제 권한 확인 및 버튼 표시
            checkCanDeletePost(postId);

            // 댓글 수 표시
            const commentCount = data.comments ? data.comments.length : 0;
            document.getElementById("commentNum").textContent = `댓글 ${commentCount}개`;

            // 댓글 렌더링
            const commentContainer = document.getElementById("commentview");
            if (data.comments && data.comments.length > 0) {
                renderComments(data.comments, commentContainer);
            } else {
                commentContainer.innerHTML = '<div class="empty-state">댓글이 없습니다.</div>';
            }

            // 게시글 데이터 로드 후 사이드바 메뉴 활성화
            setActiveSidebarMenu(data.postTypeName);
        })
        .catch(error => {
            document.getElementById("postDetail").innerHTML = `<div class="post-header"><div class="post-title">${error.message}</div></div>`;
        });
}

// 🔥 수정된 댓글 렌더링 함수 (트리 구조 복원)
function renderComments(comments, container) {
    // 댓글 ID → 닉네임 매핑
    const commentNicknameMap = {};
    comments.forEach(comment => {
        commentNicknameMap[comment.commentId] = comment.writerNickname;
        if (comment.replies) {
            comment.replies.forEach(reply => {
                commentNicknameMap[reply.commentId] = reply.writerNickname;
            });
        }
    });

    // 멘션 포맷팅
    function formatContent(content) {
        const mentionMatch = content.match(/^@([A-Za-z0-9]+)::\s*/);
        if (mentionMatch) {
            const mentionedId = mentionMatch[1];
            const mentionedName = commentNicknameMap[mentionedId] || "알 수 없음";
            return `<span class="mention">@${mentionedName}</span> ${content.replace(/^@[A-Za-z0-9]+::\s*/, "")}`;
        }
        return content;
    }

    // 댓글 HTML 생성
    function renderComment(comment, isReply = false) {
        const repliesHtml = comment.replies && comment.replies.length > 0
            ? `<div class="replies">${comment.replies.map(reply => renderComment(reply, true)).join('')}</div>`
            : '';

        const replyButton = !isReply
            ? `<span class="comment-action" onclick="showReplyForm('${comment.commentId}')">답글</span>`
            : '';

        return `
            <div class="${isReply ? 'reply-item' : 'comment-item'}">
                <div class="comment-header">
                    <span class="comment-author">${comment.writerNickname}</span>
                    <span class="comment-time">${comment.writeDate}</span>
                </div>
                <div class="comment-content" id="content-${comment.commentId}">
                    ${formatContent(comment.content)}
                </div>
                <div class="comment-actions">
                    ${replyButton}
                    <span class="comment-action" onclick="showEditForm('${comment.commentId}')">수정</span>
                    <span class="comment-action danger" onclick="deleteComment('${comment.commentId}')">삭제</span>
                    <span class="comment-action danger" onclick="showReportModal('${comment.commentId}', 'comment')">신고</span>
                </div>
                <div class="edit-form" id="edit-form-${comment.commentId}">
                    <input type="text" id="edit-input-${comment.commentId}" class="edit-input" value="${comment.content.replace(/^@[A-Za-z0-9]+::\s*/, '')}">
                    <div class="edit-buttons">
                        <button class="edit-save" onclick="saveEdit('${comment.commentId}')">저장</button>
                        <button class="edit-cancel" onclick="cancelEdit('${comment.commentId}')">취소</button>
                    </div>
                </div>
                ${!isReply ? `<div class="reply-form" id="reply-form-${comment.commentId}">
                    <div class="reply-form-group">
                        <input type="text" id="reply-input-${comment.commentId}" class="reply-input" placeholder="대댓글을 작성해주세요">
                        <button class="reply-submit" onclick="saveReply('${comment.commentId}')">등록</button>
                        <button class="reply-cancel" onclick="cancelReply('${comment.commentId}')">취소</button>
                    </div>
                </div>` : ''}
                ${repliesHtml}
            </div>
        `;
    }

    container.innerHTML = comments.map(comment => renderComment(comment)).join('');

    // 댓글 신고 상태 확인
    comments.forEach(comment => {
        checkReportStatus(comment.commentId, 'comment');
        if (comment.replies) {
            comment.replies.forEach(reply => {
                checkReportStatus(reply.commentId, 'comment');
            });
        }
    });
}

// 🔥 대댓글 관련 함수들 (기존 방식 복원)
function showReplyForm(commentId) {
    const form = document.getElementById(`reply-form-${commentId}`);
    if (form) {
        form.style.display = form.style.display === 'none' ? 'block' : 'none';
    }
}

function saveReply(commentId) {
    const input = document.getElementById(`reply-input-${commentId}`);
    const content = input.value.trim();

    if (!content) {
        alert("대댓글 내용을 입력해주세요.");
        return;
    }

    // 🔥 기존 방식으로 포맷팅 복원
    const formattedContent = `@${commentId}::${content}`;

    fetch(`/api/community/boardDetails`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            postId: postId,
            content: formattedContent,  // 포맷팅된 내용
            recommentCheck: 1,
            parentCommentId: commentId
        })
    })
    .then(res => {
        if (!res.ok) throw new Error("대댓글 등록 실패");
        return res.text();
    })
    .then(data => {
        alert("대댓글이 등록되었습니다!");
        input.value = "";
        document.getElementById(`reply-form-${commentId}`).style.display = "none";
        loadComments();
    })
    .catch(err => alert(err.message));
}

function cancelReply(commentId) {
    const form = document.getElementById(`reply-form-${commentId}`);
    const input = document.getElementById(`reply-input-${commentId}`);
    if (input) input.value = "";
    if (form) form.style.display = "none";
}

function showEditForm(commentId) {
    const form = document.getElementById(`edit-form-${commentId}`);
    const content = document.getElementById(`content-${commentId}`);

    if (form && content) {
        form.style.display = form.style.display === 'none' ? 'block' : 'none';
        content.style.display = form.style.display === 'block' ? 'none' : 'block';
    }
}

function saveEdit(commentId) {
    const input = document.getElementById(`edit-input-${commentId}`);
    const newContent = input.value.trim();

    if (!newContent) {
        alert("댓글 내용을 입력해주세요.");
        return;
    }

    fetch(`/api/community/comment/${commentId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ content: newContent })
    })
    .then(res => {
        if (!res.ok) throw new Error("댓글 수정 실패");
        return res.text();
    })
    .then(data => {
        alert("댓글이 수정되었습니다!");
        loadComments();
    })
    .catch(err => alert(err.message));
}

function cancelEdit(commentId) {
    const form = document.getElementById(`edit-form-${commentId}`);
    const content = document.getElementById(`content-${commentId}`);

    if (form && content) {
        form.style.display = "none";
        content.style.display = "block";
    }
}

function deleteComment(commentId) {
    if (!confirm("정말로 댓글을 삭제하시겠습니까?")) {
        return;
    }

    fetch(`/api/community/comment/${commentId}`, {
        method: "DELETE"
    })
    .then(res => {
        if (!res.ok) throw new Error("댓글 삭제 실패");
        return res.text();
    })
    .then(data => {
        alert("댓글이 삭제되었습니다!");
        loadComments();
    })
    .catch(err => alert(err.message));
}

// 게시물 상태 변경 관련 함수들
function checkCanToggleStatus(postId) {
    fetch(`/api/community/boardDetails/${postId}/can-toggle-status`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.canToggle) {
                showToggleButton(postId);
            }
        })
        .catch(error => console.error('상태 변경 가능 여부 확인 오류:', error));
}

function showToggleButton(postId) {
    const actionsDiv = document.querySelector('.post-actions');
    if (!actionsDiv) return;

    const toggleButton = document.createElement("button");
    toggleButton.id = `toggle-status-${postId}`;
    toggleButton.className = "btn success";
    toggleButton.onclick = () => togglePostStatus(postId);

    const categoryElement = document.querySelector('.post-category');
    const categoryText = categoryElement ? categoryElement.textContent : '';

    if (categoryText.includes('모집중')) {
        toggleButton.textContent = '모집완료';
    } else if (categoryText.includes('모집완료')) {
        toggleButton.textContent = '모집중';
    } else if (categoryText.includes('미해결')) {
        toggleButton.textContent = '해결';
    } else if (categoryText.includes('해결')) {
        toggleButton.textContent = '미해결';
    }

    actionsDiv.appendChild(toggleButton);
}

function togglePostStatus(postId) {
    if (!confirm('게시물 상태를 변경하시겠습니까?')) {
        return;
    }

    fetch(`/api/community/boardDetails/${postId}/toggle-status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            location.reload();
        } else {
            alert(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('오류가 발생했습니다.');
    });
}

// 사이드바 메뉴 활성화 설정
function setActiveSidebarMenu(postTypeName) {
    // 모든 메뉴에서 active 클래스 제거
    document.querySelectorAll('.sidebar-menu a').forEach(link => {
        link.classList.remove('active');
    });

    let targetCategory = '';

    // 1. URL 파라미터 우선 확인
    if (currentCategory) {
        targetCategory = currentCategory;
    } else if (postTypeName) {
        // 2. 게시글 타입으로 카테고리 추정
        if (postTypeName.includes('자유') || postTypeName.includes('일반')) {
            targetCategory = 'free';
        } else if (postTypeName.includes('스터디') || postTypeName.includes('모집')) {
            targetCategory = 'study';
        } else if (postTypeName.includes('질문') || postTypeName.includes('Q&A') || postTypeName.includes('QnA')) {
            targetCategory = 'qna';
        } else if (postTypeName.includes('공지')) {
            targetCategory = 'notice';
        } else {
            targetCategory = 'free'; // 기본값
        }
    } else {
        targetCategory = 'free'; // 기본값
    }

    // 해당 카테고리 메뉴 활성화
    const targetMenu = document.getElementById(`menu-${targetCategory}`);
    if (targetMenu) {
        targetMenu.classList.add('active');
    }

    console.log('활성화된 사이드바 메뉴:', targetCategory);
}

// 댓글 등록 이벤트 리스너
document.getElementById("commentSubmitBtn").addEventListener("click", () => {
    const input = document.getElementById("commentInput");
    const content = input.value.trim();

    if (!content) {
        alert("댓글 내용을 입력해주세요.");
        return;
    }

    fetch(`/api/community/boardDetails`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            postId: postId,
            content: content,
            recommentCheck: 0,
            parentCommentId: null
        })
    })
    .then(res => {
        if (!res.ok) throw new Error("댓글 등록 실패");
        return res.text();
    })
    .then(data => {
        alert("댓글이 등록되었습니다!");
        input.value = "";
        loadComments();
    })
    .catch(err => alert(err.message));
});

// 초기 로드
if (!postId) {
    document.getElementById("postDetail").innerHTML = '<div class="post-header"><div class="post-title">게시글 ID가 없습니다.</div></div>';
} else {
    loadComments();
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const reportModal = document.getElementById('reportModal');
    const deleteModal = document.getElementById('deleteReasonModal');

    if (event.target === reportModal) {
        closeReportModal();
    }
    if (event.target === deleteModal) {
        closeDeleteReasonModal();
    }
}