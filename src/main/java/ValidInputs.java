import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ValidInputs {
    REGISTER("register:(?<id>.+):(?<name>.+):(?<money>.+)"),
    LOGIN("login:(?<id>.+)"),
    GET_PRICE("get price:(?<shoename>.+)"),
    GET_QUANTITY("get quantity:(?<shoename>.+)"),
    GET_MONEY("get money"),
    CHARGE("charge:(?<money>.+)"),
    PURCHASE("purchase:(?<shoename>.+):(?<quantity>.+)"),
    LOGOUT("logout"),
    ID("[0-9]+"),
    MONEY("[0-9]+"),
    QUANTITY("[0-9]+"),
    ;
    final String regex;
    ValidInputs(String regex) {
        this.regex = regex;
    }
    private Matcher getMatcher(String input) {
        Pattern pattern = Pattern.compile(this.regex);
        Matcher matcher = pattern.matcher(input);
        matcher.matches();
        return matcher;
    }
    public boolean isMatch(String input) {
        return getMatcher(input).matches();
    }

    public String getGroup(String input, String group) {
        return getMatcher(input).group(group);
    }

}
