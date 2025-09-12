package com.spring.webflux.app.controllers;


import com.spring.webflux.app.models.documents.Producto;
import com.spring.webflux.app.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Controller
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);


    @GetMapping({"/listar","/"})
    public  Mono<String> listar (Model model){

        Flux<Producto> productos = productoService.findAllConNombreUpperCase()
                .map(producto -> {

                    producto.setNombre(producto.getNombre().toUpperCase());
                    return producto;
                });

        productos.subscribe(producto -> log.info(producto.getNombre()));

        model.addAttribute("productos", productos); // al pasar productos por aqui automaticamente se
        // va suscribir
        model.addAttribute("titulo", "Listado de productos");


        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model){

        model.addAttribute("producto", new Producto() );
        model.addAttribute("titulo", "formulario de producto");
        return Mono.just("form");
    }


    @PostMapping("/form")
    public Mono<String> guardar(Producto producto){

        return productoService.save(producto).doOnNext(p -> {log.info("Producto guardado: " +
                p.getNombre() +"  ID: " + p.getId());}).thenReturn("redirect:/listar");
    }



    @GetMapping("/listar-datadriver")
    public  String listarDataDriver(Model model){

        Flux<Producto> productos = productoService.findAllConNombreUpperCase()
                .delayElements(Duration.ofSeconds(1));
        productos.subscribe(producto -> log.info(producto.getNombre()));
        model.addAttribute("productos",new ReactiveDataDriverContextVariable(productos,
                1) ); // al pasar productos por aqui automaticamente se
        // va suscribir
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("/listar-full")
    public  String listarFull (Model model){

        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos); // al pasar productos por aqui automaticamente se
        // va suscribir
        model.addAttribute("titulo", "Listado de productos");


        return "listar";
    }

    @GetMapping("/listar-chunked")
    public  String listarChuncked (Model model){

        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos); // al pasar productos por aqui automaticamente se
        // va suscribir
        model.addAttribute("titulo", "Listado de productos");


        return "listar-chuncked";
    }

}
