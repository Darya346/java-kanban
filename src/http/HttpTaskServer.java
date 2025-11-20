package http;

import com.sun.net.httpserver.HttpServer;
import manager.TaskManager;
import manager.Managers;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private HttpServer server;

    public HttpTaskServer(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Регистрируем обработчики
            server.createContext("/tasks", new TasksHandler(taskManager));
            server.createContext("/epics", new EpicsHandler(taskManager));
            server.createContext("/subtasks", new SubtasksHandler(taskManager));
            server.createContext("/history", new HistoryHandler(taskManager));
            server.createContext("/prioritized", new PrioritizedHandler(taskManager));

            server.start();
            System.out.println("HTTP Task Server запущен на порту " + PORT);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось запустить HTTP сервер", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("HTTP Task Server остановлен");
        }
    }

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();

        // Добавляем shutdown hook для graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}