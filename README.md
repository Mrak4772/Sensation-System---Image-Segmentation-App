# Sensation System - Image Segmentation App

## Overview
Sensation System is an Android application that captures images using the device's camera, processes them through a TensorFlow Lite model for image segmentation, and displays the segmented result. The app also provides options to save and share the segmented image. This project is a demonstration of integrating machine learning with Android for practical use cases.

## Features
- Capture images using the device camera.
- Process images using a TensorFlow Lite segmentation model.
- Display segmented results in a dedicated activity.
- Save the segmented images locally.
- Placeholder functionality for sharing the segmented images.

## Technologies Used
- **Android Jetpack**: CameraX for camera functionality.
- **TensorFlow Lite**: For running the segmentation model.
- **Kotlin**: The primary programming language.
- **XML**: For designing the user interface.

## Requirements
- Android Studio Arctic Fox or later.
- A physical or virtual Android device with camera support.
- TensorFlow Lite segmentation model (`segmentation_model.tflite`).

## Setup Instructions
1. Clone this repository:
   ```bash
   git clone https://github.com/your_username/sensation-system.git
   ```
2. Open the project in Android Studio.
3. Place the TensorFlow Lite model (`segmentation_model.tflite`) in the `assets` folder.
4. Build the project and run it on a device with camera support.

## App Flow
1. **MainActivity**:
   - Captures the image.
   - Processes the image using TensorFlow Lite for segmentation.
   - Stores the segmented result.
   - Provides a button to navigate to the result screen.

2. **ResultActivity**:
   - Displays the segmented image.
   - Offers options to save and share the image.

## Screenshots
### Capture Screen
![Capture Screen](path/to/capture_screen.png)

### Segmentation Result Screen
![Result Screen](path/to/result_screen.png)

## Future Enhancements
- **Improved Sharing**: Add full sharing functionality for segmented images.
- **Dynamic Model Loading**: Allow users to choose or update models dynamically.
- **Additional Metrics**: Display confidence scores or class names alongside the segmented image.

## Contributing
Contributions are welcome! To contribute:
1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Submit a pull request with detailed information about your changes.

## License
This project is licensed under the [MIT License](LICENSE).

## Contact
For questions or suggestions, feel free to contact me:
- **Email**: your_email@example.com
- **GitHub**: [your_username](https://github.com/your_username)

---

Thank you for checking out Sensation System! If you find this project helpful or interesting, please give it a ⭐ on GitHub!

