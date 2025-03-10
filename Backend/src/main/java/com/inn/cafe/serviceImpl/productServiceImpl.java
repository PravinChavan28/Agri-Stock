package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.POJO.Product;
import com.inn.cafe.constents.AgriStockConstants;
import com.inn.cafe.dao.productDao;
import com.inn.cafe.service.productService;
import com.inn.cafe.utils.AgriStockUtils;
import com.inn.cafe.utils.EmailUtil;
import com.inn.cafe.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class productServiceImpl implements productService {
    @Autowired
    productDao productDao;

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
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        log.info("Inside addNewProduct{}", requestMap);
        try {
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, false)) {
                    productDao.save(getProductFromMap(requestMap, false));
                    return AgriStockUtils.getResponeEntity("Product Added Successfully", HttpStatus.OK);
                }
            } else {
                return AgriStockUtils.getResponeEntity(AgriStockConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println(CafeConstants.SOMETHING_WENT_WRONG);
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        try {
            return new ResponseEntity<>(productDao.getAllProduct(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, true)) {
                    Optional optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                    if (!optional.isEmpty()) {
                        productDao.save(getProductFromMap(requestMap, true));
                        return AgriStockUtils.getResponeEntity("Product is updated successfully", HttpStatus.OK);

                    } else {
                        return AgriStockUtils.getResponeEntity("Product id doesn't exist", HttpStatus.OK);
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
                Optional optional = productDao.findById(id);
                if (!optional.isEmpty()) {
                    productDao.deleteById(id);
                    //System.out.println("Product is deleted successfully");
                    return AgriStockUtils.getResponeEntity("Product is deleted successfully", HttpStatus.OK);
                }
                //System.out.println("Product id doesn't exist");
                return AgriStockUtils.getResponeEntity("Product id doesn't exist", HttpStatus.OK);
            } else {
                return AgriStockUtils.getResponeEntity(AgriStockConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println(CafeConstants.SOMETHING_WENT_WRONG);
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try {
            return new ResponseEntity<>(productDao.getByCategory(id), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProductWrapper> getProductById(Integer id) {
        try {
            return new ResponseEntity<>(productDao.getProductById(id), HttpStatus.OK);
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
            if (jwtFilter.isAdmin()) {
                Optional optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                if (!optional.isEmpty()) {
                    productDao.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    return AgriStockUtils.getResponeEntity("Product status is updated successfully", HttpStatus.OK);
                }
                return AgriStockUtils.getResponeEntity("Product id doesn't exist", HttpStatus.OK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Modifying
    @Transactional
    @Override
    public ResponseEntity<String> updateStock(Integer id, Integer updatedStock) {
        try {
            // Check if the product exists
            Optional<Product> optionalProduct = productDao.findById(id);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                // Update the stock
                product.setStock(updatedStock);
                // Save the updated product
                productDao.save(product);
                return AgriStockUtils.getResponeEntity("Product stock updated successfully", HttpStatus.OK);
            } else {
                return AgriStockUtils.getResponeEntity("Product not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")) {
            if (requestMap.containsKey("id") && validateId) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isUpdate) {
        Product product = new Product();
        Category category = new Category();

        if (isUpdate) {
            product.setId(Integer.parseInt(requestMap.get("id")));
        }

        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));
        product.setStock(Integer.parseInt(requestMap.get("stock")));
        category.setId(Integer.parseInt(requestMap.get("categoryId")));
        product.setCategory(category);
        product.setStatus(requestMap.get("status"));

        return product;
    }
}

