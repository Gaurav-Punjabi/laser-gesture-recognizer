# LSR (Laser Gesture Recognizer)
Using a remote with visual aids makes presenting the content more easier. But as these remotes are expensive not everyone can afford it.
Here comes LSR (Laser Gesture Recognizer) that lets you control you presentation using any laser pointer. All you need to do is launch LSR, face your laptop towards the projector screen. That's it! Now you can control your presentation using a cheap laser pointer.

### Table of Content
 * Installation
 * How it works

#### How it works
This application is developed in Java with OpenCV3.
 * **Algorithm**
  1. Capture the video feed from the laptop's webcam.
  2. Apply filters to remove noise from the feed.
  3. Convert the image from RGB to HSV format.
  4. Isolate the image by thresholding and picking the color range of laser pointer.
  5. Extract the largest contour from the image.
  6. Recognize the direction of the laser pointer and perform the specified operation
