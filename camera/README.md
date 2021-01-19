# CZ3002 camera

Performs face detection and gender recognition on a "surveillance camera".
Use a computer's webcam as the surveillance camera.

## requirements

- a Nvidia GPU that supports CUDA 10.2
- [anaconda](https://docs.anaconda.com/anaconda/install/)
  - [commands](https://docs.conda.io/projects/conda/en/4.6.0/_downloads/52a95608c49671267e40c689e0bc00ca/conda-cheatsheet.pdf)

## how to set-up

1. create the virtual environment: `conda create --name cz3002_venv --file cz3002_venv.txt`

## how to run demo

- activate virtual environment: `conda activate cz3002_venv`
- run the demo: `python demo_webcam.py`
  - press 'q' key to stop demo
- deactivate virtual environment when done: `conda deactivate`

## TODO

send image to server
