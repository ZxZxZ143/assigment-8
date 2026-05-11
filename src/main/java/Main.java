import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static final String FILE_NAME = "notes.txt";
    private static final String LOG_FILE = "app.log";
    private static final String MONITORING_FILE = "monitoring-output.txt";

    private static final long APP_START_TIME = System.currentTimeMillis();

    private static int commandsExecuted = 0;
    private static int notesAdded = 0;
    private static int notesListed = 0;
    private static int notesShown = 0;
    private static int notesNotFound = 0;
    private static int errorsCount = 0;

    record Note(String title, String content) {
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        run(scanner);
    }

    static void run(Scanner scanner) {
        boolean isRunning = true;

        logInfo("Application started");
        System.out.println("Добро пожаловать в приложение для заметок!");

        while (isRunning) {
            printMenu();

            System.out.print("Выберите действие: ");

            if (!scanner.hasNextLine()) {
                logWarning("Input stream ended unexpectedly");
                break;
            }

            String choice = scanner.nextLine();
            commandsExecuted++;

            switch (choice) {
                case "1":
                    addNoteFromInput(scanner);
                    break;

                case "2":
                    listNotes();
                    break;

                case "3":
                    showNoteFromInput(scanner);
                    break;

                case "4":
                    showMonitoringOutput();
                    break;

                case "0":
                    isRunning = false;
                    generateMonitoringOutput();
                    logInfo("Application finished");
                    System.out.println("Программа завершена.");
                    break;

                default:
                    errorsCount++;
                    logWarning("Unknown command: " + choice);
                    System.out.println("Ошибка: неизвестная команда.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== МЕНЮ =====");
        System.out.println("1. Добавить заметку");
        System.out.println("2. Показать все заметки");
        System.out.println("3. Показать конкретную заметку");
        System.out.println("4. Показать monitoring output");
        System.out.println("0. Выйти");
        System.out.println("================");
    }

    private static void addNoteFromInput(Scanner scanner) {
        System.out.print("Введите название заметки: ");

        if (!scanner.hasNextLine()) {
            errorsCount++;
            logWarning("Title input was not provided");
            return;
        }

        String title = scanner.nextLine();

        if (title.isBlank()) {
            errorsCount++;
            logWarning("User tried to create note with empty title");
            System.out.println("Ошибка: название заметки не может быть пустым.");
            return;
        }

        System.out.print("Введите содержание заметки: ");

        if (!scanner.hasNextLine()) {
            errorsCount++;
            logWarning("Content input was not provided");
            return;
        }

        String content = scanner.nextLine();

        if (content.isBlank()) {
            errorsCount++;
            logWarning("User tried to create note with empty content");
            System.out.println("Ошибка: содержание заметки не может быть пустым.");
            return;
        }

        addNote(title, content);
    }

    private static void showNoteFromInput(Scanner scanner) {
        System.out.print("Введите название заметки: ");

        if (!scanner.hasNextLine()) {
            errorsCount++;
            logWarning("Show note title input was not provided");
            return;
        }

        String title = scanner.nextLine();

        if (title.isBlank()) {
            errorsCount++;
            logWarning("User tried to search note with empty title");
            System.out.println("Ошибка: название заметки не может быть пустым.");
            return;
        }

        showNote(title);
    }

    private static void addNote(String title, String content) {
        List<Note> notes = loadNotes();

        Note note = new Note(title, content);
        notes.add(note);

        saveNotes(notes);

        notesAdded++;
        logInfo("Note added: " + title);

        System.out.println("Заметка \"" + title + "\" сохранена.");
    }

    private static void listNotes() {
        List<Note> notes = loadNotes();
        notesListed++;

        logInfo("User requested notes list. Notes count: " + notes.size());

        if (notes.isEmpty()) {
            System.out.println("Заметок пока нет.");
            return;
        }

        System.out.println();
        System.out.println("Все заметки:");

        for (int i = 0; i < notes.size(); i++) {
            System.out.println((i + 1) + ". " + notes.get(i).title());
        }
    }

    private static void showNote(String title) {
        List<Note> notes = loadNotes();

        for (Note note : notes) {
            if (note.title().equalsIgnoreCase(title)) {
                notesShown++;
                logInfo("Note shown: " + title);

                System.out.println();
                System.out.println("Название: " + note.title());
                System.out.println("Содержание: " + note.content());
                return;
            }
        }

        notesNotFound++;
        logWarning("Note not found: " + title);

        System.out.println("Заметка с названием \"" + title + "\" не найдена.");
    }

    private static List<Note> loadNotes() {
        List<Note> notes = new ArrayList<>();
        Path path = Paths.get(FILE_NAME);

        if (!Files.exists(path)) {
            return notes;
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            for (String line : lines) {
                String[] parts = line.split(";", 2);

                if (parts.length == 2) {
                    String title = decode(parts[0]);
                    String content = decode(parts[1]);

                    notes.add(new Note(title, content));
                }
            }

        } catch (IOException e) {
            errorsCount++;
            logError("Error while reading notes file: " + e.getMessage());
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }

        return notes;
    }

    private static void saveNotes(List<Note> notes) {
        List<String> lines = new ArrayList<>();

        for (Note note : notes) {
            String title = encode(note.title());
            String content = encode(note.content());

            lines.add(title + ";" + content);
        }

        try {
            Files.write(Paths.get(FILE_NAME), lines, StandardCharsets.UTF_8);
            logInfo("Notes saved to file. Total notes: " + notes.size());
        } catch (IOException e) {
            errorsCount++;
            logError("Error while saving notes file: " + e.getMessage());
            System.out.println("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    private static void showMonitoringOutput() {
        generateMonitoringOutput();

        try {
            String content = Files.readString(Paths.get(MONITORING_FILE), StandardCharsets.UTF_8);
            System.out.println();
            System.out.println(content);
        } catch (IOException e) {
            errorsCount++;
            logError("Error while reading monitoring output: " + e.getMessage());
            System.out.println("Ошибка при чтении monitoring output: " + e.getMessage());
        }
    }

    private static void generateMonitoringOutput() {
        long uptimeMillis = System.currentTimeMillis() - APP_START_TIME;
        long uptimeSeconds = uptimeMillis / 1000;

        int totalNotes = loadNotes().size();

        String monitoringData = String.format(
                Locale.ROOT,
                "===== MONITORING OUTPUT =====%n"
                        + "timestamp=%s%n"
                        + "uptime_seconds=%d%n"
                        + "commands_executed=%d%n"
                        + "total_notes=%d%n"
                        + "notes_added_in_session=%d%n"
                        + "list_requests_in_session=%d%n"
                        + "successful_note_views_in_session=%d%n"
                        + "note_not_found_count=%d%n"
                        + "errors_count=%d%n"
                        + "status=UP%n"
                        + "=============================%n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                uptimeSeconds,
                commandsExecuted,
                totalNotes,
                notesAdded,
                notesListed,
                notesShown,
                notesNotFound,
                errorsCount
        );

        try {
            Files.writeString(
                    Paths.get(MONITORING_FILE),
                    monitoringData,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            logInfo("Monitoring output generated");
        } catch (IOException e) {
            errorsCount++;
            logError("Error while generating monitoring output: " + e.getMessage());
            System.out.println("Ошибка при создании monitoring output: " + e.getMessage());
        }
    }

    private static void logInfo(String message) {
        writeLog("INFO", message);
    }

    private static void logWarning(String message) {
        writeLog("WARNING", message);
    }

    private static void logError(String message) {
        writeLog("ERROR", message);
    }

    private static void writeLog(String level, String message) {
        String logMessage = "[%s] [%s] %s%n".formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                level,
                message
        );

        try {
            Files.writeString(
                    Paths.get(LOG_FILE),
                    logMessage,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.out.println("Ошибка при записи лога: " + e.getMessage());
        }
    }

    private static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String text) {
        return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    }
}