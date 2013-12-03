Yet Another Java Web Stack
==========================

## Motivations

Qui n'a pas eu besoin d'un projet vide pour démarrer un nouveau projet web ? Dans cet article, je développe un site qui
permet de voir le résultat d'un kata bien connu, le
[kata anagram](http://codekata.pragprog.com/2007/01/kata_six_anagra.html)

## Architecture cible

* Partie cliente "statique"
   * AngularJS
   * Yeoman (scafolding)
   * Bower (dépendences)

* Partie serveur "dynamique"
   * Java Jersey

## Testons un site web

On commence par écrire un test qui se connecte à un serveur web et vérifie qu'on affiche le titre de la page: `anagram
kata`. Pour ça, j'utilise le framework [FluentLenium](http://www.fluentlenium.org/) qui repose sur
[Selenium](http://docs.seleniumhq.org/)

### [`git checkout step-1-fail-ui-test`](https://github.com/ericlemerdy/yet-another-java-web-stack/tree/step-1-fail-ui-test)

Tests: `mvn clean install`

Résultat: `Failed tests:   title_of_site_should_contain_the_kata_name(ui.AnagramKataPageTest): <'Erreur de chargement de la page'> should contain the String:<'Anagram Kata'>`

C'est normal puisque aucun serveur n'est démarré sur `localhost:8080`. Pour faire passer le test, il faut donc déployer
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

### [`git checkout step-2-start-test-web-server`](https://github.com/ericlemerdy/yet-another-java-web-stack/tree/step-2-start-test-web-server)

Le message d'erreur change :

`java.lang.AssertionError: <'Directory: /'> should contain the String:<'Anagram Kata'>`

Le serveur Jetty embarqué se met donc à servir le contenu statique de `/src/main/webapp`.

### [`git checkout step-3-test-pass`](https://github.com/ericlemerdy/yet-another-java-web-stack/tree/step-3-test-pass)

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

### [`git checkout step-4-using-phantomjs`](https://github.com/ericlemerdy/yet-another-java-web-stack/tree/step-4-using-phantomjs)

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

Autant s'outiller tout de suite, utilisons du code provenant d'un [gist](https://gist.github.com/dgageot/4957186) pour
ça et intégrons-le dans la classe PhantomJsTest:

    public WebDriver getDefaultDriver() {
        File phantomJsExe = new PhantomJsDownloader().downloadAndExtract();
        DesiredCapabilities capabilities = new DesiredCapabilities(of(PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsExe.getAbsolutePath()));
        WebDriver driver = new PhantomJSDriver(capabilities);
        driver.manage().window().setSize(DEFAULT_WINDOW_SIZE);
        return driver;
    }

### [`git checkout step-5-download-phantom-js`](https://github.com/ericlemerdy/yet-another-java-web-stack/tree/step-5-download-phantom-js)

Ça passe et on ne voit plus de firefox qui démarre l'interface lors du passage des tests !

En cas d'erreurs, on active les captures d'écrans pour visualiser l'erreur.

    public PhantomJsTest() {
        setSnapshotMode(Mode.TAKE_SNAPSHOT_ON_FAIL);
        setSnapshotPath(new File("target", "snapshots").getAbsolutePath());
    }

### [`git checkout step-6-snapshot-on-error`](https://github.com/ericlemerdy/yet-another-java-web-stack/tree/step-6-snapshot-on-error)

Un autre avantage est de pouvoir poser un point d'arrêt dans les tests et faire le scénario soit-même dans son
navigateur pour dissocier d'éventuels problèmes dans une classe de test et de vrais problèmes de l'application.

Ça y est, on a un site qui fonctionne. La prochaine chose à faire est naturellement de mettre le site en production
pour que les utilisateurs puissent bénéficier de ses fonctionnalités.

## Mise en production

### Release

Après avoir déplacé le projet java dans son propre répertoire, on release la version :

    cd java/
    mvn versions:set -DnewVersion=0.0.1
    mvn versions:commit
    mvn clean install

La version est installée dans `~/.m2/repository/name/lemerdy/eric/yet-another-java-web-stack/0.0.1/yet-another-java-web-stack-0.0.1.war`. On passe en version suivante:

    mvn versions:set -DnewVersion=0.0.2-SNAPSHOT
    mvn versions:commit

### [`git checkout step-7-release-0.0.1`](https://github.com/ericlemerdy/yet-another-java-web-stack/tree/step-7-release-0.0.1)


### Provisionning

Notre prochain but est de disposer d'une plate-forme pour déployer notre application web.

On va utiliser [Vagrant](https://www.vagrantup.com) pour fournir la machine virtuelle et [Puppet](https://puppetlabs.com) pour la configurer.

Créez un répertoire `platform` et initialisez une machine virtuelle Vagrant:

    mkdir platform
    cd platform/
    vagrant init

Cette machine virtuelle utilisera la box Vagrant de base. On configure aussi une IP statique pour plus de simplicité.

`/platform/Vagrantfile`:

    VAGRANTFILE_API_VERSION = "2"

    Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
      config.vm.box = "base"
      config.vm.network :private_network, ip: "10.10.10.2"
    end

Vous pouvez démarrer la machine et vous y connecter avec les commandes suivantes :

    vagrant up
    vagrant ssh

Pour la stopper, il suffit de taper: `vagrant halt`

### [`git checkout step-8-vagrant-base`](https://github.com/ericlemerdy/yet-another-java-web-stack/tree/step-8-vagrant-base)

Maintenant qu'on a une "machine", il faut installer le "middleware"... Enfin, il faut installer Tomcat quoi. À l'ancienne, il suffirait de faire:

    sudo apt-get install tomcat7

Mais on va aussi automatiser cette partie pour pouvoir partir de la feuille blanche dès qu'on aura envie de tout nettoyer. C'est pourquoi on va utiliser Puppet. Grâce à l'intégration maligne de Vagrant et Puppet, on va juste fournir des fichiers de configuration de Puppet et la tâche de provisionning de Vagrant se chargera de lancer l'agent Puppet pour appliquer la configuration. Voici la structure standard à créer:

    platform/
      Vagrantfile    # Le fichier Vagrant créé précedemment
      manifests/     # Le répertoire par défaut contenant les fichiers puppet.
        default.pp   # Le fichier puppet par défaut qui contient la configuration à appliquer.

Pour déclarer qu'on a besoin de Tomcat7, voici le contenu de `/platform/manifests/default.pp`:

    exec { "apt-get update":
      command => "/usr/bin/apt-get update",
    }

    package { "tomcat7":                      # Le package tomcat7 doit être
      ensure  => "installed",                 # installé.
      require => exec [ "apt-get update" ],   # On doit mettre à jour la
    }                                         # définitions des packets avant.

Pour appliquer cette configuration, il faut déclarer à Vagrant qu'on souhaite provisionner la machine avec Puppet:

`/platform/Vagrantfile`:

    (...)
    Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
      (...)
      config.vm.provision "puppet" do |puppet|
        puppet.options = "--verbose --debug"
      end
    end

Notez qu'on se permet de passer Puppet en Verbose pour bien comprendre ce qu'il va faire sur la plate-forme.

Pour appliquer cette configuration, il faut taper la commande `vagrant provision`. Si tout se passe correctement, vous pouvez accéder à: [http://10.10.10.2:8080/]. Ça doit montrer la page "It works" par défaut de Tomcat.

### Déploiement

