#!/usr/bin/env fish
set fish_trace 1

cd bismuth
sbt proBench/Universal/packageBin

cp Modules/Examples/Protocol\ Benchmarks/target/universal/probench.zip ../probench.zip
cd ..
rm -rf ./bin

unzip probench.zip -d ./bin
rm probench.zip
