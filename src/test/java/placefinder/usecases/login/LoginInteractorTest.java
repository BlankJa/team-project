package placefinder.usecases.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.PasswordUtil;
import placefinder.entities.User;
import placefinder.usecases.dataacessinterfaces.UserDataAccessInterface;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LoginInteractor}.
 *
 * <p>This suite is designed to exercise:
 * <ul>
 *     <li>All lines of code (100% line coverage).</li>
 *     <li>All decision outcomes / paths (100% branch coverage).</li>
 * </ul>
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>User not found (null user)</li>
 *     <li>Invalid password (password hash mismatch)</li>
 *     <li>Unverified user (blocked from login)</li>
 *     <li>Successful login (verified user with correct credentials)</li>
 *     <li>Exception handling (gateway throws exception)</li>
 * </ul>
 */
class LoginInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators & System Under Test
    // -------------------------------------------------------------------------

    /** Persistence port used by the interactor (mock). */
    private UserDataAccessInterface userGateway;

    /** Presenter / output boundary used by the interactor (mock). */
    private LoginOutputBoundary presenter;

    /** System under test. */
    private LoginInteractor interactor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        userGateway = mock(UserDataAccessInterface.class);
        presenter   = mock(LoginOutputBoundary.class);
        interactor  = new LoginInteractor(userGateway, presenter);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link LoginOutputData} instance sent to the
     * presenter during an interactor execution.
     */
    private LoginOutputData capturePresenterOutput() {
        ArgumentCaptor<LoginOutputData> captor =
                ArgumentCaptor.forClass(LoginOutputData.class);
        verify(presenter).present(captor.capture());
        return captor.getValue();
    }

    // -------------------------------------------------------------------------
    // Test cases
    // -------------------------------------------------------------------------

    /**
     * Branch 1: User not found (userGateway.findByEmail returns null).
     *
     * <p>Exercises the {@code user == null} branch.
     */
    @Test
    void userNotFound_returnsInvalidCredentialsMessage() throws Exception {
        LoginInputData input = new LoginInputData("nonexistent@example.com", "password123");

        when(userGateway.findByEmail("nonexistent@example.com")).thenReturn(null);

        interactor.execute(input);

        verify(userGateway).findByEmail("nonexistent@example.com");
        verify(userGateway, never()).save(any());

        LoginOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Invalid email or password.", out.getMessage());
        assertNull(out.getUser());
    }

    /**
     * Branch 2: User exists but password is incorrect.
     *
     * <p>Exercises the {@code !hash.equals(user.getPasswordHash())} branch.
     */
    @Test
    void incorrectPassword_returnsInvalidCredentialsMessage() throws Exception {
        String correctPassword = "correctPassword123";
        String wrongPassword = "wrongPassword456";
        String correctHash = PasswordUtil.hashPassword(correctPassword);

        User user = new User(
                1,
                "Test User",
                "user@example.com",
                correctHash,
                "Toronto",
                true,
                null
        );

        LoginInputData input = new LoginInputData("user@example.com", wrongPassword);

        when(userGateway.findByEmail("user@example.com")).thenReturn(user);

        interactor.execute(input);

        verify(userGateway).findByEmail("user@example.com");
        verify(userGateway, never()).save(any());

        LoginOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Invalid email or password.", out.getMessage());
        assertNull(out.getUser());
    }

    /**
     * Branch 3: User exists with correct password but is not verified.
     *
     * <p>Exercises the {@code !user.isVerified()} branch.
     */
    @Test
    void unverifiedUser_returnsVerificationRequiredMessage() throws Exception {
        String password = "password123";
        String hash = PasswordUtil.hashPassword(password);

        User user = new User(
                2,
                "Unverified User",
                "unverified@example.com",
                hash,
                "Vancouver",
                false,  // not verified
                "123456"
        );

        LoginInputData input = new LoginInputData("unverified@example.com", password);

        when(userGateway.findByEmail("unverified@example.com")).thenReturn(user);

        interactor.execute(input);

        verify(userGateway).findByEmail("unverified@example.com");
        verify(userGateway, never()).save(any());

        LoginOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please verify your email before signing in.", out.getMessage());
        assertNull(out.getUser());
    }

    /**
     * Branch 4 (happy path): User exists, password is correct, and user is verified.
     *
     * <p>Exercises the success branch where all conditions are met.
     */
    @Test
    void validCredentialsAndVerifiedUser_returnsSuccessWithUser() throws Exception {
        String password = "password123";
        String hash = PasswordUtil.hashPassword(password);

        User user = new User(
                3,
                "Verified User",
                "verified@example.com",
                hash,
                "Montreal",
                true,  // verified
                null
        );

        LoginInputData input = new LoginInputData("verified@example.com", password);

        when(userGateway.findByEmail("verified@example.com")).thenReturn(user);

        interactor.execute(input);

        verify(userGateway).findByEmail("verified@example.com");
        verify(userGateway, never()).save(any());

        LoginOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
        assertNull(out.getMessage());
        assertNotNull(out.getUser());
        assertEquals(user.getId(), out.getUser().getId());
        assertEquals(user.getEmail(), out.getUser().getEmail());
        assertEquals(user.getName(), out.getUser().getName());
    }

    /**
     * Branch 5: Exception handling - gateway throws an exception.
     *
     * <p>Exercises the {@code catch (Exception e)} block.
     */
    @Test
    void gatewayThrowsException_presenterReceivesFailureMessage() throws Exception {
        LoginInputData input = new LoginInputData("boom@example.com", "password123");

        when(userGateway.findByEmail("boom@example.com"))
                .thenThrow(new Exception("Database connection failed"));

        interactor.execute(input);

        verify(userGateway).findByEmail("boom@example.com");
        verify(userGateway, never()).save(any());

        LoginOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Database connection failed", out.getMessage());
        assertNull(out.getUser());
    }

    /**
     * Edge case: Exception with null message.
     */
    @Test
    void gatewayThrowsExceptionWithNullMessage_presenterReceivesNullMessage() throws Exception {
        LoginInputData input = new LoginInputData("error@example.com", "password123");

        Exception ex = new Exception((String) null);
        when(userGateway.findByEmail("error@example.com")).thenThrow(ex);

        interactor.execute(input);

        LoginOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertNull(out.getMessage());
        assertNull(out.getUser());
    }
}
