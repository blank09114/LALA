document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("modal");
  const modalClose = modal.querySelector(".close-btn");
  const modalContent = document.getElementById("modal-content-dynamic");
  const modalTitle = document.getElementById("modal-title");

  const dataMap = {
    "report-posts": Array.from({ length: 10 }, (_, i) => ({
      type: "스터디",
      title: `스터디 모집 ${i + 1}`,
      content: `신고 내용 ${i + 1}`,
      date: "2025.06.20",
      review: "좋아요",
      reporter: `사용자 ${i + 1}`
    })),
    "report-comments": Array.from({ length: 10 }, (_, i) => ({
      type: "댓글",
      title: `댓글 ${i + 1}`,
      content: `부적절한 댓글 내용 ${i + 1}`,
      date: "2025.06.21",
      review: "",
      reporter: `댓글 ${i + 1}`
    })),
    "report-reviews": Array.from({ length: 10 }, (_, i) => ({
      type: "수강평",
      title: `강의 수강평 ${i + 1}`,
      content: `수강 후 느낀 점 ${i + 1}`,
      date: "2025.06.22",
      review: `★★★★★`,
      reporter: `수강자 ${i + 1}`
    })),
    "report-replies": Array.from({ length: 10 }, (_, i) => ({
      type: "대댓글",
      title: `대댓글 ${i + 1}`,
      content: `부적절한 대댓글 내용 ${i + 1}`,
      date: "2025.06.23",
      review: "",
      reporter: `대댓글러 ${i + 1}`
    }))

  };

  
  Object.entries(dataMap).forEach(([containerId, reports]) => {
    const container = document.getElementById(containerId);
    if (!container) return;

    reports.slice(0, 3).forEach((report) => {
      const block = createReportBlock(report);
      container.appendChild(block);
    });
  });

  
  document.querySelectorAll(".open-modal").forEach((btn) => {
    btn.addEventListener("click", function (e) {
      e.preventDefault();
      const card = btn.closest(".community-card");
      const headerText = card.querySelector(".community-card-header span").textContent;
      const containerId = card.querySelector(".community-card-content").id;
      const fullData = dataMap[containerId] || [];

      modalTitle.textContent = headerText;
      modalContent.innerHTML = ""; // 초기화

      fullData.forEach((report) => {
        const block = createReportBlock(report);
        modalContent.appendChild(block);
      });

      modal.style.display = "block";
    });
  });

  modalClose.addEventListener("click", () => (modal.style.display = "none"));
  window.addEventListener("click", (e) => {
    if (e.target === modal) modal.style.display = "none";
  });


  function createReportBlock(report) {
    const block = document.createElement("div");
    block.className = "report-block";
    block.innerHTML = `
      <div class="report-row">
        <div class="report-left">
          <div><strong>타입</strong> ${report.type}</div>
          <div><strong>제목</strong> ${report.title}</div>
          <div><strong>신고 내용</strong> ${report.content}</div>
        </div>
        <div class="report-divider"></div>
        <div class="report-right">
          <div><strong>신고일</strong> ${report.date}</div>
          <div><strong>수강평</strong> ${report.review || '&nbsp;'}</div>
          <div><strong>신고자</strong> ${report.reporter}</div>
        </div>
      </div>
    `;
    return block;
  }
});


//상세 눌렀을때 넘어오는
const urlParams = new URLSearchParams(window.location.search);
const userName = urlParams.get('name');

if (userName) {
  const nameTitle = document.querySelector('.user-name');
  if (nameTitle) {
    nameTitle.textContent = `${userName}님의 상세 페이지입니다`;
  }
}
