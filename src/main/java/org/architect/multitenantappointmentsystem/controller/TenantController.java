package org.architect.multitenantappointmentsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.request.CreateTenantRequest;
import org.architect.multitenantappointmentsystem.dto.ResponseDto;
import org.architect.multitenantappointmentsystem.dto.response.TenantResponse;
import org.architect.multitenantappointmentsystem.dto.request.UpdateTenantRequest;
import org.architect.multitenantappointmentsystem.service.interfaces.TenantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService service;

    @PostMapping
    public ResponseEntity<ResponseDto<TenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {
        return ResponseDto.ok(service.createTenant(request)).toResponseEntity();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<TenantResponse>> getTenant(
            @PathVariable java.util.UUID id) {
        return ResponseDto.ok(service.getTenantById(id)).toResponseEntity();
    }

    @GetMapping("/{slug}/by-slug")
    public ResponseEntity<ResponseDto<TenantResponse>> getTenantBySlug(
            @PathVariable String slug) {
        return ResponseDto.ok(service.getTenantBySlug(slug)).toResponseEntity();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<TenantResponse>> updateTenant(
            @PathVariable java.util.UUID id,
            @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseDto.ok(service.updateTenant(id, request)).toResponseEntity();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<String>> deleteTenant(@PathVariable java.util.UUID id) {
        service.deleteTenant(id);
        return ResponseDto.ok("", "Tenant o'chirildi").toResponseEntity();
    }
//
//    @GetMapping("/get-all")
//    public ResponseEntity<ResponseDto<Page<TenantResponse>>> getAllTenants(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return ResponseDto.ok(service.getAllTenants(pageable)).toResponseEntity();
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<ResponseDto<Page<TenantResponse>>> searchTenants(
//            @RequestParam(defaultValue = "") String q,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return ResponseDto.ok(service.searchTenants(q, pageable)).toResponseEntity();
//    }
}
