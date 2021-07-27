package util;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {

    private static final MysqlDataSource DS = new MysqlDataSource();

    static {
        DS.setURL("jdbc:mysql://localhost:3306/chess");
        DS.setUser("root");
        DS.setPassword("password");
        DS.setUseSSL(false);//https安全协议，这里是去掉
        DS.setCharacterEncoding("UTF-8");
    }

    public static Connection getConnection() throws SQLException {
        return DS.getConnection();
    }

    public static void main(String[] args) throws SQLException {
        System.out.println(getConnection());
    }
    public static void close(Connection c, PreparedStatement ps, ResultSet rs) throws SQLException {
        if(rs != null)
            rs.close();
        if(ps !=null)
            ps.close();
        if(c !=null)
            c.close();
    }

    public static void close(Connection c, PreparedStatement ps) throws SQLException {
        close(c,ps,null);
    }
}
