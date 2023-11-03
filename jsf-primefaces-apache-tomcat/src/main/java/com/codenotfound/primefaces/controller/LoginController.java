package com.codenotfound.primefaces.controller;

import com.codenotfound.primefaces.model.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

@ManagedBean
public class LoginController {
    @ManagedProperty(value="#{user}")
    private User user;

    public String login() {
        // Kiểm tra thông tin đăng nhập
        if ("acc".equals(user.getUsername()) && "acc".equals(user.getPassword())) {
            // Lấy session
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);

            // Đặt thông tin người dùng vào session
            session.setAttribute("loggedInUser", user);

            return "success"; // Chuyển hướng đến trang thành công
        } else {
            return "failure"; // Xử lý khi đăng nhập thất bại
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
