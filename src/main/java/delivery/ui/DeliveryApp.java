package delivery.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene; // Импорт класса для создания сцен
import javafx.stage.Stage; // Импорт класса для главного окна

import java.io.IOException; // Импорт класса для обработки ошибок ввода/вывода

/**
 * Главный класс приложения для визуализации доставки посылок.
 * 
 * цепочка доставки:
 * Сортировочный центр → Грузовой фургон → Курьер → Пункт назначения
 */
public class DeliveryApp extends Application {
    
    /** Ширина главного окна приложения */
    private static final double SCENE_WIDTH = 800;
    
    /** Высота главного окна приложения */
    private static final double SCENE_HEIGHT = 600;
    
    /**
     * Точка входа в приложение.
     * 
     * Метод start вызывается автоматически JavaFX после инициализации приложения.
     * Загружает FXML файл с описанием интерфейса и создает главное окно.
     * 
     * @param primaryStage главное окно приложения, предоставляемое JavaFX
     * @throws IOException если не удается загрузить FXML файл
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Устанавливаем заголовок окна
        primaryStage.setTitle("Симуляция доставки посылок");
        
        // Создаем загрузчик FXML файлов
        // Указываем путь к FXML файлу относительно пакета delivery
        FXMLLoader fxmlLoader = new FXMLLoader(
            DeliveryApp.class.getResource("/delivery/delivery_ui.fxml")
        );
        
        // Загружаем интерфейс из FXML файла
        // Это создаст все элементы интерфейса и свяжет их с контроллером
        Scene scene = new Scene(fxmlLoader.load(), SCENE_WIDTH, SCENE_HEIGHT);
        
        // Устанавливаем сцену в главное окно
        primaryStage.setScene(scene);
        
        // Отображаем окно
        primaryStage.show();
    }
    
    /**
     * Главный метод приложения.
     * 
     * Запускает JavaFX приложение. Метод launch() инициализирует
     * JavaFX runtime и вызывает метод start()
     * 
     * @param args аргументы командной строки (не используются)
     *        Это обязательная сигнатура метода main() в Java.
     *        JVM всегда вызывает main с этим параметром, даже если аргументы не передаются.
     *        Если его убрать, метод не будет точкой входа.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
