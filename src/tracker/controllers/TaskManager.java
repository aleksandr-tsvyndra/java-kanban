package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.List;

public interface TaskManager {
    int addNewTask(Task newTask);

    Task updateTask(Task updatedTask);

    Task getTaskById(Integer id);

    void deleteTaskById(Integer id);

    void deleteAllTasks();

    List<Task> getAllTasks();

    int addNewEpic(Epic newEpic);

    Epic updateEpic(Epic updatedEpic);

    Epic getEpicById(Integer epicId);

    List<Epic> getAllEpics();

    List<Subtask> getAllEpicSubtasks(int epicId);

    void deleteEpicById(Integer epicId);

    void deleteAllEpics();

    Integer addNewSubtask(Subtask newSubtask, int epicId);

    Subtask updateSubtask(Subtask updatedSubtask);

    Subtask getSubtaskById(Integer subtaskId);

    List<Subtask> getAllSubtasks();

    void deleteSubtaskById(Integer id);

    void deleteAllSubtasks();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
