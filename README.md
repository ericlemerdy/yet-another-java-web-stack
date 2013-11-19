Yet Another Java Web Stack
==========================

## Motivations

Qui n'a pas eu besoin d'un projet vide pour démarrer un nouveau projet web ? Dans cet article, je développe un site qui
permet de voir le résultat d'un kata bien connu, le
[kata anagram|http://codekata.pragprog.com/2007/01/kata_six_anagra.html]

## Architecture cible
* Partie cliente "statique"
** AngularJS
** Yeoman (scafolding)
** Bower (dépendences)

* Partie serveur "dynamique"
** Java Jersey

## Testons un site web
On commence par écrire un test qui se connecte à un serveur web et vérifie qu'on affiche le titre de la page: anagram
kata. Pour ça, j'utilise le framework [FluentLenium|http://www.fluentlenium.org/] qui repose sur
[Selenium|http://docs.seleniumhq.org/].

Step 1
Tests: mvn clean install
Résultat: Failed tests:   title_of_site_should_contain_the_kata_name(ui.AnagramKataPageTest): <'Erreur de chargement de la page'> should contain the String:<'Anagram Kata'>

C'est normal puisque aucun serveur n'est démarré sur localhost:8080. Pour faire passer le test, il faut donc déployer
un serveur web et servir une page dont le titre est 'Anagram kata'.
Une JUnit Rule démarre le serveur Jetty embarqué pour servir du contenu statique :
    package util;

    import org.eclipse.jetty.server.Server;
    import org.eclipse.jetty.server.handler.HandlerList;
    import org.eclipse.jetty.servlet.DefaultServlet;
    import org.eclipse.jetty.webapp.WebAppContext;
    import org.junit.rules.ExternalResource;

    public class JettyServerRule extends ExternalResource {

        private Server server;

        @Override
        protected void before() throws Throwable {
            server = new Server(8080);
            server.setHandler(new WebAppContext(server, "src/main/webapp/", "/"));
            server.start();
        }

        @Override
        protected void after() {
            try {
                server.stop();
            } catch (Exception e) {
                System.err.println("Unable to stop test server");
            }
        }
    }
On ajoute la rule au test :
    @Rule
    public JettyServerRule server = new JettyServerRule();
Le message d'erreur change :
Step-2
    java.lang.AssertionError: <'Directory: /'> should contain the String:<'Anagram Kata'>
Le serveur Jetty embarqué se met donc à servir le contenu statique de '/src/main/webapp'.
Il suffit maintenant d'ajouter un bon fichier html qui fait passer le test :
    <html>
    <head>
        <title>Anagram Kata</title>
    </head>
    </html>

Vous avez peut-être remarqué que le démarrage de Firefox par Selenuim rend le test assez long à éxécuter. Pour
accélérer le passage du test, nous allons utiliser le navigateur sans interface PhantomJS. C'est ghostdriver qui se
charge de déclarer PhantomJS comme WebDriver à Selenium.
    <dependency>
        <groupId>com.github.detro.ghostdriver</groupId>
        <artifactId>phantomjsdriver</artifactId>
        <version>1.0.3</version>
    </dependency>
step-4
En repassant les tests, on s'apperçoit que quelque-chose manque:
    java.lang.IllegalStateException: The path to the driver executable must be set by the phantomjs.binary.path capability/system property/PATH variable; for more information, see https://github.com/ariya/phantomjs/wiki. The latest version can be downloaded from http://phantomjs.org/download.html
        at com.google.common.base.Preconditions.checkState(Preconditions.java:176)
        at org.openqa.selenium.phantomjs.PhantomJSDriverService.findPhantomJS(PhantomJSDriverService.java:237)
        at org.openqa.selenium.phantomjs.PhantomJSDriverService.createDefaultService(PhantomJSDriverService.java:182)
        at org.openqa.selenium.phantomjs.PhantomJSDriver.<init>(PhantomJSDriver.java:96)
        at org.openqa.selenium.phantomjs.PhantomJSDriver.<init>(PhantomJSDriver.java:86)
        at util.PhantomJsTest.getDefaultDriver(PhantomJsTest.java:19)
        at org.fluentlenium.adapter.FluentTest.initFluentFromDefaultDriver(FluentTest.java:123)
Il manque l'éxécutable de PhantomJS. Il faut le télécharger !
Autant s'outiller tout de suite, utilisons du code provenant d'un [gist|https://gist.github.com/dgageot/4957186] pour
ça et intégrons-le dans la classe PhantomJsTest:
    public WebDriver getDefaultDriver() {
        File phantomJsExe = new PhantomJsDownloader().downloadAndExtract();
        DesiredCapabilities capabilities = new DesiredCapabilities(of(PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsExe.getAbsolutePath()));
        WebDriver driver = new PhantomJSDriver(capabilities);
        driver.manage().window().setSize(DEFAULT_WINDOW_SIZE);
        return driver;
    }
step-5
Ça passe et on ne voit plus de firefox qui démarre l'interface lors du passage des tests !
En cas d'erreurs, on active les captures d'écrans pour visualiser l'erreur.
    public PhantomJsTest() {
        setSnapshotMode(Mode.TAKE_SNAPSHOT_ON_FAIL);
        setSnapshotPath(new File("target", "snapshots").getAbsolutePath());
    }
step-6
Un autre avantage est de pouvoir poser un point d'arrêt dans les tests et faire le scénario soit-même dans son
navigateur pour dissocier d'éventuels problèmes dans une classe de test et de vrais problèmes de l'application.