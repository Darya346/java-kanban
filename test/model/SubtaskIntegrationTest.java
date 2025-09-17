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

        // Пытаемся изменить epicId подзадачи на её собственный ID
        subtask.setEpicId(subtask.getId());
        taskManager.updateSubtask(subtask);

        // Epic ID не должен измениться на собственный ID подзадачи
        assertNotEquals(subtask.getId(), subtask.getEpicId(),
                "Subtask не должен быть своим же эпиком");
    }
}