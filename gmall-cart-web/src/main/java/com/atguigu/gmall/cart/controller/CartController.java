package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.gmallusermanage.service.CartService;
import com.atguigu.gmall.gmallusermanage.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Autowired
    CartCookieHandler cartCookieHandler;

    @Reference
    ManageService manageService;

    @RequestMapping(value = "addToCart",method = RequestMethod.POST)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        //1 获得参数 userId skuId skuNum
        String skuId=request.getParameter("skuId");
        String skuNum=request.getParameter("skuNum");

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        String userId = (String)request.getAttribute("userId");
        if(userId!=null){
            cartService.addToCart(skuId,userId, Integer.parseInt(skuNum) );
        }else{
            cartCookieHandler.addToCart(request,response,skuId,userId, Integer.parseInt(skuNum));
        }

        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);


        return "success";

    }
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public  String cartList(HttpServletRequest request,HttpServletResponse response){
        //1 检查检查登录状态
        String userId=(String)request.getAttribute("userId");


        //有userId 从后台（redis 或者 mysql）获取
        if(userId!=null){
            List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
            List<CartInfo> cartList =null;
            if(cartListFromCookie!=null&&cartListFromCookie.size()>0){
                //1 合并到后台
                cartList = cartService.mergeToCartList(cartListFromCookie, userId);
                //2 cookie中的删除掉
                cartCookieHandler.deleteCartList(request,response);
            }else{
                cartList= cartService.getCartList(userId);
            }

            request.setAttribute("cartList",cartList);
        }else{
            //没有userId从cookie中读取
            List<CartInfo> cartList = cartCookieHandler.getCartList(request);
            request.setAttribute("cartList",cartList);
        }
        return "cartList";

    }
    @RequestMapping(value = "checkCart",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public  void checkCart(HttpServletRequest request){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");

        String userId=(String) request.getAttribute("userId");
        if(userId!=null){
            cartService.checkCart(skuId,isChecked,userId);
        }
        return;

    }
    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        String userId =(String) request.getAttribute("userId");
        List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
        if(cartListFromCookie!=null&&cartListFromCookie.size()>0){
            //1 合并到后台
            List<CartInfo> cartList = cartService.mergeToCartList(cartListFromCookie, userId);
            //2 cookie中的删除掉
            cartCookieHandler.deleteCartList(request,response);
        }
        return "redirect://order.gmall.com/trade";
    }
}
