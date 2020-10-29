package models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity ("models.Emocion")
public class Emocion {
    @Id
    private ObjectId Id;
    private boolean meGusta;
    //private Usuario usuario;
    //private Comentario comentario;
    private ObjectId userId;
    private ObjectId commentId;

    public Emocion(boolean meGusta, ObjectId usuario, ObjectId comentario) {
        this.meGusta = meGusta;

        this.userId = usuario;
        this.commentId = comentario;
    }

    public boolean isMeGusta() {
        return meGusta;
    }

    public void setMeGusta(boolean meGusta) {
        this.meGusta = meGusta;
    }

    public ObjectId getUsuario() {
        return userId;
    }

    public void setUsuario(ObjectId usuario) {
        this.userId = usuario;
    }

    public ObjectId getComentario() {
        return commentId;
    }

    public void setComentario(ObjectId comentario) {
        this.commentId = comentario;
    }
}
