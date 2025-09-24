    let currentPage = 1;
    let pageSize = 10; // 한 페이지에 보여줄 사용자 수
    let allLectures = [];

    document.addEventListener("DOMContentLoaded", () => {
      const form = document.getElementById("lectureSearchForm");
      const resetButton = document.getElementById("resetBtn");

      fetchLectures();

      form.addEventListener("submit", function (e) {
        e.preventDefault();
        const query = buildQuery();
        fetchLectures(query);
      });

      resetButton.addEventListener("click", function () {
        form.reset();
        fetchLectures();
      });
    });

    function fetchLectures(query = "") {
      fetch(`/api/admin/lecturelist${query}`)
        .then(res => res.json())
        .then(data => {
          allLectures = data.lectures;
          renderTable(allLectures);
          renderPagination(data.total); // ← 전체 유저 수
        })
        .catch(err => console.error("조회 실패:", err));
    }

    function buildQuery() {
      const formData = new FormData(document.getElementById("lectureSearchForm"));
      const params = new URLSearchParams();

        for (const [key, value] of formData.entries()) {
        if (value) params.append(key, value);
        }

         params.append("page", currentPage);
         params.append("size", pageSize);


      return `?${params.toString()}`;
    }

    function renderTable(lectures) {
      const tbody = document.getElementById("lectureTableBody");
      tbody.innerHTML = "";

      lectures.forEach(lecture => {
        const row = document.createElement("tr");
        row.innerHTML = `
          <td>${lecture.lectureNickname}</td>
          <td>${lecture.contentName}</td>
          <td>${lecture.requestType}</td>
          <td>${lecture.uploadDate}</td>
          <td>${lecture.statusName}</td>
          <td><a href="/lms/main/${lecture.lectureId}"><img src="/img/info.png" class="details-icon" alt="상세"></a></td>
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
      fetchLectures(query);
    });

    pagination.appendChild(btn);
  }
}