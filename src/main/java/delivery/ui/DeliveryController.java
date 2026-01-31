package delivery.ui;

import delivery.model.*;
import delivery.transport.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Random;

/**
 * Контроллер для управления интерфейсом приложения доставки посылок.
 * Связывает FXML интерфейс с бизнес-логикой приложения.
 * 
 * Отвечает за:
 * - Обработку действий пользователя (нажатия кнопок)
 * - Управление визуализацией объектов на Canvas
 * - Координацию анимации перемещения объектов
 * - Управление цепочкой доставки посылок
 */
public class DeliveryController {
    
    // ========== Константы для размеров ==========
    /** Ширина области визуализации (Canvas) */
    private static final double CANVAS_WIDTH = 500.0;
    /** Высота области визуализации (Canvas) */
    private static final double CANVAS_HEIGHT = 500.0;
    
    // ========== FXML элементы интерфейса ==========
    /** Canvas для отрисовки объектов доставки */
    @FXML
    private Canvas visualizationCanvas;
    
    /** Кнопка создания новой посылки */
    @FXML
    private Button createPackageButton;
    
    /** Кнопка запуска симуляции доставки */
    @FXML
    private Button startSimulationButton;
    
    /** Кнопка сброса симуляции */
    @FXML
    private Button resetButton;
    
    /** Метка для отображения информации о доставке */
    @FXML
    private Label infoLabel;

    /** Метка для отображения статуса курьера */
    @FXML
    private Label courierStatusLabel;
    
    /** Список для отображения информации о посылках */
    @FXML
    private ListView<String> packageListView;
    
    /** Наблюдаемый список строк для ListView */
    private ObservableList<String> packageListItems;
    
    // ========== Объекты доставки ==========
    /** Сортировочный центр - начальная точка цепочки доставки */
    private TSortingCenter sortingCenter;
    
    /** Грузовой фургон - промежуточное звено доставки */
    private TDeliveryVan deliveryVan;
    
    /** Курьер - финальное звено доставки */
    private TCourier courier;
    
    /** Массив созданных посылок и текущее количество */
    private TPackage[] packages;
    private int packageCount;

    /** Начальная вместимость массива посылок, удваивается при переполнении */
    private static final int PACKAGES_INITIAL_CAPACITY = 16;
    
    // ========== Инструменты для визуализации и анимации ==========
    /** Контекст для рисования на Canvas */
    private GraphicsContext gc;
    
    /** Таймлайн для управления анимацией перемещения объектов */
    private Timeline animationTimeline;
    
    /** Флаг, указывающий, выполняется ли в данный момент анимация */
    private boolean isAnimating;

    /** Допуск для сравнения точек (координаты) */
    private static final double POINT_EPS = 1e-6;

    /**
     * Инициализация контроллера после загрузки FXML.
     * Вызывается автоматически JavaFX после создания всех FXML элементов.
     * 
     * Выполняет:
     * - Получение GraphicsContext для Canvas
     * - Инициализацию объектов доставки
     * - Первоначальную отрисовку сцены
     */
    @FXML
    private void initialize() {
        // Получаем контекст для рисования на Canvas
        gc = visualizationCanvas.getGraphicsContext2D();
        
        // Инициализируем список для отображения посылок
        packageListItems = FXCollections.observableArrayList();
        packageListView.setItems(packageListItems);

        // Создаем все объекты (центр, фургон, курьер)
        initializeObjects();
        
        // Отрисовываем начальное состояние сцены
        drawScene();

        // Обновляем список посылок (пока пустой)
        updatePackageList();
    }
    
