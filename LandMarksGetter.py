import os
import dlib
import cv2
import matplotlib.pyplot as plt

folder_path = './images/1-min'
predictor = dlib.shape_predictor("shape_predictor_68_face_landmarks.dat")
images_with_landmarks = []

#iterate over all the images inside a folder
for filename in os.listdir(folder_path):
    if filename.endswith(('.jpg', '.png', '.jpeg')):
        
        #load current image iteration
        image_path = os.path.join(folder_path, filename)
        image = cv2.imread(image_path)
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        #face detection
        detector = dlib.get_frontal_face_detector()
        faces = detector(gray)

        for face in faces:
            x, y, w, h = (face.left(), face.top(), face.right() - face.left(), face.bottom() - face.top())
            cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)

            #get landmarks of image
            landmarks = predictor(gray, face)

            #draw those landmarks
            for n in range(68):
                x_landmark, y_landmark = landmarks.part(n).x, landmarks.part(n).y
                cv2.circle(image, (x_landmark, y_landmark), 2, (0, 0, 255), -1)

        #append the image with the facerecognition and landmarks to the array
        images_with_landmarks.append(image)

# display all images inside array
for i, result_image in enumerate(images_with_landmarks):
    plt.subplot(1, len(images_with_landmarks), i + 1)
    plt.imshow(cv2.cvtColor(result_image, cv2.COLOR_BGR2RGB))
    plt.axis('off')
    plt.title(f"Image {i + 1}")

plt.show()
