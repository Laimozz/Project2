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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@Service
public class ProductService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private ProductResponse toResponse(Product product) {

        boolean hasImage = product.getImageType() != null && !product.getImageType().isBlank();
        String imageUrl = hasImage ? "/api/products/" + product.getId() + "/image" : null;

        if (hasImage) {
            imageUrl = "/api/products/" + product.getId() + "/image";
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(imageUrl)
                .imageName(product.getImageName())
                .imageType(product.getImageType())
                .hasImage(hasImage)
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }

    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppBadRequestException("Danh mục không tồn tại"));

        Product product = new Product();
        product.setCategory(category);

        applyUpdatableFields(product, request, true);
        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("Sản phẩm không tồn tại"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppBadRequestException("Danh mục không tồn tại"));

        product.setCategory(category);
        applyUpdatableFields(product, request, false);

        return toResponse(productRepository.save(product));
    }

    public Map<String, String> deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("Sản phẩm không tồn tại"));

        productRepository.delete(product);
        return Map.of("message", "Xóa sản phẩm thành công");
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("Không tìm thấy sản phẩm"));
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

    public Product findProductEntityById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("Không tìm thấy sản phẩm"));
    }

    private void applyUpdatableFields(Product product, ProductRequest request, boolean isCreate) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        MultipartFile imageFile = request.getImageFile();

        // Create: image optional; Update: if no new file => keep old image
        if (imageFile != null && !imageFile.isEmpty()) {
            validateImage(imageFile);
            try {
                product.setImageName(StringUtils.cleanPath(imageFile.getOriginalFilename()));
                product.setImageType(imageFile.getContentType());
                product.setImageData(imageFile.getBytes());
            } catch (Exception e) {
                throw new AppBadRequestException("Không thể đọc dữ liệu ảnh");
            }
        } else if (isCreate) {
            product.setImageName(null);
            product.setImageType(null);
            product.setImageData(null);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new AppBadRequestException("Ảnh vượt quá 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new AppBadRequestException("Chỉ hỗ trợ JPG, PNG, WEBP");
        }
    }
}