    /**
     * Инициализация объектов доставки.
     * Создает и размещает на сцене:
     * - Сортировочный центр (левый нижний угол)
     * - Грузовой фургон (левый верхний угол)
     * - Курьера (правый верхний угол)
     */
    private void initializeObjects() {
        // Сортировочный центр размещается в левом нижнем углу
        // Имеет большую вместимость (100 посылок)
        sortingCenter = new TSortingCenter(
            new Point(50, CANVAS_HEIGHT - 100),
            100   // Вместимость
        );
        
        // Грузовой фургон размещается в левом верхнем углу
        // Имеет среднюю вместимость (10 посылок) и среднюю скорость (40 единиц/час)
        deliveryVan = new TDeliveryVan(
            new Point(50, 50),
            10,   // Вместимость
            40.0  // Скорость (средняя)
        );
        
        // Курьер размещается в правом верхнем углу
        // Имеет малую вместимость (2 посылки) и высокую скорость (60 единиц/час)
        courier = new TCourier(
            new Point(CANVAS_WIDTH - 50, 50),
            2,    // Вместимость
            60.0  // Скорость (высокая)
        );
        
        packages = new TPackage[PACKAGES_INITIAL_CAPACITY];
        packageCount = 0;
        isAnimating = false;
    }
    
    /**
     * Обработчик нажатия кнопки "Создать посылку".
     * 
     * Создает новую посылку со случайными параметрами:
     * - ID не случайное (например, PKG-1, PKG-2, ...)
     * - Случайная точка назначения в пределах Canvas
     * - Случайный вес от 1.0 до 11.0
     * 
     * Посылка автоматически размещается в сортировочном центре.
     */
    @FXML
    private void handleCreatePackage() {
        // Проверяем, не выполняется ли сейчас анимация
        if (isAnimating) {
            infoLabel.setText("Дождитесь завершения текущей симуляции!");
            return;
        }
        
        Random random = new Random();
        String packageId = "PKG-" + (packageCount + 1);
        double x = 70 + random.nextDouble() * (CANVAS_WIDTH - 200);
        double y = 70 + random.nextDouble() * (CANVAS_HEIGHT - 200);
        Point destination = new Point(x, y);
        double weight = 1.0 + random.nextDouble() * 10.0;
        TPackage pkg = new TPackage(packageId, destination, weight);

        if (packageCount >= packages.length) {
            packages = Arrays.copyOf(packages, packages.length * 2);
        }
        packages[packageCount++] = pkg;
        sortingCenter.loadPackage(pkg);
        
        // Обновляем визуализацию
        drawScene();
        
        // Обновляем список посылок
        updatePackageList(); // 427 строка
        // Обновляем информационную панель
        infoLabel.setText("Создана посылка: " + pkg.getPackageId());
        //Вызывается getPackageId() (файл TPackage.java, строки 42-44), показывается сообщение.
    }
    
    /**
     * Обработчик нажатия кнопки "Запустить симуляцию".
     * 
     * Запускает цепочку доставки для посылок в сортировочном центре:
     * 1. Проверяет наличие посылок
     * 2. Проверяет наличие не доставленных посылок в центре
     * 3. Запускает цепочку доставки
     */
    @FXML
    private void handleStartSimulation() {
        // Проверяем, не выполняется ли уже анимация
        if (isAnimating) {
            infoLabel.setText("Симуляция уже выполняется!");
            return;
        }
        
        int inCenter = 0;
        for (TPackage pkg : sortingCenter.getCargoList()) {
            if (pkg.getStatus() == PackageStatus.IN_CENTER) inCenter++;
        }
        if (inCenter == 0) {
            infoLabel.setText("Нет посылок в сортировочном центре!");
            return;
        }
        
        // Устанавливаем флаг анимации и запускаем цепочку доставки
        isAnimating = true;
        executeDeliveryChain();
    }
    
    /**
     * Обработчик нажатия кнопки "Сбросить".
     * 
     * Останавливает текущую анимацию и сбрасывает все объекты в исходное состояние:
     */
    @FXML
    private void handleReset() {
        // Останавливаем анимацию, если она выполняется
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        
        // Сбрасываем флаг анимации
        isAnimating = false;
        
        initializeObjects();
        packageCount = 0;
        drawScene();
        updatePackageList();
        
        // Обновляем информационную панель
        infoLabel.setText("Симуляция сброшена");
    }
    
