#! /usr/bin/env fish
cd analysis
pipenv run jupyter notebook --ip=0.0.0.0 --port=8888 --no-browser --allow-root
