package http;

import model.Task;
import model.Epic;
import model.Subtask;
import model.Status;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class JsonConverter {

    public static String taskToJson(Task task) {
        if (task == null) return "null";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(task.getId()).append(",");
        sb.append("\"name\":\"").append(escapeJson(task.getName())).append("\",");
        sb.append("\"description\":\"").append(escapeJson(task.getDescription())).append("\",");
        sb.append("\"status\":\"").append(task.getStatus()).append("\",");
        sb.append("\"type\":\"TASK\",");

        if (task.getDuration() != null) {
            sb.append("\"duration\":").append(task.getDuration().toMinutes()).append(",");
        } else {
            sb.append("\"duration\":0,");
        }

        if (task.getStartTime() != null) {
            sb.append("\"startTime\":\"").append(task.getStartTime()).append("\"");
        } else {
            sb.append("\"startTime\":null");
        }

        sb.append("}");
        return sb.toString();
    }

    public static String epicToJson(Epic epic) {
        if (epic == null) return "null";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(epic.getId()).append(",");
        sb.append("\"name\":\"").append(escapeJson(epic.getName())).append("\",");
        sb.append("\"description\":\"").append(escapeJson(epic.getDescription())).append("\",");
        sb.append("\"status\":\"").append(epic.getStatus()).append("\",");
        sb.append("\"type\":\"EPIC\",");

        if (epic.getDuration() != null) {
            sb.append("\"duration\":").append(epic.getDuration().toMinutes()).append(",");
        } else {
            sb.append("\"duration\":0,");
        }

        if (epic.getStartTime() != null) {
            sb.append("\"startTime\":\"").append(epic.getStartTime()).append("\",");
        } else {
            sb.append("\"startTime\":null,");
        }

        if (epic.getEndTime() != null) {
            sb.append("\"endTime\":\"").append(epic.getEndTime()).append("\",");
        } else {
            sb.append("\"endTime\":null,");
        }

        sb.append("\"subtaskIds\":").append(epic.getSubtaskIds().toString());
        sb.append("}");
        return sb.toString();
    }

    public static String subtaskToJson(Subtask subtask) {
        if (subtask == null) return "null";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(subtask.getId()).append(",");
        sb.append("\"name\":\"").append(escapeJson(subtask.getName())).append("\",");
        sb.append("\"description\":\"").append(escapeJson(subtask.getDescription())).append("\",");
        sb.append("\"status\":\"").append(subtask.getStatus()).append("\",");
        sb.append("\"type\":\"SUBTASK\",");
        sb.append("\"epicId\":").append(subtask.getEpicId()).append(",");

        if (subtask.getDuration() != null) {
            sb.append("\"duration\":").append(subtask.getDuration().toMinutes()).append(",");
        } else {
            sb.append("\"duration\":0,");
        }

        if (subtask.getStartTime() != null) {
            sb.append("\"startTime\":\"").append(subtask.getStartTime()).append("\"");
        } else {
            sb.append("\"startTime\":null");
        }

        sb.append("}");
        return sb.toString();
    }

    public static String tasksToJson(List<Task> tasks) {
        if (tasks == null) return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append(taskToJson(tasks.get(i)));
            if (i < tasks.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String epicsToJson(List<Epic> epics) {
        if (epics == null) return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < epics.size(); i++) {
            sb.append(epicToJson(epics.get(i)));
            if (i < epics.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String subtasksToJson(List<Subtask> subtasks) {
        if (subtasks == null) return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < subtasks.size(); i++) {
            sb.append(subtaskToJson(subtasks.get(i)));
            if (i < subtasks.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static Task taskFromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("null")) {
                return null;
            }

            // Простой парсинг JSON
            String cleanJson = json.trim().replaceAll("[{}\"]", "");
            String[] pairs = cleanJson.split(",");

            int id = 0;
            String name = "";
            String description = "";
            Status status = Status.NEW;
            Duration duration = Duration.ZERO;
            LocalDateTime startTime = null;

            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length != 2) continue;

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                switch (key) {
                    case "id":
                        id = Integer.parseInt(value);
                        break;
                    case "name":
                        name = unescapeJson(value);
                        break;
                    case "description":
                        description = unescapeJson(value);
                        break;
                    case "status":
                        status = Status.valueOf(value);
                        break;
                    case "duration":
                        duration = Duration.ofMinutes(Long.parseLong(value));
                        break;
                    case "startTime":
                        if (!value.equals("null")) {
                            startTime = LocalDateTime.parse(value);
                        }
                        break;
                }
            }

            if (id == 0) {
                return new Task(name, description, status, duration, startTime);
            } else {
                return new Task(name, description, id, status, duration, startTime);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Epic epicFromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("null")) {
                return null;
            }

            String cleanJson = json.trim().replaceAll("[{}\"]", "");
            String[] pairs = cleanJson.split(",");

            int id = 0;
            String name = "";
            String description = "";
            Status status = Status.NEW;
            Duration duration = Duration.ZERO;
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;

            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length != 2) continue;

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                switch (key) {
                    case "id":
                        id = Integer.parseInt(value);
                        break;
                    case "name":
                        name = unescapeJson(value);
                        break;
                    case "description":
                        description = unescapeJson(value);
                        break;
                    case "status":
                        status = Status.valueOf(value);
                        break;
                    case "duration":
                        duration = Duration.ofMinutes(Long.parseLong(value));
                        break;
                    case "startTime":
                        if (!value.equals("null")) {
                            startTime = LocalDateTime.parse(value);
                        }
                        break;
                    case "endTime":
                        if (!value.equals("null")) {
                            endTime = LocalDateTime.parse(value);
                        }
                        break;
                }
            }

            Epic epic;
            if (id == 0) {
                epic = new Epic(name, description);
            } else {
                epic = new Epic(name, description, id, status, duration, startTime, endTime);
            }

            return epic;
        } catch (Exception e) {
            return null;
        }
    }

    public static Subtask subtaskFromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("null")) {
                return null;
            }

            String cleanJson = json.trim().replaceAll("[{}\"]", "");
            String[] pairs = cleanJson.split(",");

            int id = 0;
            String name = "";
            String description = "";
            Status status = Status.NEW;
            int epicId = 0;
            Duration duration = Duration.ZERO;
            LocalDateTime startTime = null;

            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length != 2) continue;

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                switch (key) {
                    case "id":
                        id = Integer.parseInt(value);
                        break;
                    case "name":
                        name = unescapeJson(value);
                        break;
                    case "description":
                        description = unescapeJson(value);
                        break;
                    case "status":
                        status = Status.valueOf(value);
                        break;
                    case "epicId":
                        epicId = Integer.parseInt(value);
                        break;
                    case "duration":
                        duration = Duration.ofMinutes(Long.parseLong(value));
                        break;
                    case "startTime":
                        if (!value.equals("null")) {
                            startTime = LocalDateTime.parse(value);
                        }
                        break;
                }
            }

            if (id == 0) {
                return new Subtask(name, description, status, epicId, duration, startTime);
            } else {
                return new Subtask(name, description, id, status, epicId, duration, startTime);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}