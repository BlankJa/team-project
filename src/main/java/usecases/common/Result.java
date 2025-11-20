package usecases.common;

public class Result<T> {
    private final T data;
    private final Object extraData;
    private final String error;
    private final boolean success;

    private Result(T data, Object extraData, String error, boolean success) {
        this.data = data;
        this.extraData = extraData;
        this.error = error;
        this.success = success;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, null, true);
    }

    public static <T> Result<T> error(String error) {
        return new Result<>(null, null, error, false);
    }

    public static <T> Result<T> success(T data, Object extraData) {
        return new Result<>(data, extraData, null, true);
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getError() { return error; }

    public <U> U getExtraData(Class<U> type) {
        return type.cast(extraData);
    }

    public Object getExtraData() {
        return extraData;
    }
}