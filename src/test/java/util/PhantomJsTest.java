package util;

import org.fluentlenium.adapter.FluentTest;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

public abstract class PhantomJsTest extends FluentTest {
    private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(1024, 768);

    public WebDriver getDefaultDriver() {
        WebDriver driver = new PhantomJSDriver();
        driver.manage().window().setSize(DEFAULT_WINDOW_SIZE);
        return driver;
    }

}
