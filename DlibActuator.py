from typing import List, Tuple
import base64
import cv2
import dlib
from FaceModel import FaceModel

class DlibActuator:
    def __init__(self):
        self.predictor = dlib.shape_predictor(
            "shape_predictor_68_face_landmarks.dat")

    def process_images(self, image_paths: List[str]) -> List[FaceModel]:
        result_faces = []

        for image_path in image_paths:
            image = cv2.imread(image_path)
            gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

            detector = dlib.get_frontal_face_detector()
            faces = detector(gray)
            hasErred = False
            for face in faces:
                if face.left() is not None:
                    x, y, w, h = (
                        face.left(), face.top(), face.right() - face.left(), face.bottom() - face.top())
                else:
                    hasErred = True
                    x, y, w, h = (-1, -1, -1, -1)

                landmarks = self.predictor(gray, face)
                landmarks_coordinates = []
                for n in range(68):
                    if (landmarks.part(n).x) != -1:
                        landmark_coordinates = (
                            landmarks.part(n).x, landmarks.part(n).y)
                    else:
                        hasErred = True
                        landmark_coordinates = (-1, -1)
                    landmarks_coordinates.append(landmark_coordinates)

                face_coordinates = (x, y, w, h)

                cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)

                for x_landmark, y_landmark in landmarks_coordinates:
                    cv2.circle(image, (x_landmark, y_landmark),
                               2, (0, 0, 255), -1)

                if not hasErred:
                    _, buffer = cv2.imencode('.png', image)
                    image_base64 = base64.b64encode(buffer).decode('utf-8')
                else:
                    image_base64 = image_path

                result_faces.append(FaceModel(
                    originalImage=image_path,
                    imageWithLandmarks=image_base64,
                    landmarksCoordinates=landmarks_coordinates,
                    faceCoordinates=face_coordinates
                ))

        return result_faces
