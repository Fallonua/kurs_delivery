package delivery.model;

/**
 * Посылка
 * 
 * Содержит информацию о:
 * - Уникальном идентификаторе посылки
 * - Точке назначения доставки
 * - Весе посылки
 * - Текущем статусе в цепочке доставки
 */

public class TPackage {
    // Уникальный идентификатор посылки
    private String packageId;
    
    // Точка назначения доставки посылки
    private Point destinationPoint;
    
    // Вес посылки
    private double weight;
    
    // Текущий статус посылки
    private PackageStatus status;
    
    // Стоимость доставки посылки
    private double deliveryCost;
    
    // Время доставки посылки в часах
    private double deliveryTime;

    // Создает новую посылку с указанными параметрами.
    public TPackage(String packageId, Point destinationPoint, double weight) {
        this.packageId = packageId; //this помогает различить поля объекта и параметры метода, когда у них одинаковые имена.
        this.destinationPoint = destinationPoint;
        this.weight = weight;
        // При создании посылка всегда находится в сортировочном центре
        this.status = PackageStatus.IN_CENTER;
    }

    // Возвращает уникальный идентификатор посылки.
    public String getPackageId() {
        return packageId;
    }

    /**
     * Возвращает номер посылки из идентификатора (PKG-1 → 1, PKG-10 → 10).
     * При неверном формате возвращает 0.
     */
    public int getPackageNumber() {
        if (packageId == null || !packageId.contains("-")) {
            return 0;
        }
        try {
            String suffix = packageId.substring(packageId.lastIndexOf("-") + 1);
            return Integer.parseInt(suffix);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Возвращает точку назначения доставки посылки.
    public Point getDestinationPoint() {
        return destinationPoint;
    }

    // Возвращает вес посылки.
    public double getWeight() {
        return weight;
    }

    // Возвращает текущий статус посылки.
    public PackageStatus getStatus() {
        return status;
    }

    // Устанавливает новый статус посылки.
    public void setStatus(PackageStatus status) {
        this.status = status;
    }
    
    // Возвращает стоимость доставки посылки.
    public double getDeliveryCost() {
        return deliveryCost;
    }
    
    // Устанавливает стоимость доставки посылки.
    public void setDeliveryCost(double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }
    
    // Возвращает время доставки посылки в часах.
    public double getDeliveryTime() {
        return deliveryTime;
    }
    
    // Устанавливает время доставки посылки в часах.
    public void setDeliveryTime(double deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    // Возвращает троковое представление посылки с информацией о весе, времени и статусе
    @Override
    public String toString() {
        if (status == PackageStatus.DELIVERED && deliveryCost > 0) {
            return String.format("%s\nВес: %.2f | Время: %.2f ч | Стоимость: %.2f", 
                packageId, weight, deliveryTime, deliveryCost);
        }
        return String.format("%s\nВес: %.2f | Статус: %s", packageId, weight, status);
    }
}
