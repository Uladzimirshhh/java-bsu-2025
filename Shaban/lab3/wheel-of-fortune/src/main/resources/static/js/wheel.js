document.addEventListener('DOMContentLoaded', function() {
    const spinBtn = document.getElementById('spinBtn');
    const resultDiv = document.getElementById('result');
    const wheel = document.querySelector('.wheel');

    if (spinBtn) {
        spinBtn.addEventListener('click', async function() {
            try {
                spinBtn.disabled = true;
                resultDiv.innerHTML = 'Вращаем колесо...';

                // Добавляем анимацию вращения
                wheel.style.transition = 'transform 2s cubic-bezier(0.2, 0.8, 0.3, 1)';
                wheel.style.transform = 'rotate(720deg)';

                const response = await fetch('/api/wheel/spin');
                const data = await response.json();

                setTimeout(() => {
                    wheel.style.transition = 'none';
                    wheel.style.transform = 'rotate(0deg)';

                    resultDiv.innerHTML = `
                        <div class="result-card" style="border-color: ${data.color}">
                            <h3>🎌 Результат:</h3>
                            <p>${data.text}</p>
                            ${data.animeCharacter ? `<p><strong>Персонаж:</strong> ${data.animeCharacter}</p>` : ''}
                            <p><strong>Жанр:</strong> ${data.genre}</p>
                        </div>
                    `;
                    spinBtn.disabled = false;
                }, 2000);

            } catch (error) {
                console.error('Error:', error);
                resultDiv.innerHTML = '<div class="error">Ошибка! Попробуйте снова.</div>';
                spinBtn.disabled = false;
            }
        });
    }
});