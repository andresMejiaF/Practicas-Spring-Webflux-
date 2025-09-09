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
import reactor.core.publisher.Flux;

@Controller
public class ProductoController {
    @Autowired
    private ProductoDao productoDao;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);


    @GetMapping({"/listar","/"})
    public  String listar (Model model){

        Flux<Producto> productos = productoDao.findAll();


        model.addAttribute("productos", productos); // al pasar productos por aqui automaticamente se
        // va suscribir
        model.addAttribute("titulo", "Listado de productos");


        return "listar";
    }

}
