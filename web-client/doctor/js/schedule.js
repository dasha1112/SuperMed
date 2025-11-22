// Загрузка расписания врача
async function loadSchedule() {
    try {
        const username = localStorage.getItem('doctorUsername');
        const response = await fetch(`${API_BASE}/api/doctor/${username}/schedule`);

        if (!response.ok) {
            throw new Error('Ошибка загрузки расписания');
        }

        const schedule = await response.json();
        renderSchedule(schedule);
    } catch (error) {
        console.error('Ошибка загрузки расписания:', error);
        showNotification('Не удалось загрузить расписание', 'error');
    }
}

// Отображение расписания
function renderSchedule(schedule) {
    const tbody = document.querySelector('#schedule tbody');
    if (!tbody) return;

    if (schedule.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="loading">Нет запланированных приемов</td></tr>';
        return;
    }

    tbody.innerHTML = schedule.map(appointment => `
        <tr>
            <td>${formatDateTime(appointment.appointment_date, appointment.start_time)}</td>
            <td>${appointment.patient_username}</td>
            <td>${appointment.doctor_name}</td>
            <td>${getAppointmentType(appointment)}</td>
            <td>
                ${appointment.status === 'scheduled' ?
                    `<button class="complete-btn" onclick="completeAppointment(${appointment.id})">
                        Завершить
                    </button>` :
                    '<span style="color: #27ae60;">Завершено</span>'
                }
            </td>
        </tr>
    `).join('');
}

// Завершение приема
async function completeAppointment(appointmentId) {
    if (!confirm('Вы уверены, что хотите отметить прием как завершенный?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/appointment/${appointmentId}/complete`, {
            method: 'POST'
        });

        if (response.ok) {
            showNotification('Прием успешно завершен');
            loadSchedule(); // Перезагружаем расписание
        } else {
            throw new Error('Не удалось завершить прием');
        }
    } catch (error) {
        console.error('Ошибка завершения приема:', error);
        showNotification('Не удалось завершить прием', 'error');
    }
}

// Вспомогательные функции
function formatDateTime(date, time) {
    const dateObj = new Date(date + 'T' + time);
    return dateObj.toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getAppointmentType(appointment) {
    // Простая логика определения типа приема
    if (appointment.start_time && appointment.end_time) {
        const start = parseInt(appointment.start_time.split(':')[0]);
        return start < 12 ? 'Утренний прием' : 'Дневной прием';
    }
    return 'Консультация';
}