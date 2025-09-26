package com.example.springboot.webflux.app;

import com.example.springboot.webflux.app.models.documents.Categoria;
import com.example.springboot.webflux.app.models.documents.Producto;
import com.example.springboot.webflux.app.models.services.ProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) random port
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringBootWebfluxApirestApplicationTests {


	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductoService productoService;


	@Value("${config.base.endpoint}")
	private String url;
	@Test
	void listarTest() {
		client.get()
				.uri(url)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Producto.class)
				.consumeWith(response -> {
					List<Producto> productos = response.getResponseBody();
					productos.forEach(p -> {
						System.out.println("p.getNombre()} = " + p.getNombre());
					});
					//  Assertions.assertEquals(9, productos.size());
					Assertions.assertTrue(productos.size() > 0);
				});
		// .hasSize(9); //otra forma mas breve si queremos exactamente 9
		;
	}

	@Test
	void verTest() {

		Producto producto= productoService.findByNombre("Switch Oled").block();

		client.get()
				.uri(url+"/{id}", Collections.singletonMap("id", producto.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Producto.class)
				.consumeWith(response -> {
					Producto p = response.getResponseBody();

					//  Assertions.assertEquals(9, productos.size());
					Assertions.assertEquals(p.getNombre(), "Switch Oled");
					Assertions.assertNotNull(p.getId());
					Assertions.assertTrue(p.getId().length() > 0);

				});
               /* .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("Xbox series");

                */
	}

	@Test
	public void crearTest(){

		Categoria categoria = productoService.findByCategoriaNombre("Electronico").block(); //entrega el objeto y no el mono

		Producto producto = new Producto("Computador", 23321.9, categoria);

		client.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(producto), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.producto.id").isNotEmpty()
				.jsonPath("$.producto.nombre").isEqualTo("Computador")
				.jsonPath("$.producto.categoria.nombre").isEqualTo("Electronico");


	}

	@Test
	public void editarTest(){

		Producto producto = productoService.findByNombre("Xbox series").block();
		Categoria categoria = productoService.findByCategoriaNombre("Electronico").block(); //entrega el objeto y no el mono
		Producto productoEditado = new Producto("Xbox series melito series s", 53321.9, categoria);

		client.put()
				.uri(url+"/{id}", Collections.singletonMap("id", producto.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(productoEditado), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("Xbox series melito series s")
				.jsonPath("$.categoria.nombre").isEqualTo("Electronico");

	}


	@Test
	public void eliminar(){

		Producto producto = productoService.findByNombre("NoteBook").block();

		client.delete()
				.uri(url+"/{id}", Collections.singletonMap("id", producto.getId()))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody()
				.isEmpty();

		client.get()
				.uri(url+"/{id}", Collections.singletonMap("id", producto.getId()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody()
				.isEmpty();
	}
}