package pvz.com.systems;

import pvz.com.entities.Entity;
import pvz.com.entities.components.ArmingComponent;
import pvz.com.entities.components.EntityState;
import pvz.com.entities.components.ExplosiveComponent;
import pvz.com.entities.components.HealthComponent;
import pvz.com.entities.components.StateComponent;
import pvz.com.entities.plants.bombs.PotatoMine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class PotatoMineStateSystem {

    public void update(List<Entity> entities) {
        for (Entity entity : entities) {

            // 1) Chỉ xử lý PotatoMine
            if (!(entity instanceof PotatoMine))
                continue;

            // 2) Lấy component
            HealthComponent health = entity.getComponent(HealthComponent.class);
            StateComponent state = entity.getComponent(StateComponent.class);
            ArmingComponent arming = entity.getComponent(ArmingComponent.class);
            ExplosiveComponent explosive = entity.getComponent(ExplosiveComponent.class);

            if (state == null)
                continue;

            // 3) Nếu chết do bị ăn/đánh
            if (health != null && health.currentHealth <= 0) {
                if (state.get() != EntityState.DYING)
                    state.set(EntityState.DYING);
                continue;
            }

            // 4) Nếu đã kích nổ thì chuyển EXPLODING
            if (explosive != null && isExplosiveTriggered(explosive)) {
                if (state.get() != EntityState.POTATOMINE_EXPLODING) {
                    state.set(EntityState.POTATOMINE_EXPLODING);
                }
                continue;
            }

            // 5) Dựa vào arming để set UNARMED / ARMING / ARMED
            EntityState newState = decideByArming(arming);
            if (state.get() != newState) {
                state.set(newState);
            }
        }
    }

    private EntityState decideByArming(ArmingComponent arming) {
        if (arming == null) {
            // Không có arming component thì coi như "ready" (tùy game bạn)
            return EntityState.POTATOMINE_ARMED;
        }

        boolean armed = readBoolean(arming, "isArmed", "armed", "isReady", "ready");
        if (armed)
            return EntityState.POTATOMINE_ARMED;

        // phân biệt UNARMED vs ARMING bằng việc timer đã chạy hay chưa
        float elapsed = readFloat(arming, "elapsed", "timer", "timePassed", "armingTimer", "time");
        float remaining = readFloat(arming, "remaining", "remainingTime", "timeLeft");

        boolean started = (elapsed > 0f) || (remaining > 0f); // nếu có timer/remaining thì coi là đã bắt đầu
        return started ? EntityState.POTATOMINE_ARMING : EntityState.POTATOMINE_UNARMED;
    }

    // =======================
    // Reflection helpers (để không phụ thuộc chặt vào field/method của component)
    // =======================

    private boolean isExplosiveTriggered(ExplosiveComponent explosive) {
        // thử các tên phổ biến
        if (readBoolean(explosive, "isTriggered", "triggered", "isExploded", "exploded", "detonated"))
            return true;
        // nếu không có cờ, mặc định là chưa
        return false;
    }

    private boolean readBoolean(Object obj, String... keys) {
        // 1) Try method: isX(), getX()
        for (String k : keys) {
            Boolean v = tryInvokeBoolean(obj, k);
            if (v != null)
                return v;
            v = tryInvokeBoolean(obj, "get" + cap(k));
            if (v != null)
                return v;
            v = tryInvokeBoolean(obj, "is" + cap(k));
            if (v != null)
                return v;
        }
        // 2) Try field
        for (String k : keys) {
            Boolean v = tryReadBooleanField(obj, k);
            if (v != null)
                return v;
        }
        return false;
    }

    private float readFloat(Object obj, String... keys) {
        // 1) Try method
        for (String k : keys) {
            Float v = tryInvokeFloat(obj, k);
            if (v != null)
                return v;
            v = tryInvokeFloat(obj, "get" + cap(k));
            if (v != null)
                return v;
        }
        // 2) Try field
        for (String k : keys) {
            Float v = tryReadFloatField(obj, k);
            if (v != null)
                return v;
        }
        return 0f;
    }

    private String cap(String s) {
        if (s == null || s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private Boolean tryInvokeBoolean(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getMethod(methodName);
            Object r = m.invoke(obj);
            if (r instanceof Boolean)
                return (Boolean) r;
        } catch (Exception ignored) {
        }
        return null;
    }

    private Float tryInvokeFloat(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getMethod(methodName);
            Object r = m.invoke(obj);
            if (r instanceof Float)
                return (Float) r;
            if (r instanceof Number)
                return ((Number) r).floatValue();
        } catch (Exception ignored) {
        }
        return null;
    }

    private Boolean tryReadBooleanField(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object r = f.get(obj);
            if (r instanceof Boolean)
                return (Boolean) r;
        } catch (Exception ignored) {
        }
        return null;
    }

    private Float tryReadFloatField(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object r = f.get(obj);
            if (r instanceof Float)
                return (Float) r;
            if (r instanceof Number)
                return ((Number) r).floatValue();
        } catch (Exception ignored) {
        }
        return null;
    }
}
