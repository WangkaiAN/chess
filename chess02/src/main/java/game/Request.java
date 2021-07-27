package game;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
// 通过这个类既能够表示 匹配请求, 又能够表示 落子请求
public class Request {
    private String type;
    private int userId;
    // 下面的这三个字段仅在 type 为 "putChess" 的时候使用
    private String roomId;
    private int row;
    private int col;
}
