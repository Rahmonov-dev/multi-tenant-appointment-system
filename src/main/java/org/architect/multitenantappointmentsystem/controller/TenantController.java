package org.architect.multitenantappointmentsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.dto.request.CreateTenantRequest;
import org.architect.multitenantappointmentsystem.dto.ResponseDto;
import org.architect.multitenantappointmentsystem.dto.response.TenantResponse;
import org.architect.multitenantappointmentsystem.dto.request.UpdateTenantRequest;
import org.architect.multitenantappointmentsystem.security.TenantContext;
import org.architect.multitenantappointmentsystem.service.TenantServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {
    private final TenantServiceImpl service;

    @PostMapping
    public ResponseEntity<ResponseDto<TenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {
        return ResponseDto.ok(service.createTenant(request)).toResponseEntity();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<TenantResponse>> getTenant(
            @PathVariable Long id) {
        return ResponseDto.ok(service.getTenantById(id)).toResponseEntity();
    }

    @GetMapping("/by-key/{tenantKey}")
    public ResponseEntity<ResponseDto<TenantResponse>> getTenantByKey(
            @PathVariable String tenantKey) {
        return ResponseDto.ok(service.getTenantByKey(tenantKey)).toResponseEntity();
    }

    @GetMapping("/{slug}/by-slug")
    public ResponseEntity<ResponseDto<TenantResponse>> getTenantBySlug(
            @PathVariable String slug) {
        return ResponseDto.ok(service.getTenantBySlug(slug)).toResponseEntity();
    }

    @PutMapping("/{slug}")
    public ResponseEntity<ResponseDto<TenantResponse>> updateTenant(
            @PathVariable String slug,
            @Valid @RequestBody UpdateTenantRequest request) {
        Long id = TenantContext.getTenantId();
        return ResponseDto.ok(service.updateTenant(id, request)).toResponseEntity();
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<ResponseDto<String>> deleteTenant(@PathVariable String slug) {
        Long id = TenantContext.getTenantId();
        service.deleteTenant(id);
        return ResponseDto.ok("", "Tenant o'chirildi").toResponseEntity();
    }

    @GetMapping("/get-all")
    public ResponseEntity<ResponseDto<List<TenantResponse>>> getAllTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TenantResponse> tenants = service.getAllTenants(pageable);
        return ResponseDto.ok(tenants).toResponseEntity();
    }
}
