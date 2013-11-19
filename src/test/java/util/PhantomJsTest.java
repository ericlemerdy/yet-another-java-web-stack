package util;

import org.fluentlenium.adapter.FluentTest;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static com.google.common.collect.ImmutableMap.of;
import static org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY;

public abstract class PhantomJsTest extends FluentTest {
    private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(1024, 768);

    public WebDriver getDefaultDriver() {
        File phantomJsExe = new PhantomJsDownloader().downloadAndExtract();
        DesiredCapabilities capabilities = new DesiredCapabilities(of(PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsExe.getAbsolutePath()));
        WebDriver driver = new PhantomJSDriver(capabilities);
        driver.manage().window().setSize(DEFAULT_WINDOW_SIZE);
        return driver;
    }

}
