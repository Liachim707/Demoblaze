import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class HomeworkTest {

    @Test
    void testAddToCartAndPlaceOrder() {
        // 1. Перейти на страницу: https://demoblaze.com
        open("https://demoblaze.com");

        // Логин: test; пароль: test
        $("#loginUsername").setValue("test");
        $("#loginPassword").setValue("test");
        $("#loginModal .btn-primary").click();

        // 2. Добавить в корзину три товара: Nokia lumia 1520, Iphone 6 32gb и Sony vaio i7
        String[] products = {"Nokia lumia 1520", "Iphone 6 32gb", "Sony vaio i7"};
        double[] prices = new double[3];

        for (int i = 0; i < products.length; i++) {
            // Кликнуть на карточку товара
            $(byText(products[i])).click();

            // Кликнуть по кнопке Add to Cart
            $(".btn-success").click();

            // Нажать OK в диалоговом окне
            Selenide.confirm();

            // Вернуться на страницу Home
            $(byText("Home")).click();

            // Сохранить цену товара для сверки
            prices[i] = Double.parseDouble($(".price").text().replace("$", ""));
        }

        // 3. Перейти в корзину Cart
        $("#cartur").click();

        // 6. Проверить, что итоговая цена Total является суммой цен трех выбранных товаров
        double expectedTotal = prices[0] + prices[1] + prices[2];
        String totalText = $(".total").text();
        double actualTotal = Double.parseDouble(totalText.replace("$", ""));

        assert Math.abs(actualTotal - expectedTotal) < 0.01 :
                "Total price mismatch. Expected: " + expectedTotal + ", Actual: " + actualTotal;

        // 7. Сделать заказ по клику на Place Order
        $(".btn-success").click();

        // 8. Заполнить все поля формы заказа
        $("#name").setValue("Test User");
        $("#country").setValue("TestCountry");
        $("#city").setValue("TestCity");
        $("#card").setValue("1234567890123456");
        $("#month").setValue("12");
        $("#year").setValue("2025");

        // 9. Нажать Purchase
        $(".btn-primary").click();

        // 10. Проверить, что сумма Amount верна
        String amountText = $(".amount").text();
        double amount = Double.parseDouble(amountText.replace("$", ""));
        assert Math.abs(amount - expectedTotal) < 0.01 :
                "Amount mismatch. Expected: " + expectedTotal + ", Actual: " + amount;

        // 11. Получить текст с Id заказа и вывести в консоль
        String orderIdText = $(".order-label").text();
        System.out.println("Order ID: " + orderIdText);

        // 12. Кликнуть OK и проверить, что окно пропало
        Selenide.confirm();
        $(".purchase-container").should(disappear);
    }
}
