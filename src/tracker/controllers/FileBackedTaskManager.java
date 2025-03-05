package tracker.controllers;

import tracker.util.TaskStatus;
import tracker.util.TaskType;

import tracker.exceptions.FileManagerSaveException;
import tracker.exceptions.FileManagerLoadException;
import java.io.IOException;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.List;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.Files;

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

    public static FileBackedTaskManager loadFromFile(File file) {
        try {
            FileBackedTaskManager taskManager = new FileBackedTaskManager(file, new InMemoryHistoryManager());
            List<String> lines = Files.readAllLines(file.toPath());
            if (!lines.isEmpty()) {
                int[] tasksId = new int[lines.size() - 1];
                for (int i = 1; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String[] contents = line.split(",");
                    TaskType type = TaskType.valueOf(contents[1]);
                    switch (type) {
                        case TaskType.TASK:
                            Task task = taskFromString(line);
                            taskManager.tasks.put(task.getId(), task);
                            tasksId[i - 1] = task.getId();
                            break;
                        case TaskType.EPIC:
                            Epic epic = epicFromString(line);
                            taskManager.epicTasks.put(epic.getId(), epic);
                            tasksId[i - 1] = epic.getId();
                            break;
                        case TaskType.SUBTASK:
                            Subtask subtask = subtaskFromString(line);
                            taskManager.subtasks.put(subtask.getId(), subtask);
                            Epic subEpic = taskManager.epicTasks.get(subtask.getEpicId());
                            subEpic.addSubtaskInEpic(subtask);
                            tasksId[i - 1] = subtask.getId();
                            break;
                    }
                }
                taskManager.id = getMaxId(tasksId) + 1;
            }
            return taskManager;
        } catch (IOException e) {
            String errorMessage = "Ошибка при загрузке из файла: " + e.getMessage();
            System.out.println(errorMessage);
            throw new FileManagerLoadException(errorMessage);
        }
    }

    private static Task taskFromString(String value) {
        String[] data = value.split(",");
        return new Task(data[2], data[4], Integer.parseInt(data[0]), TaskStatus.valueOf(data[3]));
    }

    private static Epic epicFromString(String value) {
        String[] data = value.split(",");
        return new Epic(data[2], data[4], Integer.parseInt(data[0]));
    }

    private static Subtask subtaskFromString(String value) {
        String[] data = value.split(",");
        Subtask subtask = new Subtask(data[2], data[4], Integer.parseInt(data[0]), TaskStatus.valueOf(data[3]));
        subtask.setEpicId(Integer.parseInt(data[5]));
        return subtask;
    }

    private static int getMaxId(int[] array) {
        int maxId = 0;
        for (int id : array) {
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId;
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
