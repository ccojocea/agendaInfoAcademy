Note:
Daca aplicatia este pornita utilizand jar-ul, directorul src/agendaFiles cu pozele pentru banner trebuie sa existe in acelasi director cu Agenda.jar (asta afecteaza toate operatiunile de scriere pe disc)

Cand programul porneste, verifica existenta unui fisier de inregistrare. Daca acesta exista, este corect si nu este corupt in nici un fel, atunci programul afiseaza interfata completa. Daca nu exista, se afiseaza modul shareware.
Daca se introduce codul de inregistrare ("test1"), se afiseaza interfata completa si se scrie fisierul de inregistrare. Daca se introduce codul de inregistrare ("debug"), va aparea o noua optiune in meniu cu care se poate popula lista cu abonati randomizati (87 de CNP-uri unice). Pentru a "reseta" aplicatia, se sterge fisierul de inregistrare din src\agendaFiles.

Daca exista date salvate anterior, acestea sunt incarcate automat la pornire, si salvate automat la iesire din program. Daca userul foloseste optiunea de save manual si specifica un alt fisier, acesta va fi incarcat automat si se salveaza automat pe el la iesire (va ramane separat fisierul vechi de salvare... daca exista)

Pentru implementarea salvarii automate la 5 min(folosit 1 minut din motive de testare mai rapida) am folosit java.util.Timer, desi in timerul respectiv pe langa salvare schimb si textul dintr-un label care sa notifice utilizatorul.
A ramas comentata in codul sursa si o implementare folosind javax.swing.Timer pentru aceeasi salvare automata. Sunt curios ce rezolvare mai eleganta ar exista pentru aceasta parte de cod (Am 2 timere, unul in celalalt), sau daca era mai bine sa folosesc altceva din start.

Am inclus un fisier cu date salvate anterior dar separat (acesta nu va fi incarcat automat - agenda1.agd). Daca fisierul va fi incarcat, programul va face toate salvarile automate pe el si va fi setat ca nou fisier de incarcare automata la pornire. Acest fisier este inclus doar din motive de testare. Acesta a fost creat manual cu versiunea 1.0. In versiunea 1.1 se poate popula lista folosind optiunea de debug.

Change List
v1.1
- Added limited "Debug" options
- Fixed sorting bug