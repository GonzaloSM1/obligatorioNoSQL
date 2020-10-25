package com.example.obligatorionosql;

import com.mongodb.DBCollection;
import dev.morphia.Morphia;
import com.mongodb.MongoClient;
import models.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class Controller {

    private Morphia morphia = new Morphia();
    private MongoClient mongoClient = new MongoClient();
    private String DBName = "miniTwitter";

    @RequestMapping(value= "/crearusuario/{email}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void crearUsuario(String email){

        Usuario usuario = new Usuario(email);

        morphia.createDatastore(mongoClient, DBName).save(usuario);

    }

}
