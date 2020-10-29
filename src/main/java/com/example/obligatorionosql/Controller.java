package com.example.obligatorionosql;

import com.mongodb.*;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import models.DtLeerComentario;
import models.Emocion;
import models.Usuario;
import models.Comentario;
import models.DtComentario;
import models.DtComentarioPers;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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

        if(email == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El email no puede ser vacío");

        if(existeUser(email))
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Ya existe usuario");

        Usuario usuario = new Usuario(email);

        morphia.createDatastore(mongoClient, DBName).save(usuario);

    }

    @RequestMapping(value= "/crearcomentario/{texto}/{email}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void crearComentario(String texto, String email) {

        if(texto == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El texto no puede ser vacío");

        if(email == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El email no puede ser vacío");

        if(texto.length()>256){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Texto mayor a 256");
        }

        ObjectId userId = getUserId(email);

        Comentario comentario = new Comentario(texto, userId);
        ds.save(comentario);
    }

    @RequestMapping(value= "/listarcomentariosusuario/{email}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public DtComentarioPers listarComentariosUsuario(String email) {
        if(email == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El email no puede ser vacío");

        List<DtComentario> listaComent = new ArrayList<>();

        if (existeUser(email)) {

            ObjectId userid = getUserId(email);
            DBCollection collection = ds.getCollection(Comentario.class);

            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("userid", userid);

            List<DBObject> comentarios = collection.find(searchQuery).toArray();

            if(comentarios.size()==0) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No tiene comentarios");
            }

            for (DBObject coment : comentarios) {
                ObjectId comid = new ObjectId( ((BasicDBObject) coment).getString("_id"));

                String text = (((BasicDBObject) coment).getString("texto"));

                DtComentario dtcom = new DtComentario(comid.toString(),text);

                listaComent.add(dtcom);
            }
            DtComentarioPers compers = new DtComentarioPers(userid.toString(),listaComent);

            return compers;

        } else {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "El usuario no existe");
        }

    }

    @RequestMapping(value= "/leercomentario/{comId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public DtLeerComentario leerComentario(String comId) {

        ObjectId objIdComId = null;

        if(comId == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El id no puede ser vacío");

        try {
            objIdComId = new ObjectId(comId);
        }catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El id ingresado no corresponde a un tipo válido (ObjectId)");
        }

        //Exista comentario
        if(!existeConentario(objIdComId))
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No existe el comentario");

        //Obtengo datos de comentario
        DBCollection collection = ds.getCollection(Comentario.class);

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", objIdComId);

        DBObject comentario = collection.find(searchQuery).next();

        //Obtengo texto del comentario
        String text = (((BasicDBObject) comentario).getString("texto"));

        //Obtengo datos del usuario
        ObjectId usrId = new ObjectId( ((BasicDBObject) comentario).getString("userid"));

        //Cantidad de meGusta

        List<Integer> a = cantidadEmociones(comId);
        int meGusta = a.get(0);
        int noMeGusta = a.get(1);

        return new DtLeerComentario(usrId.toString(), comId, text, meGusta, noMeGusta);

    }

    @RequestMapping(value= "/agregarEmocion/{comId}/{email}/{emocion}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void agregarEmocion(String comId, String userId, boolean emocion) {

        ObjectId usrId;
        if (!existeUser(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No existe el usuario");
        } else {
                usrId = getUserId(userId);
                ObjectId comentId;
                if (!existeComentario(comId)) {
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No existe el comentario");
                } else {
                        comentId = getCommentId(comId);
                        Emocion emocion1 = new Emocion(emocion, usrId, comentId);
                        ds.save(emocion1);
                }
        }

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

    private boolean existeComentario(String id) {
        DBCollection collection = ds.getCollection(Comentario.class);
        BasicDBObject searchQuery = new BasicDBObject();
        ObjectId id1 = new ObjectId(id);
        searchQuery.put("_id", id1);
        if (!collection.find(searchQuery).hasNext()){
            return false;

        } else
            return true;
    }

    private ObjectId getCommentId(String id) {
        DBCollection collection = ds.getCollection(Comentario.class);
        BasicDBObject searchQuery = new BasicDBObject();
        ObjectId id1 = new ObjectId(id);
        searchQuery.put("_id", id1);
        if (!collection.find(searchQuery).hasNext()){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No existe comentario");

        }
        DBObject comment = collection.find(searchQuery).next();
        return new ObjectId(((BasicDBObject) comment).getString("_id"));
    }

    private boolean existeConentario(ObjectId comId){
        DBCollection collection = ds.getCollection(Comentario.class);
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("_id", comId);

        if(!collection.find(searchQuery).hasNext()) {
            return false;
        }

        DBObject comentario = collection.find(searchQuery).next();
        return true;
    }

    private List<Integer> cantidadEmociones(String idComentario){
        List<Integer> megusta = new ArrayList<Integer>();
        ObjectId com1 = getCommentId(idComentario);

        DBCollection collection = ds.getCollection(Emocion.class);

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("commentId", com1);

        List<DBObject> emociones = collection.find(searchQuery).toArray();
        int gusta = 0;
        int noGusta = 0;


        for (DBObject emo : emociones) {
            boolean meGusta = (((BasicDBObject) emo).getBoolean("meGusta"));
            if (meGusta){
                gusta ++;
            }
            if (!meGusta){
                noGusta ++;
            }

        }
        megusta.add(0, gusta);
        megusta.add(1, noGusta);
        return megusta;
    }

}