    /**
     * Выполнение цепочки доставки посылок.
     * 
     * Последовательность этапов:
     * 1. Фургон едет к сортировочному центру
     * 2. Загружаем посылки из центра в фургон (до вместимости фургона)
     * 3. Фургон едет к точке передачи (центр Canvas)
     * 4. Курьер едет к точке передачи
     * 5. Передаем посылки из фургона курьеру (до вместимости курьера)
     * 6. Курьер доставляет посылки по очереди (первая - полная стоимость, вторая - стоимость фургона)
     * 7. Доставка завершена, выводятся итоговые данные
     */
    private void executeDeliveryChain() {
        // ========== ЭТАП 1: Фургон едет к сортировочному центру ==========
        Point sortingCenterLoc = sortingCenter.getCurrentLocation();
        // Анимируем движение фургона к центру (2 секунды)
        animateMove(deliveryVan, sortingCenterLoc, 2000, () -> { //
            // Этот код выполнится ПОСЛЕ того, как фургон доедет
            // ========== ЭТАП 2: Загрузка посылок из центра в фургон ==========
            int vanCap = deliveryVan.getCapacity();
            TPackage[] packagesToLoad = new TPackage[vanCap];
            int toLoadCount = 0;
            for (TPackage pkg : sortingCenter.getCargoList()) {
                if (pkg.getStatus() == PackageStatus.IN_CENTER && toLoadCount < vanCap) {
                    packagesToLoad[toLoadCount++] = pkg;
                }
            }
            for (int i = 0; i < toLoadCount; i++) {
                TPackage pkg = packagesToLoad[i];
                sortingCenter.unloadPackage(pkg);
                deliveryVan.loadPackage(pkg);
                pkg.setStatus(PackageStatus.IN_VAN);
            }
            drawScene();
            updatePackageList();
            infoLabel.setText(String.format("Загружено %d посылок в фургон", toLoadCount));
            
            // ========== ЭТАП 3: Фургон едет к точке передачи ==========
            // Точка передачи - центр Canvas
            Point transferPoint = new Point(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2);
            // Рассчитываем стоимость фургона
            // Вызывается метод из TDeliveryVan.java, строки 36-48
            DeliveryInfo vanToTransfer = deliveryVan.moveTo(transferPoint);
            
            animateMove(deliveryVan, transferPoint, 2000, () -> {
                // ========== ЭТАП 4: Курьер едет к точке передачи ==========
                // Движение курьера к точке передачи не входит в стоимость посылок
                courier.setCurrentTarget(null);
                courier.setNextDeliveryPoint(null);
                animateMove(courier, transferPoint, 1500, () -> {
                    // ========== ЭТАП 5: Передача посылок из фургона курьеру ==========
                    int courierCap = courier.getCapacity();
                    TPackage[] packagesForCourier = new TPackage[courierCap];
                    int forCourierCount = 0;
                    for (TPackage pkg : deliveryVan.getCargoList()) {
                        if (pkg.getStatus() == PackageStatus.IN_VAN && forCourierCount < courierCap) {
                            packagesForCourier[forCourierCount++] = pkg;
                        }
                    }
                    for (int i = 0; i < forCourierCount; i++) {
                        TPackage pkg = packagesForCourier[i];
                        deliveryVan.unloadPackage(pkg);
                        courier.loadPackage(pkg);
                        pkg.setStatus(PackageStatus.WITH_COURIER);
                    }
                    drawScene();
                    updatePackageList();
                    infoLabel.setText(String.format("Передано %d посылок курьеру", forCourierCount));
                    deliverPackagesSequentially(packagesForCourier, forCourierCount, 0, vanToTransfer);
                });
            });
        });
    }
    
