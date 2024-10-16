package tracker.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import tracker.controllers.TaskManager;

class ManagersTest {
    @Test
    @DisplayName("Проверяем, что возвращается готовый к работе экземпляр менеджеров")
    void shouldReturnInitializedAndWorkableTaskManager() {
        final TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "Менеджер не найден.");
        assertNotNull(taskManager.getHistory(), "История просмотров задач не найдена.");
    }
}