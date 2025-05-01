package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.List;

public interface TaskManager {
    int addNewTask(Task newTask);

    Task updateTask(Task updatedTask);

    Task getTaskById(int id);

    void deleteTaskById(int id);

    void deleteAllTasks();

    List<Task> getAllTasks();

    int addNewEpic(Epic newEpic);

    Epic updateEpic(Epic updatedEpic);

    Epic getEpicById(int epicId);

    List<Epic> getAllEpics();

    List<Subtask> getAllEpicSubtasks(int epicId);

    void deleteEpicById(int epicId);

    void deleteAllEpics();

    int addNewSubtask(Subtask newSubtask, int epicId);

    Subtask updateSubtask(Subtask updatedSubtask);

    Subtask getSubtaskById(int subtaskId);

    List<Subtask> getAllSubtasks();

    void deleteSubtaskById(int id);

    void deleteAllSubtasks();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
