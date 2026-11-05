# The echo canceller JARVIS uses to let the user talk over a reply on the loudspeaker.
cmake_minimum_required(VERSION 3.22.1)
project(shilex_aec C)

add_library(shilex_aec SHARED
    aec_jni.c
    speexdsp/mdf.c
    speexdsp/preprocess.c
    speexdsp/filterbank.c
    speexdsp/fftwrap.c
    speexdsp/kiss_fft.c
    speexdsp/kiss_fftr.c)
target_include_directories(shilex_aec PRIVATE speexdsp speexdsp/include)
target_compile_definitions(shilex_aec PRIVATE HAVE_CONFIG_H)
target_compile_options(shilex_aec PRIVATE -O2 -ffast-math -Wno-unused-parameter)
target_link_libraries(shilex_aec m)
# 16 KB pages (newer Android versions require it for native libraries).
target_link_options(shilex_aec PRIVATE "-Wl,-z,max-page-size=16384")
