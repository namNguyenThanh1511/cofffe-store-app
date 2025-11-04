package namnt.vn.coffestore.utils;

import java.text.DecimalFormat;
public class CurrencyUtils {
    /**
     * Định dạng giá tiền (đơn vị đầu vào đã là VNĐ)
     */
    public static String formatPrice(double priceInVND) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(priceInVND) + "₫";
    }

    /**
     * Định dạng giá tiền VNĐ với prefix
     */
    public static String formatPriceWithPrefix(double priceInVND) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return "VNĐ " + formatter.format(priceInVND);
    }
}
