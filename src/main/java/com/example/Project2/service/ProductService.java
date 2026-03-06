package com.example.Project2.service;

import com.example.Project2.dto.request.ProductRequest;
import com.example.Project2.dto.response.ProductResponse;
import com.example.Project2.entity.Category;
import com.example.Project2.entity.Product;
import com.example.Project2.exception.AppBadRequestException;
import com.example.Project2.repository.CategoryRepository;
import com.example.Project2.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Mapping Product → ProductResponse
    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }


    //Admin
    public ProductResponse createProduct(ProductRequest productRequest){
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(()-> new RuntimeException("Not Found"));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setImageUrl(productRequest.getImageUrl());
        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Integer id ,ProductRequest request){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("Sản phẩm không tồn tại"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppBadRequestException("Danh mục không tồn tại"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    public Map<String , String> deleteProduct(Integer id){

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("Sản phẩm không tồn tại"));

        productRepository.delete(product);
        return Map.of("message", "Xóa sản phẩm thành công");
    }


    //public
    public Page<ProductResponse> getAllProducts(Pageable pageable){
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    public ProductResponse getProductById(Integer id){

        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Not Found"));

        return toResponse(product);

    }

    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(this::toResponse);
    }

    public Page<ProductResponse> getProductsByCategory(Integer categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppBadRequestException("Danh mục không tồn tại"));

        return productRepository.findByCategory_Id(categoryId, pageable)
                .map(this::toResponse);
    }
}
