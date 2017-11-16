package gui;

import aplicatie.CarteDeTelefon;
import aplicatie.Shareware;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import exceptii.RegisterException;
import exceptii.SaveException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 *
 * @author ccojo
 */
public class MainFrame extends javax.swing.JFrame {
    private static JFrame mainframe;
    private CarteDeTelefon m = new CarteDeTelefon();
    private List<File> poze = new ArrayList<>();
    private int pozaCurenta;
    private static final String REGVALUE = "true89124987";
    private static final String REGPATH = "src"+File.separator+"agendaFiles"+File.separator+"fregistered.reg";
    private static final File CONFIGPATH = new File("src"+File.separator+"agendaFiles"+File.separator+"config.cfg");
    private static File volatilePath = new File("src"+File.separator+"agendaFiles"+File.separator+"agenda.agd");
    private static final String POZEPATH = "src"+File.separator+"agendaFiles";
    //salvare/incarcare automata din fisier predefinit - disabled (trebuie schimbat si in locurile unde este folosit volatilePath):
    //private static final File DATAPATH = new File("src"+File.separator+"Files"+File.separator+"agenda.agd");
    private String cnpLoad;
    private TableRowSorter<TableModel> rowSorter;
    
    /**
     * Creates new form MainFrame
     */
    public MainFrame() {        
        initComponents();
        
        //pentru implementare cu baza de date - disabled for now
        jToggleButtonDatabase.setEnabled(false);
        jToggleButtonDatabase.setVisible(false);
        
        //optiune debug
        jMenuDebug.setEnabled(false);
        jMenuDebug.setVisible(false);
                
        //verificare existenta fisier inregistrare aplicatie si continut:
        try {
            File fregistered = new File(REGPATH);
            if (fregistered.exists() && fregistered.canRead()){
                ObjectInputStream ois;
                try (FileInputStream fis = new FileInputStream(fregistered)) {
                    ois = new ObjectInputStream(fis);
                    Shareware isRegistered = (Shareware) ois.readObject();
                    if (isRegistered.toString().equals(REGVALUE)) {
                        registerApp();
                    } else if (isRegistered.toString().equals("true100")) {
                        registerApp();
                        jMenuDebug.setEnabled(true);
                        jMenuDebug.setVisible(true);
                    }
                }
                ois.close();
            } else {
                incarcaPoze();
            }
        } catch (IOException | ClassNotFoundException e) {
            incarcaPoze();
            JOptionPane.showMessageDialog(
                    this,
                        "Posibila corupere a datelor de inregistrare. Incercati sa inregistrati aplicatia din nou." + 
                        System.lineSeparator() +        
                        "Nume si cod eroare: " + e.getMessage(),
                    "OOOPS!",
                    JOptionPane.ERROR_MESSAGE
            );
            File fregistered = new File(REGPATH);
            if (fregistered.exists() && fregistered.canWrite()){
                if(fregistered.delete()){
                    JOptionPane.showMessageDialog(this, "Fisierul corupt a fost sters", "Ma simt mai bine", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Fisierul nu a putut fi sters", "Tot trist", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        
        //incarcare date si setare in tabel
        m = incarcareInfoAuto();
        jTable1.setModel(m);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        //popup menu tabel
        jMenuItemSterge.setText("Sterge");
        jMenuItemModifica.setText("Modifica");
        jPopupMenu1.add(jMenuItemSterge);
        jPopupMenu1.add(jMenuItemModifica);
        jTable1.setComponentPopupMenu(jPopupMenu1);
        
        //dezactivare functionalitate de baza "Enter" pe tabel, pentru a deschide fereastra de modificare
        jTable1.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
        jTable1.getActionMap().put("Enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
            modificaIncarca();
            }
        });
        
        //cautare tabel
        rowSorter = new TableRowSorter<>(jTable1.getModel());
        jTable1.setRowSorter(rowSorter);
        jLabelCautare.setForeground(Color.red);
        jTextFieldCauta.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e) {
                String text = jTextFieldCauta.getText();

                if (text.trim().length() == 0) {
                    jLabelCautare.setText("");
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    jLabelCautare.setText("Filtru cautare activ");
                }
            }

            public void removeUpdate(DocumentEvent e) {
                String text = jTextFieldCauta.getText();

                if (text.trim().length() == 0) {
                    jLabelCautare.setText("");
                    rowSorter.setRowFilter(null);
                } else {
                    jLabelCautare.setText("Filtru cautare activ");
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        });
        
        salvare5min(m);
    }
    
    //urmeaza 4 metode doar pentru splashScreen - luate de pe net si adaptate un pic
    private static SplashScreen mySplash;
    private static Graphics2D splashGraphics;
    private static Graphics2D splashGraphics2;
    private static Rectangle2D splashTextArea;
    private static Rectangle2D splashNameArea;
    private static Rectangle2D splashProgressArea;
    
    private static void splashInit(){
        mySplash = SplashScreen.getSplashScreen();
            if (mySplash != null){   
                Dimension ssDim = mySplash.getSize();
                int height = ssDim.height;
                int width = ssDim.width;
                // stake out some area for our status information
                splashNameArea = new Rectangle2D.Double(width * .6, height* .08, width * .2, 18);
                splashTextArea = new Rectangle2D.Double(width * .3, height* .88, width * .4, 18);
                splashProgressArea = new Rectangle2D.Double(width * .2, height*.93, width*.6, 20 );

                // create the Graphics environment for drawing status info
                splashGraphics = mySplash.createGraphics();
                Font font = new Font("Dialog", Font.PLAIN, 14);
                splashGraphics.setFont(font);
                splashGraphics2 = mySplash.createGraphics();
                Font fontNume = new Font("Dialog", Font.BOLD, 14);
                splashGraphics2.setFont(fontNume);
            
                // initialize the status info
                splashText("Se incarca...");
                splashProgress(0);
        }
    }
        
    public static void splashText(String str){
        if (mySplash != null && mySplash.isVisible()){
            // important to check here so no other methods need to know if there
            // really is a Splash being displayed

            // erase the last status text
            splashGraphics.setPaint(Color.WHITE);
            splashGraphics.fill(splashTextArea);

            // draw the text
            splashGraphics.setPaint(Color.BLACK);
            splashGraphics.drawString(str, (int)(splashTextArea.getX() + 10),(int)(splashTextArea.getY() + 15));
            
            //adauga nume
            splashGraphics2.setPaint(Color.BLACK);
            splashGraphics2.drawString("Versiunea 1.1", (int)(splashNameArea.getX() + 45),(int)(splashNameArea.getY() + 12));
            splashGraphics2.setPaint(Color.RED);
            splashGraphics2.drawString("Cosmin Cojocea", (int)(splashNameArea.getX() + 27),(int)(splashNameArea.getY() + 32));
            
            // make sure it's displayed
            mySplash.update();
        }
    }
    
    public static void splashProgress(int pct){
        if (mySplash != null && mySplash.isVisible()){
            // Note: 3 colors are used here to demonstrate steps
            splashGraphics.setPaint(Color.WHITE);
            splashGraphics.fill(splashProgressArea);

            // draw an outline
            splashGraphics.setPaint(Color.BLUE);
            splashGraphics.draw(splashProgressArea);

            // Calculate the width corresponding to the correct percentage
            int x = (int) splashProgressArea.getMinX();
            int y = (int) splashProgressArea.getMinY();
            int wid = (int) splashProgressArea.getWidth();
            int hgt = (int) splashProgressArea.getHeight();

            int doneWidth = Math.round(pct*wid/100.f);
            doneWidth = Math.max(0, Math.min(doneWidth, wid-1));  // limit 0-width

            // fill the done part
            splashGraphics.setPaint(Color.DARK_GRAY);
            splashGraphics.fillRect(x, y+1, doneWidth, hgt);

            // make sure it's displayed
            mySplash.update();
        }
    }
        
    private static void appInit(){
        for(int i=0;i<=10;i++)
        {
            int pctDone = i * 10;
            splashText("Se incarca (mai incet)..." + i);
            splashProgress(pctDone);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                //ignore
            }
        }
    }    
    
