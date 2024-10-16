package tracker.model;

import tracker.util.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Subtask> epicSubtasks;

    public Epic(String title, String description, int id) {
        super(title, description, id, TaskStatus.NEW);
        epicSubtasks = new ArrayList<>();
    }

    public void calculateEpicStatus() {
        int newSubtasks = 0;
        int doneSubtasks = 0;

        for (Subtask sub : epicSubtasks) {
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

    public void deleteSubtaskInEpic(int deletedSubId) {
        for (Subtask sub : epicSubtasks) {
            if (sub.getId() == deletedSubId) {
                epicSubtasks.remove(sub);
                break;
            }
        }
    }

    public void deleteAllEpicSubtasks() {
        if (!epicSubtasks.isEmpty()) {
            epicSubtasks.clear();
        }
    }

    public List<Subtask> getEpicSubtasks() { return epicSubtasks; }

    public void setEpicSubtasks(List<Subtask> subtasks) { this.epicSubtasks = subtasks; }

    public void updateSubtaskInEpic(Subtask updatedSubtask) {
        for (Subtask sub : epicSubtasks) {
            if (sub.equals(updatedSubtask)) {
                epicSubtasks.remove(sub);
                break;
            }
        }
        epicSubtasks.add(updatedSubtask);
    }

    @Override
    public String toString() {
        return "tracker.model.Epic{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", epicSubtasks=" + epicSubtasks +
                '}';
    }
}
