package com.example.obligatorionosql;

import dev.morphia.Morphia;
import models.Comentario;
import models.Emocion;
import models.Usuario;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching

@SpringBootApplication
public class ObligatorionosqlApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(ObligatorionosqlApplication.class, args);

        Morphia morphia = new Morphia();
        morphia.map(Usuario.class);
        morphia.map(Comentario.class);
        morphia.map(Emocion.class);
        
    }

}
