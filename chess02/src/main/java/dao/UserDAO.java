package dao;

import model.User;
import util.DBUtil;

import java.sql.*;

public class UserDAO {

    public static User query(User input) throws SQLException {
        //1.创建数据库连接Connection
        Connection c = DBUtil.getConnection();

        //2.创建操作命令对象Statement
        String sql = "select * from user where id=? and password=? ";
        PreparedStatement ps = c.prepareStatement(sql);

        //3.执行SQL，替换占位符，之后再执行
        ps.setInt(1,input.getId());
        ps.setString(2,input.getPassword());
        ResultSet rs = ps.executeQuery();

        User query = null;
        //4.如果是查询操作，处理结果集
        while(rs.next()){//移动到下一行，有数据就返回true
            query = new User();
            query.setId(rs.getInt("id"));
            query.setPassword(rs.getString("password"));
            query.setIntegral(rs.getInt("integral"));
        }

        //5.释放资源
        DBUtil.close(c, ps, rs);

        return query;
    }

}

