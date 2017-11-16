package aplicatie;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author ccojo
 */
abstract class NrTel implements Serializable, Comparator{
    private static final long serialVersionUID = 1L;
    
    protected String nrTel;
    protected String tipTel;
    
    public NrTel(String nr){
        this.nrTel = nr;
    }
    
    public String toString(){
        return nrTel;
    }
    
//    abstract void setNrTel(NrTel nrTel);
    
    public String getNrTel(){
        return this.nrTel;
    }
    
    public String getTipTel(){
        return this.tipTel;
    }
    
    abstract boolean valideazaNr();    
    
}
