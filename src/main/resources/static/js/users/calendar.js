// DOM Elements & Constants
const elements = {
    sideDay: document.getElementById('sideDay'), // 현재 선택한 날짜(일)를 표시하는 HTML 요소
    sideWeekday: document.getElementById('sideWeekday'), // 현재 선택한 요일을 표시하는 HTML 요소
    dailyList: document.getElementById('dailyList'), // 일간 계획을 표시하기 위한 리스트 요소
    weeklyList: document.getElementById('weeklyList'), // 주간 계획을 표시하기 위한 리스트 요소
    eventInput: document.getElementById('eventInput'), // 계획 입력을 위한 텍스트 입력 필드
    saveBtn: document.getElementById('saveEventBtn'), // 새로운 이벤트 저장 버튼
    datesContainer: document.getElementById('datesContainer') // 달력을 구성하는 날짜 셀 컨테이너
};

const config = {
    weekdays: ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'], // 요일 배열
    apiBaseUrl: '/users/mypage/calendar', // API 요청의 기본 경로
    today: new Date() // 오늘 날짜 객체
};

// State
const state = {
    currentYear: config.today.getFullYear(), // 현재 년도
    currentMonth: config.today.getMonth(), // 현재 월 (0부터 시작)
    isWeeklyMode: false, // 주간 모드 활성화 여부
    savedEvents: {}, // 저장된 일간 이벤트 객체
    weeklyEvents: {}, // 저장된 주간 이벤트 객체
    expandedDates: {} // 확장된 날짜(더보기 버튼을 누른 상태) 상태
};

// Utility Functions (유틸리티 함수)
const utils = {
    getDateKey: (year, month, day) => `${year}-${month + 1}-${day}`, // 특정 연/월/일을 문자열 키로 변환
    getWeeklyKey: (year, month, weekNumber) => `${year}-${month + 1}-week-${weekNumber}`, // 주간 이벤트의 키 생성
    getWeekNumber: (date) => {
        // 특정 날짜의 주차 계산 (달의 첫 번째 날 기준)
        const firstDay = new Date(date.getFullYear(), date.getMonth(), 1);
        return Math.ceil((date.getDate() + firstDay.getDay()) / 7);
    },
    formatDate: (date) => {
        // 날짜를 'YYYY-MM-DD' 형식으로 포맷
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0'); // 두 자리로 표시
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    },
    getSundayOfWeek: (year, month, day) => {
        // 특정 날짜의 주 일요일 날짜 계산
        const date = new Date(year, month, day);
        const sunday = new Date(date);
        sunday.setDate(date.getDate() - date.getDay());
        return utils.formatDate(sunday);
    },
    isToday: (date) => {
        // 특정 날짜가 오늘인지 판별
        const today = config.today;
        return date.getFullYear() === today.getFullYear() &&
               date.getMonth() === today.getMonth() &&
               date.getDate() === today.getDate();
    }
};

