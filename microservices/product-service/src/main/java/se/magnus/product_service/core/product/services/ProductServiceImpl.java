package se.magnus.product_service.core.product.services;

import org.springframework.web.bind.annotation.RestController;

import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;

@RestController
public class ProductServiceImpl implements ProductService {

    @Override
    public Product getProduct(int productId) {
        
        return new Product(123, "Shirt",212, "Uttar pradesh");
    }

}
