package com.bhavi.ecommerce.productservice.config;

import com.bhavi.ecommerce.productservice.model.Product;
import com.bhavi.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if(productRepository.findAll().isEmpty()) {
            //Product 1
            Product p1 = Product.builder()
                    .name("HP Laptop")
                    .price(BigDecimal.valueOf(45000))
                    .category("Electronics")
                    .description("Laptop for gaming and office use as well")
                    .imageUrl("www.hp.com/1233242n2n/image")
                    .stockQuantity(25)
                    .build();

            productRepository.save(p1);

            //Product 2
            Product p2 = Product.builder()
                    .name("Wildcraft trekking bag")
                    .price(BigDecimal.valueOf(1000))
                    .category("Accessories")
                    .description("Wildcraft bag for trekking and daily use")
                    .imageUrl("www.hp.com/1233242n2n/image")
                    .stockQuantity(50)
                    .build();

            productRepository.save(p2);

            //Product 3
            Product p3 = Product.builder()
                    .name("Iphone 16")
                    .price(BigDecimal.valueOf(100000))
                    .category("Electronics")
                    .description("Mobile phone")
                    .imageUrl("www.hp.com/1233242n2n/image")
                    .stockQuantity(25)
                    .build();

            productRepository.save(p3);

            //Product 4
            Product p4 = Product.builder()
                    .name("Charger redmi")
                    .price(BigDecimal.valueOf(500))
                    .category("Electronics")
                    .description("50watt charger for fast charging")
                    .imageUrl("www.hp.com/1233242n2n/image")
                    .stockQuantity(25)
                    .build();

            productRepository.save(p4);

            //Product 5
            Product p5 = Product.builder()
                    .name("Hair dryer")
                    .price(BigDecimal.valueOf(3000))
                    .category("Electronics")
                    .description("Hair dryer with warranty")
                    .imageUrl("www.hp.com/1233242n2n/image")
                    .stockQuantity(25)
                    .build();

            productRepository.save(p5);

            //Product 6
            Product p6 = Product.builder()
                    .name("Dumbbells")
                    .price(BigDecimal.valueOf(4000))
                    .category("Accessories")
                    .description("10KG dumbbells")
                    .imageUrl("www.hp.com/1233242n2n/image")
                    .stockQuantity(100)
                    .build();

            productRepository.save(p6);
        }
    }
}
