package com.inn.cafe.POJO;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;



@NamedQuery(
        name = "Product.getAllProduct",
        query = "SELECT new com.inn.cafe.wrapper.ProductWrapper(u.id, u.name, u.description, u.price, u.stock, u.category.id, u.category.name, u.status) FROM Product u"
)


@NamedQuery(
        name = "Product.updateProductStatus",
        query = "UPDATE Product u SET u.status =:status WHERE u.id =:id"
)

@NamedQuery(
        name = "Product.getByCategory",
        query = "SELECT new com.inn.cafe.wrapper.ProductWrapper(u.id, u.name, u.description, u.price, u.stock, u.category.id, u.category.name, u.status) FROM Product u WHERE u.category.id=:id AND u.status='true'"
)


@NamedQuery(
        name = "Product.getProductById",
        query = "SELECT new com.inn.cafe.wrapper.ProductWrapper(u.id, u.name, u.description, u.price) FROM Product u WHERE u.id=:id"
)

@NamedQuery(
        name = "Product.updateStock",
        query = "UPDATE Product p SET p.stock = :stock WHERE p.id = :id"
)


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "product")
public class Product implements Serializable {
    private static final long serialVersionUID = 123456L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_fk", nullable = false)
    private Category category;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Integer price;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "status")
    private String status;

}
