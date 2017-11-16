package aplicatie;

import java.io.Serializable;

/**
 *
 * @author ccojo
 */
//    Bucureşti - 021 xxx xx xx  sau 031 xxx xx xx
//    Alte judete - 02yy xxx xxx sau 03yy xxx xxx
public class NrFix extends NrTel implements Serializable{
    private static final long serialVersionUID = 1L;
    
    public NrFix(String nrFix){
        super(nrFix);
        this.tipTel = "fix";
    }

    @Override
    public boolean valideazaNr() {
        return valideazaNr(this.nrTel);
    }
    
    private boolean valideazaNr(String nr){
        if(this.nrTel.length() != 10 || !this.nrTel.matches("[0-9]+")){
            throw new IllegalArgumentException("Numarul de telefon trebuie format din 10 caractere numerice!");
        }
        if(!this.nrTel.startsWith("0")){
            throw new IllegalArgumentException("Prefix telefon fix invalid - prima cifra nu este 0");
        }
        if(!this.nrTel.substring(1, 2).matches("[2-3]+")){
            throw new IllegalArgumentException("Prefix telefon fix invalid - a doua cifra poate fi doar 2 sau 3");
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
