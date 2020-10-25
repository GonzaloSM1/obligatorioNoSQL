package models;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;
import java.util.List;

@Entity("models.Usuario")

public class Usuario {
    @Id
    private ObjectId id;
    private String email;

    @Reference
    private List<Comentario> comentarios;

    public Usuario(String email) {
        this.email = email;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
