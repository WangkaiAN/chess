package model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class JSONResponse {
    //业务操作是否成功
    private boolean success;
    //业务数据
    private Object data;
    //错误码
    private String code;
    //错误消息
    private String message;
}
