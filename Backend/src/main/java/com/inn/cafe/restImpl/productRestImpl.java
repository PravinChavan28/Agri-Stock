package com.inn.cafe.restImpl;

import com.inn.cafe.constents.AgriStockConstants;
import com.inn.cafe.dao.productDao;
import com.inn.cafe.rest.productRest;
import com.inn.cafe.service.productService;
import com.inn.cafe.utils.AgriStockUtils;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class productRestImpl implements productRest {
    @Autowired
    productService productService;

    @Autowired
    productDao productDao;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
            //System.out.println("inside userRestImpl");
            return productService.addNewProduct(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println("Before return");
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        try {
            return productService.getAllProduct();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            return productService.update(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> delete(Integer id) {
        try {
            return productService.delete(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try {
            return productService.getByCategory(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        try {
            return productService.getProductById(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Modifying
    @Transactional
    @Override
    public ResponseEntity<String> updateProductStatus(Map<String, String> requestMap) {
        try {
            return productService.updateProductStatus(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    @PostMapping(path = "/updateStock/{id}")
    public ResponseEntity<String> updateStock(Integer id, Map<String, Integer> requestMap) {
        try {
            // Extract updated stock from the request body
            Integer updatedStock = requestMap.get("stock");

            // Validate updated stock value
            if (updatedStock == null || updatedStock < 0) {
                return AgriStockUtils.getResponeEntity("Invalid stock value", HttpStatus.BAD_REQUEST);
            }

            // Call service method to update the stock
            productService.updateStock(id, updatedStock);

            // Return success response
            return AgriStockUtils.getResponeEntity("Stock updated successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}






