package aplicatie;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author ccojo
 */
public class Abonat implements Serializable, Comparator{
    private static final long serialVersionUID = 1L;
    
    private String nume;
    private String prenume;
    private String cnp;
    private NrTel nrTel;
          
    private Abonat(String nume, String prenume, String cnp, NrTel nrTel){
        this.nume = primaLiteraMare(nume);
        this.prenume = primaLiteraMare(prenume);
        this.cnp = cnp;
        this.nrTel = nrTel;
    }
    
    public static Abonat getInstance(String nume, String prenume, String cnp, NrTel nrTel){
        if(!valideazaNume(nume) || !valideazaPrenume(prenume) || !valideazaCNP(cnp) || !nrTel.valideazaNr()){
            throw new RuntimeException("Validare abonat invalida!");
        } else {
            return new Abonat(nume, prenume, cnp, nrTel);
        }
    }
    
    //getteri
    public String getNume(){
        return nume;
    }
    
    public String getPrenume(){
        return prenume;
    }
    
    public String getCNP(){
        return cnp;
    }
    
    public NrTel getNrTel(){
        return nrTel;
    }
    
    public String getTipTel(){
        return nrTel.getTipTel();
    }
    
    //setteri
    public void setNume(String nume){
        if(valideazaNume(nume)){
            this.nume = primaLiteraMare(nume);
        }
    }
    
    public void setPrenume(String nume){
        if(valideazaPrenume(nume)){
            this.prenume = primaLiteraMare(nume);
        }
    }
        
    public void setCNP(String cnp){
        if(valideazaCNP(cnp)){
            this.cnp = cnp;
        }
    }
    
    public void setNrTel(NrTel nrTel){
        if(nrTel.valideazaNr()){
            this.nrTel = nrTel;
        }
    }
    
    //aplicata automat numelui/prenumelui sa fie salvat doar cu prima litera mare.
    private static String primaLiteraMare(String nume) {
        char[] array = nume.toCharArray();
        for(int i = 0; i<array.length; i++){
            array[i] = Character.toLowerCase(array[i]);
        }
        array[0] = Character.toUpperCase(array[0]);
        return new String(array);
    }
    
    //validari
    private static boolean valideazaNuPre(String nume, String tip){
        if (nume == null || nume.length() < 3){
            throw new IllegalArgumentException(tip + " trebuie format din cel putin 3 litere!");
        }
        if (nume.length() > 30){
            throw new IllegalArgumentException(tip + " ar trebui scurtat un pic");
        }
        char[] chars = nume.toCharArray();
        for (char c : chars){
            if(!Character.isLetter(c)){
                throw new IllegalArgumentException(tip + " trebuie format doar din litere!");
            }
        }
        return true;
    }
    
    private static boolean valideazaNume(String nume) {
        return valideazaNuPre(nume, "Numele");
    }
    
    private static boolean valideazaPrenume(String prenume){
        return valideazaNuPre(prenume, "Prenumele");
    }
    
    private static boolean valideazaCNP(String cnp) {
        //verificare lungime string si caractere numerice
        if (cnp.length() != 13 || !cnp.matches("[0-9]+")){
            throw new IllegalArgumentException("CNP invalid - 13 cifre");
        }
        //verificare ca data de nastere din cnp sa fie corecta (fara an) - De dezactivat for easier testing
        int ziCNP = Integer.parseInt(cnp.substring(5, 7));
        int lunaCNP = Integer.parseInt(cnp.substring(3, 5));
        if(ziCNP < 1 || ziCNP > 31 || lunaCNP < 1 || lunaCNP > 12){
            throw new IllegalArgumentException("CNP invalid - Data de nastere)");
        }
        
        //verificare cifra de control CNP - De dezactivat for easier testing
        String[] strings = cnp.split("");
        int[] ints = new int[strings.length];
        for(int i = 0; i < strings.length; i++){
            ints[i] = Integer.parseInt(strings[i]);
        }
        int suma = ints[0]*2+ints[1]*7+ints[2]*9+ints[3]*1+ints[4]*4+ints[5]*6+ints[6]*3+ints[7]*5+ints[8]*8+ints[9]*2+ints[10]*7+ints[11]*9;
        int rest = suma % 11;
        int control = 0;
        if(rest == 10){
            control = 1;
        } else {
            control = rest;
        }
        if(control != ints[12]){
            throw new IllegalArgumentException("CNP Invalid - Cifra de control");
        }
        
        //verificare ca prima cifra din cnp sa fie 1,2,5,6,7,8,9
        switch (Integer.parseInt(cnp.substring(0,1))){
            case 3:
            case 4:
                throw new IllegalArgumentException("CNP cam vechi - prima cifra indica anii 1800-1899 - chiar mai traieste?");
            default:
                break;
        }
        return true;
    }
    
    @Override
    public String toString(){
        return nume + " " + prenume + " " + cnp + " " + nrTel;
    }
    
    public boolean equals(Abonat other){
        return this.cnp.equals(other.cnp);
    }

    //override obligatoriu compare (nu este folosit in varianta actuala a programului)
    //am incercat sa rescriu ca sa imi dea -1 / 1 / 0 dar doar in ideea ca in 
    //lista noastra nu se poate crea un abonat cu CNP identic cu altul
    @Override
    public int compare(Object o1, Object o2) {
        Abonat a1 = (Abonat) o1;
        Abonat a2 = (Abonat) o2;
        boolean equal = false;
        if (a1.getNume().compareTo(a2.getNume()) == 0){
            equal = true;
        }
        if (a1.getPrenume().compareTo(a2.getPrenume()) == 0){
            equal = true;
        }
        int comparatorCNP = 0;
        if (a1.getCNP().compareTo(a2.getCNP()) == 0){
            equal = true;
        } else if (a1.getCNP().compareTo(a2.getCNP()) > 0){
            comparatorCNP = 1;
        } else if (a1.getCNP().compareTo(a2.getCNP()) < 0){
            comparatorCNP = -1;
        }
        if (a1.getNrTel().toString().compareTo(a2.getNrTel().toString()) == 0){
            equal = true;
        }
        if(equal){
            return 0;
        }else return comparatorCNP;
    }

    static class dupaNume implements Comparator<Abonat>{
        public int compare(Abonat a1, Abonat a2)
        {
            String nume1 = a1.getNume();
            String nume2 = a2.getNume();

            return nume1.compareTo(nume2);
        }
    }
    
    static class dupaPrenume implements Comparator<Abonat>{
        public int compare(Abonat a1, Abonat a2)
        {
            String nume1 = a1.getPrenume();
            String nume2 = a2.getPrenume();

            return nume1.compareTo(nume2);
        }
    }
        
    static class dupaCNP implements Comparator<Abonat>{
        public int compare(Abonat a1, Abonat a2)
        {
            String cnp1 = a1.getCNP();
            String cnp2 = a2.getCNP();

            return cnp1.compareTo(cnp2);
        }
    }
            
    static class dupaNrTel implements Comparator<Abonat>{
        public int compare(Abonat a1, Abonat a2)
        {
            String tel1 = a1.getNrTel().toString();
            String tel2 = a2.getNrTel().toString();

            return tel1.compareTo(tel2);
        }
    }
}
