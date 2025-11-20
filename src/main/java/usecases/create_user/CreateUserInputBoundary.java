package usecases.create_user;

public interface CreateUserInputBoundary {
    CreateUserOutputData execute(CreateUserInputData inputData);
}
