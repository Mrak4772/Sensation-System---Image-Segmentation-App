import tensorflow as tf

# Load the TensorFlow Lite model
model_path = "C:/Users/MrAk47724/AndroidStudioProjects/SensationSystem2/app/src/main/assets/segmentation_model.tflite"
# Load the TFLite model interpreter
interpreter = tf.lite.Interpreter(model_path=model_path)
interpreter.allocate_tensors()

# Get input and output details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

print("Input Details:", input_details)
print("Output Details:", output_details)
