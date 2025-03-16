package tracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tracker.util.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

class TaskTest {
    @Test
    @DisplayName("Сравнение объектов по Id")
    void shouldConsiderEqualsWithTheSameId() {
        Task taskOne = new Task("Задача 1", "Описание 1", 1, TaskStatus.NEW);
        Task taskTwo = new Task("Задача 2", "Описание 2", 1, TaskStatus.DONE);

        assertEquals(taskOne, taskTwo, "Объекты класса Task равны друг другу, если равен их id.");

        Epic epicOne = new Epic("Эпик 1", "Описание 1", 2);
        Epic epicTwo = new Epic("Эпик 2", "Описание 2", 2);

        assertEquals(epicOne, epicTwo, "Объекты класса Epic равны друг другу, если равен их id.");

        Subtask subtaskOne = new Subtask("Подзадача 1", "Описание 1", 3, TaskStatus.NEW,
                LocalDateTime.of(2025, 5, 18, 14, 20), Duration.ofMinutes(5));
        Subtask subtaskTwo = new Subtask("Подзадача 2", "Описание 2", 3, TaskStatus.DONE,
                LocalDateTime.of(2025, 6, 19, 15, 25), Duration.ofMinutes(15));

        assertEquals(subtaskOne, subtaskTwo, "Объекты класса Subtask равны друг другу, если равен их id.");
    }
}