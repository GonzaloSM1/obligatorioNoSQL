package models;
import org.bson.types.ObjectId;

public class DtComentario {
    private String usrId;
    private String comId;
    private String comText;


    public DtComentario(String usrId, String comId, String comText) {
        this.usrId = usrId;
        this.comId = comId;
        this.comText = comText;

    }

    public String getComId() {
        return comId;
    }

    public String getComText() {
        return comText;
    }

    public String getUsrId() {
        return usrId;
    }
}