    //salvare la 5 minute - se va face un autosave initial la 10 secunde pentru testare, salvare la 1 minut(5 cam multe pt testare)
    //daca agenda nu contine nici un abonat nu se va face salvarea
    //daca initial agenda a fost goala dar s-au adaugat abonati, 
    //apoi a fost golita din nou, datele vor fi salvate la iesire sau dupa adaugarea unui nou abonat
    //timer in timer - face ce vreau dar parca nu suna bine - de cautat daca se poate implementa altfel
    private void salvare5min(CarteDeTelefon agenda){
        //implementare java.util.Timer:
        Timer timerS = new Timer();
	timerS.schedule(new TimerTask() {
            @Override
            public void run() {
                int x = m.getRowCount();
                if(x != 0){ //verificare daca agenda este goala
                salvareInfoAuto(agenda);
                jLabelSalvare.setText("Datele au fost salvate");    
                Timer timerS2 = new Timer();
                timerS2.schedule(new TimerTask(){
                    @Override
                    public void run(){
                        jLabelSalvare.setText("");
                    }
                }, 5000);
                }
		}	
        }, 10000, 60000);
        
        //implementare javax.swing.Timer:
//        javax.swing.Timer timer = new javax.swing.Timer(60000, new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                
//                int x = m.getRowCount();
//                if(x != 0){ //verificare daca agenda este goala
//
//                salvareInfoAuto(agenda);
//                jLabelSalvare.setText("Datele au fost salvate");
//        
//                javax.swing.Timer timer2 = new javax.swing.Timer(1, new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        jLabelSalvare.setText("");
//                    }
//                    });
//                timer2.setRepeats(false); // Only execute once
//                //delay initial care in cazul de fata va lasa textul initial on screen pt 5 secunde pana il seteaza la ""
//                timer2.setInitialDelay(5000);
//                timer2.start();
//                
//                } //inchide if
//            }
//        });
//        
//        //prima salvare dupa 10 secunde de la pornire
//        timer.setInitialDelay(10000);
//        timer.setRepeats(true);
//        timer.start();
    }

