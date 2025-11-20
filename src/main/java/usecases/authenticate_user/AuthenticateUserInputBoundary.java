package usecases.authenticate_user;

public interface AuthenticateUserInputBoundary {
    AuthenticateUserOutputData execute(AuthenticateUserInputData inputData);
}
