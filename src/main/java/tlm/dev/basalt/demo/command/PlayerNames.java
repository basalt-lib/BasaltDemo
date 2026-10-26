package tlm.dev.basalt.demo.command;

import java.util.regex.Pattern;

final class PlayerNames {

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

    private PlayerNames() {
    }

    static boolean valid(String name) {
        return name != null && NAME.matcher(name).matches();
    }
}
