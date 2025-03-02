package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Category;
import com.inn.cafe.constents.AgriStockConstants;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.rest.CategoryRest;
import com.inn.cafe.service.CategoryService;
import com.inn.cafe.utils.AgriStockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class CategoryRestImpl implements CategoryRest {
    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryDao categoryDao;
    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try {
            //System.out.println("inside userRestImpl");
            return categoryService.addNewCategory(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println("Before return");
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String Value) {
        try {
            return categoryService.getAllCategory(Value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            return categoryService.update(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> delete(Integer id) {

        try {
            return categoryService.delete(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }


}
