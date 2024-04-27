# Project Overview
Our project revolves around developing and deploying a robust API tailored to tackle critical challenges in facial analysis and visualization. The API functions as the backbone of our system, orchestrating seamless communication between disparate components and facilitating efficient data processing.

## Methodology of Face Detection
- **Data Combination:**
  - We blend synthetic datasets with real-world data to create a robust training set for our face detection model.
  - This combination ensures that our model can generalize well across various scenarios and demographics.
- **Model Comparison:**
  - We rigorously compare our existing data model with a new one trained on ethical data.
  - This comparison allows us to assess the accuracy and efficacy of the new model in real-world applications.

## Face Detection Process
- **Feature Extraction:**
  - We utilize various techniques such as canny, prewitt, and dlib shape predictor to extract features from the input images.
- **Coordinate Merging:**
  - The extracted features are then merged to generate comprehensive coordinate data representing facial landmarks.
- **Depth Estimation:**
  - We employ stereo vision techniques to estimate the depth of facial features, enhancing the spatial understanding of the face.
- **3D Face Registration:**
  - The merged coordinates are utilized in Delaunay triangulation to reconstruct the 3D structure of the face, enabling accurate facial analysis and visualization.

## Future Work
- **Smoothing of the Face:**
  - Enhancing the visual quality of the reconstructed face by implementing smoothing techniques.
- **Improving Depth Estimation:**
  - Refining our depth estimation algorithms to achieve higher accuracy and robustness.
- **Implementation of Additional Features:**
  - Integrating additional features into our API to expand its functionality and utility in facial analysis applications.

## Integration
- It's important to note that our API needs to be hosted locally and connected to the app using the correct URL. Please ensure to modify the URL in the Android files accordingly. Additionally, the API branch contains a 'requirements.txt' file that includes all the libraries and dependencies necessary for the proper functioning of the API. Before hosting the API locally, ensure that you have installed all the libraries listed in the 'requirements.txt' file. This ensures seamless integration and communication between the app and the API, allowing users to leverage its capabilities effectively.

## FastAPI Swagger User Interface 

![Screenshot 2024-04-27 121214](https://github.com/farhatrkhalil/Voxel-Visage/assets/100374222/2b863659-c963-4eff-95d3-c2961e9b699c)
