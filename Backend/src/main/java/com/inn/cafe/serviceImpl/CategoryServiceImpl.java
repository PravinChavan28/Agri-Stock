package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.constents.AgriStockConstants;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.service.CategoryService;
import com.inn.cafe.utils.AgriStockUtils;
import com.inn.cafe.utils.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    com.inn.cafe.JWT.jwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;
    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    EmailUtil emailUtil;
    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        log.info("Inside addNewCategory{}", requestMap);
        try {
            if(jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap, false)){
                    categoryDao.save(getCategoryFromMap(requestMap , false));
                    return AgriStockUtils.getResponeEntity("Category Added Successfully", HttpStatus.OK);
                }
            }else{
                return AgriStockUtils.getResponeEntity(AgriStockConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println(CafeConstants.SOMETHING_WENT_WRONG);
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")) {
            if(requestMap.containsKey("id") && validateId){
                return true;
            }else if(!validateId){
                return true;
            }
        }
        return false;
    }
    private Category getCategoryFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();
        if(isAdd){
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String Value) {
        try {
            if(!Strings.isNullOrEmpty(Value) && Value.equalsIgnoreCase("true")) {
                return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.OK);
            }
            return new ResponseEntity<>(categoryDao.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateCategoryMap(requestMap , true)) {

                    Optional optional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));

                    if (!optional.isEmpty()) {
                        categoryDao.save(getCategoryFromMap(requestMap,true));
                        return AgriStockUtils.getResponeEntity("Category is updated successfully", HttpStatus.OK);

                    } else {
                        return AgriStockUtils.getResponeEntity("Category id doesn't exist", HttpStatus.OK);
                    }

                }
                return AgriStockUtils.getResponeEntity(AgriStockConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else {
                return AgriStockUtils.getResponeEntity(AgriStockConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> delete(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional optional = categoryDao.findById(id);
                if (!optional.isEmpty()) {
                    categoryDao.deleteById(id);
                    //System.out.println("Product is deleted successfully");
                    return AgriStockUtils.getResponeEntity("Category is deleted successfully", HttpStatus.OK);
                }
                //System.out.println("Product id doesn't exist");
                return AgriStockUtils.getResponeEntity("Category id doesn't exist", HttpStatus.OK);
            } else {
                return AgriStockUtils.getResponeEntity(AgriStockConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println(CafeConstants.SOMETHING_WENT_WRONG);
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
