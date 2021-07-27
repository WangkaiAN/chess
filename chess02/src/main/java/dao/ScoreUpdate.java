package dao;

import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ScoreUpdate {

    public static int query(int id) throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;
        try {
            //1.创建数据库连接Connection
            c = DBUtil.getConnection();
            //2.创建操作命令对象Statement
            String sql = "update user set integral=integral+1 where id=?";
            ps = c.prepareStatement(sql);
            //3: 替换占位符+执行sql
            ps.setInt(1, id);

            return ps.executeUpdate();

        } finally {
            //5.释放资源
            DBUtil.close(c, ps);
        }
    }
}
