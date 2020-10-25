package models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity("models.Comentario")
public class Comentario {
    @Id
    private ObjectId id;
    private String texto;
    private ObjectId userid;

    public Comentario(String texto, ObjectId userid) {
        this.texto = texto;
        this.userid = userid;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public ObjectId getUsuario() {
        return userid;
    }

    public void setUsuario(ObjectId userid) {
        this.userid = userid;
    }

}
