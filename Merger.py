from RoiCreator import * 


#function to merge canny and prewitt coordinates
#returns a set of x,y coordinates
def merge_coordinates(canny_points, prewitt_points):
    merged_coordinates = []
    merged_coordinates.extend(canny_points)
    merged_coordinates.extend(prewitt_points)
    return merged_coordinates

#function to perform feature correspondence between 2 images of the same face
#where the 2 images are offcentered
#returns a set of 2 sets of x,y coordinates
# i.e      [     [[xl1,yl1],[xr1,yr1]] , [[xl2,yl2],[xr2,yr2]]    ]
def perform_feature_correspondence(left_landmarks, right_landmarks):
    corresponding_points = []
    left_landmarks = left_landmarks[0]
    right_landmarks = right_landmarks[0]
    for i in range(81):
      coordinatesLeft = left_landmarks[i]
      coordinatesRight = right_landmarks[i]
      currentIndex = [[coordinatesLeft[0], coordinatesLeft[1]], [coordinatesRight[0], coordinatesRight[1]]]
      corresponding_points.append(currentIndex)
    return corresponding_points

#creates a rudimentary disparity map using the formula in the research paper
#returns a set of z coordinates
def create_disparity_map(corresponding_points):
    disparity_model = []
    for points in corresponding_points:
        left_x = points[0][0]
        left_y = points[0][1]
        right_x = points[1][0]
        right_y = points[1][1]
        disparity = np.sqrt((left_x - right_x)**2 + (left_y - right_y)**2)
        disparity_model.append(disparity)

    return disparity_model

#this function will merge xy coordinates with the z coordinates from create_disparity_map
#returns a set of x,y,z coordinates
def merge_dlib_coordinates(xy_coordinates, z_coordinates):
    merged_coordinates = []
    xy_coordinates = xy_coordinates[0]
    for i in range(81):
      x_coordinate = xy_coordinates[i][0]
      y_coordinate = xy_coordinates[i][1]
      weight = 1
      if 36 <= i <= 39 or 42 <= i <= 47:
          weight = 0.98
      elif i == 27:
          weight = 1.005
      elif i == 28:
          weight = 1.006
      elif i == 29:
          weight = 1.009
      elif i == 30:
          weight = 1.040
      elif i == 31 or i == 35:
          weight = 1.014
      elif i == 32 or i == 34:
          weight = 1.06
      elif i == 33:
          weight = 1.070
      elif 48 <= i <= 60:
          weight = 1.030
      elif 61 <= i <= 67:
          weight = 1.015
      elif 70 <= i <= 74:
          weight = 1.009
      elif 75 <= i <= 80 or 17 <= i <= 26:
          weight = 1.020
      z_coordinate = z_coordinates[i] * weight
      currentIndex = [x_coordinate, y_coordinate, z_coordinate]
      merged_coordinates.append(currentIndex)
    #here
    first_index = merged_coordinates[0]
    last_index = merged_coordinates[16]
    #handle extra coordinates
    merged_coordinates.append([first_index[0], first_index[1] * 1.1 , first_index[2]])
    merged_coordinates.append([last_index[0], last_index[1] * 1.1 , last_index[2]])
    return merged_coordinates

#indices of roi_array:
#0: left eye, 1: right eye, 2: nose, 3: mouth, 4: featureless face (without eyes nose mouth etc...), 5: face all,
#6: left eyebrow, 7: right eyebrow
def merge_dlib3d_with_new(sixtyEightLandmarks3D, new_points, roi_array, image):
    new_points_3d = []
    for point in new_points:
        if not is_inside_roi(point[0], point[1], roi_array[5]):
            continue
        elif is_inside_roi(point[0], point[1], roi_array[0] ): #if inside left eye
            z_depth = get_closest_dlib_landmark(sixtyEightLandmarks3D, point)
            new_points_3d.append([point[0], point[1], z_depth])
        elif is_inside_roi(point[0], point[1], roi_array[1]): #if inside right eye
            z_depth = get_closest_dlib_landmark(sixtyEightLandmarks3D, point)
            new_points_3d.append([point[0], point[1], z_depth])
        # elif is_inside_roi(point[0], point[1], roi_array[2]): #if inside nose
        #     z_depth = get_closest_dlib_landmark(sixtyEightLandmarks3D, point)
        #     new_points_3d.append([point[0], point[1], z_depth])
        elif is_inside_roi(point[0], point[1], roi_array[3]): #if inside mouth
            z_depth = get_closest_dlib_landmark(sixtyEightLandmarks3D, point)
            new_points_3d.append([point[0], point[1], z_depth])
        elif is_inside_roi(point[0], point[1], roi_array[6]): #if inside left eyebrow
            z_depth = get_closest_dlib_landmark(sixtyEightLandmarks3D, point)
            new_points_3d.append([point[0], point[1], z_depth])
        elif is_inside_roi(point[0], point[1], roi_array[7]): #if inside right eyebrow
            z_depth = get_closest_dlib_landmark(sixtyEightLandmarks3D, point)
            new_points_3d.append([point[0], point[1], z_depth])

    merged_result = sixtyEightLandmarks3D[:]
    merged_result.extend(new_points_3d)
    return merged_result

def scale(final_result):
    vertices = np.array(final_result)
    #vertices[:, [1, 2]] = vertices[:, [2, 1]]
    vertices[:, 0] /= 12
    vertices[:, 1] /= -13
    vertices[:, 2] /= -12
    vertices[:, 0] *= 1.15
    vertices[:, 1] *= 1.15
    vertices[:, 2] *= 1.15
    vertices[:, 2] += 48 #53
    vertices[:, 0] -= 7 #9
    vertices[:, 1] -= -16.5
    vertices[:,2] -= 30
    vertices[:,1] += 30
    vertices[:,1] += 5
    vertices[:,0] -= 2
    vertices[:,2] += 1.3
    vertices[:,1] -= 8
    vertices[:, 1] += 5
    return vertices


def get_closest_dlib_landmark(dlib_array, point):
  min_distance = float('inf')
  closest_point = float('inf')
  closest_landmark = None
  for landmark in dlib_array:
    distance = (landmark[0] - point[0])**2 + (landmark[1] - point[1])**2
    if distance < min_distance and distance < closest_point:
        closest_point = distance
        closest_landmark = landmark
  return closest_landmark[2]
