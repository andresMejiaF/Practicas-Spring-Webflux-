package com.spring.webflux.app.models.dao;

import com.spring.webflux.app.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao  extends ReactiveMongoRepository<Producto, String> {


}
