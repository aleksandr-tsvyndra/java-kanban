import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    public HashMap<Integer, Task> tasks;
    public HashMap<Integer, Epic> epicTasks;
    public HashMap<Integer, Subtask> subtasks;

    static int id = 1;

    public TaskManager() {
        tasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subtasks = new HashMap<>();
    }

    public void addNewTask(Task newTask) {
        int newId = generateNewId();
        newTask.setId(newId);
        tasks.put(newTask.getId(), newTask);
    }

    public void updateTask(Task updatedTask) {
        if (!tasks.containsKey(updatedTask.getId())) {
            System.out.println("Ошибка: задачи с таким id не существует!");
            return;
        }
        tasks.put(updatedTask.getId(), updatedTask);
    }

    public Task getTaskById(Integer id) { return tasks.get(id); }

    public void deleteTaskById(Integer id) {
        tasks.remove(id);
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public ArrayList<String> getAllTasks() {
        ArrayList<String> taskList = new ArrayList<>();
        for (Task task : tasks.values()) {
            taskList.add(task.toString());
        }
        return taskList;
    }

    public void addNewEpic(Epic newEpic) {
        int newId = generateNewId();
        newEpic.setId(newId);
        epicTasks.put(newEpic.getId(), newEpic);
    }

    public void updateEpic(Epic updatedEpic) {
        int epicId = updatedEpic.getId();
        if (!epicTasks.containsKey(epicId)) {
            System.out.println("Ошибка: эпика с таким id не существует!");
            return;
        }
        Epic epic = epicTasks.get(epicId);
        ArrayList<Subtask> subtasks = epic.getEpicSubtasks();
        updatedEpic.setEpicSubtasks(subtasks);
        updatedEpic.calculateEpicStatus();
        epicTasks.put(epicId, updatedEpic);
    }

    public Epic getEpicById(Integer epicId) {
        if (!epicTasks.containsKey(epicId)) {
            System.out.println("Ошибка: эпика с таким id не существует!");
            return null;
        }
        return epicTasks.get(epicId);
    }

    public ArrayList<String> getAllEpics() {
        ArrayList<String> epicList = new ArrayList<>();
        for (Epic epic : epicTasks.values()) {
            epicList.add(epic.toString());
        }
        return epicList;
    }

    public ArrayList<String> getAllEpicSubtasks(int epicId) {
        Epic epic = epicTasks.get(epicId);
        ArrayList<String> subtaskList = new ArrayList<>();
        for (Subtask sub : epic.getEpicSubtasks()) {
            subtaskList.add(sub.toString());
        }
        return subtaskList;
    }

    public void deleteEpicById(Integer epicId) {
        if (!epicTasks.containsKey(epicId)) {
            System.out.println("Ошибка: эпика с таким id не существует!");
            return;
        }
        Epic epic = epicTasks.get(epicId);
        ArrayList<Subtask> deletedSubs = epic.getEpicSubtasks();
        for (Subtask sub : deletedSubs) {
            subtasks.remove(sub.getId());
        }
        epicTasks.remove(epicId);
    }

    public void deleteAllEpics() {
        epicTasks.clear();
        subtasks.clear();
    }

    public void addNewSubtask(Subtask newSubtask, int epicId) {
        if (!epicTasks.containsKey(epicId)) {
            System.out.println("Ошибка: эпика с таким id не существует!");
            return;
        }
        int newId = generateNewId();
        newSubtask.setId(newId);
        subtasks.put(newSubtask.getId(), newSubtask);
        Epic epic = epicTasks.get(epicId);
        ArrayList<Subtask> epicSubtasks = epic.getEpicSubtasks();
        newSubtask.setEpicId(epicId);
        epicSubtasks.add(newSubtask);
        epic.calculateEpicStatus();
    }

    public void updateSubtask(Subtask updatedSubtask) {
        int subtaskId = updatedSubtask.getId();
        Subtask sub = subtasks.get(subtaskId);
        updatedSubtask.setEpicId(sub.getEpicId());
        subtasks.put(subtaskId, updatedSubtask);
        int epicId = updatedSubtask.getEpicId();
        Epic epic = epicTasks.get(epicId);
        epic.updateSubtaskInEpic(updatedSubtask);
        epic.calculateEpicStatus();
    }

    public Subtask getSubtaskById(Integer subtaskId) {
        if (!subtasks.containsKey(subtaskId)) {
            System.out.println("Ошибка: подзадачи с таким id не существует!");
            return null;
        }
        return subtasks.get(subtaskId);
    }

    public ArrayList<String> getAllSubtasks() {
        ArrayList<String> subtaskList = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            subtaskList.add(subtask.toString());
        }
        return subtaskList;
    }

    public void deleteSubtaskById(Integer id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("Ошибка: подзадачи с таким id не существует!");
            return;
        }
        Subtask subtask = subtasks.get(id);
        Epic epic = epicTasks.get(subtask.getEpicId());
        epic.deleteSubtaskInEpic(subtask.getId());
        epic.calculateEpicStatus();
        subtasks.remove(subtask.getId());
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epicTasks.values()) {
            epic.deleteAllEpicSubtasks();
            epic.calculateEpicStatus();
        }

    }

    private int generateNewId() { return id++; }
}
