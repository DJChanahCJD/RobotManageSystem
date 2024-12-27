package robotManageSystem.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;  // 状态码，1 代表失败，0 代表成功
    private String msg;    // 提示信息
    private T data;       // 数据

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功，不带数据
    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    // 成功，带数据
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    // 失败
    public static <T> Result<T> error(String msg) {
        return new Result<>(1, msg, null);
    }
}