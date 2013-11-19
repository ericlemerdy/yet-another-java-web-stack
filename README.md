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