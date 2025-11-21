package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.dtos.response.CollectionResponse;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.entites.RoleEntity;
import com.ecommerce.kientv84.dtos.response.ResponeResult;
import com.ecommerce.kientv84.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Toàn bộ method trong class sẽ trả về json, khác @Controller ở chỗ là Controller thì được hiểu là trang web sd html
//@RestController + @ResponseBody (ngầm) để: Tự động trả về các status theo chuẩn mà không cần quy định
// Tự động chuyển hóa kết quả thành chuỗi json
// Tự động gán Content-Type: application/json

@RequiredArgsConstructor
@RequestMapping("/v1/api")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/roles")
    public ResponseEntity<List<RoleEntity>> getAllRole() {
        return  ResponseEntity.ok(roleService.getAllRole());
    }

    @GetMapping("/role/{id}")
    public ResponseEntity<RoleEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }

    @PostMapping("/role")
    public ResponseEntity<RoleEntity> createRole(@RequestBody RoleEntity role) {
        return ResponseEntity.ok(roleService.createRole(role));
    }

    @PostMapping("/role/{id}")
    public ResponseEntity<RoleEntity> updateRole(@PathVariable Long id, @RequestBody RoleEntity updateData) {
        return ResponseEntity.ok(roleService.updateRole(id, updateData));
    }

    @PostMapping("/roles") //Tại sao sd post mà không dùng delete method
    //POST là method luôn mong đợi có body. Nên khi: chọn Body → raw → JSON
    // ==> Postman tự động gán Content-Type: application/json vào header để yêu cầu body!!!
    public ResponseEntity<Boolean> deleteRole(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(roleService.deleteRole(ids));
    }
}