    //metoda salvare date folosita la iesirea din program (salveaza path-ul de salvare al datelor in config file pt incarcare automata + salveaza datele)
    private void salvareInfoAuto(CarteDeTelefon agenda){
        //Salvare date configurare
        try {
            File fConfig = CONFIGPATH;
            if (fConfig.exists() && !fConfig.canWrite()) {
                throw new SaveException();
            }
            try (FileOutputStream fos = new FileOutputStream(fConfig); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(volatilePath);
            }
        } catch (SaveException se) {
            JOptionPane.showMessageDialog(
                    this, 
                        "Fisier configurare inaccesibil" +
                        System.lineSeparator() + 
                        se.getMessage(),
                    "ERROR!",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IOException iOException) {
            eroare(iOException);
        }
        
        //Salvare informatii agenda
        try {
            CarteDeTelefon agendaSave = agenda.clone();
            File fAgenda = volatilePath;
            if (fAgenda.exists() && !fAgenda.canWrite()) {
                throw new SaveException();
            }
            try (FileOutputStream fos = new FileOutputStream(fAgenda); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(agendaSave);
            }
        } catch (SaveException se) {
            JOptionPane.showMessageDialog(
                    this, 
                        "Nu exista permisiuni pentru a salva in acest fisier" +
                        System.lineSeparator() + 
                        se.getMessage(),
                    "ERROR!",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IOException iOException) {
            eroare(iOException);
        }
    }

    //metoda salvare manuala - daca se salveaza intr-un alt fisier decat cel predefinit
    private void salvare(CarteDeTelefon agenda){
        CarteDeTelefon agendaSave = agenda.clone();
        if (jFileChooser1.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            try{
                File fisierSelectat = jFileChooser1.getSelectedFile();
                String fileName = fisierSelectat.getPath();
                if(!fileName.endsWith(".agd")){
                    if(fileName.contains(".")){
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    }
                    fileName += ".agd";
                    //notificare utilizator de schimbare sau adaugare extensie
                    //eroare("Fisierului selectat i s-a adaugat extensia .agd!");
                    fisierSelectat = new File(fileName);
                }
                if (fisierSelectat.exists() && !fisierSelectat.canWrite()) {
                    throw new SaveException();
                }
                try (FileOutputStream fos = new FileOutputStream(fisierSelectat); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(agendaSave);
                }
                volatilePath = fisierSelectat;

            } catch (IOException e){
                eroare("Eroare la salvarea datelor");
            } catch (SaveException se){
                JOptionPane.showMessageDialog(
                    this, 
                        "Nu exista permisiuni pentru a salva in acest fisier" +
                        System.lineSeparator() + 
                        se.getMessage(),
                    "ERROR!",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    //metoda folosita la pornirea programului pentru a incarca date automat, foloseste CONFIG pentru a vedea unde au fost salvate datele ultima oara
    private CarteDeTelefon incarcareInfoAuto(){ 
        try {
            File fConfig = CONFIGPATH;
            if (fConfig.exists() && fConfig.isFile() && fConfig.canRead()){
                ObjectInputStream ois;
                try (FileInputStream fis = new FileInputStream(fConfig)) {
                    ois = new ObjectInputStream(fis);
                    volatilePath = (File) ois.readObject();
                }
                ois.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            eroare(e);
        }
        CarteDeTelefon agenda = new CarteDeTelefon();
        try {
            File fagenda = volatilePath;
            if (fagenda.exists() && fagenda.isFile() && fagenda.canRead()){
                ObjectInputStream ois;
                try (FileInputStream fis = new FileInputStream(fagenda)) {
                    ois = new ObjectInputStream(fis);
                    agenda = (CarteDeTelefon) ois.readObject();
                }
                ois.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            eroare(e);
        }
        return agenda;
    }
    
    private void eroare(String err) {
        JOptionPane.showMessageDialog(
                this,
                err,
                "Eroare",
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void eroare(Exception e){
        JOptionPane.showMessageDialog(
                this,
                    "Vai ce surpriza: " +
                    System.lineSeparator() + 
                    e.getMessage(),
                "ERROR!",
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void info(String info) {
        JOptionPane.showMessageDialog(
                this,
                info,
                "Oops!",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void inchideAdauga(){
        jTextAdaugaNume.setText("");
        jTextAdaugaPrenume.setText("");
        jTextAdaugaCNP.setText("");
        jTextAdaugaTelefon.setText("");
        jDialogAdauga.dispose();
    }
    
    private void registerApp(){
        jMenuLoad.setEnabled(true);
        jMenuSave.setEnabled(true);
        jLabelShareware.setEnabled(false);
        jLabelShareware.setVisible(false);
        jMenuInregistrare.setVisible(false);
        jSeparator2.setVisible(false); 
    }
    
    private void modificaIncarca(){
        if(jTable1.getSelectedRow() != -1){
            cnpLoad = jTable1.getValueAt(jTable1.getSelectedRow(), 2).toString();
            jTextModificaNume.setText(jTable1.getValueAt(jTable1.getSelectedRow(), 0).toString());
            jTextModificaPrenume.setText(jTable1.getValueAt(jTable1.getSelectedRow(), 1).toString());
            jTextModificaCNP.setText(cnpLoad);
            jTextModificaTelefon.setText(jTable1.getValueAt(jTable1.getSelectedRow(), 3).toString());
            //verificare daca tipul telefonului este Mobil sau Fix
            String tipTel = m.cautaCNP(cnpLoad).getTipTel();
            if(tipTel.equals("mobil")){
                jComboModif.setSelectedIndex(0);
            } else if (tipTel.equals("fix")){
                jComboModif.setSelectedIndex(1);
            }
            jDialogModifica.setLocationRelativeTo(null);
            jDialogModifica.setVisible(true);
        } else {
            info("Selectati un abonat din tabel");
        }
    }
    
    //metoda ce se ocupa de banner-ul shareware, erori ce pot aparea din lipsa pozelor, rotatie poze
    private void incarcaPoze(){
        File folderPoze = new File(POZEPATH);
        if(!folderPoze.exists() || !folderPoze.isDirectory() || !folderPoze.canRead() || !folderPoze.canExecute()){
            jLabelShareware.setText("Nu gasesc imaginile... erau mai frumoase decat textul asta rosu!");
            jLabelShareware.setForeground(Color.red);
        } else {
            FileFilter filtruPNG = new FileFilter(){
                @Override
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".png");
                }  
            };
            File[] fisiere = folderPoze.listFiles(filtruPNG);
            if (fisiere.length == 0){
                jLabelShareware.setText("Nu gasesc imaginile... erau mai frumoase decat textul asta roz!");
                jLabelShareware.setForeground(Color.pink);
            } else {
                poze = Arrays.asList(fisiere);
                pozaCurenta = 0;

	javax.swing.Timer timerP = new javax.swing.Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
		afiseazaPoza();
		}
            });
            timerP.setRepeats(true);
            timerP.setInitialDelay(0);
            timerP.start();
            }
        }
    }
    
    private void afiseazaPoza(){
        File f = poze.get(pozaCurenta);
        ImageIcon i = new ImageIcon(f.getAbsolutePath());
        jLabelShareware.setIcon(i);
        jLabelShareware.setText("");
        pozaCurenta = (pozaCurenta + 1) % poze.size();
    }

    private void getSortare(){
        boolean selectedNume = jRadioButtonNume.isSelected();
        boolean selectedPrenume = jRadioButtonPrenume.isSelected();
        boolean selectedCNP = jRadioButtonCNP.isSelected();
        boolean selectedNrTel = jRadioButtonTelefon.isSelected();
        boolean selectedAsc = jRadioButtonAsc.isSelected();
        boolean selectedDesc = jRadioButtonDesc.isSelected();
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        SortOrder directie = SortOrder.ASCENDING;
        int column = 0;
        if(selectedAsc){
            directie = SortOrder.ASCENDING;
            if(selectedNume){
                column = 0;
                //functiona si metoda precedenta dar nu se folosea de metodele de sortare ale tabelului, 
                //si nu ar fi aratat userului care coloana este sortata
//                jTable1.getRowSorter().setSortKeys(null);
//                m.sortare(1, 'a');
            }
            if(selectedPrenume){
                column = 1;
//                jTable1.getRowSorter().setSortKeys(null);
//                m.sortare(2, 'a');
            }
            if(selectedCNP){
                column = 2;
//                jTable1.getRowSorter().setSortKeys(null);
//                m.sortare(3, 'a');
            }
            if(selectedNrTel){
                column = 3;
//                jTable1.getRowSorter().setSortKeys(null);
//                m.sortare(4, 'a');
            }
        }
        if(selectedDesc){
            directie = SortOrder.DESCENDING;
            if(selectedNume){
                column = 0;
//                jTable1.getRowSorter().setSortKeys(null);
//                m.sortare(1, 'd');
            }
            if(selectedPrenume){
                column = 1;
//                jTable1.getRowSorter().setSortKeys(null);
//                m.sortare(2, 'd');
            }
            if(selectedCNP){
                column = 2;
//                jTable1.getRowSorter().setSortKeys(null);
//                m.sortare(3, 'd');
            }
            if(selectedNrTel){
                column = 3;
//                jTable1.getRowSorter().setSortKeys(null);
//                m.sortare(4, 'd');
            }
        }
        sortKeys.add(new RowSorter.SortKey(column, directie));
        rowSorter.setSortKeys(sortKeys);
        rowSorter.sort();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialogSortare = new javax.swing.JDialog();
        jRadioButtonNume = new javax.swing.JRadioButton();
        jRadioButtonPrenume = new javax.swing.JRadioButton();
        jRadioButtonCNP = new javax.swing.JRadioButton();
        jRadioButtonTelefon = new javax.swing.JRadioButton();
        jRadioButtonAsc = new javax.swing.JRadioButton();
        jRadioButtonDesc = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButtonSort = new javax.swing.JButton();
        jButtonCancelSort = new javax.swing.JButton();
        jDialogAdauga = new javax.swing.JDialog();
        jButtonAdaugaCancel = new javax.swing.JButton();
        jButtonAdaugaAdauga = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextAdaugaNume = new javax.swing.JTextField();
        jTextAdaugaPrenume = new javax.swing.JTextField();
        jTextAdaugaCNP = new javax.swing.JTextField();
        jTextAdaugaTelefon = new javax.swing.JTextField();
        jComboAdaugaTel = new javax.swing.JComboBox<>();
        jDialogCauta = new javax.swing.JDialog();
        jLabel7 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButtonCancel = new javax.swing.JButton();
        jButtonButton = new javax.swing.JButton();
        jComboBoxCauta = new javax.swing.JComboBox<>();
        jDialogModifica = new javax.swing.JDialog();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jTextModificaNume = new javax.swing.JTextField();
        jTextModificaPrenume = new javax.swing.JTextField();
        jTextModificaCNP = new javax.swing.JTextField();
        jTextModificaTelefon = new javax.swing.JTextField();
        jButtonModifCancel = new javax.swing.JButton();
        jButtonModifModifica = new javax.swing.JButton();
        jComboModif = new javax.swing.JComboBox<>();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jDialogIesire = new javax.swing.JDialog();
        jLabel16 = new javax.swing.JLabel();
        jButtonNuIesire = new javax.swing.JButton();
        jButtonDaIesire = new javax.swing.JButton();
        jDialogInregistrare = new javax.swing.JDialog();
        jButtonCancelInreg = new javax.swing.JButton();
        jButtonOKInreg = new javax.swing.JButton();
        jTextFieldInregistrare = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jDialogAbout = new javax.swing.JDialog();
        jButtonAbout = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jDialogSterge = new javax.swing.JDialog();
        jLabel19 = new javax.swing.JLabel();
        jButtonStergeNu = new javax.swing.JButton();
        jButtonStergeDa = new javax.swing.JButton();
        jFileChooser1 = new javax.swing.JFileChooser();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItemSterge = new javax.swing.JMenuItem();
        jMenuItemModifica = new javax.swing.JMenuItem();
        jButtonAdauga = new javax.swing.JButton();
        jButtonModifica = new javax.swing.JButton();
        jButtonSterge = new javax.swing.JButton();
        jButtonSortare = new javax.swing.JButton();
        jButtonIesire = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabelShareware = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldCauta = new javax.swing.JTextField();
        jLabelSalvare = new javax.swing.JLabel();
        jToggleButtonDatabase = new javax.swing.JToggleButton();
        jLabelCautare = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuLoad = new javax.swing.JMenuItem();
        jMenuSave = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuIesire = new javax.swing.JMenuItem();
        jMenuAbonati = new javax.swing.JMenu();
        jMenuAdauga = new javax.swing.JMenuItem();
        jMenuSterge = new javax.swing.JMenuItem();
        jMenuModifica = new javax.swing.JMenuItem();
        jMenuSortare = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuInregistrare = new javax.swing.JMenuItem();
        jMenuDebug = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuAbout = new javax.swing.JMenuItem();

        jDialogSortare.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialogSortare.setTitle("Sortare");
        jDialogSortare.setLocationByPlatform(true);
        jDialogSortare.setMinimumSize(new java.awt.Dimension(230, 275));
        jDialogSortare.setModal(true);
        jDialogSortare.setResizable(false);

        buttonGroup1.add(jRadioButtonNume);
        jRadioButtonNume.setSelected(true);
        jRadioButtonNume.setText("Nume");

        buttonGroup1.add(jRadioButtonPrenume);
        jRadioButtonPrenume.setText("Prenume");

        buttonGroup1.add(jRadioButtonCNP);
        jRadioButtonCNP.setText("CNP");

        buttonGroup1.add(jRadioButtonTelefon);
        jRadioButtonTelefon.setText("Telefon");

        buttonGroup2.add(jRadioButtonAsc);
        jRadioButtonAsc.setSelected(true);
        jRadioButtonAsc.setText("Ascendent");

        buttonGroup2.add(jRadioButtonDesc);
        jRadioButtonDesc.setText("Descendent");

        jLabel5.setText("Sorteaza dupa:");

        jLabel6.setText("Mod sortare:");

        jButtonSort.setText("Sorteaza");
        jButtonSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSortActionPerformed(evt);
            }
        });

        jButtonCancelSort.setText("Inapoi");
        jButtonCancelSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelSortActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jDialogSortareLayout = new javax.swing.GroupLayout(jDialogSortare.getContentPane());
        jDialogSortare.getContentPane().setLayout(jDialogSortareLayout);
        jDialogSortareLayout.setHorizontalGroup(
            jDialogSortareLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogSortareLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogSortareLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonNume, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonTelefon, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonCNP, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonPrenume, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonAsc, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addGroup(jDialogSortareLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonSort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonCancelSort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jDialogSortareLayout.setVerticalGroup(
            jDialogSortareLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogSortareLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogSortareLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDialogSortareLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButtonNume)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButtonPrenume)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButtonCNP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButtonTelefon)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButtonAsc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButtonDesc))
                    .addGroup(jDialogSortareLayout.createSequentialGroup()
                        .addComponent(jButtonSort)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonCancelSort)))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jDialogAdauga.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialogAdauga.setTitle("Adauga");
        jDialogAdauga.setMinimumSize(new java.awt.Dimension(330, 250));
        jDialogAdauga.setModal(true);
        jDialogAdauga.setResizable(false);
        jDialogAdauga.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                jDialogAdaugaWindowClosing(evt);
            }
        });

