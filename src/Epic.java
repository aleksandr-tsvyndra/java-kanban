import java.util.ArrayList;

public class Epic extends Task {
    private String title;
    private String description;
    private int id;
    private TaskStatus status;
    private ArrayList<Subtask> epicSubtasks;

    public Epic(String title, String description, int id, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
        this.epicSubtasks = new ArrayList<>();
    }

    public String getTitle() { return title; }

    public void setTitle(String newTitle) { title = newTitle; }

    public String getDescription() { return description; }

    public void setDescription(String newDescription) { description = newDescription; }

    public int getId() { return id; }

    public void setId(int newId) { id = newId; }

    public TaskStatus getStatus() { return status; }

    public void setStatus(TaskStatus newStatus) { status = newStatus; }

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

    public ArrayList<Subtask> getEpicSubtasks() {
        return epicSubtasks;
    }

    public void setEpicSubtasks(ArrayList<Subtask> subtasks) {
        this.epicSubtasks = subtasks;
    }

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
        return "Epic{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", epicSubtasks=" + epicSubtasks +
                '}';
    }
}
