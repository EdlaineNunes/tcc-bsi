//package com.tcc.edlaine.crosscutting.utils;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//
//import static org.apache.commons.lang3.StringUtils.isNotBlank;
//
public abstract class MoneyUtils {
//
//    public static final BigDecimal NO_VALUE = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
//
//    public static BigDecimal convertCentsStringToBigDecimal(String str) {
//        if (isNotBlank(str)) {
//            return new BigDecimal(str).setScale(2, RoundingMode.HALF_DOWN)
//                    .divide(BigDecimal.valueOf(100), RoundingMode.HALF_DOWN);
//        }
//        return BigDecimal.ZERO;
//    }
//
//    public static String convertMoneyToCents(String value) {
//        if(isNotBlank(value)) {
//            return new BigDecimal(value).setScale(2, RoundingMode.HALF_DOWN)
//                    .toString()
//                    .replace(".", "");
//        }
//        return "0";
//    }
}
