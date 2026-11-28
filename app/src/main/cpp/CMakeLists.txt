cmake_minimum_required(VERSION 3.22.1)
project("keyboardsynth")

add_library(keyboardsynth SHARED keyboard_jni.cpp)
target_compile_definitions(keyboardsynth PRIVATE TSF_IMPLEMENTATION)
target_include_directories(keyboardsynth PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
target_link_libraries(keyboardsynth log android)