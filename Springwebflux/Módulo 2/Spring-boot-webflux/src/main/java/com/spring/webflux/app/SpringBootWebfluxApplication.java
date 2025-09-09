package com.spring.webflux.app;

import com.spring.webflux.app.models.dao.ProductoDao;
import com.spring.webflux.app.models.documents.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {
    @Autowired
    private ProductoDao dao;
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;


    private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        mongoTemplate.dropCollection("productos")
                .subscribe();

        Flux.just( new Producto("TV panasonic", 4500.88),
                new Producto("Xbox series", 2500.88),
                new Producto("Play 5", 4500.88),
                new Producto("Switch Oled", 4500.88),
                new Producto("Diademas", 4500.88)
        ).flatMap(producto -> dao.save(producto))
                .subscribe(producto -> log.info("Insert: "+producto.getId()+ " " + producto.getNombre()));
    }
}
