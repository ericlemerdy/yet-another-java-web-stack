package ui;

import org.fluentlenium.adapter.FluentTest;
import org.junit.Rule;
import org.junit.Test;
import util.JettyServerRule;

import static org.fest.assertions.Assertions.assertThat;

public class AnagramKataPageTest extends FluentTest {

    @Rule
    public JettyServerRule server = new JettyServerRule();

    @Test
    public void title_of_site_should_contain_the_kata_name() {
        goTo("http://localhost:8080");
        assertThat(title()).contains("Anagram Kata");
    }
}