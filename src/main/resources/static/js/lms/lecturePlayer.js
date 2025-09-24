const videoBox = document.getElementById('videoBox');
const video = document.getElementById('video');
const playBtn = document.getElementById('playBtn');
const play = document.getElementById('play');
const timer = document.getElementById('timer');
const timeBar = document.getElementById('timeBar');
const playTimeBar = document.getElementById('playTimeBar');
const playHandle = document.getElementById('playHandle');
let isDragging = false;
let hoverTimer = null;

// 재생 버튼 활성화
videoBox.addEventListener('mouseenter', () =>
{ hoverTimer = setTimeout(() => { playBtn.style.display = 'block'; }, 500); });
videoBox.addEventListener('mouseleave', () =>
{
    clearTimeout(hoverTimer);
    playBtn.style.display = 'none';
});

// 재생
function togglePlay()
{
    if (video.paused) { video.play(); }
    else { video.pause(); }
    updatePlayText();
}

// 텍스트 동기화
function updatePlayText()
{
    if (video.paused)
    {
        playBtn.textContent = '▶';
        play.textContent = '▶';
    }
    else
    {
        playBtn.textContent = '❚❚';
        play.textContent = '❚❚';
    }
}

// 시간 포맷
function formatTime(seconds)
{
    const m = Math.floor(seconds / 60).toString().padStart(2, '0');
    const s = Math.floor(seconds % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
}

// 재생 속도
function setPlaybackRate(rate)
{
    const video = document.getElementById('video');
    video.playbackRate = rate;
}

// 재생 시간 표시
video.addEventListener('timeupdate', () =>
{ timer.textContent = `${formatTime(video.currentTime)} / ${formatTime(video.duration || 0)}`; });

// 전체 시간 표시
video.addEventListener('loadedmetadata', () =>
{ timer.textContent = `${formatTime(0)} / ${formatTime(video.duration)}`; });

// 전체 화면
function toggleFullscreen() {
    if (!document.fullscreenElement) {
        video.setAttribute('controls', 'true');
        video.requestFullscreen();
    }
    else { document.exitFullscreen(); }
}

// 전체화면 해제 시 controls 제거
document.addEventListener('fullscreenchange', () =>
{
    if (!document.fullscreenElement)
    {
        video.removeAttribute('controls');

        video.style.display = 'none';
        setTimeout(() => { video.style.display = ''; }, 0);

        updatePlayText();
    }
});

// 재생 바
video.addEventListener('timeupdate', () =>
{
    const percent = (video.currentTime / video.duration) * 100;
    playTimeBar.style.width = `${percent}%`;
    timer.textContent = `${formatTime(video.currentTime)} / ${formatTime(video.duration || 0)}`;
});

// 재생 위치 변경
timeBar.addEventListener('click', (e) =>
{
    const rect = timeBar.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const newTime = (clickX / rect.width) * video.duration;
    video.currentTime = newTime;
});

// 핸들러
playHandle.addEventListener('mousedown', (e) =>
{
    isDragging = true;
    document.body.style.userSelect = 'none';
});
document.addEventListener('mousemove', (e) =>
{
    if (!isDragging) return;

    const rect = timeBar.getBoundingClientRect();
    let offsetX = e.clientX - rect.left;

    offsetX = Math.max(0, Math.min(offsetX, rect.width));

    const percent = offsetX / rect.width;
    playTimeBar.style.width = `${percent * 100}%`;
    video.currentTime = percent * video.duration;
});
document.addEventListener('mouseup', () =>
{
    isDragging = false;
    document.body.style.userSelect = '';
});