    /**
     * Доставляет посылки курьером по очереди.
     * Стоимость рассчитывается так:
     * - Стоимость фургона = расстояние от сортировочного центра до точки передачи * стоимость фургона
     * - Стоимость курьера = расстояние от точки передачи до места назначения * стоимость курьера
     * После доставки всех посылок проверяет, остались ли посылки в фургоне, и если да - продолжает доставку.
     * 
     * @param packagesToDeliver массив посылок для доставки
     * @param deliverCount      количество посылок в массиве
     * @param index             текущий индекс
     * @param vanToTransfer     информация о движении фургона от центра до точки передачи
     */
    private void deliverPackagesSequentially(TPackage[] packagesToDeliver, int deliverCount, int index,
                                             DeliveryInfo vanToTransfer) {
        if (index >= deliverCount) {
            int remCap = deliveryVan.getCapacity();
            TPackage[] remainingPackages = new TPackage[remCap];
            int remCount = 0;
            for (TPackage pkg : deliveryVan.getCargoList()) {
                if (pkg.getStatus() == PackageStatus.IN_VAN && remCount < remCap) {
                    remainingPackages[remCount++] = pkg;
                }
            }
            if (remCount > 0) {
                final int remCountFinal = remCount;
                Point transferPoint = new Point(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2);
                infoLabel.setText(String.format("Курьер возвращается за оставшимися %d посылками", remCount));
                courier.setCurrentTarget(null);
                courier.setNextDeliveryPoint(null);
                animateMove(courier, transferPoint, 1500, () -> {
                    int batchCap = courier.getCapacity();
                    TPackage[] nextBatch = new TPackage[batchCap];
                    int batchCount = 0;
                    for (int i = 0; i < remCountFinal && batchCount < batchCap; i++) {
                        TPackage pkg = remainingPackages[i];
                        deliveryVan.unloadPackage(pkg);
                        courier.loadPackage(pkg);
                        pkg.setStatus(PackageStatus.WITH_COURIER);
                        nextBatch[batchCount++] = pkg;
                    }
                    drawScene();
                    updatePackageList();
                    infoLabel.setText(String.format("Забрано еще %d посылок", batchCount));
                    deliverPackagesSequentially(nextBatch, batchCount, 0, vanToTransfer);
                });
            } else {
                courier.setCurrentTarget(null);
                courier.setNextDeliveryPoint(null);
                isAnimating = false;
                infoLabel.setText("Все посылки доставлены!");
                updatePackageList();
            }
            return;
        }

        TPackage currentPkg = packagesToDeliver[index];
        Point destination = currentPkg.getDestinationPoint();
        DeliveryInfo courierToDestination = courier.moveTo(destination);
        Point nextDest = index + 1 < deliverCount
            ? packagesToDeliver[index + 1].getDestinationPoint()
            : null;
        courier.setCurrentTarget(destination);
        courier.setNextDeliveryPoint(nextDest);

        animateMove(courier, destination, 2000, () -> {
            courier.unloadPackage(currentPkg);
            currentPkg.setStatus(PackageStatus.DELIVERED);
            courier.setCurrentTarget(null);
            courier.setNextDeliveryPoint(null);
            double totalCost = vanToTransfer.getCost() + courierToDestination.getCost();
            double totalTime = vanToTransfer.getTime() + courierToDestination.getTime();
            currentPkg.setDeliveryCost(totalCost);
            currentPkg.setDeliveryTime(totalTime);
            drawScene();
            updatePackageList();
            infoLabel.setText(String.format("Посылка %s доставлена. Время: %.2f ч, Стоимость: %.2f",
                currentPkg.getPackageId(), totalTime, totalCost));
            deliverPackagesSequentially(packagesToDeliver, deliverCount, index + 1, vanToTransfer);
        });
    }
    
    /**
     * Сравнивает две точки с допуском (для раскраски пунктов назначения).
     */
    private boolean pointsEqual(Point a, Point b) {
        if (a == null || b == null) return a == b;
        return a.distanceTo(b) < POINT_EPS;
    }

    /**
     * Обновляет список посылок в ListView.
     */
    private void updatePackageList() {
        packageListItems.clear();
        for (int i = 0; i < packageCount; i++) {
            packageListItems.add(packages[i].toString());
        }
    }

