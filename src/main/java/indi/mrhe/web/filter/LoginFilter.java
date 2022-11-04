package indi.mrhe.web.filter;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        //判断访问资源路径是否和登录注册相关
        //1,在数组中存储登陆和注册相关的资源路径
        String[] urls = {"/login.html", "register.html", "/imgs/", "/css/", "/user/", "/element-ui/", "/js/"};
        //2,获取当前访问的资源路径
        String url = req.getRequestURL().toString();

        //3,遍历数组，获取到每一个需要放行的资源路径
        for (String u : urls) {
            //4,判断当前访问的资源路径字符串是否包含要放行的的资源路径字符串
            /*
                比如当前访问的资源路径是  /brand-demo/login.jsp
                而字符串 /brand-demo/login.jsp 包含了  字符串 /login.jsp ，所以这个字符串就需要放行
            */
            if (url.contains(u)) {
                //找到了，放行
                chain.doFilter(request, response);
                //break;
                return;
            }
        }

        //1. 判断session中是否有user
        HttpSession session = req.getSession();
        Object user = session.getAttribute("user");

        //2. 判断user是否为null
        if (user != null) {
            // 登录过了
            //放行
            chain.doFilter(request, response);
        } else {
            // 没有登陆，存储提示信息，跳转到登录页面
            //1. 创建Cookie对象,存储没有登入的信息
            Cookie c_loginStatus = new Cookie("loginStatus", "notLoggedIn");
            c_loginStatus.setPath("/"); // 默认路径
            //2. 发送
            resp.addCookie(c_loginStatus);
            // req.setAttribute("login_msg", "您尚未登陆！");
            // req.getRequestDispatcher("/login.jsp").forward(req, response);
            //重定向到欢迎界面
            resp.sendRedirect("http://localhost:8080/brand-case/login.html");
        }
    }

    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }
}
