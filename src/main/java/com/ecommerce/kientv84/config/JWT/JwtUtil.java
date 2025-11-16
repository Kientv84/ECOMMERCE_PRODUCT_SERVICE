package com.ecommerce.kientv84.config.JWT;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;

    @Autowired
    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    // Tạo token
    public String generateToken(String email) {
        return JWT.create() // – tạo một builder để xây JWT.
                .withSubject(email)
                .withIssuer(jwtConfig.getIssuer()) // định danh ai tạo token
                .withIssuedAt(new Date()) // Thời gian token được created
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtConfig.getExpiration())) //thời điểm token hết hạn. Lấy thời gian hiện tại + thời gian cấu hình.
                .sign(Algorithm.HMAC256(jwtConfig.getSecret())); // ký token bằng thuật toán HMAC-SHA256 và secret key
    }

    // Xác thực token và trả về email
    public String validateToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret())) //yêu cầu token phải được ký bằng thuật toán HMAC256 và đúng secret
                .withIssuer(jwtConfig.getIssuer()) //hỉ chấp nhận token có issuer đúng như cấu hình
                .build()
                .verify(token); //kiểm tra tính hợp lệ của token:

        return decodedJWT.getSubject(); // email
    }

    public String generateRefreshToken(String email) {
        long refreshExpiration = 7 * 24 * 60 * 60 * 1000; // 7 days

        return JWT.create()
                .withSubject(email)
                .withIssuer(jwtConfig.getIssuer())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshExpiration))
                .sign(Algorithm.HMAC256(jwtConfig.getSecret()));
    }

    public String validateRefreshToken(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret()))
                .withIssuer(jwtConfig.getIssuer())
                .build()
                .verify(token);

        return decodedJWT.getSubject();
    }

    //Cấu trúc cua 1 JWT: <Header>.<Payload>.<Signature>
    // HEADER {
    //  "alg": "HS256",
    //  "typ": "JWT"
    //}

    // PAYLOAD {
    //| Phương thức           | Nội dung trong Payload                          |
    //| --------------------- | ----------------------------------------------- |
    //| `.withSubject(email)` | `sub` – Subject (người dùng đại diện cho token) |
    //| `.withIssuer()`       | `iss` – Ai phát hành token                      |
    //| `.withIssuedAt()`     | `iat` – Ngày giờ phát hành                      |
    //| `.withExpiresAt()`    | `exp` – Ngày giờ hết hạn                        |

    // SIGNATURE: HMACSHA256(
    //  base64UrlEncode(header) + "." + base64UrlEncode(payload),
    //  secret
    //)
}