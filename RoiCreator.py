import numpy as np
import numpy as np
from scipy.spatial import Delaunay
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import plotly.graph_objs as go
predictor_path = "./drive/MyDrive/shapePredictorOriginal.dat"
import matplotlib.pyplot as plt
from matplotlib.path import Path
from matplotlib.patches import Polygon

def create_roi(image, landmarks, start_idx, end_idx, connections):
    landmarks = landmarks[0]
    roi_mask = []
    for connection in connections:
        roi_mask.append((landmarks[connection][0], landmarks[connection][1]))
    return roi_mask

def create_eyebrow_roi(image, landmarks, start_idx, end_idx, connections):
    landmarks = landmarks[0]
    roi_mask = []
    #multipliers = [1.0, 1.10, 1.17, 1.14, 1.0]
    multipliers = [1.0, 4, 4, 4, 4]
    for i, connection in enumerate(connections):
        roi_mask.append((landmarks[connection][0], landmarks[connection][1]))
        if i != 0 and i != 4:
            roi_mask.append((landmarks[connection][0], landmarks[connection][1] * multipliers[i]))
    return roi_mask

def create_left_eye_roi(image, landmarks):
    left_eye_connections = [ 36,37, 38, 39, 40, 41]
    return create_roi(image, landmarks, 36, 42, left_eye_connections)

def create_right_eye_roi(image, landmarks):
    right_eye_connections = [
        42, 43, 44, 45, 46, 47
    ]
    return create_roi(image, landmarks, 42, 48, right_eye_connections)

def create_nose_roi(image, landmarks):
    # nose_connections = [
    #     (27, 28), (28, 29), (29, 30), (30, 31), (31, 32), (33, 34), (34, 35), (35, 27)
    # ]
    nose_connections = [
         27, 31, 32, 33, 34, 35
    ]
    return create_roi(image, landmarks, 27, 36, nose_connections)

def create_mouth_roi(image, landmarks):
    mouth_connections = [
        48, 49, 50, 51 , 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67
    ]
    return create_roi(image, landmarks, 48, 68, mouth_connections)

def create_face_roi(image, landmarks):
    #selected_landmarks = landmarks[:27]
    connections = [
        0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,68,69
    ]
    return create_roi(image, landmarks, 0, 69, connections)

def create_left_eyebrow_roi(image, landmarks):
    selected_landmarks = landmarks[17:22]
    connections = [
        17,18,19,20,21
    ]
    return create_roi(image, landmarks, 17, 21, connections)

def create_right_eyebrow_roi(image, landmarks):
    selected_landmarks = landmarks[21:27]
    connections = [
        22,23,24,25,26
    ]
    return create_roi(image, landmarks, 22, 26, connections)

def get_featureless_roi(left_eye_roi, right_eye_roi, mouth_roi, nose_roi, face_roi, left_eyebrow_roi, right_eyebrow_roi):
      combined_roi = np.concatenate((left_eye_roi, right_eye_roi, nose_roi, mouth_roi, left_eyebrow_roi, right_eyebrow_roi))
      featureless_roi = np.array([point for point in face_roi if not any(np.array_equal(point, p) for p in combined_roi)])
      return featureless_roi

def unique_coordinates_from_lines(roi_lines):
    unique_coordinates = set()
    for line in roi_lines:
        for point in line:
            unique_coordinates.add(point)
    return list(unique_coordinates)

# def is_inside_roi(x, y, roi_lines):
#     roi_mask = np.zeros((image.shape[0], image.shape[1]), dtype=np.uint8)
#     roi_lines = unique_coordinates_from_lines(roi_lines)
#     pts = np.array([coord for coord in roi_lines], np.int32)
#     cv2.fillPoly(roi_mask, [pts], 255)
#     return roi_mask[y, x] == 255

def is_inside_roi(x, y, roi_lines):
    path = Path(roi_lines)
    return path.contains_point((x, y))


def check_coordinate_direction(image, x, y):
    image_width = image.shape[1]
    image_center_x = image_width // 2
    if x < image_center_x:
        location = 'left'
    elif x > image_center_x:
        location = 'right'
    else:
        location = 'middle'

    return location