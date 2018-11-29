package fr.enssat.cheikhsalioundiaye.playervideoenrichi;

//classe qui comprend les informations des URLs
public class URL_Position {
    String URL;//String qui correspond a l'url
    int start; //Int qui correspond au temps de départ de l'affichage de l'url
    public URL_Position(String URL, int start){
        this.URL = URL;
        this.start = start;
    }
}
