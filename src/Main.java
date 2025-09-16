import manager.Managers;
import manager.TaskManager;
import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        System.out.println("=== Создание задач ===ее");

        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic1.getId()));

        // Просматриваем задачи
        taskManager.getTaskById(task1.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.getSubtaskById(subtask1.getId());

        System.out.println("История просмотров:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }

        System.out.println("=== Просматриваем еще задачи ===");
        taskManager.getTaskById(task2.getId());
        taskManager.getSubtaskById(subtask2.getId());

        System.out.println("Обновленная история:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}