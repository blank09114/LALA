//왼쪽부분
function InstructorProfile_lecture() {
    const providerId = window.serverData ? window.serverData.providerId : null;
    console.log('Test Value:', providerId);


    // 프로필 이미지
    fetch(`/api/course/providerDetail/${providerId}`)
            .then(response => {
                if (!response.ok) throw new Error('응답 실패: ' + response.status);
                return response.json();
            })
            .then(data => {
                console.log('API 응답(프로필 이미지):', data);
                const profileImage = document.getElementById('profileImage');
                if (!profileImage) return;

                const profilename = data.infos?.profilename;

                if (profilename) {
                    // 절대 경로면 그대로, 상대 경로면 /upload/profile/ 붙이기
                    profileImage.src = profilename.startsWith('http')
                        ? profilename
                        : `/upload/profile/${profilename}`;
                } else {
                    profileImage.src = '/img/profileImg.png';
                }
            })
            .catch(error => {
                console.error('프로필이미지 에러:', error);
                const profileImage = document.getElementById('profileImage');
                if (profileImage) {
                    profileImage.src = '/img/profileImg.png';
                }
            });

    //const providerId = document.getElementById('providerId').dataset.providerId;
    //지식제공자 이름 출력
    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답:', data);

            // 지식제공자 닉네임만 교체
            document.getElementById('nickname').textContent = data.infos.nickname;
        })
        .catch(error => {
            console.error('에에러러:', error);
            document.getElementById('nickname').textContent = '에러 발생';
        });


    //수강생 수
    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답:', data);


            document.getElementById('studentNum').textContent = data.infos.studentNum;
        })
        .catch(error => {
            console.error('에에러러:', error);
            document.getElementById('studentNum').textContent = '에러 발생';
        });

    //수강평수
    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답:', data);


            document.getElementById('reviewNum').textContent = data.infos.reviewNum;
        })
        .catch(error => {
            console.error('에에러러:', error);
            document.getElementById('reviewNum').textContent = '에러 발생';
        });


    // 평균 평점
    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답(평균 평점):', data);
            document.getElementById('reviewAvg').textContent = data.infos.reviewAvg;
        })
        .catch(error => {
            console.error('에에러러:', error);
            document.getElementById('reviewAvg').textContent = '에러 발생';
        });

    // 외부 링크
    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답(외부 링크):', data);
            const externalLinkElement = document.getElementById('externalLink');
            externalLinkElement.textContent = '관련 링크 이동';
            externalLinkElement.href = data.infos.externalLink;
        })
        .catch(error => {
            console.error('에에러러:', error);
            document.getElementById('externalLink').textContent = '에러 발생';
        });

    // 소개글
    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답(소개글):', data);
            document.getElementById('infoContent').textContent = data.infos.infoContent;
        })
        .catch(error => {
            console.error('에에러러:', error);
            document.getElementById('infoContent').textContent = '에러 발생';
        });


}

//찜하기 버튼 눌렀을 때 토스트되는거?
function showToastFav() {
    const toast = document.getElementById("toast");
    toast.textContent = "해당 지식제공자가 좋아요 목록에 추가되었습니다.";
    toast.style.visibility = "visible";
    toast.style.opacity = "1";

    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.visibility = "hidden";
    }, 2000); // 3초 후 사라짐
}


let currentPage = 1;
const reviewsPerPage = 3;
let reviewsData = [];

// 리뷰 전체 불러오기 & 초기 렌더링
function InstructorProfile_review() {
    const providerId = window.serverData ? window.serverData.providerId : null;
    console.log('providerId 확인:', providerId);

    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답(수강평):', data);
            reviewsData = Array.isArray(data.reviews) ? data.reviews : [];

            // 전체 수강평 개수 표시
            const totalEl = document.querySelector('.total-number');
            if (totalEl) totalEl.textContent = reviewsData.length;

            renderReviews();
            renderPagination();
        })
        .catch(error => {
            console.error('수강평 로드 실패:', error);
        });
}

