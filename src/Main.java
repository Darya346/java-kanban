public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new InMemoryTaskManager();

        System.out.println("=== Создание задач ===");

        // Создаем две обычные задачи
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание задачи 2", Status.NEW));

        // Создаем эпик с двумя подзадачами
        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = taskManager.createSubtask(
                new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(
                new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic1.getId()));

        // Создаем эпик с одной подзадачей
        Epic epic2 = taskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = taskManager.createSubtask(
                new Subtask("Подзадача 3", "Описание подзадачи 3", Status.NEW, epic2.getId()));

        System.out.println("\n=== Все задачи после создания ===");
        System.out.println("Задачи: " + taskManager.getAllTasks());
        System.out.println("Эпики: " + taskManager.getAllEpics());
        System.out.println("Подзадачи: " + taskManager.getAllSubtasks());

        System.out.println("\n=== Подзадачи эпика 1 ===");
        System.out.println(taskManager.getSubtasksByEpicId(epic1.getId()));

        System.out.println("\n=== Изменение статусов ===");

        // Меняем статусы всех созданных объектов
        task1.setStatus(Status.DONE);
        taskManager.updateTask(task1);

        task2.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task2);

        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        subtask3.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask3);

        System.out.println("После изменения статусов:");
        System.out.println("Задачи: " + taskManager.getAllTasks());
        System.out.println("Эпики: " + taskManager.getAllEpics());
        System.out.println("Подзадачи: " + taskManager.getAllSubtasks());

        System.out.println("\n=== Проверка статуса эпика после изменения подзадач ===");
        System.out.println("Статус эпика 1: " + epic1.getStatus());
        System.out.println("Статус эпика 2: " + epic2.getStatus());

        System.out.println("\n=== Удаление задач ===");

        // Удаляем одну задачу и один эпик
        System.out.println("Удаляем задачу 2 и эпик 1");
        taskManager.deleteTaskById(task2.getId());
        taskManager.deleteEpicById(epic1.getId());

        System.out.println("После удаления:");
        System.out.println("Задачи: " + taskManager.getAllTasks());
        System.out.println("Эпики: " + taskManager.getAllEpics());
        System.out.println("Подзадачи: " + taskManager.getAllSubtasks());

        System.out.println("\n=== Тест автономного расчета статуса эпика ===");
        Epic testEpic = taskManager.createEpic(new Epic("Тестовый эпик", "Для проверки статуса"));
        System.out.println("Статус пустого эпика: " + testEpic.getStatus());

        Subtask testSubtask = taskManager.createSubtask(
                new Subtask("Тестовая подзадача", "NEW статус", Status.NEW, testEpic.getId()));
        System.out.println("Статус эпика с NEW подзадачей: " + testEpic.getStatus());

        testSubtask.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(testSubtask);
        System.out.println("Статус эпика с IN_PROGRESS подзадачей: " + testEpic.getStatus());

        testSubtask.setStatus(Status.DONE);
        taskManager.updateSubtask(testSubtask);
        System.out.println("Статус эпика с DONE подзадачей: " + testEpic.getStatus());

        System.out.println("\n=== Проверка получения задач по ID ===");
        Task foundTask = taskManager.getTaskById(task1.getId());
        System.out.println("Найдена задача по ID " + task1.getId() + ": " + foundTask);

        Epic foundEpic = taskManager.getEpicById(epic2.getId());
        System.out.println("Найден эпик по ID " + epic2.getId() + ": " + foundEpic);

        Subtask foundSubtask = taskManager.getSubtaskById(subtask3.getId());
        System.out.println("Найдена подзадача по ID " + subtask3.getId() + ": " + foundSubtask);
    }
}