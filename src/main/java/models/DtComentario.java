package models;
import org.bson.types.ObjectId;

public class DtComentario {
    private String comId;
    private String comText;


    public DtComentario(String comId, String comText) {
        this.comId = comId;
        this.comText = comText;

    }

    public String getComId() {
        return comId;
    }

    public String getComText() {
        return comText;
    }

}
