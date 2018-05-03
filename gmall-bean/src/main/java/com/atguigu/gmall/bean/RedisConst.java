package com.atguigu.gmall.bean;

public class RedisConst {

    public static final String SKUKEY_PREFIX = "sku" ;

    public static final String SKUKEY_SUFFIX = "info";
    public static final String SKULOCK_SUFFIX = "lock";
    public static final long SKULOCK_EXPIRE_PX = 10000;

    public static final int SKUKEY_TIMEOUT = 24*60*60;
}
