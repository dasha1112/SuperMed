const API_BASE = 'http://localhost:4567';

// Проверка авторизации при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    if (!localStorage.getItem('doctorLoggedIn')) {
        window.location.href = 'login.html';
    } else {
        // Загружаем данные врача
        loadDoctorData();
    }
});

// Загрузка данных врача
async function loadDoctorData() {
    try {
        const username = localStorage.getItem('doctorUsername');
        const userInfo = document.querySelector('.user-info span');
        if (userInfo) {
            userInfo.textContent = `Доктор ${username}`;
        }
    } catch (error) {
        console.error('Ошибка загрузки данных врача:', error);
    }
}

// Выход из системы
function logout() {
    if (confirm('Вы уверены, что хотите выйти?')) {
        localStorage.removeItem('doctorLoggedIn');
        localStorage.removeItem('doctorUsername');
        window.location.href = 'login.html';
    }
}

// Показать/скрыть уведомление
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        background: ${type === 'error' ? '#e74c3c' : '#2ecc71'};
        color: white;
        border-radius: 4px;
        z-index: 1001;
        box-shadow: 0 2px 5px rgba(0,0,0,0.2);
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 3000);
}