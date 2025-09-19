package com.example.springboot.webflux.app.models.dao;

import com.example.springboot.webflux.app.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao  extends ReactiveMongoRepository<Producto, String> {


}
