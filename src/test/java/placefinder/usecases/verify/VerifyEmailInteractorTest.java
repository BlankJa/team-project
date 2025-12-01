package placefinder.usecases.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.User;
import placefinder.usecases.dataacessinterfaces.UserDataAccessInterface;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * unit tests for {@link VerifyEmailInteractor}.
 *
 * <p>This suite is designed to exercise:
 * <ul>
 *     <li>All lines of code (100% line coverage).</li>
 *     <li>All decision outcomes / paths (100% branch coverage).</li>
 * </ul>
 *
 */
class VerifyEmailInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators & System Under Test
    // -------------------------------------------------------------------------

    /** Persistence port used by the interactor (mock). */
    private UserDataAccessInterface userDataAccessInterface;

    /** Presenter / output boundary used by the interactor (mock). */
    private VerifyEmailOutputBoundary presenter;

    /** System under test. */
    private VerifyEmailInteractor interactor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        userDataAccessInterface = mock(UserDataAccessInterface.class);
        presenter   = mock(VerifyEmailOutputBoundary.class);
        interactor  = new VerifyEmailInteractor(userDataAccessInterface, presenter);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link VerifyEmailOutputData} instance sent to the
     * presenter during an interactor execution.
     */
    private VerifyEmailOutputData capturePresenterOutput() {
        ArgumentCaptor<VerifyEmailOutputData> captor =
                ArgumentCaptor.forClass(VerifyEmailOutputData.class);
        verify(presenter).present(captor.capture());
        return captor.getValue();
    }

    // -------------------------------------------------------------------------
    // Validation branch tests (email / code presence + trim / null-handling)
    // -------------------------------------------------------------------------

    /**
     * Branch 1a: email is {@code null}, code is non-empty.
     *
     * <p>Exercises:
     * <ul>
     *     <li>{@code inputData.getEmail() == null} branch of the ternary.</li>
     *     <li>{@code email.isEmpty()} evaluates to {@code true}.</li>
     *     <li>{@code code.isEmpty()} is never evaluated due to short-circuit.</li>
     * </ul>
     */
    @Test
    void nullEmailNonEmptyCode_triggersRequiredFieldsError() throws Exception {
        VerifyEmailInputData input =
                new VerifyEmailInputData(null, "123456");

        interactor.execute(input);

        // No persistence calls should be made on pure validation failure
        verifyNoInteractions(userDataAccessInterface);

        VerifyEmailOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Email and code are required.", out.getMessage());
    }

    /**
     * Branch 1b: email is non-empty, code is whitespace → after trim becomes empty.
     *
     * <p>Exercises:
     * <ul>
     *     <li>{@code inputData.getCode() != null} branch of the ternary.</li>
     *     <li>{@code email.isEmpty()} is {@code false}.</li>
     *     <li>{@code code.isEmpty()} is evaluated and {@code true}.</li>
     * </ul>
     */
    @Test
    void nonEmptyEmailBlankCode_triggersRequiredFieldsError() throws Exception {
        VerifyEmailInputData input =
                new VerifyEmailInputData("user@example.com", "   ");

        interactor.execute(input);

        verifyNoInteractions(userDataAccessInterface);

        VerifyEmailOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Email and code are required.", out.getMessage());
    }

    // -------------------------------------------------------------------------
    // Main control-flow branches inside try { ... }
    // -------------------------------------------------------------------------

    /**
     * Branch 2: No user exists for the provided email.
     *
     * <p>Exercises the {@code user == null} branch.
     */
    @Test
    void noUserForEmail_returnsNoAccountMessage() throws Exception {
        VerifyEmailInputData input =
                new VerifyEmailInputData("user@example.com", "123456");

        when(userDataAccessInterface.findByEmail("user@example.com")).thenReturn(null);

        interactor.execute(input);

        verify(userDataAccessInterface).findByEmail("user@example.com");
        verify(userDataAccessInterface, never()).save(any());

        VerifyEmailOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("No account found for that email.", out.getMessage());
    }

    /**
     * Branch 3: User exists and is already verified.
     *
     * <p>Exercises the {@code user.isVerified() == true} branch.
     */
    @Test
    void alreadyVerifiedUser_returnsAlreadyVerifiedMessage() throws Exception {
        User user = new User(
                1,
                "Existing User",
                "verified@example.com",
                "hash",
                "Toronto",
                true,
                "ignored-code"
        );

        VerifyEmailInputData input =
                new VerifyEmailInputData("verified@example.com", "whatever");

        when(userDataAccessInterface.findByEmail("verified@example.com")).thenReturn(user);

        interactor.execute(input);

        verify(userDataAccessInterface).findByEmail("verified@example.com");
        verify(userDataAccessInterface, never()).save(any());

        VerifyEmailOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
        assertEquals("This email is already verified.", out.getMessage());

        // Sanity check: user state remains unchanged
        assertTrue(user.isVerified());
        assertEquals("ignored-code", user.getVerificationCode());
    }

    /**
     * Branch 4a: User exists, not verified, but storedCode is {@code null}.
     *
     * <p>Exercises the {@code storedCode == null} side of
     * {@code if (storedCode == null || !storedCode.equals(code))}.
     */
    @Test
    void nullStoredCode_isTreatedAsInvalidCode() throws Exception {
        User user = new User(
                2,
                "Null Code User",
                "nullcode@example.com",
                "hash",
                "Lahore",
                false,
                null // stored verification code is null
        );

        VerifyEmailInputData input =
                new VerifyEmailInputData("nullcode@example.com", "123456");

        when(userDataAccessInterface.findByEmail("nullcode@example.com")).thenReturn(user);

        interactor.execute(input);

        verify(userDataAccessInterface).findByEmail("nullcode@example.com");
        verify(userDataAccessInterface, never()).save(any());

        VerifyEmailOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Invalid verification code.", out.getMessage());

        assertFalse(user.isVerified());
        assertNull(user.getVerificationCode());
    }

    /**
     * Branch 4b: User exists, not verified, storedCode non-null but
     * does not match the provided code.
     *
     * <p>Exercises the {@code storedCode != null} AND
     * {@code !storedCode.equals(code)} side of the condition.
     */
    @Test
    void mismatchingCode_returnsInvalidVerificationMessage() throws Exception {
        User user = new User(
                3,
                "Mismatch User",
                "user@example.com",
                "hash",
                "Karachi",
                false,
                "STORED-CODE"
        );

        VerifyEmailInputData input =
                new VerifyEmailInputData("user@example.com", "ENTERED-CODE");

        when(userDataAccessInterface.findByEmail("user@example.com")).thenReturn(user);

        interactor.execute(input);

        verify(userDataAccessInterface).findByEmail("user@example.com");
        verify(userDataAccessInterface, never()).save(any());

        VerifyEmailOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Invalid verification code.", out.getMessage());

        assertFalse(user.isVerified());
        assertEquals("STORED-CODE", user.getVerificationCode());
    }

    /**
     * Branch 5 (happy path): User exists, not verified, and the provided
     * code matches the stored verification code after trimming.
     *
     * <p>Exercises the "success" branch where:
     * <ul>
     *     <li>{@code storedCode != null}</li>
     *     <li>{@code storedCode.equals(code)} is {@code true}</li>
     * </ul>
     */
    @Test
    void validCode_marksUserVerified_clearsCode_andPersists() throws Exception {
        User user = new User(
                4,
                "Happy Path User",
                "user@example.com",
                "hash",
                "Islamabad",
                false,
                "123456"
        );

        // Extra whitespace in both fields to exercise trim() logic
        VerifyEmailInputData input =
                new VerifyEmailInputData("  user@example.com  ", " 123456 ");

        when(userDataAccessInterface.findByEmail("user@example.com")).thenReturn(user);

        interactor.execute(input);

        verify(userDataAccessInterface).findByEmail("user@example.com");
        verify(userDataAccessInterface).save(user);

        // Verify mutations on the entity
        assertTrue(user.isVerified());
        assertNull(user.getVerificationCode());

        VerifyEmailOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
        assertEquals("Email verified successfully. You can now log in.",
                out.getMessage());
    }

    // -------------------------------------------------------------------------
    // Exception handling branch (catch block)
    // -------------------------------------------------------------------------

    /**
     * Branch 6: An exception is thrown by the gateway during lookup.
     *
     * <p>Exercises the {@code catch (Exception e)} block and ensures the
     * interactor translates the technical failure into a user-facing error.
     */
    @Test
    void gatewayThrowsException_presenterReceivesFailureMessage() throws Exception {
        VerifyEmailInputData input =
                new VerifyEmailInputData("boom@example.com", "999999");

        when(userDataAccessInterface.findByEmail("boom@example.com"))
                .thenThrow(new Exception("DB is down"));

        interactor.execute(input);

        verify(userDataAccessInterface).findByEmail("boom@example.com");
        verify(userDataAccessInterface, never()).save(any());

        VerifyEmailOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertTrue(out.getMessage().startsWith("Verification failed: "));
        assertTrue(out.getMessage().contains("DB is down"));
    }

    /**
     * Additional validation branch: email is non-null but code is null.
     *
     * <p>This specifically exercises the {@code inputData.getCode() == null}
     * branch of the ternary that normalizes the code value.
     */
    @Test
    void nonEmptyEmailNullCode_triggersRequiredFieldsError() throws Exception {
        // email non-null, code is actually null
        VerifyEmailInputData input =
                new VerifyEmailInputData("user@example.com", null);

        interactor.execute(input);

        // Gateway must not be called for invalid input
        verifyNoInteractions(userDataAccessInterface);

        VerifyEmailOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Email and code are required.", out.getMessage());
    }
}
