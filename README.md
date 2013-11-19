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
Résultat: java.lang.AssertionError: <'Erreur de chargement de la page'> should contain the String:<'Anagram Kata'>

C'est normal puisque aucun serveur n'est démarré sur localhost:8080. Pour faire passer le test, il faut donc déployer
un serveur web et servir une page dont le titre est 'Anagram kata'.