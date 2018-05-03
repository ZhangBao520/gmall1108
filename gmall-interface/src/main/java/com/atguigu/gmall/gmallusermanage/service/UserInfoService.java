package com.atguigu.gmall.gmallusermanage.service;


import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {
    public List<UserInfo> getUserInfoList(UserInfo userInfoQuery);

    public UserInfo getUserInfo(UserInfo userInfoQuery);

    public void delete(UserInfo userInfoQuery);

    public void addUserInfo(UserInfo userInfo);

    public void updateUserInfo(UserInfo userInfo);

    public List<UserAddress> getUserAddressList(String userId);

    public UserInfo login(UserInfo userInfo);

    public  UserInfo verify(String userId);
}
