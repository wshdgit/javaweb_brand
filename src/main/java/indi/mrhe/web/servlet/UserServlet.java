package indi.mrhe.web.servlet;

import com.alibaba.fastjson.JSON;
import indi.mrhe.util.CheckCodeUtil;
import indi.mrhe.pojo.User;
import indi.mrhe.service.UserService;
import indi.mrhe.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/user/*")
public class UserServlet extends BaseServlet {
    private UserService userService = new UserServiceImpl();

    public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //1. 接收用户数据
        BufferedReader br = request.getReader();
        String params = br.readLine();//json字符串

        String remember = request.getParameter("remember");

        //转为User对象
        User user = JSON.parseObject(params, User.class);

        //2. 调用service查询
        user = userService.login(user.getUsername(), user.getPassword());

        //3. 判断
        if (user != null) {
            //登录成功

            //判断用户是否勾选记住我，字符串写前面是为了避免出现空指针异常
            if ("true".equals(remember)) {
                //勾选了，发送Cookie
                //1. 创建Cookie对象
                Cookie c_username = new Cookie("username", user.getUsername());
                Cookie c_password = new Cookie("password", user.getPassword());
                // String contextPath = request.getContextPath();  // 当前项目的根目录
                c_username.setPath("/"); // 默认路径
                c_password.setPath("/"); // 默认路径
                // 设置Cookie的存活时间
                c_username.setMaxAge(60 * 60 * 24 * 7);
                c_password.setMaxAge(60 * 60 * 24 * 7);
                //2. 发送
                response.addCookie(c_username);
                response.addCookie(c_password);
            }

            //将登陆成功后的user对象，存储到session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            // 响应成功的标识
            response.getWriter().write("success");
        } else {
            // 登录失败,
            // 响应失败的标识
            response.getWriter().write("failure");
        }
    }

    public void selectByUsername(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //1. 接收用户名
        String username = request.getParameter("username");
        //2. 调用service查询User对象，此处不进行业务逻辑处理，直接给 flag 赋值为 true，表明用户名占用
        boolean flag = userService.selectByUsername(username);
        //3. 响应标记
        response.getWriter().write("" + (flag ? "success" : "failure"));
    }

    public void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取用户输入的验证码
        String checkCode = request.getParameter("checkCode");

        // 程序生成的验证码，从Session获取
        HttpSession session = request.getSession();
        String checkCodeGen = (String) session.getAttribute("checkCodeGen");

        // 比对
        if (!checkCodeGen.equalsIgnoreCase(checkCode)) {
            // 验证码错误返回验证码错误失败
            //注册失败，跳转到注册页面
            response.getWriter().write("verificationCodeError");
            // 不允许注册
            return;
        }
        //1. 接收用户数据
        BufferedReader br = request.getReader();
        String params = br.readLine();//json字符串

        //转为User对象
        User user = JSON.parseObject(params, User.class);

        //2. 调用service 注册
        boolean flag = userService.register(user);
        //3. 判断注册成功与否
        if (flag) {
            //注册功能，返回success
            response.getWriter().write("success");
        } else {
            //注册失败，返回失败
            response.getWriter().write("failure");
        }
    }

    public void verificationCode(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 要在Response响应(response.getOutputStream())之前进行Session创建(request.getSession())
        // 如果在Response响应后创建Session会抛出异常
        // (因为那时候服务器已经将数据发送到客户端了，即：就无法发送Session ID 了)
        HttpSession session = request.getSession();
        // 生成验证码
        ServletOutputStream os = response.getOutputStream();
        String checkCode = CheckCodeUtil.outputVerifyImage(100, 50, os, 4);

        // 存入Session
        session.setAttribute("checkCodeGen", checkCode);
    }

    public void getUsername(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //将登陆成功后的user对象，存储到session
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user != null)
            // 用户已经登入则返回用户名
            response.getWriter().write(user.getUsername());
        else
            // 用户未登入返回空字符串
            response.getWriter().write("");
    }

    public void signOut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        // 1.将Session中用户信息销毁
        // session.removeAttribute("user");
        // 销毁session
        session.invalidate();
        // 2.返回成功标识
        response.getWriter().write("success");
    }

    public void removeAccount(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            String username = user.getUsername();
            userService.delete(username);
            // 将Session中用户信息销毁或者销毁Session
            // session.removeAttribute("user");
            session.invalidate();
            //返回成功
            response.getWriter().write("success");
        } else {
            // 返回失败
            response.getWriter().write("failure");
        }
    }
}
