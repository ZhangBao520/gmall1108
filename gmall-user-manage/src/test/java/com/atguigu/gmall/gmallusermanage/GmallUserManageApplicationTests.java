package com.atguigu.gmall.gmallusermanage;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.gmallusermanage.service.UserInfoService;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallUserManageApplicationTests {

	@Autowired
	UserInfoService userInfoService;

	@Test
	public void showAddressList() {
		List<UserAddress> userAddressList = userInfoService.getUserAddressList("2");
		for (UserAddress userAddress : userAddressList) {
			System.err.println("userAddress = " + userAddress);
		}

	}



}
