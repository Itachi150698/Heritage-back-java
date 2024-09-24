package com.codeWithProjects.ecom.services.admin.category;

import com.codeWithProjects.ecom.dto.CategoryDto;
import com.codeWithProjects.ecom.entity.Category;
import com.codeWithProjects.ecom.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;

    @Cacheable
    public Category createCategory(CategoryDto categoryDto){

        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }
}
