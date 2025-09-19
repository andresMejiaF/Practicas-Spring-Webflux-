package com.example.springboot.webflux.app;

import com.example.springboot.webflux.app.models.documents.Categoria;
import com.example.springboot.webflux.app.models.documents.Producto;
import com.example.springboot.webflux.app.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner {

	@Autowired
	private ProductoService productoService;
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate();
	}

	public void mongoTemplate(){
		mongoTemplate.dropCollection("productos")
				.subscribe();

		mongoTemplate.dropCollection("categorias")
				.subscribe();

		Categoria electronico = new Categoria("Electronico");
		Categoria deporte = new Categoria("deporte");
		Categoria computacion = new Categoria("computacion");
		Categoria muebles = new Categoria("muebles");

		Flux.just(electronico, deporte, computacion, muebles)
				.flatMap( productoService::saveCategoria)
				.doOnNext(categoria -> {
					log.info("Categoría creada: " + categoria.getNombre()+ "Id: " + categoria.getId());
				}).thenMany(    //para mono solo then

						Flux.just( new Producto("TV panasonic", 4500.88, electronico),
								new Producto("Xbox series", 2500.88, electronico),
								new Producto("Play 5", 4500.88, electronico),
								new Producto("Switch Oled", 4500.88, electronico),
								new Producto("Diademas", 4500.88, electronico),
								new Producto("NoteBook", 4500.88, computacion),
								new Producto("HP", 4500.88, computacion),
								new Producto("Bicicleta", 4500.88, deporte),
								new Producto("Cajon", 4500.88, muebles)
						).flatMap(producto -> {
							producto.setCreateAt(new Date());
							return productoService.save(producto);
						}))
				.subscribe(producto -> log.info("Insert: "+producto.getId()+ " " + producto.getNombre()));

	}
}
