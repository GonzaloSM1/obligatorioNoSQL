package com.example.obligatorionosql;

import com.mongodb.*;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.internal.MorphiaCursor;
import io.swagger.models.Model;
import models.Usuario;
import models.Comentario;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/")
public class Controller {

    private Morphia morphia = new Morphia();
    private MongoClient mongoClient = new MongoClient();
    private String DBName = "miniTwitter";
    private Datastore ds = morphia.createDatastore(mongoClient, DBName);

    @RequestMapping(value= "/crearusuario/{email}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void crearUsuario(String email){

        if(existeUser(email)){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Ya existe usuario");
        }

        Usuario usuario = new Usuario(email);

        morphia.createDatastore(mongoClient, DBName).save(usuario);

    }

    @RequestMapping(value= "/crearcomentario/{texto}/{email}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void crearComentario(String texto, String email) {
            if(texto.length()>256){
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Texto mayor a 256");
            }

            ObjectId userId = getUserId(email);

            Comentario comentario = new Comentario(texto, userId);
            ds.save(comentario);
    }

    private ObjectId getUserId(String email){
        DBCollection collection = ds.getCollection(Usuario.class);
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("email", email);

        if(!collection.find(searchQuery).hasNext()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No existe usuario");
        }

        DBObject user = collection.find(searchQuery).next();
        return new ObjectId(((BasicDBObject) user).getString("_id"));
        
    }

    private boolean existeUser(String email){
        DBCollection collection = ds.getCollection(Usuario.class);
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("email", email);

        if(!collection.find(searchQuery).hasNext()) {
            return false;
        }

        DBObject user = collection.find(searchQuery).next();
        return true;
    }

}
