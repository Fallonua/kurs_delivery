package delivery.transport;

import delivery.model.Point;
import delivery.model.TPackage;

import java.util.Arrays;

/**
 * Класс сортировочного центра - начального звена цепочки доставки.
 *
 * - Большая вместимость (100 посылок)
 * - Хранение текущего местоположения
 * - Управление грузом (загрузка/разгрузка посылок)
 */
public class TSortingCenter {
    private Point currentLocation;
    private int capacity;
    private TPackage[] cargo;
    private int cargoCount;

    /**
     * Создает новый сортировочный центр с указанными параметрами.
     *
     * @param currentLocation местоположение сортировочного центра
     * @param capacity        вместимость (100 посылок)
     */
    public TSortingCenter(Point currentLocation, int capacity) {
        this.currentLocation = currentLocation;
        this.capacity = capacity;
        this.cargo = new TPackage[capacity];
        this.cargoCount = 0;
    }

    /**
     * Загружает посылку в сортировочный центр.
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

    /**
     * Разгружает посылку из сортировочного центра.
     *
     * @param pkg посылка для разгрузки
     * @return посылку, если она была успешно удалена, иначе null
     */
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

    /**
     * Возвращает копию массива посылок в сортировочном центре.
     */
    public TPackage[] getCargoList() {
        return Arrays.copyOf(cargo, cargoCount);
    }

    /**
     * Возвращает текущее местоположение сортировочного центра.
     */
    public Point getCurrentLocation() {
        return currentLocation;
    }
}
