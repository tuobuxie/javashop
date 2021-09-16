package com.enation.app.shop.core.goods.utils;


import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class AmountUtils {
    public static DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public static DecimalFormat dFormat = new DecimalFormat("#");

    /**
     * 1、计算手续费<br>
     *
     * @param amount
     * @param rate
     * @return
     * @see
     */
    public static String getRateAmount(String amount, String rate) {
        String transformRate = AmountUtils.dividePercent(rate);
        BigDecimal b1 = new BigDecimal(transformRate);
        BigDecimal b2 = new BigDecimal(amount);
        Double rateAmount = AmountUtils.format(b1.multiply(b2).doubleValue());
        if (!isgtZero(rateAmount)) {
            // 如果算出计费不大于零，则默认一分钱
            rateAmount = Double.valueOf("0.01");
        }
        return decimalFormat.format(rateAmount);
    }

    /**
     * 1、金额转换保留两位小数点<br>
     *
     * @param amount
     * @return
     * @see
     */
    public static String getDoubleAmount(String amount) {
        Double amountDouble = Double.parseDouble(amount);
        return decimalFormat.format(amountDouble);
    }

    // s1-s2
    public static String getSubtractAmount(String s1, String s2) {
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        return decimalFormat.format(subtract(d1, d2));
    }

    // s1+s2
    public static String getAddAmount(String s1, String s2) {
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        return decimalFormat.format(add(d1, d2));
    }

    // s1+s2+S3
    public static String getAddAmount(String s1, String s2, String s3) {
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        Double d3 = Double.parseDouble(s3);
        return decimalFormat.format(add(add(d1, d2), d3));
    }

    // s1>0
    public static boolean isgtZeroToStr(String s1) {
        Double d1 = Double.parseDouble(s1);
        return compare(d1, ZERO) > 0;
    }

    // 分转元
    public static String getAmountToYuan(String amount) {
        BigDecimal d1 = new BigDecimal(amount);
        BigDecimal d2 = new BigDecimal("100");
        return decimalFormat.format(divide(d1, d2).doubleValue());
    }

    // 元转分
    public static String getAmountToPenny(String amount) {
        BigDecimal d1 = new BigDecimal(amount).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
        BigDecimal d2 = new BigDecimal("100");
        return dFormat.format(d1.multiply(d2));
    }

    // 元转分
    public static Long getAmountToPennyTwo(Long amount) {
        BigDecimal d1 = new BigDecimal(amount).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
        BigDecimal d2 = new BigDecimal("100");
        return Long.valueOf(dFormat.format(d1.multiply(d2)));
    }

    private static final double ZERO = 0.0001;
    private static final int SCALE = 2;

    /**
     * 判断金额是否相等
     */
    public static boolean equals(String s1, String s2) {
        if (StringUtils.isBlank(s1) || StringUtils.isBlank(s1)) {
            return false;
        }
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        if (d1 == null || d2 == null) {
            return false;
        }

        return Math.abs(d1 - d2) < ZERO;
    }


    public static double format(double value) {
        return new BigDecimal(Double.toString(value)).setScale(SCALE, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static int compare(double d1, double d2) {
        return new BigDecimal(Double.toString(d1)).setScale(SCALE, BigDecimal.ROUND_HALF_UP).compareTo(
                new BigDecimal(Double.toString(d2)).setScale(SCALE, BigDecimal.ROUND_HALF_UP));
    }

    public static boolean gt(String s1, String s2) {
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        return compare(d1, d2) > 0;
    }


    public static boolean et(String s1, String s2) {
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        return compare(d1, d2) == 0;
    }


    public static boolean ne(String s1, String s2) {
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        return compare(d1, d2) != 0;
    }


    public static boolean lt(String s1, String s2) {
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        return compare(d1, d2) < 0;
    }

    public static boolean ge(String s1, String s2) {
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        return compare(d1, d2) >= 0;
    }

    public static boolean le(String s1, String s2) {
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        return compare(d1, d2) <= 0;
    }


    public static Double add(Double d1, Double d2) {
        return new Double(AmountUtils.format(d1.doubleValue() + d2.doubleValue()));
    }


    public static Double subtract(Double d1, Double d2) {
        return new Double(AmountUtils.format(d1.doubleValue() - d2.doubleValue()));
    }

    public static boolean isgtZero(double d1) {
        return compare(d1, ZERO) > 0;
    }

    public static boolean isgtZero(Double d1) {
        if (d1 == null)
            return false;
        return compare(d1, ZERO) > 0;
    }

    public static BigDecimal multiply(double d1, double d2) {
        String s1 = String.valueOf(d1);
        String s2 = String.valueOf(d2);
        BigDecimal b1 = new BigDecimal(s1);
        BigDecimal b2 = new BigDecimal(s2);
        BigDecimal bb = b1.multiply(b2);
        return bb;
    }

    public static BigDecimal divide(double d1, double d2, int scale) {
        String s1 = String.valueOf(d1);
        String s2 = String.valueOf(d2);
        BigDecimal b1 = new BigDecimal(s1);
        BigDecimal b2 = new BigDecimal(s2);

        BigDecimal bb = b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
        return bb;
    }

    public static BigDecimal divide(double d1, double d2) {
        String s1 = String.valueOf(d1);
        String s2 = String.valueOf(d2);
        BigDecimal b1 = new BigDecimal(s1);
        BigDecimal b2 = new BigDecimal(s2);

        BigDecimal bb = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
        return bb;
    }

    public static BigDecimal divide(BigDecimal d1, BigDecimal d2, int scale) {
        BigDecimal bb = d1.divide(d2, scale, BigDecimal.ROUND_HALF_UP);
        return bb;
    }

    public static BigDecimal divide(BigDecimal d1, BigDecimal d2) {
        BigDecimal bb = d1.divide(d2, 2, BigDecimal.ROUND_HALF_UP);
        return bb;
    }

    /**
     * 保留两位小数金额 注意慎用
     */
    public static String retainTwo(String amount) {
        DecimalFormat df = new DecimalFormat("0.00");

        return df.format(Double.parseDouble(amount));
    }


    public static String dividePercent(String percent) {
        DecimalFormat df = new DecimalFormat("#.#####");
        percent = percent.replace("%", "");
        BigDecimal d1 = new BigDecimal(percent);
        BigDecimal d2 = new BigDecimal("100");
        return df.format(d1.divide(d2).doubleValue());
    }

    /**
     * 百分号数字相加
     **/
    public static String getAddPercent(String percent1, String percent2) {
        DecimalFormat df = new DecimalFormat("#.#####");
        percent1 = percent1.replace("%", "");
        percent2 = percent2.replace("%", "");
        BigDecimal d1 = new BigDecimal(percent1);
        BigDecimal d2 = new BigDecimal(percent2);
        BigDecimal d3 = new BigDecimal("100");
        BigDecimal d4 = new BigDecimal(Double.toString(d1.doubleValue() + d2.doubleValue()));
        return df.format(d4.divide(d3).doubleValue());

    }

    /**
     * 百分号比例金额处理
     **/
    public static String getPercentAmount(String amount, String percent) {
        BigDecimal b1 = new BigDecimal(dividePercent(percent));
        BigDecimal b2 = new BigDecimal(amount);
        Double rateAmount = AmountUtils.format(b1.multiply(b2).doubleValue());
        return decimalFormat.format(rateAmount);
    }

    /**
     * 第一位是被除数，第二位是除数
     **/
    public static String divideStr(String dividend, String divider) {
        BigDecimal b1 = new BigDecimal(dividend);
        BigDecimal b2 = new BigDecimal(divider);
        // 这里是保留4位小数
        BigDecimal bb = b1.divide(b2, 4, BigDecimal.ROUND_HALF_UP);
        return Double.toString(bb.doubleValue());
    }


    public static String getPercentStr(String dividend, String divider) {
        DecimalFormat df = new DecimalFormat("#.##");
        BigDecimal b1 = new BigDecimal(divideStr(dividend, divider));
        BigDecimal b2 = new BigDecimal("100");
        return df.format(b1.multiply(b2).doubleValue()).concat("%");
    }
}
