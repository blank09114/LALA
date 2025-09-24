let allUsers = [];
let currentPage = 1;
let pageSize = 10; // 한 페이지에 보여줄 사용자 수

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("searchUserForm");
  const resetBtn = document.getElementById("resetBtn");

  fetchUsers();

  form.addEventListener("submit", function (e) {
    e.preventDefault();
    const query = buildQuery();
    fetchUsers(query);
  });

  resetBtn.addEventListener("click", function () {
    form.reset();
    fetchUsers();
  });
});


//function fetchUsers(query = "") {
//  fetch(`/api/admin/userlist${query}`)
//    .then(res => res.json())
//    .then(data => {
//      allUsers = data;
//      renderTable(allUsers);
//    })
//    .catch(err => console.error("회원 목록 조회 실패:", err));
//}

function fetchUsers(query = "") {
  fetch(`/api/admin/userlist${query}`)
    .then(res => res.json())
    .then(data => {
      allUsers = data.users;
      renderTable(allUsers);
      renderPagination(data.total); // ← 전체 유저 수
    })
    .catch(err => console.error("회원 목록 조회 실패:", err));
}

//function buildQuery() {
//  const formData = new FormData(document.getElementById("searchUserForm"));
//  const params = new URLSearchParams();
//
//  for (const pair of formData.entries()) {
//    params.append(pair[0], pair[1]);
//  }
//
//  return `?${params.toString()}`;
//}

function buildQuery() {
  const formData = new FormData(document.getElementById("searchUserForm"));
  const params = new URLSearchParams();

  for (const [key, value] of formData.entries()) {
    if (value) params.append(key, value);
  }

  params.append("page", currentPage);
  params.append("size", pageSize);

  return `?${params.toString()}`;
}


function renderTable(users) {
  const tbody = document.getElementById("userTableBody");
  tbody.innerHTML = "";

  users.forEach(user => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${user.userNickname}</td>
      <td>${user.email}</td>
      <td>${user.accountTypeName}</td>
      <td>${user.membershipRegistrationDate}</td>
      <td>${user.activityTypeName}</td>
      <td><a href="/admin/user/${user.userId}"><img src="/img/info.png" class="details-icon" alt="상세"></a></td>
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
      fetchUsers(query);
    });

    pagination.appendChild(btn);
  }
}


