package com.itsnow.utils;

import com.itsnow.domain.dto.UserDTO;

/**
 * @author itsnow
 * @date 2026/4/25
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
