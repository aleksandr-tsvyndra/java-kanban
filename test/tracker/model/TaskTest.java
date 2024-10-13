package tracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import tracker.util.TaskStatus;

class TaskTest {
    @Test
    void shouldConsiderEqualsWithTheSameId() {
        Task taskOne = new Task("Задача 1", "Описание 1", 1, TaskStatus.NEW);
        Task taskTwo = new Task("Задача 2", "Описание 2", 1, TaskStatus.DONE);

        assertEquals(taskOne, taskTwo, "Объекты класса Task равны друг другу, если равен их id.");

        Epic epicOne = new Epic("Эпик 1", "Описание 1", 2, TaskStatus.NEW);
        Epic epicTwo = new Epic("Эпик 2", "Описание 2", 2, TaskStatus.IN_PROGRESS);

        assertEquals(epicOne, epicTwo, "Объекты класса Epic равны друг другу, если равен их id.");

        Subtask subtaskOne = new Subtask("Подзадача 1", "Описание 1", 3, TaskStatus.NEW);
        Subtask subtaskTwo = new Subtask("Подзадача 2", "Описание 2", 3, TaskStatus.DONE);

        assertEquals(subtaskOne, subtaskTwo, "Объекты класса Subtask равны друг другу, если равен их id.");
    }
}