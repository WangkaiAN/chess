package game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

//进行匹配
public class Matcher {
    private Gson gson = new GsonBuilder().create();
    private BlockingDeque<Request> matchQueue = new LinkedBlockingDeque<>();

    public void addMatchQueue(Request request) throws InterruptedException {
        // offer 方法对于 BlockingQueue 没有 "阻塞" 功能
        // put 方法就有这样的 "阻塞" 的功能了
        // matchQueue.offer(request);
        matchQueue.put(request);
    }
    public void handlerMatch() throws InterruptedException, IOException {
        // 当前匹配算法就只实现最简单粗暴的~~
        // 就只要队列中存在两个元素, 就给配成一对
        // 1. 先尝试从队列中取出两个元素
        //    此处使用 take 而不是 poll 目的就是为了能够阻塞等待
        //    如果当前队列没有元素, 此处的 take 就阻塞了
        //    就可以避免浪费 CPU 资源
        Request player1 = matchQueue.take();
        Request player2 = matchQueue.take();
        System.out.println("Matcher到两个玩家："+ player1.getUserId()+","+player2.getUserId());
        // 2. 根据玩家的 id , 获取到当前玩家的 Session 对象.
        //    为了后面的返回数据做铺垫
        //    同时也是在检查玩家的在线状态.
        Session session1 = OnlineUserManager.getInstance().
                getSession(player1.getUserId());
        Session session2 = OnlineUserManager.getInstance().
                getSession(player2.getUserId());
        if (session1 == null) {
            // 玩家 1 已经掉线. 就把玩家 2 的匹配请求给重新插入到匹配队列中
            matchQueue.put(player2);
            return;
        }
        if (session2 == null) {
            // 玩家 2 已经掉线, 就把玩家 1 的匹配请求给重新插入到匹配队列中
            matchQueue.put(player1);
            return;
        }
        // 3. 如果两个玩家都没掉线, 就可以把两个玩家放到同一个房间中
        //    进行对战了
        //    此处要实现的逻辑就是创建一个房间, 把玩家放进去.
        Room room = new Room();
        room.setUserId1(player1.getUserId());
        room.setUserId2(player2.getUserId());
        //    再把房间放到 "房间管理器" 中
        RoomManager.getInstance().addRoom(room);
        // 4. 给两个玩家反馈 "匹配成功" 消息给客户端发送
        //  1) 给玩家1返回
        MatcherResponse response1 = new MatcherResponse();
        response1.setRoomId(room.getRoomId());
        response1.setWhite(true);
        response1.setOtherUserId(player2.getUserId());
        String respString1 = gson.toJson(response1);
        session1.getBasicRemote().sendText(respString1);
        //  2) 给玩家2返回
        MatcherResponse response2 = new MatcherResponse();
        response2.setRoomId(room.getRoomId());
        response2.setWhite(false);
        response2.setOtherUserId(player1.getUserId());
        String respString2 = gson.toJson(response2);
        session2.getBasicRemote().sendText(respString2);
    }

    //此处也把这个Matcher类实现为单例模式
    private Matcher(){
        // 在此处创建一个专门的扫描线程, 来扫描队列中是否存在合适的玩家配对
        Thread t = new Thread(){
            @Override
            public void run() {
                // 线程的入口方法.
                // 这个扫描队列的过程是自始至终的.
                // 只要服务器正在运行, 就可能有客户端发来匹配请求
                // 就需要持续不断的进行处理
                while(true){
                    try {
                        handlerMatch();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    private static Matcher matcher = new Matcher();
    public static Matcher getInstance() {
        return matcher;
    }
}
