// 제목만 렌더: 자료 이름만 넣는다
export function renderLectureTop(data) {
    const titleElem = document.querySelector(".sectionHeader");
    if (!titleElem) return;

    const title = data.name || "제목 없음";
    titleElem.textContent = title;
}

// @문자숫자:: 패턴을 제거하는 함수
function removeUserIdPattern(text) {
    if (!text) return text;
    console.log('원본 텍스트:', text);
    const cleaned = text.replace(/@[A-Z]\d+::/g, '').trim();
    console.log('정리된 텍스트:', cleaned);
    return cleaned;
}


// 댓글 렌더링
export function createQnaItemHTML(q) {
    const nickname = q.nickname || "익명";
    const profilename = q.profilename || "/images/defaultProfile.png";
    const questionDate = new Date(q.questionDate).toLocaleDateString('ko-KR');
    // @L숫자:: 패턴 제거
    const questionContent = removeUserIdPattern(q.questionContent) || "";

    const hasAnswer = q.answerContent && q.answerContent.trim() !== "";
    const answerDate = q.answerDate ? new Date(q.answerDate).toLocaleDateString('ko-KR') : "";
    // 답변 내용에서도 @L숫자:: 패턴 제거
    const answerContent = removeUserIdPattern(q.answerContent) || "";

    const answerHTML = hasAnswer ? `
        <div class="qnaItem answer">
            <div class="qnaItemHeader">
                <img class="profile answerProf" src="${profilename}">
                <p class="info"><span>${nickname}</span> · ${answerDate}</p>
            </div>
            <p class="qnaItemContent">${answerContent}</p>
        </div>` : "";

    return `
        <div class="qnaItem">
            <div class="qnaItemHeader">
                <img class="profile" src="${profilename}">
                <p class="info">${nickname} · ${questionDate}</p>
            </div>
            <p class="qnaItemContent">${questionContent}</p>
            <div class="answerList">${answerHTML}</div>
        </div>`;
}

// 댓글 리스트 렌더링
export function fetchAndRenderQnaList(materialId, qnaListSelector = ".qnaList", countSelector = "#qnaCount") {
    fetch(`/api/lms/myRecentLecture/chapters/materials/Detail/qna/${materialId}`)
        .then(res => res.json())
        .then(data => {
            const qnaListElem = document.querySelector(qnaListSelector);
            const qnaCountElem = document.querySelector(countSelector);
            if (!Array.isArray(data)) return;

            qnaListElem.innerHTML = "";
            qnaCountElem.textContent = data.length;

            data.forEach(q => {
                const qnaHTML = createQnaItemHTML(q);
                qnaListElem.insertAdjacentHTML("beforeend", qnaHTML);
            });
        })
        .catch(err => console.error("댓글 로딩 실패", err));
}

// 댓글 등록
export function submitQuestion({ inputSelector, materialId, onSuccess }) {
    const input = document.querySelector(inputSelector);
    const content = input.value.trim();
    if (!content) return;

    const contentId = "LCON" + materialId.substring(4);

    fetch(`/api/lms/myRecentLecture/chapters/materials/questionDetail/regComment`, {
        method: 'POST',
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            lectureContentId: contentId,
            questionContent: content
        })
    })
        .then(res => {
            if (!res.ok) throw new Error("댓글 등록 실패");
            return res.json();
        })
        .then(() => {
            input.value = '';
            if (typeof onSuccess === "function") onSuccess();
        })
        .catch(err => {
            console.error(err);
            alert("댓글 등록 중 오류 발생");
        });
}

// 우측 메뉴 렌더링
export async function renderLmsMenu() {
    const partList = document.querySelector(".partList");

    try {
        // 강의 정보 먼저 가져오기
        const lectureRes = await fetch('/api/lms/myRecentLecture');
        const lectureData = await lectureRes.json();
        const lectureId = lectureData.lectureId;

        const fullTitle = lectureData.lectureTitle || "강의 제목 없음";

        // 메뉴 쪽 제목만 렌더링 (메인은 건드리지 않음!!)
        const menuLecTitle = document.querySelector(".menu .lecTitle");
        if (menuLecTitle) menuLecTitle.textContent = truncate(fullTitle, 10);

        // 목차 가져오기
        const chaptersRes = await fetch('/api/lms/myRecentLecture/chapters');
        const chapters = await chaptersRes.json();

        const progressText = document.querySelector(".progressText");
        const progressBar = document.querySelector(".progressBar");
        if (progressText && progressBar) {
            progressText.textContent = `진도율(${progress.progressPercentage}%)`;
            progressBar.style.width = `${progress.progressPercentage}%`;
        }

        // 초기화
        partList.innerHTML = "";

        // 목차 및 자료 렌더링
        for (let i = 0; i < chapters.length; i++) {
            const chapter = chapters[i];
            const matRes = await fetch(`/api/lms/myRecentLecture/chapters/${chapter.chapterId}/materials`);
            const matData = await matRes.json();
            const materials = mergeMaterials(matData);  // ← 통합 + 타입 부여

            const part = document.createElement("div");
            part.className = "part";

            const chapterName = truncate(chapter.name, 10);
            part.innerHTML = `
                <button class="partHeader" onclick="toggleClassList(this)">
                    <p class="partHeaderText">${i + 1}. ${chapterName}</p>
                    <p class="partHeaderText">${materials.length}차시</p>
                </button>
                <div class="classList" style="display: none;"></div>
            `;

            const classList = part.querySelector(".classList");

            materials.forEach(m => {
                const icon = m.type === 'video' ? 'video'
                            : m.type === 'quiz' ? 'quiz'
                            : m.type === 'file' ? 'file'
                            : 'watch';

                const rawTitle = m.name || m.question || m.fileName || "제목 없음";
                const title = truncate(rawTitle, 15);

                const btn = document.createElement("button");
                btn.className = "class";
                btn.innerHTML = `
                    <div class="classLeft">
                        <img class="icon" src="/img/lms/${icon}.png">
                        <p class="partText">${title}</p>
                    </div>
                `;

                btn.addEventListener("click", () => {
                    location.href = `/lms/material/${m.matId}`;
                });
                btn.addEventListener("click", () => {
                    let url = "#";
                    if (m.type === 'video') url = `/lms/material/video/${m.matId}`;
                    else if (m.type === 'quiz') url = `/lms/material/question/${m.matId}`;
                    else if (m.type === 'file') url = `/lms/material/file/${m.matId}`;
                    location.href = url;
                });

                classList.appendChild(btn);
            });

            partList.appendChild(part);
        }

    } catch (err) {
        console.error("📛 우측 메뉴 렌더링 실패:", err);
    }
}

// 문자열 자르기
function truncate(text, maxLength) {
    return (text && text.length > maxLength)
        ? text.slice(0, maxLength) + "…"
        : text;
}

// 자료 통합: video, quiz, file 각각 타입 부여 후 병합
function mergeMaterials(matData) {
    const videoItems = (matData.video || []).map(m => ({ ...m, type: 'video' }));
    const quizItems = (matData.question || []).map(m => ({ ...m, type: 'quiz' }));
    const fileItems = (matData.addi || []).map(m => ({ ...m, type: 'file' }));

    return [...videoItems, ...quizItems, ...fileItems];
}