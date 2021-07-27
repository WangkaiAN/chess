package game;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
//通过这个类来表示落子响应
public class PutChessPesponse {
    private String type = "putChess";
    private int userId;
    private int row;
    private int col;
    private int winner;
}
