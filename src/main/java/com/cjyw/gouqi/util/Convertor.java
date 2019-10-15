/*
 * @(#)DataConvertor.java
 * 
 * Create Version: 1.0.0
 * Author: Leo Ding
 * Create Date: 2014-08-08
 * 
 * Copyright (c) 2014 dingyahui. All Right Reserved.
 */
package com.cjyw.gouqi.util;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * 数据转换工具类
 */
public class Convertor {
    
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', 
                                             '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String HEX_STRING = "0123456789abcdef";

    /**
     * 将字节数组转换为int值。
     * 
     * @param   b           字节数组
     * @param   bo          字节顺序，要么是BIG_ENDIAN，要么是LITTLE_ENDIAN。如果传null，则使用默认值BIG_ENDIAN。
     * 
     * @return  int值
     */
    public static int bytesToInt(byte[] b, ByteOrder bo) {
        
        ByteBuffer buf = ByteBuffer.wrap(b);
        if (bo != null) buf.order(bo);
        
        return buf.getInt();
    }
    
    /**
     * 将字节数组转换为十六进制字符串。
     * 
     * @param   b           字节数组
     * 
     * @return  十六进制字符串（小写）
     */
    public static String bytesToHex(byte[] b) {
        return b == null ? null : bytesToHex0(b).toString();
    }

    public static String bytesToBinary(byte[] b) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            result.append(StringUtils.leftPad(Long.toString(b[i] & 0xff, 2), 8, '0'));
        }
        return result.toString();
     }

    /**
     * 将字节数组转换为十六进制字符串。
     * 
     * @param b             字节数组
     * @return  十六进制字符串（小写）
     */
    public static StringBuilder bytesToHex0(byte[] b) {
        if (b == null) return null;
        
        int len = b.length;
        StringBuilder result = new StringBuilder(len * 2);
        
        for (int i = 0; i < len; i++) {
            result.append(HEX_CHARS[(b[i] >> 4) & 0x0f]);
            result.append(HEX_CHARS[b[i] & 0x0f]);
        }
        
        return result;
    }


}