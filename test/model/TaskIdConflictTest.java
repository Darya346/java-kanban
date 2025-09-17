package model;

import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskIdConflictTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void noConflictBetweenGeneratedAndManualIds() {
        // Создаем задачу с автоматически сгенерированным ID
        Task autoTask = taskManager.createTask(new Task("Auto", "Description", Status.NEW));

        // Создаем задачу вручную с таким же ID
        Task manualTask = new Task("Manual", "Description", autoTask.getId(), Status.NEW);

        // Пытаемся обновить задачу с существующим ID
        taskManager.updateTask(manualTask);

        // Проверяем, что задача обновилась, а не создалась новая
        assertEquals(1, taskManager.getAllTasks().size(), "Должна остаться одна задача");
        assertEquals("Manual", taskManager.getTaskById(autoTask.getId()).getName(),
                "Задача должна быть обновлена, а не создана новая");
    }

    @Test
    void tasksWithDifferentIdsCoexist() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Description", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Description", Status.NEW));

        assertNotEquals(task1.getId(), task2.getId(), "Задачи должны иметь разные ID");
        assertEquals(2, taskManager.getAllTasks().size(), "Обе задачи должны существовать");
    }
}