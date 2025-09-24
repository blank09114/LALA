$(document).ready(function() {

  // 비밀번호 일치 여부 체크 함수 추가
  function checkPasswordMatch() {
      const password = $('#password').val();
      const confirmPassword = $('#passwordConfirmation').val();
      const messageElement = $('#passwordMatchMessage');

      if (confirmPassword === '') {
          messageElement.text('').removeClass('success error');
          return false;
      }

      if (password === confirmPassword) {
          messageElement.text('비밀번호가 일치합니다.').removeClass('error').addClass('success');
          return true;
      } else {
          messageElement.text('비밀번호가 일치하지 않습니다.').removeClass('success').addClass('error');
          return false;
      }
  }

  function validateForm() {
      let isValid = true;

      // 이메일 중복 검사
      if ($('#emailCheckMessage').hasClass('error')) {
          isValid = false;
      }

      // 이메일 인증 확인 (필수인 경우)
      if (!$('#verificationMessage').hasClass('success')) {
          isValid = false;
      }

      // 비밀번호 일치 검사
      const passwordConfirm = $('#passwordConfirmation').val();
      if (passwordConfirm !== '' && !checkPasswordMatch()) {
          isValid = false;
      }

      // 모든 필수 필드가 채워져 있는지 확인
      const requiredFields = ['#email', '#password', '#passwordConfirmation', '#userNickname'];
      requiredFields.forEach(field => {
          if ($(field).val().trim() === '') {
              isValid = false;
          }
      });

      $('#signup-button').prop('disabled', !isValid);
      return isValid;
  }

  // 비밀번호 입력 시 실시간 검사 추가
  $('#password, #passwordConfirmation').on('input', function() {
      checkPasswordMatch();
      validateForm();
  });

  // 비밀번호 확인 필드 포커스 아웃 시에도 검사
  $('#passwordConfirmation').on('blur', function() {
      checkPasswordMatch();
      validateForm();
  });

  // 이메일 중복 검사
  // 1. 이메일 중복되면 인증 메일 보내기 버튼은 숨김
  // 2. 이메일 중복이 없다면 인증 메일 보내기 버튼 활성화 됨
  $('#check-email-button').on('click', function() {
      let email = $('#email').val().trim();

      if (email === "") {
          $('#emailCheckMessage').text("이메일을 입력하세요.").removeClass('success').addClass('error');
          $('#email').focus();  // 이메일 input으로 포커스 이동
          return; // 함수 종료
      }

      $.ajax({
          type: 'POST',
          url: '/auth/check-email',
          contentType: 'application/json',
          data: JSON.stringify({ mail: email }),
          success: function(response) {
              if (response) {
                  $('#emailCheckMessage').text("아이디가 이미 존재합니다.").removeClass('success').addClass('error');
                  $('#send-code-button').hide();
              } else {
                  $('#emailCheckMessage').text("사용 가능한 아이디입니다.").removeClass('error').addClass('success');
                  $('#send-code-button').show();
              }
              validateForm();
          },
          error: function(error) {
              $('#emailCheckMessage').text('이메일 확인 중 오류가 발생했습니다. 다시 시도해주세요.').removeClass('success').addClass('error');
              $('#send-code-button').hide();
              validateForm();
          }
      });
  });

  // 인증 메일
  $('#send-code-button').on('click', function() {
      let email = $('#email').val();

      $.ajax({
          type: 'POST',
          url: '/auth/mail',
          contentType: 'application/json',
          data: JSON.stringify({ mail: email }),
          success: function(response) {
              $('#verifyCodeSection').show();
              alert('인증 메일이 발송되었습니다. 인증 번호를 확인해주세요.');
          },
          error: function(error) {
              alert('메일 발송에 실패했습니다. 다시 시도해주세요.');
          }
      });
  });

  $('#verify-code-button').on('click', function() {
      let email = $('#email').val();
      let code = parseInt($('#verificationCode').val()); // 숫자로 변환

      $.ajax({
          type: 'POST',
          url: '/auth/verify-code',
          contentType: 'application/json',
          data: JSON.stringify({
              emailAddress: email,        // 필드명 변경
              verificationCode: code      // 필드명 변경
          }),
          success: function(response) {
              if (response === 'Verified') {
                  $('#verificationMessage').text('인증 성공').removeClass('error').addClass('success');
              } else {
                  $('#verificationMessage').text('인증 실패. 올바른 코드를 입력하세요.').removeClass('success').addClass('error');
              }
              validateForm(); // 인증 결과에 따라 폼 유효성 재검사
          },
          error: function(xhr, status, error) {
              console.log('Status:', status);
              console.log('Error:', error);
              console.log('Response:', xhr.responseText);
              $('#verificationMessage').text('인증 실패. 다시 시도해주세요.').removeClass('success').addClass('error');
              validateForm();
          }
      });
  });

  // 인증 코드 발송 버튼 --> 초기 상태에서는 비활성화
  $('#send-code-button').hide();

  // 인증 코드 입력 란 숨기기
  $('#verifyCodeSection').hide();

  // 초기에 회원가입 버튼 비활성화
  $('#signup-button').prop('disabled', true);
});

// 1) 비밀번호 보이기/숨기기 토글
document.querySelectorAll('.eye-btn').forEach(btn => {
  const icon  = btn.querySelector('img');
  const input = btn.closest('.password-wrapper').querySelector('input');
  btn.addEventListener('click', () => {
    const show = input.type === 'password';
    input.type  = show ? 'text' : 'password';
    icon.src    = show ? '/img/eye.png' : '/img/eye.png';
  });
});

// 2) 실시간 비밀번호 요구사항 검증
const pwInput = document.getElementById('password');
const rule1   = document.getElementById('rule-1');
const rule2   = document.getElementById('rule-2');

if (pwInput) {
  pwInput.addEventListener('input', () => {
    const v = pwInput.value;
    // 길이 체크 (공백 제외)
    const lengthOK = v.replace(/\s/g, '').length >= 8
                  && v.replace(/\s/g, '').length <= 32;
    if (rule2) rule2.style.opacity = lengthOK ? '1' : '0.3';

    // 종류 체크
    const hasLetter  = /[A-Za-z]/.test(v);
    const hasDigit   = /\d/.test(v);
    const hasSpecial = /[^A-Za-z0-9]/.test(v);
    const countKinds = [hasLetter, hasDigit, hasSpecial].filter(x => x).length;
    if (rule1) rule1.style.opacity = countKinds >= 2 ? '1' : '0.3';
  });
}

// 관심 카테고리 설정
// 폼 로드 후에 아래 코드 삽입
const dropdownToggle = document.querySelector('.dropdown-toggle');
const dropdownMenu   = document.querySelector('.dropdown-menu');
const hiddenInput    = document.getElementById('category');

if (dropdownToggle && dropdownMenu) {
  // 토글 열기/닫기
  dropdownToggle.addEventListener('click', () => {
    dropdownMenu.classList.toggle('open');
  });

  // 카테고리 선택
  dropdownMenu.querySelectorAll('li').forEach(li => {
    li.addEventListener('click', () => {
      const val = li.dataset.value;
      if (hiddenInput) hiddenInput.value = val;
      dropdownToggle.innerHTML = val + ' <span class="arrow">▼</span>';
      dropdownMenu.classList.remove('open');
    });
  });

  // 폼 외부 클릭 시 닫기
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.interest-group')) {
      dropdownMenu.classList.remove('open');
    }
  });
}