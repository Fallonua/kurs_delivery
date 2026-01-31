package delivery.transport;

import delivery.model.DeliveryInfo;
import delivery.model.Point;

/**
 * Класс курьера - финального звена цепочки доставки.
 * 
 * Наследуется от TTransportUnit и реализует специфику курьера:
 * - Самая высокая скорость движения (60 единиц/час)
 * - Самая высокая стоимость за единицу расстояния (1.5)
 * - Малая вместимость (2 посылки)
 * 
 * Курьер выполняет финальный этап доставки:
 * - Получает посылки от грузовых фургонов
 * - Доставляет посылки непосредственно в пункты назначения
 */
public class TCourier extends TTransportUnit {

    // Базовая стоимость за единицу расстояния для курьера.
    private static final double BASE_COST = 1.5;

    // Точка, к которой курьер сейчас едет (пункт назначения посылки или null).
    private Point currentTarget;
    // Следующая точка доставки в очереди (или null).
    private Point nextDeliveryPoint;

    /**
     * Создает нового курьера с указанными параметрами.
     * 
     * @param currentLocation начальное местоположение курьера
     * @param capacity вместимость (2 посылки)
     * @param speed скорость движения (обычно 60 единиц/час)
     */
    public TCourier(Point currentLocation, int capacity, double speed) {
        super(currentLocation, capacity, speed);
    }

    // Рассчитывает информацию о доставке до указанной точки.
    @Override
    public DeliveryInfo moveTo(Point destination) {
        // Вычисляем расстояние от текущей позиции до цели
        double distance = currentLocation.distanceTo(destination);
        
        // Рассчитываем время доставки: расстояние / скорость
        double time = distance / speed;
        
        // Рассчитываем стоимость доставки: расстояние * базовая стоимость
        double cost = distance * BASE_COST;
        
        // Возвращаем объект с информацией о доставке
        return new DeliveryInfo(time, cost);
    }

    public Point getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(Point currentTarget) {
        this.currentTarget = currentTarget;
    }

    public Point getNextDeliveryPoint() {
        return nextDeliveryPoint;
    }

    public void setNextDeliveryPoint(Point nextDeliveryPoint) {
        this.nextDeliveryPoint = nextDeliveryPoint;
    }
}
