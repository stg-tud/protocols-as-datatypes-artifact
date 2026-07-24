#! /usr/bin/env fish
rm -rf .git .bsp .metals .scala-build .github
rm -f .gitignore .gitattributes .gitmodules
rm bundle-up.fish
zip -r artifact.zip .
