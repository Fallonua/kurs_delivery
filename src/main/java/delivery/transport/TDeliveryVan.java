package delivery.transport;

import delivery.model.DeliveryInfo;
import delivery.model.Point;

/**
 * Класс грузового фургона - промежуточного звена цепочки доставки.
 * 
 * Наследуется от TTransportUnit и реализует специфику грузового фургона:
 * - Средняя скорость движения (40 единиц/час)
 * - Средняя стоимость за единицу расстояния (0.5)
 * - Средняя вместимость (10 посылок)
 * 
 * Грузовой фургон выполняет роль промежуточного транспорта:
 * - Забирает посылки из сортировочного центра
 * - Транспортирует их к точке передачи курьерам
 */
public class TDeliveryVan extends TTransportUnit {

    //Базовая стоимость за единицу расстояния для грузового фургона.
    private static final double BASE_COST = 0.5;

    /**
     * Создает новый грузовой фургон с указанными параметрами.
     * 
     * @param currentLocation начальное местоположение фургона
     * @param capacity вместимость (обычно 20 посылок)
     * @param speed скорость движения (обычно 40 единиц/час - средняя)
     */
    public TDeliveryVan(Point currentLocation, int capacity, double speed) {
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
}
