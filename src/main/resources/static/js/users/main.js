/**
 * 마이페이지 주간 캘린더 모듈 (리팩토링 버전)
 *
 * 기능:
 * - 일요일부터 토요일까지의 주간 캘린더 표시
 * - 캘린더 일정과 주간 플래너를 분리하여 표시
 * - 캘린더 일정: 각 날짜별로 표시 (초록색)
 * - 주간 플래너: 별도 영역에 전체 목록으로 표시 (파란색, 완료시 회색)
 * 
 * 개선사항:
 * - 에러 처리 강화
 * - 코드 모듈화
 * - 성능 최적화
 * - 유지보수성 향상
 */

// ==================== 상수 정의 ====================
const CALENDAR_CONFIG = {
    DAYS_KOR: ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'],
    DAYS_PER_WEEK: 7,
    TOP_ROW_DAYS: 4, // 상단 행에 표시할 요일 수 (일~수)
    RENDER_DELAY: 100 // 렌더링 지연 시간 (ms)
};

const DOM_SELECTORS = {
    WEEKDAYS_TOP: 'weekdays-top',
    WEEKDAYS_BOTTOM: 'weekdays-bottom',
    TASKS_TOP: 'tasks-top',
    TASKS_BOTTOM: 'tasks-bottom'
};

const CSS_CLASSES = {
    SCHEDULE_ITEM: 'schedule-item',
    CALENDAR_ITEM: 'calendar-item',
    PLANNER_SUMMARY: 'planner-summary',
    COMPLETED: 'completed',
    CHECK_MARK: 'check-mark',
    NO_TASK: 'no-task',
    CALENDAR_MORE: 'calendar-more'
};

