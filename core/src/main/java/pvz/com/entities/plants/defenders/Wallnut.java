package pvz.com.entities.plants.defenders;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;

public class Wallnut extends Plant {

    private static final float SCALE_X = 0.7f; // rộng 70% ô
    private static final float SCALE_Y = 0.8f; // cao 80% ô

    // Constructor CHÍNH: có gridCol, gridRow để sync với grid
    public Wallnut(float x, float y, int gridCol, int gridRow) {
        // Khung sườn: vị trí + kích thước theo ô
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // Hình ảnh
        this.addComponent(new SpriteComponent("images/Plants/Wallnut.gif"));

        // Máu trâu
        this.addComponent(new HealthComponent(4000));

        // Phe
        this.addComponent(new TeamComponent(Team.PLANT));

        // Vị trí trên lưới
        this.addComponent(new GridPositionComponent(gridCol, gridRow));

        // Trạng thái (để sau này đổi sprite theo % máu)
        this.addComponent(new StateComponent(EntityState.IDLE));
    }

    // OPTIONAL: overload giữ backward compatibility
    public Wallnut(float x, float y) {
        this(x, y, -1, -1);
    }
}
