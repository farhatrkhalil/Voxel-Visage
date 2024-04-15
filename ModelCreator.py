from typing import List, Tuple
import base64
import cv2
import dlib
import os
import numpy as np
from scipy.spatial import Delaunay
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import plotly.graph_objs as go
import pandas as pd
from scipy.spatial import Delaunay
import plotly.figure_factory as ff
import pandas as pd

from Visualizer import *
from LandmarksProvider import *
from RoiCreator import *
from Merger import *

class ModelCreator:
    def __init__(self):
        self.predictor = dlib.shape_predictor("shape_predictor_68_face_landmarks.dat")

    def create_model(self, image_paths: List[str]) -> List[str]:
        predictor_path = "./shape_predictor_68_face_landmarks.dat"
        result_files = [] 
        image_front_path = image_paths[0]
        oimage = cv2.imread(image_front_path)
        image2 = cv2.imread(image_front_path)

        image = resize_image(oimage, 185)
        imag2 = resize_image(oimage, 185)
        front_landmarks, color = extract_dlib_landmarks(image, predictor_path)

        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        img_gaussian = cv2.GaussianBlur(gray,(3,3),0)
        canny_points = generate_canny_points(img_gaussian)
        prewitt_points = prewitt(img_gaussian, 255)
        merged_coords_new = merge_coordinates(canny_points, prewitt_points)

        left_image = cv2.imread(image_paths[1])
        left_image = resize_image(left_image, 650)
        right_image = cv2.imread(image_paths[2])
        right_image = resize_image(right_image, 650)

        left_landmarks, ignore = extract_dlib_landmarks(left_image, predictor_path)
        right_landmarks, ignore = extract_dlib_landmarks(right_image, predictor_path)

        corresponding_points = perform_feature_correspondence(left_landmarks, right_landmarks)
        disparity_model = create_disparity_map(corresponding_points)
        sixtyEightLandmarks3D = merge_dlib_coordinates(front_landmarks, disparity_model)

        left_eye_roi = create_left_eye_roi(image, front_landmarks)
        right_eye_roi = create_right_eye_roi(image, front_landmarks)
        nose_roi = create_nose_roi(image, front_landmarks)
        mouth_roi = create_mouth_roi(image, front_landmarks)
        face_roi = create_face_roi(image, front_landmarks)
        left_eyebrow_roi = create_left_eyebrow_roi(image, front_landmarks)
        right_eyebrow_roi = create_right_eyebrow_roi(image, front_landmarks)
        featureless_roi = get_featureless_roi(left_eye_roi, right_eye_roi, mouth_roi, nose_roi, face_roi, left_eyebrow_roi, right_eyebrow_roi)
        roi_array = [left_eye_roi, right_eye_roi, nose_roi, mouth_roi, featureless_roi, face_roi, left_eyebrow_roi, right_eyebrow_roi]

        final_result = merge_dlib3d_with_new(sixtyEightLandmarks3D, merged_coords_new, roi_array, image2)
        final_result = scale(final_result)

        #visualise_3d_coordinates(final_result)
        write_obj_file(final_result, "output.obj", color, left_eye_roi, right_eye_roi)
        return "output.obj"
