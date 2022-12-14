
package geovis.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 接口返回对象实体
 *
 * @author tt.zhang
 * @param <T>
 */
@Data
public final class Result<T> implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(Result.class);

    private static final long serialVersionUID = 1L;
    public static final String SUCCESSFUL_CODE = "000000";
    public static final String SUCCESSFUL_MESG = "处理成功";
    /**
     * 错误码
     */
    private Integer code = 0;

    /**
     * 错误信息
     */
    private String msg = null;

    private Integer count = 0;

    /**
     * 返回结果实体
     */
    private T data = null;

    public Result() {
    }

    public Result(Integer code, String msg, Integer count, T data) {
        this.code = code;
        this.msg = msg;
        this.count = count;
        this.data = data;
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getDesc();
    }

    public Result(ResultEnum resultEnum, T data) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getDesc();
        this.data = data;
    }

    public static <T> Result<T> error(String msg) {
        logger.debug("返回错误：code={}, msg={}", ResultEnum.ERROR.getCode(), msg);
        return new Result<T>(ResultEnum.ERROR.getCode(), msg, null);
    }

    public static <T> Result<T> error(ResultEnum resultEnum) {
        logger.debug("返回错误：code={}, msg={}", resultEnum.getCode(), resultEnum.getDesc());
        return new Result<T>(resultEnum.getCode(), resultEnum.getDesc(), null);
    }

    public static <T> Result<T> error(int code, String msg) {
        logger.debug("返回错误：code={}, msg={}", code, msg);
        return new Result<T>(code, msg, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(ResultEnum.SUCCESS.getCode(), "响应成功", 0,data);
    }

    public static <T> Result<T> success(T data, Integer count) {
        return new Result<T>(ResultEnum.SUCCESS.getCode(), "响应成功", count,data);
    }

    /**
     * 成功code=000000
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isSuccess() {
        return SUCCESSFUL_CODE.equals(this.code);
    }

    /**
     * 失败
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isFail() {
        return !isSuccess();
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public static String getSuccessfulCode() {
        return SUCCESSFUL_CODE;
    }

    public static String getSuccessfulMesg() {
        return SUCCESSFUL_MESG;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
