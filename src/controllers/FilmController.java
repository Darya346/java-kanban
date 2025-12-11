package controllers;

import model.Film;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    private final Map<Long, Film> films = new HashMap<>();
    private long idCounter = 1;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);

        // Валидация даты релиза
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза не может быть раньше {}", MIN_RELEASE_DATE);
            throw new RuntimeException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен с ID: {}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);

        if (!films.containsKey(film.getId())) {
            log.error("Фильм с ID {} не найден", film.getId());
            throw new RuntimeException("Фильм не найден");
        }

        // Валидация даты релиза
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Дата релиза не может быть раньше {}", MIN_RELEASE_DATE);
            throw new RuntimeException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        films.put(film.getId(), film);
        log.info("Фильм с ID {} обновлен", film.getId());
        return film;
    }
}