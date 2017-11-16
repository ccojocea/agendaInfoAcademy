package aplicatie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author ccojo
 */
public class CarteDeTelefon extends AbstractTableModel implements Serializable{
    private static final long serialVersionUID = 1L;
        
    private ArrayList<Abonat> ppl = new ArrayList<>();
       
    public void adauga(Abonat a){
        ppl.add(a);
        fireTableDataChanged();
    }
    
    //nu se va adauga in baza de date un alt abonat cu un CNP existent in baza de date
    public void adauga(String n, String p, String c, NrTel nr) {
        if(valideazaCNPunic(c)){
            throw new IllegalArgumentException("Acest CNP exista deja in baza de date!");
        }
        adauga(Abonat.getInstance(n, p, c, nr));
    }
    
    public void adaugaDebug(ArrayList<Abonat> ppl){
        this.ppl.clear();
        this.ppl.addAll(ppl);
        fireTableDataChanged();
    }
    
    public void sterge(Abonat abonat){
        ppl.remove(abonat);
        fireTableDataChanged();
    }
    
    public void sterge(String cnp){
        for(int i = 0; i < ppl.size(); i++){
            if(ppl.get(i).getCNP().equals(cnp)){
                sterge(ppl.get(i));
            }
        }
    }
    
    public void modifica(String cnpLoad, String nume, String prenume, String cnp, NrTel nrTel){
        for(int i = 0; i < ppl.size(); i++){
            if(ppl.get(i).getCNP().equals(cnpLoad)){
                ppl.get(i).setNume(nume);
                ppl.get(i).setPrenume(prenume);
                ppl.get(i).setCNP(cnp);
                ppl.get(i).setNrTel(nrTel);
            }
        }
        fireTableDataChanged();
    }
    
    public void sortare(int x, char directie){
        if(directie == 'a'){
            switch(x){
            case 1:
                sort(new Abonat.dupaNume());
                break;
            case 2:
                sort(new Abonat.dupaPrenume());
                break;
            case 3:
                sort(new Abonat.dupaCNP());
                break;
            case 4:
                sort(new Abonat.dupaNrTel());
                break;
            default:
                throw new IllegalArgumentException("Doar valori intre 1-4 pentru sortare");
            }
        } else if (directie == 'd'){
            switch(x){
            case 1:
                sortRev(new Abonat.dupaNume());
                break;
            case 2:
                sortRev(new Abonat.dupaPrenume());
                break;
            case 3:
                sortRev(new Abonat.dupaCNP());
                break;
            case 4:
                sortRev(new Abonat.dupaNrTel());
                break;
            default:
                throw new IllegalArgumentException("Doar valori intre 1-4 pentru sortare");
            }
        }
    }
    
    public void sort(Comparator c){
        Collections.sort(ppl, c);
        fireTableDataChanged();
    }
    
    public void sortRev(Comparator c){
        Collections.sort(ppl, Collections.reverseOrder(c));
        fireTableDataChanged();
    }
    
    public Abonat cautaCNP(String cnp){
        Abonat a = null;
        for (int i = 0; i < ppl.size(); i++){
            if(ppl.get(i).getCNP().equals(cnp)){
                a = ppl.get(i);
            }
        }
        return a;
    }
    
    public boolean valideazaCNPunic(String c){
        boolean result = false;
        for (int i = 0; i < this.ppl.size(); i++) {
            if(this.ppl.get(i).getCNP().equals(c)){
                result = this.ppl.get(i).getCNP().equals(c);
            }
        }
        return result;
    }
        
    @Override
    public String toString(){
        return "Agenda contine: " + ppl.size() + " inregistrari.";
    }
    
    public Abonat getElementAt(int index){
        return ppl.get(index);
    }
    
    @Override
    public String getColumnName(int index){
        switch(index){
            case 0:
                return "Nume";
            case 1:
                return "Prenume";
            case 2:
                return "CNP";
            case 3:
                return "Telefon";
            default:
                return null;
        }
    }
    
    @Override
    public int getRowCount() {
        return ppl.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Abonat abonat = ppl.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return abonat.getNume();
            case 1:
                return abonat.getPrenume();
            case 2:
                return abonat.getCNP();
            case 3:
                return abonat.getNrTel();
            default:
                throw new IndexOutOfBoundsException();
        }
    }
    
    public CarteDeTelefon clone() {
        CarteDeTelefon clona = new CarteDeTelefon();
        for(int i = 0; i<ppl.size(); i++){
            Abonat b = ppl.get(i);
            Abonat a = Abonat.getInstance(b.getNume(), b.getPrenume(), b.getCNP(), b.getNrTel());
            clona.adauga(a);
            }
        return clona;
    }
}


