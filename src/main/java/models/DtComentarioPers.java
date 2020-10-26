package models;

import java.util.List;

public class DtComentarioPers {
    private String usrId;
    private List<DtComentario> comentarios;


    public DtComentarioPers(String usrId, List<DtComentario> comentarios) {
        this.usrId = usrId;
        this.comentarios = comentarios;

    }

    public List<DtComentario> getComentarios() { return comentarios; }

    public String getUsrId() {
        return usrId;
    }
}
