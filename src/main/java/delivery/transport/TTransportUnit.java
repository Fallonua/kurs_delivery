package delivery.transport;

import delivery.model.DeliveryInfo;
import delivery.model.Point;
import delivery.model.Status;
import delivery.model.TPackage;

import java.util.Arrays;

/**
 * Абстрактный базовый класс для всех транспортных единиц в системе доставки.
 *
 * Определяет общую структуру и поведение для подвижных единиц:
 * - Грузовой фургон (TDeliveryVan)
 * - Курьер (TCourier)
 *
 * Реализует общую функциональность:
 * - Хранение текущей позиции
 * - Управление грузом (загрузка/разгрузка посылок)
 * - Отслеживание статуса (FREE/ON_MOVE)
 *
 * Абстрактный метод moveTo() реализован в каждом наследнике
 * с учетом специфики типа транспортной единицы (скорость, стоимость).
 */
public abstract class TTransportUnit {
    // Текущее местоположение транспортной единицы (точка на карте)
    protected Point currentLocation;
    
    // Максимальная вместимость транспортной единицы (количество посылок)
    protected int capacity;
    
    // Массив посылок и текущее количество
    protected TPackage[] cargo;
    protected int cargoCount;
    
    // Скорость движения транспортной единицы (условные единицы в час)
    protected double speed;
    
    // Текущий статус транспортной единицы (FREE или ON_MOVE)
    protected Status status;

    //Создает новую посылку с указанными параметрами.
    public TTransportUnit(Point currentLocation, int capacity, double speed) {
        this.currentLocation = currentLocation;
        this.capacity = capacity;
        this.speed = speed;
        this.cargo = new TPackage[capacity];
        this.cargoCount = 0;
        this.status = Status.FREE;
    }

    /**
     * Абстрактный метод для расчета информации о доставке до указанной точки.
     *
     * Формула расчета времени: расстояние / скорость
     * Формула расчета стоимости: расстояние * базовая_стоимость
     * 
     * @param destination - целевая точка назначения
     * @return - объект DeliveryInfo с рассчитанным временем и стоимостью доставки
     */
    public abstract DeliveryInfo moveTo(Point destination);

    /**
     * Загружает посылку в транспортную единицу.
     * 
     * @param pkg посылка для загрузки
     * @return true, если посылка успешно загружена, false если нет свободного места
     */
    public boolean loadPackage(TPackage pkg) {
        if (cargoCount < capacity) {
            cargo[cargoCount++] = pkg;
            return true;
        }
        return false;
    }

    public TPackage unloadPackage(TPackage pkg) {
        for (int i = 0; i < cargoCount; i++) {
            if (cargo[i] == pkg) {
                // Сдвигаем элементы влево для сохранения порядка
                for (int j = i; j < cargoCount - 1; j++) {
                    cargo[j] = cargo[j + 1];
                }
                cargo[cargoCount - 1] = null;
                cargoCount--;
                return pkg;
            }
        }
        return null;
    }

    // Возвращает текущее местоположение транспортной единицы.
    public Point getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Устанавливает новое местоположение транспортной единицы.
     * 
     * Используется при анимации перемещения объектов.
     * 
     * @param currentLocation новая точка местоположения
     */
    public void setCurrentLocation(Point currentLocation) {
        this.currentLocation = currentLocation;
    }

    // Возвращает максимальную вместимость транспортной единицы.
    public int getCapacity() {
        return capacity;
    }

    /** Возвращает копию массива посылок (длина = cargoCount). */
    public TPackage[] getCargoList() {
        return Arrays.copyOf(cargo, cargoCount);
    }

    // Возвращает скорость движения транспортной единицы.
    public double getSpeed() {
        return speed;
    }

    // Возвращает текущий статус транспортной единицы (FREE или ON_MOVE)
    public Status getStatus() {
        return status;
    }

    // Устанавливает новый статус транспортной единицы (FREE или ON_MOVE).
    public void setStatus(Status status) {
        this.status = status;
    }
}
