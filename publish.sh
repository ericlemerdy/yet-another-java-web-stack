#!/bin/sh

pandoc --from markdown --to html5 --output index.html --self-contained README.md
sed '11i   <link rel="stylesheet" href="index.css" />' index.html > index.html.1
mv index.html.1 index.html
