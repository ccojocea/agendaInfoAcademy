package aplicatie;

import java.io.Serializable;

/**
 *
 * @author ccojo
 */
public class Shareware implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private boolean registered;
    private int curiousNumber;
    
    public Shareware(int x){
        this.registered = true;
        if(x == 1){
            this.curiousNumber = 89124987;
        } else if (x == 2){
            this.curiousNumber = 100;
        }
        
    }
    
    public String toString(){
        return registered + Integer.toString(curiousNumber);
    }
}
