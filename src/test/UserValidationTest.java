import controllers.UserController;
import exception.ValidationException;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    @Test
    void shouldSetLoginAsNameIfNameIsEmpty() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("email@test.com");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName(""); // Пустое имя

        User createdUser = controller.create(user);

        assertEquals("login", createdUser.getName());
    }

    @Test
    void shouldThrowExceptionForInvalidEmail() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("invalid-email"); // Нет @
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionForSpacesInLogin() {
        User user = new User();
        user.setLogin("log in"); // Пробел
        user.setEmail("email@test.com");
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class, () -> controller.create(user));
    }
}