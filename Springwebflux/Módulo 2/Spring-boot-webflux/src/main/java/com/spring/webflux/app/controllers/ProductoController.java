package com.spring.webflux.app.controllers;


import com.spring.webflux.app.models.documents.Producto;
import com.spring.webflux.app.models.services.ProductoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Controller
@SessionAttributes("producto")
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
        model.addAttribute("boton", "Crear");

        return Mono.just("form");
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarV2(@PathVariable String id, Model model){
        return productoService.findById(id).doOnNext(p -> {
            log.info("Producto: " + p.getNombre());
            model.addAttribute("boton", "Editar");
            model.addAttribute("titulo", "Editar producto");
            model.addAttribute("producto", p );
        }).defaultIfEmpty(new Producto())
                .flatMap(producto -> {

                    if(producto.getId() == null){
                        return Mono.error( new InterruptedException("El producto no existe"));
                    }

                    return Mono.just(producto);
                })
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));

    }
    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model){

        Mono<Producto> productoMono = productoService.findById(id).doOnNext(p -> {
            log.info("Producto: " + p.getNombre());
        }).defaultIfEmpty(new Producto());

        model.addAttribute("boton", "Editar");
        model.addAttribute("titulo", "Editar producto");
        model.addAttribute("producto", productoMono );
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> guardar(@Valid Producto producto, BindingResult result, SessionStatus status,
                                Model model){ //binding result
        // tiene que ir pegado a producto al objeto que etsamso validando
        if(result.hasErrors()){
            model.addAttribute("titulo", "Errores en formulario producto");
            model.addAttribute("boton", "Guardar");
            return Mono.just("form");
        }else{
            status.setComplete();

            if(producto.getCreateAt() == null){
                producto.setCreateAt(new Date());
            }

            return productoService.save(producto).doOnNext(p -> {log.info("Producto guardado: " +
                    p.getNombre() +"  ID: " + p.getId());}).thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }

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
