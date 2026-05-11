import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final Path notesFile = Path.of("notes.txt");

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() throws Exception {
        Files.deleteIfExists(notesFile);

        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        Files.deleteIfExists(notesFile);
    }

    @Test
    void shouldAddNoteAndSaveToFile() {
        runApp("""
                1
                Учеба
                Подготовиться к защите
                0
                """);

        String output = getOutput();

        assertTrue(output.contains("Заметка \"Учеба\" сохранена."));
        assertTrue(Files.exists(notesFile));
    }

    @Test
    void shouldShowAllNotes() {
        runApp("""
                1
                Учеба
                Подготовиться к защите
                1
                Работа
                Закончить отчет
                2
                0
                """);

        String output = getOutput();

        assertTrue(output.contains("Все заметки:"));
        assertTrue(output.contains("1. Учеба"));
        assertTrue(output.contains("2. Работа"));
    }

    @Test
    void shouldShowSpecificNote() {
        runApp("""
                1
                Учеба
                Подготовиться к защите
                3
                Учеба
                0
                """);

        String output = getOutput();

        assertTrue(output.contains("Название: Учеба"));
        assertTrue(output.contains("Содержание: Подготовиться к защите"));
    }

    @Test
    void shouldShowMessageWhenNotesListIsEmpty() {
        runApp("""
                2
                0
                """);

        String output = getOutput();

        assertTrue(output.contains("Заметок пока нет."));
    }

    @Test
    void shouldShowMessageWhenNoteNotFound() {
        runApp("""
                3
                Спорт
                0
                """);

        String output = getOutput();

        assertTrue(output.contains("Заметка с названием \"Спорт\" не найдена."));
    }

    @Test
    void shouldShowErrorWhenTitleIsEmpty() {
        runApp("""
                1
                
                0
                """);

        String output = getOutput();

        assertTrue(output.contains("Ошибка: название заметки не может быть пустым."));
    }

    @Test
    void shouldShowErrorWhenContentIsEmpty() {
        runApp("""
                1
                Учеба
                
                0
                """);

        String output = getOutput();

        assertTrue(output.contains("Ошибка: содержание заметки не может быть пустым."));
    }

    @Test
    void shouldShowErrorWhenCommandIsUnknown() {
        runApp("""
                9
                0
                """);

        String output = getOutput();

        assertTrue(output.contains("Ошибка: неизвестная команда."));
    }

    private void runApp(String input) {
        Scanner scanner = new Scanner(input);
        Main.run(scanner);
    }

    private String getOutput() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}