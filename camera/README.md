# CZ3002 camera

Performs face detection and gender classification on a "surveillance camera".
Use a computer's webcam as the surveillance camera.

## requirements

- [anaconda](https://docs.anaconda.com/anaconda/install/)
  - [commands](https://docs.conda.io/projects/conda/en/4.6.0/_downloads/52a95608c49671267e40c689e0bc00ca/conda-cheatsheet.pdf)

## how to set-up

1. create the virtual environment: `conda create --name cz3002_venv --file cz3002_venv.txt`
2. activate virtual environment: `conda activate cz3002_venv`
3. install pip dependencies: `pip install -r requirements.txt`
4. deactivate virtual environment when done: `conda deactivate`
5. get your Firebase Service Account key and save it in the directory `configurations` as `key.json`
6. create `camera_config.json` in the directory `configurations`

example `camera_config.json`:

```json
{
    "camera_information": {
        "toilet_gender": 0,
        "toilet_location": "N3-01-01",
        "notification_interval": 5
    },
    "face_detection": {
        "lffd_symbol_file_path": "face_detection/model/symbol_10_560_25L_8scales_v1_deploy.json",
        "lffd_model_file_path": "face_detection/model/train_10_560_25L_8scales_v1_iter_1400000.params"
    },
    "gender_classification": {
        "ssrnet_prefix": "gender_classification/ssr2_imdb_gender/model",
        "ssrnet_epoch_num": 0
    },
    "violation_handling": {
        "databaseURL": "https://cz3002-xxxxx-default-rtdb.firebaseio.com/",
        "storageBucket": "cz3002-xxxxx.appspot.com"
    }
}
```

description of each property in `camera_config.json`:

```json
{
    "camera_information": {
        "toilet_gender": "the gender of the toilet being monitored by the camera (0 for female, 1 for male)",
        "toilet_location": "location of the toilet being monitored by the camera",
        "notification_interval": "time interval (in seconds) between each notification"
    },
    "face_detection": {
        "lffd_symbol_file_path": "path to symbol file of face detection model",
        "lffd_model_file_path": "path to model params of face detection model"
    },
    "gender_classification": {
        "ssrnet_prefix": "prefix of the path for gender classification model",
        "ssrnet_epoch_num": "epoch at which gender classification model was saved"
    },
    "violation_handling": {
        "databaseURL": "database URL copied from Firebase console",
        "storageBucket": "storage bucket copied from Firebase console, without starting 'gs://' and ending '/'"
    }
}
```

## how to run demo

1. activate virtual environment: `conda activate cz3002_venv`
2. run the demo: `python main.py`
    - view parameters: `python main.py --help`
    - press 'q' key (or 'ctrl-c') to stop demo
3. deactivate virtual environment when done: `conda deactivate`

## TODO

- write more unit tests
- refactor code more
- update documentation
