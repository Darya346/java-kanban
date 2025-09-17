package model;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskImmutabilityTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void taskRemainsUnchangedWhenAddedToManager() {
        // Создаем оригинальную задачу
        Task originalTask = new Task("Original", "Original Description", Status.NEW);

        // Сохраняем оригинальные значения
        String originalName = originalTask.getName();
        String originalDescription = originalTask.getDescription();
        Status originalStatus = originalTask.getStatus();

        // Добавляем в менеджер
        Task createdTask = taskManager.createTask(originalTask);

        // Проверяем, что оригинальная задача не изменилась
        assertEquals(originalName, originalTask.getName(), "Оригинальное имя не должно измениться");
        assertEquals(originalDescription, originalTask.getDescription(), "Оригинальное описание не должно измениться");
        assertEquals(originalStatus, originalTask.getStatus(), "Оригинальный статус не должен измениться");

        // Проверяем, что созданная задача имеет те же значения
        assertEquals(originalName, createdTask.getName(), "Имя должно сохраниться");
        assertEquals(originalDescription, createdTask.getDescription(), "Описание должно сохраниться");
        assertEquals(originalStatus, createdTask.getStatus(), "Статус должен сохраниться");
    }
}