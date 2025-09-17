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
    void historyPreservesTaskData() {
        // Создаем задачу
        Task originalTask = new Task("Original", "Original Description", 1, Status.NEW);

        // Добавляем в историю
        historyManager.add(originalTask);

        // Изменяем оригинальную задачу
        originalTask.setName("Modified");
        originalTask.setDescription("Modified Description");
        originalTask.setStatus(Status.DONE);

        // Получаем задачу из истории
        Task historicalTask = historyManager.getHistory().get(0);

        // Проверяем, что данные в истории сохранились оригинальными
        assertEquals("Original", historicalTask.getName(), "История должна сохранить оригинальное имя");
        assertEquals("Original Description", historicalTask.getDescription(),
                "История должна сохранить оригинальное описание");
        assertEquals(Status.NEW, historicalTask.getStatus(), "История должна сохранить оригинальный статус");
        assertEquals(1, historicalTask.getId(), "История должна сохранить оригинальный ID");
    }

    @Test
    void historyContainsIndependentCopies() {
        Task task = new Task("Test", "Description", 1, Status.NEW);
        historyManager.add(task);

        // Изменяем оригинальную задачу
        task.setName("Changed");

        // Задача в истории не должна измениться
        assertNotEquals(task.getName(), historyManager.getHistory().get(0).getName(),
                "Задача в истории не должна зависеть от изменений оригинала");
    }
}