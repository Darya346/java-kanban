package model;

import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskIntegrationTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Description", Status.NEW, epic.getId()));

        // Создаем подзадачу, которая пытается быть своим же эпиком
        Subtask invalidSubtask = new Subtask("Invalid", "Description", subtask.getId(), Status.NEW, subtask.getId());

        // Попытка создания должна вернуть null
        Subtask result = taskManager.createSubtask(invalidSubtask);
        assertNull(result, "Subtask не должен быть своим же эпиком");
    }
}