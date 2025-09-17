package manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefaultReturnsInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager, "Менеджер не должен быть null");
        assertTrue(manager instanceof InMemoryTaskManager, "Должен возвращаться InMemoryTaskManager");

        // Проверяем, что менеджер готов к работе
        assertDoesNotThrow(() -> manager.getAllTasks());
        assertDoesNotThrow(() -> manager.getAllEpics());
        assertDoesNotThrow(() -> manager.getAllSubtasks());
    }

    @Test
    void getDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "History manager не должен быть null");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "Должен возвращаться InMemoryHistoryManager");

        // Проверяем, что history manager готов к работе
        assertDoesNotThrow(() -> historyManager.getHistory());
    }
}