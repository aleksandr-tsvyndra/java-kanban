package tracker.util;

import tracker.controllers.HistoryManager;
import tracker.controllers.InMemoryHistoryManager;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        HistoryManager historyManager = getDefaultHistoryManager();
        return new InMemoryTaskManager(historyManager);
    }

    private static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
