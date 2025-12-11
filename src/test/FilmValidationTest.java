import controllers.FilmController;
import exception.ValidationException;
import model.Film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    @Test
    void shouldCreateFilmWithStandardData() {
        Film film = new Film();
        film.setName("Titanic");
        film.setDescription("Ship crash");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(1997, 12, 19));

        Film createdFilm = controller.create(film);

        assertNotNull(createdFilm);
        assertEquals(1, createdFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        Film film = new Film();
        film.setName(""); // Пустое имя
        film.setDescription("Description");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.now());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            controller.create(film);
        });
        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        Film film = new Film();
        film.setName("Name");
        // Создаем строку длиной 201 символ
        String longDescription = "a".repeat(201);
        film.setDescription(longDescription);
        film.setDuration(100);
        film.setReleaseDate(LocalDate.now());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            controller.create(film);
        });
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsTooOld() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setDuration(100);
        // Дата раньше 28 декабря 1895
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            controller.create(film);
        });
        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Name");
        film.setDescription("Description");
        film.setDuration(-1); // Отрицательная длительность
        film.setReleaseDate(LocalDate.now());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            controller.create(film);
        });
        assertEquals("Продолжительность фильма должна быть положительной", exception.getMessage());
    }
}