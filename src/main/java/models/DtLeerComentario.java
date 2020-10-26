package models;

public class DtLeerComentario {

    private String usrId;
    private String comId;
    private String texto;
    private int cantMeGusta;
    private int cantNoMeGusta;

    public DtLeerComentario(String usrId, String comId, String texto, int cantMeGusta, int cantNoMeGusta) {
        this.usrId = usrId;
        this.comId = comId;
        this.texto = texto;
        this.cantMeGusta = cantMeGusta;
        this.cantNoMeGusta = cantNoMeGusta;
    }

    public String getUsrId() {
        return usrId;
    }

    public String getComId() {
        return comId;
    }

    public String getTexto() {
        return texto;
    }

    public int getCantMeGusta() {
        return cantMeGusta;
    }

    public int getCantNoMeGusta() {
        return cantNoMeGusta;
    }
}
