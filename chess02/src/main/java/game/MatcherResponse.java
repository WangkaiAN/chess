package game;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
//处理匹配响应
public class MatcherResponse {
    private String type = "startMatch";
    private String roomId;
    private boolean isWhite;
    private int otherUserId;
}
