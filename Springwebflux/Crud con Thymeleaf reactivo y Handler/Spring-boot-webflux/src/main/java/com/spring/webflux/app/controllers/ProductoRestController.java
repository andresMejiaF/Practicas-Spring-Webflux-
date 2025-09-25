package com.spring.webflux.app.controllers;

import com.spring.webflux.app.SpringBootWebfluxApplication;
import com.spring.webflux.app.models.dao.ProductoDao;
import com.spring.webflux.app.models.documents.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {


    @Autowired
    private ProductoDao dao;

    private static final Logger log = LoggerFactory.getLogger(ProductoRestController.class);

    @GetMapping()
    public Flux<Producto> index(){
        Flux<Producto> productos = dao.findAll()
                .map(producto -> {

                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                }).doOnNext(producto -> log.info(producto.getNombre()));

        return productos;
    }

    @GetMapping("/{id}")
    public Mono<Producto> index(@PathVariable String id){
       // Mono<Producto> producto = dao.findById(id); mejor opción


        //opción de practica
        Flux<Producto> productos = dao.findAll();

        Mono<Producto> producto = productos.filter(producto1 -> producto1.getId().equals(id))
                .next()
                .doOnNext(producto2 -> log.info(producto2.getNombre())); // emite solo el primer item dentro del flux

        return producto;
    }
}
