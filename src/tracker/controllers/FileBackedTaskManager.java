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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public FileBackedTaskManager(File file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
    }

    private void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("id,type,name,status,description,start,duration,epic");
            for (var task : tasks.values()) {
                bufferedWriter.write(taskToString(task));
            }
            for (var epic : epicTasks.values()) {
                bufferedWriter.write(epicToString(epic));
            }
            for (var sub : subtasks.values()) {
                bufferedWriter.write(subtaskToString(sub));
            }
        } catch (IOException e) {
            String errorMessage = "Ошибка при сохранении в файл: " + e.getMessage();
            System.out.println(errorMessage);
            throw new FileManagerSaveException(errorMessage);
        }
    }

    private String taskToString(Task task) {
        if (task.getStartTime() == null) {
            return String.format("\n%d,%s,%s,%s,%s,", task.getId(), TaskType.TASK, task.getTitle(),
                    task.getStatus(), task.getDescription());
        }

        return String.format("\n%d,%s,%s,%s,%s,%s,%s,", task.getId(), TaskType.TASK, task.getTitle(),
                task.getStatus(), task.getDescription(), task.getStartTime().format(DATE_TIME_FORMATTER),
                task.getDuration().toMinutes());
    }

    private String epicToString(Epic epic) {
        return String.format("\n%d,%s,%s,%s,%s,", epic.getId(), TaskType.EPIC, epic.getTitle(),
                epic.getStatus(), epic.getDescription());
    }

    private String subtaskToString(Subtask sub) {
        if (sub.getStartTime() == null) {
            return String.format("\n%d,%s,%s,%s,%s,%d,", sub.getId(), TaskType.SUBTASK, sub.getTitle(),
                    sub.getStatus(), sub.getDescription(), sub.getEpicId());
        }

        return String.format("\n%d,%s,%s,%s,%s,%s,%s,%d,", sub.getId(), TaskType.SUBTASK, sub.getTitle(),
                sub.getStatus(), sub.getDescription(), sub.getStartTime().format(DATE_TIME_FORMATTER),
                sub.getDuration().toMinutes(), sub.getEpicId());
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
                            var task = taskFromString(line);
                            taskManager.tasks.put(task.getId(), task);
                            tasksId[i - 1] = task.getId();
                            break;
                        case TaskType.EPIC:
                            var epic = epicFromString(line);
                            taskManager.epicTasks.put(epic.getId(), epic);
                            tasksId[i - 1] = epic.getId();
                            break;
                        case TaskType.SUBTASK:
                            var sub = subtaskFromString(line);
                            taskManager.subtasks.put(sub.getId(), sub);
                            var subEpic = taskManager.epicTasks.get(sub.getEpicId());
                            subEpic.addSubtaskInEpic(sub);
                            tasksId[i - 1] = sub.getId();
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

        if (data.length == 5) {
            return new Task(data[2], data[4], Integer.parseInt(data[0]), TaskStatus.valueOf(data[3]));
        }

        return new Task(data[2], data[4], Integer.parseInt(data[0]), TaskStatus.valueOf(data[3]),
                LocalDateTime.parse(data[5], DATE_TIME_FORMATTER), Duration.ofMinutes(Integer.parseInt(data[6])));
    }

    private static Epic epicFromString(String value) {
        String[] data = value.split(",");
        return new Epic(data[2], data[4], Integer.parseInt(data[0]));
    }

    private static Subtask subtaskFromString(String value) {
        String[] data = value.split(",");

        Subtask sub;
        if (data.length == 6) {
            sub = new Subtask(data[2], data[4], Integer.parseInt(data[0]), TaskStatus.valueOf(data[3]));
            sub.setEpicId(Integer.parseInt(data[5]));
        } else {
            sub = new Subtask(data[2], data[4], Integer.parseInt(data[0]), TaskStatus.valueOf(data[3]),
                    LocalDateTime.parse(data[5], DATE_TIME_FORMATTER), Duration.ofMinutes(Integer.parseInt(data[6])));
            sub.setEpicId(Integer.parseInt(data[7]));
        }

        return sub;
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
    public int addNewTask(Task task) {
        int newId = super.addNewTask(task);
        save();
        return newId;
    }

    @Override
    public Task updateTask(Task updatedTask) {
        var task = super.updateTask(updatedTask);
        save();
        return task;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public int addNewEpic(Epic epic) {
        int newId = super.addNewEpic(epic);
        save();
        return newId;
    }

    @Override
    public Epic updateEpic(Epic updatedEpic) {
        var epic = super.updateEpic(updatedEpic);
        save();
        return epic;
    }

    @Override
    public void deleteEpicById(int epicId) {
        super.deleteEpicById(epicId);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public int addNewSubtask(Subtask sub, int epicId) {
        int newId = super.addNewSubtask(sub, epicId);
        save();
        return newId;
    }

    @Override
    public Subtask updateSubtask(Subtask updatedSub) {
        var sub = super.updateSubtask(updatedSub);
        save();
        return sub;
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}
