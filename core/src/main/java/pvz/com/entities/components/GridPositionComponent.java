package pvz.com.entities.components;

public class GridPositionComponent {
    public int col; // Cột (x)
    public int row; // Hàng/Làn đường (y)

    public GridPositionComponent(int col, int row) {
        this.col = col;
        this.row = row;
    }
    
    public void set(int col, int row) {
        this.col = col;
        this.row = row;
    }
}