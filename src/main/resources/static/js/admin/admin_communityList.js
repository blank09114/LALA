    let currentPage = 1;
    let pageSize = 10; // 한 페이지에 보여줄 사용자 수
    let allPosts = [];

  document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("searchPostForm");
    const resetBtn = document.getElementById("resetBtn");

    fetchPosts(); // 초기 전체 조회

    form.addEventListener("submit", function (e) {
      e.preventDefault();
      const query = buildQuery();
      fetchPosts(query);
    });

    resetBtn.addEventListener("click", function () {
      form.reset();
      fetchPosts();
    });
  });

  function buildQuery() {
        const formData = new FormData(document.getElementById("searchPostForm"));
        const params = new URLSearchParams();

          for (const [key, value] of formData.entries()) {
          if (value) params.append(key, value);
          }

           params.append("page", currentPage);
           params.append("size", pageSize);


        return `?${params.toString()}`;
      }

  function fetchPosts(query = "") {
    fetch(`/api/admin/communitylist${query}`)
      .then(res => res.json())
      .then(data => {
        allPosts = data.communitys;
        renderTable(allPosts);
        renderPagination(data.total); // ← 전체 유저 수
      })
      .catch(err => console.error("게시물 목록 조회 실패:", err));
  }


  function renderTable(communitys) {
    const tbody = document.getElementById("postTableBody");
    tbody.innerHTML = "";

    communitys.forEach(post => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${post.writerNickname}</td>
        <td>${post.contentTitle}</td>
        <td>${post.accountType}</td>
        <td>${post.postType}</td>
        <td>${post.writeDate}</td>
        <td>${post.reportType ? '✅' : '❌'}</td>
        <td><a href="/auth/community/boardDetails/${post.postId}"><img src="/img/info.png" class="details-icon" alt="상세"></a></td>
      `;
      tbody.appendChild(row);
    });
  }

  function renderPagination(total) {
    const pagination = document.querySelector(".pagination");
    pagination.innerHTML = "";

    const totalPages = Math.ceil(total / pageSize);

    for (let i = 1; i <= totalPages; i++) {
      const btn = document.createElement("button");
      btn.textContent = i;
      btn.classList.add("page-btn");

      if (i === currentPage) btn.classList.add("active");

       btn.addEventListener("click", () => {
           currentPage = i;
           const query = buildQuery();
           fetchPosts(query);
         });
           pagination.appendChild(btn);
    }
}

//    const next = document.createElement("button");
//    next.textContent = ">";
//    next.onclick = () => {
//      if (currentPage < pageCount) {
//        currentPage++;
//        updateView();
//      }
//    };
//    pagination.appendChild(next);
//  }