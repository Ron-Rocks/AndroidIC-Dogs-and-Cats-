# AndroidIC-Dogs-and-Cats
An Image classifier which classifies image as dog or cat

# Working

This app uses the camera to get the picture of the animal and then 
displays either Cat or Dog and the probability

This app uses Firebase as a backend service to provide the ".tflite" model

then it gets the input image rescales it to (300,300) and runs the interprtor

# Files

AndroidCustomModel.ipynb is the colab notebook which is responsible for
>Getting the data

>Training the model

>Saving the model

>Conversion to ".tflite" and saving


model.08-0.58.hdf5  is the trained model, saved using ModelCheckpoint callback

model.tflite  is the converted model used in the android app
