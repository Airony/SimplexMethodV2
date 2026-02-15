package models;

public class Vertex<T> {
    private T x ; 
    private T y ;
    
    public Vertex(T x, T y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Vertex [x=" + x + ", y=" + y + "]";
    }

    public T getX() {
        return x;
    }

    public T getY() {
        return y;
    }

    public void setX(T x) {
        this.x = x;
    }

    public void setY(T y) {
        this.y = y;
    }
    public Vertex<T> clone() {
        return new Vertex<T>(x, y) ; 
    } 
    
}
