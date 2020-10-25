package models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity
public class Emocion {
    @Id
    private ObjectId Id;
    private boolean meGusta;

    private Usuario usuario;
    private Comentario comentario;

    public Emocion(boolean meGusta, Usuario usuario, Comentario comentario) {
        this.meGusta = meGusta;
        this.usuario = usuario;
        this.comentario = comentario;
    }

    public boolean isMeGusta() {
        return meGusta;
    }

    public void setMeGusta(boolean meGusta) {
        this.meGusta = meGusta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Comentario getComentario() {
        return comentario;
    }

    public void setComentario(Comentario comentario) {
        this.comentario = comentario;
    }
}
