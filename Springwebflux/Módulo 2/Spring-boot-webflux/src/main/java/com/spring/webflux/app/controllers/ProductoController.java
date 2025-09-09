package com.spring.webflux.app.controllers;

import com.spring.webflux.app.SpringBootWebfluxApplication;
import com.spring.webflux.app.models.dao.ProductoDao;
import com.spring.webflux.app.models.documents.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Controller
public class ProductoController {
    @Autowired
    private ProductoDao productoDao;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);


    @GetMapping({"/listar","/"})
    public  String listar (Model model){

        Flux<Producto> productos = productoDao.findAll()
                .map(producto -> {

                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                });

        productos.subscribe(producto -> log.info(producto.getNombre()));

        model.addAttribute("productos", productos); // al pasar productos por aqui automaticamente se
        // va suscribir
        model.addAttribute("titulo", "Listado de productos");


        return "listar";
    }

    @GetMapping("/listar-datadriver")
    public  String listarDataDriver(Model model){

        Flux<Producto> productos = productoDao.findAll()
                .map(producto -> {

                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                }).delayElements(Duration.ofSeconds(1));
        productos.subscribe(producto -> log.info(producto.getNombre()));
        model.addAttribute("productos",new ReactiveDataDriverContextVariable(productos,
                1) ); // al pasar productos por aqui automaticamente se
        // va suscribir
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("/listar-full")
    public  String listarFull (Model model){

        Flux<Producto> productos = productoDao.findAll()
                .map(producto -> {

                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                }).repeat(5000);

        model.addAttribute("productos", productos); // al pasar productos por aqui automaticamente se
        // va suscribir
        model.addAttribute("titulo", "Listado de productos");


        return "listar";
    }

}
