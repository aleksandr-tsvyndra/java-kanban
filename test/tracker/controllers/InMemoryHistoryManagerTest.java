package tracker.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tracker.model.Task;
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
    void shouldAddTask() {
        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "История пустая.");
    }

    @Test
    void shouldNotAddMoreThantTenTasks() {
        for (int i = 1; i < 12; i++) {
            historyManager.add(task);
            task.setId(i);
        }
        final List<Task> history = historyManager.getHistory();
        final Task actualFirstTask = history.getFirst();

        assertNotNull(history, "История пустая.");
        assertTrue(history.size() < 11, "В истории не может быть больше 10 элементов.");
        assertTrue(1 != actualFirstTask.getId(),
                "Если список исчерпан, из него нужно удалить самый старый элемент");
    }
}