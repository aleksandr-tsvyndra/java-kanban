package tracker.model;

import tracker.util.TaskStatus;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, int id, TaskStatus status) {
        setTitle(title);
        setDescription(description);
        setId(id);
        setStatus(status);
    }

    public int getEpicId() { return epicId; }

    public void setEpicId(int epicId) { this.epicId = epicId; }

    @Override
    public String toString() {
        return "tracker.model.Subtask{" +
                "title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", epicId=" + epicId +
                ", status=" + getStatus() +
                '}';
    }
}