        jButtonAdaugaCancel.setText("Inapoi");
        jButtonAdaugaCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAdaugaCancelActionPerformed(evt);
            }
        });

        jButtonAdaugaAdauga.setText("Adauga");
        jButtonAdaugaAdauga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAdaugaAdaugaActionPerformed(evt);
            }
        });

        jLabel1.setText("Nume");

        jLabel2.setText("Prenume");

        jLabel3.setText("CNP");

        jLabel4.setText("Telefon");

        jComboAdaugaTel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Mobil", "Fix" }));

        javax.swing.GroupLayout jDialogAdaugaLayout = new javax.swing.GroupLayout(jDialogAdauga.getContentPane());
        jDialogAdauga.getContentPane().setLayout(jDialogAdaugaLayout);
        jDialogAdaugaLayout.setHorizontalGroup(
            jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogAdaugaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jDialogAdaugaLayout.createSequentialGroup()
                        .addGroup(jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextAdaugaNume, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .addComponent(jTextAdaugaPrenume)
                            .addComponent(jTextAdaugaCNP)
                            .addGroup(jDialogAdaugaLayout.createSequentialGroup()
                                .addComponent(jTextAdaugaTelefon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboAdaugaTel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(jDialogAdaugaLayout.createSequentialGroup()
                        .addComponent(jButtonAdaugaCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 158, Short.MAX_VALUE)
                        .addComponent(jButtonAdaugaAdauga)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jDialogAdaugaLayout.setVerticalGroup(
            jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogAdaugaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextAdaugaNume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextAdaugaPrenume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextAdaugaCNP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextAdaugaTelefon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboAdaugaTel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jDialogAdaugaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAdaugaCancel)
                    .addComponent(jButtonAdaugaAdauga))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jDialogCauta.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialogCauta.setTitle("Cauta");
        jDialogCauta.setMinimumSize(new java.awt.Dimension(305, 150));
        jDialogCauta.setModal(true);
        jDialogCauta.setResizable(false);

        jLabel7.setText("Cauta abonat dupa:");

        jButtonCancel.setText("Inapoi");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonButton.setText("Cauta");

        jComboBoxCauta.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nume", "Prenume", "CNP", "Telefon" }));

        javax.swing.GroupLayout jDialogCautaLayout = new javax.swing.GroupLayout(jDialogCauta.getContentPane());
        jDialogCauta.getContentPane().setLayout(jDialogCautaLayout);
        jDialogCautaLayout.setHorizontalGroup(
            jDialogCautaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogCautaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogCautaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextField1)
                    .addGroup(jDialogCautaLayout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jComboBoxCauta, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jDialogCautaLayout.createSequentialGroup()
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonButton)))
                .addContainerGap())
        );
        jDialogCautaLayout.setVerticalGroup(
            jDialogCautaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogCautaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogCautaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jComboBoxCauta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogCautaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jDialogModifica.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialogModifica.setTitle("Modifica");
        jDialogModifica.setMinimumSize(new java.awt.Dimension(270, 240));
        jDialogModifica.setModal(true);
        jDialogModifica.setResizable(false);

        jLabel12.setText("Nume");

        jLabel13.setText("Prenume");

        jLabel14.setText("CNP");

        jLabel15.setText("Telefon");

        jButtonModifCancel.setText("Inapoi");
        jButtonModifCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonModifCancelActionPerformed(evt);
            }
        });

        jButtonModifModifica.setText("Modifica");
        jButtonModifModifica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonModifModificaActionPerformed(evt);
            }
        });

        jComboModif.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Mobil", "Fix" }));

        javax.swing.GroupLayout jDialogModificaLayout = new javax.swing.GroupLayout(jDialogModifica.getContentPane());
        jDialogModifica.getContentPane().setLayout(jDialogModificaLayout);
        jDialogModificaLayout.setHorizontalGroup(
            jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogModificaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jDialogModificaLayout.createSequentialGroup()
                        .addGroup(jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogModificaLayout.createSequentialGroup()
                                .addComponent(jTextModificaTelefon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboModif, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTextModificaNume)
                            .addComponent(jTextModificaPrenume)
                            .addComponent(jTextModificaCNP)))
                    .addGroup(jDialogModificaLayout.createSequentialGroup()
                        .addComponent(jButtonModifCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                        .addComponent(jButtonModifModifica)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jDialogModificaLayout.setVerticalGroup(
            jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogModificaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextModificaNume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jTextModificaPrenume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jTextModificaCNP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jTextModificaTelefon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboModif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jDialogModificaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonModifCancel)
                    .addComponent(jButtonModifModifica))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jDialogIesire.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialogIesire.setTitle("Confirmare");
        jDialogIesire.setMinimumSize(new java.awt.Dimension(290, 130));
        jDialogIesire.setModal(true);
        jDialogIesire.setResizable(false);

        jLabel16.setText("Sunteti sigur ca doriti inchiderea programului?");

        jButtonNuIesire.setMnemonic('n');
        jButtonNuIesire.setText("NU");
        jButtonNuIesire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNuIesireActionPerformed(evt);
            }
        });

        jButtonDaIesire.setMnemonic('d');
        jButtonDaIesire.setText("DA");
        jButtonDaIesire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDaIesireActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jDialogIesireLayout = new javax.swing.GroupLayout(jDialogIesire.getContentPane());
        jDialogIesire.getContentPane().setLayout(jDialogIesireLayout);
        jDialogIesireLayout.setHorizontalGroup(
            jDialogIesireLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogIesireLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogIesireLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDialogIesireLayout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jDialogIesireLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jButtonNuIesire, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonDaIesire, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(23, 23, 23))))
        );
        jDialogIesireLayout.setVerticalGroup(
            jDialogIesireLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogIesireLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jDialogIesireLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonNuIesire)
                    .addComponent(jButtonDaIesire))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jDialogInregistrare.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialogInregistrare.setMinimumSize(new java.awt.Dimension(260, 170));
        jDialogInregistrare.setModal(true);
        jDialogInregistrare.setResizable(false);

        jButtonCancelInreg.setText("Inapoi");
        jButtonCancelInreg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelInregActionPerformed(evt);
            }
        });

        jButtonOKInreg.setText("OK");
        jButtonOKInreg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKInregActionPerformed(evt);
            }
        });

        jLabel18.setText("Introduceti codul de inregistrare al aplicatiei:");

        javax.swing.GroupLayout jDialogInregistrareLayout = new javax.swing.GroupLayout(jDialogInregistrare.getContentPane());
        jDialogInregistrare.getContentPane().setLayout(jDialogInregistrareLayout);
        jDialogInregistrareLayout.setHorizontalGroup(
            jDialogInregistrareLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogInregistrareLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogInregistrareLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(jTextFieldInregistrare)
                    .addGroup(jDialogInregistrareLayout.createSequentialGroup()
                        .addComponent(jButtonCancelInreg, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonOKInreg, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jDialogInregistrareLayout.setVerticalGroup(
            jDialogInregistrareLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogInregistrareLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldInregistrare, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jDialogInregistrareLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancelInreg)
                    .addComponent(jButtonOKInreg))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jDialogAbout.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialogAbout.setTitle("About");
        jDialogAbout.setMinimumSize(new java.awt.Dimension(185, 170));
        jDialogAbout.setModal(true);
        jDialogAbout.setResizable(false);

        jButtonAbout.setText("OK");
        jButtonAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAboutActionPerformed(evt);
            }
        });

        jLabel9.setText("Agenda telefonica");

        jLabel10.setText("Versiunea 1.0");

        jLabel11.setText("Autor: Cojocea Cosmin");

        javax.swing.GroupLayout jDialogAboutLayout = new javax.swing.GroupLayout(jDialogAbout.getContentPane());
        jDialogAbout.getContentPane().setLayout(jDialogAboutLayout);
        jDialogAboutLayout.setHorizontalGroup(
            jDialogAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogAboutLayout.createSequentialGroup()
                .addGroup(jDialogAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jDialogAboutLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jDialogAboutLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jDialogAboutLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jDialogAboutLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jButtonAbout, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jDialogAboutLayout.setVerticalGroup(
            jDialogAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogAboutLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addComponent(jButtonAbout, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                .addContainerGap())
        );

        jDialogSterge.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialogSterge.setTitle("Confirmare");
        jDialogSterge.setMinimumSize(new java.awt.Dimension(280, 115));
        jDialogSterge.setModal(true);
        jDialogSterge.setResizable(false);

        jLabel19.setText("Sunteti sigur ca doriti sa stergeti acest abonat?");

        jButtonStergeNu.setText("Nu");
        jButtonStergeNu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStergeNuActionPerformed(evt);
            }
        });

        jButtonStergeDa.setText("Da");
        jButtonStergeDa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStergeDaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jDialogStergeLayout = new javax.swing.GroupLayout(jDialogSterge.getContentPane());
        jDialogSterge.getContentPane().setLayout(jDialogStergeLayout);
        jDialogStergeLayout.setHorizontalGroup(
            jDialogStergeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogStergeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jDialogStergeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel19)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialogStergeLayout.createSequentialGroup()
                        .addComponent(jButtonStergeNu, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonStergeDa, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jDialogStergeLayout.setVerticalGroup(
            jDialogStergeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogStergeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jDialogStergeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonStergeNu)
                    .addComponent(jButtonStergeDa))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jFileChooser1.setApproveButtonToolTipText("");
        jFileChooser1.setCurrentDirectory(new java.io.File("C:\\Users\\ccojo\\Documents\\NetBeansProjects\\ProiectFinal\\src\\Files"));

        jMenuItemSterge.setText("jMenuItem1");
        jMenuItemSterge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemStergeActionPerformed(evt);
            }
        });

        jMenuItemModifica.setText("jMenuItem2");
        jMenuItemModifica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemModificaActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Agenda telefonica");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(800, 640));
        setPreferredSize(new java.awt.Dimension(800, 600));
        setResizable(false);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jButtonAdauga.setText("Adauga");
        jButtonAdauga.setToolTipText("Adauga abonat");
        jButtonAdauga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAdaugaActionPerformed(evt);
            }
        });

        jButtonModifica.setText("Modifica");
        jButtonModifica.setToolTipText("Modifica abonat selectat");
        jButtonModifica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonModificaActionPerformed(evt);
            }
        });

        jButtonSterge.setText("Sterge");
        jButtonSterge.setToolTipText("Sterge abonat selectat");
        jButtonSterge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStergeActionPerformed(evt);
            }
        });

        jButtonSortare.setText("Sortare");
        jButtonSortare.setToolTipText("Deschide meniul de sortare al abonatilor");
        jButtonSortare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSortareActionPerformed(evt);
            }
        });

        jButtonIesire.setText("Iesire");
        jButtonIesire.setToolTipText("Bye :(");
        jButtonIesire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIesireActionPerformed(evt);
            }
        });

        jTable1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nume", "Prenume", "CNP", "Telefon"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTable1MousePressed(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable1KeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
            jTable1.getColumnModel().getColumn(3).setResizable(false);
        }

        jLabelShareware.setBackground(new java.awt.Color(255, 0, 102));
        jLabelShareware.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jLabelShareware.setForeground(new java.awt.Color(255, 0, 51));
        jLabelShareware.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelShareware.setText("SHAREWARE PICTURE");

        jLabel8.setText("Cauta abonat:");

        jTextFieldCauta.setToolTipText("Cauta abonat dupa orice atribut");

        jLabelSalvare.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabelSalvare.setForeground(new java.awt.Color(0, 51, 255));

        jToggleButtonDatabase.setText("Database");

        jLabelCautare.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelCautare.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jMenuFile.setMnemonic('f');
        jMenuFile.setText("File");
        jMenuFile.setContentAreaFilled(false);
        jMenuFile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jMenuLoad.setMnemonic('l');
        jMenuLoad.setText("Load");
        jMenuLoad.setEnabled(false);
        jMenuLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuLoadActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuLoad);

        jMenuSave.setMnemonic('s');
        jMenuSave.setText("Save");
        jMenuSave.setEnabled(false);
        jMenuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSaveActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuSave);
        jMenuFile.add(jSeparator1);

        jMenuIesire.setMnemonic('i');
        jMenuIesire.setText("Iesire");
        jMenuIesire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuIesireActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuIesire);

        jMenuBar1.add(jMenuFile);

        jMenuAbonati.setMnemonic('a');
        jMenuAbonati.setText("Abonati");

        jMenuAdauga.setMnemonic('d');
        jMenuAdauga.setText("Adauga...");
        jMenuAdauga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAdaugaActionPerformed(evt);
            }
        });
        jMenuAbonati.add(jMenuAdauga);

        jMenuSterge.setMnemonic('e');
        jMenuSterge.setText("Sterge...");
        jMenuSterge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuStergeActionPerformed(evt);
            }
        });
        jMenuAbonati.add(jMenuSterge);

        jMenuModifica.setMnemonic('m');
        jMenuModifica.setText("Modifica...");
        jMenuModifica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuModificaActionPerformed(evt);
            }
        });
        jMenuAbonati.add(jMenuModifica);

        jMenuSortare.setText("Sortare...");
        jMenuSortare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSortareActionPerformed(evt);
            }
        });
        jMenuAbonati.add(jMenuSortare);

        jMenuBar1.add(jMenuAbonati);

        jMenuHelp.setMnemonic('h');
        jMenuHelp.setText("Help");

        jMenuInregistrare.setMnemonic('r');
        jMenuInregistrare.setText("Inregistrare");
        jMenuInregistrare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuInregistrareActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuInregistrare);

        jMenuDebug.setText("Debug - Lista abonati");
        jMenuDebug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuDebugActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuDebug);
        jMenuHelp.add(jSeparator2);

        jMenuAbout.setMnemonic('b');
        jMenuAbout.setText("About");
        jMenuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuAbout);

        jMenuBar1.add(jMenuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelShareware, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonModifica, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(jButtonSterge, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonSortare, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonAdauga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonIesire, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jToggleButtonDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabelSalvare, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextFieldCauta, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabelCautare, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonAdauga)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonSterge)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonModifica)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonSortare)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 277, Short.MAX_VALUE)
                        .addComponent(jToggleButtonDatabase))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelCautare, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldCauta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonIesire)
                    .addComponent(jLabelSalvare))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelShareware, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonIesireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIesireActionPerformed
        jDialogIesire.setLocationRelativeTo(null);
        jDialogIesire.setVisible(true);
    }//GEN-LAST:event_jButtonIesireActionPerformed

    private void jButtonAdaugaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAdaugaActionPerformed
        jDialogAdauga.setLocationRelativeTo(null);
        jDialogAdauga.setVisible(true);
    }//GEN-LAST:event_jButtonAdaugaActionPerformed
   
    private void jMenuLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuLoadActionPerformed
        jFileChooser1.setAcceptAllFileFilterUsed(false);
        jFileChooser1.setFileFilter(new FileNameExtensionFilter("*.agd", "agd"));
        jFileChooser1.setFileSelectionMode(JFileChooser.FILES_ONLY);
        try {
            if (jFileChooser1.showOpenDialog(this) == jFileChooser1.APPROVE_OPTION) {
                File fisierSelectat = jFileChooser1.getSelectedFile();
                String testExt = fisierSelectat.toString();
                if (!testExt.endsWith(".agd")){
                    throw new IOException("Fisierul selectat nu are extensia '.agd'");
                } else {
                    if (fisierSelectat.exists() && fisierSelectat.isFile() && fisierSelectat.canRead()) {
                    ObjectInputStream ois;
                    try (FileInputStream fis = new FileInputStream(fisierSelectat)) {
                        ois = new ObjectInputStream(fis);
                        m = (CarteDeTelefon) ois.readObject();
                        jTable1.setModel(m);
                        rowSorter = new TableRowSorter<>(jTable1.getModel());
                        jTable1.setRowSorter(rowSorter);
                        volatilePath = fisierSelectat;
                    }
                    ois.close();
                    }
                }
            }
        } catch (IOException e){
            eroare("Incarcare fisier nereusita: " + e);
        } catch (HeadlessException | ClassNotFoundException e){
            eroare(e);
        } 
                   
    }//GEN-LAST:event_jMenuLoadActionPerformed

    private void jButtonSortareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSortareActionPerformed
        jDialogSortare.setLocationRelativeTo(null);
        jDialogSortare.setVisible(true);
    }//GEN-LAST:event_jButtonSortareActionPerformed

    private void jMenuIesireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuIesireActionPerformed
        jDialogIesire.setLocationRelativeTo(null);
        jDialogIesire.setVisible(true);
    }//GEN-LAST:event_jMenuIesireActionPerformed

    private void jButtonModificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonModificaActionPerformed
        modificaIncarca();
    }//GEN-LAST:event_jButtonModificaActionPerformed

    private void jButtonCancelSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelSortActionPerformed
        jDialogSortare.dispose();
    }//GEN-LAST:event_jButtonCancelSortActionPerformed

    private void jButtonAdaugaCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAdaugaCancelActionPerformed
        inchideAdauga();
    }//GEN-LAST:event_jButtonAdaugaCancelActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        jDialogCauta.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonModifCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonModifCancelActionPerformed
        jDialogModifica.dispose();
    }//GEN-LAST:event_jButtonModifCancelActionPerformed

    private void jButtonNuIesireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNuIesireActionPerformed
        jDialogIesire.dispose();
    }//GEN-LAST:event_jButtonNuIesireActionPerformed

    private void jButtonDaIesireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDaIesireActionPerformed
        salvareInfoAuto(m);
        System.exit(0);
    }//GEN-LAST:event_jButtonDaIesireActionPerformed

    private void jButtonAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAboutActionPerformed
        jDialogAbout.dispose();
    }//GEN-LAST:event_jButtonAboutActionPerformed

    private void jMenuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAboutActionPerformed
        jDialogAbout.setLocationRelativeTo(null);
        jDialogAbout.setVisible(true);
    }//GEN-LAST:event_jMenuAboutActionPerformed

    private void jButtonOKInregActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKInregActionPerformed
        if(jTextFieldInregistrare.getText().equals("test1")){
            JOptionPane.showMessageDialog(
                    this,
                    "Aplicatia a fost inregistrata",
                    "Succes",
                    JOptionPane.INFORMATION_MESSAGE
            );
            jDialogInregistrare.dispose();
            registerApp();
            try {
                Shareware registered = new Shareware(1);
                File fregistered = new File(REGPATH);
                if (fregistered.exists() && !fregistered.canWrite()){
                    throw new RegisterException();
                }
                FileOutputStream fos = new FileOutputStream(fregistered);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(registered);
                oos.close();
                fos.close();
            } catch (IOException iOException) {
                JOptionPane.showMessageDialog(
                        this,
                        "Procesul de salvare a inregistrarii a intampinat o problema!" + 
                        System.lineSeparator() +
                        iOException.getMessage(),
                        "OOOOPS!?",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (RegisterException re) {
                JOptionPane.showMessageDialog(
                        this, 
                        "Fisierul de inregistrare nu poate fi modificat" +
                        System.lineSeparator() +
                        "Permisiuni de scriere inexistente",
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else if (jTextFieldInregistrare.getText().equals("debug")){
            jDialogInregistrare.dispose();
            registerApp();
            jMenuDebug.setEnabled(true);
            jMenuDebug.setVisible(true);
            try {
                Shareware registered = new Shareware(2);
                File fregistered = new File(REGPATH);
                if (fregistered.exists() && !fregistered.canWrite()){
                    throw new RegisterException();
                }
                FileOutputStream fos = new FileOutputStream(fregistered);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(registered);
                oos.close();
                fos.close();
            } catch (IOException iOException) {
                JOptionPane.showMessageDialog(
                        this,
                        "Procesul de salvare a inregistrarii a intampinat o problema!" + 
                        System.lineSeparator() +
                        iOException.getMessage(),
                        "OOOOPS!?",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (RegisterException re) {
                JOptionPane.showMessageDialog(
                        this, 
                        "Fisierul de inregistrare nu poate fi modificat" +
                        System.lineSeparator() +
                        "Permisiuni de scriere inexistente",
                        "ERROR",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Cod incorect",
                    "Fail",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_jButtonOKInregActionPerformed

    private void jMenuInregistrareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuInregistrareActionPerformed
        jDialogInregistrare.setLocationRelativeTo(null);
        jDialogInregistrare.setVisible(true);
    }//GEN-LAST:event_jMenuInregistrareActionPerformed

    private void jButtonCancelInregActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelInregActionPerformed
        jDialogInregistrare.dispose();
    }//GEN-LAST:event_jButtonCancelInregActionPerformed

    private void jMenuAdaugaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAdaugaActionPerformed
        jDialogAdauga.setLocationRelativeTo(null);
        jDialogAdauga.setVisible(true);
    }//GEN-LAST:event_jMenuAdaugaActionPerformed

    private void jMenuModificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuModificaActionPerformed
        modificaIncarca();
    }//GEN-LAST:event_jMenuModificaActionPerformed

    private void jButtonStergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStergeActionPerformed
        if(jTable1.getSelectedRow() != -1){
            jDialogSterge.setLocationRelativeTo(null);
            jDialogSterge.setVisible(true);
        } else {
            info("Selectati un abonat din tabel");
        }
    }//GEN-LAST:event_jButtonStergeActionPerformed

    private void jMenuStergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuStergeActionPerformed
        if(jTable1.getSelectedRow() != -1){        
            jDialogSterge.setLocationRelativeTo(null);
            jDialogSterge.setVisible(true);
        } else {
            info("Selectati un abonat din tabel");
        }
    }//GEN-LAST:event_jMenuStergeActionPerformed

    private void jButtonStergeNuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStergeNuActionPerformed
        jDialogSterge.dispose();
    }//GEN-LAST:event_jButtonStergeNuActionPerformed

    private void jButtonAdaugaAdaugaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAdaugaAdaugaActionPerformed
        try {
            String nume = jTextAdaugaNume.getText();
            String prenume = jTextAdaugaPrenume.getText();
            String cnp = jTextAdaugaCNP.getText();
            String nr = jTextAdaugaTelefon.getText();
            if (jComboAdaugaTel.getSelectedIndex() == 0) {
                aplicatie.NrMobil nrTel = new aplicatie.NrMobil(nr);
                m.adauga(nume, prenume, cnp, nrTel);
            } else {
                aplicatie.NrFix nrTel = new aplicatie.NrFix(nr);
                m.adauga(nume, prenume, cnp, nrTel);
            }
            //chemarea functiei de sortare nu mai este necesara daca folosesc sortarea direct pe tabel
            //getSortare();
            inchideAdauga();
        } catch (IllegalArgumentException e) {
            eroare(e.getMessage());
        }
    }//GEN-LAST:event_jButtonAdaugaAdaugaActionPerformed

    private void jButtonStergeDaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStergeDaActionPerformed
        String cnp = jTable1.getValueAt(jTable1.getSelectedRow(), 2).toString();
        m.sterge(cnp);
        jDialogSterge.dispose();
    }//GEN-LAST:event_jButtonStergeDaActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        salvareInfoAuto(m);
    }//GEN-LAST:event_formWindowClosing

    private void jMenuSortareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSortareActionPerformed
        jDialogSortare.setLocationRelativeTo(null);
        jDialogSortare.setVisible(true);
    }//GEN-LAST:event_jMenuSortareActionPerformed

    private void jButtonSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSortActionPerformed
        getSortare();
        jDialogSortare.dispose();
        //sortarea ramane la fel dupa ce a fost selectata (si daca se adauga inregistrari noi vor fi sortate asa cum a ales userul)
