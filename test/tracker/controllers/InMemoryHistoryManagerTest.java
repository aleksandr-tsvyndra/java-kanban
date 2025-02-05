package tracker.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tracker.model.Task;
import tracker.util.Managers;
import tracker.util.TaskStatus;

import java.util.List;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task;

    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("Задача 1", "Описание 1", 0, TaskStatus.NEW);
    }

    @Test
    @DisplayName("Сохранение задачи в историю")
    void shouldAddTask() {
        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "История пустая.");
    }

    @Test
    @DisplayName("История просмотров не должна содержать повторов")
    void shouldNotRepeatTaskWithTheSameId() {
        final Task task1 = new Task("Задача 1", "Описание 1", 0, TaskStatus.NEW);
        task1.setId(1);
        historyManager.add(task1);

        final Task task2 = new Task("Задача 2", "Описание 2", 0, TaskStatus.NEW);
        task2.setId(2);
        historyManager.add(task2);

        // добавляем задачу с тем же id, что и у task1
        final Task taskWithTheSameId = new Task("Задача 1", "Описание 1", 0, TaskStatus.NEW);
        taskWithTheSameId.setId(1);
        historyManager.add(taskWithTheSameId);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая.");
        assertEquals(2, history.size(), "В истории должно быть две задачи.");
    }

    @Test
    @DisplayName("Метод remove() должен удалять задачу из истории просмотров по id")
    void shouldRemoveTaskInHistoryById() {
        // добавляем в историю просмотров 4 задачи
        for (int i = 1; i < 5; i++) {
            task.setId(i);
            historyManager.add(task);
        }

        // удаляем задачу с id 1
        historyManager.remove(1);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая.");
        assertEquals(3, history.size(), "В истории должно остаться три задачи.");
    }

    @Test
    @DisplayName("При просмотре одной и той же задачи в истории должен отобразиться лишь последний просмотр")
    void shouldAddToHistoryOnlyRecentlyViewedTask() {
        // добавляем в историю просмотров 10 задач
        for (int i = 1; i < 11; i++) {
            final Task task1 = new Task("Задача 1", "Описание 1", 0, TaskStatus.NEW);
            task1.setId(i);
            historyManager.add(task1);
        }

        // смотрим задачу с id 5
        final Task t = new Task("Задача 1", "Описание 1", 0, TaskStatus.NEW);
        t.setId(5);
        historyManager.add(t);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая.");
        assertEquals(10, history.size(), "В истории должно быть десять задач.");

        final Task firstTask = history.getFirst();
        assertEquals(5, firstTask.getId(), "Первой в истории просмотров должна идти задача с id 5.");

        // старая версия задачи с id 5 находилась в списке под индексом 5
        final Task oldTask = history.get(5);
        assertTrue(firstTask.getId() != oldTask.getId(), "Старая версия задачи с id 5 должна быть " +
                "удалена из истории просмотров.");
        // теперь на месте старой задачи с id 5 находится задача с id 6
        assertEquals(6, oldTask.getId(), "На месте старой версии задачи с id 5 должна быть " +
                "задача с id 6.");
    }

    @Test
    @DisplayName("При удалении задач история просмотров должна быть пустой")
    void shouldClearHistoryAfterDeletingAllTasks() {
        // добавляем в историю просмотров 3 задачи
        for (int i = 1; i < 4; i++) {
            final Task task1 = new Task("Задача 1", "Описание 1", 0, TaskStatus.NEW);
            task1.setId(i);
            historyManager.add(task1);
        }

        // удаляем задачи из истории просмотров
        for (int i = 1; i < 4; i++) {
            historyManager.remove(i);
        }

        final List<Task> history = historyManager.getHistory();

        assertEquals(0, history.size(), "История не очистилась.");
        // в переменных head и tail должен быть null

    }
}