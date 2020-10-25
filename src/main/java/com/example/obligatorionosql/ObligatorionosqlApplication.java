package com.example.obligatorionosql;

import com.mongodb.MongoClient;
import dev.morphia.Morphia;
import models.Comentario;
import models.Emocion;
import models.Usuario;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ObligatorionosqlApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(ObligatorionosqlApplication.class, args);

        MongoClient mongoClient = new MongoClient();
        Morphia morphia = new Morphia();
        morphia.map(Usuario.class);
        morphia.map(Comentario.class);
        morphia.map(Emocion.class);

        Usuario usuario = new Usuario("juancito69@shemale.com");

        morphia.createDatastore(mongoClient, "miniTwitter").save(usuario);
    }

}
