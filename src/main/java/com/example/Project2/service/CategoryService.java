package com.example.Project2.service;

import com.example.Project2.dto.request.CategoryRequest;
import com.example.Project2.dto.response.CategoryResponse;
import com.example.Project2.entity.Category;
import com.example.Project2.exception.AppBadRequestException;
import com.example.Project2.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;


    //ADMIN
    //Them danh muc
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        if(categoryRepository.existsByName(categoryRequest.getName())){
            throw new AppBadRequestException("Ten category da ton tai");
        }
        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        categoryRepository.save(category);

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public CategoryResponse updateCategory(Integer id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NOT FOUND"));

        if (categoryRepository.existsByName(request.getName())) {
            throw new AppBadRequestException("Tên danh mục đã tồn tại");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        categoryRepository.save(category);

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public Map<String , String> deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not Found"));

        categoryRepository.delete(category);

        return Map.of("message", "Xóa danh mục thành công");
    }

    public List<CategoryResponse> getCategories() {

        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> categoryResponses = new ArrayList<>();
        for (Category c : categories){
            CategoryResponse categoryResponse = new CategoryResponse(c.getId() , c.getName() , c.getDescription());

            categoryResponses.add(categoryResponse);
        }
        return categoryResponses;
    }

    public CategoryResponse getCategoryById(int id){
        Category category = categoryRepository.findById(id).
                orElseThrow(() -> new RuntimeException("NOT FOUND"));

        CategoryResponse categoryResponse = new CategoryResponse(category.getId() , category.getName(), category.getDescription());

        return categoryResponse;
    }
}
