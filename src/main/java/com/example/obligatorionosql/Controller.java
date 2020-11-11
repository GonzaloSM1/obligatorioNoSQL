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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import java.util.*;

@RestController
@RequestMapping("/")
public class Controller {

    private Morphia morphia = new Morphia();
    private MongoClient mongoClient = new MongoClient();
    private String DBName = "miniTwitter";
    private Datastore ds = morphia.createDatastore(mongoClient, DBName);
    private Jedis jedis = new Jedis();
    private long cantcach = 10;
    private long indcach = contadorcache();



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
  //  @CachePut(value="Comentario")

//  @CachePut(value="#com:#id")
//  @RequestMapping(method = RequestMethod.POST)
//  @ResponseStatus(HttpStatus.OK)
//  public Comentario savecache(Comentario com){
//      // System.out.println(id);
//      System.out.println(com);
//      ds.save(com);
//      return com;
//  }

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
        String key =  ds.getKey(comentario).getId().toString();
        addCom(comentario, key);

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

   // @Cacheable(value = "/leercomentario/{comId}", key = "#comId")
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
        if(!existeComentario(objIdComId.toString()))
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No existe el comentario");

        if(jedis.exists(comId)){
            return getComCache(comId);
        }
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
        ObjectId comentId;
        if(comId == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El id del comentario no puede ser vacío");

        if(userId == null)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El id del usuario no puede ser vacío");

        if (!existeUser(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No existe el usuario");
        } else {
            usrId = getUserId(userId);

            if (!existeComentario(comId)) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No existe el comentario");
            } else {
                comentId = getCommentId(comId);
                DBCollection collection = ds.getCollection(Emocion.class);
                BasicDBObject searchQuery = new BasicDBObject();
                //BasicDBObject ops = new BasicDBObject();
                BasicDBObject query = new BasicDBObject();
                searchQuery.put("userId", usrId);
                searchQuery.put("commentId", comentId);
                Emocion emocion1 = new Emocion(emocion, usrId, comentId);
                if (!collection.find(searchQuery).hasNext()){
                    if(jedis.exists(comId)){
                        emocCach(comId, emocion);
                    }
                    ds.save(emocion1);
                } else {
                    Query<Emocion> query2 = ds.createQuery(Emocion.class);
                    query2.and(
                            query2.criteria("userId").equal(usrId),
                            query2.criteria("commentId").equal(comentId)
                    );

                    DBCollection collection1 = ds.getCollection(Emocion.class);

                    DBObject emoc = collection1.find(searchQuery).next();

                    //Obtengo texto del comentario
                    boolean meGusta = (((BasicDBObject) emoc).getBoolean("meGusta"));
                    if(jedis.exists(comId)) {
                        actemoCach(meGusta, comentId, emocion );
                    }
                    UpdateOperations ops = ds
                            .createUpdateOperations(Emocion.class)
                            .set("meGusta", emocion);
                    ds.update(query2, (UpdateOperations<Query<Emocion>>) ops);
                }

            }
        }

    }


    private ObjectId getUserId(String email){
        DBCollection collection = ds.getCollection(Usuario.class);
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("email", email);

        if(!collection.find(searchQuery).hasNext()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "No existe usuario2");
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
        if(jedis.exists(id.toString())){
            return true;
        }
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

    private void addCom(Comentario com, String key){
        long cant = jedis.dbSize();
        if (cant >= cantcach) {
            ScanParams params = new ScanParams();
            params.match("*");
            ScanResult<String> scanResult = jedis.scan("0", params);
            List<String> keys = scanResult.getResult();
            int max = Integer.MAX_VALUE;
            String delkey = "";
            for (String ky : keys) {
                int var1 = Integer.parseInt(jedis.hget(ky, "index"));
                if (var1 < max) {
                    max = var1;
                    delkey = ky;
                }
            }
            System.out.print(delkey + "esta es la q borro \n");
            System.out.print(indcach);
            jedis.del(delkey);
        }

            jedis.hset(key,"texto", com.getTexto());
            jedis.hset(key,"usrId", com.getUsuario().toString());
            jedis.hset(key,"cantMeGusta", "0");
            jedis.hset(key,"cantNoMeGusta", "0");
            String a = Long.toString(indcach);
            jedis.hset(key,"index", a);
            indcach++;
    }
    private long contadorcache(){
        if (jedis.dbSize() != 0){
            ScanParams params = new ScanParams();
            params.match("*");
            ScanResult<String> scanResult = jedis.scan("0", params);
            List<String> keys = scanResult.getResult();
            for (String ky : keys) {
                int var1 = Integer.parseInt(jedis.hget(ky,"index"));
                if (var1 > indcach){
                    indcach = var1;
                }
            }
            indcach++;
            return indcach;
        }else{
            return 0;
        }
    }
    private DtLeerComentario getComCache(String comid){
        System.out.print("Tomo de cache");
        return new DtLeerComentario(jedis.hget(comid,"usrId"), comid, jedis.hget(comid,"texto"), Integer.parseInt(jedis.hget(comid,"cantMeGusta")), Integer.parseInt(jedis.hget(comid,"cantNoMeGusta")));
    }

    private void emocCach(String comid, Boolean emoc){
        if(emoc){
            int cmg = Integer.parseInt(jedis.hget(comid, "cantMeGusta"));
            cmg++;
            jedis.hset(comid, "cantMeGusta", Integer.toString(cmg));
        }else{
            int cng = Integer.parseInt(jedis.hget(comid, "cantNoMeGusta"));
            cng++;
            jedis.hset(comid, "cantNoMeGusta", Integer.toString(cng));
        }
        jedis.hset(comid, "index", Long.toString(indcach));
        indcach++;
    }
    private void actemoCach(Boolean vmeGusta, ObjectId comid, Boolean nmeGusta){
        if (vmeGusta == nmeGusta) {
            jedis.hset(String.valueOf(comid), "index", Long.toString(indcach));
            indcach++;
        }else{
            if(vmeGusta){
                int cmg = Integer.parseInt(jedis.hget(String.valueOf(comid), "cantMeGusta"));
                cmg--;
                jedis.hset(String.valueOf(comid), "cantMeGusta", Integer.toString(cmg));
                int cmng = Integer.parseInt(jedis.hget(String.valueOf(comid), "cantNoMeGusta"));
                cmng++;
                jedis.hset(String.valueOf(comid), "cantNoMeGusta", Integer.toString(cmng));
            }else{
                int cmg = Integer.parseInt(jedis.hget(String.valueOf(comid), "cantMeGusta"));
                cmg++;
                jedis.hset(String.valueOf(comid), "cantMeGusta", Integer.toString(cmg));
                int cmng = Integer.parseInt(jedis.hget(String.valueOf(comid), "cantNoMeGusta"));
                cmng--;
                jedis.hset(String.valueOf(comid), "cantNoMeGusta", Integer.toString(cmng));
            }
        }




    }

}
