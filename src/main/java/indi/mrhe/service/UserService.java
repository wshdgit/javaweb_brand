package indi.mrhe.service;

import indi.mrhe.pojo.User;

public interface UserService {
    /**
     * 登录方法
     *
     * @param username
     * @param password
     * @return
     */
    public User login(String username, String password);

    /**
     * 查看用户名是否存在
     *
     * @param username
     * @return
     */
    public boolean selectByUsername(String username);

    /**
     * 注册方法
     *
     * @return
     */

    public boolean register(User user);

    /**
     * 删除方法
     *
     * @return
     */

    public void delete(String username);
}
