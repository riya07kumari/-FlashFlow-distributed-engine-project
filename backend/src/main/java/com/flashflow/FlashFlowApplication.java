package com.flashflow;

import com.flashflow.model.Product;
import com.flashflow.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlashFlowApplication {

    private static final Logger log = LoggerFactory.getLogger(FlashFlowApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FlashFlowApplication.class, args);
        log.info("=================================================================");
        log.info("🚀 FlashFlow Backend Engine started successfully on port 8080!");
        log.info("📖 Swagger Docs: http://localhost:8080/swagger-ui.html");
        log.info("🗄️ H2 Console:  http://localhost:8080/h2-console");
        log.info("=================================================================");
    }

    @Bean
    public CommandLineRunner seedDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                log.info("Seeding initial flash-sale catalog with limited inventory...");

                Product p1 = new Product(
                        "Sony PlayStation 5 Pro (Limited Edition)",
                        "Next-gen gaming console with 2TB high-speed SSD and AI-enhanced PSSR upscaling.",
                        new BigDecimal("699.99"),
                        5, // Only 5 in stock! Perfect for race-condition testing
                        "https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=600&auto=format&fit=crop&q=80",
                        "Gaming"
                );

                Product p2 = new Product(
                        "Apple iPhone 16 Pro Max (Titanium Desert)",
                        "A18 Pro Bionic processor, 48MP Fusion camera, and revolutionary battery life.",
                        new BigDecimal("1199.99"),
                        10, // Only 10 in stock
                        "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=600&auto=format&fit=crop&q=80",
                        "Smartphones"
                );

                Product p3 = new Product(
                        "NVIDIA GeForce RTX 5090 OC 32GB",
                        "Ultra-enthusiast Blackwell architecture GPU for 8K gaming and local LLM AI inference.",
                        new BigDecimal("1999.99"),
                        3, // Only 3 in stock!
                        "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?w=600&auto=format&fit=crop&q=80",
                        "Components"
                );

                Product p4 = new Product(
                        "Keychron Q1 Pro Custom Mechanical Keyboard",
                        "Full aluminum CNC body, hot-swappable switches, wireless Bluetooth 5.1.",
                        new BigDecimal("199.99"),
                        15,
                        "https://images.unsplash.com/photo-1595225476474-87563907a212?w=600&auto=format&fit=crop&q=80",
                        "Accessories"
                );

                productRepository.saveAll(List.of(p1, p2, p3, p4));
                log.info("Successfully seeded {} flash-sale products.", productRepository.count());
            }
        };
    }
}
