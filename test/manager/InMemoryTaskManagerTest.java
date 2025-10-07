package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testTaskImmutability() {
        // Создаем задачу через менеджер
        Task task = taskManager.createTask(new Task("Test", "Description", Status.NEW));
        int originalId = task.getId();
        String originalName = task.getName();

        // Пытаемся изменить задачу через сеттеры
        task.setId(999);
        task.setName("Modified");
        task.setStatus(Status.DONE);

        // Получаем задачу из менеджера
        Task managedTask = taskManager.getTaskById(originalId);

        // Проверяем, что данные в менеджере не изменились
        assertEquals(originalId, managedTask.getId());
        assertEquals(originalName, managedTask.getName());
        assertEquals(Status.NEW, managedTask.getStatus());
    }

    @Test
    void testEpicImmutability() {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
        int originalId = epic.getId();

        // Пытаемся изменить эпик
        epic.setId(999);
        epic.setName("Modified");

        // Получаем эпик из менеджера
        Epic managedEpic = taskManager.getEpicById(originalId);

        // Проверяем, что данные в менеджере не изменились
        assertEquals(originalId, managedEpic.getId());
        assertEquals("Test Epic", managedEpic.getName());
    }

    @Test
    void testHistoryWithoutDuplicates() {
        Task task = taskManager.createTask(new Task("Test", "Description", Status.NEW));

        // Многократно запрашиваем одну и ту же задачу
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        // В истории должна быть только одна запись
        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    void testDataIntegrityAfterDeletion() {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Desc", Status.NEW, epic.getId()));

        int subtaskId = subtask.getId();
        int epicId = epic.getId();

        // Удаляем подзадачу
        taskManager.deleteSubtaskById(subtaskId);

        // Проверяем, что эпик не содержит удаленную подзадачу
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertFalse(updatedEpic.getSubtaskIds().contains(subtaskId));

        // Проверяем, что подзадача удалена из хранилища
        assertNull(taskManager.getSubtaskById(subtaskId));
    }

    @Test
    void testEmptyHistoryAfterTaskDeletion() {
        Task task = taskManager.createTask(new Task("Test", "Description", Status.NEW));

        // Добавляем в историю
        taskManager.getTaskById(task.getId());
        assertEquals(1, taskManager.getHistory().size());

        // Удаляем задачу
        taskManager.deleteTaskById(task.getId());

        // История должна быть пустой
        assertTrue(taskManager.getHistory().isEmpty());
    }
}