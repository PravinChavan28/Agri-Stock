package com.inn.cafe.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductWrapper {
    Integer id;
    String name;
    String description;
    Integer price;
    Integer stock;
    String status;
    Integer categoryId;
    String categoryName;


    public ProductWrapper(Integer id, String name , String description , Integer price , Integer stock , Integer categoryId , String categoryName , String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.status = status;
    }

    public ProductWrapper(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProductWrapper(Integer id, String name, String description, Integer price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public boolean isValid() {
        return price != null && price > 0 && stock != null && stock >= 0 && categoryId != null && categoryId > 0;
    }
}
