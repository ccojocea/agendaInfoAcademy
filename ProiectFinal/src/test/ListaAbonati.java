package test;

import aplicatie.Abonat;
import aplicatie.CarteDeTelefon;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 * @author ccojo
 */

//TO DO:
//metoda generatoare de CNP-uri unice valide pentru verificarile din clasa Abonat
public class ListaAbonati {
    private static final String NUMEPATH = "src"+File.separator+"test"+File.separator+"nume.txt";
    private static final String PRENUMEPATH = "src"+File.separator+"test"+File.separator+"prenume.txt";
    private static final String CNPPATH = "src"+File.separator+"test"+File.separator+"cnp.txt";
    public CarteDeTelefon mTest = new CarteDeTelefon();
    public List<String> numeList;
    public List<String> prenumeList;
    public List<String> cnpList;
    public int tipTel;
    public String nume;
    public String prenume;
    public String cnp;
    public String tel;
    
    public String getNume(){
        int i = (int)(Math.random()*numeList.size());
        return numeList.get(i);
    }
    
    public String getPrenume(){
        int i = (int)(Math.random()*prenumeList.size());
        return prenumeList.get(i);
    }
    
    public String getCNP(int i){
        return cnpList.get(i);
    }
    
    public String getNrMobil(){
        int a = 0;
        int b = 7;
        int c = 2 + (int)(Math.random() * ((7 - 2) + 1));
        int d = (int)(Math.random()* 9);
        int e = (int)(Math.random()* 9);
        int f = (int)(Math.random()* 9);
        int g = (int)(Math.random()* 9);
        int h = (int)(Math.random()* 9);
        int i = (int)(Math.random()* 9);
        int j = (int)(Math.random()* 9);
   
        return a+""+b+""+c+""+d+""+e+""+f+""+g+""+h+""+i+""+j;
    }
    
    public String getNrFix(){
        int a = 0;
        int b = (int)(Math.random()*1) + 2;
        int c = (int)(Math.random()* 9);
        int d = (int)(Math.random()* 9);
        int e = (int)(Math.random()* 9);
        int f = (int)(Math.random()* 9);
        int g = (int)(Math.random()* 9);
        int h = (int)(Math.random()* 9);
        int i = (int)(Math.random()* 9);
        int j = (int)(Math.random()* 9);
   
        return a+""+b+""+c+""+d+""+e+""+f+""+g+""+h+""+i+""+j;
    }
    
    public CarteDeTelefon letsRandomThis(){
        CarteDeTelefon carte = new CarteDeTelefon();
        int minLength = cnpList.size(); 
        System.out.println("CNP List Size: " + minLength);
        int iterator = 0;
        while(iterator < minLength){
            nume = getNume();
            prenume = getPrenume();
            cnp = getCNP(iterator);
            tipTel = (int) (Math.random() * 2);
            if(tipTel == 0){
                tel = getNrMobil();
                System.out.println(nume + " " + prenume + " " + cnp + " Mobil " + tel);
                aplicatie.NrMobil nrTel = new aplicatie.NrMobil(tel);
                carte.adauga(nume, prenume, cnp, nrTel);
            } else if (tipTel == 1) {
                tel = getNrFix();
                System.out.println(nume + " " + prenume + " " + cnp + " Fix " + tel);
                aplicatie.NrFix nrTel = new aplicatie.NrFix(tel);
                carte.adauga(nume, prenume, cnp, nrTel);
            } else if (tipTel > 1) {
                System.out.println("Something fucked up in your math!");
            }
            iterator++;
        }
        return carte;
    }
    
    public ArrayList<Abonat> getArrayListAbonat(CarteDeTelefon carte){
        ArrayList<Abonat> ppl = new ArrayList<>();
        for(int i = 0; i < carte.getRowCount(); i++){
            ppl.add(carte.getElementAt(i));
        }
        return ppl;
    }
       
    static List readFile(String path, Charset encoding)throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path), encoding);
        return lines;
    }
    
    public List readFileJar(String path)throws IOException {
        InputStream in = getClass().getResourceAsStream(path);
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        String line;
        List<String> lines = new ArrayList();
        while ((line=input.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }
    
    public CarteDeTelefon adaugareDateRandom() throws IOException{
        this.numeList = readFile(NUMEPATH, StandardCharsets.UTF_8);
        this.prenumeList = readFile(PRENUMEPATH, StandardCharsets.UTF_8);
        this.cnpList = readFile(CNPPATH, StandardCharsets.UTF_8);
        return this.letsRandomThis();
    }
    
    public CarteDeTelefon adaugareDateRandomJar() throws IOException{
        this.numeList = readFileJar("nume.txt");
        this.prenumeList = readFileJar("prenume.txt");
        this.cnpList = readFileJar("cnp.txt");
        return this.letsRandomThis();
    }
    
}
