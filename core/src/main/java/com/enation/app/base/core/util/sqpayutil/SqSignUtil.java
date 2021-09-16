package com.enation.app.base.core.util.sqpayutil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqSignUtil {
    /**
     * =
     */
    public static final String QSTRING_EQUAL = "=";
    /**
     * &
     */
    public static final String QSTRING_SPLIT = "&";

    /**
     * 1、MD5签名前的数据处理<br>
     * 2、…<br>
     *
     * @param map
     * @param md5Key
     * @return
     * @see
     */
    public static String md5SignValue(Map<String, String> map, String md5Key) {
        StringBuffer b = orderMapStr(map);
        b.append(Md5Utils.md5HexUpper(md5Key));
        return Md5Utils.md5HexUpper(b.toString());
    }

    public static String verifyMd5Value(Map<String, String> map, String md5Key) {
        HashMap<String, String> data = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ("MD5info".equals(key)) {
                continue;
            }
            data.put(key, value);
        }
        return md5SignValue(data, md5Key);
    }

    /**
     * RSA签名前的字符串
     *
     * @param map
     * @return
     */
    public static String rsaSignValue(Map<String, String> map) {
        StringBuffer b = orderMapStr(map);
        return b.toString().substring(0, b.length() - 1);
    }
    /**
     * 处理排序问题
     *
     * @param map
     * @return
     */
    public static StringBuffer orderMapStr(Map<String, String> map) {
        StringBuffer b = new StringBuffer();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            b.append(entry.getKey());
            b.append(QSTRING_EQUAL);
            if (entry.getValue() != null) {
                b.append(entry.getValue());
            }
            b.append(QSTRING_SPLIT);
        }
        return b;
    }
}
