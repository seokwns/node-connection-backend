package node.connection.jwt;


import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JweDecoder {

    private static final String INFO_PREFIX = "NextAuth.js Generated Encryption Key";

    // HKDF를 사용하여 키를 유도하는 메서드
    public static byte[] hkdf(byte[] ikm, byte[] salt, byte[] info, int length) throws Exception {
        // HKDF 초기화
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        HKDFParameters params = new HKDFParameters(ikm, salt, info);
        hkdf.init(params);

        byte[] result = new byte[length];
        hkdf.generateBytes(result, 0, result.length);
        return result;
    }

    // JavaScript의 getDerivedEncryptionKey에 해당하는 메서드
    public static KeyParameter getDerivedEncryptionKey(String keyMaterial, String salt) throws Exception {
        byte[] ikm = keyMaterial.getBytes(StandardCharsets.UTF_8);
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        byte[] info = (INFO_PREFIX + (salt.isEmpty() ? "" : " (" + salt + ")")).getBytes(StandardCharsets.UTF_8);
        byte[] derivedKey = hkdf(ikm, saltBytes, info, 32); // 32 bytes for A256GCM
        return new KeyParameter(derivedKey);
    }

    // JWE 디코딩 메서드
    public static String decodeJwe(String jweString, KeyParameter encryptionKey) throws Exception {
        JWEObject jweObject = JWEObject.parse(jweString);
        DirectDecrypter decrypter = new DirectDecrypter(encryptionKey.getKey());
        jweObject.decrypt(decrypter);
        return jweObject.getPayload().toString();
    }
}