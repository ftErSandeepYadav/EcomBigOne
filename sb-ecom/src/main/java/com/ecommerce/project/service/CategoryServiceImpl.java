package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired private CategoryRepository categoryRepository ;
    @Autowired private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategoryCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetals = PageRequest.of(pageNumber-1, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetals);

        List<Category> categoryList = categoryPage.getContent();
        if(categoryRepository.findAll().isEmpty()){
            throw new APIException("No Categories created till now ");
        }
        if(categoryList.isEmpty()){
            throw new APIException("Page doesn't exists");
        }
        List<CategoryDTO> categoryDTOList = categoryList.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        CategoryResponse categoryListResponse = new CategoryResponse();
        categoryListResponse.setContent(categoryDTOList);
        categoryListResponse.setPageNumber(categoryPage.getNumber()+1);
        categoryListResponse.setPageSize(categoryPage.getSize());
        categoryListResponse.setTotalElements(categoryPage.getTotalElements());
        categoryListResponse.setTotalPages(categoryPage.getTotalPages());
        categoryListResponse.setLastPage(categoryPage.isLast());

        return categoryListResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(savedCategory != null){
            throw new APIException("Category with name " + category.getCategoryName() + " already exists");
        }
        category = categoryRepository.save(category) ;
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if(category.isPresent()){
            categoryRepository.deleteById(categoryId);
            return modelMapper.map(category.get(), CategoryDTO.class);
        }
        throw new ResourceNotFoundException("Category", "CategoryID", categoryId);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        Optional<Category> oldCategory = categoryRepository.findById(categoryId);
        if(oldCategory.isPresent()){
            category.setCategoryId(categoryId);
            category = categoryRepository.save(category);
            return modelMapper.map(category, CategoryDTO.class);
        }
        throw new ResourceNotFoundException("Category", "CategoryID", categoryId);
    }
}
