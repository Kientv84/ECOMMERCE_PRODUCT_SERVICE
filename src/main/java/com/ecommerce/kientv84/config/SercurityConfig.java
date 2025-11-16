package com.ecommerce.kientv84.config;

import com.ecommerce.kientv84.config.JWT.JwtAuthenticationFilter;
import org.apache.catalina.filters.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


//@Configuration đánh dấu đây là class config để quản lý các bean
@Configuration
public class SercurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    //Anotation @Bean dùng để đánh dấu một method trả v một object để spring container nhận thấy và quản lý, dùng để custom Bean
    // Lúc này chúng ta có thể sd anotation @Autowired để inject và sử dụng
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Default hash by 10
    }


    // Cấu hình filter chain để tắt password mặc định của Spring Security
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // SecurityFilterChain đại diện cho clas cấu hình bảo mật (Sercurity) trong Spring sercurity 6+
        // filterChain() chính là nơi bạn cấu hình cho phép hay chặn các request HTTP.
        // dùng http để cấu hình: Cho phép/không cho phép truy cập. Bảo mật password. Tắt bật các chức năng bảo mật như CSRF, CORS...
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF nếu là API, CSRF = Cross Site Request Forgery (Giả mạo request).
                //với REST API không dùng cookies hoặc form, CSRF không cần thiết và nên tắt để tránh lỗi khi gọi API.
                .cors(Customizer.withDefaults())
                //Được bật mặc định trong Spring Security, nhưng khi bạn làm REST API thì nên tắt đi.
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth //cấu hình quyền truy cập cho các URL endpoint.
                        .requestMatchers( "/authentication/login").permitAll() // cho phép gọi không cần login, Tất cả các request bắt đầu bằng /system_user/ và /auth/ sẽ được phép truy cập mà không cần login.
                        .anyRequest().authenticated() // các endpoint khác cần login, Các request khác bắt buộc phải đăng nhập (có xác thực).
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) //đảm bảo rằng filter xử lý token được thực thi trước khi Spring xác thực bằng username/password mặc định.
                .httpBasic(Customizer.withDefaults()); // có thể dùng hoặc không, Giúp bạn test nhanh với các công cụ như Postman (thêm header Authorization: Basic ...

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}


// Note lưu ý: Khi sử dụng spring security thì khi start ứng dụng... ==>  spring security sẽ tự động bật mode security ==> Tự sinh ra một form đăng nhâp
// sinh ra một password ngẫu nhiên ở console mỗi lần chạy

// Câu hỏi 1: Tại sao spring security lại tự động sinh ra form đăng nhập
// Lý do là vì nó được thiết kế để
//1. Bảo về toàn bộ ứng dụng khỏi truy cập trái phép
//2. Khi chưa cấu hình gì thì nó tự động hiểu chúng ta đang làm web UI
// nên sẽ tạo form login mặt định bằng html để chúng ta login vào mới có thể thao tác được với các api

// Câu hỏi số 2: Vậy tạo sao spring security lại tự động tạo ra một password
// Lý do là vì chúng ta chưa cấu hình người dùng hoặc chưa tồn tại tk admin nào nên spring sẽ mặc định tạo 1 user tên là user
// Và sẽ in ra một password ngẫu nhiên vào console log mỗi lần khởi động.

// CÂu hỏi số 3: Tại sao spring security lại tự động chặn các request
// Khi khi không có người dùng đăng nhập ==> sẽ chặn lại ( Lỗi 401/ 403) hoặc chuyển hướng đến form login (nếu là web app).
// ===> nhằm bảo vệ API hoặc giao diện khỏi bị truy cập trái phép.

// Khi nào cần tắt hoặc thay đổi những hành vi mặt định này
//1. Khi thực hiện web app api trả về thuần json ( ko có HTML )
//2. Tự handle api Login riêng (JWT, OAuth, ... ) ==> Tắt mặt định gọi form login  .formLogin(form -> form.disable()), 	Tắt luôn user/password mặc định và tự cấu hình logic
//3. Dùng frontend riêng (React, Angular...) 	Tắt redirect và CSRF để frontend gọi API không lỗi