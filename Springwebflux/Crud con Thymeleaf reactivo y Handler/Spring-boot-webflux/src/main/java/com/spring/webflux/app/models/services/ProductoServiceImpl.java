package com.spring.webflux.app.models.services;

import com.spring.webflux.app.models.dao.CategoriaDao;
import com.spring.webflux.app.models.dao.ProductoDao;
import com.spring.webflux.app.models.documents.Categoria;
import com.spring.webflux.app.models.documents.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service //esta notación es un estereotipo de component,
// indica que esta clase es una fachada, un service en la lógica de negocio
public class ProductoServiceImpl implements ProductoService{

    @Autowired
    private ProductoDao dao;

    @Autowired
    private CategoriaDao categoriaDao;
    @Override
    public Flux<Producto> findAll() {
        return dao.findAll();
    }

    @Override
    public Flux<Producto> findAllConNombreUpperCase() {
        return dao.findAll().map(producto -> {

            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        });
    }

    @Override
    public Flux<Producto> findAllConNombreUpperCaseRepeat() {
        return findAllConNombreUpperCase().repeat(5000);
    }

    @Override
    public Mono<Producto> findById(String id) {
        return dao.findById(id);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return dao.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return dao.delete(producto);
    }

    @Override
    public Flux<Categoria> findAllCategorie() {
        return categoriaDao.findAll();
    }

    @Override
    public Mono<Categoria> findCategoriaById(String id) {
        return categoriaDao.findById(id);
    }

    @Override
    public Mono<Categoria> saveCategoria(Categoria categoria) {
        return categoriaDao.save(categoria);
    }

    @Override
    public Mono<Producto> findByNombre(String nombre) {
        return dao.findByNombre(nombre);
    }

    @Override
    public Mono<Categoria> findByCategoriaNombre(String nombre) {
        return categoriaDao.findByNombre(nombre);
    }
}
