// 지식제공자 정보 로드 (왼쪽부분)
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


    // 닉네임
    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답(닉네임):', data);
            document.getElementById('nickname').textContent = data.infos.nickname;
        })
        .catch(error => {
            console.error('에에러러:', error);
            document.getElementById('nickname').textContent = '에러 발생';
        });

    // 수강생 수
    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답(수강생 수):', data);
            document.getElementById('studentNum').textContent = data.infos.studentNum;
        })
        .catch(error => {
            console.error('에에러러:', error);
            document.getElementById('studentNum').textContent = '에러 발생';
        });

    // 수강평 수
    fetch(`/api/course/providerDetail/${providerId}`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답(수강평 수):', data);
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

//찜하기 버튼 눌렀을 때 토스트?되는거
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

//
// 강의들 가져오기 (오른쪽 부분 강의 가져오는거..)
function InstructorProfile_lecture2() {
    const providerId = window.serverData ? window.serverData.providerId : null;
    console.log('Test Value:', providerId);


        // 전체 강의수 표시하기
          fetch(`/api/course/providerDetail/${providerId}`)
            .then(response => response.json())
            .then(data => {
              const cnt = Array.isArray(data.lectures) ? data.lectures.length : 0;
              const totalCount = document.querySelector('#total-count');
              if (totalCount) {
                totalCount.innerHTML = `
                  전체 <span style="
                    font-weight: bold;
                    color: #00B37E;
                    font-size: 18px;
                  ">${cnt}</span>
                `;
              }
            })
            .catch(error => {
              console.error('전체 강의수 불러오기 에러:', error);
            });




}

//강의 동적으로 생성하기2..
const pagesPerGroup = 5;
const lectureList = document.getElementById("lecture-list");
const pagination = document.getElementById("pagination");
const totalCount = document.getElementById("total-count");

const pageSize = 6;
let currentPage = 1;
window.lectures = [];

function loadLectures() {
  const providerId = window.serverData ? window.serverData.providerId : null;
  console.log('Test Value:', providerId);

  fetch(`/api/course/providerDetail/${providerId}`)
    .then(response => {
      if (!response.ok) throw new Error('네트워크 응답 오류');
      return response.json();
    })
    .then(data => {
      console.log('API 응답(강의목록):', data);
      window.lectures = Array.isArray(data.lectures) ? data.lectures : [];
      totalPage = Math.ceil(window.lectures.length / pageSize);

      renderLectures(currentPage);
      renderPagination();
    })
    .catch(error => {
      console.error('강의 로드 실패:', error);
    });
}


function renderLectures(page) {
  const container = document.getElementById('lecture-card');
  container.innerHTML = '';

  const startIdx = (page - 1) * pageSize;
  const endIdx = Math.min(startIdx + pageSize, window.lectures.length);
  const pageLectures = lectures.slice(startIdx, endIdx);

  pageLectures.forEach(lec => {
    const lecItem = document.createElement('a');
    lecItem.classList.add('lecItem');
    lecItem.href = `/course/detail/${lec.lectureId}`;

    const discountedPrice = Math.round(lec.price * (100 - lec.discountRate) / 100);

    lecItem.innerHTML = `
      <div class="lecImg" data-thumbnail="${lec.thumbnail}"></div>
      <div class="lecInfo">
        <h2 class="lecTitle">${lec.title}</h2>
        <p class="lecContent">
          ${
            lec.discountRate > 0
              ? `
                <div>
                  <span class="regPrice">₩${lec.price.toLocaleString()}</span><br>
                  <span class="salePrice">
                    <span class="discount" style="color: red;">${lec.discountRate}%&nbsp;</span>
                    <span>₩${discountedPrice.toLocaleString()}</span>
                  </span><br>
                </div>
              `
              : `
                <div>
                  <span class="salePrice">₩${lec.price.toLocaleString()}</span><br>
                </div>
              `
          }
          <span style="color: #FAB005;">★</span>${lec.avgReviewRate !== null ? lec.avgReviewRate.toFixed(1) : '0.0'} (${lec.reviewNum}) 👤${lec.studentNum || 0}</span><br>
        </p>
        <div class="lecGrade">
          ${
            lec.difficulty === 0 ? '입문' :
            lec.difficulty === 1 ? '초급' :
            lec.difficulty === 2 ? '중급' :
            lec.difficulty === 3 ? '고급' :
            '기타'
          }
        </div>
      </div>
    `;

    container.appendChild(lecItem);
  });

  const lecImages = document.querySelectorAll('.lecImg');
  lecImages.forEach(el => {
    const url = el.getAttribute('data-thumbnail');
    if (url) {
      el.style.backgroundImage = `url('${url}')`;
    } else {
      el.style.backgroundImage = `url('/img/LALA_log.png')`;
    }
  });
}




//페이징
function renderPagination() {
  const pagination = document.getElementById('pagination');
  pagination.innerHTML = '';

  const prevBtn = document.createElement('button');
  prevBtn.className = 'page-btn prev';
  prevBtn.textContent = '〈';
  prevBtn.disabled = currentPage === 1;
  prevBtn.addEventListener('click', () => {
    if (currentPage > 1) {
      currentPage--;
      renderLectures(currentPage);
      renderPagination();
    }
  });
  pagination.appendChild(prevBtn);

  for (let i = 1; i <= totalPage; i++) {
    const pageBtn = document.createElement('button');
    pageBtn.className = `page-btn ${i === currentPage ? 'active' : ''}`;
    pageBtn.textContent = i;
    pageBtn.addEventListener('click', () => {
      currentPage = i;
      renderLectures(currentPage);
      renderPagination();
    });
    pagination.appendChild(pageBtn);
  }

  const nextBtn = document.createElement('button');
  nextBtn.className = 'page-btn next';
  nextBtn.textContent = '〉';
  nextBtn.disabled = currentPage === totalPage;
  nextBtn.addEventListener('click', () => {
    if (currentPage < totalPage) {
      currentPage++;
      renderLectures(currentPage);
      renderPagination();
    }
  });
  pagination.appendChild(nextBtn);
}

/*찜하기 시작*/
//function JjimBtn(providerId) {
//     fetch('/users/mypage/like-provider/toggle', {
//         method: 'POST',
//         headers: {
//             'Content-Type': 'application/json',
//         },
//         body: JSON.stringify({ providerId })
//     })
//     .then(res => res.json())
//     .then(data => {
//         const favBtn = document.getElementById('favBtn');
//
//         if (data.liked) {
//             showToastFav();
//             favBtn.classList.add('active');
//             favBtn.textContent = "찜 완료";
//         } else {
//             alert('찜이 해제되었습니다.');
//             favBtn.classList.remove('active');
//             favBtn.textContent = "찜하기";
//         }
//
//         if (typeof loadProviders === 'function' && currentTab === 'providers') {
//             loadProviders(1);
//         }
//     })
//     .catch(err => {
//         console.error('찜 처리 실패:', err);
//     });
// }

function JjimBtn() {
    const providerId = window.serverData ? window.serverData.providerId : null;
    if (!providerId) {
        console.error('providerId 없음');
        return;
    }

    fetch(`/api/bookmark/toggle`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ providerId })
    })
    .then(res => res.json())
    .then(data => {
        const favBtn = document.getElementById('favBtn');

        if (data.liked) {
            showToastFav();
            favBtn.classList.add('active');
            favBtn.textContent = "찜 완료";
        } else {
            alert('찜이 해제되었습니다.');
            favBtn.classList.remove('active');
            favBtn.textContent = "찜하기";
        }

        if (typeof loadProviders === 'function' && currentTab === 'providers') {
            loadProviders(1);
        }
    })
    .catch(err => {
        console.error('찜 처리 실패:', err);
    });
}

/*찜하기 끝*/

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
    InstructorProfile_lecture2();
    loadLectures();
};
