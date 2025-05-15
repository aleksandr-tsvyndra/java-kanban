package tracker.util;

import tracker.controllers.FileBackedTaskManager;
import tracker.controllers.HistoryManager;
import tracker.controllers.InMemoryHistoryManager;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;

import java.io.File;

public class Managers {
    public static TaskManager getDefault() {
        HistoryManager historyManager = getDefaultHistoryManager();
        return new InMemoryTaskManager(historyManager);
    }

    public static TaskManager getDefault(File file) {
        HistoryManager historyManager = getDefaultHistoryManager();
        return new FileBackedTaskManager(file, historyManager);
    }

    private static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