// API Module (서버와 통신하는 모듈)
const api = {
    async request(endpoint, data, method = 'POST') {
        // 서버 API에 요청을 보냄
        const response = await fetch(`${config.apiBaseUrl}${endpoint}`, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`); // 응답 에러 핸들링
        return response.json();
    },

    async fetchEvents(year, month) {
        // 특정 연/월의 이벤트를 서버로부터 가져옴
        try {
            const data = await this.request('/events', { year, month: month + 1 });
            return this.groupEventsByDate(data); // 날짜별로 그룹화하여 반환
        } catch (error) {
            console.error('이벤트 조회 실패:', error);
            return {};
        }
    },

    async fetchWeeklyEvents(year, month) {
        // 특정 연/월의 주간 이벤트를 서버로부터 가져옴
        try {
            const data = await this.request('/weekly-events', { year, month: month + 1 });
            return this.groupWeeklyEvents(data); // 주별로 그룹화하여 반환
        } catch (error) {
            console.error('주간 이벤트 조회 실패:', error);
            return {};
        }
    },

    groupEventsByDate(eventList) {
        // 이벤트를 날짜별로 그룹화
        const grouped = {};
        if (Array.isArray(eventList)) {
            eventList.forEach(event => {
                const [year, month, day] = event.calendarDate.split('-').map(Number);
                const key = utils.getDateKey(year, month - 1, day);
                if (!grouped[key]) grouped[key] = [];
                grouped[key].push({ text: event.content, content: event.content });
            });
        }
        return grouped;
    },

    groupWeeklyEvents(eventList) {
        // 이벤트를 주별로 그룹화
        const grouped = {};
        if (Array.isArray(eventList)) {
            eventList.forEach(event => {
                const startDate = new Date(event.startDate);
                const key = utils.getWeeklyKey(startDate.getFullYear(), startDate.getMonth(), utils.getWeekNumber(startDate));
                if (!grouped[key]) grouped[key] = [];
                grouped[key].push({
                    id: `${event.startDate}-${event.content}`,
                    text: event.content,
                    content: event.content,
                    startDate: event.startDate,
                    completed: Boolean(event.completed)
                });
            });
        }
        return grouped;
    },

    async saveEvent(dateKey, text, isWeekly = false) {
        // 새로운 이벤트를 저장
        try {
            const [year, month, dayOrWeek] = dateKey.split('-');
            const data = { year: +year, month: +month, eventText: text, isWeekly };
            if (isWeekly) data.weekNumber = +dayOrWeek.replace('week-', '');
            else data.day = +dayOrWeek;

            await this.request('/save-event', data);
            return true;
        } catch (error) {
            console.error('저장 실패:', error);
            alert('저장에 실패했습니다.');
            return false;
        }
    },

    async saveWeeklyEvent(sundayDate, content) {
        try {
            await this.request('/save-weekly-event', {sundayDate, content});
            return true;
        } catch (error) {
            console.error('주간 이벤트 저장 실패:', error);
            alert('주간 이벤트 저장에 실패했습니다.');
            return false;
        }
    },

    async updateEvent(year, month, day, oldContent, newContent) {
        try {
            const result = await this.request('/update-event', {year, month, day, oldContent, newContent}, 'PUT');
            return result.status === 'success';
        } catch (error) {
            console.error('업데이트 실패:', error);
            return false;
        }
    },

    async deleteEvent(year, month, day, content) {
        try {
            const result = await this.request('/delete-event', {year, month, day, content}, 'DELETE');
            return result.status === 'success';
        } catch (error) {
            console.error('삭제 실패:', error);
            return false;
        }
    },

    async updateWeeklyEvent(startDate, oldContent, newContent) {
        try {
            const result = await this.request('/update-weekly-event', {startDate, oldContent, newContent}, 'PUT');
            return result.status === 'success';
        } catch (error) {
            console.error('주간 이벤트 업데이트 실패:', error);
            return false;
        }
    },

    async deleteWeeklyEvent(startDate, content) {
        try {
            const result = await this.request('/delete-weekly-event', {startDate, content}, 'DELETE');
            return result.status === 'success';
        } catch (error) {
            console.error('주간 이벤트 삭제 실패:', error);
            return false;
        }
    },

    async updatePlannerCheck(startDate, content, completed) {
        try {
            const result = await this.request('/update-planner-check', {startDate, content, completed}, 'PUT');
            return result.status === 'success';
        } catch (error) {
            console.error('체크 상태 업데이트 실패:', error);
            return false;
        }
    }
};

// UI Module (화면 렌더링 관련 모듈)
const ui = {
    createElement(tag, className, text, styles = {}) {
        // 새로운 DOM 요소 생성 (속성과 스타일 적용)
        const el = document.createElement(tag);
        if (className) el.className = className;
        if (text) el.textContent = text;
        Object.assign(el.style, styles);
        return el;
    },

    createEventElement(eventData, index, dateKey, type = 'daily') {
        // 이벤트를 보여주는 리스트 요소 생성
        const li = this.createElement('li', '', '', { display: 'flex', alignItems: 'center', marginBottom: '5px' });

        if (type === 'weekly') {
            const checkbox = this.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.checked = eventData.completed;
            checkbox.style.marginRight = '8px';
            checkbox.onchange = () => this.handleCheckboxChange(checkbox, eventData, index, dateKey);
            li.appendChild(checkbox);
        }

        const span = this.createElement('span', '', eventData.text, { flex: '1', cursor: 'pointer' });
        if (type === 'weekly' && eventData.completed) Object.assign(span.style, { textDecoration: 'line-through', color: '#999' });
        span.onclick = () => this.editEvent(span, eventData, index, dateKey, type);
        li.appendChild(span);

        const deleteBtn = this.createElement('span', '', '❌', { color: '#e74c3c', cursor: 'pointer', marginLeft: '8px', fontSize: '12px' });
        deleteBtn.onclick = () => this.deleteEvent(eventData, index, dateKey, type);
        li.appendChild(deleteBtn);

        return li;
    },

    async handleCheckboxChange(checkbox, eventData, index, dateKey) {
        const completed = checkbox.checked;
        const success = await api.updatePlannerCheck(eventData.startDate, eventData.content, completed);

        if (success) {
            const parts = dateKey.split('-');
            const weekKey = utils.getWeeklyKey(+parts[0], +parts[1], +parts[2]);

            // ✅ 상태 업데이트
            if (state.weeklyEvents[weekKey] && state.weeklyEvents[weekKey][index]) {
                state.weeklyEvents[weekKey][index].completed = completed;
            }

            // ✅ 사이드 패널의 span 요소 즉시 업데이트
            const span = checkbox.nextSibling;
            if (span) {
                if (completed) {
                    Object.assign(span.style, {
                        textDecoration: 'line-through',
                        color: '#999'
                    });
                } else {
                    Object.assign(span.style, {
                        textDecoration: 'none',
                        color: 'inherit'
                    });
                }
            }

            // ✅ 캘린더 내 주간 계획 목록도 즉시 업데이트
            calendar.refreshWeeklyDisplay();

            // ✅ 전체 캘린더 이벤트 새로고침 (캘린더 셀 내의 이벤트들)
            calendar.regenerateEvents();

            // ✅ 메인 페이지의 주간 캘린더도 업데이트 (있는 경우)
            if (window.WeeklyCalendar && typeof window.WeeklyCalendar.render === 'function') {
                window.WeeklyCalendar.render();
            }

        } else {
            // 실패 시 체크박스 상태 되돌리기
            checkbox.checked = !completed;
            alert('체크 상태 업데이트에 실패했습니다.');
        }
    },

    async deleteEvent(eventData, index, dateKey, type) {
        if (!confirm('정말 삭제하시겠습니까?')) return;

        let success = false;
        let renderKey = dateKey;

        if (type === 'daily') {
            // 일간 이벤트 삭제
            const [year, month, day] = dateKey.split('-').map(Number);
            success = await api.deleteEvent(year, month, day, eventData.content);

            if (success) {
                // 로컬 상태에서 삭제
                if (state.savedEvents[dateKey]) {
                    state.savedEvents[dateKey].splice(index, 1);
                    if (state.savedEvents[dateKey].length === 0) {
                        delete state.savedEvents[dateKey];
                    }
                }
            }
        } else if (type === 'weekly') {
            // 주간 이벤트 삭제
            success = await api.deleteWeeklyEvent(eventData.startDate, eventData.content);

            if (success) {
                // 로컬 상태에서 삭제
                const parts = dateKey.split('-');
                const weekKey = utils.getWeeklyKey(+parts[0], +parts[1], +parts[2]);

                if (state.weeklyEvents[weekKey]) {
                    state.weeklyEvents[weekKey].splice(index, 1);
                    if (state.weeklyEvents[weekKey].length === 0) {
                        delete state.weeklyEvents[weekKey];
                    }
                }

                // 주간 이벤트의 경우 해당 주의 일요일 날짜로 렌더링 키 설정
                const startDate = new Date(eventData.startDate);
                const sunday = new Date(startDate);
                sunday.setDate(sunday.getDate() - sunday.getDay());
                renderKey = utils.getDateKey(sunday.getFullYear(), sunday.getMonth(), sunday.getDate());
            }
        }

        if (success) {
            // UI 업데이트
            calendar.render(renderKey);
            calendar.regenerateEvents();

            if (type === 'weekly') {
                calendar.refreshWeeklyDisplay();
            }

            // 메인 페이지의 주간 캘린더도 업데이트 (있는 경우)
            if (window.WeeklyCalendar && typeof window.WeeklyCalendar.render === 'function') {
                window.WeeklyCalendar.render();
            }
        } else {
            alert('삭제에 실패했습니다. 다시 시도해 주세요.');
        }
    },

    async editEvent(span, eventData, index, dateKey, type) {
        const input = this.createElement('input');
        input.type = 'text';
        input.value = span.textContent;
        input.style.flex = '1';

        const handleSave = async () => {
            const newText = input.value.trim();
            if (!newText || newText === span.textContent) {
                // ✅ 안전한 DOM 조작: parentNode 존재 확인
                if (span.parentNode && input.parentNode) {
                    span.parentNode.replaceChild(span, input);
                }
                return;
            }

            let success = false;
            let renderKey = dateKey;

            if (type === 'daily') {
                const [year, month, day] = dateKey.split('-').map(Number);
                success = await api.updateEvent(year, month, day, span.textContent, newText);
                if (success) {
                    state.savedEvents[dateKey][index].text = newText;
                    state.savedEvents[dateKey][index].content = newText;
                }
            } else {
                // 주간 계획 수정 로직
                success = await api.updateWeeklyEvent(eventData.startDate, span.textContent, newText);
                if (success) {
                    const startDate = new Date(eventData.startDate);
                    const sunday = new Date(startDate);
                    sunday.setDate(sunday.getDate() - sunday.getDay());
                    const weekKey = utils.getWeeklyKey(sunday.getFullYear(), sunday.getMonth(), utils.getWeekNumber(sunday));

                    if (state.weeklyEvents[weekKey] && state.weeklyEvents[weekKey][index]) {
                        state.weeklyEvents[weekKey][index].text = newText;
                        state.weeklyEvents[weekKey][index].content = newText;
                        renderKey = utils.getDateKey(sunday.getFullYear(), sunday.getMonth(), sunday.getDate());
                    }
                }
            }

            if (success) {
                span.textContent = newText;
                // ✅ 안전한 DOM 조작: parentNode 존재 확인
                if (span.parentNode && input.parentNode) {
                    span.parentNode.replaceChild(span, input);
                }
                calendar.render(renderKey);
                calendar.regenerateEvents();

                if (type === 'weekly') {
                    calendar.refreshWeeklyDisplay();
                }
            } else {
                alert('수정에 실패했습니다. 다시 시도해 주세요.');
                // ✅ 안전한 DOM 조작: parentNode 존재 확인
                if (span.parentNode && input.parentNode) {
                    span.parentNode.replaceChild(span, input);
                }
            }
        };

        const handleCancel = () => {
            // ✅ 안전한 DOM 조작: parentNode 존재 확인
            if (span.parentNode && input.parentNode) {
                span.parentNode.replaceChild(span, input);
            }
        };

        input.onkeydown = (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                handleSave();
            }
            if (e.key === 'Escape') {
                e.preventDefault();
                handleCancel();
            }
        };

        input.onblur = handleSave;

        // ✅ 안전한 DOM 조작: parentNode 존재 확인
        if (span.parentNode) {
            span.parentNode.replaceChild(input, span);
            input.focus();
            input.select();
        } else {
            console.warn('span의 부모 요소가 존재하지 않습니다.');
        }
    }
};

// Calendar Module (캘린더 구성 및 렌더링)
const calendar = {
    async generate(year, month) {
        // 특정 연/월의 캘린더 화면을 생성
        state.savedEvents = await api.fetchEvents(year, month);
        state.weeklyEvents = await api.fetchWeeklyEvents(year, month);

        elements.datesContainer.innerHTML = '';
        this.updateMonthActive(month);

        const startOffset = new Date(year, month, 1).getDay(); // 첫 번째 날의 요일
        const daysInMonth = new Date(year, month + 1, 0).getDate(); // 해당 월의 총 일수
        let currentDay = 1;

        for (let week = 1; week <= 6; week++) {
            const weekRow = ui.createElement('div', 'week');

            for (let d = 0; d < 7; d++) {
                const cell = ui.createElement('span');
                if (week === 1 && d < startOffset) {
                    cell.innerHTML = ''; // 공백 셀
                } else if (currentDay <= daysInMonth) {
                    this.populateCell(cell, year, month, currentDay); // 날짜 셀 채우기
                    currentDay++;
                }
                weekRow.appendChild(cell);
            }

            // 주간 체크리스트 추가
            const checkCell = ui.createElement('div', 'check-cell');
            const ul = ui.createElement('ul');
            ul.id = `weekly-plan-${week}`;
            this.renderWeeklyList(week, ul);
            checkCell.appendChild(ul);
            weekRow.appendChild(checkCell);
            elements.datesContainer.appendChild(weekRow);
        }

        document.getElementById('calendarYear').textContent = year; // 헤더에 연도 표시
    },

    populateCell(cell, year, month, day) {
        // 날짜 셀에 데이터 세팅
        const date = new Date(year, month, day);
        const dateKey = utils.getDateKey(year, month, day);

        cell.innerHTML = String(day).padStart(2, '0');
        cell.dataset.date = dateKey;

        if (utils.isToday(date)) cell.classList.add('today');

        cell.onclick = () => {
            // 날짜 클릭 이벤트 핸들링
            elements.sideDay.textContent = day;
            elements.sideWeekday.textContent = config.weekdays[date.getDay()];
            elements.eventInput.value = '';
            this.render(dateKey);
        };

        this.renderCellEvents(cell, dateKey); // 해당 날짜의 이벤트 렌더링
    },

    renderCellEvents(cell, dateKey) {
        const events = state.savedEvents[dateKey];
        if (!Array.isArray(events)) return;

        const isExpanded = state.expandedDates[dateKey];
        const showList = isExpanded ? events : events.slice(0, 3);

        showList.forEach(event => {
            const ev = ui.createElement('div', 'event', event.text);
            cell.appendChild(ev);
        });

        if (events.length > 3) {
            const btn = ui.createElement('div', 'more-btn',
                isExpanded ? "접기" : `+${events.length - 3} 더보기`);
            btn.onclick = (e) => {
                e.stopPropagation();
                state.expandedDates[dateKey] = !isExpanded;
                this.regenerateEvents();
            };
            cell.appendChild(btn);
        }
    },

    updateMonthActive(month) {
        document.querySelectorAll('.months span').forEach((s, idx) => {
            s.classList.toggle('active', idx === month);
        });
    },

    render(dateKey) {
        elements.dailyList.innerHTML = '';
        elements.weeklyList.innerHTML = '';

        // Daily events 렌더링
        (state.savedEvents[dateKey] || []).forEach((event, i) => {
            elements.dailyList.appendChild(ui.createEventElement(event, i, dateKey, 'daily'));
        });

        // Weekly events 렌더링
        const [year, month, day] = dateKey.split('-').map(Number);
        const date = new Date(year, month - 1, day);
        const weekNum = utils.getWeekNumber(date);
        const weeklyKey = utils.getWeeklyKey(year, month - 1, weekNum);

        (state.weeklyEvents[weeklyKey] || []).forEach((event, i) => {
            elements.weeklyList.appendChild(
                ui.createEventElement(event, i, `${year}-${month - 1}-${weekNum}`, 'weekly')
            );
        });
    },

renderWeeklyList(weekNumber, ul) {
    ul.innerHTML = '';
    const key = utils.getWeeklyKey(state.currentYear, state.currentMonth, weekNumber);
    const events = state.weeklyEvents[key] || [];

    const isExpanded = state.expandedDates[key];
    const visibleEvents = isExpanded ? events : events.slice(0, 3);

    visibleEvents.forEach((event) => {
        const li = ui.createElement('li', '', '', {
            display: 'flex',
            alignItems: 'center',
            marginBottom: '2px',
            fontSize: '12px',
            color: event.completed ? '#999' : '#000',
            textDecoration: event.completed ? 'line-through' : 'none'
        });

        if (event.completed) {
            const checkMark = ui.createElement('span', '', '✓', {
                marginRight: '4px',
                color: '#28a745',
                fontWeight: 'bold'
            });
            li.appendChild(checkMark);
        }

        const span = ui.createElement('span', '', event.text, {
            flex: '1',
            textDecoration: event.completed ? 'line-through' : 'none',
            color: event.completed ? '#999' : 'inherit'
        });
        li.appendChild(span);
        ul.appendChild(li);
    });

    if (events.length > 3) {
        const toggleLi = ui.createElement('li', 'more-btn', isExpanded ? '접기' : `+${events.length - 3} 더보기`);
        toggleLi.style.cursor = 'pointer';
        toggleLi.onclick = () => {
            state.expandedDates[key] = !isExpanded;
            calendar.refreshWeeklyDisplay();
        };
        ul.appendChild(toggleLi);
    }
},

    regenerateEvents() {
        document.querySelectorAll('span[data-date]').forEach(cell => {
            cell.querySelectorAll('.event, .more-btn').forEach(e => e.remove());
            this.renderCellEvents(cell, cell.dataset.date);
        });
    },

    // ✅ 주간 표시 새로고침 메서드 추가
    refreshWeeklyDisplay() {
        // 모든 주간 계획 목록 새로고침
        for (let week = 1; week <= 6; week++) {
            const ul = document.getElementById(`weekly-plan-${week}`);
            if (ul) {
                this.renderWeeklyList(week, ul);
            }
        }
    }
};

// Event Handlers (이벤트 설정)
const handlers = {
    setupNavigation() {
        // 월별 네비게이션 클릭 이벤트 설정
        document.querySelectorAll('.months span').forEach((span, index) => {
            span.onclick = async () => {
                state.currentMonth = index;
                await calendar.generate(state.currentYear, state.currentMonth);
                this.updateSidePanel(); // 사이드 패널 업데이트
            };
        });

        // 연도 변경 네비게이션 설정
        document.getElementById('prevYear').onclick = async () => {
            state.currentYear--;
            await calendar.generate(state.currentYear, state.currentMonth);
        };

        document.getElementById('nextYear').onclick = async () => {
            state.currentYear++;
            await calendar.generate(state.currentYear, state.currentMonth);
        };
    },

    setupSave() {
        // 저장 버튼 클릭 이벤트 설정
        elements.saveBtn.onclick = async () => {
            const text = elements.eventInput.value.trim();
            if (!text) return;

            const selectedDay = parseInt(elements.sideDay.textContent);
            let success = false;

            if (state.isWeeklyMode) {
                const sundayDate = utils.getSundayOfWeek(state.currentYear, state.currentMonth, selectedDay);
                success = await api.saveWeeklyEvent(sundayDate, text);
            } else {
                const dateKey = utils.getDateKey(state.currentYear, state.currentMonth, selectedDay);
                success = await api.saveEvent(dateKey, text, false);
                if (success) {
                    if (!state.savedEvents[dateKey]) state.savedEvents[dateKey] = [];
                    state.savedEvents[dateKey].push({ text, content: text });
                }
            }

            if (success) {
                elements.eventInput.value = '';
                await calendar.generate(state.currentYear, state.currentMonth);
                const dateKey = utils.getDateKey(state.currentYear, state.currentMonth, selectedDay);
                calendar.render(dateKey);
            }
        };

        // ⌨️ Enter 키로도 저장되도록 처리
        elements.eventInput.addEventListener('keydown', async (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                elements.saveBtn.click();
            }
        });
    }
,

    setupModeToggle() {
        document.querySelector('.daily p').onclick = () => {
            state.isWeeklyMode = false;
            elements.eventInput.placeholder = '오늘의 계획을 입력하세요';
        };

        document.querySelector('.daily .weekly').onclick = () => {
            state.isWeeklyMode = true;
            elements.eventInput.placeholder = '이번 주 체크리스트를 입력하세요';
        };
    },

    updateSidePanel() {
        // 사이드 패널 날짜 및 요일 업데이트
        const isCurrentMonth = state.currentYear === config.today.getFullYear() &&
            state.currentMonth === config.today.getMonth();
        if (isCurrentMonth) {
            elements.sideDay.textContent = config.today.getDate();
            elements.sideWeekday.textContent = config.weekdays[config.today.getDay()];
            calendar.render(utils.getDateKey(state.currentYear, state.currentMonth, config.today.getDate()));
        } else {
            elements.sideDay.textContent = "1";
            elements.sideWeekday.textContent = config.weekdays[new Date(state.currentYear, state.currentMonth, 1).getDay()];
            calendar.render(utils.getDateKey(state.currentYear, state.currentMonth, 1));
        }
    }
};

// Initialize (초기화)
document.addEventListener('DOMContentLoaded', async () => {
    handlers.setupNavigation(); // 네비게이션 초기화
    handlers.setupSave(); // 저장 버튼 초기화
    handlers.setupModeToggle(); // 주간/일간 모드 토글 초기화

    await calendar.generate(state.currentYear, state.currentMonth); // 초기 캘린더 생성
    handlers.updateSidePanel(); // 사이드바 초기 상태 업데이트
});