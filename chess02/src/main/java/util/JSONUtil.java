package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

//工具类
public class JSONUtil {

    private static final ObjectMapper M = new ObjectMapper();
    //序列化方法，将Java对象转化为json 字符串
    public static  String serialize(Object o) throws JsonProcessingException {
        return M.writeValueAsString(o);
    }

    //反序列化方法，将json 字符串反序列化Java对象
    public static <T> T deserialize(InputStream is, Class<T> c) throws IOException {
        return M.readValue(is,c);
    }

}
