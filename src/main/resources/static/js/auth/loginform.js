    const pwInput   = document.getElementById('login-pw');
    const toggleBtn = document.querySelector('.eye-btn');
    const eyeIcon   = toggleBtn.querySelector('img');

    toggleBtn.addEventListener('click', () => {
      const isHidden = pwInput.type === 'password';
      // 1) 입력 타입 토글
      pwInput.type = isHidden ? 'text' : 'password';
      // 2) 아이콘 이미지 토글 (eye.png ↔ eye-off.png)
      eyeIcon.src  = isHidden ? '/img/eye.png' : '/img/eye.png';
    });