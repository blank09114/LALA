// 현재 날짜 기준으로 최근 4일 날짜 생성
function generateRecentDates(days = 4) {
    const dates = [];
    const today = new Date();
    
    for (let i = days - 1; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(today.getDate() - i);
        
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        dates.push(`${month}.${day}`);
    }
    
    return dates;
}

// 차트 생성
const ctx = document.getElementById('visitChart').getContext('2d');

const visitChart = new Chart(ctx, {
    type: 'line',
    data: {
        labels: generateRecentDates(4), // 동적으로 생성된 날짜
        datasets: [{
            label: '방문자 수',
            data: [5, 10, 15, 12], // 실제 데이터는 서버에서 가져와야 함
            fill: false,
            borderColor: '#3e95cd',
            tension: 0.2
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: {
                display: false
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                min: 0,
                max: 20
            }
        }
    }
});