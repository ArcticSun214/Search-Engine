//A class meant to be used as a frontier entry. The X is a string
//representing a URL. The Y is an integer representing hop level of the URL.
public class Tuple<X, Y> {
    public final X x;
    public final Y y;
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}
    