//        jRadioButtonAsc.setSelected(true);
//        jRadioButtonNume.setSelected(true);
    }//GEN-LAST:event_jButtonSortActionPerformed

    private void jDialogAdaugaWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jDialogAdaugaWindowClosing
        inchideAdauga();
    }//GEN-LAST:event_jDialogAdaugaWindowClosing

    private void jButtonModifModificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonModifModificaActionPerformed
        String nume = jTextModificaNume.getText();
        String prenume = jTextModificaPrenume.getText();
        String cnp = jTextModificaCNP.getText();
        String tel = jTextModificaTelefon.getText();
        try {
            //verificam ca noul CNP modificat nu este asignat unui alt abonat din baza de date pentru a nu permite duplicate
            CarteDeTelefon newCarte = m.clone();
            newCarte.sterge(cnpLoad);
            if(newCarte.valideazaCNPunic(cnp)){
                throw new IllegalArgumentException("CNP-ul exista in baza de date");
            } else {
                if (jComboModif.getSelectedIndex() == 0) {
                    aplicatie.NrMobil nrTel = new aplicatie.NrMobil(tel);
                    m.modifica(cnpLoad, nume, prenume, cnp, nrTel);
                } else {
                    aplicatie.NrFix nrTel = new aplicatie.NrFix(tel);
                    m.modifica(cnpLoad, nume, prenume, cnp, nrTel);
                }
                jDialogModifica.dispose();
            }

        } catch (IllegalArgumentException e) {
            eroare(e.getMessage());
        }
    }//GEN-LAST:event_jButtonModifModificaActionPerformed

    private void jMenuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveActionPerformed
        jFileChooser1.setAcceptAllFileFilterUsed(false);
        jFileChooser1.setFileFilter(new FileNameExtensionFilter("*.agd", "agd"));
        jFileChooser1.setFileSelectionMode(JFileChooser.FILES_ONLY);
        salvare(m);
    }//GEN-LAST:event_jMenuSaveActionPerformed

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        //click in afara tabelului deselecteaza randul curent
        if(jTable1.getSelectedRow() != -1){
            jTable1.clearSelection();
        }
    }//GEN-LAST:event_formMouseClicked

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        //al doilea click pe acelasi rand incarca meniul de modificare
        if(/*evt.getButton() == 1 && */evt.getClickCount() == 2){
            modificaIncarca();
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jTable1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MousePressed
        //selectare rand tabel si prin click dreapta pentru a selecta si afisa meniul popup printr-un singur click
        Point point = evt.getPoint();
        int currentRow = jTable1.rowAtPoint(point);
        jTable1.setRowSelectionInterval(currentRow, currentRow);
    }//GEN-LAST:event_jTable1MousePressed

    private void jMenuItemStergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemStergeActionPerformed
        if(jTable1.getSelectedRow() != -1){        
            jDialogSterge.setLocationRelativeTo(null);
            jDialogSterge.setVisible(true);
        } else {
            info("Selectati un abonat din tabel");
        }
    }//GEN-LAST:event_jMenuItemStergeActionPerformed

    private void jMenuItemModificaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemModificaActionPerformed
        modificaIncarca();
    }//GEN-LAST:event_jMenuItemModificaActionPerformed

    private void jTable1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_DELETE){
            if(jTable1.getSelectedRow() != -1){        
                jDialogSterge.setLocationRelativeTo(null);
                jDialogSterge.setVisible(true);
            } else {
                return;
                //nu afisez nimic daca se apasa pe delete fara abonat selectat
                //info("Selectati un abonat din tabel");
            }
        }
    }//GEN-LAST:event_jTable1KeyPressed

    private void jMenuDebugActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuDebugActionPerformed
