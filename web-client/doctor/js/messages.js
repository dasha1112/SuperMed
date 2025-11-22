let currentConversation = null;

// Загрузка диалогов врача
async function loadConversations() {
    try {
        const username = localStorage.getItem('doctorUsername');
        const response = await fetch(`${API_BASE}/api/doctor/${username}/conversations`);
        
        if (!response.ok) {
            throw new Error('Ошибка загрузки диалогов');
        }
        
        const conversations = await response.json();
        renderConversations(conversations);
    } catch (error) {
        console.error('Ошибка загрузки диалогов:', error);
        showNotification('Не удалось загрузить диалоги', 'error');
    }
}

// Отображение списка диалогов
function renderConversations(conversations) {
    const conversationsList = document.querySelector('.conversations-list');
    if (!conversationsList) return;
    
    if (conversations.length === 0) {
        conversationsList.innerHTML = '<div class="loading">Нет активных диалогов</div>';
        return;
    }
    
    conversationsList.innerHTML = conversations.map(conv => `
        <div class="conversation" data-patient="${conv.participant}" onclick="selectConversation('${conv.participant}')">
            <div class="patient-name">${conv.participant}</div>
            <div class="last-message">Последнее сообщение: ${formatDateTime(conv.lastMessageTime)}</div>
        </div>
    `).join('');
}

// Выбор диалога
async function selectConversation(patientUsername) {
    currentConversation = patientUsername;

    // Обновляем активный класс
    document.querySelectorAll('.conversation').forEach(conv => {
        conv.classList.remove('active');
    });
    const selectedConv = document.querySelector(`.conversation[data-patient="${patientUsername}"]`);
    if (selectedConv) {
        selectedConv.classList.add('active');
    }

    // Обновляем заголовок чата
    const chatHeader = document.querySelector('.chat-header');
    if (chatHeader) {
        chatHeader.innerHTML = `<div class="patient-name">${patientUsername}</div>`;
    }

    // Показываем область чата
    document.querySelector('.chat-area').style.display = 'flex';

    // Загружаем сообщения
    await loadMessages(patientUsername);
}

// Загрузка сообщений диалога
async function loadMessages(patientUsername) {
    try {
        const doctorUsername = localStorage.getItem('doctorUsername');
        const response = await fetch(`${API_BASE}/api/messages/conversation?doctor=${doctorUsername}&patient=${patientUsername}`);

        if (!response.ok) {
            throw new Error('Ошибка загрузки сообщений');
        }

        const messages = await response.json();
        renderMessages(messages);
    } catch (error) {
        console.error('Ошибка загрузки сообщений:', error);
        showNotification('Не удалось загрузить сообщения', 'error');
    }
}

// Отображение сообщений
function renderMessages(messages) {
    const messagesList = document.querySelector('.messages-list');
    if (!messagesList) return;

    if (messages.length === 0) {
        messagesList.innerHTML = '<div class="loading">Нет сообщений</div>';
        return;
    }

    const doctorUsername = localStorage.getItem('doctorUsername');
    messagesList.innerHTML = messages.map(msg => `
        <div class="message ${msg.senderUsername === doctorUsername ? 'doctor' : 'patient'}">
            <div class="message-text">${msg.messageText}</div>
            <div class="message-time">${formatDateTime(msg.timestamp)}</div>
        </div>
    `).join('');

    // Прокручиваем к последнему сообщению
    messagesList.scrollTop = messagesList.scrollHeight;
}

// Отправка сообщения
async function sendMessage() {
    if (!currentConversation) {
        showNotification('Выберите диалог для отправки сообщения', 'error');
        return;
    }

    const input = document.querySelector('.message-input input');
    const messageText = input.value.trim();

    if (!messageText) {
        showNotification('Введите текст сообщения', 'error');
        return;
    }

    try {
        const message = {
            sender_username: localStorage.getItem('doctorUsername'),
            receiver_username: currentConversation,
            message_text: messageText
        };

        const response = await fetch(`${API_BASE}/api/messages/send`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(message)
        });

        if (response.ok) {
            input.value = '';
            await loadMessages(currentConversation); // Перезагружаем сообщения
        } else {
            throw new Error('Не удалось отправить сообщение');
        }
    } catch (error) {
        console.error('Ошибка отправки сообщения:', error);
        showNotification('Не удалось отправить сообщение', 'error');
    }
}

// Форматирование даты
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '';

    const date = new Date(dateTimeString);
    return date.toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Обработка отправки сообщения по Enter
document.addEventListener('DOMContentLoaded', function() {
    const messageInput = document.querySelector('.message-input input');
    if (messageInput) {
        messageInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    }

    const sendButton = document.querySelector('.send-btn');
    if (sendButton) {
        sendButton.addEventListener('click', sendMessage);
    }
});