// ==================== 유틸리티 클래스 ====================
class DateUtils {
    /**
     * Date 객체를 YYYY-MM-DD 형식의 문자열로 변환
     * @param {Date} date - 변환할 Date 객체
     * @returns {string} YYYY-MM-DD 형식의 날짜 문자열
     */
    static formatDate(date) {
        if (!DateUtils.isValidDate(date)) {
            console.warn('유효하지 않은 날짜:', date);
            return 'Invalid-Date';
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    /**
     * Date 객체를 표시용 요일 형식으로 변환
     * @param {Date} date - 변환할 Date 객체
     * @returns {string} "요일 (월/일)" 형식의 문자열
     */
    static formatDisplayDay(date) {
        if (!DateUtils.isValidDate(date)) {
            console.warn('유효하지 않은 날짜:', date);
            return 'Invalid Date';
        }

        const dayString = CALENDAR_CONFIG.DAYS_KOR[date.getDay()];
        const month = date.getMonth() + 1;
        const day = date.getDate();
        return `${dayString} (${month}/${day})`;
    }

    /**
     * 날짜 객체의 유효성 검증
     * @param {*} date - 검증할 날짜
     * @returns {boolean} 유효한 Date 객체인지 여부
     */
    static isValidDate(date) {
        return date instanceof Date && !isNaN(date.getTime());
    }

    /**
     * 현재 주의 날짜 배열을 반환 (일요일부터 토요일까지)
     * @returns {Date[]} 7일간의 Date 객체 배열
     */
    static getCurrentWeekDates() {
        const today = new Date();
        const dayOfWeek = today.getDay();
        const startOfWeek = new Date(today);

        // 현재 날짜에서 요일 수만큼 빼서 일요일로 이동
        startOfWeek.setDate(today.getDate() - dayOfWeek);

        // 일요일부터 토요일까지 7일간의 배열 생성
        return Array.from({ length: CALENDAR_CONFIG.DAYS_PER_WEEK }, (_, index) => {
            const date = new Date(startOfWeek);
            date.setDate(startOfWeek.getDate() + index);
            return date;
        });
    }
}

// ==================== 데이터 처리 클래스 ====================
class DataProcessor {
    /**
     * 캘린더 데이터를 날짜별로 분류하여 맵 생성
     * @param {Array} calendarData - 서버에서 받은 캘린더 데이터
     * @returns {Object} 날짜를 키로 하는 일정 배열의 맵
     */
    static buildCalendarScheduleMap(calendarData) {
        const scheduleMap = {};

        if (!Array.isArray(calendarData) || calendarData.length === 0) {
            console.info('캘린더 데이터가 없습니다.');
            return scheduleMap;
        }

        calendarData.forEach((schedule, index) => {
            try {
                if (!DataProcessor.isValidSchedule(schedule)) {
                    console.warn(`유효하지 않은 일정 데이터 (인덱스: ${index}):`, schedule);
                    return;
                }

                const dateKey = DataProcessor.extractDateKey(schedule.calendarDate);
                
                if (!scheduleMap[dateKey]) {
                    scheduleMap[dateKey] = [];
                }

                scheduleMap[dateKey].push({
                    content: schedule.content,
                    type: 'calendar'
                });
            } catch (error) {
                console.error(`일정 처리 중 오류 (인덱스: ${index}):`, error, schedule);
            }
        });

        return scheduleMap;
    }

    /**
     * 현재 주간에 해당하는 플래너만 필터링
     * @param {Array} plannerData - 전체 플래너 데이터
     * @param {Array} currentWeekDates - 현재 주간의 날짜 배열
     * @returns {Array} 현재 주간에 해당하는 플래너 배열
     */
    static filterCurrentWeekPlanners(plannerData, currentWeekDates) {
        if (!Array.isArray(plannerData) || plannerData.length === 0) {
            console.info('플래너 데이터가 없습니다.');
            return [];
        }

        if (!Array.isArray(currentWeekDates) || currentWeekDates.length !== CALENDAR_CONFIG.DAYS_PER_WEEK) {
            console.error('유효하지 않은 주간 날짜 배열:', currentWeekDates);
            return [];
        }

        const weekStart = DateUtils.formatDate(currentWeekDates[0]); // 일요일
        const weekEnd = DateUtils.formatDate(currentWeekDates[6]);   // 토요일

        return plannerData.filter((planner, index) => {
            try {
                if (!DataProcessor.isValidPlanner(planner)) {
                    console.warn(`유효하지 않은 플래너 데이터 (인덱스: ${index}):`, planner);
                    return false;
                }

                const plannerStart = DateUtils.formatDate(new Date(planner.startDate));
                const plannerEnd = DateUtils.formatDate(new Date(planner.endDate));

                // 플래너 기간과 현재 주간이 겹치는지 확인
                return plannerStart <= weekEnd && plannerEnd >= weekStart;
            } catch (error) {
                console.error(`플래너 날짜 처리 중 오류 (인덱스: ${index}):`, error, planner);
                return false;
            }
        });
    }

    /**
     * 일정 데이터의 유효성 검증
     * @param {Object} schedule - 검증할 일정 데이터
     * @returns {boolean} 유효한 일정 데이터인지 여부
     */
    static isValidSchedule(schedule) {
        return schedule && 
               typeof schedule === 'object' && 
               schedule.calendarDate && 
               schedule.content;
    }

    /**
     * 플래너 데이터의 유효성 검증
     * @param {Object} planner - 검증할 플래너 데이터
     * @returns {boolean} 유효한 플래너 데이터인지 여부
     */
    static isValidPlanner(planner) {
        return planner && 
               typeof planner === 'object' && 
               planner.startDate && 
               planner.endDate && 
               planner.content;
    }

    /**
     * 날짜 문자열에서 날짜 키 추출
     * @param {string} dateString - 날짜 문자열
     * @returns {string} 추출된 날짜 키 (YYYY-MM-DD)
     */
    static extractDateKey(dateString) {
        return dateString.split(' ')[0].split('T')[0];
    }
}

// ==================== DOM 조작 클래스 ====================
class DOMRenderer {
    /**
     * 특정 날짜의 일정을 표시하는 테이블 셀 생성
     * @param {Array} schedules - 해당 날짜의 일정 배열
     * @returns {HTMLTableCellElement} 생성된 td 요소
     */
    static createScheduleCell(schedules) {
        const cell = document.createElement('td');

        if (schedules && schedules.length > 0) {
            const list = DOMRenderer.createScheduleList(schedules);
            cell.appendChild(list);
        } else {
            cell.innerHTML = `<ul><li class="${CSS_CLASSES.NO_TASK}"></li></ul>`;
        }

        return cell;
    }

    /**
     * 일정 목록 생성
     * @param {Array} schedules - 일정 배열
     * @returns {HTMLUListElement} 생성된 ul 요소
     */
    static createScheduleList(schedules) {
        const list = document.createElement('ul');

        schedules.forEach(schedule => {
            const listItem = document.createElement('li');
            listItem.textContent = schedule.content;
            listItem.className = `${CSS_CLASSES.SCHEDULE_ITEM} ${CSS_CLASSES.CALENDAR_ITEM}`;
            list.appendChild(listItem);
        });

        return list;
    }

    /**
     * 주간 플래너 영역의 셀 생성
     * @param {Array} planners - 현재 주간의 플래너 목록
     * @param {Array} weekDates - 현재 주간의 날짜 배열
     * @returns {HTMLTableCellElement} 생성된 주간 플래너 셀
     */
    static createPlannerCell(planners, weekDates) {
        const cell = document.createElement('td');

        if (planners.length > 0) {
            const list = DOMRenderer.createPlannerList(planners);
            cell.appendChild(list);
        } else {
            cell.innerHTML = `<ul><li class="${CSS_CLASSES.NO_TASK}">이번 주 플래너 없음</li></ul>`;
        }

        DOMRenderer.addCalendarLink(cell);
        return cell;
    }

    /**
     * 플래너 목록 생성
     * @param {Array} planners - 플래너 배열
     * @returns {HTMLUListElement} 생성된 ul 요소
     */
    static createPlannerList(planners) {
        const list = document.createElement('ul');

        planners.forEach(planner => {
            const listItem = document.createElement('li');
            const completedClass = planner.completed ? CSS_CLASSES.COMPLETED : '';
            listItem.className = `${CSS_CLASSES.PLANNER_SUMMARY} ${completedClass}`;

            if (planner.completed) {
                listItem.innerHTML = `${planner.content} <span class="${CSS_CLASSES.CHECK_MARK}">✓</span>`;
            } else {
                listItem.textContent = planner.content;
            }

            list.appendChild(listItem);
        });

        return list;
    }

    /**
     * 캘린더 보기 링크를 셀에 추가
     * @param {HTMLElement} cell - 링크를 추가할 셀 요소
     */
    static addCalendarLink(cell) {
        const linkContainer = document.createElement('div');
        linkContainer.className = CSS_CLASSES.CALENDAR_MORE;
        linkContainer.innerHTML = '<a href="/mypage/calendar">🗓️ 캘린더 보기 →</a>';
        cell.appendChild(linkContainer);
    }

    /**
     * 테이블 헤더 셀 생성
     * @param {string} text - 헤더에 표시할 텍스트
     * @returns {HTMLTableHeaderCellElement} 생성된 th 요소
     */
    static createHeaderCell(text) {
        const headerCell = document.createElement('th');
        headerCell.textContent = text;
        return headerCell;
    }
}

// ==================== 메인 캘린더 클래스 ====================
class WeeklyCalendar {
    constructor() {
        this.elements = null;
        this.weekDates = null;
        this.scheduleMap = null;
        this.currentWeekPlanners = null;
    }

    /**
     * DOM 요소들을 가져와서 저장
     * @returns {boolean} 모든 요소를 성공적으로 가져왔는지 여부
     */
    initializeElements() {
        this.elements = {};

        for (const [key, id] of Object.entries(DOM_SELECTORS)) {
            const element = document.getElementById(id);
            if (!element) {
                console.error(`필수 DOM 요소를 찾을 수 없습니다: ${id}`);
                return false;
            }
            this.elements[key] = element;
        }

        return true;
    }

    /**
     * 데이터 초기화
     */
    initializeData() {
        this.weekDates = DateUtils.getCurrentWeekDates();
        this.scheduleMap = DataProcessor.buildCalendarScheduleMap(window.calendarData || []);
        this.currentWeekPlanners = DataProcessor.filterCurrentWeekPlanners(
            window.plannerData || [], 
            this.weekDates
        );

        console.log('데이터 초기화 완료:', {
            weekDates: this.weekDates.length,
            schedules: Object.keys(this.scheduleMap).length,
            planners: this.currentWeekPlanners.length
        });
    }

    /**
     * 기존 테이블 내용 초기화
     */
    clearExistingContent() {
        Object.values(this.elements).forEach(element => {
            element.innerHTML = '';
        });
    }

    /**
     * 상단 행 렌더링 (일요일~수요일)
     */
    renderTopRow() {
        for (let i = 0; i < CALENDAR_CONFIG.TOP_ROW_DAYS; i++) {
            const date = this.weekDates[i];

            // 요일 헤더 생성
            const headerCell = DOMRenderer.createHeaderCell(DateUtils.formatDisplayDay(date));
            this.elements.WEEKDAYS_TOP.appendChild(headerCell);

            // 일정 셀 생성
            const dateKey = DateUtils.formatDate(date);
            const daySchedules = this.scheduleMap[dateKey] || [];
            this.elements.TASKS_TOP.appendChild(DOMRenderer.createScheduleCell(daySchedules));
        }
    }

    /**
     * 하단 행 렌더링 (목요일~토요일 + 주간 플래너)
     */
    renderBottomRow() {
        // 하단 3일 (목~토)
        for (let i = CALENDAR_CONFIG.TOP_ROW_DAYS; i < CALENDAR_CONFIG.DAYS_PER_WEEK; i++) {
            const date = this.weekDates[i];

            // 요일 헤더 생성
            const headerCell = DOMRenderer.createHeaderCell(DateUtils.formatDisplayDay(date));
            this.elements.WEEKDAYS_BOTTOM.appendChild(headerCell);

            // 일정 셀 생성
            const dateKey = DateUtils.formatDate(date);
            const daySchedules = this.scheduleMap[dateKey] || [];
            this.elements.TASKS_BOTTOM.appendChild(DOMRenderer.createScheduleCell(daySchedules));
        }

        // 주간 플래너 헤더 및 셀 생성
        this.renderPlannerSection();
    }

    /**
     * 주간 플래너 섹션 렌더링
     */
    renderPlannerSection() {
        const startDate = this.weekDates[0];
        const endDate = this.weekDates[6];
        
        const plannerHeaderText = `주간 플래너 (${startDate.getMonth() + 1}/${startDate.getDate()}~${endDate.getMonth() + 1}/${endDate.getDate()})`;
        const plannerHeader = DOMRenderer.createHeaderCell(plannerHeaderText);
        this.elements.WEEKDAYS_BOTTOM.appendChild(plannerHeader);

        // 주간 플래너 셀 생성
        const plannerCell = DOMRenderer.createPlannerCell(this.currentWeekPlanners, this.weekDates);
        this.elements.TASKS_BOTTOM.appendChild(plannerCell);
    }

    /**
     * 주간 캘린더 전체 렌더링 (메인 진입점)
     */
    render() {
        try {
            console.log('주간 캘린더 렌더링 시작');

            // 1. DOM 요소 초기화
            if (!this.initializeElements()) {
                console.error('DOM 요소 초기화 실패');
                return false;
            }

            // 2. 데이터 초기화
            this.initializeData();

            // 3. 기존 내용 초기화
            this.clearExistingContent();

            // 4. 상단 행 렌더링
            this.renderTopRow();

            // 5. 하단 행 렌더링
            this.renderBottomRow();

            console.log('주간 캘린더 렌더링 완료');
            return true;

        } catch (error) {
            console.error('주간 캘린더 렌더링 중 오류:', error);
            return false;
        }
    }
}

// ==================== 초기화 및 전역 노출 ====================

/**
 * 캘린더 인스턴스 생성 및 초기화
 */
const initializeCalendar = () => {
    const calendar = new WeeklyCalendar();
    
    // 데이터 로딩 완료 대기 후 렌더링
    setTimeout(() => {
        const success = calendar.render();
        if (!success) {
            
        }
    }, CALENDAR_CONFIG.RENDER_DELAY);

    return calendar;
};

/**
 * DOM 로드 완료 시 초기화 실행
 */
document.addEventListener('DOMContentLoaded', () => {
    const calendar = initializeCalendar();
    
    // 전역 접근을 위해 window 객체에 할당
    window.WeeklyCalendar = calendar;
    window.DateUtils = DateUtils;
    window.DataProcessor = DataProcessor;
    window.DOMRenderer = DOMRenderer;
});


