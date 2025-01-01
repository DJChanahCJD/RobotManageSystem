package robotManageSystem.dto;

public class BaseResponse<T> {
    public Integer code;      // 状态码
    public String message;    // 消息
    public T result;         // 数据

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public BaseResponse<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public BaseResponse<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getResult() {
        return result;
    }

    public BaseResponse<T> setResult(T result) {
        this.result = result;
        return this;
    }

    // 静态工厂方法
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<T>()
            .setCode(200)
            .setMessage("success")
            .setResult(data);
    }

    public static <T> BaseResponse<T> ok() {
        return new BaseResponse<T>()
            .setCode(200)
            .setMessage("success");
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<T>()
            .setCode(500)
            .setMessage(message);
    }

    public static <T> BaseResponse<T> error(Integer code, String message) {
        return new BaseResponse<T>()
            .setCode(code)
            .setMessage(message);
    }

    public static <T> BaseResponse<T> unauthorized(String message) {
        return new BaseResponse<T>()
            .setCode(401)
            .setMessage(message);
    }
}
