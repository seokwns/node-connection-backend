package node.connection._core.utils;

import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;

public class Hash {

    @Value("${security.jwe.secret}")
    private static String CHARACTERS;

    private static final SecureRandom random = new SecureRandom();

    public static final int DEFAULT_LENGTH = 8;


    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public static String generate(int length) {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(index));
        }

        return builder.toString();
    }
}
