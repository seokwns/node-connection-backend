package node.connection.service;




import node.connection.entity.UserEntity;
import node.connection.jwt.JweDecoder;
import node.connection.repository.UserRepository;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JweDecoder jweDecoder;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱을 위한 ObjectMapper

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JweDecoder jweDecoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jweDecoder = jweDecoder;
    }

    public boolean authenticateUserWithJwe(String jweToken) throws Exception {
        // 비밀키 및 소금 설정
        String secret = "qnK6e3llKcOCVCcnMNhux5KhGIUm3MgfezS4MhvoKoI="; // 설정된 키 재료
        String salt = ""; // 설정된 소금

        // 파생된 암호화 키 생성
        KeyParameter encryptionKey = JweDecoder.getDerivedEncryptionKey(secret, salt);

        // JWE 토큰 디코딩
        try {
            String decodedPayload = JweDecoder.decodeJwe(jweToken, encryptionKey);
            // 디코딩된 페이로드에서 전화번호와 비밀번호 추출
            JsonNode payloadNode = objectMapper.readTree(decodedPayload);
            String phone = payloadNode.get("iat").asText();
            String password = payloadNode.get("exp").asText();

            // 전화번호로 사용자 조회
            UserEntity user = userRepository.findByPhonenum(phone);


            // 비밀번호 검증
            if (user != null && bCryptPasswordEncoder.matches(password, user.getPassword())) {
                return true;  // 인증 성공
            }

            return false;  // 인증 실패


        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 500 오류 발생 원인 파악
            e.printStackTrace();
            throw new RuntimeException("JWE decoding failed", e);
        }
    }
}