//  DOAR PENTRU DEBUG - creeaza o lista de abonati random folosind metodele din test.ListaAbonati
        try {
            test.ListaAbonati lista = new test.ListaAbonati();
            CarteDeTelefon m2 = lista.adaugareDateRandomJar();
            m.adaugaDebug(lista.getArrayListAbonat(m2));
        } catch (IOException iOException) {
            eroare(iOException);
        }
    }//GEN-LAST:event_jMenuDebugActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //metodele ce tin de splash screen
        splashInit();
        appInit();
        if (mySplash != null){
            mySplash.close();
        }
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainframe = new MainFrame();
                mainframe.setVisible(true);
                }
            });
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton jButtonAbout;
    private javax.swing.JButton jButtonAdauga;
    private javax.swing.JButton jButtonAdaugaAdauga;
    private javax.swing.JButton jButtonAdaugaCancel;
    private javax.swing.JButton jButtonButton;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonCancelInreg;
    private javax.swing.JButton jButtonCancelSort;
    private javax.swing.JButton jButtonDaIesire;
    private javax.swing.JButton jButtonIesire;
    private javax.swing.JButton jButtonModifCancel;
    private javax.swing.JButton jButtonModifModifica;
    private javax.swing.JButton jButtonModifica;
    private javax.swing.JButton jButtonNuIesire;
    private javax.swing.JButton jButtonOKInreg;
    private javax.swing.JButton jButtonSort;
    private javax.swing.JButton jButtonSortare;
    private javax.swing.JButton jButtonSterge;
    private javax.swing.JButton jButtonStergeDa;
    private javax.swing.JButton jButtonStergeNu;
    private javax.swing.JComboBox<String> jComboAdaugaTel;
    private javax.swing.JComboBox<String> jComboBoxCauta;
    private javax.swing.JComboBox<String> jComboModif;
    private javax.swing.JDialog jDialogAbout;
    private javax.swing.JDialog jDialogAdauga;
    private javax.swing.JDialog jDialogCauta;
    private javax.swing.JDialog jDialogIesire;
    private javax.swing.JDialog jDialogInregistrare;
    private javax.swing.JDialog jDialogModifica;
    private javax.swing.JDialog jDialogSortare;
    private javax.swing.JDialog jDialogSterge;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelCautare;
    private javax.swing.JLabel jLabelSalvare;
    private javax.swing.JLabel jLabelShareware;
    private javax.swing.JMenu jMenuAbonati;
    private javax.swing.JMenuItem jMenuAbout;
    private javax.swing.JMenuItem jMenuAdauga;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuDebug;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuIesire;
    private javax.swing.JMenuItem jMenuInregistrare;
    private javax.swing.JMenuItem jMenuItemModifica;
    private javax.swing.JMenuItem jMenuItemSterge;
    private javax.swing.JMenuItem jMenuLoad;
    private javax.swing.JMenuItem jMenuModifica;
    private javax.swing.JMenuItem jMenuSave;
    private javax.swing.JMenuItem jMenuSortare;
    private javax.swing.JMenuItem jMenuSterge;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JRadioButton jRadioButtonAsc;
    private javax.swing.JRadioButton jRadioButtonCNP;
    private javax.swing.JRadioButton jRadioButtonDesc;
    private javax.swing.JRadioButton jRadioButtonNume;
    private javax.swing.JRadioButton jRadioButtonPrenume;
    private javax.swing.JRadioButton jRadioButtonTelefon;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextAdaugaCNP;
    private javax.swing.JTextField jTextAdaugaNume;
    private javax.swing.JTextField jTextAdaugaPrenume;
    private javax.swing.JTextField jTextAdaugaTelefon;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextFieldCauta;
    private javax.swing.JTextField jTextFieldInregistrare;
    private javax.swing.JTextField jTextModificaCNP;
    private javax.swing.JTextField jTextModificaNume;
    private javax.swing.JTextField jTextModificaPrenume;
    private javax.swing.JTextField jTextModificaTelefon;
    private javax.swing.JToggleButton jToggleButtonDatabase;
    // End of variables declaration//GEN-END:variables
}