    /**
     * Обновляет метку статуса курьера.
     * Свободен / в пути на точку N / в пути к фургону за новой партией.
     */
    private void updateCourierStatusLabel() {
        if (courierStatusLabel == null || courier == null) return;
        Point currentTarget = courier.getCurrentTarget();
        String msg;
        if (courier.getStatus() == Status.FREE && currentTarget == null) {
            msg = "Статус курьера: свободен";
        } else if (courier.getStatus() == Status.ON_MOVE && currentTarget != null) {
            int num = 0;
            for (int i = 0; i < packageCount; i++) {
                TPackage pkg = packages[i];
                if (pointsEqual(pkg.getDestinationPoint(), currentTarget)) {
                    num = pkg.getPackageNumber();
                    break;
                }
            }
            msg = "Статус курьера: в пути на точку " + (num != 0 ? num : "?");
        } else if (courier.getStatus() == Status.ON_MOVE && currentTarget == null) {
            msg = "Статус курьера: в пути к фургону за новой партией";
        } else {
            msg = "Статус курьера: свободен";
        }
        courierStatusLabel.setText(msg);
    }
    
    /**
     * Анимация плавного перемещения транспортной единицы к целевой точке.
     * 
     * Использует Timeline для создания плавной анимации с эффектом ease-in-out.
     * Анимация обновляется каждые 16 миллисекунд (~60 FPS).
     * 
     * @param unit транспортная единица, которую нужно переместить
     * @param destination целевая точка назначения
     * @param durationMs длительность анимации в миллисекундах
     * @param onComplete callback-функция, вызываемая после завершения анимации
     */
    private void animateMove(TTransportUnit unit, Point destination, int durationMs, Runnable onComplete) {
        // Останавливаем предыдущую анимацию, если она выполняется
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        
        // Устанавливаем статус "в движении"
        unit.setStatus(Status.ON_MOVE);
        
        // Сохраняем начальную позицию
        Point start = new Point(unit.getCurrentLocation().getX(), unit.getCurrentLocation().getY());
        Point end = destination;
        
        // Проверяем расстояние до цели
        double distance = start.distanceTo(end); // Вызывается метод из Point.java, строки 42-48
        // Если расстояние очень мало (< 1 единицы), завершаем сразу
        if (distance < 1.0) {
            unit.setStatus(Status.FREE);
            unit.setCurrentLocation(end);
            drawScene();
            if (onComplete != null) { // Проверяем, что callback передан
                onComplete.run(); // Вызываем callback
            }
            return;
        }
        
        // Массив для хранения времени начала анимации (используем массив для обхода final)
        final long[] startTime = {System.currentTimeMillis()};
        
        // Создаем Timeline с кадром обновления каждые 16 мс (~60 FPS)
        animationTimeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            // Вычисляем прошедшее время
            long elapsed = System.currentTimeMillis() - startTime[0];
            // Вычисляем прогресс анимации от 0.0 до 1.0
            double progress = Math.min(1.0, elapsed / (double) durationMs);
            
            // Применяем функцию плавности (ease-in-out)
            // Это создает более естественное движение: медленный старт, ускорение, замедление
            double easedProgress = progress < 0.5
                ? 2 * progress * progress                    // Первая половина: ускорение
                : -1 + (4 - 2 * progress) * progress;        // Вторая половина: замедление

            // Вычисляем текущую позицию с учетом плавности
            double x = start.getX() + (end.getX() - start.getX()) * easedProgress;
            double y = start.getY() + (end.getY() - start.getY()) * easedProgress;
            
            // Обновляем позицию объекта
            unit.setCurrentLocation(new Point(x, y));
            // Перерисовываем сцену
            drawScene();
            
            // Если анимация завершена (прогресс >= 1.0)
            if (progress >= 1.0) {
                // Останавливаем анимацию
                animationTimeline.stop();
                // Устанавливаем статус "свободен"
                unit.setStatus(Status.FREE);
                // Устанавливаем финальную позицию (на случай погрешностей округления)
                unit.setCurrentLocation(end);
                // Перерисовываем сцену
                drawScene();
                // Вызываем callback-функцию завершения
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }));
        
        // Устанавливаем бесконечный цикл (будет остановлен вручную)
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
        // Запускаем анимацию
        animationTimeline.play();
    }
    
    /**
     * Отрисовка всей сцены на Canvas.
     * 
     * Очищает Canvas и отрисовывает все объекты:
     * - Сортировочный центр
     * - Грузовой фургон
     * - Курьера
     * - Все посылки
     */
    private void drawScene() {
        // Очищаем Canvas
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Отрисовываем все объекты в правильном порядке
        drawSortingCenter();  // Сортировочный центр
        drawDeliveryVan();    // Грузовой фургон
        drawCourier();        // Курьер
        drawPackages();       // Все посылки

        updateCourierStatusLabel();
    }
    
    /**
     * Отрисовка сортировочного центра.
     */
    private void drawSortingCenter() {
        Point loc = sortingCenter.getCurrentLocation();
        
        // Рисуем основание домика (синий прямоугольник 80x50)
        gc.setFill(Color.BLUE);
        gc.fillRect(loc.getX() - 40, loc.getY() - 10, 80, 50);
        
        // Рисуем крышу домика (синий треугольник)
        double[] roofX = {loc.getX() - 40, loc.getX(), loc.getX() + 40};
        double[] roofY = {loc.getY() - 10, loc.getY() - 40, loc.getY() - 10};
        gc.fillPolygon(roofX, roofY, 3);
        
        // Рисуем дверь (темно-синий прямоугольник)
        gc.setFill(Color.DARKBLUE);
        gc.fillRect(loc.getX() - 10, loc.getY() + 10, 20, 30);
        
        // Рисуем белый текст "Центр" в центре домика
        gc.setFill(Color.WHITE);
        gc.fillText("Центр", loc.getX() - 20, loc.getY() + 15);
    }

    /**
     * Отрисовка грузового фургона.
     */
    private void drawDeliveryVan() {
        Point loc = deliveryVan.getCurrentLocation();
        
        // Рисуем кузов фургона (зеленый прямоугольник 60x30)
        gc.setFill(Color.GREEN);
        gc.fillRect(loc.getX() - 30, loc.getY() - 15, 60, 30);
        
        // Рисуем кабину фургона (темно-зеленый прямоугольник 20x20)
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(loc.getX() - 30, loc.getY() - 15, 20, 20);
        
        // Рисуем окно кабины (светло-голубой прямоугольник)
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(loc.getX() - 28, loc.getY() - 13, 16, 12);
        
        // Рисуем левое колесо (черный круг)
        gc.setFill(Color.BLACK);
        gc.fillOval(loc.getX() - 25, loc.getY() + 10, 12, 12);
        // Обод колеса (серый круг)
        gc.setFill(Color.GRAY);
        gc.fillOval(loc.getX() - 23, loc.getY() + 12, 8, 8);
        
        // Рисуем правое колесо (черный круг)
        gc.setFill(Color.BLACK);
        gc.fillOval(loc.getX() + 13, loc.getY() + 10, 12, 12);
        // Обод колеса (серый круг)
        gc.setFill(Color.GRAY);
        gc.fillOval(loc.getX() + 15, loc.getY() + 12, 8, 8);
        
        // Рисуем белый текст "Фургон" в центре кузова
        gc.setFill(Color.WHITE);
        gc.fillText("Фургон", loc.getX() - 20, loc.getY() + 5);
        
        // Рисуем черный текст со статусом под фургоном
        gc.setFill(Color.BLACK);
        gc.fillText(deliveryVan.getStatus().toString(), loc.getX() - 20, loc.getY() + 35);
    }

    /**
     * Отрисовка курьера.
     */
    private void drawCourier() {
        Point loc = courier.getCurrentLocation();

        // Рисуем голову (красный круг)
        gc.setFill(Color.RED);
        gc.fillOval(loc.getX() - 8, loc.getY() - 25, 16, 16);
        
        // Рисуем тело (красный прямоугольник)
        gc.fillRect(loc.getX() - 6, loc.getY() - 9, 12, 18);
        
        // Рисуем левую руку (красный прямоугольник)
        gc.fillRect(loc.getX() - 12, loc.getY() - 5, 6, 12);
        
        // Рисуем правую руку (красный прямоугольник)
        gc.fillRect(loc.getX() + 6, loc.getY() - 5, 6, 12);
        
        // Рисуем левую ногу (красный прямоугольник)
        gc.fillRect(loc.getX() - 6, loc.getY() + 9, 5, 12);
        
        // Рисуем правую ногу (красный прямоугольник)
        gc.fillRect(loc.getX() + 1, loc.getY() + 9, 5, 12);
        
        // Рисуем черный текст "Курьер" над человечком
        gc.setFill(Color.BLACK);
        gc.fillText("Курьер", loc.getX() - 20, loc.getY() - 35);
        
        // Рисуем черный текст со статусом под человечком
        gc.setFill(Color.BLACK);
        gc.fillText(courier.getStatus().toString(), loc.getX() - 20, loc.getY() + 25);
    }

    /**
     * Отрисовка всех посылок.
     * 
     * Отрисовывает посылки в зависимости от их статуса:
     * - IN_CENTER: в сортировочном центре
     * - IN_VAN: в фургоне
     * - WITH_COURIER: у курьера
     * - DELIVERED: доставлена (зеленая точка в пункте назначения)
     * 
     * Также отображает пункты назначения для всех посылок.
     */
    private void drawPackages() {
        // ========== Посылки в сортировочном центре ==========
        int centerPackageOffset = 0;  // Смещение для размещения нескольких посылок
        for (TPackage pkg : sortingCenter.getCargoList()) {
            if (pkg.getStatus() == PackageStatus.IN_CENTER) {
                Point loc = sortingCenter.getCurrentLocation();
                // Размещаем посылки с небольшим смещением по горизонтали
                drawPackage(loc.getX() - 30 + centerPackageOffset * 15, loc.getY() - 10, pkg);
                centerPackageOffset++;
            }
        }
        
        // ========== Посылки в фургоне ==========
        int vanPackageOffset = 0;  // Смещение для размещения нескольких посылок
        for (TPackage pkg : deliveryVan.getCargoList()) {
            if (pkg.getStatus() == PackageStatus.IN_VAN) {
                Point loc = deliveryVan.getCurrentLocation();
                // Размещаем посылки с небольшим смещением по горизонтали
                drawPackage(loc.getX() - 20 + vanPackageOffset * 12, loc.getY(), pkg);
                vanPackageOffset++;
            }
        }
        
        // ========== Посылки у курьера ==========
        for (TPackage pkg : courier.getCargoList()) {
            if (pkg.getStatus() == PackageStatus.WITH_COURIER) {
                Point loc = courier.getCurrentLocation();
                // У курьера обычно одна посылка, размещаем в центре
                drawPackage(loc.getX(), loc.getY(), pkg);
            }
        }
        
        // ========== Отображение пунктов назначения ==========
        Point currentTarget = courier.getCurrentTarget();
        Point nextDeliveryPoint = courier.getNextDeliveryPoint();
        for (int i = 0; i < packageCount; i++) {
            TPackage pkg = packages[i];
            Point dest = pkg.getDestinationPoint();
            int num = pkg.getPackageNumber();
            Color fillColor;
            String label;
            if (pointsEqual(dest, currentTarget)) {
                fillColor = Color.YELLOW;
                label = String.valueOf(num);
            } else if (pointsEqual(dest, nextDeliveryPoint)) {
                fillColor = Color.ORANGE;
                label = String.valueOf(num);
            } else if (pkg.getStatus() == PackageStatus.DELIVERED) {
                fillColor = Color.GREEN;
                label = String.valueOf(num);
            } else {
                fillColor = Color.RED;
                label = String.valueOf(num);
            }
            gc.setFill(fillColor);
            gc.fillOval(dest.getX() - 5, dest.getY() - 5, 10, 10);
            gc.setFill(Color.BLACK);
            gc.fillText(label, dest.getX() + 10, dest.getY() + 3);
        }
    }
    
    /**
     * Отрисовка одной посылки.
     * 
     * @param x координата X центра посылки
     * @param y координата Y центра посылки
     * @param pkg посылка для отрисовки
     */
    private void drawPackage(double x, double y, TPackage pkg) {
        // Рисуем желтый квадрат (10x10)
        gc.setFill(Color.YELLOW);
        gc.fillRect(x - 5, y - 5, 10, 10);
        
        // Рисуем черный текст с ID посылки справа от квадрата
        gc.setFill(Color.BLACK);
        gc.fillText(pkg.getPackageId(), x + 8, y);
    }
}
