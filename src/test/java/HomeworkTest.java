import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HomeworkTest {

    private static int extractPrice(String priceText) {
        String digits = priceText.replaceAll("[^0-9]", "");
        return Integer.parseInt(digits);
    }

    @Test
    void testAddToCartAndPlaceOrder() {
        // 1. Открыть сайт и авторизоваться.
        open("https://demoblaze.com");

        $("#login2").click();
        $("#logInModal").shouldBe(visible);
        $("#loginusername").setValue("test");
        $("#loginpassword").setValue("test");
        $("#logInModal button.btn-primary").click();
        $("#nameofuser").shouldHave(text("Welcome test"));

        // 2. Добавить в корзину три товара и сохранить их цены со страницы товара.
        String[] products = {"Nokia lumia 1520", "Iphone 6 32gb", "Sony vaio i7"};
        int expectedTotal = 0;

        for (String product : products) {
            $(byText(product)).click();

            String priceText = $("#tbodyid .price-container").shouldBe(visible).text();
            int productPrice = extractPrice(priceText);
            expectedTotal += productPrice;

            $("a.btn.btn-success").click();
            // DemoBlaze показывает обычный JavaScript alert после добавления товара.
            switchTo().alert().accept();

            $("a.nav-link[href='index.html']").click();
            $(byText(product)).shouldBe(visible);
        }

        // 3. Открыть корзину и проверить наличие выбранных товаров.
        $("#cartur").click();
        $("#tbodyid tr").shouldHave(size(products.length));

        for (String product : products) {
            $("#tbodyid").shouldHave(text(product));
        }

        // 4. Сверить сумму цен в корзине с суммой, рассчитанной на страницах товаров.
        int cartTotal = extractPrice($("#totalp").shouldBe(visible).text());
        assertEquals(expectedTotal, cartTotal, "Итоговая сумма корзины рассчитана неверно");

        // 5. Оформить заказ.
        $("button[data-target='#orderModal']").click();
        $("#orderModal").shouldBe(visible);

        $("#name").setValue("Test User");
        $("#country").setValue("Test Country");
        $("#city").setValue("Test City");
        $("#card").setValue("1234567890123456");
        $("#month").setValue("12");
        $("#year").setValue("2026");

        $("#orderModal button.btn-primary").click();

        // 6. Проверить сумму и получить ID из окна подтверждения заказа.
        $(".sweet-alert").shouldBe(visible);
        String confirmationText = $(".sweet-alert").getText();

        Matcher amountMatcher = Pattern.compile("Amount:\\s*(\\d+)").matcher(confirmationText);
        if (!amountMatcher.find()) {
            throw new AssertionError("Не удалось найти сумму заказа в подтверждении: " + confirmationText);
        }
        int orderAmount = Integer.parseInt(amountMatcher.group(1));
        assertEquals(expectedTotal, orderAmount, "Сумма заказа не совпадает с суммой корзины");

        String orderId = confirmationText.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("Id:"))
                .map(line -> line.substring("Id:".length()).trim())
                .findFirst()
                .orElseThrow(() -> new AssertionError("ID заказа не найден: " + confirmationText));

        System.out.println("ID заказа: " + orderId);

        // 7. Закрыть подтверждение и убедиться, что оно исчезло.
        $(".sweet-alert button.confirm").click();
        $(".sweet-alert").should(disappear);
    }
}
