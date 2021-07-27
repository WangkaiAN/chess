package game;

import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    //仍然使用一个HashMap来管理房间
    //key就是roomId, value room对象本身
    private ConcurrentHashMap<String,Room> rooms = new ConcurrentHashMap<>();

    // 把房间放到房间管理器中
    public void addRoom(Room room) {
        rooms.put(room.getRoomId(), room);
    }

    // 把房间从房间管理器中移除
    public void removeRoom(String roomId) {
        if(getRoom(roomId)==null){
            return;
        }
        rooms.remove(roomId);
    }

    // 根据房间 id 查找对应的房间
    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    private RoomManager() {}
    private static RoomManager roomManager = new RoomManager();
    public static RoomManager getInstance() {
        return roomManager;
    }
}
