package tracker.model;

import tracker.util.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Epic extends Task {
    private final Map<Integer, Subtask> epicSubtasks;

    public Epic(String title, String description, int id) {
        super(title, description, id, TaskStatus.NEW);
        epicSubtasks = new HashMap<>();
    }

    public void calculateEpicStatus() {
        int newSubtasks = 0;
        int doneSubtasks = 0;

        for (Subtask sub : epicSubtasks.values()) {
            if (sub.getStatus() == TaskStatus.NEW) {
                newSubtasks += 1;
            } else {
                doneSubtasks += 1;
            }
        }

        if (epicSubtasks.isEmpty() || newSubtasks > 0 && doneSubtasks == 0) {
            setStatus(TaskStatus.NEW);
        } else if (newSubtasks == 0 && doneSubtasks > 0) {
            setStatus(TaskStatus.DONE);
        } else {
            setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public void addSubtaskInEpic(Subtask subtask) { epicSubtasks.put(subtask.getId(), subtask); }

    public void deleteSubtaskInEpic(int subId) { epicSubtasks.remove(subId); }

    public void deleteAllEpicSubtasks() {
        if (!epicSubtasks.isEmpty()) {
            epicSubtasks.clear();
        }
    }

    public List<Subtask> getEpicSubtasks() { return new ArrayList<>(epicSubtasks.values()); }

    public void setEpicSubtasks(List<Subtask> subtasks) {
        for (Subtask sub : subtasks) {
            epicSubtasks.put(sub.getId(), sub);
        }
    }

    public void updateSubtaskInEpic(Subtask updatedSubtask) {
        Integer subId = updatedSubtask.getId();
        epicSubtasks.put(subId, updatedSubtask);
    }

    @Override
    public String toString() {
        return "tracker.model.Epic{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", epicSubtasks=" + getEpicSubtasks() +
                '}';
    }
}