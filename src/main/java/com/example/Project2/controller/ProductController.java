package com.example.Project2.controller;

import com.example.Project2.dto.request.ProductRequest;
import com.example.Project2.dto.response.ProductResponse;
import com.example.Project2.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    //ADMIN
    @PostMapping("/admin/products")
    public ResponseEntity<ProductResponse> createProduct(
           @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PutMapping("/admin/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Integer id,
            @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(
            @PathVariable Integer id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }


    //public API
    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductByID(@PathVariable Integer id){
        return ResponseEntity.ok(productService.getProductById(id));
    }


    @GetMapping("/products/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
    }


    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductByCategory(
            @PathVariable("categoryId") Integer id ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page , size);
        return ResponseEntity.ok(productService.getProductsByCategory(id , pageable));
    }

}
