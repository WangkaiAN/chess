package game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.ScoreUpdate;
import lombok.SneakyThrows;

import javax.websocket.Session;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class Room {
    private Gson gson = new GsonBuilder().create();

    private static final int MAX_ROW = 15;
    private static final int MAX_COL = 15;

    // 此处我们需要保证房间 id 的唯一性
    // 每次创建一个新的房间实例, 该房间实例都是和其他房间不重复
    // 此处我们使用 UUID 来作为房间 id, 保证每个房间的编号不重复
    private String roomId;
    // 两个对局的玩家
    private int userId1;
    private int userId2;

    // 游戏状态. 也就是棋盘上的情况
    // 约定 玩家1 的棋子用 1 表示. 玩家2 的棋子用 2 表示.
    // 未落子的空白位置, 使用 0 表示. 初始情况下棋盘里就是全 0
    private int[][] chessBoard = new int[MAX_ROW][MAX_COL];

    public Room() {
        this.roomId = UUID.randomUUID().toString();
        // 在此处创建一个专门的扫描线程, 来扫描两个用户是否在线
        Thread t = new Thread(){
            @SneakyThrows
            @Override
            public void run() {
                // 线程的入口方法.
                // 这个扫描队列的过程是自始至终的.
                // 只要服务器正在运行, 就可能有客户端发来匹配请求
                // 就需要持续不断的进行处理
                while(true){
                    Session session1 = OnlineUserManager.getInstance().getSession(userId1);
                    Session session2 = OnlineUserManager.getInstance().getSession(userId2);
//                    String responseString = gson.toJson(response);
                    if (session1 == null && session2 == null) {
                        // 此时认为两个玩家都掉线了. 这局就没了
                        // 直接让该方法结束, 销毁当前房间对象
                        // 把房间从房间管理器中删除即可
                        RoomManager.getInstance().removeRoom(roomId);
                        System.out.println("两个玩家都掉线了! " + roomId);
                        break;
                    }
                    if (session1 == null) {
                        // 玩家1 掉线, 直接判定玩家2 获胜
                        try {
                            ScoreUpdate.query(userId2);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        response.setRow(0);
                        response.setCol(0);
                        response.setUserId(userId2);
                        response.setWinner(userId2);
                        String responseString = gson.toJson(response);
                        RoomManager.getInstance().removeRoom(roomId);
                        session2.getBasicRemote().sendText(responseString);
                        System.out.println("game over ! " + roomId);
                        System.out.println("miss"+userId1+" userId"+userId2+"Winner");
                        break;
                    }
                    if (session2 == null) {
                        // 玩家2 掉线, 直接判定玩家1 获胜
                        try {
                            ScoreUpdate.query(userId1);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        response.setRow(0);
                        response.setCol(0);
                        response.setWinner(userId1);
                        response.setUserId(userId1);
                        String responseString = gson.toJson(response);
                        RoomManager.getInstance().removeRoom(roomId);
                        session1.getBasicRemote().sendText(responseString);
                        System.out.println("game over ! " + roomId);
                        System.out.println("miss"+userId2+" userId"+userId1+"Winner");
                        break;
                    }
                }
            }
        };
        t.start();
    }
    public String getRoomId() {
        return roomId;
    }
    public int getUserId1() {
        return userId1;
    }
    public int getUserId2() {
        return userId2;
    }
    public void setUserId1(int userId1) {
        this.userId1 = userId1;
    }
    public void setUserId2(int userId2) {
        this.userId2 = userId2;
    }

    PutChessPesponse response = new PutChessPesponse();
    //预期通过这个方法。来完成这个具体的落子过程
    public void putChess(Request request) throws IOException{
        // 1. 需要把这个子给放到棋盘上
        //    放到棋盘上的位置, 就是 request 中的 row 和 col 描述的
        //    到底是给棋盘上放 1 还是 2 这样的数据, 就需要根据用户请求中的 userId 来进行设定
        int chess = request.getUserId() == userId1 ? 1:2;
        int row = request.getRow();
        int col = request.getCol();
        if(chessBoard[row][col] != 0){
            //此处说明玩家落子的位置是错误的，该位置已经有子了
            System.out.println("error putChess!!!"+request);
            return;
        }
        chessBoard[row][col] = chess;
        //打印一下棋盘，方便调试
        printChessBoard();
        //2.检查游戏是否结束了。需要判断游戏是否结束
        int winner = checkWinner(chess,row,col);
        // 3. 把响应返回给玩家了. 告诉所有的玩家, 当前是谁, 把某个颜色的子放到某个位置上了,
        // a)先构建一个响应对象

        response.setUserId(request.getUserId());
        response.setRow(row);
        response.setCol(col);
        response.setWinner(winner);
        //    b) 给玩家进行响应, 响应的时候, 就需要根据玩家 id, 获取到玩家的 Session 对象
        Session session1 = OnlineUserManager.getInstance().getSession(userId1);
        Session session2 = OnlineUserManager.getInstance().getSession(userId2);
        //    c) 如果某个玩家掉线了, 就给对方响应一个 "您获胜了"
//        if (session1 == null && session2 == null) {
//            // 此时认为两个玩家都掉线了. 这局就没了
//            // 直接让该方法结束, 销毁当前房间对象
//            // 把房间从房间管理器中删除即可
//            RoomManager.getInstance().removeRoom(roomId);
//            System.out.println("两个玩家都掉线了! " + roomId);
//            return;
//        }
//        if (session1 == null) {
//            // 玩家1 掉线, 直接判定玩家2 获胜
//            ScoreUpdate.query(userId2);
//            response.setWinner(userId2);
//            RoomManager.getInstance().removeRoom(roomId);
//            System.out.println("game over, 房间被销毁! " + roomId);
//            System.out.println("miss1 userId"+userId2+"Winner");
//        }
//        if (session2 == null) {
//            // 玩家2 掉线, 直接判定玩家1 获胜
//            ScoreUpdate.query(userId1);
//            response.setWinner(userId1);
//            RoomManager.getInstance().removeRoom(roomId);
//            System.out.println("game over, 房间被销毁! " + roomId);
//            System.out.println("miss2 userId"+userId1+"Winner");
//        }
        //    d) 把当前构造好的响应对象, 转化成 JSON 字符串, 写回给客户端
        String responseString = gson.toJson(response);
        if (session1 != null) {
            session1.getBasicRemote().sendText(responseString);
        }
        if (session2 != null) {
            session2.getBasicRemote().sendText(responseString);
        }
        // 4. 如果当前胜负已分, 此时就销毁当前的房间
        if (winner != 0) {
            // 把房间从房间管理器中删除
            RoomManager.getInstance().removeRoom(roomId);
            if(winner== userId1){
//                ScoreUpdate.query(userId1);
                System.out.println("userId"+userId1+"Winner");
            }else{
//                ScoreUpdate.query(userId2);
                System.out.println("userId"+userId2+"Winner");
            }
//            System.out.println("game over, 房间被销毁! " + roomId);
        }
    }

    private int checkWinner(int chess, int row, int col) {
        // done 表示当前是否已经分出胜负
        // 如果 done 为 true 表示已经找到了五子连珠的情况
        boolean done = false;
        // 1. 检查一行的五种情况
        for (int c = col - 4; c <= col; c++) {
            // c 是这一行的最左侧, c+4 是这一行的最右侧
            if (c < 0 || c + 4 >= MAX_COL) {
                continue;
            }
            if (chessBoard[row][c] == chess
                    && chessBoard[row][c+1] == chess
                    && chessBoard[row][c+2] == chess
                    && chessBoard[row][c+3] == chess
                    && chessBoard[row][c+4] == chess
            ) {
                done = true;
            }
        }
        // 2. 检查一列的五种情况
        for (int r = row - 4; r <= row; r++) {
            if (r < 0 || r + 4>= MAX_ROW) {
                continue;
            }
            if (chessBoard[r][col] == chess
                    && chessBoard[r+1][col] == chess
                    && chessBoard[r+2][col] == chess
                    && chessBoard[r+3][col] == chess
                    && chessBoard[r+4][col] == chess
            ) {
                done = true;
            }
        }
        // 3. 检查左对角线的五种情况
        for (int r = row - 4, c = col - 4; r <= row && c <= col; r++, c++) {
            if (r < 0 || r + 4 >= MAX_ROW || c < 0 || c + 4 >= MAX_COL) {
                continue;
            }
            if (chessBoard[r][c] == chess
                    && chessBoard[r + 1][c + 1] == chess
                    && chessBoard[r + 2][c + 2] == chess
                    && chessBoard[r + 3][c + 3] == chess
                    && chessBoard[r + 4][c + 4] == chess
            ) {
                done = true;
            }
        }
        // 4. 检查右对角线的五种情况
        for (int r = row - 4, c = col + 4; r <= row && c >= col; r++, c--) {
            if (r < 0 || r + 4 >= MAX_ROW || c - 4 < 0 || c >= MAX_COL) {
                continue;
            }
            if (chessBoard[r][c] == chess
                    && chessBoard[r + 1][c - 1] == chess
                    && chessBoard[r + 2][c - 2] == chess
                    && chessBoard[r + 3][c - 3] == chess
                    && chessBoard[r + 4][c - 4] == chess
            ) {
                done = true;
            }
        }
        if (!done) {
            return 0;
        }
        return chess == 1 ? userId1 : userId2;
    }

    private void printChessBoard() {
        // 实现打印棋盘. 就可以在服务器这边看到当前棋面上的内容了
        System.out.println("======================");
        for (int r = 0; r < MAX_ROW; r++) {
            for (int c = 0; c < MAX_COL; c++) {
                System.out.print(chessBoard[r][c] + " ");
            }
            System.out.println();
        }
        System.out.println("======================");
    }
}
