import java.io.DataOutputStream;
import java.io.IOException;

public class StoreController {
    public static boolean isValidId(String id) {
        return ValidInputs.ID.isMatch(id);
    }

    public static boolean isValidName(String name) {
        return ValidInputs.NAME.isMatch(name);
    }

    public static boolean isValidMoney(String moneyStr) {
        return ValidInputs.MONEY.isMatch(moneyStr);
    }

    public static boolean isValidProductName(String productName) {
        for(int i = 1; i <= 4; i++) {
            if(productName.equals("shoe" + i)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidQuantity(String quantityStr) {
        return ValidInputs.QUANTITY.isMatch(quantityStr);
    }
    public static int getPrice(String productName) {
        switch (productName) {
            case "shoe1":
                return 100;
            case "shoe2":
                return 200;
            case "shoe3":
                return 300;
            case "shoe4":
                return 400;
            default:
                return 0;
        }
    }
}
