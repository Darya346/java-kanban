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
    void testCreateAndGetTask() {
        Task task = new Task("Test Task", "Test Description", Status.NEW);
        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask);
        assertNotNull(createdTask.getId());
        assertEquals("Test Task", createdTask.getName());
        assertEquals("Test Description", createdTask.getDescription());
        assertEquals(Status.NEW, createdTask.getStatus());

        Task retrievedTask = taskManager.getTaskById(createdTask.getId());
        assertEquals(createdTask, retrievedTask);
    }

    @Test
    void testCreateAndGetEpic() {
        Epic epic = new Epic("Test Epic", "Test Epic Description");
        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic);
        assertNotNull(createdEpic.getId());
        assertEquals("Test Epic", createdEpic.getName());
        assertEquals("Test Epic Description", createdEpic.getDescription());
        assertEquals(Status.NEW, createdEpic.getStatus());
        assertTrue(createdEpic.getSubtaskIds().isEmpty());

        Epic retrievedEpic = taskManager.getEpicById(createdEpic.getId());
        assertEquals(createdEpic, retrievedEpic);
    }

    @Test
    void testCreateAndGetSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
        Subtask subtask = new Subtask("Test Subtask", "Test Description", Status.NEW, epic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdSubtask);
        assertNotNull(createdSubtask.getId());
        assertEquals("Test Subtask", createdSubtask.getName());
        assertEquals("Test Description", createdSubtask.getDescription());
        assertEquals(Status.NEW, createdSubtask.getStatus());
        assertEquals(epic.getId(), createdSubtask.getEpicId());

        Subtask retrievedSubtask = taskManager.getSubtaskById(createdSubtask.getId());
        assertEquals(createdSubtask, retrievedSubtask);

        List<Subtask> epicSubtasks = taskManager.getSubtasksByEpicId(epic.getId());
        assertEquals(1, epicSubtasks.size());
        assertEquals(createdSubtask.getId(), epicSubtasks.get(0).getId());
    }

    @Test
    void testUpdateTask() {
        Task task = taskManager.createTask(new Task("Original", "Desc", Status.NEW));
        Task updatedTask = new Task("Updated", "Updated Desc", task.getId(), Status.IN_PROGRESS);

        taskManager.updateTask(updatedTask);

        Task retrievedTask = taskManager.getTaskById(task.getId());
        assertEquals("Updated", retrievedTask.getName());
        assertEquals("Updated Desc", retrievedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, retrievedTask.getStatus());
    }

    @Test
    void testUpdateEpic() {
        Epic epic = taskManager.createEpic(new Epic("Original", "Desc"));
        Epic updatedEpic = new Epic("Updated", "Updated Desc");
        updatedEpic.setId(epic.getId());

        taskManager.updateEpic(updatedEpic);

        Epic retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals("Updated", retrievedEpic.getName());
        assertEquals("Updated Desc", retrievedEpic.getDescription());
    }

    @Test
    void testUpdateSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Original", "Desc", Status.NEW, epic.getId()));
        Subtask updatedSubtask = new Subtask("Updated", "Updated Desc", subtask.getId(), Status.DONE, epic.getId());

        taskManager.updateSubtask(updatedSubtask);

        Subtask retrievedSubtask = taskManager.getSubtaskById(subtask.getId());
        assertEquals("Updated", retrievedSubtask.getName());
        assertEquals("Updated Desc", retrievedSubtask.getDescription());
        assertEquals(Status.DONE, retrievedSubtask.getStatus());
    }

    @Test
    void testDeleteTask() {
        Task task = taskManager.createTask(new Task("Test", "Desc", Status.NEW));
        int taskId = task.getId();

        taskManager.deleteTaskById(taskId);

        assertNull(taskManager.getTaskById(taskId));
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void testDeleteEpic() {
        Epic epic = taskManager.createEpic(new Epic("Test", "Desc"));
        int epicId = epic.getId();

        taskManager.deleteEpicById(epicId);

        assertNull(taskManager.getEpicById(epicId));
        assertTrue(taskManager.getAllEpics().isEmpty());
    }

    @Test
    void testDeleteSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Test", "Desc", Status.NEW, epic.getId()));
        int subtaskId = subtask.getId();

        taskManager.deleteSubtaskById(subtaskId);

        assertNull(taskManager.getSubtaskById(subtaskId));
        assertTrue(taskManager.getAllSubtasks().isEmpty());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void testDeleteAllTasks() {
        taskManager.createTask(new Task("Task 1", "Desc", Status.NEW));
        taskManager.createTask(new Task("Task 2", "Desc", Status.IN_PROGRESS));

        taskManager.deleteAllTasks();

        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void testDeleteAllEpics() {
        taskManager.createEpic(new Epic("Epic 1", "Desc"));
        taskManager.createEpic(new Epic("Epic 2", "Desc"));

        taskManager.deleteAllEpics();

        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testDeleteAllSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        taskManager.createSubtask(new Subtask("Sub 1", "Desc", Status.NEW, epic.getId()));
        taskManager.createSubtask(new Subtask("Sub 2", "Desc", Status.DONE, epic.getId()));

        taskManager.deleteAllSubtasks();

        assertTrue(taskManager.getAllSubtasks().isEmpty());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void testGetAllTasks() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Desc", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Desc", Status.IN_PROGRESS));

        List<Task> allTasks = taskManager.getAllTasks();

        assertEquals(2, allTasks.size());
        assertTrue(allTasks.contains(task1));
        assertTrue(allTasks.contains(task2));
    }

    @Test
    void testGetAllEpics() {
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Desc"));
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "Desc"));

        List<Epic> allEpics = taskManager.getAllEpics();

        assertEquals(2, allEpics.size());
        assertTrue(allEpics.contains(epic1));
        assertTrue(allEpics.contains(epic2));
    }

    @Test
    void testGetAllSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Sub 1", "Desc", Status.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Sub 2", "Desc", Status.DONE, epic.getId()));

        List<Subtask> allSubtasks = taskManager.getAllSubtasks();

        assertEquals(2, allSubtasks.size());
        assertTrue(allSubtasks.contains(subtask1));
        assertTrue(allSubtasks.contains(subtask2));
    }

    @Test
    void testEpicStatusCalculation() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));

        // Пустой эпик - NEW
        assertEquals(Status.NEW, epic.getStatus());

        // Все подзадачи NEW - эпик NEW
        Subtask sub1 = taskManager.createSubtask(new Subtask("Sub 1", "Desc", Status.NEW, epic.getId()));
        Epic updatedEpic1 = taskManager.getEpicById(epic.getId());
        assertEquals(Status.NEW, updatedEpic1.getStatus());

        // Все подзадачи DONE - эпик DONE
        taskManager.updateSubtask(new Subtask("Sub 1", "Desc", sub1.getId(), Status.DONE, epic.getId()));
        Epic updatedEpic2 = taskManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, updatedEpic2.getStatus());

        // Подзадачи NEW и DONE - эпик IN_PROGRESS
        Subtask sub2 = taskManager.createSubtask(new Subtask("Sub 2", "Desc", Status.NEW, epic.getId()));
        Epic updatedEpic3 = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic3.getStatus());

        // Все подзадачи IN_PROGRESS - эпик IN_PROGRESS
        taskManager.updateSubtask(new Subtask("Sub 1", "Desc", sub1.getId(), Status.IN_PROGRESS, epic.getId()));
        taskManager.updateSubtask(new Subtask("Sub 2", "Desc", sub2.getId(), Status.IN_PROGRESS, epic.getId()));
        Epic updatedEpic4 = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic4.getStatus());
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
    void testTaskImmutability() {
        Task task = taskManager.createTask(new Task("Test", "Description", Status.NEW));
        int originalId = task.getId();

        // Пытаемся изменить задачу через сеттеры
        task.setId(999);
        task.setName("Modified");

        // Получаем задачу из менеджера
        Task managedTask = taskManager.getTaskById(originalId);

        // Проверяем, что данные в менеджере не изменились
        assertEquals(originalId, managedTask.getId());
        assertEquals("Test", managedTask.getName());
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
    void testCreateTaskWithNull() {
        Task result = taskManager.createTask(null);
        assertNull(result);
    }

    @Test
    void testCreateEpicWithNull() {
        Epic result = taskManager.createEpic(null);
        assertNull(result);
    }

    @Test
    void testCreateSubtaskWithNull() {
        Subtask result = taskManager.createSubtask(null);
        assertNull(result);
    }

    @Test
    void testCreateSubtaskWithInvalidEpic() {
        Subtask subtask = new Subtask("Test", "Desc", Status.NEW, 999);
        Subtask result = taskManager.createSubtask(subtask);
        assertNull(result);
    }

    @Test
    void testUpdateNonExistentTask() {
        Task task = new Task("Test", "Desc", 999, Status.NEW);
        taskManager.updateTask(task);

        // Не должно быть исключений
        assertNull(taskManager.getTaskById(999));
    }

    @Test
    void testUpdateNonExistentEpic() {
        Epic epic = new Epic("Test", "Desc");
        epic.setId(999);
        taskManager.updateEpic(epic);

        // Не должно быть исключений
        assertNull(taskManager.getEpicById(999));
    }

    @Test
    void testUpdateNonExistentSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = new Subtask("Test", "Desc", 999, Status.NEW, epic.getId());
        taskManager.updateSubtask(subtask);

        // Не должно быть исключений
        assertNull(taskManager.getSubtaskById(999));
    }

    @Test
    void testDeleteNonExistentTask() {
        taskManager.deleteTaskById(999);
        // Не должно быть исключений
    }

    @Test
    void testDeleteNonExistentEpic() {
        taskManager.deleteEpicById(999);
        // Не должно быть исключений
    }

    @Test
    void testDeleteNonExistentSubtask() {
        taskManager.deleteSubtaskById(999);
        // Не должно быть исключений
    }

    @Test
    void testGetNonExistentTask() {
        Task task = taskManager.getTaskById(999);
        assertNull(task);
    }

    @Test
    void testGetNonExistentEpic() {
        Epic epic = taskManager.getEpicById(999);
        assertNull(epic);
    }

    @Test
    void testGetNonExistentSubtask() {
        Subtask subtask = taskManager.getSubtaskById(999);
        assertNull(subtask);
    }

    @Test
    void testGetSubtasksByNonExistentEpic() {
        List<Subtask> subtasks = taskManager.getSubtasksByEpicId(999);
        assertTrue(subtasks.isEmpty());
    }
}