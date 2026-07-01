cmake_minimum_required(VERSION 3.10)
project(DriderXServer)
add_executable(driderx_server src/main.cpp)
target_link_libraries(driderx_server pthread)
