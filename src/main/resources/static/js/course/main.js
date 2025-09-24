// 주간 시작일 계산 (일요일 기준)
function getStartOfWeek(date) {
  const result = new Date(date);
  const day = result.getDay(); // 0 = Sunday
  result.setDate(result.getDate() - day);
  return result;
}

// 날짜 포맷
function formatDate(date) {
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${mm}/${dd}`;
}
function formatHeaderDate(date) {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${yyyy}.${mm}.${dd}.`;
}

let monday = getStartOfWeek(new Date());

// 캘린더 전체 렌더링 시작
function updateCalendarUI() {
  monday = getStartOfWeek(new Date()); // 항상 이번 주로 고정
  renderCalendarHeader();

  const startDateParam = monday.toISOString().slice(0, 10); // yyyy-MM-dd
  fetch(`/api/course/main/data?startDate=${startDateParam}`)
    .then(res => res.json())
    .then(data => {
      renderCalendarLarge(data.studyPlanner.calender || []);
      renderPlannerGoal(data.studyPlanner.planner || []);
      renderRecentLecture(data.recentLecture);
    })
    .catch(err => console.error("메인 페이지 데이터 로딩 실패:", err));
}

// 요일 헤더 및 주간 범위 갱신
function renderCalendarHeader() {
  const cells = document.querySelectorAll('.calendar .cell');
  const dayNames = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];

  let dayIndex = 0;
  cells.forEach(cell => {
    if (cell.querySelector('.todo.goal')) return; // 주간 목표 셀 제외

    const dayEl = cell.querySelector('.day');
    if (!dayEl) return;

    const date = new Date(monday);
    date.setDate(monday.getDate() + dayIndex);

    const newText = `${dayNames[dayIndex]} (${formatDate(date)})`;
    dayEl.textContent = newText;
    dayIndex++;
  });

  // 주간 범위 텍스트
  const endDate = new Date(monday);
  endDate.setDate(monday.getDate() + 6);
  document.querySelectorAll(".range").forEach(el => {
    el.textContent = `${formatHeaderDate(monday)} ~ ${formatHeaderDate(endDate)}`;
  });

  // 주간 목표 셀 제목
  document.querySelectorAll(".todo.goal").forEach(goalUl => {
    const dayLabel = goalUl.previousElementSibling;
    if (dayLabel && dayLabel.classList.contains('day')) {
      dayLabel.textContent = `주간 목표 (${formatDate(monday)} ~ ${formatDate(endDate)})`;
    }
  });
}

// 캘린더 일정 렌더링
function renderCalendarLarge(calendarList) {
  const cells = document.querySelectorAll('.calendar .cell');
  let dayIndex = 0;

  cells.forEach(cell => {
    if (cell.querySelector('.todo.goal')) return; // 주간 목표 셀 제외

    // 기존 일정 제거
    const todos = cell.querySelectorAll('.todo, .emptyMsg');
    todos.forEach(el => el.remove());

    // 날짜 계산
    const d = new Date(monday);
    d.setDate(monday.getDate() + dayIndex);
    const key = d.toISOString().slice(0, 10);
    dayIndex++;

    const schedules = calendarList.filter(item => item.calendarDate === key);

    if (schedules.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'emptyMsg';
      empty.textContent = '일정이 없습니다.';
      cell.appendChild(empty);
    } else {
      const ul = document.createElement('ul');
      ul.className = 'todo';
      schedules.forEach(item => {
        const li = document.createElement('li');
        li.textContent = item.content;
        ul.appendChild(li);
      });
      cell.appendChild(ul);
    }
  });
}

// 주간 목표 렌더링
function renderPlannerGoal(plannerList) {
  const ul = document.querySelector('.todo.goal');
  if (!ul) return;

  ul.innerHTML = ''; // 초기화

  const start = new Date(monday);
  const end = new Date(monday);
  end.setDate(start.getDate() + 6);

  const filtered = plannerList.filter(item => {
    const s = new Date(item.startDate);
    const e = new Date(item.endDate);
    return e >= start && s <= end;
  });

  if (filtered.length === 0) {
    const empty = document.createElement('div');
    empty.className = 'emptyMsg';
    empty.textContent = '주간 목표가 없습니다.';
    ul.appendChild(empty);
    return;
  }

  filtered.forEach(item => {
    const li = document.createElement('li');
    li.textContent = item.content + (item.completed ? ' ✅' : '');
    ul.appendChild(li);
  });
}

// 최근 강의 렌더링
function renderRecentLecture(recentLecture) {
  const wrapper = document.querySelector(".recentLec");
  const titleEl = document.querySelector("#recentLecTitle");
  const progressEl = document.querySelector("#recentLecProgress");
  const bar = document.querySelector(".progressBar2");

  if (!wrapper || !titleEl || !progressEl || !bar) return;

  if (!recentLecture || !recentLecture.lectureId) {
    wrapper.style.display = "none"; // 아무것도 안 보여줌
    return;
  }

  const progress = recentLecture.progressRate ?? 0;

  wrapper.style.display = "block";
  wrapper.setAttribute("data-id", recentLecture.lectureId);
  titleEl.innerText = recentLecture.title;
  progressEl.innerText = `진도율: ${progress}%`;
  bar.style.width = `${progress}%`;
}

// 로그아웃
function logOut() {
  alert("LALA에서 로그아웃합니다.");
  fetch("/auth/logout", { method: "POST" }).then(() => {
    location.href = "/auth/login";
  }).catch(err => {
    console.error("로그아웃 실패:", err);
  });
}

// 초기 실행
document.addEventListener("DOMContentLoaded", updateCalendarUI);
