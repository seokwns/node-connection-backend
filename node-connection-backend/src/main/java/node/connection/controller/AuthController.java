package node.connection.controller;

import node.connection.jwt.JweDecoder;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {


    private static final String SECRET = "zm4BqNdeg5dz8sfRuuVbsxdJqXas2b5gse6JTT70hns=";
    private static final String SALT = "";

    @PostMapping("/decode-jwe")
    public String decodeJweToken(@RequestBody String jweToken) {
        try {
            // Derive the encryption key
            KeyParameter encryptionKey = JweDecoder.getDerivedEncryptionKey(SECRET, SALT);

            // Decode the JWE token
            return JweDecoder.decodeJwe(jweToken, encryptionKey);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error decoding token: " + e.getMessage();
        }
    }
}