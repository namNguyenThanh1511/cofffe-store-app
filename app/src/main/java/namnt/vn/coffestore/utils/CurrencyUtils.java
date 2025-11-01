package namnt.vn.coffestore.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    
    // Tỷ giá USD sang VNĐ (có thể điều chỉnh)
    private static final double USD_TO_VND_RATE = 25000;
    
    /**
     * Format giá tiền theo VNĐ
     * @param priceInUSD Giá theo USD
     * @return Chuỗi định dạng VNĐ (ví dụ: "100.000₫")
     */
    public static String formatPrice(double priceInUSD) {
        double priceInVND = priceInUSD * USD_TO_VND_RATE;
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(priceInVND) + "₫";
    }
    
    /**
     * Format giá tiền theo VNĐ với prefix
     * @param priceInUSD Giá theo USD
     * @return Chuỗi định dạng VNĐ với prefix (ví dụ: "VNĐ 100.000")
     */
    public static String formatPriceWithPrefix(double priceInUSD) {
        double priceInVND = priceInUSD * USD_TO_VND_RATE;
        DecimalFormat formatter = new DecimalFormat("#,###");
        return "VNĐ " + formatter.format(priceInVND);
    }
    
    /**
     * Chuyển đổi USD sang VNĐ
     * @param priceInUSD Giá theo USD
     * @return Giá theo VNĐ
     */
    public static double convertToVND(double priceInUSD) {
        return priceInUSD * USD_TO_VND_RATE;
    }
}
