package tracker.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tracker.model.Task;
import tracker.util.TaskStatus;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    @DisplayName("Сохранение задачи в историю")
    void shouldAddTask() {
        // Пробуем добавить Таск в историю
        historyManager.add(new Task("Таск", "Описание", 1, TaskStatus.NEW));

        // Получаем историю задач для проверки
        var history = historyManager.getHistory();

        // Смотрим, сохранился ли Таск в историю задач
        assertNotNull(history, "История пустая");
        assertEquals(1, history.size(), "В истории некорректное число задач");
    }

    @Test
    @DisplayName("История задач не должна содержать повторов")
    void shouldNotRepeatTaskWithTheSameId() {
        // Добавляем два Таска в историю
        var task1 = new Task("Таск 1", "Описание", 0, TaskStatus.NEW);
        task1.setId(1);
        historyManager.add(task1);

        var task2 = new Task("Таск 2", "Описание", 0, TaskStatus.NEW);
        task2.setId(2);
        historyManager.add(task2);

        // Получаем историю задач, чтобы посмотреть, что там 2 задачи
        var history = historyManager.getHistory();
        assertNotNull(history, "История пустая");
        assertEquals(2, history.size(), "История должна содержать 2 задачи");

        // Добавляем задачу с тем же id, что и у task1
        var taskWithId1 = new Task("Таск 1", "Описание", 0, TaskStatus.NEW);
        taskWithId1.setId(1);
        historyManager.add(taskWithId1);

        // Получаем историю задач для проверки
        var history1 = historyManager.getHistory();

        // Проверяем, чтобы в истории осталось то же количество задач, что и ранее - 2
        assertNotNull(history1, "История пустая");
        assertEquals(2, history1.size(), "В истории должно остаться 2 задачи");
    }

    @Test
    @DisplayName("Удаление первой задачи из истории просмотров")
    void shouldRemoveFirstTaskInHistory() {
        // Добавляем в историю просмотров 5 задач
        Task task;
        for (int i = 1; i < 6; i++) {
            task = new Task("Таск", "Описание", 0, TaskStatus.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        // Получаем историю задач, чтобы посмотреть, что там ровно 5 задач
        var history = historyManager.getHistory();
        assertNotNull(history, "История пустая");
        assertEquals(5, history.size(), "История должна содержать 5 задач");
        assertEquals(5, history.getFirst().getId(), "В начале истории должен быть Таск с id 5");

        // Удаляем задачу с id 5, которая находится в начале истории просмотров
        historyManager.remove(5);

        // Получаем историю задач для проверки
        var history1 = historyManager.getHistory();

        // Смотрим, удалился ли Таск с id 5 из истории задач
        assertNotNull(history1, "История пустая");
        assertEquals(4, history1.size(), "В истории должно остаться четыре задачи");
        assertNotEquals(5, history1.getFirst().getId(),
                "Таск с id 5 должен был удалиться из начала истории");
    }

    @Test
    @DisplayName("Удаление задачи из середины истории просмотров")
    void shouldRemoveMiddleTaskInHistory() {
        // Добавляем в историю просмотров 5 задач
        Task task;
        for (int i = 1; i < 6; i++) {
            task = new Task("Таск", "Описание", 0, TaskStatus.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        // Получаем историю задач, чтобы посмотреть, что там ровно 5 задач
        var history = historyManager.getHistory();
        assertNotNull(history, "История пустая");
        assertEquals(5, history.size(), "История должна содержать 5 задач");
        assertEquals(3, history.get(2).getId(), "В самой середине истории должен быть Таск с id 3");

        // Удаляем задачу с id 3, которая находится в середине истории просмотров
        historyManager.remove(3);

        // Получаем историю задач для проверки
        var history1 = historyManager.getHistory();

        // Смотрим, удалился ли Таск с id 3 из истории задач
        assertNotNull(history1, "История пустая");
        assertEquals(4, history1.size(), "В истории должно остаться четыре задачи");
        assertNotEquals(3, history1.get(2).getId(),
                "Таск с id 3 должен был удалиться из середины истории");
    }

    @Test
    @DisplayName("Удаление последней задачи из истории просмотров")
    void shouldRemoveLastTaskInHistory() {
        // Добавляем в историю просмотров 5 задач
        Task task;
        for (int i = 1; i < 6; i++) {
            task = new Task("Таск", "Описание", 0, TaskStatus.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        // Получаем историю задач, чтобы посмотреть, что там ровно 5 задач
        var history = historyManager.getHistory();
        assertNotNull(history, "История пустая");
        assertEquals(5, history.size(), "История должна содержать 5 задач");
        assertEquals(1, history.getLast().getId(), "Последним в истории должен быть Таск с id 1");

        // Удаляем задачу с id 1, которая находится в самом конце истории просмотров
        historyManager.remove(1);

        // Получаем историю задач для проверки
        var history1 = historyManager.getHistory();

        // Смотрим, удалился ли Таск с id 1 из истории задач
        assertNotNull(history1, "История пустая");
        assertEquals(4, history1.size(), "В истории должно остаться четыре задачи");
        assertEquals(2, history1.getLast().getId(), "Последним в истории должен быть Таск с id 2");
    }

    @Test
    @DisplayName("При просмотре одной и той же задачи в истории должен отобразиться лишь последний просмотр")
    void shouldAddToHistoryOnlyRecentlyViewedTask() {
        // Добавляем в историю просмотров 10 задач
        Task task;
        for (int i = 1; i < 11; i++) {
            task = new Task("Таск", "Описание", 0, TaskStatus.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        // Получаем историю задач, чтобы посмотреть изначальный порядок Тасков
        var history = historyManager.getHistory();
        assertNotNull(history, "История пустая");
        assertEquals(10, history.size(), "История должна содержать 10 задач");
        assertEquals(10, history.getFirst().getId(), "Первым в истории должен быть Таск с id 10");
        assertEquals(5, history.get(5).getId(), "Таск с id 5 должен идти в истории 6-м с начала");

        // Смотрим Таск с id 5
        var viewedTask = new Task("Таск", "Описание", 0, TaskStatus.NEW);
        viewedTask.setId(5);
        historyManager.add(viewedTask);

        // Снова получаем историю задач для проверки
        var history1 = historyManager.getHistory();

        // Проверяем, что после просмотра Таска с id 5 он стал идти первым в истории просмотров,
        // тогда как его старый просмотр из середины истории успешно удалился
        assertNotNull(history1, "История пустая");
        assertEquals(10, history1.size(), "В истории должно быть 10 задач");
        assertEquals(5, history1.getFirst().getId(), "Первым в истории должен стать Таск с id 5");
        assertNotEquals(5, history1.get(5).getId(),
                "Таск с id 5 должен удалиться с прежней позиции в середине истории");
    }

    @Test
    @DisplayName("При удалении задач история просмотров должна быть пустой")
    void shouldClearHistoryAfterDeletingAllTasks() {
        // Добавляем в историю просмотров 3 задачи
        for (int i = 1; i < 4; i++) {
            final Task task1 = new Task("Таск", "Описание", 0, TaskStatus.NEW);
            task1.setId(i);
            historyManager.add(task1);
        }

        // Получаем историю задач, чтобы посмотреть, что она не пустая
        var history = historyManager.getHistory();
        assertNotNull(history, "История пустая");
        assertEquals(3, history.size(), "История должна содержать 3 задачи");

        // Удаляем задачи из истории просмотров
        for (int i = 1; i < 4; i++) {
            historyManager.remove(i);
        }

        // Получаем историю просмотров для проверки
        var history1 = historyManager.getHistory();

        // Смотрим, что история просмотров успешно очистилась
        assertTrue(history1.isEmpty(), "История не очистилась");
    }
}