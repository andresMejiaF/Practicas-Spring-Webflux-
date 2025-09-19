package com.spring.webflux.app.controllers;


import com.spring.webflux.app.models.documents.Categoria;
import com.spring.webflux.app.models.documents.Producto;
import com.spring.webflux.app.models.services.ProductoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Controller
@SessionAttributes("producto")
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    @Value("${config.uploads.path}")
    private String path;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @ModelAttribute("categorias")
    public  Flux<Categoria> categorias (){
        return productoService.findAllCategorie();
    }

    @GetMapping("/uploads/img/{nombreFoto:.+}")
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException {

        Path ruta = Paths.get(path).resolve(nombreFoto).toAbsolutePath();

        Resource imagen = new UrlResource(ruta.toUri());

        return Mono.just(

                ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + imagen.getFilename() + "\"" )
                        .body(imagen)

        );
    }

    @GetMapping("/ver/{id}")
    public  Mono<String> ver(Model model, @PathVariable String id){

        return productoService.findById(id)
                .doOnNext(p -> {
                    model.addAttribute("producto", p);
                    model.addAttribute("titulo", "DETALLE PRODUCTO");
                }).switchIfEmpty(Mono.just(new Producto()))
                .flatMap(producto -> {
                    if(producto.getId() == null){
                        return Mono.error( new InterruptedException("El producto no existe"));
                    }
                    return Mono.just(producto);
                }
                )
                .then(Mono.just("ver"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }

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
                                Model model, @RequestPart FilePart file){ //binding result
        // tiene que ir pegado a producto al objeto que etsamso validando
        if(result.hasErrors()){
            model.addAttribute("titulo", "Errores en formulario producto");
            model.addAttribute("boton", "Guardar");
            return Mono.just("form");
        }else{
            status.setComplete();
            Mono<Categoria> categoria = productoService.findCategoriaById(producto.getCategoria().getId());

           return categoria.flatMap(c -> {

               if(producto.getCreateAt() == null){
                   producto.setCreateAt(new Date());
               }

               if(!file.filename().isEmpty()){
                   producto.setFoto(UUID.randomUUID().toString() + file.filename().replace(" ", " ")
                           .replace(" ", " ")
                           .replace(":", " ")
                           .replace("\\", " "));
               }

                producto.setCategoria(c);
               return productoService.save(producto);
            }).doOnNext(p -> {
               log.info("Categoria guardada: " +
                               p.getCategoria().getNombre() +"  ID Categoria: " + p.getCategoria().getId());
                log.info("Producto guardado: " +
                    p.getNombre() +"  ID: " + p.getId());})

                    .flatMap(p -> {
                        if(!file.filename().isEmpty()){
                            return file.transferTo(new File(path + p.getFoto()));
                        }
                        return  Mono.empty();
                    })
                   .thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }

    }
    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id){ //binding result
        return productoService.findById(id)
                .defaultIfEmpty(new Producto())
                .flatMap(producto -> {
                    if(producto.getId() == null){
                        return Mono.error( new InterruptedException("El producto a eliminar no existe"));
                    }
                    return Mono.just(producto);
                })
                .flatMap(productoService::delete)
                        .then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
                    .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
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
