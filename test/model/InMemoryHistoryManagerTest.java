package manager;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testAddAndGetHistory() {
        Task task = new Task("Test", "Description", Status.NEW);
        task.setId(1);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void testRemoveDuplicates() {
        Task task = new Task("Test", "Description", Status.NEW);
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task); // Дубликат
        historyManager.add(task); // Еще один дубликат

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    void testRemoveFromHistory() {
        Task task1 = new Task("Test1", "Description1", Status.NEW);
        Task task2 = new Task("Test2", "Description2", Status.NEW);
        task1.setId(1);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    void testHistoryOrderPreservation() {
        Task task1 = new Task("Test1", "Description1", Status.NEW);
        Task task2 = new Task("Test2", "Description2", Status.NEW);
        Task task3 = new Task("Test3", "Description3", Status.NEW);
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void testRemoveNonExistentTask() {
        // Удаление несуществующей задачи не должно вызывать исключений
        assertDoesNotThrow(() -> historyManager.remove(999));
    }

    @Test
    void testEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    // Новые тесты для спринта 8
    @Test
    void testRemoveFromBeginning() {
        Task task1 = createTask(1, "Task 1");
        Task task2 = createTask(2, "Task 2");
        Task task3 = createTask(3, "Task 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
    }

    @Test
    void testRemoveFromMiddle() {
        Task task1 = createTask(1, "Task 1");
        Task task2 = createTask(2, "Task 2");
        Task task3 = createTask(3, "Task 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
    }

    @Test
    void testRemoveFromEnd() {
        Task task1 = createTask(1, "Task 1");
        Task task2 = createTask(2, "Task 2");
        Task task3 = createTask(3, "Task 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task2.getId(), history.get(1).getId());
    }

    private Task createTask(int id, String name) {
        Task task = new Task(name, "Description", Status.NEW);
        task.setId(id);
        return task;
    }
}