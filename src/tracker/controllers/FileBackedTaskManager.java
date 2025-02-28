package tracker.controllers;

import tracker.util.TaskType;

import tracker.exceptions.FileManagerSaveException;
import java.io.IOException;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
    }

    private void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("id,type,name,status,description,epic");
            for (Task task : tasks.values()) {
                bufferedWriter.write(taskToString(task));
            }
            for (Epic epic : epicTasks.values()) {
                bufferedWriter.write(epicToString(epic));
            }
            for (Subtask subtask : subtasks.values()) {
                bufferedWriter.write(subtaskToString(subtask));
            }
        } catch (IOException e) {
            String errorMessage = "Ошибка при сохранении в файл: " + e.getMessage();
            System.out.println(errorMessage);
            throw new FileManagerSaveException(errorMessage);
        }
    }

    private String taskToString(Task task) {
        return String.format("\n%d,%s,%s,%s,%s,", task.getId(), TaskType.TASK, task.getTitle(), task.getStatus(),
                task.getDescription());
    }

    private String epicToString(Epic epic) {
        return String.format("\n%d,%s,%s,%s,%s,", epic.getId(), TaskType.EPIC, epic.getTitle(), epic.getStatus(),
                epic.getDescription());
    }

    private String subtaskToString(Subtask subtask) {
        return String.format("\n%d,%s,%s,%s,%s,%d", subtask.getId(), TaskType.SUBTASK, subtask.getTitle(),
                subtask.getStatus(), subtask.getDescription(), subtask.getEpicId());
    }

    @Override
    public int addNewTask(Task newTask) {
        int newId = super.addNewTask(newTask);
        save();
        return newId;
    }

    @Override
    public Task updateTask(Task updatedTask) {
        Task task = super.updateTask(updatedTask);
        save();
        return task;
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public int addNewEpic(Epic newEpic) {
        int newId = super.addNewEpic(newEpic);
        save();
        return newId;
    }

    @Override
    public Epic updateEpic(Epic updatedEpic) {
        Epic epic = super.updateEpic(updatedEpic);
        save();
        return epic;
    }

    @Override
    public void deleteEpicById(Integer epicId) {
        super.deleteEpicById(epicId);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Integer addNewSubtask(Subtask newSubtask, int epicId) {
        Integer newId = super.addNewSubtask(newSubtask, epicId);
        save();
        return newId;
    }

    @Override
    public Subtask updateSubtask(Subtask updatedSubtask) {
        Subtask subtask = super.updateSubtask(updatedSubtask);
        save();
        return subtask;
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}
