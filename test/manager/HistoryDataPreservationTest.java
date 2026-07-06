package manager;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HistoryDataPreservationTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void historyKeepsData() {
        Task originalTask = new Task("Original", "Original Description", 1, Status.NEW);

        historyManager.add(originalTask);

        originalTask.setName("Modified");
        originalTask.setDescription("Modified Description");
        originalTask.setStatus(Status.DONE);

        Task historicalTask = historyManager.getHistory().get(0);

        assertEquals("Original", historicalTask.getName());
        assertEquals("Original Description", historicalTask.getDescription());
        assertEquals(Status.NEW, historicalTask.getStatus());
        assertEquals(1, historicalTask.getId());
    }

    @Test
    void historyHasCopies() {
        Task task = new Task("Test", "Description", 1, Status.NEW);
        historyManager.add(task);

        task.setName("Changed");

        assertNotEquals(task.getName(), historyManager.getHistory().get(0).getName());
    }
}