// 리뷰 리스트 렌더링
function renderReviews() {
    const reviewList = document.querySelector('.review-list');
    reviewList.innerHTML = ''; // 기존 내용 비움

    const start = (currentPage - 1) * reviewsPerPage;
    const end = start + reviewsPerPage;
    const pageReviews = reviewsData.slice(start, end);

    pageReviews.forEach((review) => {
        const reviewItem = document.createElement('div');
        reviewItem.classList.add('review-container');
        reviewItem.innerHTML = `
      <div class="review1">
        <div class="thumbnail">
          <img src="${review.thumbnail && review.thumbnail.startsWith('http') ? review.thumbnail : '/img/thumbnail.png'}" alt="수강평 썸네일">
        </div>
        <div>
          <div>
            <div class="star">${makeStars(review.avgReviewRate)}</div>
            <div class="nickname1">${review.writerId || '익명'}</div>
            <div class="writeDate">${formatDate(review.writeDate)}</div>
          </div>
          <div class="title">[리뷰ID:${review.reviewId}]</div>
        </div>
      </div>
      <div class="review2">
        <div class="content">${review.content || '내용 없음'}</div>
      </div>
    `;
        reviewList.appendChild(reviewItem);
    });
}

// 페이징ㅇ
function renderPagination() {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    const totalPages = Math.ceil(reviewsData.length / reviewsPerPage);

    const prevBtn = document.createElement('button');
    prevBtn.innerHTML = '&lt;';
    prevBtn.classList.add('page-btn');
    prevBtn.disabled = currentPage === 1;
    prevBtn.addEventListener('click', () => {
        if (currentPage > 1) {
            currentPage--;
            renderReviews();
            renderPagination();
        }
    });
    pagination.appendChild(prevBtn);

    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement('button');
        btn.classList.add('page-btn');
        if (i === currentPage) btn.classList.add('active');
        btn.textContent = i;
        btn.addEventListener('click', () => {
            currentPage = i;
            renderReviews();
            renderPagination();
        });
        pagination.appendChild(btn);
    }

    const nextBtn = document.createElement('button');
    nextBtn.innerHTML = '&gt;';
    nextBtn.classList.add('page-btn');
    nextBtn.disabled = currentPage === totalPages;
    nextBtn.addEventListener('click', () => {
        if (currentPage < totalPages) {
            currentPage++;
            renderReviews();
            renderPagination();
        }
    });
    pagination.appendChild(nextBtn);
}

// 별점 숫자를 별 모양으로 변환
function makeStars(rate) {
    const parsedRate = parseFloat(rate);
    if (isNaN(parsedRate)) return '☆☆☆☆☆'; // 숫자가 아니면 기본값

    const r = Math.round(parsedRate); // 0~5 사이 정수로 반올림
    const filled = '★'.repeat(r);
    const empty = '☆'.repeat(5 - r);
    return filled + empty;
}


// 날짜 포맷 변환(yyyy.mm.dd)
function formatDate(dateString) {
    if (!dateString) return '날짜 없음';
    const d = new Date(dateString);
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}


/*티스토리, 블로그 링크 가져오기 시작*/
const providerId = window.serverData?.providerId;

fetch(`/api/course/providerDetail/${providerId}`)
  .then(res => res.json())
  .then(data => {
    const linksStr = data.infos.externalLink;
    if (!linksStr) return;

    const links = linksStr.split(',').map(link => link.trim()).filter(link => link);

    // 첫 번째 링크 (tistory)
    const tistoryAnchor = document.getElementById('externalLink1');
    const tistoryImg = tistoryAnchor?.previousElementSibling; // 이미지 태그

    if (links[0] && tistoryAnchor) {
      tistoryAnchor.href = links[0];
      tistoryAnchor.textContent = '관련 링크 이동';
    } else {
      tistoryAnchor?.remove();
      tistoryImg?.remove(); // 이미지도 같이 제거
    }

    // 두 번째 링크 (blog)
    const blogAnchor = document.getElementById('externalLink2');
    const blogImg = blogAnchor?.previousElementSibling; // 이미지 태그

    if (links[1] && blogAnchor) {
      blogAnchor.href = links[1];
      blogAnchor.textContent = '관련 링크 이동';
    } else {
      blogAnchor?.remove();
      blogImg?.remove(); // 이미지도 같이 제거
    }
  })
  .catch(err => {
    console.error('externalLink 불러오기 실패:', err);
  });
/*티스토리, 블로그 링크 가져오기 끝*/

window.onload = function() {
    InstructorProfile_lecture();
    InstructorProfile_review();
};

