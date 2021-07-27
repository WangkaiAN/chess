package Servlet;

import dao.UserDAO;
import model.JSONResponse;
import model.User;
import util.JSONUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        //解析请求数据（请求数据类型不同，调用API不同）
        User input = JSONUtil.deserialize(req.getInputStream(),User.class);

        //处理业务，数据库增删查改
        User query = null;
        try {
            query = UserDAO.query(input);
        } catch (SQLException e) {
            throw new RuntimeException("数据库操作失败mysql error");
        }

        //返回响应数据（json）
        JSONResponse json = new JSONResponse();
        if(query == null){//根据输入的账号密码查不到
            json.setCode("00001");
            json.setMessage("密码或用户名错误");
        }else {
            json.setSuccess(true);
            json.setCode("200");
            json.setData(query.getIntegral());
            HttpSession session = req.getSession();//获取不到就创建
            //保存用户信息
            session.setAttribute("user",query);
        }
        //json.setSuccess(true);
        PrintWriter pw = resp.getWriter();
        pw.println(JSONUtil.serialize(json));
    }
}

