LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := mirrorcoach
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS := \
	-llog \
	-landroid \
	-ljnigraphics \

LOCAL_SRC_FILES := \
	/Users/michal/repos/sobel1/app/src/main/jni/Android.mk \
	/Users/michal/repos/sobel1/app/src/main/jni/Application.mk \
	/Users/michal/repos/sobel1/app/src/main/jni/native.c \

LOCAL_C_INCLUDES += /Users/michal/repos/sobel1/app/src/main/jni
LOCAL_C_INCLUDES += /Users/michal/repos/sobel1/app/src/debug/jni

include $(BUILD_SHARED_LIBRARY)
