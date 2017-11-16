package aplicatie;

import java.io.Serializable;

/**
 *
 * @author ccojo
 */
//072x xxx xxx - Vodafone
//073x xxx xxx - Vodafone
//074x xxx xxx - Orange
//075x xxx xxx - Orange     
//076x xxx xxx – Cosmote
//077x xxx xxx – Digimobil
//078x xxx xxx – Telemobil
public class NrMobil extends NrTel implements Serializable{
    private static final long serialVersionUID = 1L;
    
    public NrMobil(String nrMob){
        super(nrMob);
        this.tipTel = "mobil";
    }
    
    @Override
    public boolean valideazaNr() {
        return valideazaNr(this.nrTel);
    }

    private boolean valideazaNr(String nr) {
        //forma tel mobil verificata 07xx xxx xxx
        if(this.nrTel.length() != 10 || !this.nrTel.matches("[0-9]+")){
            throw new IllegalArgumentException("Numarul de telefon trebuie format din 10 caractere numerice!");
        }
        String prefix = this.nrTel.substring(0, 2);
        if(!prefix.equals("07")){
            throw new IllegalArgumentException("Prefix telefon mobil invalid");
        }
        return true;
    }
    
//    @Override
//    void setNrTel(NrTel tel) {
//        this.nrTel = tel.toString();
//    }
    
    public String getTipTel(){
        return this.tipTel;
    }
    
    @Override
    public int compare(Object o1, Object o2) {
        NrTel nr1 = (NrTel) o1;
        NrTel nr2 = (NrTel) o2;
        return nr1.getNrTel().compareTo(nr2.getNrTel());
    }
}
