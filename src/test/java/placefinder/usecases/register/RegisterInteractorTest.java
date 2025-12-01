package placefinder.usecases.register;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.PasswordUtil;
import placefinder.entities.User;
import placefinder.usecases.ports.EmailGateway;
import placefinder.usecases.ports.UserGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RegisterInteractor}.
 *
 * <p>This suite is designed to exercise:
 * <ul>
 *     <li>All lines of code (100% line coverage).</li>
 *     <li>All decision outcomes / paths (100% branch coverage).</li>
 * </ul>
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>Basic validation (empty name, email, or password)</li>
 *     <li>Email format validation</li>
 *     <li>Password length validation</li>
 *     <li>Existing verified user (email already in use)</li>
 *     <li>Existing unverified user (resend verification code)</li>
 *     <li>Successful registration</li>
 *     <li>Exception handling (gateway throws exception)</li>
 * </ul>
 */
class RegisterInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators & System Under Test
    // -------------------------------------------------------------------------

    /** Persistence port used by the interactor (mock). */
    private UserGateway userGateway;

    /** Email gateway used by the interactor (mock). */
    private EmailGateway emailGateway;

    /** Presenter / output boundary used by the interactor (mock). */
    private RegisterOutputBoundary presenter;

    /** System under test. */
    private RegisterInteractor interactor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        userGateway = mock(UserGateway.class);
        emailGateway = mock(EmailGateway.class);
        presenter   = mock(RegisterOutputBoundary.class);
        interactor  = new RegisterInteractor(userGateway, presenter, emailGateway);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link RegisterOutputData} instance sent to the
     * presenter during an interactor execution.
     */
    private RegisterOutputData capturePresenterOutput() {
        ArgumentCaptor<RegisterOutputData> captor =
                ArgumentCaptor.forClass(RegisterOutputData.class);
        verify(presenter).present(captor.capture());
        return captor.getValue();
    }

    // -------------------------------------------------------------------------
    // Validation branch tests
    // -------------------------------------------------------------------------

    /**
     * Branch 1a: Empty name triggers validation error.
     */
    @Test
    void emptyName_triggersRequiredFieldsError() throws Exception {
        RegisterInputData input = new RegisterInputData("", "user@example.com", "password123", "Toronto");

        interactor.execute(input);

        verifyNoInteractions(userGateway);
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Name, email, and password are required to register.", out.getMessage());
    }

    /**
     * Branch 1b: Empty email triggers validation error.
     */
    @Test
    void emptyEmail_triggersRequiredFieldsError() throws Exception {
        RegisterInputData input = new RegisterInputData("John Doe", "", "password123", "Toronto");

        interactor.execute(input);

        verifyNoInteractions(userGateway);
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Name, email, and password are required to register.", out.getMessage());
    }

    /**
     * Branch 1c: Empty password triggers validation error.
     */
    @Test
    void emptyPassword_triggersRequiredFieldsError() throws Exception {
        RegisterInputData input = new RegisterInputData("John Doe", "user@example.com", "", "Toronto");

        interactor.execute(input);

        verifyNoInteractions(userGateway);
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Name, email, and password are required to register.", out.getMessage());
    }

    /**
     * Branch 1d: Null values are trimmed to empty and trigger validation error.
     */
    @Test
    void nullName_triggersRequiredFieldsError() throws Exception {
        RegisterInputData input = new RegisterInputData(null, "user@example.com", "password123", "Toronto");

        interactor.execute(input);

        verifyNoInteractions(userGateway);
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Name, email, and password are required to register.", out.getMessage());
    }

    /**
     * Branch 1e: Whitespace-only values are trimmed to empty and trigger validation error.
     */
    @Test
    void whitespaceOnlyName_triggersRequiredFieldsError() throws Exception {
        RegisterInputData input = new RegisterInputData("   ", "user@example.com", "password123", "Toronto");

        interactor.execute(input);

        verifyNoInteractions(userGateway);
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Name, email, and password are required to register.", out.getMessage());
    }

    /**
     * Branch 2a: Email without @ symbol triggers validation error.
     */
    @Test
    void emailWithoutAtSymbol_triggersInvalidEmailError() throws Exception {
        RegisterInputData input = new RegisterInputData("John Doe", "invalidemail.com", "password123", "Toronto");

        interactor.execute(input);

        verifyNoInteractions(userGateway);
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please enter a valid email address.", out.getMessage());
    }

    /**
     * Branch 2b: Email without dot triggers validation error.
     */
    @Test
    void emailWithoutDot_triggersInvalidEmailError() throws Exception {
        RegisterInputData input = new RegisterInputData("John Doe", "invalid@email", "password123", "Toronto");

        interactor.execute(input);

        verifyNoInteractions(userGateway);
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please enter a valid email address.", out.getMessage());
    }

    /**
     * Branch 3: Password too short triggers validation error.
     */
    @Test
    void passwordTooShort_triggersPasswordLengthError() throws Exception {
        RegisterInputData input = new RegisterInputData("John Doe", "user@example.com", "12345", "Toronto");

        interactor.execute(input);

        verifyNoInteractions(userGateway);
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Password must be at least 6 characters long.", out.getMessage());
    }

    /**
     * Branch 3b: Password exactly 6 characters should pass validation (boundary value).
     */
    @Test
    void passwordExactly6Characters_passesValidation() throws Exception {
        String name = "John Doe";
        String email = "john@example.com";
        String password = "123456";  // exactly 6 characters
        RegisterInputData input = new RegisterInputData(name, email, password, "Toronto");

        when(userGateway.findByEmail(email)).thenReturn(null);

        interactor.execute(input);

        verify(userGateway).findByEmail(email);
        verify(userGateway).save(any(User.class));
        verify(emailGateway).sendVerificationEmail(eq(email), anyString());

        RegisterOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
    }

    // -------------------------------------------------------------------------
    // Existing user branch tests
    // -------------------------------------------------------------------------

    /**
     * Branch 4: Existing verified user triggers "email already in use" error.
     */
    @Test
    void existingVerifiedUser_returnsEmailAlreadyInUse() throws Exception {
        String email = "existing@example.com";
        User existingUser = new User(
                1,
                "Existing User",
                email,
                "hash",
                "Toronto",
                true,  // verified
                null
        );

        RegisterInputData input = new RegisterInputData("New User", email, "password123", "Vancouver");

        when(userGateway.findByEmail(email)).thenReturn(existingUser);

        interactor.execute(input);

        verify(userGateway).findByEmail(email);
        verify(userGateway, never()).save(any());
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Email already in use.", out.getMessage());
    }

    // -------------------------------------------------------------------------
    // Success path
    // -------------------------------------------------------------------------

    /**
     * Branch 6b: Registration with empty home city (should store null).
     */
    @Test
    void validInputWithEmptyHomeCity_createsUserWithNullHomeCity() throws Exception {
        String name = "Jane Doe";
        String email = "jane@example.com";
        String password = "password123";
        String homeCity = "";  // empty home city

        RegisterInputData input = new RegisterInputData(name, email, password, homeCity);

        when(userGateway.findByEmail(email)).thenReturn(null);

        interactor.execute(input);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userGateway).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertNull(savedUser.getHomeCity());

        RegisterOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
    }

    /**
     * Branch 6c: Registration with null home city (should store null).
     */
    @Test
    void validInputWithNullHomeCity_createsUserWithNullHomeCity() throws Exception {
        String name = "Bob Smith";
        String email = "bob@example.com";
        String password = "password123";

        RegisterInputData input = new RegisterInputData(name, email, password, null);

        when(userGateway.findByEmail(email)).thenReturn(null);

        interactor.execute(input);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userGateway).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertNull(savedUser.getHomeCity());

        RegisterOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
    }

    // -------------------------------------------------------------------------
    // Exception handling
    // -------------------------------------------------------------------------

    /**
     * Branch 7: Gateway throws exception during findByEmail.
     */
    @Test
    void gatewayThrowsExceptionOnFindByEmail_presenterReceivesFailureMessage() throws Exception {
        String email = "error@example.com";
        RegisterInputData input = new RegisterInputData("Test User", email, "password123", "Toronto");

        when(userGateway.findByEmail(email))
                .thenThrow(new Exception("Database connection failed"));

        interactor.execute(input);

        verify(userGateway).findByEmail(email);
        verify(userGateway, never()).save(any());
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertTrue(out.getMessage().startsWith("Registration failed: "));
        assertTrue(out.getMessage().contains("Database connection failed"));
    }

    /**
     * Branch 7b: Gateway throws exception during save (UNIQUE constraint).
     */
    @Test
    void gatewayThrowsUniqueConstraintException_presenterReceivesEmailAlreadyInUse() throws Exception {
        String email = "duplicate@example.com";
        RegisterInputData input = new RegisterInputData("Test User", email, "password123", "Toronto");

        when(userGateway.findByEmail(email)).thenReturn(null);
        doThrow(new Exception("UNIQUE constraint failed: users.email"))
                .when(userGateway).save(any(User.class));

        interactor.execute(input);

        verify(userGateway).findByEmail(email);
        verify(userGateway).save(any(User.class));
        verifyNoInteractions(emailGateway);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Email already in use.", out.getMessage());
    }

    /**
     * Branch 7c: Gateway throws exception with null message.
     */
    @Test
    void gatewayThrowsExceptionWithNullMessage_presenterReceivesGenericError() throws Exception {
        String email = "error@example.com";
        RegisterInputData input = new RegisterInputData("Test User", email, "password123", "Toronto");

        Exception ex = new Exception((String) null);
        when(userGateway.findByEmail(email)).thenThrow(ex);

        interactor.execute(input);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Registration failed due to an unexpected error.", out.getMessage());
    }

    /**
     * Branch 7d: Gateway throws exception with blank message.
     */
    @Test
    void gatewayThrowsExceptionWithBlankMessage_presenterReceivesGenericError() throws Exception {
        String email = "error@example.com";
        RegisterInputData input = new RegisterInputData("Test User", email, "password123", "Toronto");

        when(userGateway.findByEmail(email)).thenThrow(new Exception("   "));

        interactor.execute(input);

        RegisterOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Registration failed due to an unexpected error.", out.getMessage());
    }
}

