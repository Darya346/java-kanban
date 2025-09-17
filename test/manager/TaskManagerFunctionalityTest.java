package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskManagerFunctionalityTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void canAddAndFindAllTaskTypes() {
        // Создаем задачи всех типов
        Task task = taskManager.createTask(new Task("Task", "Description", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Description", Status.NEW, epic.getId()));

        // Проверяем, что все создались
        assertNotNull(task, "Задача должна быть создана");
        assertNotNull(epic, "Epic должен быть создан");
        assertNotNull(subtask, "Subtask должен быть создан");

        // Проверяем поиск по ID
        assertNotNull(taskManager.getTaskById(task.getId()), "Должна найтись задача по ID");
        assertNotNull(taskManager.getEpicById(epic.getId()), "Должен найтись epic по ID");
        assertNotNull(taskManager.getSubtaskById(subtask.getId()), "Должен найтись subtask по ID");

        // Проверяем получение всех задач
        assertEquals(1, taskManager.getAllTasks().size(), "Должна быть 1 задача");
        assertEquals(1, taskManager.getAllEpics().size(), "Должен быть 1 epic");
        assertEquals(1, taskManager.getAllSubtasks().size(), "Должен быть 1 subtask");
    }
}