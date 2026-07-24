#! /usr/bin/env fish
rm -rf .git .bsp .metals .scala-build .github
rm -f .gitignore .gitattributes .gitmodules
rm bundle-up.fish
zip -r artifact.zip .

cp README.md README_PDF.md
sed -i '1c ---' README_PDF.md

pandoc -V margin-left=2cm -V margin-right=2cm -V margin-top=2cm -V margin-botton=2cm -V papersize=a4 -o README.pdf --standalone --metadata title="PRDTs: Composable Design and Verification of Consensus Protocols using Replicated Data Types (Artifact)" --highlight-style=kate --toc README_PDF.md
