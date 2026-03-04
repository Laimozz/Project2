package com.example.Project2.controller;

import com.example.Project2.dto.request.CategoryRequest;
import com.example.Project2.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<?> GetCategories(){
        return new ResponseEntity<>(categoryService.getCategories(), HttpStatus.OK);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<?> GetCategoryByID(@PathVariable Integer id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    //ADMIN API
    @PostMapping("/admin/categories")
    public ResponseEntity<?> CreateCategory(@RequestBody CategoryRequest categoryRequest){
        return new ResponseEntity<>(categoryService.createCategory(categoryRequest) , HttpStatus.CREATED);
    }

    @PutMapping("/admin/categories/{id}")
    public ResponseEntity<?> UpdateCategory(@PathVariable Integer id , @RequestBody CategoryRequest request){
        return ResponseEntity.ok(categoryService.updateCategory(id,request));
    }

    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<?> DeleteCategory(@PathVariable Integer id){
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }
}
