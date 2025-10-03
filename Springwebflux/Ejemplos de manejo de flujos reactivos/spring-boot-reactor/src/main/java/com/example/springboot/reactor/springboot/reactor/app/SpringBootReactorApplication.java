package com.example.springboot.reactor.springboot.reactor.app;

import com.example.springboot.reactor.springboot.reactor.app.models.Comentarios;
import com.example.springboot.reactor.springboot.reactor.app.models.Usuario;
import com.example.springboot.reactor.springboot.reactor.app.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * Clase principal de la aplicación Spring Boot que demuestra distintos
 * ejemplos prácticos del uso de Project Reactor (Flux y Mono).
 *
 * Incluye:
 * - Creación de flujos con Flux y Mono.
 * - Transformaciones con map y flatMap.
 * - Manejo de errores y reintentos.
 * - Contrapresión.
 * - Combinación de publishers (zipWith).
 * - Uso de collectList, filter, delayElements e interval.
 */
@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    /**
     * Método principal que inicia la aplicación Spring Boot.
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    /**
     * Se ejecuta al arrancar la aplicación.
     * Aquí puedes invocar cualquier ejemplo.
     */
    @Override
    public void run(String... args) throws Exception {
       // ejemploContrapresion();
        ejemploToString();
    }

    /**
     * Ejemplo de contrapresión manual.
     * Se consumen 10 elementos, pero solo se piden de 5 en 5 al Publisher.
     */
    public void ejemploContrapresion() {
        Flux.range(1, 10)
                .log()
                .subscribe(new Subscriber<Integer>() {
                    private Subscription s;
                    private Integer limite = 5;
                    private Integer consumido = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(limite);
                    }

                    @Override
                    public void onNext(Integer t) {
                        logger.info(t.toString());
                        consumido++;
                        if (consumido == limite) {
                            consumido = 0;
                            s.request(limite);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {}

                    @Override
                    public void onComplete() {}
                });
    }

    /**
     * Ejemplo de creación de un Flux con Flux.create().
     * Emite valores cada segundo usando un Timer.
     * - Finaliza en 10.
     * - Lanza un error en 5.
     */
    public void ejemploIntervaloDesdeCreate() throws InterruptedException {
        Flux.create(emitter -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                private Integer contador = 0;

                @Override
                public void run() {
                    emitter.next(++contador);
                    if (contador == 10) {
                        timer.cancel();
                        emitter.complete();
                    }
                    if (contador == 5) {
                        timer.cancel();
                        emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
                    }
                }
            }, 1000, 1000);
        }).subscribe(
                next -> logger.info(next.toString()),
                error -> logger.error(error.getMessage()),
                () -> logger.info("Terminamos!!")
        );
    }

    /**
     * Ejemplo de un intervalo infinito con control de errores.
     * - Emite valores cada segundo.
     * - En 5 lanza error.
     * - Se reintenta 2 veces gracias a retry(2).
     * - CountDownLatch evita que el método termine antes.
     */
    public void ejemploIntervaloInfinito() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown)
                .flatMap(i -> {
                    if (i >= 5) {
                        return Flux.error(new InterruptedException("Solo hasta 5"));
                    }
                    return Mono.just(i);
                })
                .map(i -> "Hola " + i)
                .retry(2)
                .subscribe(
                        s -> logger.info(s),
                        e -> logger.error(e.getMessage())
                );

        latch.await();
    }

    /**
     * Ejemplo de delayElements.
     * Emite números del 1 al 12 con un retraso de 1 segundo entre cada emisión.
     */
    public void ejemploDelayElement() throws InterruptedException {
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> logger.info(i.toString()));

        rango.blockLast();
    }

    /**
     * Ejemplo de interval combinado con zipWith.
     * Sincroniza un rango con un intervalo de 1 segundo.
     */
    public void ejemploInterval() {
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

        rango.zipWith(retraso, (ra, re) -> ra)
                .doOnNext(i -> logger.info(i.toString()))
                .blockLast();
    }

    /**
     * Ejemplo de zipWith con dos flujos.
     * Combina valores transformados y los imprime como String.
     */
    public void ejemploZipWithRangos() {
        Flux<Integer> rangos = Flux.range(0, 4);
        Flux.just(1, 2, 3, 4)
                .map(i -> (i * 2))
                .zipWith(rangos, (uno, dos) ->
                        String.format("primer flux: %d, y segundo Flux: %d", uno, dos))
                .subscribe(text -> logger.info(text));
    }

    /**
     * Ejemplo de zipWith para combinar un Usuario y sus Comentarios.
     * Forma 2: usando map sobre la tupla resultante.
     */
    public void ejemploUsuarioComentarioZipWithForma2() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
        Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentarios("jelou, esto es una prueba");
            comentarios.addComentarios("test");
            comentarios.addComentarios("bla bla bla no se que poner");
            return comentarios;
        });

        Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono.zipWith(comentariosMono)
                .map(tuple -> new UsuarioComentarios(tuple.getT1(), tuple.getT2()));

        usuarioComentariosMono.subscribe(uc -> logger.info(uc.toString()));
    }

    /**
     * Ejemplo de zipWith para combinar un Usuario y sus Comentarios.
     * Forma directa con función de combinación.
     */
    public void ejemploUsuarioComentarioZipWith() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
        Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentarios("jelou, esto es una prueba");
            comentarios.addComentarios("test");
            comentarios.addComentarios("bla bla bla no se que poner");
            return comentarios;
        });

        Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono.zipWith(
                comentariosMono,
                (usuario, comentariosUsuario) -> new UsuarioComentarios(usuario, comentariosUsuario)
        );

        usuarioComentariosMono.subscribe(uc -> logger.info(uc.toString()));
    }

    /**
     * Ejemplo de flatMap para combinar un Usuario con sus Comentarios.
     */
    public void ejemploUsuarioComentarioFlatMap() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
        Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentarios("jelou, esto es una prueba");
            comentarios.addComentarios("test");
            comentarios.addComentarios("bla bla bla no se que poner");
            return comentarios;
        });

        usuarioMono.flatMap(u -> comentariosMono.map(c -> new UsuarioComentarios(u, c)))
                .subscribe(uc -> logger.info(uc.toString()));
    }

    /**
     * Método auxiliar que crea un Usuario de ejemplo.
     */
    public Usuario crearUsuario() {
        return new Usuario("Andres", "Guzman");
    }

    /**
     * Ejemplo de collectList.
     * Convierte un Flux de usuarios en una lista única (Mono<List<Usuario>>).
     */
    public void ejemploCollectList() {
        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Andres", "Guzman"));
        usuariosList.add(new Usuario("John", "Locke"));
        usuariosList.add(new Usuario("pedro", "picapiedra"));
        usuariosList.add(new Usuario("Bruce", "lee"));
        usuariosList.add(new Usuario("Bruce", "willis"));

        Flux.fromIterable(usuariosList)
                .collectList()
                .subscribe(list -> list.forEach(item -> logger.info(item.toString())));
    }

    /**
     * Ejemplo de map + flatMap para filtrar y transformar nombres.
     */
    public void ejemploToString() {
        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Andres", "Guzman"));
        usuariosList.add(new Usuario("John", "Locke"));
        usuariosList.add(new Usuario("pedro", "picapiedra"));
        usuariosList.add(new Usuario("Bruce", "lee"));
        usuariosList.add(new Usuario("Bruce", "willis"));

        Flux.fromIterable(usuariosList)
                .map(usuario -> usuario.getNombre().toUpperCase()
                        .concat(" ")
                        .concat(usuario.getApellido().toUpperCase()))
                .flatMap(nombre -> nombre.contains("BRUCE") ? Mono.just(nombre) : Mono.empty())
                .map(String::toLowerCase)
                .subscribe(usuario -> logger.info(usuario));
    }

    /**
     * Ejemplo de flatMap para transformar una lista de Strings en Usuarios.
     * Se filtran solo los de nombre "Bruce".
     */
    public void ejemploFlatmap() {
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Andres Guzman");
        usuariosList.add("John Locke");
        usuariosList.add("pedro picapiedra");
        usuariosList.add("Marta habla");
        usuariosList.add("Diego perea");
        usuariosList.add("Bruce lee");
        usuariosList.add("Bruce Willis");

        Flux.fromIterable(usuariosList)
                .map(nombre -> new Usuario(
                        nombre.split(" ")[0].toUpperCase(),
                        nombre.split(" ")[1].toUpperCase()))
                .flatMap(usuario -> usuario.getNombre().equalsIgnoreCase("bruce") ?
                        Mono.just(usuario) : Mono.empty())
                .map(usuario -> {
                    usuario.setNombre(usuario.getNombre().toLowerCase());
                    return usuario;
                })
                .subscribe(usuario -> logger.info(usuario.toString()));
    }

    /**
     * Ejemplo de creación de Flux a partir de un Iterable.
     * - Transforma Strings en objetos Usuario.
     * - Filtra solo los que se llaman Bruce.
     * - Muestra el proceso con doOnNext y map.
     */
    public void ejemploIterable() {
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Andres Guzman");
        usuariosList.add("John Locke");
        usuariosList.add("pedro picapiedra");
        usuariosList.add("Marta habla");
        usuariosList.add("Diego perea");
        usuariosList.add("Bruce lee");
        usuariosList.add("Bruce Willis");

        Flux<String> nombres = Flux.fromIterable(usuariosList);

        Flux<Usuario> usuarios = nombres
                .map(nombre -> new Usuario(
                        nombre.split(" ")[0].toUpperCase(),
                        nombre.split(" ")[1].toUpperCase()))
                .filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
                .doOnNext(usuario -> {
                    if (usuario == null) {
                        throw new RuntimeException("Nombres no pueden ser vacíos");
                    }
                    System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
                })
                .map(usuario -> {
                    usuario.setNombre(usuario.getNombre().toLowerCase());
                    return usuario;
                });

        usuarios.subscribe(
                e -> logger.info(e.toString()),
                error -> logger.error(error.getMessage()),
                () -> logger.info("Terminó la ejecución del observable con éxito")
        );
    }
}
